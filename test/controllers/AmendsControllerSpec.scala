/*
 * Copyright 2019 HM Revenue & Customs
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

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import auth.{AuthFunction, AuthFunctionImpl, authenticatedFakeRequest}
import com.kenshoo.play.metrics.PlayModule
import common.Exceptions.RequiredValueNotDefinedException
import config._
import config.wiring.PlaFormPartialRetriever
import connectors.{KeyStoreConnector, PLAConnector}
import constructors.{DisplayConstructors, ResponseConstructors}
import enums.ApplicationType
import forms.{AmendCurrentPensionForm, AmendOverseasPensionsForm, AmendPensionsTakenBeforeForm, AmendPensionsTakenBetweenForm}
import mocks.AuthMock
import models._
import models.amendModels._
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.Environment
import play.api.http.HeaderNames.CACHE_CONTROL
import play.api.i18n.{I18nSupport, Lang, Messages, MessagesApi}
import play.api.libs.json.{JsNull, Json}
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import testHelpers._
import uk.gov.hmrc.auth.core.retrieve.Retrievals
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.pages.amends._
import views.html.pages.fallback.technicalError

import scala.concurrent.Future

class AmendsControllerSpec extends UnitSpec with MockitoSugar with KeystoreTestHelper
                              with BeforeAndAfterEach with AuthMock with WithFakeApplication with I18nSupport {
  override def bindModules = Seq(new PlayModule)

  val mockDisplayConstructors: DisplayConstructors   = mock[DisplayConstructors]
  val mockResponseConstructors: ResponseConstructors = mock[ResponseConstructors]
  val mockKeystoreConnector: KeyStoreConnector       = mock[KeyStoreConnector]
  val mockPlaConnector: PLAConnector                 = mock[PLAConnector]
  val mockMCC: MessagesControllerComponents          = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val mockAuthFunction: AuthFunction                 = mock[AuthFunction]
  val mockEnv: Environment                           = mock[Environment]
  val messagesApi: MessagesApi                       = mockMCC.messagesApi

  implicit val templateRenderer: LocalTemplateRenderer = MockTemplateRenderer.renderer
  implicit val partialRetriever: PlaFormPartialRetriever = mock[PlaFormPartialRetriever]
  implicit val mockAppConfig: FrontendAppConfig = fakeApplication.injector.instanceOf[FrontendAppConfig]
  implicit val mockPlaContext: PlaContext = mock[PlaContext]
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val mockLang: Lang = mock[Lang]

  override def beforeEach(): Unit = {
    reset(
      mockKeystoreConnector,
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
      mockEnv)

    val controller = new AmendsController(
      mockKeystoreConnector,
      mockPlaConnector,
      mockDisplayConstructors,
      mockMCC,
      mockResponseConstructors,
      authFunction
    )
  }

  val sessionId = UUID.randomUUID.toString
  implicit val fakeRequest = FakeRequest()
  val mockUsername = "mockuser"
  val mockUserId = "/auth/oid/" + mockUsername

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


  val ip2016NoDebitProtection = ProtectionModel(
    psaCheckReference = Some("testPSARef"),
    uncrystallisedRights = Some(100000.00),
    nonUKRights = Some(0.0),
    preADayPensionInPayment = Some(0.0),
    postADayBenefitCrystallisationEvents = Some(0.0),
    notificationId = Some(12),
    protectionID = Some(12345),
    protectionType = Some("IP2016"),
    status = Some("dormant"),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456"))
  val testAmendIP2016ProtectionModelWithNoDebit = AmendProtectionModel(ip2016NoDebitProtection, ip2016NoDebitProtection)

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

    AmendDisplaySectionModel("PensionsTakenBefore", Seq(
      AmendDisplayRowModel("YesNo", Some(controllers.routes.AmendsController.amendPensionsTakenBefore("ip2014", "active")), None, "No")
    )
    ),
    AmendDisplaySectionModel("PensionsTakenBetween", Seq(
      AmendDisplayRowModel("YesNo", Some(controllers.routes.AmendsController.amendPensionsTakenBetween("ip2014", "active")), None, "No")
    )
    ),
    AmendDisplaySectionModel("OverseasPensions", Seq(
      AmendDisplayRowModel("YesNo", Some(controllers.routes.AmendsController.amendOverseasPensions("ip2014", "active")), None, "Yes"),
      AmendDisplayRowModel("Amt", Some(controllers.routes.AmendsController.amendOverseasPensions("ip2014", "active")), None, "£100,000")
    )
    ),
    AmendDisplaySectionModel("CurrentPensions",Seq(
      AmendDisplayRowModel("Amt", Some(controllers.routes.AmendsController.amendCurrentPensions("ip2014", "active")), None, "£1,000,000")
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


  def keystoreFetchCondition[T](data: Option[T]): Unit = {
    when(mockKeystoreConnector.fetchAndGetFormData[T](ArgumentMatchers.anyString())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(data))
  }



  "In AmendsController calling the amendsSummary action" when {
    "there is no stored amends model" in new Setup {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      keystoreFetchCondition[AmendProtectionModel](None)

      val result = await(controller.amendsSummary("ip2016", "open")(fakeRequest))
      val jsoupDoc = Jsoup.parse(bodyOf(result))

      status(result) shouldBe 500

      jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
      jsoupDoc.body.getElementById("tryAgainLink").attr("href") shouldEqual s"${controllers.routes.ReadProtectionsController.currentProtections()}"
      result.header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache"
    }

    "there is a stored, updated amends model" in new Setup {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))
      when(mockDisplayConstructors.createAmendDisplayModel(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(tstAmendDisplayModel)

      val result = await(controller.amendsSummary("ip2014", "dormant")(fakeRequest))
      val jsoupDoc = Jsoup.parse(bodyOf(result))

      status(result) shouldBe 200
      jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.amends.heading.IP2014.changed")
    }
  }



  "Calling the amendProtection action" when {
    "the hidden fields in the amendment summary page have not been populated correctly" in new Setup {

      object DataItem extends AuthorisedFakeRequestToPost(controller.amendProtection, ("protectionTypez", "stuff"))

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))


      status(DataItem.result) shouldBe 500
      DataItem.jsoupDoc.body.getElementById("tryAgainLink").attr("href") shouldEqual s"${controllers.routes.ReadProtectionsController.currentProtections()}"
      DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
      DataItem.result.header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache"
    }

    "the microservice returns a conflict response" in new Setup {

      object DataItem extends AuthorisedFakeRequestToPost(controller.amendProtection, ("protectionType", "IP2014"), ("status", "dormant"))

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))
      when(mockKeystoreConnector.saveData(ArgumentMatchers.anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(CacheMap("GA", Map.empty)))
      when(mockPlaConnector.amendProtection(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.successful(HttpResponse(409)))

      status(DataItem.result) shouldBe 500
      DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
      DataItem.jsoupDoc.body.getElementById("tryAgainLink").attr("href") shouldEqual s"${controllers.routes.ReadProtectionsController.currentProtections()}"
      DataItem.result.header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache"
    }

    "the microservice returns a manual correspondence needed response" in new Setup {
      object DataItem extends AuthorisedFakeRequestToPost(controller.amendProtection, ("protectionType", "IP2014"), ("status", "dormant"))

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))
      when(mockKeystoreConnector.saveData(ArgumentMatchers.anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(CacheMap("GA", Map.empty)))
      when(mockPlaConnector.amendProtection(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.successful(HttpResponse(423)))

      status(DataItem.result) shouldBe 423
      DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.mcNeeded.pageHeading")
    }
  }

    "the microservice returns an invalid json response" in new Setup {
      lazy val result = await(controller.amendProtection()(authenticatedFakeRequest().withFormUrlEncodedBody(("protectionType", "IP2014"), ("eggs", "dormant"))))
      lazy val jsoupDoc = Jsoup.parse(bodyOf(result))


      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))
      when(mockKeystoreConnector.saveData(ArgumentMatchers.anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(CacheMap("GA", Map.empty)))
      when(mockPlaConnector.amendProtection(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
        .thenReturn(Future.successful(HttpResponse(200, responseJson = Some(Json.parse("""{"result":"doesNotMatter"}""")))))
      when(mockResponseConstructors.createAmendResponseModelFromJson(ArgumentMatchers.any()))
        .thenReturn(None)

      status(result) shouldBe 500
      jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
      jsoupDoc.
        body.getElementById("tryAgainLink")
        .attr("href") shouldEqual
        s"${controllers.routes.ReadProtectionsController.currentProtections()}"
      result.header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache"
    }

    "the microservice returns a response with no notificationId" in new Setup {
      object DataItem extends AuthorisedFakeRequestToPost(controller.amendProtection, ("protectionType", "IP2014"), ("status", "dormant"))

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))

      when(mockPlaConnector.amendProtection(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
        .thenReturn(Future.successful(HttpResponse(200, responseJson = Some(Json.parse("""{"result":"doesNotMatter"}""")))))
      when(mockResponseConstructors.createAmendResponseModelFromJson(ArgumentMatchers.any()))
        .thenReturn(Some(AmendResponseModel(noNotificationIdProtection)))
      when(mockKeystoreConnector.saveData(ArgumentMatchers.anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(CacheMap("GA", Map.empty)))


      status(DataItem.result) shouldBe 500
      DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.noNotificationId.title")
      DataItem.jsoupDoc.body.getElementsByTag("a").attr("href") shouldEqual s"${controllers.routes.ReadProtectionsController.currentProtections()}"
      DataItem.result.header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache"
    }

    "the microservice returns a valid response" in new Setup {
      lazy val result = await(controller.amendProtection()(authenticatedFakeRequest().withFormUrlEncodedBody(("protectionType", "IP2014"), ("status", "dormant"))))
      lazy val jsoupDoc = Jsoup.parse(bodyOf(result))


      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))

        when(mockPlaConnector.amendProtection(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future.successful(HttpResponse(200, responseJson = Some(Json.parse("""{"result":"doesNotMatter"}""")))))
        when(mockResponseConstructors.createAmendResponseModelFromJson(ArgumentMatchers.any())).thenReturn(Some(tstActiveAmendResponseModel))
        when(mockKeystoreConnector.saveData(ArgumentMatchers.anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(CacheMap("keyStoreId", Map.empty)))

        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some(s"${routes.AmendsController.amendmentOutcome()}")
    }

  "Calling the amendmentOutcome action" when {
    "there is no outcome object stored in keystore" in new Setup {
      lazy val result = await(controller.amendmentOutcome()(fakeRequest))
      lazy val jsoupDoc = Jsoup.parse(bodyOf(result))
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        keystoreFetchCondition[AmendResponseModel](None)
        keystoreFetchCondition[AmendsGAModel](None)

        status(result) shouldBe 500
        jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
        jsoupDoc.body.getElementById("tryAgainLink").attr("href") shouldEqual s"${controllers.routes.ReadProtectionsController.currentProtections()}"
        result.header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache"
    }

    "there is an active protection outcome in keystore" in new Setup {
      lazy val result = await(controller.amendmentOutcome()(fakeRequest))
      lazy val jsoupDoc = Jsoup.parse(bodyOf(result))
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        when(mockKeystoreConnector.fetchAndGetFormData[AmendResponseModel](ArgumentMatchers.startsWith("amendResponseModel"))(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(Some(tstActiveAmendResponseModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[AmendsGAModel](ArgumentMatchers.startsWith("AmendsGA"))(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(Some(AmendsGAModel(Some("updatedValue"),Some("changedToYes"),Some("changedToNo"),None,Some("addedPSO")))))
        when(mockDisplayConstructors.createActiveAmendResponseDisplayModel(ArgumentMatchers.any())).thenReturn(tstActiveAmendResponseDisplayModel)

        status(result) shouldBe 200
        jsoupDoc.body.getElementById("amendmentOutcome").text shouldEqual Messages("amendResultCode.33.heading")
      }
    }


    "there is an inactive protection outcome in keystore" in new Setup {
      lazy val result = await(controller.amendmentOutcome()(fakeRequest))
      lazy val jsoupDoc = Jsoup.parse(bodyOf(result))
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        when(mockKeystoreConnector.fetchAndGetFormData[AmendResponseModel](ArgumentMatchers.startsWith("amendResponseModel"))(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(Some(tstInactiveAmendResponseModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[AmendsGAModel](ArgumentMatchers.startsWith("AmendsGA"))(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(Some(AmendsGAModel(None,Some("changedToNo"),Some("changedToYes"),None,None))))
        when(mockDisplayConstructors.createInactiveAmendResponseDisplayModel(ArgumentMatchers.any())).thenReturn(tstInactiveAmendResponseDisplayModel)

        status(result) shouldBe 200
        jsoupDoc.body.getElementById("resultPageHeading").text shouldEqual Messages("amendResultCode.43.heading")
    }


  "Calling the .amendCurrentPensions action" when {

    "not supplied with a stored model" in new Setup {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      keystoreFetchCondition[AmendProtectionModel](None)

      val result = await(controller.amendCurrentPensions("ip2016", "open")(fakeRequest))
      val jsoupDoc = Jsoup.parse(bodyOf(result))
      status(result) shouldBe 500
    }

    "supplied with a stored test model (£100000, IP2016, dormant)" in new Setup {
      val testModel = new AmendProtectionModel(ProtectionModel(None, None), ProtectionModel(None, None, uncrystallisedRights = Some(100000)))
      lazy val result = await(controller.amendCurrentPensions("ip2016", "dormant")(fakeRequest))
      lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      keystoreFetchCondition[AmendProtectionModel](Some(testModel))
      status(result) shouldBe 200

      keystoreFetchCondition[AmendProtectionModel](Some(testModel))
      jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.currentPensions.title")
    }

      "return some HTML that" should {

        "contain some text and use the character set utf-8" in new Setup {
          val testModel = new AmendProtectionModel(ProtectionModel(None, None), ProtectionModel(None, None, uncrystallisedRights = Some(100000)))
          lazy val result = await(controller.amendCurrentPensions("ip2016", "dormant")(fakeRequest))
          lazy val jsoupDoc = Jsoup.parse(bodyOf(result))
          mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
          keystoreFetchCondition[AmendProtectionModel](Some(testModel))

          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }

        "have the value 100000 completed in the amount input by default" in new Setup {
          lazy val result = await(controller.amendCurrentPensions("ip2016", "dormant")(fakeRequest))
          lazy val jsoupDoc = Jsoup.parse(bodyOf(result))
          val testModel = new AmendProtectionModel(ProtectionModel(None, None), ProtectionModel(None, None, uncrystallisedRights = Some(100000)))

          mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
          keystoreFetchCondition[AmendProtectionModel](Some(testModel))

          jsoupDoc.body.getElementById("amendedUKPensionAmt").attr("value") shouldBe "100000"
        }
      }
    }

    "supplied with a stored test model (£100000, IP2014, dormant)" in new Setup {
      lazy val result = await(controller.amendCurrentPensions("ip2016", "dormant")(fakeRequest))
      lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))

        status(result) shouldBe 200
    }


  "Submitting Amend IP16 Current Pensions data" when {

    "the data is valid" in new Setup {
      object DataItem extends AuthorisedFakeRequestToPost(controller.submitAmendCurrentPension, ("amendedUKPensionAmt", "100000"), ("protectionType", "ip2016"), ("status", "dormant"))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        keystoreSaveCondition[AmendProtectionModel](mockKeystoreConnector)
        keystoreFetchCondition[AmendProtectionModel](Some(testIP16DormantModel))

        status(DataItem.result) shouldBe 303
        redirectLocation(DataItem.result) shouldBe Some(s"${routes.AmendsController.amendsSummary("ip2016", "dormant")}")
    }

    "the data is invalid" in new Setup {
      object DataItem extends AuthorisedFakeRequestToPost(controller.submitAmendCurrentPension, ("amendedUKPensionAmt", ""))
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        status(DataItem.result) shouldBe 400
      }

    "the model can't be fetched from keyStore" in new Setup {
      object DataItem extends AuthorisedFakeRequestToPost(controller.submitAmendCurrentPension, ("amendedUKPensionAmt", "1000000"), ("protectionType", "IP2016"), ("status", "dormant"))
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        keystoreFetchCondition[AmendProtectionModel](None)

        status(DataItem.result) shouldBe 500
      }
    }

  "In AmendsController calling the .amendPensionsTakenBefore action" when {

    "not supplied with a stored model" in new Setup {
      lazy val result = await(controller.amendPensionsTakenBefore("ip2016", "open")(fakeRequest))
      lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      keystoreFetchCondition[AmendProtectionModel](None)

      status(result) shouldBe 500
    }

    "supplied with the stored test model for (dormant, IP2016, preADay = £0.0)" in new Setup {
      lazy val result = await(controller.amendPensionsTakenBefore("ip2016", "dormant")(fakeRequest))
      lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModelWithNoDebit))

      jsoupDoc.body.getElementById("amendedPensionsTakenBefore-no").attr("checked") shouldBe "checked"
    }


    "supplied with the stored test model for (dormant, IP2016, preADay = £2000)" in new Setup {

      lazy val result = await(controller.amendPensionsTakenBefore("ip2016", "dormant")(fakeRequest))
      lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))

      status(result) shouldBe 200
    }

    "should take the user to the pensions taken before page" in new Setup {
      lazy val result = await(controller.amendPensionsTakenBefore("ip2016", "dormant")(fakeRequest))
      lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))

      jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.pensionsTakenBefore.title")
    }

    "return some HTML that" should {

      "contain some text and use the character set utf-8" in new Setup {
        lazy val result = await(controller.amendPensionsTakenBefore("ip2016", "dormant")(fakeRequest))
        lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))

        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }

      "have the value of the check box set as 'Yes' by default" in new Setup {
        lazy val result = await(controller.amendPensionsTakenBefore("ip2016", "dormant")(fakeRequest))
        lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))

        jsoupDoc.body.getElementById("amendedPensionsTakenBefore-yes").attr("checked") shouldBe "checked"
      }

      "have the value of the input field set to 2000 by default" in new Setup {
        lazy val result = await(controller.amendPensionsTakenBefore("ip2016", "dormant")(fakeRequest))
        lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))

        jsoupDoc.body.getElementById("amendedPensionsTakenBeforeAmt").attr("value") shouldBe "2000"
      }
    }
    "supplied with the stored test model for (dormant, IP2014, preADay = £2000)" in new Setup {
      lazy val result = await(controller.amendPensionsTakenBefore("ip2016", "dormant")(fakeRequest))
      lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))

      status(result) shouldBe 200
    }
  }



  "Submitting Amend IP16 Pensions Taken Before data" when {

    "the data is invalid" in new Setup {
  lazy val result = await(controller.submitAmendPensionsTakenBefore(fakeRequest))
  lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

        status(result) shouldBe 400
      }
    }

    "the data is invalidated by additional validation" in new Setup {
      object DataItem extends AuthorisedFakeRequestToPost(controller.submitAmendPensionsTakenBefore,
        ("amendedPensionsTakenBefore", "yes"), ("amendedPensionsTakenBeforeAmt", "-1"), ("protectionType", "ip2016"), ("status", "dormant"))


        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        status(DataItem.result) shouldBe 400
      }


    "the model can't be fetched from keyStore" in new Setup {
      object DataItem extends AuthorisedFakeRequestToPost(controller.submitAmendPensionsTakenBefore,
        ("amendedPensionsTakenBefore", "no"), ("amendedPensionsTakenBeforeAmt", "0"), ("protectionType", "ip2016"), ("status", "dormant"))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        keystoreFetchCondition[AmendProtectionModel](None)

        status(DataItem.result) shouldBe 500
    }

    "the data is valid with a no" in new Setup {
      object DataItem extends AuthorisedFakeRequestToPost(controller.submitAmendPensionsTakenBefore,
        ("amendedPensionsTakenBefore", "no"), ("amendedPensionsTakenBeforeAmt", "0"), ("protectionType", "ip2016"), ("status", "dormant"))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        keystoreSaveCondition[PensionsTakenBeforeModel](mockKeystoreConnector)
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))

        status(DataItem.result) shouldBe 303
        redirectLocation(DataItem.result) shouldBe Some(s"${routes.AmendsController.amendsSummary("ip2016", "dormant")}")
      }

    "the data is valid with a yes" in new Setup {
      object DataItem extends AuthorisedFakeRequestToPost(controller.submitAmendPensionsTakenBefore,
        ("amendedPensionsTakenBefore", "yes"), ("amendedPensionsTakenBeforeAmt", "10"), ("protectionType", "ip2016"), ("status", "dormant"))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
        keystoreSaveCondition[PensionsTakenBeforeModel](mockKeystoreConnector)
        keystoreSaveCondition[AmendProtectionModel](mockKeystoreConnector)

      status(DataItem.result) shouldBe 303
        redirectLocation(DataItem.result) shouldBe Some(s"${routes.AmendsController.amendsSummary("ip2016", "dormant")}")
      }

  "In AmendsController calling the .amendPensionsTakenBetween action" when {
    "not supplied with a stored model" in new Setup {

      lazy val result = await(controller.amendPensionsTakenBetween("ip2016", "open")(fakeRequest))
      lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        keystoreFetchCondition[AmendProtectionModel](None)

        status(result) shouldBe 500

    }
    "supplied with the stored test model for (dormant, IP2016, preADay = £0.0)" in new Setup {
      lazy val result = await(controller.amendPensionsTakenBetween("ip2016", "dormant")(fakeRequest))
      lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModelWithNoDebit))
        jsoupDoc.body.getElementById("amendedPensionsTakenBetween-no").attr("checked") shouldBe "checked"
      }
    }

    "supplied with the stored test model for (dormant, IP2016, preADay = £2000)" in new Setup {

      lazy val result = await(controller.amendPensionsTakenBetween("ip2016", "dormant")(fakeRequest))
      lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
        status(result) shouldBe 200
      }

      "should take the user to the pensions taken before page" in new Setup {
        lazy val result = await(controller.amendPensionsTakenBetween("ip2016", "dormant")(fakeRequest))
        lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
        jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.pensionsTakenBetween.title")
      }

      "return some HTML that" should {

        "contain some text and use the character set utf-8" in new Setup {
          lazy val result = await(controller.amendPensionsTakenBetween("ip2016", "dormant")(fakeRequest))
          lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

          mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
          keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))

          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }

        "have the value of the check box set as 'Yes' by default" in new Setup {
          lazy val result = await(controller.amendPensionsTakenBetween("ip2016", "dormant")(fakeRequest))
          lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

          mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
          keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))

          jsoupDoc.body.getElementById("amendedPensionsTakenBetween-yes").attr("checked") shouldBe "checked"
        }

        "have the value of the input field set to 2000 by default" in new Setup {
          lazy val result = await(controller.amendPensionsTakenBetween("ip2016", "dormant")(fakeRequest))
          lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

          mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
          keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))

          jsoupDoc.body.getElementById("amendedPensionsTakenBetweenAmt").attr("value") shouldBe "2000"
        }
      }

    "supplied with the stored test model for (dormant, IP2014, preADay = £2000))" in new Setup {
      lazy val result = await(controller.amendPensionsTakenBetween("ip2014", "dormant")(fakeRequest))
      lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))

        status(result) shouldBe 200
      }

  "Submitting Amend IP16 Pensions Taken Between data" when {

    "the model can't be fetched from keyStore" in new Setup {
      object DataItem extends AuthorisedFakeRequestToPost(controller.submitAmendPensionsTakenBetween,
        ("amendedPensionsTakenBetween", "no"), ("amendedPensionsTakenBetweenAmt", "0"), ("protectionType", "ip2016"), ("status", "dormant"))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        keystoreFetchCondition[AmendProtectionModel](None)

        status(DataItem.result) shouldBe 500
      }


    "the data is valid with a no response" in new Setup {
      object DataItem extends AuthorisedFakeRequestToPost(controller.submitAmendPensionsTakenBetween,
        ("amendedPensionsTakenBetween", "no"), ("amendedPensionsTakenBetweenAmt", "0"), ("protectionType", "ip2016"), ("status", "dormant"))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
        keystoreSaveCondition[AmendProtectionModel](mockKeystoreConnector)

      status(DataItem.result) shouldBe 303

        redirectLocation(DataItem.result) shouldBe Some(s"${routes.AmendsController.amendsSummary("ip2016", "dormant")}")
      }

    "the data is valid with a yes response" in new Setup {
      object DataItem extends AuthorisedFakeRequestToPost(controller.submitAmendPensionsTakenBetween,
        ("amendedPensionsTakenBetween", "yes"), ("amendedPensionsTakenBetweenAmt", "10"), ("protectionType", "ip2016"), ("status", "dormant"))


        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
        keystoreSaveCondition[AmendProtectionModel](mockKeystoreConnector)

      status(DataItem.result) shouldBe 303

        redirectLocation(DataItem.result) shouldBe Some(s"${routes.AmendsController.amendsSummary("ip2016", "dormant")}")
      }


    "the data is invalid" in new Setup {
      object DataItem extends AuthorisedFakeRequestToPost(controller.submitAmendPensionsTakenBetween,
        ("amendedPensionsTakenBetweenAmt", ""), ("protectionType", "ip2016"), ("status", "dormant"))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        status(DataItem.result) shouldBe 400
      }


    "the data is invalid on additional validation" in new Setup {
      object DataItem extends AuthorisedFakeRequestToPost(controller.submitAmendPensionsTakenBetween,
        ("amendedPensionsTakenBetween", "yes"), ("amendedPensionsTakenBetweenAmt", ""), ("protectionType", "ip2016"), ("status", "dormant"))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        status(DataItem.result) shouldBe 400
      }
  }

  "In AmendsController calling the .amendOverseasPensions action" when {

    "not supplied with a stored model" in new Setup {
      lazy val result = await(controller.amendOverseasPensions("ip2016", "open")(fakeRequest))
      lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        keystoreFetchCondition[AmendProtectionModel](None)

        status(result) shouldBe 500
      }

    "supplied with the stored test model for (dormant, IP2016, nonUKRights = £0.0)" in new Setup {
      lazy val result = await(controller.amendOverseasPensions("ip2016", "dormant")(fakeRequest))
      lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModelWithNoDebit))

        jsoupDoc.body.getElementById("amendedOverseasPensions-no").attr("checked") shouldBe "checked"
      }


    "supplied with the stored test model for (dormant, IP2016, nonUKRights = £2000)" in new Setup {

      lazy val result = await(controller.amendOverseasPensions("ip2016", "dormant")(fakeRequest))
      lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))

        status(result) shouldBe 200
      }

      "should take the user to the overseas pensions page" in new Setup {
        lazy val result = await(controller.amendOverseasPensions("ip2016", "dormant")(fakeRequest))
        lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))

        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
        jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.overseasPensions.title")
      }

      "return some HTML that" should {

        "contain some text and use the character set utf-8" in new Setup {
          lazy val result = await(controller.amendOverseasPensions("ip2016", "dormant")(fakeRequest))
          lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

          mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
          keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))

          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }

        "have the value of the check box set as 'Yes' by default" in new Setup {

          lazy val result = await(controller.amendOverseasPensions("ip2016", "dormant")(fakeRequest))
          lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

          mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
          keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))

          keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
          jsoupDoc.body.getElementById("amendedOverseasPensions-yes").attr("checked") shouldBe "checked"
        }

        "have the value of the input field set to 2000 by default" in new Setup {
          lazy val result = await(controller.amendOverseasPensions("ip2016", "dormant")(fakeRequest))
          lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

          mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
          keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))

          keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
          jsoupDoc.body.getElementById("amendedOverseasPensionsAmt").attr("value") shouldBe "2000"
        }
      }

    "supplied with the stored test model for (dormant, IP2014, nonUKRights = £2000)" in new Setup {
      lazy val result = await(controller.amendOverseasPensions("ip2014", "dormant")(fakeRequest))
      lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))

        status(result) shouldBe 200
      }
  }

  "Submitting Amend IP16 Overseas Pensions data" when {

    "there is an error reading the form" in new Setup {
      lazy val result = await(controller.submitAmendOverseasPensions(fakeRequest))
      lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

      status(result) shouldBe 400
      }

    "the model can't be fetched from keyStore" in new Setup {
      object DataItem extends AuthorisedFakeRequestToPost(controller.submitAmendOverseasPensions,
        ("amendedOverseasPensions", "no"), ("amendedOverseasPensionsAmt", "0"), ("protectionType", "ip2016"), ("status", "dormant"))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        keystoreFetchCondition[AmendProtectionModel](None)

        status(DataItem.result) shouldBe 500
      }


    "the data is valid with a no response" in new Setup {
      object DataItem extends AuthorisedFakeRequestToPost(controller.submitAmendOverseasPensions,
        ("amendedOverseasPensions", "no"), ("amendedOverseasPensionsAmt", "0"), ("protectionType", "ip2016"), ("status", "dormant"))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
      keystoreSaveCondition[AmendProtectionModel](mockKeystoreConnector)

        status(DataItem.result) shouldBe 303
        redirectLocation(DataItem.result) shouldBe Some(s"${routes.AmendsController.amendsSummary("ip2016", "dormant")}")
      }

    "the data is valid with a yes response" in new Setup {
      object DataItem extends AuthorisedFakeRequestToPost(controller.submitAmendOverseasPensions,
        ("amendedOverseasPensions", "yes"), ("amendedOverseasPensionsAmt", "10"), ("protectionType", "ip2016"), ("status", "dormant"))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
        keystoreSaveCondition[AmendProtectionModel](mockKeystoreConnector)

        status(DataItem.result) shouldBe 303
        redirectLocation(DataItem.result) shouldBe Some(s"${routes.AmendsController.amendsSummary("ip2016", "dormant")}")
      }


    "the data is invalid" in new Setup {
      object DataItem extends AuthorisedFakeRequestToPost(controller.submitAmendOverseasPensions,
        ("amendedOverseasPensions", "yes"), ("amendedOverseasPensionsAmt", ""), ("protectionType", "ip2016"), ("status", "dormant"))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

        status(DataItem.result) shouldBe 400
        DataItem.jsoupDoc.getElementsByClass("error-notification").text should include(Messages("pla.base.errors.errorQuestion"))
      }
    }

  "Calling the amendPsoDetails action" when {

    val testProtectionNoPsoList = ProtectionModel(
      psaCheckReference = Some("psaRef"),
      protectionID = Some(1234),
      pensionDebits = None
    )

    val testProtectionEmptyPsoList = ProtectionModel(
      psaCheckReference = Some("psaRef"),
      protectionID = Some(1234),
      pensionDebits = Some(List.empty)
    )

    val testProtectionSinglePsoList = ProtectionModel(
      psaCheckReference = Some("psaRef"),
      protectionID = Some(1234),
      pensionDebits = Some(List(PensionDebitModel("2016-12-23", 1000.0)))
    )

    val testProtectionMultiplePsoList = ProtectionModel(
      psaCheckReference = Some("psaRef"),
      protectionID = Some(1234),
      pensionDebits = Some(List(PensionDebitModel("2016-12-23", 1000.0), PensionDebitModel("2016-12-27", 11322.75)))
    )

    "there is no amendment model fetched from keystore" in new Setup {

      lazy val result = await(controller.amendPsoDetails("ip2014", "open")(fakeRequest))
      lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      keystoreFetchCondition[AmendProtectionModel](None)

      status(result) shouldBe 500
    }
    "show the technical error page for existing protections" in new Setup {
      lazy val result = await(controller.amendPsoDetails("ip2014", "open")(fakeRequest))
      lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      keystoreFetchCondition[AmendProtectionModel](None)

      jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
      jsoupDoc.body.getElementById("tryAgainLink").attr("href") shouldEqual s"${controllers.routes.ReadProtectionsController.currentProtections()}"
      result.header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache"
    }

    "there is no PSO list stored in the AmendProtectionModel" in new Setup {

      lazy val result = await(controller.amendPsoDetails("ip2014", "open")(fakeRequest))
      lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      keystoreFetchCondition[AmendProtectionModel](Some(AmendProtectionModel(testProtectionNoPsoList, testProtectionNoPsoList)))
      status(result) shouldBe 200

      jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.psoDetails.title")
      jsoupDoc.body.getElementById("psoDay").attr("value") shouldEqual ""
      jsoupDoc.body.getElementById("psoMonth").attr("value") shouldEqual ""
      jsoupDoc.body.getElementById("psoYear").attr("value") shouldEqual ""
    }

    "there is an empty PSO list stored in the AmendProtectionModel" in new Setup {

      lazy val result = await(controller.amendPsoDetails("ip2016", "open")(fakeRequest))
      lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      keystoreFetchCondition[AmendProtectionModel](Some(AmendProtectionModel(testProtectionEmptyPsoList, testProtectionEmptyPsoList)))

      status(result) shouldBe 200
      jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.psoDetails.title")
      jsoupDoc.body.getElementById("psoDay").attr("value") shouldEqual ""
      jsoupDoc.body.getElementById("psoMonth").attr("value") shouldEqual ""
      jsoupDoc.body.getElementById("psoYear").attr("value") shouldEqual ""
    }


    "there is a PSO list of one PSO stored in the AmendProtectionModel" in new Setup {

      object DataItem extends AuthorisedFakeRequestTo(controller.amendPsoDetails("ip2016", "open"))

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      keystoreFetchCondition[AmendProtectionModel](Some(AmendProtectionModel(testProtectionSinglePsoList, testProtectionSinglePsoList)))

      status(DataItem.result) shouldBe 200

      DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.psoDetails.title")
      DataItem.jsoupDoc.body.getElementById("psoDay").attr("value") shouldEqual "23"
      DataItem.jsoupDoc.body.getElementById("psoMonth").attr("value") shouldEqual "12"
      DataItem.jsoupDoc.body.getElementById("psoYear").attr("value") shouldEqual "2016"
      DataItem.jsoupDoc.body.getElementById("psoAmt").attr("value") shouldEqual "1000"
    }


    "there is a PSO list of more then one PSO stored in the AmendProtectionModel" in new Setup {

      lazy val result = await(controller.amendPsoDetails("ip2016", "open")(fakeRequest))
      lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      keystoreFetchCondition[AmendProtectionModel](Some(AmendProtectionModel(testProtectionMultiplePsoList, testProtectionMultiplePsoList)))

      status(result) shouldBe 500
      jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
      jsoupDoc.body.getElementById("tryAgainLink").attr("href") shouldEqual s"${controllers.routes.ReadProtectionsController.currentProtections()}"
      result.header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache"
    }
  }


  "Submitting Amend PSOs data" when {

    "submitting valid data for IP14" in new Setup {

      object DataItem extends AuthorisedFakeRequestToPost(controller.submitAmendPsoDetails,
        ("psoDay", "6"),
        ("psoMonth", "4"),
        ("psoYear", "2014"),
        ("psoAmt", "100000"),
        ("protectionType", "ip2014"),
        ("status", "open"),
        ("existingPSO", "true")
      )

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))
      when(mockKeystoreConnector.saveData(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(CacheMap("", Map("" -> JsNull))))

      status(DataItem.result) shouldBe 303
      redirectLocation(DataItem.result) shouldBe Some(s"${routes.AmendsController.amendsSummary("ip2014", "open")}")
    }

    "submitting valid data for IP16" in new Setup {

      object DataItem extends AuthorisedFakeRequestToPost(controller.submitAmendPsoDetails,
        ("psoDay", "6"),
        ("psoMonth", "4"),
        ("psoYear", "2016"),
        ("psoAmt", "100000"),
        ("protectionType", "ip2016"),
        ("status", "open"),
        ("existingPSO", "true")
      )

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
      when(mockKeystoreConnector.saveData(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(CacheMap("", Map("" -> JsNull))))

      status(DataItem.result) shouldBe 303
      redirectLocation(DataItem.result) shouldBe Some(s"${routes.AmendsController.amendsSummary("ip2016", "open")}")
    }

    "submitting invalid data" in new Setup {

      object DataItem extends AuthorisedFakeRequestToPost(controller.submitAmendPsoDetails,
        ("psoDay", ""),
        ("psoMonth", "1"),
        ("psoYear", "2015"),
        ("psoAmt", "100000"),
        ("protectionType", "ip2014"),
        ("status", "open"),
        ("existingPSO", "true")
      )

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      status(DataItem.result) shouldBe 400
    }

    "submitting data which fails additional validation" in new Setup {

      object DataItem extends AuthorisedFakeRequestToPost(controller.submitAmendPsoDetails,
        ("psoDay", "36"),
        ("psoMonth", "1"),
        ("psoYear", "2015"),
        ("psoAmt", "100000"),
        ("protectionType", "ip2014"),
        ("status", "open"),
        ("existingPSO", "true")
      )

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      status(DataItem.result) shouldBe 400
    }
  }


  "Removing a recently added PSO" when {

    val testProtectionSinglePsoList = ProtectionModel(
      psaCheckReference = Some("psaRef"),
      protectionID = Some(1234),
      pensionDebits = Some(List(PensionDebitModel("2016-12-23", 1000.0)))
    )

    "there is no amend protection model fetched from keystore" in new Setup {
      lazy val result = await(controller.removePso("ip2016", "open")(fakeRequest))
      lazy val jsoupDoc = Jsoup.parse(bodyOf(result))


      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      keystoreFetchCondition[AmendProtectionModel](None)
      status(result) shouldBe 500
    }
      "show the technical error page for existing protections" in new Setup {
        lazy val result = await(controller.removePso("ip2016", "open")(fakeRequest))
        lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        keystoreFetchCondition[AmendProtectionModel](None)

        jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
        jsoupDoc.body.getElementById("tryAgainLink").attr("href") shouldEqual s"${controllers.routes.ReadProtectionsController.currentProtections()}"
      }
      "have the correct cache control" in new Setup {
        lazy val result = await(controller.removePso("ip2016", "open")(fakeRequest))
        lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        keystoreFetchCondition[AmendProtectionModel](None)

        result.header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache"
      }

    "a valid amend protection model is fetched from keystore" in new Setup {
      lazy val result = await(controller.removePso("ip2016", "open")(fakeRequest))
      lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        keystoreFetchCondition[AmendProtectionModel](Some(AmendProtectionModel(testProtectionSinglePsoList, testProtectionSinglePsoList)))
        status(result) shouldBe 200
      }

      "show the remove pso page with correct details" in new Setup {
        lazy val result = await(controller.removePso("ip2016", "open")(fakeRequest))
        lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        keystoreFetchCondition[AmendProtectionModel](Some(AmendProtectionModel(testProtectionSinglePsoList, testProtectionSinglePsoList)))

        jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.psoDetails.title")
        jsoupDoc.body.getElementById("protectionType").`val`() shouldEqual "ip2016"
        jsoupDoc.body.getElementById("status").`val`() shouldEqual "open"
      }


    "choosing remove on the remove page" in new Setup {
      lazy val result = await(controller.submitRemovePso(fakeRequest))
      lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        status(result) shouldEqual 400
      }

      "return 500 if the an amend protection model could not be retrieved from keystore" in new Setup {
        object DataItem extends AuthorisedFakeRequestToPost(controller.submitRemovePso,
          ("protectionType", "ip2016"),
          ("status", "open")
        )
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        keystoreFetchCondition[AmendProtectionModel](None)

        status(DataItem.result) shouldEqual 500
      }
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
      object DataItem extends AuthorisedFakeRequestToPost(controller.submitRemovePso, ("protectionType", "ip2016"), ("status", "open"))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
        keystoreSaveCondition[AmendProtectionModel](mockKeystoreConnector)

      status(DataItem.result) shouldBe 303
        redirectLocation(DataItem.result) shouldBe Some(s"${routes.AmendsController.amendsSummary("ip2016", "open")}")

    }


  "Calling amendmentOutcomeResult" when {

    "provided with no models" in new Setup {
      val appType = ApplicationType.existingProtections
      lazy val result = controller.amendmentOutcomeResult(None, None, "")(fakeRequest)

      status(result) shouldBe INTERNAL_SERVER_ERROR
      await(bodyOf(result)) shouldBe technicalError(appType.toString).body
    }

    "provided with a model without an Id" in new Setup {
      lazy val result = controller.amendmentOutcomeResult(Some(AmendResponseModel(ProtectionModel(None, None))),
        Some(AmendsGAModel(None, None, None, None, None)), "")(fakeRequest)


        the [RequiredValueNotDefinedException] thrownBy await(result) should have message "Value not found for notificationId in amendmentOutcome"

    }

    "provided with a model with an active amendment code" in new Setup {
      val modelGA = Some(AmendsGAModel(None, None, None, None, None))
      val model = AmendResponseModel(ProtectionModel(Some("ref"), Some(33), notificationId = Some(33)))

      when(mockKeystoreConnector.saveData(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(CacheMap("", Map("" -> JsNull))))

      when(mockDisplayConstructors.createActiveAmendResponseDisplayModel(ArgumentMatchers.any()))
        .thenReturn(ActiveAmendResultDisplayModel(ApplicationType.IP2014, "33", "£1,100,000", None))

      lazy val result = controller.amendmentOutcomeResult(Some(model), modelGA, "")

      await(bodyOf(result)) shouldBe outcomeActive(ActiveAmendResultDisplayModel(ApplicationType.IP2014, "33", "£1,100,000", None), modelGA).body
      status(result) shouldBe OK
    }

    "provided with a model with an inactive amendment code" in new Setup {
      val modelGA = Some(AmendsGAModel(None, None, None, None, None))
      val model = AmendResponseModel(ProtectionModel(Some("ref"), Some(1), notificationId = Some(41)))
      lazy val result = controller.amendmentOutcomeResult(Some(model), modelGA, "")

      when(mockKeystoreConnector.saveData(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(CacheMap("", Map("" -> JsNull))))

      when(mockDisplayConstructors.createInactiveAmendResponseDisplayModel(ArgumentMatchers.any()))
        .thenReturn(InactiveAmendResultDisplayModel("41", Seq()))

        status(result) shouldBe OK
        await(bodyOf(result)) shouldBe outcomeInactive(InactiveAmendResultDisplayModel("41", Seq()), modelGA).body
    }
  }

  "Calling createPsoDetailsList" when {

    "not supplied with a PSO amount" should {

      "return the correct value not found exception" in new Setup() {
        the [RequiredValueNotDefinedException] thrownBy {
          controller.createPsoDetailsList(AmendPSODetailsModel(Some(1), Some(3), Some(2017), None, "", "", false))
        } should have message "Value not found for psoAmt in createPsoDetailsList"
      }
    }

    "supplied with a PSO amount" should {

      "return the correct list" in new Setup {
        controller.createPsoDetailsList(AmendPSODetailsModel(Some(1), Some(3), Some(2017), Some(1), "", "", false)) shouldBe Some(List(PensionDebitModel("2017-03-01", 1)))
      }
    }
  }

  "Calling getRouteUsingModel" when {

    "supplied an IP2016 AmendCurrentPensionModel" should {

      "return a status of OK" in new Setup() {
        val model = AmendCurrentPensionModel(Some(1000), "ip2016", "dormant")
        val result = controller.getRouteUsingModel(model)(fakeRequest)
        await(bodyOf(result)) shouldBe amendCurrentPensions(AmendCurrentPensionForm.amendCurrentPensionForm.fill(model)).body
        status(result) shouldBe OK
      }
    }

    "supplied an IP2014 AmendCurrentPensionModel" should {

      "return a status of OK" in new Setup() {
        val model = AmendCurrentPensionModel(Some(1000), "ip2014", "dormant")
        val result = controller.getRouteUsingModel(model)(fakeRequest)
        await(bodyOf(result)) shouldBe amendIP14CurrentPensions(AmendCurrentPensionForm.amendCurrentPensionForm.fill(model)).body
        status(result) shouldBe OK
      }
    }

    "supplied an IP2016 AmendPensionsTakenBeforeModel" should {

      "return a status of OK" in new Setup() {
        val model = AmendPensionsTakenBeforeModel("", Some(1000), "ip2016", "dormant")
        lazy val result = controller.getRouteUsingModel(model)(fakeRequest)
        await(bodyOf(result)) shouldBe amendPensionsTakenBefore(AmendPensionsTakenBeforeForm.amendPensionsTakenBeforeForm.fill(model)).body
        status(result) shouldBe OK
      }
    }

    "supplied an IP2014 AmendPensionsTakenBeforeModel" should {
      "return a status of OK" in new Setup() {
        val model = AmendPensionsTakenBeforeModel("", Some(1000), "ip2014", "dormant")
        val result = controller.getRouteUsingModel(model)(fakeRequest)
        await(bodyOf(result)) shouldBe amendIP14PensionsTakenBefore(AmendPensionsTakenBeforeForm.amendPensionsTakenBeforeForm.fill(model)).body
        status(result) shouldBe OK
      }
    }

    "supplied an IP2016 AmendPensionsTakenBetweenModel" should {
      "return a status of OK" in new Setup() {
        val model = AmendPensionsTakenBetweenModel("", Some(1000), "ip2016", "dormant")
        lazy val result = controller.getRouteUsingModel(model)(fakeRequest)
        status(result) shouldBe OK
        await(bodyOf(result)) shouldBe amendPensionsTakenBetween(AmendPensionsTakenBetweenForm.amendPensionsTakenBetweenForm.fill(model)).body
      }
    }

    "supplied an IP2014 AmendPensionsTakenBetweenModel" should {
      "return a status of OK" in new Setup() {
        val model = AmendPensionsTakenBetweenModel("", Some(1000), "ip2014", "dormant")
        lazy val result = controller.getRouteUsingModel(model)(fakeRequest)
        status(result) shouldBe OK
        await(bodyOf(result)) shouldBe amendIP14PensionsTakenBetween(AmendPensionsTakenBetweenForm.amendPensionsTakenBetweenForm.fill(model)).body
      }
    }

    "supplied an IP2016 AmendOverseasPensionsModel" should {
      "return a status of OK" in new Setup() {
        val model = AmendOverseasPensionsModel("", Some(1000), "ip2016", "dormant")
        lazy val result = controller.getRouteUsingModel(model)(fakeRequest)
        status(result) shouldBe OK
        await(bodyOf(result)) shouldBe amendOverseasPensions(AmendOverseasPensionsForm.amendOverseasPensionsForm.fill(model)).body
      }
    }

    "supplied an IP2014 AmendOverseasPensionsModel" should {
      "return a status of OK" in new Setup() {
        val model = AmendOverseasPensionsModel("", Some(1000), "ip2014", "dormant")
        lazy val result = controller.getRouteUsingModel(model)(fakeRequest)
        status(result) shouldBe OK
        await(bodyOf(result)) shouldBe amendIP14OverseasPensions(AmendOverseasPensionsForm.amendOverseasPensionsForm.fill(model)).body
      }
    }
  }
}
