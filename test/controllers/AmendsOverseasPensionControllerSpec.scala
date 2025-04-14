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
import config._
import connectors.PLAConnector
import constructors.{DisplayConstructors, ResponseConstructors}
import mocks.AuthMock
import models._
import models.amendModels._
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.Environment
import play.api.i18n.{I18nSupport, Lang, Messages, MessagesApi}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionCacheService
import testHelpers._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import views.html.pages.amends._
import views.html.pages.fallback.{noNotificationId, technicalError}
import views.html.pages.result.manualCorrespondenceNeeded

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class AmendsOverseasPensionControllerSpec extends FakeApplication
  with MockitoSugar
  with SessionCacheTestHelper
  with BeforeAndAfterEach with AuthMock with I18nSupport {

  implicit lazy val mockMessage: Messages = fakeApplication().injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

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
  val mockAmendCurrentPensions: amendCurrentPensions = app.injector.instanceOf[amendCurrentPensions]
  val mockAmendPensionsTakenBefore: amendPensionsTakenBefore = app.injector.instanceOf[amendPensionsTakenBefore]
  val mockAmendPensionsWorthBefore: amendPensionsWorthBefore = app.injector.instanceOf[amendPensionsWorthBefore]
  val mockAmendPensionsTakenBetween: amendPensionsTakenBetween = app.injector.instanceOf[amendPensionsTakenBetween]
  val mockAmendPensionsUsedBetween: amendPensionsUsedBetween = app.injector.instanceOf[amendPensionsUsedBetween]
  val mockAmendIP14CurrentPensions: amendIP14CurrentPensions = app.injector.instanceOf[amendIP14CurrentPensions]
  val mockAmendIP14PensionsTakenBefore: amendIP14PensionsTakenBefore = app.injector.instanceOf[amendIP14PensionsTakenBefore]
  val mockAmendIP14PensionsWorthBefore: amendIP14PensionsWorthBefore = app.injector.instanceOf[amendIP14PensionsWorthBefore]
  val mockAmendIP14PensionsTakenBetween: amendIP14PensionsTakenBetween = app.injector.instanceOf[amendIP14PensionsTakenBetween]
  val mockAmendIP14PensionsUsedBetween: amendIP14PensionsUsedBetween = app.injector.instanceOf[amendIP14PensionsUsedBetween]
  val mockAmendOverseasPensions: amendOverseasPensions = app.injector.instanceOf[amendOverseasPensions]
  val mockAmendIP414OverseasPensions: amendIP14OverseasPensions = app.injector.instanceOf[amendIP14OverseasPensions]
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

  override def beforeEach(): Unit = {
    reset(mockSessionCacheService)
    reset(mockPlaConnector)
    reset(mockDisplayConstructors)
    reset(mockAuthConnector)
    reset(mockEnv)
    reset(mockResponseConstructors)
    super.beforeEach()
  }

  val testIP16DormantModel = AmendProtectionModel(ProtectionModel(None, None), ProtectionModel(None, None, protectionType = Some("IP2016"), status = Some("dormant"), relevantAmount = Some(100000), uncrystallisedRights = Some(100000)))

  class Setup {
    val authFunction = new AuthFunctionImpl (
      mockMCC,
      mockAuthConnector,
      mockTechnicalError
      )

    val controller = new AmendsOverseasPensionController(
      mockSessionCacheService,
      mockMCC,
      authFunction,
      mockTechnicalError,
      mockAmendOverseasPensions,
      mockAmendIP414OverseasPensions
    )
  }

  val sessionId = UUID.randomUUID.toString
  implicit val fakeRequest: FakeRequest[AnyContent] = FakeRequest()
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

  def cacheFetchCondition[T](data: Option[T]): Unit = {
    when(mockSessionCacheService.fetchAndGetFormData[T](anyString())(any(), any()))
      .thenReturn(Future.successful(data))
  }

  "In AmendsOverseasPensionController calling the .amendOverseasPensions action" when {

    "not supplied with a stored model" in new Setup {
      lazy val result = controller.amendOverseasPensions("ip2016", "open")(fakeRequest)
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        cacheFetchCondition[AmendProtectionModel](None)

        status(result) shouldBe 500
      }

    "supplied with the stored test model for (dormant, IP2016, nonUKRights = £0.0)" in new Setup {
      lazy val result = controller.amendOverseasPensions("ip2016", "dormant")(fakeRequest)
      lazy val jsoupDoc = Jsoup.parse(contentAsString(result))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        cacheFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModelWithNoDebit))

        jsoupDoc.body.getElementById("conditional-amendedOverseasPensions").attr("class") shouldBe "govuk-radios__conditional govuk-radios__conditional--hidden"
      }


    "supplied with the stored test model for (dormant, IP2016, nonUKRights = £2000)" in new Setup {

      lazy val result = controller.amendOverseasPensions("ip2016", "dormant")(fakeRequest)
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        cacheFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))

        status(result) shouldBe 200
      }

      "should take the user to the overseas pensions page" in new Setup {
        lazy val result = controller.amendOverseasPensions("ip2016", "dormant")(fakeRequest)
        lazy val jsoupDoc = Jsoup.parse(contentAsString(result))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        cacheFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))

        cacheFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
        jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.overseasPensions.title")
      }

      "return some HTML that" should {

        "contain some text and use the character set utf-8" in new Setup {
          lazy val result = controller.amendOverseasPensions("ip2016", "dormant")(fakeRequest)
          mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
          cacheFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))

          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }

        "have the value of the check box set as 'Yes' by default" in new Setup {

          lazy val result = controller.amendOverseasPensions("ip2016", "dormant")(fakeRequest)
          lazy val jsoupDoc = Jsoup.parse(contentAsString(result))

          mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
          cacheFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))

          cacheFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
          jsoupDoc.body.getElementById("conditional-amendedOverseasPensions").attr("class") shouldBe "govuk-radios__conditional"
        }

        "have the value of the input field set to 2000 by default" in new Setup {
          lazy val result = controller.amendOverseasPensions("ip2016", "dormant")(fakeRequest)
          lazy val jsoupDoc = Jsoup.parse(contentAsString(result))

          mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
          cacheFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))

          cacheFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
          jsoupDoc.body.getElementById("amendedOverseasPensionsAmt").attr("value") shouldBe "2000"
        }
      }

    "supplied with the stored test model for (dormant, IP2014, nonUKRights = £2000)" in new Setup {
      lazy val result = controller.amendOverseasPensions("ip2014", "dormant")(fakeRequest)
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        cacheFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))

        status(result) shouldBe 200
      }
  }

  "Submitting Amend IP16 Overseas Pensions data" when {

    "there is an error reading the form" in new Setup {
      lazy val result = controller.submitAmendOverseasPensions("ip2016", "dormant")(fakeRequest)
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

      status(result) shouldBe 400
      }

    "the model can't be fetched from cache" in new Setup {
      object DataItem extends AuthorisedFakeRequestToPost(controller.submitAmendOverseasPensions("ip2016", "dormant"),
        ("amendedOverseasPensions", "no"), ("amendedOverseasPensionsAmt", "0"))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        cacheFetchCondition[AmendProtectionModel](None)

        status(DataItem.result) shouldBe 500
      }


    "the data is valid with a no response" in new Setup {
      object DataItem extends AuthorisedFakeRequestToPost(controller.submitAmendOverseasPensions("ip2016", "dormant"),
        ("amendedOverseasPensions", "no"), ("amendedOverseasPensionsAmt", "0"))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
      cacheSaveCondition[AmendProtectionModel](mockSessionCacheService)

        status(DataItem.result) shouldBe 303
        redirectLocation(DataItem.result) shouldBe Some(s"${routes.AmendsController.amendsSummary("ip2016", "dormant")}")
      }

    "the data is valid with a yes response" in new Setup {
      object DataItem extends AuthorisedFakeRequestToPost(controller.submitAmendOverseasPensions("ip2016", "dormant"),
        ("amendedOverseasPensions", "yes"), ("amendedOverseasPensionsAmt", "10"))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        cacheFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
        cacheSaveCondition[AmendProtectionModel](mockSessionCacheService)

        status(DataItem.result) shouldBe 303
        redirectLocation(DataItem.result) shouldBe Some(s"${routes.AmendsController.amendsSummary("ip2016", "dormant")}")
      }


    "the data is invalid" in new Setup {
      object DataItem extends AuthorisedFakeRequestToPost(controller.submitAmendOverseasPensions("ip2016", "dormant"),
        ("amendedOverseasPensions", "yes"), ("amendedOverseasPensionsAmt", ""))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

        status(DataItem.result) shouldBe 400
        DataItem.jsoupDoc.getElementsByClass("govuk-error-message").text should include(Messages("pla.overseasPensions.amount.errors.mandatoryError.ip2016"))
      }
    }

  "Submitting Amend IP14 Overseas Pensions data" when {

    "there is an error reading the form" in new Setup {
      lazy val result = controller.submitAmendOverseasPensions("ip2014", "dormant")(fakeRequest)
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

      status(result) shouldBe 400
      }

    "the model can't be fetched from cache" in new Setup {
      object DataItem extends AuthorisedFakeRequestToPost(controller.submitAmendOverseasPensions("ip2014", "dormant"),
        ("amendedOverseasPensions", "no"), ("amendedOverseasPensionsAmt", "0"))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        cacheFetchCondition[AmendProtectionModel](None)

        status(DataItem.result) shouldBe 500
      }


    "the data is valid with a no response" in new Setup {
      object DataItem extends AuthorisedFakeRequestToPost(controller.submitAmendOverseasPensions("ip2014", "dormant"),
        ("amendedOverseasPensions", "no"), ("amendedOverseasPensionsAmt", "0"))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))
      cacheSaveCondition[AmendProtectionModel](mockSessionCacheService)

        status(DataItem.result) shouldBe 303
        redirectLocation(DataItem.result) shouldBe Some(s"${routes.AmendsController.amendsSummary("ip2014", "dormant")}")
      }

    "the data is valid with a yes response" in new Setup {
      object DataItem extends AuthorisedFakeRequestToPost(controller.submitAmendOverseasPensions("ip2014", "dormant"),
        ("amendedOverseasPensions", "yes"), ("amendedOverseasPensionsAmt", "10"))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        cacheFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))
        cacheSaveCondition[AmendProtectionModel](mockSessionCacheService)

        status(DataItem.result) shouldBe 303
        redirectLocation(DataItem.result) shouldBe Some(s"${routes.AmendsController.amendsSummary("ip2014", "dormant")}")
      }


    "the data is invalid" in new Setup {
      object DataItem extends AuthorisedFakeRequestToPost(controller.submitAmendOverseasPensions("ip2014", "dormant"),
        ("amendedOverseasPensions", "yes"), ("amendedOverseasPensionsAmt", ""))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

        status(DataItem.result) shouldBe 400
        DataItem.jsoupDoc.getElementsByClass("govuk-error-message").text should include(Messages("pla.overseasPensions.amount.errors.mandatoryError.ip2014"))
      }
    }
}
