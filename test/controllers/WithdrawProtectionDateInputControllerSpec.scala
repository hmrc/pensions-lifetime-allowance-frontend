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
import mocks.AuthMock
import models._
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionCacheService
import testHelpers.{FakeApplication, SessionCacheTestHelper}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import views.html.pages.fallback.technicalError
import views.html.pages.withdraw.withdrawDate

import scala.concurrent.{ExecutionContext, Future}

class WithdrawProtectionDateInputControllerSpec
  extends FakeApplication with MockitoSugar with AuthMock with BeforeAndAfterEach with SessionCacheTestHelper with FakeRequestHelper {

  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: Materializer = mock[Materializer]
  implicit val mockAppConfig: FrontendAppConfig = fakeApplication().injector.instanceOf[FrontendAppConfig]
  implicit val mockPlaContext: PlaContext = mock[PlaContext]
  implicit val application: Application = mock[Application]
  implicit val executionContext: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  val messagesApi: MessagesApi = fakeApplication().injector.instanceOf[MessagesApi]
  implicit val testMessages: Messages = messagesApi.preferred(fakeRequest)

  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
  val mockPlaConnector: PLAConnector = mock[PLAConnector]
  val mockDisplayConstructors: DisplayConstructors = mock[DisplayConstructors]
  val mockMCC: MessagesControllerComponents = fakeApplication().injector.instanceOf[MessagesControllerComponents]
  val mockAuthFunction: AuthFunction = mock[AuthFunction]
  val mockWithdrawDate: withdrawDate = app.injector.instanceOf[withdrawDate]
  val mockTechnicalError: technicalError = app.injector.instanceOf[technicalError]
  implicit val formWithCSRF: FormWithCSRF = app.injector.instanceOf[FormWithCSRF]

  class Setup {

    val authFunction: AuthFunction = new AuthFunction {
      override implicit val plaContext: PlaContext = mockPlaContext
      override implicit val appConfig: FrontendAppConfig = mockAppConfig
      override implicit val technicalError: technicalError = mockTechnicalError
      override implicit val ec: ExecutionContext = executionContext

      override def authConnector: AuthConnector = mockAuthConnector
    }

    val controller: WithdrawProtectionDateInputController = new WithdrawProtectionDateInputController(
      mockSessionCacheService,
      mockMCC,
      authFunction,
      mockWithdrawDate,
      mockTechnicalError
    ) {
    }
  }

  override def beforeEach(): Unit = {
    reset(mockAuthConnector)
    reset(mockPlaConnector)
    reset(mockPlaContext)
    reset(mockDisplayConstructors)
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
    protectionReference = Some("PSA123456"))

  val lang: Lang = mock[Lang]

   "In WithdrawProtectionController calling the getWithdrawDateInput action" when {

      "there is a stored protection model" should {
        "return 200" in new Setup {
          mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
          cacheFetchCondition[ProtectionModel](Some(ip2016Protection))

          lazy val result: Future[Result] = controller.getWithdrawDateInput(fakeRequest)

          status(result) shouldBe OK
        }
      }

     "there is not stored protection model" should {

       "return 500" in new Setup {

         mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
         cacheFetchCondition[ProtectionModel](None)

         lazy val result: Future[Result] = controller.getWithdrawDateInput(fakeRequest)

         status(result) shouldBe INTERNAL_SERVER_ERROR
       }
     }
    }

  "In WithdrawProtectionController calling the postWithdrawDateInput action" when {

    "there is no stored protection model" should {
      "return 500" in new Setup {
        mockAuthConnector(Future.successful({}))
        cacheFetchCondition[ProtectionModel](None)
        lazy val result: Future[Result] = controller.postWithdrawDateInput(fakeRequest)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "there is a stored protection model" should {

      "return 400 when form is submitted with errors" in new Setup {

        mockAuthConnector(Future.successful({}))

        cacheFetchCondition[ProtectionModel](Some(ip2016Protection))
        cacheSaveCondition[WithdrawDateFormModel](mockSessionCacheService)


        val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest().withFormUrlEncodedBody(("withdrawDate.day", "20"), ("withdrawDate.month", "7"), ("withdrawDate.year", "abcd")).withMethod("POST")

        lazy val result: Future[Result] = controller.postWithdrawDateInput(request)
        status(result) shouldBe BAD_REQUEST
      }

      "return 303 when valid data is submitted" in new Setup {
        mockAuthConnector(Future.successful({}))

        cacheFetchCondition[ProtectionModel](Some(ip2016Protection))
        cacheSaveCondition[WithdrawDateFormModel](mockSessionCacheService)


        val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest().withFormUrlEncodedBody(("withdrawDate.day", "20"), ("withdrawDate.month", "7"), ("withdrawDate.year", "2017")).withMethod("POST")

        lazy val result: Future[Result] = controller.postWithdrawDateInput(request)
        status(result) shouldBe SEE_OTHER
      }
    }
  }

  def cacheFetchCondition[T](data: Option[T]): Unit = {
    when(mockSessionCacheService.fetchAndGetFormData[T](anyString())(any(), any()))
      .thenReturn(Future.successful(data))
  }
  
}

