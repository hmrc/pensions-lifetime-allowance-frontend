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

import auth.{AuthFunction, AuthFunctionImpl}
import common.Exceptions.RequiredValueNotDefinedException
import config._
import connectors.PlaConnectorError.{ConflictResponseError, IncorrectResponseBodyError, LockedResponseError}
import connectors.{PLAConnector, PlaConnectorV2}
import constructors.DisplayConstructors
import enums.ApplicationType
import mocks.AuthMock
import models._
import models.amendModels._
import models.cache.CacheMap
import models.pla.response.AmendProtectionResponse
import models.pla.{AmendProtectionLifetimeAllowanceType, AmendProtectionResponseStatus}
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.ArgumentMatchers.{any, anyString, startsWith, eq => eqTo}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.Environment
import play.api.http.HeaderNames.CACHE_CONTROL
import play.api.i18n.{I18nSupport, Lang, Messages, MessagesApi}
import play.api.libs.json.JsNull
import play.api.mvc.{AnyContent, MessagesControllerComponents, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionCacheService
import testHelpers._
import testdata.PlaV2TestData.amendProtectionResponse
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
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

  implicit lazy val mockMessage: Messages =
    fakeApplication().injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  val mockDisplayConstructors: DisplayConstructors = mock[DisplayConstructors]
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
  val mockPlaConnector: PLAConnector               = mock[PLAConnector]
  val mockPlaConnectorV2: PlaConnectorV2           = mock[PlaConnectorV2]
  val mockMCC: MessagesControllerComponents        = fakeApplication().injector.instanceOf[MessagesControllerComponents]
  val mockAuthFunction: AuthFunction               = mock[AuthFunction]
  val mockManualCorrespondenceNeeded: manualCorrespondenceNeeded = app.injector.instanceOf[manualCorrespondenceNeeded]
  val mockNoNotificationID: noNotificationId                     = app.injector.instanceOf[noNotificationId]
  val mockAmendPsoDetails: amendPsoDetails                       = app.injector.instanceOf[amendPsoDetails]
  val mockTechnicalError: technicalError                         = app.injector.instanceOf[technicalError]
  val mockOutcomeActive: outcomeActive                           = app.injector.instanceOf[outcomeActive]
  val mockOutcomeInactive: outcomeInactive                       = app.injector.instanceOf[outcomeInactive]
  val mockAmendSummary: amendSummary                             = app.injector.instanceOf[amendSummary]
  val mockEnv: Environment                                       = mock[Environment]
  val messagesApi: MessagesApi                                   = mockMCC.messagesApi

  implicit val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]
  implicit val mockPlaContext: PlaContext       = mock[PlaContext]
  implicit val system: ActorSystem              = ActorSystem()
  implicit val materializer: Materializer       = mock[Materializer]
  implicit val mockLang: Lang                   = mock[Lang]
  implicit val formWithCSRF: FormWithCSRF       = app.injector.instanceOf[FormWithCSRF]
  implicit val ec: ExecutionContext             = app.injector.instanceOf[ExecutionContext]

  override def beforeEach(): Unit = {
    reset(mockSessionCacheService)
    reset(mockPlaConnector)
    reset(mockPlaConnectorV2)
    reset(mockAppConfig)
    reset(mockDisplayConstructors)
    reset(mockAuthConnector)
    reset(mockEnv)
    super.beforeEach()
  }

  val testIP16DormantModel = AmendProtectionModel(
    ProtectionModel(None, None),
    ProtectionModel(
      None,
      None,
      protectionType = Some("IP2016"),
      status = Some("dormant"),
      relevantAmount = Some(100000),
      uncrystallisedRights = Some(100000)
    )
  )

  class Setup {

    val authFunction = new AuthFunctionImpl(
      mockMCC,
      mockAuthConnector,
      mockTechnicalError
    )

    val controller = new AmendsController(
      mockSessionCacheService,
      mockPlaConnector,
      mockPlaConnectorV2,
      mockDisplayConstructors,
      mockMCC,
      authFunction,
      mockManualCorrespondenceNeeded,
      mockNoNotificationID,
      mockTechnicalError,
      mockOutcomeActive,
      mockOutcomeInactive,
      mockAmendSummary
    )

  }

  implicit val fakeRequest: FakeRequest[AnyContent] = FakeRequest()

  private val testNino: String = "AB123456A"

  val ip2016Protection = ProtectionModel(
    psaCheckReference = Some("testPSARef"),
    uncrystallisedRights = Some(100000.00),
    nonUKRights = Some(2000.00),
    preADayPensionInPayment = Some(2000.00),
    postADayBenefitCrystallisationEvents = Some(2000.00),
    notificationId = Some(12),
    protectionID = Some(12345),
    protectionType = Some("IP2016"),
    status = Some("dormant"),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456")
  )

  val testAmendIP2016ProtectionModel = AmendProtectionModel(ip2016Protection, ip2016Protection)

  val ip2014Protection = ProtectionModel(
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

  val testAmendIP2014ProtectionModel = AmendProtectionModel(ip2014Protection, ip2014Protection)

  val noNotificationIdProtection = ProtectionModel(
    psaCheckReference = Some("testPSARef"),
    protectionID = Some(12345),
    uncrystallisedRights = Some(100000.00),
    nonUKRights = Some(0.0),
    preADayPensionInPayment = Some(0.0),
    postADayBenefitCrystallisationEvents = Some(0.0),
    protectionType = Some("IP2014"),
    status = Some("dormant"),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456")
  )

  val tstPensionContributionNoPsoDisplaySections = Seq(
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

  val tstAmendDisplayModel = AmendDisplayModel(
    protectionType = "IP2014",
    amended = true,
    pensionContributionSections = tstPensionContributionNoPsoDisplaySections,
    psoAdded = false,
    psoSections = Seq.empty,
    totalAmount = "£1,100,000"
  )

  val ip2014ActiveAmendmentProtection = ProtectionModel(
    psaCheckReference = Some("psaRef"),
    protectionID = Some(12345),
    notificationId = Some(33)
  )

  val tstActiveAmendResponseModel = AmendResponseModel(ip2014ActiveAmendmentProtection)

  val tstActiveAmendResponseDisplayModel = ActiveAmendResultDisplayModel(
    protectionType = ApplicationType.IP2014,
    notificationId = "33",
    protectedAmount = "£1,100,000",
    details = None
  )

  val ip2016InactiveAmendmentProtection = ProtectionModel(
    psaCheckReference = Some("psaRef"),
    protectionID = Some(12345),
    notificationId = Some(43)
  )

  val tstInactiveAmendResponseModel = AmendResponseModel(ip2016InactiveAmendmentProtection)

  val tstInactiveAmendResponseDisplayModel = InactiveAmendResultDisplayModel(
    notificationId = "43",
    additionalInfo = Seq.empty
  )

  private def cacheFetchCondition[T](data: Option[T]): Unit =
    when(mockSessionCacheService.fetchAndGetFormData[T](anyString())(any(), any()))
      .thenReturn(Future.successful(data))

  private def parseToJsoupDoc(resultF: Future[Result]): Document =
    Jsoup.parse(contentAsString(resultF))

  "In AmendsController calling the amendsSummary action" when {
    "there is no stored amends model" in new Setup {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some(testNino))
      cacheFetchCondition[AmendProtectionModel](None)

      val result   = controller.amendsSummary("ip2016", "open")(fakeRequest)
      val jsoupDoc = Jsoup.parse(contentAsString(result))

      status(result) shouldBe 500

      jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
      jsoupDoc.body
        .getElementById("tryAgainLink")
        .attr("href") shouldEqual s"${controllers.routes.ReadProtectionsController.currentProtections}"
      await(result).header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache"
    }

    "there is a stored, updated amends model" in new Setup {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some(testNino))
      cacheFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))
      when(mockDisplayConstructors.createAmendDisplayModel(any())(any())).thenReturn(tstAmendDisplayModel)

      val result   = controller.amendsSummary("ip2014", "dormant")(fakeRequest)
      val jsoupDoc = Jsoup.parse(contentAsString(result))

      status(result) shouldBe 200
      jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.amends.heading.IP2014.changed")
    }
  }

  "Calling the amendProtection action" when {

    "AppConfig.hipMigrationEnabled is set to true" should {
      "call PlaConnectorV2" in new Setup {
        when(mockAppConfig.hipMigrationEnabled).thenReturn(true)
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some(testNino))
        cacheFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))
        when(mockPlaConnectorV2.amendProtection(any(), any())(any(), any()))
          .thenReturn(Future.successful(Right(amendProtectionResponse)))
        when(mockSessionCacheService.saveFormData(anyString(), any())(any(), any()))
          .thenReturn(Future.successful(CacheMap("cacheId", Map.empty)))

        controller.amendProtection("IP2014", "dormant")(fakeRequest).futureValue

        verify(mockPlaConnectorV2).amendProtection(
          eqTo(testNino),
          eqTo(testAmendIP2014ProtectionModel.updatedProtection)
        )(any(), any())
      }
    }

    "AppConfig.hipMigrationEnabled is set to false" should {
      "call PlaConnector" in new Setup {
        when(mockAppConfig.hipMigrationEnabled).thenReturn(false)
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some(testNino))
        cacheFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))
        when(mockPlaConnector.amendProtection(any(), any())(any(), any()))
          .thenReturn(Future.successful(Right(ProtectionModel(None, None, notificationId = Some(33)))))
        when(mockSessionCacheService.saveFormData(anyString(), any())(any(), any()))
          .thenReturn(Future.successful(CacheMap("cacheId", Map.empty)))

        controller.amendProtection("IP2014", "dormant")(fakeRequest).futureValue

        verify(mockPlaConnector).amendProtection(
          eqTo(testNino),
          eqTo(testAmendIP2014ProtectionModel.updatedProtection)
        )(any(), any())
      }
    }

    "PlaConnector returns a valid response" in new Setup {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some(testNino))
      cacheFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))
      when(mockPlaConnector.amendProtection(any(), any())(any(), any()))
        .thenReturn(Future.successful(Right(ProtectionModel(None, None, notificationId = Some(33)))))
      when(mockSessionCacheService.saveFormData(anyString(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("cacheId", Map.empty)))

      val result = controller.amendProtection("IP2014", "dormant")(fakeRequest)

      status(result) shouldBe 303
      redirectLocation(result) shouldBe Some(s"${routes.AmendsController.amendmentOutcome}")
    }

    "PlaConnector returns LockedResponseError should return Locked response and manual correspondence page" in new Setup {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some(testNino))
      cacheFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))
      when(mockSessionCacheService.saveFormData(anyString(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("GA", Map.empty)))
      when(mockPlaConnector.amendProtection(any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(LockedResponseError)))

      val result = controller.amendProtection("IP2014", "dormant")(fakeRequest)

      status(result) shouldBe 423

      val jsoupDoc = parseToJsoupDoc(result)
      jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.mcNeeded.pageHeading")
    }

    "PlaConnector returns ConflictResponseError should return InternalServerError and technical error page" in new Setup {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some(testNino))
      cacheFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))
      when(mockSessionCacheService.saveFormData(anyString(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("GA", Map.empty)))
      when(mockPlaConnector.amendProtection(any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(ConflictResponseError)))

      val result = controller.amendProtection("IP2014", "dormant")(fakeRequest)

      status(result) shouldBe 500

      val jsoupDoc = parseToJsoupDoc(result)
      jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
      jsoupDoc.body
        .getElementById("tryAgainLink")
        .attr("href") shouldEqual s"${controllers.routes.ReadProtectionsController.currentProtections}"
      await(result).header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache"
    }

    "PlaConnector returns IncorrectResponseBodyError should return InternalServerError and technical error page" in new Setup {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some(testNino))
      cacheFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))
      when(mockSessionCacheService.saveFormData(anyString(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("GA", Map.empty)))
      when(mockPlaConnector.amendProtection(any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(IncorrectResponseBodyError)))

      val result = controller.amendProtection("IP2014", "invalidstatus")(fakeRequest)

      status(result) shouldBe 500

      val jsoupDoc = Jsoup.parse(contentAsString(result))
      jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
      jsoupDoc.body
        .getElementById("tryAgainLink")
        .attr("href") shouldEqual
        s"${controllers.routes.ReadProtectionsController.currentProtections}"
      await(result).header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache"
    }

    "PlaConnector returns a response with no notificationId" in new Setup {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some(testNino))
      cacheFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))
      when(mockPlaConnector.amendProtection(any(), any())(any(), any()))
        .thenReturn(Future.successful(Right(ProtectionModel(None, None))))
      when(mockSessionCacheService.saveFormData(anyString(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("GA", Map.empty)))

      val result = controller.amendProtection("IP2014", "dormant")(fakeRequest)

      status(result) shouldBe 500

      val jsoupDoc = Jsoup.parse(contentAsString(result))
      jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.noNotificationId.title")
      jsoupDoc.body
        .getElementsByClass("govuk-link")
        .get(1)
        .attr("href") shouldEqual s"${controllers.routes.ReadProtectionsController.currentProtections}"
      await(result).header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache"
    }

  }

  "Calling the amendmentOutcome action" when {

    "there is no outcome object stored in cache" in new Setup {
      lazy val result   = controller.amendmentOutcome()(fakeRequest)
      lazy val jsoupDoc = Jsoup.parse(contentAsString(result))
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some(testNino))
      cacheFetchCondition[AmendResponseModel](None)
      cacheFetchCondition[AmendsGAModel](None)

      status(result) shouldBe 500
      jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
      jsoupDoc.body
        .getElementById("tryAgainLink")
        .attr("href") shouldEqual s"${controllers.routes.ReadProtectionsController.currentProtections}"
      await(result).header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache"
    }

    "there is an active protection outcome in cache" in new Setup {
      lazy val result   = controller.amendmentOutcome()(fakeRequest)
      lazy val jsoupDoc = Jsoup.parse(contentAsString(result))
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some(testNino))
      when(
        mockSessionCacheService.fetchAndGetFormData[AmendResponseModel](startsWith("amendResponseModel"))(any(), any())
      ).thenReturn(Future.successful(Some(tstActiveAmendResponseModel)))
      when(mockSessionCacheService.fetchAndGetFormData[AmendsGAModel](startsWith("AmendsGA"))(any(), any())).thenReturn(
        Future.successful(
          Some(AmendsGAModel(Some("updatedValue"), Some("changedToYes"), Some("changedToNo"), None, Some("addedPSO")))
        )
      )
      when(mockDisplayConstructors.createActiveAmendResponseDisplayModel(any()))
        .thenReturn(tstActiveAmendResponseDisplayModel)

      status(result) shouldBe 200
      jsoupDoc.body.getElementsByClass("govuk-panel__title").text shouldEqual Messages("amendResultCode.33.heading")
    }

    "there is an inactive protection outcome in cache" in new Setup {
      lazy val result   = controller.amendmentOutcome()(fakeRequest)
      lazy val jsoupDoc = Jsoup.parse(contentAsString(result))
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some(testNino))
      when(
        mockSessionCacheService.fetchAndGetFormData[AmendResponseModel](startsWith("amendResponseModel"))(any(), any())
      ).thenReturn(Future.successful(Some(tstInactiveAmendResponseModel)))
      when(mockSessionCacheService.fetchAndGetFormData[AmendsGAModel](startsWith("AmendsGA"))(any(), any()))
        .thenReturn(Future.successful(Some(AmendsGAModel(None, Some("changedToNo"), Some("changedToYes"), None, None))))
      when(mockDisplayConstructors.createInactiveAmendResponseDisplayModel(any()))
        .thenReturn(tstInactiveAmendResponseDisplayModel)

      status(result) shouldBe 200
      jsoupDoc.body.getElementById("resultPageHeading").text shouldEqual Messages("amendResultCode.43.heading")
    }
  }

  "Calling amendmentOutcomeResult" when {

    "provided with no models" in new Setup {
      val appType     = ApplicationType.existingProtections
      lazy val result = controller.amendmentOutcomeResult(None, None, "")(fakeRequest)

      status(result) shouldBe INTERNAL_SERVER_ERROR
      contentAsString(result) shouldBe mockTechnicalError(appType.toString).body
    }

    "provided with a model without an Id" in new Setup {
      lazy val result = controller.amendmentOutcomeResult(
        Some(AmendResponseModel(ProtectionModel(None, None))),
        Some(AmendsGAModel(None, None, None, None, None)),
        ""
      )(fakeRequest)

      (the[RequiredValueNotDefinedException] thrownBy await(result) should have)
        .message("Value not found for notificationId in amendmentOutcome")

    }

    "provided with a model with an active amendment code" in new Setup {
      val modelGA = Some(AmendsGAModel(None, None, None, None, None))
      val model   = AmendResponseModel(ProtectionModel(Some("ref"), Some(33), notificationId = Some(33)))

      when(mockSessionCacheService.saveFormData(any(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("", Map("" -> JsNull))))

      when(mockDisplayConstructors.createActiveAmendResponseDisplayModel(any()))
        .thenReturn(ActiveAmendResultDisplayModel(ApplicationType.IP2014, "33", "£1,100,000", None))

      lazy val result = controller.amendmentOutcomeResult(Some(model), modelGA, "")

      contentAsString(result) shouldBe mockOutcomeActive(
        ActiveAmendResultDisplayModel(ApplicationType.IP2014, "33", "£1,100,000", None),
        modelGA
      ).body
      status(result) shouldBe OK
    }

    "provided with a model with an inactive amendment code" in new Setup {
      val modelGA     = Some(AmendsGAModel(None, None, None, None, None))
      val model       = AmendResponseModel(ProtectionModel(Some("ref"), Some(1), notificationId = Some(41)))
      lazy val result = controller.amendmentOutcomeResult(Some(model), modelGA, "")

      when(mockSessionCacheService.saveFormData(any(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("", Map("" -> JsNull))))

      when(mockDisplayConstructors.createInactiveAmendResponseDisplayModel(any()))
        .thenReturn(InactiveAmendResultDisplayModel("41", Seq()))

      status(result) shouldBe OK
      contentAsString(result) shouldBe mockOutcomeInactive(InactiveAmendResultDisplayModel("41", Seq()), modelGA).body
    }
  }

}
