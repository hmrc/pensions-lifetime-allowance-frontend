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
import auth.AuthFunction
import config.wiring.PlaFormPartialRetriever
import config.{FrontendAppConfig, PlaContext}
import connectors.PLAConnector
import constructors.DisplayConstructors
import forms.WithdrawDateForm
import mocks.AuthMock
import models._
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.HeaderNames.CACHE_CONTROL
import play.api.i18n.Lang
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, Configuration, Environment}
import services.SessionCacheService
import testHelpers.{AuthorisedFakeRequestToPost, FakeApplication, SessionCacheTestHelper}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import models.cache.CacheMap
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import views.html.pages.fallback.technicalError
import views.html.pages.withdraw.{withdrawConfirm, withdrawConfirmation, withdrawDate, withdrawImplications}

import scala.concurrent.{ExecutionContext, Future}

class WithdrawProtectionControllerSpec extends FakeApplication with MockitoSugar with AuthMock with BeforeAndAfterEach with SessionCacheTestHelper{

  implicit val mockPartialRetriever: PlaFormPartialRetriever = mock[PlaFormPartialRetriever]
  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: Materializer = mock[Materializer]
  implicit val mockAppConfig: FrontendAppConfig = fakeApplication().injector.instanceOf[FrontendAppConfig]
  implicit val mockPlaContext: PlaContext = mock[PlaContext]
  implicit val application = mock[Application]
  implicit val executionContext: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
  val mockPlaConnector: PLAConnector = mock[PLAConnector]
  val mockDisplayConstructors: DisplayConstructors = mock[DisplayConstructors]
  val mockMCC: MessagesControllerComponents = fakeApplication().injector.instanceOf[MessagesControllerComponents]
  val mockAuthFunction: AuthFunction = mock[AuthFunction]
  val mockWithdrawConfirm: withdrawConfirm = app.injector.instanceOf[withdrawConfirm]
  val mockWithdrawConfirmation: withdrawConfirmation = app.injector.instanceOf[withdrawConfirmation]
  val mockWithdrawDate: withdrawDate = app.injector.instanceOf[withdrawDate]
  val mockWithdrawImplications: withdrawImplications = app.injector.instanceOf[withdrawImplications]
  val mockTechnicalError: technicalError = app.injector.instanceOf[technicalError]
  val mockEnv: Environment = mock[Environment]
  implicit val formWithCSRF: FormWithCSRF = app.injector.instanceOf[FormWithCSRF]

  class Setup {

    val authFunction = new AuthFunction {
      override implicit val partialRetriever: PlaFormPartialRetriever = mockPartialRetriever
      override implicit val plaContext: PlaContext = mockPlaContext
      override implicit val appConfig: FrontendAppConfig = mockAppConfig
      override implicit val technicalError: technicalError = mockTechnicalError
      override implicit val ec: ExecutionContext = executionContext

      override def authConnector: AuthConnector = mockAuthConnector
    }

    val controller = new WithdrawProtectionController(
      mockSessionCacheService,
      mockPlaConnector,
      mockDisplayConstructors,
      mockMCC,
      authFunction,
      mockWithdrawConfirm,
      mockWithdrawConfirmation,
      mockWithdrawDate,
      mockWithdrawImplications,
      mockTechnicalError
    ) {
    }
  }

