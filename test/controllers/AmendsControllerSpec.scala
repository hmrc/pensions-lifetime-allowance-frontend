/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import auth.AuthFunctionImpl
import common.Exceptions
import config._
import connectors.PlaConnectorError.{ConflictResponseError, IncorrectResponseBodyError, LockedResponseError}
import connectors.{CitizenDetailsConnector, PLAConnector, PlaConnectorV2}
import constructors.DisplayConstructors
import enums.ApplicationType
import enums.ApplicationType.IP2014
import mocks.AuthMock
import models._
import models.amendModels._
import models.cache.CacheMap
import org.mockito.ArgumentMatchers.{any, anyString, eq => eqTo}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.SessionCacheService
import testHelpers._
import testdata.AmendProtectionOutcomeViewsTestData.{amendResultDisplayModelIP14, amendsActiveResultModelIP14}
import testdata.PlaV2TestData.amendProtectionResponse
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import utils.Constants
import views.html.pages.amends._
import views.html.pages.fallback.{noNotificationId, technicalError}
import views.html.pages.result.manualCorrespondenceNeeded

import scala.concurrent.{ExecutionContext, Future}

class AmendsControllerSpec
    extends FakeApplication
    with MockitoSugar
    with SessionCacheTestHelper
    with BeforeAndAfterEach
    with AuthMock
    with ScalaFutures
    with I18nSupport {

  private val displayConstructors: DisplayConstructors         = mock[DisplayConstructors]
  private val citizenDetailsConnector: CitizenDetailsConnector = mock[CitizenDetailsConnector]
  private val sessionCacheService: SessionCacheService         = mock[SessionCacheService]
  private val plaConnector: PLAConnector                       = mock[PLAConnector]
  private val plaConnectorV2: PlaConnectorV2                   = mock[PlaConnectorV2]

  private val messagesControllerComponents: MessagesControllerComponents =
    fakeApplication().injector.instanceOf[MessagesControllerComponents]

  private val manualCorrespondenceNeededView: manualCorrespondenceNeeded = mock[manualCorrespondenceNeeded]
  private val noNotificationIdView: noNotificationId                     = mock[noNotificationId]
  private val technicalErrorView: technicalError                         = mock[technicalError]
  private val outcomeActiveView: outcomeActive                           = mock[outcomeActive]
  private val outcomeInactiveView: outcomeInactive                       = mock[outcomeInactive]
  private val outcomeAmendedView: outcomeAmended                         = mock[outcomeAmended]
  private val amendSummaryView: amendSummary                             = mock[amendSummary]

  override val messagesApi: MessagesApi = messagesControllerComponents.messagesApi

  private implicit val appConfig: FrontendAppConfig = mock[FrontendAppConfig]
  private implicit val plaContext: PlaContext       = mock[PlaContext]
  private implicit val ec: ExecutionContext         = app.injector.instanceOf[ExecutionContext]

  private val authFunction = new AuthFunctionImpl(
    messagesControllerComponents,
    mockAuthConnector,
    technicalErrorView
  )

  private val controller = new AmendsController(
    sessionCacheService,
    citizenDetailsConnector,
    plaConnector,
    plaConnectorV2,
    displayConstructors,
    messagesControllerComponents,
    authFunction,
    manualCorrespondenceNeededView,
    noNotificationIdView,
    technicalErrorView,
    outcomeActiveView,
    outcomeInactiveView,
    outcomeAmendedView,
    amendSummaryView
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(sessionCacheService)
    reset(citizenDetailsConnector)
    reset(plaConnector)
    reset(plaConnectorV2)
    reset(displayConstructors)
    reset(mockAuthConnector)
    reset(manualCorrespondenceNeededView)
    reset(noNotificationIdView)
    reset(technicalErrorView)
    reset(outcomeActiveView)
    reset(outcomeInactiveView)
    reset(outcomeAmendedView)
    reset(amendSummaryView)
    reset(appConfig)

    mockAuthRetrieval[Option[String]](Retrievals.nino, Some(testNino))
    when(manualCorrespondenceNeededView.apply()(any(), any())).thenReturn(HtmlFormat.empty)
    when(noNotificationIdView.apply()(any(), any())).thenReturn(HtmlFormat.empty)
    when(technicalErrorView.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(outcomeActiveView.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(outcomeInactiveView.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(outcomeAmendedView.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(amendSummaryView.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  private implicit val fakeRequest: FakeRequest[AnyContent] = FakeRequest()

  private val testNino: String = "AB123456A"

  private val ip2014Protection = ProtectionModel(
    psaCheckReference = Some("testPSARef"),
    uncrystallisedRights = Some(100000.00),
    nonUKRights = Some(2000.00),
    preADayPensionInPayment = Some(2000.00),
    postADayBenefitCrystallisationEvents = Some(2000.00),
    notificationId = Some(12),
    protectionID = Some(12345),
    protectionType = Some("IP2014"),
    status = Some("dormant"),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456")
  )

  private val testAmendIP2014ProtectionModel = AmendProtectionModel(ip2014Protection, ip2014Protection)

  private val testPensionContributionNoPsoDisplaySections = Seq(
    AmendDisplaySectionModel(
      "OverseasPensions",
      Seq(
        AmendDisplayRowModel(
          "YesNo",
          Some(controllers.routes.AmendsOverseasPensionController.amendOverseasPensions("ip2014", "active")),
          None,
          "Yes"
        ),
        AmendDisplayRowModel(
          "Amt",
          Some(controllers.routes.AmendsOverseasPensionController.amendOverseasPensions("ip2014", "active")),
          None,
          "£100,000"
        )
      )
    ),
    AmendDisplaySectionModel(
      "CurrentPensions",
      Seq(
        AmendDisplayRowModel(
          "Amt",
          Some(controllers.routes.AmendsCurrentPensionController.amendCurrentPensions("ip2014", "active")),
          None,
          "£1,000,000"
        )
      )
    ),
    AmendDisplaySectionModel(
      "CurrentPsos",
      Seq(
        AmendDisplayRowModel("YesNo", None, None, "No")
      )
    )
  )

  private val testAmendDisplayModel = AmendDisplayModel(
    protectionType = "IP2014",
    amended = true,
    pensionContributionSections = testPensionContributionNoPsoDisplaySections,
    psoAdded = false,
    psoSections = Seq.empty,
    totalAmount = "£1,100,000"
  )

  private val testPersonalDetails = PersonalDetailsModel(Person("McTestFace", "Testy"))

  private def cacheFetchCondition[T](key: String)(data: Option[T]): Unit =
    when(sessionCacheService.fetchAndGetFormData[T](key)(any(), any()))
      .thenReturn(Future.successful(data))

  "In AmendsController calling the amendsSummary action" when {

    "there is no stored amends model" in {
      cacheFetchCondition[AmendProtectionModel](anyString())(None)

      val result = controller.amendsSummary("ip2016", "open")(fakeRequest)

      status(result) shouldBe 500
      verify(technicalErrorView).apply(eqTo(ApplicationType.existingProtections.toString))(any(), any())
    }

    "there is a stored, updated amends model" in {
      cacheFetchCondition[AmendProtectionModel](anyString())(Some(testAmendIP2014ProtectionModel))
      when(displayConstructors.createAmendDisplayModel(any())(any())).thenReturn(testAmendDisplayModel)
      val protectionType   = "ip2014"
      val protectionStatus = "dormant"

      val result = controller.amendsSummary(protectionType, protectionStatus)(fakeRequest)

      status(result) shouldBe 200
      verify(amendSummaryView)
        .apply(eqTo(testAmendDisplayModel), eqTo(protectionType), eqTo(protectionStatus))(any(), any())
    }
  }

  "Calling the amendProtection action" when {

    "AppConfig.hipMigrationEnabled is set to true" should {
      "call PlaConnectorV2" in {
        when(appConfig.hipMigrationEnabled).thenReturn(true)
        cacheFetchCondition[AmendProtectionModel](anyString())(Some(testAmendIP2014ProtectionModel))
        when(plaConnectorV2.amendProtection(any(), any())(any(), any()))
          .thenReturn(Future.successful(Right(amendProtectionResponse)))
        when(sessionCacheService.saveFormData(anyString(), any())(any(), any()))
          .thenReturn(Future.successful(CacheMap("cacheId", Map.empty)))

        controller.amendProtection("IP2014", "dormant")(fakeRequest).futureValue

        verify(plaConnectorV2).amendProtection(
          eqTo(testNino),
          eqTo(testAmendIP2014ProtectionModel.updatedProtection)
        )(any(), any())
      }
    }

    "AppConfig.hipMigrationEnabled is set to false" should {
      "call PlaConnector" in {
        when(appConfig.hipMigrationEnabled).thenReturn(false)
        cacheFetchCondition[AmendProtectionModel](anyString())(Some(testAmendIP2014ProtectionModel))
        when(plaConnector.amendProtection(any(), any())(any(), any()))
          .thenReturn(Future.successful(Right(ProtectionModel(None, None, notificationId = Some(33)))))
        when(sessionCacheService.saveFormData(anyString(), any())(any(), any()))
          .thenReturn(Future.successful(CacheMap("cacheId", Map.empty)))

        controller.amendProtection("IP2014", "dormant")(fakeRequest).futureValue

        verify(plaConnector).amendProtection(
          eqTo(testNino),
          eqTo(testAmendIP2014ProtectionModel.updatedProtection)
        )(any(), any())
      }
    }

    "PlaConnector returns a valid response should redirect to amendmentOutcome" in {
      cacheFetchCondition[AmendProtectionModel](anyString())(Some(testAmendIP2014ProtectionModel))
      when(plaConnector.amendProtection(any(), any())(any(), any()))
        .thenReturn(Future.successful(Right(ProtectionModel(None, None, notificationId = Some(33)))))
      when(sessionCacheService.saveFormData(anyString(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("cacheId", Map.empty)))

      val result = controller.amendProtection("IP2014", "dormant")(fakeRequest)

      status(result) shouldBe 303
      redirectLocation(result) shouldBe Some(s"${routes.AmendsController.amendmentOutcome}")
    }

    "PlaConnector returns LockedResponseError should return Locked response and manual correspondence page" in {
      cacheFetchCondition[AmendProtectionModel](anyString())(Some(testAmendIP2014ProtectionModel))
      when(sessionCacheService.saveFormData(anyString(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("GA", Map.empty)))
      when(plaConnector.amendProtection(any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(LockedResponseError)))

      val result = controller.amendProtection("IP2014", "dormant")(fakeRequest)

      status(result) shouldBe 423
      verify(manualCorrespondenceNeededView).apply()(any(), any())
    }

    "PlaConnector returns ConflictResponseError should return InternalServerError and technical error page" in {
      cacheFetchCondition[AmendProtectionModel](anyString())(Some(testAmendIP2014ProtectionModel))
      when(sessionCacheService.saveFormData(anyString(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("GA", Map.empty)))
      when(plaConnector.amendProtection(any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(ConflictResponseError)))

      val result = controller.amendProtection("IP2014", "dormant")(fakeRequest)

      status(result) shouldBe 500
      verify(technicalErrorView).apply(eqTo(ApplicationType.existingProtections.toString))(any(), any())
    }

    "PlaConnector returns IncorrectResponseBodyError should return InternalServerError and technical error page" in {
      cacheFetchCondition[AmendProtectionModel](anyString())(Some(testAmendIP2014ProtectionModel))
      when(sessionCacheService.saveFormData(anyString(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("GA", Map.empty)))
      when(plaConnector.amendProtection(any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(IncorrectResponseBodyError)))

      val result = controller.amendProtection("IP2014", "invalidstatus")(fakeRequest)

      status(result) shouldBe 500
      verify(technicalErrorView).apply(eqTo(ApplicationType.existingProtections.toString))(any(), any())
    }

    "PlaConnector returns a response with no notificationId" in {
      cacheFetchCondition[AmendProtectionModel](anyString())(Some(testAmendIP2014ProtectionModel))
      when(plaConnector.amendProtection(any(), any())(any(), any()))
        .thenReturn(Future.successful(Right(ProtectionModel(None, None))))
      when(sessionCacheService.saveFormData(anyString(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("GA", Map.empty)))

      val result = controller.amendProtection("IP2014", "dormant")(fakeRequest)

      status(result) shouldBe 500
      verify(noNotificationIdView).apply()(any(), any())
    }
  }

  "AmendsController on amendmentOutcome" when {

    val emptyAmendsGAModel: AmendsGAModel = AmendsGAModel(None, None, None, None, None)

    "there is no AmendResponseModel stored in cache" should {
      "return Internal Server Error" in {
        cacheFetchCondition(eqTo("amendResponseModel"))(None)
        cacheFetchCondition(eqTo("AmendsGA"))(Some(emptyAmendsGAModel))
        when(citizenDetailsConnector.getPersonDetails(anyString())(any()))
          .thenReturn(Future.successful(Some(testPersonalDetails)))

        val result = controller.amendmentOutcome()(fakeRequest)

        status(result) shouldBe INTERNAL_SERVER_ERROR
        verify(technicalErrorView).apply(eqTo(ApplicationType.existingProtections.toString))(any(), any())
      }
    }

    "AmendResponseModel stored in cache contains NO notification ID" should {
      "throw exception" in {
        cacheFetchCondition(eqTo("amendResponseModel"))(Some(AmendResponseModel(ProtectionModel(None, None))))
        cacheFetchCondition(eqTo("AmendsGA"))(Some(emptyAmendsGAModel))
        when(citizenDetailsConnector.getPersonDetails(anyString())(any()))
          .thenReturn(Future.successful(Some(testPersonalDetails)))

        val exc = controller.amendmentOutcome()(fakeRequest).failed.futureValue

        exc shouldBe Exceptions.RequiredValueNotDefinedException("amendmentOutcome", "notificationId")
      }
    }

    Constants.amendmentCodesList.foreach { notificationId =>
      s"AmendResponseModel stored in cache contains notification ID: $notificationId" should {
        "return Ok status with outcomeAmended view" in {
          val amendResponseModel =
            AmendResponseModel(ProtectionModel(Some("psaRef"), Some(12345), notificationId = Some(notificationId)))
          cacheFetchCondition(eqTo("amendResponseModel"))(Some(amendResponseModel))
          cacheFetchCondition(eqTo("AmendsGA"))(Some(emptyAmendsGAModel))
          when(citizenDetailsConnector.getPersonDetails(anyString())(any()))
            .thenReturn(Future.successful(Some(testPersonalDetails)))
          when(sessionCacheService.saveFormData(any(), any())(any(), any()))
            .thenReturn(Future.successful(CacheMap("", Map.empty)))

          val amendResultDisplayModel = amendResultDisplayModelIP14.copy(notificationId = notificationId)
          when(displayConstructors.createAmendResultDisplayModel(any(), any(), anyString()))
            .thenReturn(amendResultDisplayModel)

          val result = controller.amendmentOutcome()(fakeRequest)

          status(result) shouldBe OK
          verify(sessionCacheService)
            .saveFormData(eqTo("openProtection"), eqTo(amendResponseModel.protection))(any(), any())
          verify(outcomeAmendedView).apply(eqTo(amendResultDisplayModel))(any(), any())
        }
      }
    }

    Constants.activeAmendmentCodes.diff(Constants.amendmentCodesList).foreach { notificationId =>
      s"AmendResponseModel stored in cache contains notification ID: $notificationId" should {
        "return Ok status with outcomeActive view" in {
          val amendResponseModel =
            AmendResponseModel(ProtectionModel(Some("psaRef"), Some(12345), notificationId = Some(notificationId)))
          cacheFetchCondition(eqTo("amendResponseModel"))(Some(amendResponseModel))
          cacheFetchCondition(eqTo("AmendsGA"))(Some(emptyAmendsGAModel))
          when(citizenDetailsConnector.getPersonDetails(anyString())(any()))
            .thenReturn(Future.successful(Some(testPersonalDetails)))
          when(sessionCacheService.saveFormData(any(), any())(any(), any()))
            .thenReturn(Future.successful(CacheMap("", Map.empty)))

          val activeAmendResultDisplayModel = amendsActiveResultModelIP14.copy(notificationId = notificationId.toString)
          when(displayConstructors.createActiveAmendResponseDisplayModel(any(), any(), any()))
            .thenReturn(activeAmendResultDisplayModel)

          val result = controller.amendmentOutcome()(fakeRequest)

          status(result) shouldBe OK
          verify(sessionCacheService)
            .saveFormData(eqTo("openProtection"), eqTo(amendResponseModel.protection))(any(), any())
          verify(outcomeActiveView)
            .apply(eqTo(activeAmendResultDisplayModel), eqTo(Some(emptyAmendsGAModel)), eqTo(appConfig))(any(), any())
        }
      }
    }

    (25 to 44).diff(Constants.activeAmendmentCodes).foreach { notificationId =>
      s"AmendResponseModel stored in cache contains notification ID: $notificationId" should {
        "return Ok status with outcomeInactive view" in {
          val amendResponseModel =
            AmendResponseModel(ProtectionModel(Some("psaRef"), Some(12345), notificationId = Some(notificationId)))
          cacheFetchCondition(eqTo("amendResponseModel"))(Some(amendResponseModel))
          cacheFetchCondition(eqTo("AmendsGA"))(Some(emptyAmendsGAModel))
          when(citizenDetailsConnector.getPersonDetails(anyString())(any()))
            .thenReturn(Future.successful(Some(testPersonalDetails)))
          when(sessionCacheService.saveFormData(any(), any())(any(), any()))
            .thenReturn(Future.successful(CacheMap("", Map.empty)))

          val inactiveAmendResultDisplayModel = InactiveAmendResultDisplayModel(notificationId, Seq())
          when(displayConstructors.createInactiveAmendResponseDisplayModel(any()))
            .thenReturn(inactiveAmendResultDisplayModel)

          val result = controller.amendmentOutcome()(fakeRequest)

          status(result) shouldBe OK
          verify(sessionCacheService, times(0)).saveFormData(any(), any())(any(), any())
          verify(outcomeInactiveView)
            .apply(eqTo(inactiveAmendResultDisplayModel), eqTo(Some(emptyAmendsGAModel)))(any(), any())
        }
      }
    }
  }

}
