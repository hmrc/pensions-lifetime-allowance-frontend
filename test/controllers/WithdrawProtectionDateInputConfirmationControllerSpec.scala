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

import auth.AuthFunction
import config.{FrontendAppConfig, PlaContext}
import connectors.PLAConnector
import constructors.DisplayConstructors
import controllers.helpers.FakeRequestHelper
import forms.WithdrawDateForm
import mocks.AuthMock
import models._
import models.cache.CacheMap
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.http.HeaderNames.CACHE_CONTROL
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, Environment}
import services.SessionCacheService
import testHelpers.{AuthorisedFakeRequestToPost, FakeApplication, SessionCacheTestHelper}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import uk.gov.hmrc.http.HttpResponse
import views.html.pages.fallback.technicalError
import views.html.pages.withdraw.{withdrawConfirm, withdrawConfirmation, withdrawDate, withdrawImplications}

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class WithdrawProtectionDateInputConfirmationControllerSpec
    extends FakeApplication
    with MockitoSugar
    with AuthMock
    with BeforeAndAfterEach
    with SessionCacheTestHelper
    with FakeRequestHelper {

  implicit val system: ActorSystem                = ActorSystem()
  implicit val mat: Materializer                  = mock[Materializer]
  implicit val mockAppConfig: FrontendAppConfig   = fakeApplication().injector.instanceOf[FrontendAppConfig]
  implicit val mockPlaContext: PlaContext         = mock[PlaContext]
  implicit val application: Application           = mock[Application]
  implicit val executionContext: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  val messagesApi: MessagesApi        = fakeApplication().injector.instanceOf[MessagesApi]
  implicit val testMessages: Messages = messagesApi.preferred(fakeRequest)

  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
  val mockPlaConnector: PLAConnector               = mock[PLAConnector]
  val mockDisplayConstructors: DisplayConstructors = mock[DisplayConstructors]
  val mockMCC: MessagesControllerComponents        = fakeApplication().injector.instanceOf[MessagesControllerComponents]
  val mockAuthFunction: AuthFunction               = mock[AuthFunction]
  val mockWithdrawConfirm: withdrawConfirm         = app.injector.instanceOf[withdrawConfirm]
  val mockWithdrawDate: withdrawDate               = app.injector.instanceOf[withdrawDate]
  val mockWithdrawImplications: withdrawImplications = app.injector.instanceOf[withdrawImplications]
  val mockTechnicalError: technicalError             = app.injector.instanceOf[technicalError]
  val mockEnv: Environment                           = mock[Environment]
  implicit val formWithCSRF: FormWithCSRF            = app.injector.instanceOf[FormWithCSRF]

  class Setup {

    val authFunction: AuthFunction = new AuthFunction {
      override implicit val plaContext: PlaContext         = mockPlaContext
      override implicit val appConfig: FrontendAppConfig   = mockAppConfig
      override implicit val technicalError: technicalError = mockTechnicalError
      override implicit val ec: ExecutionContext           = executionContext

      override def authConnector: AuthConnector = mockAuthConnector
    }

    val controller: WithdrawProtectionDateInputConfirmationController =
      new WithdrawProtectionDateInputConfirmationController(
        mockSessionCacheService,
        mockMCC,
        authFunction,
        mockWithdrawConfirm,
        mockWithdrawDate,
        mockTechnicalError
      ) {}

  }

  override def beforeEach(): Unit = {
    reset(mockAuthConnector, mockPlaConnector, mockPlaContext, mockDisplayConstructors)
    super.beforeEach()
  }

  val ip2016Protection: ProtectionModel = ProtectionModel(
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
    protectionReference = Some("PSA123456")
  )

  val withdrawDateForm: WithdrawDateFormModel = WithdrawDateFormModel(
    withdrawDate = LocalDate.of(2017, 9, 5)
  )

  val withdraw: JsObject = Json.obj(
    "withdrawDate.day"   -> "0",
    "withdrawDate.month" -> "2",
    "withdrawDate.year"  -> "2017"
  )

  val invalidWithdrawDateForm: WithdrawDateFormModel = WithdrawDateFormModel(
    withdrawDate = LocalDate.of(2016, 9, 3)
  )

  val tstPensionContributionNoPsoDisplaySections: Seq[AmendDisplaySectionModel] = Seq(
    AmendDisplaySectionModel(
      "PensionsTakenBefore",
      Seq(
        AmendDisplayRowModel(
          "YesNo",
          Some(controllers.routes.AmendsPensionTakenBeforeController.amendPensionsTakenBefore("ip2014", "active")),
          None,
          "No"
        )
      )
    ),
    AmendDisplaySectionModel(
      "PensionsTakenBetween",
      Seq(
        AmendDisplayRowModel(
          "YesNo",
          Some(controllers.routes.AmendsPensionTakenBetweenController.amendPensionsTakenBetween("ip2014", "active")),
          None,
          "No"
        )
      )
    ),
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
    AmendDisplaySectionModel("CurrentPsos", Seq(AmendDisplayRowModel("YesNo", None, None, "No")))
  )

  val tstAmendDisplayModel: AmendDisplayModel = AmendDisplayModel(
    "IP2014",
    amended = true,
    tstPensionContributionNoPsoDisplaySections,
    psoAdded = false,
    Seq(),
    "£1,100,000"
  )

  val lang: Lang = mock[Lang]

  "In WithdrawProtectionController calling the getSubmitWithdrawDateInput action" when {
    "there is a stored protection Model" should {
      "return a 200" in new Setup {
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

        when(mockSessionCacheService.fetchAndGetFormData[Any](anyString())(any(), any()))
          .thenReturn(
            Future.successful(Some(ip2016Protection)),
            Future.successful(Some(withdrawDateForm))
          )

        lazy val result: Future[Result] = controller.getSubmitWithdrawDateInput(fakeRequest)
        status(result) shouldBe OK
      }
    }

    "there is no stored protection model" should {
      "return a 500" in new Setup {
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        cacheFetchCondition[ProtectionModel](None)
        lazy val result: Future[Result] = controller.getSubmitWithdrawDateInput(fakeRequest)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "In withdrawProtectionController calling the submitWithdrawDateInput action" when {

    "there is a stored protection model" should {
      "return 200" in new Setup {

        object UserRequest
            extends AuthorisedFakeRequestToPost(
              controller.submitWithdrawDateInput,
              ("withdrawDate.day", "20"),
              ("withdrawDate.month", "7"),
              ("withdrawDate.year", "2017")
            )
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

        cacheFetchCondition[ProtectionModel](Some(ip2016Protection))
        status(UserRequest.result) shouldBe OK
      }
    }
    "there is a stored protection model" should {
      "return 400 Bad Request" in new Setup {

        object InvalidDayRequest
            extends AuthorisedFakeRequestToPost(
              controller.submitWithdrawDateInput,
              ("withdrawDate.day", "20000"),
              ("withdrawDate.month", "10"),
              ("withdrawDate.year", "2017")
            )
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

        cacheFetchCondition[ProtectionModel](Some(ip2016Protection))
        status(InvalidDayRequest.result) shouldBe BAD_REQUEST
      }
    }
    "there is a stored protection model" should {
      "return 400 Bad Request with single error" in new Setup {

        object InvalidMultipleErrorRequest
            extends AuthorisedFakeRequestToPost(
              controller.submitWithdrawDateInput,
              ("withdrawDate.day", "20000"),
              ("withdrawDate.month", "70000"),
              ("withdrawDate.year", "2010000007")
            )
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

        cacheFetchCondition[ProtectionModel](Some(ip2016Protection))
        status(InvalidMultipleErrorRequest.result) shouldBe BAD_REQUEST
      }
    }
    "there is a stored protection model" should {
      "return 400 Bad Request with date in past error" in new Setup {

        object BadRequestDateInPast
            extends AuthorisedFakeRequestToPost(
              controller.submitWithdrawDateInput,
              ("withdrawDate.day", "20"),
              ("withdrawDate.month", "1"),
              ("withdrawDate.year", "2012")
            )
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

        cacheFetchCondition[ProtectionModel](Some(ip2016Protection))
        status(BadRequestDateInPast.result) shouldBe BAD_REQUEST
      }
    }
  }

  "In WithdrawProtectionController calling the validateAndSaveWithdrawDateForm action" when {
    "the form has errors" should {
      "return a 400" in new Setup {

        val requestWithForm: FakeRequest[JsValue] = FakeRequest().withBody(Json.toJson(invalidWithdrawDateForm))
        lazy val result: Future[Result] = controller.validateAndSaveWithdrawDateForm(ip2016Protection)(requestWithForm)
        status(result) shouldBe BAD_REQUEST
      }
    }

    "the form does not have errors" should {
      "return a 303" in new Setup {
        when(mockSessionCacheService.saveFormData[WithdrawDateFormModel](any(), any())(any(), any()))
          .thenReturn(Future.successful(CacheMap("test", Map.empty)))

        val requestWithFormInvalid: FakeRequest[AnyContentAsFormUrlEncoded] =
          FakeRequest()
            .withFormUrlEncodedBody(
              ("withdrawDate.day", "5"),
              ("withdrawDate.month", "9"),
              ("withdrawDate.year", "2017")
            )
            .withMethod("POST")
        lazy val result: Future[Result] =
          controller.validateAndSaveWithdrawDateForm(ip2016Protection)(requestWithFormInvalid)

        status(result) shouldBe SEE_OTHER
        await(result).header.headers(
          "Location"
        ) shouldBe "/check-your-pension-protections/withdraw-protection/date-input-confirmation"

      }
    }
  }

  "In WithdrawProtectionController calling the fetchWithdrawDateForm action" when {
    "there is a stored withdrawDateForm" should {
      "return a 400" in new Setup {
        cacheFetchCondition[WithdrawDateFormModel](None)
        lazy val result: Future[Result] = controller.fetchWithdrawDateForm(ip2016Protection)(fakeRequest, lang)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "there is no stored withdrawDateForm" should {
      "return a 200" in new Setup {
        cacheFetchCondition[WithdrawDateFormModel](Some(withdrawDateForm))
        lazy val result: Future[Result] = controller.fetchWithdrawDateForm(ip2016Protection)(fakeRequest, lang)
        status(result) shouldBe OK
      }
    }
  }

  "GetWithdrawDateModel" should {

    "return the correct date" in new Setup {
      val model: WithdrawDateFormModel = withdrawDateForm
      controller.getWithdrawDateModel(model) shouldBe "2017-09-05"
    }
  }

  "GetWithdrawDate" should {

    "return the correct date" in new Setup {
      val form: Form[WithdrawDateFormModel] = WithdrawDateForm.withdrawDateForm(LocalDate.now()).fill(withdrawDateForm)
      controller.getWithdrawDate(form) shouldBe "2017-09-05"
    }
  }

  def cacheFetchCondition[T](data: Option[T]): Unit =
    when(mockSessionCacheService.fetchAndGetFormData[T](anyString())(any(), any()))
      .thenReturn(Future.successful(data))

}
