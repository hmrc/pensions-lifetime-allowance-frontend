/*
 * Copyright 2020 HM Revenue & Customs
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

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import config.{FrontendAppConfig, LocalTemplateRenderer, PlaContext}
import config.wiring.PlaFormPartialRetriever
import mocks.AuthMock
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.{Configuration, Environment, Mode}
import play.api.i18n.Messages
import play.api.mvc.{MessagesControllerComponents, Result}
import play.api.mvc.Results._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import testHelpers.{KeystoreTestHelper, MockTemplateRenderer}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class AuthFunctionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with KeystoreTestHelper with BeforeAndAfterEach with AuthMock {

  implicit val system: ActorSystem                        = ActorSystem()
  implicit val materializer:ActorMaterializer             = ActorMaterializer()
  implicit val templateRenderer: LocalTemplateRenderer    = MockTemplateRenderer.renderer
  implicit val partialRetriever: PlaFormPartialRetriever  = mock[PlaFormPartialRetriever]
  implicit val mockAppConfig: FrontendAppConfig           = fakeApplication.injector.instanceOf[FrontendAppConfig]
  implicit val mockPlaContext: PlaContext                 = mock[PlaContext]
  implicit val hc: HeaderCarrier                          = HeaderCarrier()

  val mockPlayAuthConnector                 = mock[PlayAuthConnector]
  implicit val mockMessages: Messages       = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  val mockEnv: Environment                  = mock[Environment]
  val mockMCC: MessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]

  override def beforeEach() {
    reset(mockPlayAuthConnector,
      mockAuthConnector,
      mockPlaContext)
  }

  class TestAuthFunction extends AuthFunction {

    lazy val plaContext: PlaContext       = mockPlaContext
    lazy val appConfig: FrontendAppConfig = mockAppConfig
    override lazy val authConnector       = mockAuthConnector

    override def config: Configuration = mock[Configuration]
    override def env: Environment = mock[Environment]
    override def personalIVUrl = "http://www.test.com"
    override def ggLoginUrl = "http://www.gglogin.com"
    override def origin = "origin"

    override implicit val partialRetriever: PlaFormPartialRetriever = mock[PlaFormPartialRetriever]
    override implicit val templateRenderer: LocalTemplateRenderer = mock[LocalTemplateRenderer]
    }

  lazy val fakeRequest = FakeRequest(GET, "http://www.pla-frontend.gov.uk/ip16-start-page")

  "In AuthFunction calling the genericAuthWithoutNino action" when {
    "passing auth validation" should {

      "execute the body" in {
        val authFunction = new TestAuthFunction
        mockAuthConnector(Future.successful({}))
        val result = await(authFunction.genericAuthWithoutNino("IP2016")(Ok("Test body"))(fakeRequest, mockMessages, hc))
        status(result) shouldBe 200
        bodyOf(result) shouldBe "Test body"
      }
    }

    "failing auth validation" should {

      "redirect to IV uplift page on an insufficient confidence level exception" in {
        val authFunction = new TestAuthFunction
        when(mockEnv.mode).thenReturn(Mode.Test)
        mockAuthConnector(Future.failed(new InsufficientConfidenceLevel("")))
        val result = authFunction.genericAuthWithoutNino("IP2016")(InternalServerError("Test body"))(fakeRequest, mockMessages, hc)

        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("http://www.test.com?origin=pensions-lifetime-allowance-frontend&confidenceLevel=200&completionURL=http://www.pla-frontend.gov.uk/ip16-start-page&failureURL=http://localhost:9010/protect-your-lifetime-allowance/not-authorised")
      }

      "return a 500 on an unexpected authorisation exception" in {
        val authFunction = new TestAuthFunction
        mockAuthConnector(Future.failed(new InternalError("")))
        val result = authFunction.genericAuthWithoutNino("IP2016")(Ok("Test body"))(fakeRequest, mockMessages, hc)

        status(result) shouldBe 500
      }

      "redirect to IV uplift page on an insufficient enrolments exception" in {
        val authFunction = new TestAuthFunction
        mockAuthConnector(Future.failed(new InsufficientEnrolments("")))
        val result = authFunction.genericAuthWithoutNino("IP2016")(InternalServerError("Test body"))(fakeRequest, mockMessages, hc)

        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("http://www.test.com?origin=pensions-lifetime-allowance-frontend&confidenceLevel=200&completionURL=http://www.pla-frontend.gov.uk/ip16-start-page&failureURL=http://localhost:9010/protect-your-lifetime-allowance/not-authorised")
      }

      "redirect to gg login page on an no active session exception" in {
        val authFunction = new TestAuthFunction
        mockAuthConnector(Future.failed(new SessionRecordNotFound))
        val result = authFunction.genericAuthWithoutNino("IP2016")(InternalServerError("Test body"))(fakeRequest, mockMessages, hc)

        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("http://www.gglogin.com?continue=http%3A%2F%2Fwww.pla-frontend.gov.uk%2Fip16-start-page&origin=origin")
      }
    }
  }

  "In AuthFunction calling the genericAuthWithNino action" when {
    val body: String => Future[Result] = nino => Ok(nino)
    "passing auth validation" should {

      "execute the body" in {
        val authFunction = new TestAuthFunction
        mockAuthConnector(Future.successful(Option("AA000001A")))
        val result = await(authFunction.genericAuthWithNino("IP2016")(body)(fakeRequest, mockMessages, hc))

        status(result) shouldBe 200
        bodyOf(result) shouldBe "AA000001A"
      }
    }
    "failing auth validation" should {

      "redirect to IV uplift page on an insufficient confidence level exception" in {
        val authFunction = new TestAuthFunction
        mockAuthConnector(Future.failed(new InsufficientConfidenceLevel("")))
        val result = await(authFunction.genericAuthWithNino("IP2016")(body)(fakeRequest, mockMessages, hc))

        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("http://www.test.com?origin=pensions-lifetime-allowance-frontend&confidenceLevel=200&completionURL=http://www.pla-frontend.gov.uk/ip16-start-page&failureURL=http://localhost:9010/protect-your-lifetime-allowance/not-authorised")
      }

      "return a 500 on an unexpected authorisation exception" in {
        val authFunction = new TestAuthFunction
        mockAuthConnector(Future.failed(new InternalError("")))
        val result = await(authFunction.genericAuthWithNino("IP2016")(body)(fakeRequest, mockMessages, hc))

        status(result) shouldBe 500
      }

      "redirect to IV uplift page on an insufficient enrolments exception" in {
        val authFunction = new TestAuthFunction
        mockAuthConnector(Future.failed(new InsufficientEnrolments("")))
        val result = await(authFunction.genericAuthWithNino("IP2016")(body)(fakeRequest, mockMessages, hc))

        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("http://www.test.com?origin=pensions-lifetime-allowance-frontend&confidenceLevel=200&completionURL=http://www.pla-frontend.gov.uk/ip16-start-page&failureURL=http://localhost:9010/protect-your-lifetime-allowance/not-authorised")
      }

      "redirect to gg login page on an no active session exception" in {
        val authFunction = new TestAuthFunction
        mockAuthConnector(Future.failed(new SessionRecordNotFound))
        val result = await(authFunction.genericAuthWithNino("IP2016")(body)(fakeRequest, mockMessages, hc))

        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("http://www.gglogin.com?continue=http%3A%2F%2Fwww.pla-frontend.gov.uk%2Fip16-start-page&origin=origin")
      }
    }
  }

}