  override def beforeEach(): Unit = {
    reset(mockAuthConnector, mockPlaConnector, mockPlaContext, mockDisplayConstructors)
    super.beforeEach()
  }

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
    certificateDate = Some("2016-09-04T09:00:19.157"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456"))

  val withdrawDateForm = WithdrawDateFormModel(
    withdrawDay = Some(5),
    withdrawMonth = Some(9),
    withdrawYear = Some(2017)
  )

  val withdraw = Json.obj(
    "withdrawDate.day" -> "0",
    "withdrawDate.month" -> "2",
    "withdrawDate.year" -> "2017"
  )

  val invalidWithdrawDateForm = WithdrawDateFormModel(
    withdrawDay = Some(3),
    withdrawMonth = Some(9),
    withdrawYear = Some(2016)
  )

  val tstPensionContributionNoPsoDisplaySections = Seq(

    AmendDisplaySectionModel("PensionsTakenBefore", Seq(
      AmendDisplayRowModel("YesNo", Some(controllers.routes.AmendsController.amendPensionsTakenBefore("ip2014", "active")), None, "No"))
    ),
    AmendDisplaySectionModel("PensionsTakenBetween", Seq(
      AmendDisplayRowModel("YesNo", Some(controllers.routes.AmendsController.amendPensionsTakenBetween("ip2014", "active")), None, "No"))
    ),
    AmendDisplaySectionModel("OverseasPensions", Seq(
      AmendDisplayRowModel("YesNo", Some(controllers.routes.AmendsController.amendOverseasPensions("ip2014", "active")), None, "Yes"),
      AmendDisplayRowModel("Amt", Some(controllers.routes.AmendsController.amendOverseasPensions("ip2014", "active")), None, "£100,000"))
    ),
    AmendDisplaySectionModel("CurrentPensions", Seq(
      AmendDisplayRowModel("Amt", Some(controllers.routes.AmendsController.amendCurrentPensions("ip2014", "active")), None, "£1,000,000"))
    ),
    AmendDisplaySectionModel("CurrentPsos", Seq(
      AmendDisplayRowModel("YesNo", None, None, "No")))
  )

  val tstAmendDisplayModel = AmendDisplayModel("IP2014", amended = true,
    tstPensionContributionNoPsoDisplaySections,
    psoAdded = false, Seq(), "£1,100,000"
  )

  val fakeRequest = FakeRequest()

  val lang = mock[Lang]

    "In WithdrawProtectionController calling the showWithdrawConfirmation action" should {

      "return 200" in new Setup {
        mockAuthConnector(Future.successful({}))
        when(mockSessionCacheService.remove(any())).thenReturn(Future.successful(mock[HttpResponse]))
        status(controller.showWithdrawConfirmation("")(fakeRequest)) shouldBe 200
      }
    }

    "In WithdrawProtectionController calling the displayWithdrawConfirmation action" when {

      "handling an OK response" in new Setup {
        lazy val result = {
          mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
          cacheFetchCondition[ProtectionModel](Some(ip2016Protection))
          when(mockPlaConnector.amendProtection(anyString(), any())(any(), any()))
            .thenReturn(Future.successful(HttpResponse(status = OK, body = "")))
          controller.displayWithdrawConfirmation("")(fakeRequest)
        }
          status(result) shouldBe SEE_OTHER

      }

      "handling a non-OK response" in new Setup {
        lazy val result = {
          mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
          cacheFetchCondition[ProtectionModel](Some(ip2016Protection))
          when(mockPlaConnector.amendProtection(anyString(), any())(any(), any()))
            .thenReturn(Future.successful(HttpResponse(status = INTERNAL_SERVER_ERROR, body = "")))
          controller.displayWithdrawConfirmation("")(fakeRequest)
        }
          status(result) shouldBe INTERNAL_SERVER_ERROR
          await(result).header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache"
      }
    }

    "In WithdrawProtectionController calling the withdrawImplications action" when {

      "there is no stored protection model" should {
        "return 500" in new Setup {
          lazy val result = {
            mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
            cacheFetchCondition[ProtectionModel](None)
            controller.withdrawImplications(fakeRequest)
          }
          status(result) shouldBe INTERNAL_SERVER_ERROR
          await(result).header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache"

        }
      }

      "there is a stored protection model" should {

        "return 200" in new Setup {
          mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

          cacheFetchCondition[ProtectionModel](Some(ip2016Protection))
          when(mockDisplayConstructors.createWithdrawSummaryTable(any())).thenReturn(tstAmendDisplayModel)

          val result = controller.withdrawImplications(fakeRequest)
          status(result) shouldBe OK
        }
      }
    }


    "In WithdrawProtectionController calling the getWithdrawDateInput action" when {

      "there is a stored protection model" should {
        "return 200" in new Setup {
          mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
          cacheFetchCondition[ProtectionModel](Some(ip2016Protection))

          lazy val result = controller.getWithdrawDateInput(fakeRequest)

          status(result) shouldBe OK
        }
      }
    }

  "In WithdrawProtectionController calling the postWithdrawDateInput action" when {

    "there is no stored protection model" should {
      "return 500" in new Setup {
        mockAuthConnector(Future.successful({}))
        cacheFetchCondition[ProtectionModel](None)
        lazy val result = controller.postWithdrawDateInput(fakeRequest)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "there is a stored protection model" should {
      "return 303" in new Setup {
        mockAuthConnector(Future.successful({}))

        cacheFetchCondition[ProtectionModel](Some(ip2016Protection))
        cacheSaveCondition[WithdrawDateFormModel](mockSessionCacheService)


        val request = FakeRequest().withFormUrlEncodedBody(("withdrawDate.day", "20"), ("withdrawDate.month", "7"), ("withdrawDate.year", "2017")).withMethod("POST")

        lazy val result = controller.postWithdrawDateInput(request)
        status(result) shouldBe SEE_OTHER
      }
    }
  }

    "In WithdrawProtectionController calling the getSubmitWithdrawDateInput action" when {
      "there is a stored protection Model" should {
        "return a 200" in new Setup {
          mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

          when(mockSessionCacheService.fetchAndGetFormData[Any](anyString())(any(), any()))
            .thenReturn(
              Future.successful(Some(ip2016Protection)),
              Future.successful(Some(withdrawDateForm))
            )

          lazy val result = controller.getSubmitWithdrawDateInput(fakeRequest)
          status(result) shouldBe OK
        }
      }

      "there is no stored protection model" should {
        "return a 500" in new Setup {
          mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
          cacheFetchCondition[ProtectionModel](None)
          lazy val result = controller.getSubmitWithdrawDateInput(fakeRequest)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      }
    }

    "In withdrawProtectionController calling the submitWithdrawDateInput action" when {

      "there is a stored protection model" should {
        "return 200" in new Setup {

          object UserRequest extends AuthorisedFakeRequestToPost(controller.submitWithdrawDateInput,
            ("withdrawDate.day", "20"), ("withdrawDate.month", "7"), ("withdrawDate.year", "2017"))
          mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

          cacheFetchCondition[ProtectionModel](Some(ip2016Protection))
          status(UserRequest.result) shouldBe OK
        }
      }
       "there is a stored protection model" should {
         "return 400 Bad Request" in new Setup {

           object InvalidDayRequest extends AuthorisedFakeRequestToPost(controller.submitWithdrawDateInput,
             ("withdrawDate.day", "20000"), ("withdrawDate.month", "10"), ("withdrawDate.year", "2017"))
           mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

           cacheFetchCondition[ProtectionModel](Some(ip2016Protection))
           status(InvalidDayRequest.result) shouldBe BAD_REQUEST
         }
       }
         "there is a stored protection model" should {
           "return 400 Bad Request with single error" in new Setup {

             object InvalidMultipleErrorRequest extends AuthorisedFakeRequestToPost(controller.submitWithdrawDateInput,
               ("withdrawDate.day", "20000"), ("withdrawDate.month", "70000"), ("withdrawDate.year", "2010000007"))
             mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

             cacheFetchCondition[ProtectionModel](Some(ip2016Protection))
             status(InvalidMultipleErrorRequest.result) shouldBe BAD_REQUEST
           }
         }
      "there is a stored protection model" should {
        "return 400 Bad Request with date in past error" in new Setup {

          object BadRequestDateInPast extends AuthorisedFakeRequestToPost(controller.submitWithdrawDateInput,
            ("withdrawDate.day", "20"), ("withdrawDate.month", "1"), ("withdrawDate.year", "2012"))
          mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

          cacheFetchCondition[ProtectionModel](Some(ip2016Protection))
          status(BadRequestDateInPast.result) shouldBe BAD_REQUEST
        }
      }
    }

    "In WithdrawProtectionController calling the validateAndSaveWithdrawDateForm action" when {
      "the form has errors" should {
        "return a 400" in new Setup {

          val requestWithForm = FakeRequest().withBody(Json.toJson(invalidWithdrawDateForm))
          lazy val result = controller.validateAndSaveWithdrawDateForm(ip2016Protection)(requestWithForm)
          status(result) shouldBe BAD_REQUEST
        }
      }

      "the form does not have errors" should {
        "return a 303" in new Setup {
          when(mockSessionCacheService.saveFormData[WithdrawDateFormModel](any(), any())(any(), any()))
            .thenReturn(Future.successful(CacheMap("test", Map.empty)))

          val requestWithFormInvalid = FakeRequest().withFormUrlEncodedBody(("withdrawDate.day", "5"), ("withdrawDate.month", "9"), ("withdrawDate.year", "2017")).withMethod("POST")
          lazy val result = controller.validateAndSaveWithdrawDateForm(ip2016Protection)(requestWithFormInvalid)

          status(result) shouldBe SEE_OTHER
          await(result).header.headers("Location") shouldBe  "/protect-your-lifetime-allowance/withdraw-protection/date-input-confirmation"

        }
      }
    }

    "In WithdrawProtectionController calling the fetchWithdrawDateForm action" when {
      "there is a stored withdrawDateForm" should {
        "return a 400" in new Setup {
          cacheFetchCondition[WithdrawDateFormModel](None)
          lazy val result = controller.fetchWithdrawDateForm(ip2016Protection)(fakeRequest, lang)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      }

      "there is no stored withdrawDateForm" should {
        "return a 200" in new Setup {
          cacheFetchCondition[WithdrawDateFormModel](Some(withdrawDateForm))
          lazy val result = controller.fetchWithdrawDateForm(ip2016Protection)(fakeRequest, lang)
          status(result) shouldBe OK
        }
      }
    }

  "GetWithdrawDateModel" should {

    "return the correct date" in new Setup {
        val model = withdrawDateForm
        controller.getWithdrawDateModel(model) shouldBe "2017-09-05"
      }
    }


  "GetWithdrawDate" should {

    "return the correct date" in new Setup {
        val form = WithdrawDateForm.withdrawDateForm.fill(withdrawDateForm)
        controller.getWithdrawDate(form) shouldBe "2017-09-05"
      }
    }


  def cacheFetchCondition[T](data: Option[T]): Unit = {
    when(mockSessionCacheService.fetchAndGetFormData[T](anyString())(any(), any()))
      .thenReturn(Future.successful(data))
  }
  
}

