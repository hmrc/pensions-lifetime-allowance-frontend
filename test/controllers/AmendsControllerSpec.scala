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

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import auth.{AuthFunction, AuthFunctionImpl, authenticatedFakeRequest}
import common.Exceptions.RequiredValueNotDefinedException
import config._
import connectors.PLAConnector
import constructors.{DisplayConstructors, ResponseConstructors}
import enums.ApplicationType
import mocks.AuthMock
import models._
import models.amendModels._
import org.jsoup.Jsoup
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.Environment
import play.api.http.HeaderNames.CACHE_CONTROL
import play.api.i18n.{I18nSupport, Lang, Messages, MessagesApi}
import play.api.libs.json.{JsNull, Json}
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionCacheService
import testHelpers._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import uk.gov.hmrc.http.HttpResponse
import models.cache.CacheMap
import org.mockito.ArgumentMatchers.{any, anyString, startsWith}
import views.html.pages.amends._
import views.html.pages.fallback.{noNotificationId, technicalError}
import views.html.pages.result.manualCorrespondenceNeeded
import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class AmendsControllerSpec extends FakeApplication
  with MockitoSugar
  with SessionCacheTestHelper
  with BeforeAndAfterEach with AuthMock with I18nSupport {

  implicit lazy val mockMessage = fakeApplication().injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  val mockDisplayConstructors: DisplayConstructors   = mock[DisplayConstructors]
  val mockResponseConstructors: ResponseConstructors = mock[ResponseConstructors]
  val mockSessionCacheService: SessionCacheService       = mock[SessionCacheService]
  val mockPlaConnector: PLAConnector                 = mock[PLAConnector]
  val mockMCC: MessagesControllerComponents          = fakeApplication().injector.instanceOf[MessagesControllerComponents]
  val mockAuthFunction: AuthFunction                 = mock[AuthFunction]
  val mockManualCorrespondenceNeeded: manualCorrespondenceNeeded = app.injector.instanceOf[manualCorrespondenceNeeded]
  val mockNoNotificationID: noNotificationId         = app.injector.instanceOf[noNotificationId]
  val mockAmendPsoDetails: amendPsoDetails           = app.injector.instanceOf[amendPsoDetails]
  val mockTechnicalError: technicalError             = app.injector.instanceOf[technicalError]
  val mockOutcomeActive: outcomeActive                = app.injector.instanceOf[outcomeActive]
  val mockOutcomeInactive: outcomeInactive            = app.injector.instanceOf[outcomeInactive]
  val mockRemovePsoDebits: removePsoDebits            = app.injector.instanceOf[removePsoDebits]
  val mockAmendSummary: amendSummary                  = app.injector.instanceOf[amendSummary]
  val mockEnv: Environment                            = mock[Environment]
  val messagesApi: MessagesApi                        = mockMCC.messagesApi

  implicit val mockAppConfig: FrontendAppConfig = fakeApplication().injector.instanceOf[FrontendAppConfig]
  implicit val mockPlaContext: PlaContext = mock[PlaContext]
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = mock[Materializer]
  implicit val mockLang: Lang = mock[Lang]
  implicit val formWithCSRF: FormWithCSRF = app.injector.instanceOf[FormWithCSRF]
  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  override def beforeEach() = {
    reset(
      mockSessionCacheService,
      mockPlaConnector,
      mockDisplayConstructors,
      mockAuthConnector,
      mockEnv,
      mockResponseConstructors
    )
    super.beforeEach()
  }

  val testIP16DormantModel = AmendProtectionModel(ProtectionModel(None, None), ProtectionModel(None, None, protectionType = Some("IP2016"), status = Some("dormant"), relevantAmount = Some(100000), uncrystallisedRights = Some(100000)))

  class Setup {
    val authFunction = new AuthFunctionImpl (
      mockMCC,
      mockAuthConnector,
      mockTechnicalError
      )
    val rpsoController = new AmendsRemovePensionSharingOrderController(
      mockSessionCacheService,
      mockPlaConnector,
      mockMCC,
      authFunction,
      mockTechnicalError,
      mockRemovePsoDebits
    )
    val controller = new AmendsController(
      mockSessionCacheService,
      mockPlaConnector,
      mockDisplayConstructors,
      mockMCC,
      mockResponseConstructors,
      authFunction,
      mockManualCorrespondenceNeeded,
      mockNoNotificationID,
      mockTechnicalError,
      mockOutcomeActive,
      mockOutcomeInactive,
      mockAmendSummary
    )
  }

  implicit val fakeRequest = FakeRequest()

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
    protectionReference = Some("PSA123456"))

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
    protectionReference = Some("PSA123456"))

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

    AmendDisplaySectionModel("OverseasPensions", Seq(
      AmendDisplayRowModel("YesNo", Some(controllers.routes.AmendsOverseasPensionController.amendOverseasPensions("ip2014", "active")), None, "Yes"),
      AmendDisplayRowModel("Amt", Some(controllers.routes.AmendsOverseasPensionController.amendOverseasPensions("ip2014", "active")), None, "£100,000")
    )
    ),
    AmendDisplaySectionModel("CurrentPensions",Seq(
      AmendDisplayRowModel("Amt", Some(controllers.routes.AmendsCurrentPensionController.amendCurrentPensions("ip2014", "active")), None, "£1,000,000")
    )
    ),
    AmendDisplaySectionModel("CurrentPsos", Seq(
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


  def cacheFetchCondition[T](data: Option[T]): Unit = {
    when(mockSessionCacheService.fetchAndGetFormData[T](anyString())(any(), any()))
      .thenReturn(Future.successful(data))
  }



  "In AmendsController calling the amendsSummary action" when {
    "there is no stored amends model" in new Setup {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](None)

      val result = controller.amendsSummary("ip2016", "open")(fakeRequest)
      val jsoupDoc = Jsoup.parse(contentAsString(result))

      status(result) shouldBe 500

      jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
      jsoupDoc.body.getElementById("tryAgainLink").attr("href") shouldEqual s"${controllers.routes.ReadProtectionsController.currentProtections}"
      await(result).header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache"
    }

    "there is a stored, updated amends model" in new Setup {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))
      when(mockDisplayConstructors.createAmendDisplayModel(any())(any())).thenReturn(tstAmendDisplayModel)

      val result = controller.amendsSummary("ip2014", "dormant")(fakeRequest)
      val jsoupDoc = Jsoup.parse(contentAsString(result))

      status(result) shouldBe 200
      jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.amends.heading.IP2014.changed")
    }
  }



  "Calling the amendProtection action" when {
    "the microservice returns a conflict response" in new Setup {
      object DataItem extends AuthorisedFakeRequestToPost(controller.amendProtection("IP2014", "dormant"))

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))
      when(mockSessionCacheService.saveFormData(anyString(), any())(any(), any())).thenReturn(Future.successful(CacheMap("GA", Map.empty)))
      when(mockPlaConnector.amendProtection(any(), any())(any(), any())).thenReturn(Future.successful(HttpResponse(409, "")))

      status(DataItem.result) shouldBe 500
      DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
      DataItem.jsoupDoc.body.getElementById("tryAgainLink").attr("href") shouldEqual s"${controllers.routes.ReadProtectionsController.currentProtections}"
      await(DataItem.result).header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache"
    }

    "the microservice returns a manual correspondence needed response" in new Setup {
      object DataItem extends AuthorisedFakeRequestToPost(controller.amendProtection("IP2014", "dormant"))

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))
      when(mockSessionCacheService.saveFormData(anyString(), any())(any(), any())).thenReturn(Future.successful(CacheMap("GA", Map.empty)))
      when(mockPlaConnector.amendProtection(any(), any())(any(), any())).thenReturn(Future.successful(HttpResponse(423, "")))

      status(DataItem.result) shouldBe 423
      DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.mcNeeded.pageHeading")
    }
  }

    "the microservice returns an invalid json response" in new Setup {
      lazy val result = controller.amendProtection("IP2014", "invalidstatus")(authenticatedFakeRequest().withMethod("POST"))
      lazy val jsoupDoc = Jsoup.parse(contentAsString(result))


      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))
      when(mockSessionCacheService.saveFormData(anyString(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("GA", Map.empty)))
      when(mockPlaConnector.amendProtection(any(), any())(any(),any()))
        .thenReturn(Future.successful(HttpResponse(status = 200, json = Json.parse("""{"result":"doesNotMatter"}"""), headers = Map.empty)))
      when(mockResponseConstructors.createAmendResponseModelFromJson(any()))
        .thenReturn(None)

      status(result) shouldBe 500
      jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
      jsoupDoc.
        body.getElementById("tryAgainLink")
        .attr("href") shouldEqual
        s"${controllers.routes.ReadProtectionsController.currentProtections}"
      await(result).header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache"
    }

    "the microservice returns a response with no notificationId" in new Setup {
      object DataItem extends AuthorisedFakeRequestToPost(controller.amendProtection("IP2014", "dormant"))

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))

      when(mockPlaConnector.amendProtection(any(), any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(status = 200, json = Json.parse("""{"result":"doesNotMatter"}"""), headers = Map.empty)))
      when(mockResponseConstructors.createAmendResponseModelFromJson(any()))
        .thenReturn(Some(AmendResponseModel(noNotificationIdProtection)))
      when(mockSessionCacheService.saveFormData(anyString(), any())(any(), any())).thenReturn(Future.successful(CacheMap("GA", Map.empty)))


      status(DataItem.result) shouldBe 500
      DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.noNotificationId.title")
      DataItem.jsoupDoc.body.getElementsByClass("govuk-link").get(1).attr("href") shouldEqual s"${controllers.routes.ReadProtectionsController.currentProtections}"
      await(DataItem.result).header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache"
    }

    "the microservice returns a valid response" in new Setup {
      lazy val result = controller.amendProtection("IP2014", "dormant")(authenticatedFakeRequest().withMethod("POST"))
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        cacheFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))

        when(mockPlaConnector.amendProtection(any(), any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(status = 200, json = Json.parse("""{"result":"doesNotMatter"}"""), headers = Map.empty)))
        when(mockResponseConstructors.createAmendResponseModelFromJson(any())).thenReturn(Some(tstActiveAmendResponseModel))
        when(mockSessionCacheService.saveFormData(anyString(), any())(any(), any()))
          .thenReturn(Future.successful(CacheMap("cacheId", Map.empty)))

        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some(s"${routes.AmendsController.amendmentOutcome}")
    }

  "Calling the amendmentOutcome action" when {
    "there is no outcome object stored in cache" in new Setup {
      lazy val result = controller.amendmentOutcome()(fakeRequest)
      lazy val jsoupDoc = Jsoup.parse(contentAsString(result))
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        cacheFetchCondition[AmendResponseModel](None)
        cacheFetchCondition[AmendsGAModel](None)

        status(result) shouldBe 500
        jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
        jsoupDoc.body.getElementById("tryAgainLink").attr("href") shouldEqual s"${controllers.routes.ReadProtectionsController.currentProtections}"
        await(result).header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache"
    }

    "there is an active protection outcome in cache" in new Setup {
      lazy val result = controller.amendmentOutcome()(fakeRequest)
      lazy val jsoupDoc = Jsoup.parse(contentAsString(result))
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        when(mockSessionCacheService.fetchAndGetFormData[AmendResponseModel](startsWith("amendResponseModel"))(any(), any())).thenReturn(Future.successful(Some(tstActiveAmendResponseModel)))
        when(mockSessionCacheService.fetchAndGetFormData[AmendsGAModel](startsWith("AmendsGA"))(any(), any())).thenReturn(Future.successful(Some(AmendsGAModel(Some("updatedValue"),Some("changedToYes"),Some("changedToNo"),None,Some("addedPSO")))))
        when(mockDisplayConstructors.createActiveAmendResponseDisplayModel(any())).thenReturn(tstActiveAmendResponseDisplayModel)

        status(result) shouldBe 200
        jsoupDoc.body.getElementsByClass("govuk-panel__title").text shouldEqual Messages("amendResultCode.33.heading")
      }
    }


    "there is an inactive protection outcome in cache" in new Setup {
      lazy val result = controller.amendmentOutcome()(fakeRequest)
      lazy val jsoupDoc = Jsoup.parse(contentAsString(result))
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        when(mockSessionCacheService.fetchAndGetFormData[AmendResponseModel](startsWith("amendResponseModel"))(any(), any())).thenReturn(Future.successful(Some(tstInactiveAmendResponseModel)))
        when(mockSessionCacheService.fetchAndGetFormData[AmendsGAModel](startsWith("AmendsGA"))(any(), any())).thenReturn(Future.successful(Some(AmendsGAModel(None,Some("changedToNo"),Some("changedToYes"),None,None))))
        when(mockDisplayConstructors.createInactiveAmendResponseDisplayModel(any())).thenReturn(tstInactiveAmendResponseDisplayModel)

        status(result) shouldBe 200
        jsoupDoc.body.getElementById("resultPageHeading").text shouldEqual Messages("amendResultCode.43.heading")
    }

    "Choosing remove with a valid amend protection model" in new Setup {
      val ip2016Protection = ProtectionModel(
        psaCheckReference = Some("testPSARef"),
        uncrystallisedRights = Some(100000.00),
        nonUKRights = Some(2000.00),
        preADayPensionInPayment = Some(2000.00),
        postADayBenefitCrystallisationEvents = Some(2000.00),
        notificationId = Some(12),
        protectionID = Some(12345),
        protectionType = Some("IP2016"),
        status = Some("open"),
        certificateDate = Some("2016-04-17"),
        pensionDebits = Some(List(PensionDebitModel("2016-12-23", 1000.0))),
        protectedAmount = Some(1250000),
        protectionReference = Some("PSA123456"))

      val testAmendIP2016ProtectionModel = AmendProtectionModel(ip2016Protection, ip2016Protection)
      object DataItem extends AuthorisedFakeRequestToPost(rpsoController.submitRemovePso("ip2016", "open"))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        cacheFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
        cacheSaveCondition[AmendProtectionModel](mockSessionCacheService)

      status(DataItem.result) shouldBe 303
        redirectLocation(DataItem.result) shouldBe Some(s"${routes.AmendsController.amendsSummary("ip2016", "open")}")

    }


  "Calling amendmentOutcomeResult" when {

    "provided with no models" in new Setup {
      val appType = ApplicationType.existingProtections
      lazy val result = controller.amendmentOutcomeResult(None, None, "")(fakeRequest)

      status(result) shouldBe INTERNAL_SERVER_ERROR
      contentAsString(result) shouldBe mockTechnicalError(appType.toString).body
    }

    "provided with a model without an Id" in new Setup {
      lazy val result = controller.amendmentOutcomeResult(Some(AmendResponseModel(ProtectionModel(None, None))),
        Some(AmendsGAModel(None, None, None, None, None)), "")(fakeRequest)


        the [RequiredValueNotDefinedException] thrownBy await(result) should have message "Value not found for notificationId in amendmentOutcome"

    }

    "provided with a model with an active amendment code" in new Setup {
      val modelGA = Some(AmendsGAModel(None, None, None, None, None))
      val model = AmendResponseModel(ProtectionModel(Some("ref"), Some(33), notificationId = Some(33)))

      when(mockSessionCacheService.saveFormData(any(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("", Map("" -> JsNull))))

      when(mockDisplayConstructors.createActiveAmendResponseDisplayModel(any()))
        .thenReturn(ActiveAmendResultDisplayModel(ApplicationType.IP2014, "33", "£1,100,000", None))

      lazy val result = controller.amendmentOutcomeResult(Some(model), modelGA, "")

      contentAsString(result) shouldBe mockOutcomeActive(ActiveAmendResultDisplayModel(ApplicationType.IP2014, "33", "£1,100,000", None), modelGA).body
      status(result) shouldBe OK
    }

    "provided with a model with an inactive amendment code" in new Setup {
      val modelGA = Some(AmendsGAModel(None, None, None, None, None))
      val model = AmendResponseModel(ProtectionModel(Some("ref"), Some(1), notificationId = Some(41)))
      lazy val result = controller.amendmentOutcomeResult(Some(model), modelGA, "")

      when(mockSessionCacheService.saveFormData(any(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("", Map("" -> JsNull))))

      when(mockDisplayConstructors.createInactiveAmendResponseDisplayModel(any()))
        .thenReturn(InactiveAmendResultDisplayModel("41", Seq()))

        status(result) shouldBe OK
        contentAsString(result) shouldBe mockOutcomeInactive(InactiveAmendResultDisplayModel("41", Seq()), modelGA).body
    }
  }

  "Calling createPsoDetailsList" when {

    "not supplied with a PSO amount" should {

      "return the correct value not found exception" in new Setup() {
        the [RequiredValueNotDefinedException] thrownBy {
          controller.createPsoDetailsList(AmendPSODetailsModel(LocalDate.of(2017, 3, 1), None))
        } should have message "Value not found for psoAmt in createPsoDetailsList"
      }
    }

    "supplied with a PSO amount" should {

      "return the correct list" in new Setup {
        controller.createPsoDetailsList(AmendPSODetailsModel(LocalDate.of(2017, 3, 1), Some(1))) shouldBe Some(List(PensionDebitModel("2017-03-01", 1)))
      }
    }
  }
}
