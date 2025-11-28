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

package auth

import config.FrontendAppConfig
import mocks.AuthMock
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.mvc.Results._
import play.api.mvc.{MessagesControllerComponents, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Environment, Mode}
import testHelpers.{FakeApplication, SessionCacheTestHelper}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.http.HeaderCarrier
import views.html.pages.fallback.technicalError

import java.net.URLEncoder
import scala.concurrent.{ExecutionContext, Future}

class AuthFunctionSpec
    extends FakeApplication
    with MockitoSugar
    with SessionCacheTestHelper
    with BeforeAndAfterEach
    with AuthMock {

  implicit val system: ActorSystem              = ActorSystem()
  implicit val materializer: Materializer       = mock[Materializer]
  implicit val mockAppConfig: FrontendAppConfig = inject[FrontendAppConfig]
  implicit val hc: HeaderCarrier                = HeaderCarrier()

  val mockPlayAuthConnector: PlayAuthConnector = mock[PlayAuthConnector]

  val requestUrl  = "https://www.pla-frontend.gov.uk/ip16-start-page"
  val fakeRequest = FakeRequest(GET, requestUrl)

  implicit val mockMessages: Messages =
    inject[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  val mockEnv: Environment                  = mock[Environment]
  val mockMCC: MessagesControllerComponents = inject[MessagesControllerComponents]

  override def beforeEach(): Unit = {
    reset(mockPlayAuthConnector)
    reset(mockAuthConnector)
  }

  class TestAuthFunction extends AuthFunction {

    val appConfig: FrontendAppConfig                              = mockAppConfig
    override val authConnector: AuthConnector                     = mockAuthConnector
    override def upliftEnvironmentUrl(requestUri: String): String = requestUri
    override implicit val technicalError: technicalError          = inject[technicalError]
    override implicit val ec: ExecutionContext                    = inject[ExecutionContext]
  }

  "In AuthFunction calling the genericAuthWithoutNino action" when {
    "passing auth validation" should {

      "execute the body" in {
        val authFunction = new TestAuthFunction
        mockAuthConnector(Future.successful {})
        val result = authFunction.genericAuthWithoutNino("IP2016")(Future.successful(Ok("Test body")))(
          fakeRequest,
          mockMessages,
          hc
        )
        status(result) shouldBe 200
        contentAsString(result) shouldBe "Test body"
      }
    }

    "failing auth validation" should {

      "redirect to IV uplift page on an insufficient confidence level exception" in {
        val authFunction = new TestAuthFunction
        when(mockEnv.mode).thenReturn(Mode.Test)
        mockAuthConnector(Future.failed(InsufficientConfidenceLevel("")))
        val result = authFunction.genericAuthWithoutNino("IP2016")(Future.successful(InternalServerError("Test body")))(
          fakeRequest,
          mockMessages,
          hc
        )

        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some(
          s"${mockAppConfig.ivUpliftUrl}?" + "origin=pensions-lifetime-allowance-frontend&confidenceLevel=200&completionURL=https://www.pla-frontend.gov.uk/ip16-start-page&failureURL=http://localhost:9010/check-your-pension-protections-and-enhancements/not-authorised"
        )
      }

      "return a 500 on an unexpected authorisation exception" in {
        val authFunction = new TestAuthFunction
        mockAuthConnector(Future.failed(InternalError("")))
        val result = authFunction.genericAuthWithoutNino("IP2016")(Future.successful(Ok("Test body")))(
          fakeRequest,
          mockMessages,
          hc
        )

        status(result) shouldBe 500
      }

      "redirect to IV uplift page on an insufficient enrolments exception" in {
        val authFunction = new TestAuthFunction
        mockAuthConnector(Future.failed(InsufficientEnrolments("")))
        val result = authFunction.genericAuthWithoutNino("IP2016")(Future.successful(InternalServerError("Test body")))(
          fakeRequest,
          mockMessages,
          hc
        )

        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some(
          s"${mockAppConfig.ivUpliftUrl}?" + "origin=pensions-lifetime-allowance-frontend&confidenceLevel=200&completionURL=https://www.pla-frontend.gov.uk/ip16-start-page&failureURL=http://localhost:9010/check-your-pension-protections-and-enhancements/not-authorised"
        )
      }

      "redirect to gg login page on an no active session exception" in {
        val authFunction = new TestAuthFunction
        mockAuthConnector(Future.failed(new SessionRecordNotFound))
        val result = authFunction.genericAuthWithoutNino("IP2016")(Future.successful(InternalServerError("Test body")))(
          fakeRequest,
          mockMessages,
          hc
        )

        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some(
          s"${mockAppConfig.ggSignInUrl}?continue=${URLEncoder.encode(requestUrl, "UTF-8")}&origin=${mockAppConfig.appName}"
        )
      }
    }
  }

  "In AuthFunction calling the genericAuthWithNino action" when {
    val body: String => Future[Result] = nino => Future.successful(Ok(nino))
    "passing auth validation" should {

      "execute the body" in {
        val authFunction = new TestAuthFunction
        mockAuthConnector(Future.successful(Option("AA000001A")))
        val result = authFunction.genericAuthWithNino("IP2016")(body)(fakeRequest, mockMessages, hc)

        status(result) shouldBe 200
        contentAsString(result) shouldBe "AA000001A"
      }
    }
    "failing auth validation" should {

      "redirect to IV uplift page on an insufficient confidence level exception" in {
        val authFunction = new TestAuthFunction
        mockAuthConnector(Future.failed(InsufficientConfidenceLevel("")))
        val result = authFunction.genericAuthWithNino("IP2016")(body)(fakeRequest, mockMessages, hc)

        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some(
          s"${mockAppConfig.ivUpliftUrl}?" + "origin=pensions-lifetime-allowance-frontend&confidenceLevel=200&completionURL=https://www.pla-frontend.gov.uk/ip16-start-page&failureURL=http://localhost:9010/check-your-pension-protections-and-enhancements/not-authorised"
        )
      }

      "return a 500 on an unexpected authorisation exception" in {
        val authFunction = new TestAuthFunction
        mockAuthConnector(Future.failed(InternalError("")))
        val result = authFunction.genericAuthWithNino("IP2016")(body)(fakeRequest, mockMessages, hc)

        status(result) shouldBe 500
      }

      "redirect to IV uplift page on an insufficient enrolments exception" in {
        val authFunction = new TestAuthFunction
        mockAuthConnector(Future.failed(InsufficientEnrolments("")))
        val result = authFunction.genericAuthWithNino("IP2016")(body)(fakeRequest, mockMessages, hc)

        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some(
          s"${mockAppConfig.ivUpliftUrl}?" + "origin=pensions-lifetime-allowance-frontend&confidenceLevel=200&completionURL=https://www.pla-frontend.gov.uk/ip16-start-page&failureURL=http://localhost:9010/check-your-pension-protections-and-enhancements/not-authorised"
        )
      }

      "redirect to gg login page on an no active session exception" in {
        val authFunction = new TestAuthFunction
        mockAuthConnector(Future.failed(new SessionRecordNotFound))
        val result = authFunction.genericAuthWithNino("IP2016")(body)(fakeRequest, mockMessages, hc)

        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some(
          s"${mockAppConfig.ggSignInUrl}?continue=${URLEncoder.encode(requestUrl, "UTF-8")}&origin=${mockAppConfig.appName}"
        )
      }
    }
  }

}
