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
import common.Strings
import common.Strings.{ProtectionTypeUrl, StatusUrl}
import config._
import connectors.PlaConnectorError.{ConflictResponseError, IncorrectResponseBodyError, LockedResponseError}
import connectors.{CitizenDetailsConnector, PlaConnector}
import constructors.display.DisplayConstructors
import enums.ApplicationType
import mocks.AuthMock
import models.amend.{AmendProtectionModel, AmendsGAModel}
import models.cache.CacheMap
import models.display.{AmendDisplayModel, AmendDisplayRowModel, AmendDisplaySectionModel}
import models.pla.AmendableProtectionType
import models.pla.response.ProtectionStatus.{Dormant, Open}
import models.pla.response.ProtectionType.{FixedProtection2016, IndividualProtection2014, IndividualProtection2016}
import models.pla.response.{
  AmendProtectionResponse,
  AmendProtectionResponseStatus,
  ProtectionRecord,
  ProtectionRecordsList,
  ReadProtectionsResponse
}
import models.{AmendResponseModel, Person, PersonalDetailsModel, ProtectionModel}
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
import testdata.AmendProtectionOutcomeViewsTestData.amendResultDisplayModelIP14
import testdata.PlaConnectorTestData.amendProtectionResponse
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import utils.Constants
import views.html.pages.amends._
import views.html.pages.fallback.technicalError
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
  private val plaConnector: PlaConnector                       = mock[PlaConnector]
  private val appConfig: FrontendAppConfig                     = mock[FrontendAppConfig]

  private val messagesControllerComponents: MessagesControllerComponents =
    inject[MessagesControllerComponents]

  private val manualCorrespondenceNeededView: manualCorrespondenceNeeded     = mock[manualCorrespondenceNeeded]
  private val technicalErrorView: technicalError                             = mock[technicalError]
  private val amendOutcomeView: amendOutcome                                 = mock[amendOutcome]
  private val amendOutcomeNoNotificationIdView: amendOutcomeNoNotificationId = mock[amendOutcomeNoNotificationId]
  private val amendSummaryView: amendSummary                                 = mock[amendSummary]

  override val messagesApi: MessagesApi = messagesControllerComponents.messagesApi

  private val ec: ExecutionContext = inject[ExecutionContext]

  private val authFunction = new AuthFunctionImpl(
    messagesControllerComponents,
    mockAuthConnector,
    technicalErrorView
  )(appConfig, ec)

  private val controller = new AmendsController(
    sessionCacheService = sessionCacheService,
    citizenDetailsConnector = citizenDetailsConnector,
    plaConnector = plaConnector,
    displayConstructors = displayConstructors,
    mcc = messagesControllerComponents,
    authFunction = authFunction,
    manualCorrespondenceNeeded = manualCorrespondenceNeededView,
    technicalError = technicalErrorView,
    amendOutcome = amendOutcomeView,
    amendOutcomeNoNotificationId = amendOutcomeNoNotificationIdView,
    amendSummary = amendSummaryView
  )(ec)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(sessionCacheService)
    reset(citizenDetailsConnector)
    reset(plaConnector)
    reset(plaConnector)
    reset(displayConstructors)
    reset(mockAuthConnector)
    reset(manualCorrespondenceNeededView)
    reset(technicalErrorView)
    reset(amendOutcomeView)
    reset(amendOutcomeNoNotificationIdView)
    reset(amendSummaryView)
    reset(appConfig)

    mockAuthRetrieval[Option[String]](Retrievals.nino, Some(testNino))
    when(manualCorrespondenceNeededView.apply()(any(), any())).thenReturn(HtmlFormat.empty)
    when(technicalErrorView.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(amendOutcomeView.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(amendOutcomeNoNotificationIdView.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
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
    identifier = Some(12345),
    protectionType = Some(IndividualProtection2014.toString),
    status = Some(Dormant.toString),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456")
  )

  private val psaCheckReference              = "PSA12345678A"
  private val testAmendIP2014ProtectionModel = AmendProtectionModel(ip2014Protection, ip2014Protection)

  private val testPensionContributionNoPsoDisplaySections = Seq(
    AmendDisplaySectionModel(
      "OverseasPensions",
      Seq(
        AmendDisplayRowModel(
          "YesNo",
          Some(
            controllers.routes.AmendsOverseasPensionController
              .amendOverseasPensions(Strings.ProtectionTypeUrl.IndividualProtection2014, "active")
          ),
          None,
          "Yes"
        ),
        AmendDisplayRowModel(
          "Amt",
          Some(
            controllers.routes.AmendsOverseasPensionController
              .amendOverseasPensions(Strings.ProtectionTypeUrl.IndividualProtection2014, "active")
          ),
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
          Some(
            controllers.routes.AmendsCurrentPensionController
              .amendCurrentPensions(Strings.ProtectionTypeUrl.IndividualProtection2014, "active")
          ),
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
    protectionType = IndividualProtection2014.toString,
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

      val result = controller.amendsSummary(
        Strings.ProtectionTypeUrl.IndividualProtection2016,
        Strings.StatusUrl.Open
      )(fakeRequest)

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

    "PlaConnector returns a valid response should redirect to amendmentOutcome" in {
      cacheFetchCondition[AmendProtectionModel](anyString())(Some(testAmendIP2014ProtectionModel))
      when(plaConnector.amendProtection(any(), any())(any(), any()))
        .thenReturn(Future.successful(Right(amendProtectionResponse)))
      when(sessionCacheService.saveFormData(anyString(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("cacheId", Map.empty)))

      val result =
        controller.amendProtection(ProtectionTypeUrl.IndividualProtection2014, StatusUrl.Dormant)(fakeRequest)

      status(result) shouldBe 303
      redirectLocation(result) shouldBe Some(s"${routes.AmendsController.amendmentOutcome}")
    }

    "PlaConnector returns LockedResponseError should return Locked response and manual correspondence page" in {
      cacheFetchCondition[AmendProtectionModel](anyString())(Some(testAmendIP2014ProtectionModel))
      when(sessionCacheService.saveFormData(anyString(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("GA", Map.empty)))
      when(plaConnector.amendProtection(any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(LockedResponseError)))

      val result =
        controller.amendProtection(ProtectionTypeUrl.IndividualProtection2014, StatusUrl.Dormant)(fakeRequest)

      status(result) shouldBe 423
      verify(manualCorrespondenceNeededView).apply()(any(), any())
    }

    "PlaConnector returns ConflictResponseError should return InternalServerError and technical error page" in {
      cacheFetchCondition[AmendProtectionModel](anyString())(Some(testAmendIP2014ProtectionModel))
      when(sessionCacheService.saveFormData(anyString(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("GA", Map.empty)))
      when(plaConnector.amendProtection(any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(ConflictResponseError)))

      val result =
        controller.amendProtection(ProtectionTypeUrl.IndividualProtection2014, StatusUrl.Dormant)(fakeRequest)

      status(result) shouldBe 500
      verify(technicalErrorView).apply(eqTo(ApplicationType.existingProtections.toString))(any(), any())
    }

    "PlaConnector returns IncorrectResponseBodyError should return InternalServerError and technical error page" in {
      cacheFetchCondition[AmendProtectionModel](anyString())(Some(testAmendIP2014ProtectionModel))
      when(sessionCacheService.saveFormData(anyString(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("GA", Map.empty)))
      when(plaConnector.amendProtection(any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(IncorrectResponseBodyError)))

      val result = controller.amendProtection(ProtectionTypeUrl.IndividualProtection2014, "invalidstatus")(fakeRequest)

      status(result) shouldBe 500
      verify(technicalErrorView).apply(eqTo(ApplicationType.existingProtections.toString))(any(), any())
    }

    "PlaConnector returns a response with no notificationId" should {
      "redirect to amendment outcome page" in {
        val response = AmendProtectionResponse(
          lifetimeAllowanceIdentifier = 1,
          lifetimeAllowanceSequenceNumber = 1,
          lifetimeAllowanceType = AmendableProtectionType.IndividualProtection2014,
          certificateDate = Some("2025-10-28"),
          certificateTime = Some("093820"),
          status = AmendProtectionResponseStatus.Open,
          protectionReference = Some("psaRef"),
          relevantAmount = 1_350_000,
          preADayPensionInPaymentAmount = 375_000,
          postADayBenefitCrystallisationEventAmount = 375_000,
          uncrystallisedRightsAmount = 375_000,
          nonUKRightsAmount = 375_000,
          pensionDebitAmount = None,
          pensionDebitEnteredAmount = None,
          notificationIdentifier = None,
          protectedAmount = Some(1_350_000),
          pensionDebitStartDate = None,
          pensionDebitTotalAmount = Some(150_000)
        )

        cacheFetchCondition[AmendProtectionModel](anyString())(Some(testAmendIP2014ProtectionModel))
        when(plaConnector.amendProtection(any(), any())(any(), any()))
          .thenReturn(Future.successful(Right(response)))
        when(sessionCacheService.saveFormData(anyString(), any())(any(), any()))
          .thenReturn(Future.successful(CacheMap("GA", Map.empty)))

        val result =
          controller.amendProtection(ProtectionTypeUrl.IndividualProtection2014, StatusUrl.Dormant)(fakeRequest)

        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some(routes.AmendsController.amendmentOutcome.toString)
      }
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

    Constants.amendmentCodesList.diff(Constants.fixedProtectionNotificationIds).foreach { notificationId =>
      s"AmendResponseModel stored in cache contains notification ID: $notificationId" should {
        "return Ok status with amendOutcome view" in {
          val amendResponseModel =
            AmendResponseModel(ProtectionModel(Some("psaRef"), Some(12345), notificationId = Some(notificationId)))
          cacheFetchCondition(eqTo("amendResponseModel"))(Some(amendResponseModel))
          cacheFetchCondition(eqTo("AmendsGA"))(Some(emptyAmendsGAModel))
          when(citizenDetailsConnector.getPersonDetails(anyString())(any()))
            .thenReturn(Future.successful(Some(testPersonalDetails)))
          when(sessionCacheService.saveFormData(any(), any())(any(), any()))
            .thenReturn(Future.successful(CacheMap("", Map.empty)))
          val amendResultDisplayModel = amendResultDisplayModelIP14.copy(notificationId = notificationId)
          when(displayConstructors.createAmendOutcomeDisplayModel(any(), any(), anyString())(any()))
            .thenReturn(amendResultDisplayModel)

          val result = controller.amendmentOutcome()(fakeRequest)

          status(result) shouldBe OK
          verify(sessionCacheService)
            .saveFormData(eqTo("openProtection"), eqTo(amendResponseModel.protection))(any(), any())
          verify(amendOutcomeView).apply(eqTo(amendResultDisplayModel))(any(), any())
        }
      }
    }

    Constants.fixedProtectionNotificationIds.foreach { notificationId =>
      s"AmendResponseModel stored in cache contains notification ID: $notificationId" should {

        val amendResponseModel =
          AmendResponseModel(ProtectionModel(Some(psaCheckReference), Some(2), notificationId = Some(notificationId)))

        val individualProtectionRecord = ProtectionRecord(
          identifier = 2,
          sequenceNumber = 2,
          `type` = IndividualProtection2016,
          certificateDate = "2016-09-04",
          certificateTime = "090019",
          status = Dormant,
          protectionReference = Some("IP16123001"),
          relevantAmount = Some(1000001),
          preADayPensionInPaymentAmount = Some(3000),
          postADayBenefitCrystallisationEventAmount = Some(100),
          uncrystallisedRightsAmount = Some(100),
          nonUKRightsAmount = Some(100),
          pensionDebitAmount = Some(100),
          pensionDebitEnteredAmount = Some(100),
          protectedAmount = Some(100000),
          pensionDebitStartDate = Some("2016-09-04"),
          pensionDebitTotalAmount = Some(10000),
          lumpSumAmount = None,
          lumpSumPercentage = None,
          enhancementFactor = Some(5.6)
        )
        val fixedProtectionRecord = individualProtectionRecord.copy(
          `type` = FixedProtection2016,
          status = Open,
          protectionReference = Some("FP16123456")
        )

        val readProtectionResponseModel = {
          val fixedProtectionRecordsList      = ProtectionRecordsList(fixedProtectionRecord, None)
          val individualProtectionRecordsList = ProtectionRecordsList(individualProtectionRecord, None)

          ReadProtectionsResponse(
            psaCheckReference,
            Some(Seq(fixedProtectionRecordsList, individualProtectionRecordsList))
          )
        }

        "call PLAConnector" in {
          cacheFetchCondition(eqTo("amendResponseModel"))(Some(amendResponseModel))
          cacheFetchCondition(eqTo("AmendsGA"))(Some(emptyAmendsGAModel))
          when(citizenDetailsConnector.getPersonDetails(anyString())(any()))
            .thenReturn(Future.successful(Some(testPersonalDetails)))
          when(plaConnector.readProtections(any())(any(), any()))
            .thenReturn(Future.successful(Right(readProtectionResponseModel)))
          when(sessionCacheService.saveFormData(any(), any())(any(), any()))
            .thenReturn(Future.successful(CacheMap("", Map.empty)))
          val amendResultDisplayModel = amendResultDisplayModelIP14.copy(notificationId = notificationId)
          when(displayConstructors.createAmendOutcomeDisplayModel(any(), any(), anyString())(any()))
            .thenReturn(amendResultDisplayModel)

          controller.amendmentOutcome()(fakeRequest).futureValue

          verify(plaConnector).readProtections(eqTo(testNino))(any(), any())
        }

        "call SessionCacheService.saveFormData providing correct data" in {
          cacheFetchCondition(eqTo("amendResponseModel"))(Some(amendResponseModel))
          cacheFetchCondition(eqTo("AmendsGA"))(Some(emptyAmendsGAModel))
          when(citizenDetailsConnector.getPersonDetails(anyString())(any()))
            .thenReturn(Future.successful(Some(testPersonalDetails)))
          when(plaConnector.readProtections(any())(any(), any()))
            .thenReturn(Future.successful(Right(readProtectionResponseModel)))
          when(sessionCacheService.saveFormData(any(), any())(any(), any()))
            .thenReturn(Future.successful(CacheMap("", Map.empty)))
          val amendResultDisplayModel = amendResultDisplayModelIP14.copy(notificationId = notificationId)
          when(displayConstructors.createAmendOutcomeDisplayModel(any(), any(), anyString())(any()))
            .thenReturn(amendResultDisplayModel)

          controller.amendmentOutcome()(fakeRequest).futureValue

          val expectedProtectionModel =
            ProtectionModel(psaCheckReference, fixedProtectionRecord).copy(notificationId = Some(notificationId))
          verify(sessionCacheService)
            .saveFormData(eqTo("openProtection"), eqTo(expectedProtectionModel))(any(), any())
        }

        "return Ok status with amendOutcome view" in {
          cacheFetchCondition(eqTo("amendResponseModel"))(Some(amendResponseModel))
          cacheFetchCondition(eqTo("AmendsGA"))(Some(emptyAmendsGAModel))
          when(citizenDetailsConnector.getPersonDetails(anyString())(any()))
            .thenReturn(Future.successful(Some(testPersonalDetails)))
          when(plaConnector.readProtections(any())(any(), any()))
            .thenReturn(Future.successful(Right(readProtectionResponseModel)))
          when(sessionCacheService.saveFormData(any(), any())(any(), any()))
            .thenReturn(Future.successful(CacheMap("", Map.empty)))
          val amendResultDisplayModel = amendResultDisplayModelIP14.copy(notificationId = notificationId)
          when(displayConstructors.createAmendOutcomeDisplayModel(any(), any(), anyString())(any()))
            .thenReturn(amendResultDisplayModel)

          val result = controller.amendmentOutcome()(fakeRequest)

          status(result) shouldBe OK
          verify(amendOutcomeView).apply(eqTo(amendResultDisplayModel))(any(), any())
        }
      }
    }

    "AmendResponseModel stored in cache contains no notification ID" should {
      import testdata.AmendProtectionDisplayModelTestData._

      "return Ok status with amendOutcomeNoNotificationId view" in {
        val amendResponseModel = amendResponseModelNoNotificationIdIndividualProtection2014

        val amendResultDisplayModel = amendResultDisplayModelNoNotificationIdIndividualProtection2014

        cacheFetchCondition(eqTo("amendResponseModel"))(Some(amendResponseModel))
        cacheFetchCondition(eqTo("AmendsGA"))(Some(emptyAmendsGAModel))
        when(citizenDetailsConnector.getPersonDetails(anyString())(any()))
          .thenReturn(Future.successful(Some(testPersonalDetails)))
        when(displayConstructors.createAmendOutcomeDisplayModelNoNotificationId(any(), any(), any())(any()))
          .thenReturn(amendResultDisplayModel)

        val result = controller.amendmentOutcome()(fakeRequest)

        status(result) shouldBe OK
        verify(amendOutcomeNoNotificationIdView).apply(eqTo(amendResultDisplayModel))(any(), any())
      }

    }

  }

}
