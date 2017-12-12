/*
 * Copyright 2017 HM Revenue & Customs
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
import play.api.mvc.Results._
import org.scalatest.mock.MockitoSugar
import play.api.{Configuration, Environment}
import testHelpers.{KeystoreTestHelper, MockTemplateRenderer}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.mockito.Matchers
import org.mockito.Mockito._
import mocks.AuthMock
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.OneAppPerSuite
import play.api.mvc.Result
import play.api.test.Helpers._
import uk.gov.hmrc.renderer.TemplateRenderer

import scala.concurrent.{ExecutionContext, Future}

class AuthFunctionSpec extends UnitSpec with OneAppPerSuite with MockitoSugar with KeystoreTestHelper with BeforeAndAfterEach with AuthMock {

  val mockPlayAuthConnector = mock[PlayAuthConnector]
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  override def beforeEach() {
    reset(mockPlayAuthConnector)
  }

  object TestAuthFunction extends AuthFunction  {
    lazy val appConfig = MockConfig
    override lazy val authConnector = mockAuthConnector
    override def config: Configuration = mock[Configuration]
    override def env: Environment = mock[Environment]
    override val postSignInRedirectUrl = "http://www.pla-frontend.gov.uk/ip16-start-page"

    override def personalIVUrl = "http://www.test.com"

    override def ggLoginUrl = "http://www.gglogin.com"

    override def origin = "origin"
    override implicit val templateRenderer: TemplateRenderer = MockTemplateRenderer

  }


  "In AuthFunction calling the genericAuthWithoutNino action" when {
    "passing auth validation" should {

      "execute the body" in {
        mockAuthConnector(Future.successful({}))
        val result = await(TestAuthFunction.genericAuthWithoutNino("IP2016")(Ok("Test body"))(fakeRequest))
        status(result) shouldBe 200
        bodyOf(result) shouldBe "Test body"
      }
    }

    "failing auth validation" should {

      "redirect to IV uplift page on an insufficient confidence level exception" in {
        mockAuthConnector(Future.failed(new InsufficientConfidenceLevel("")))
        val result = TestAuthFunction.genericAuthWithoutNino("IP2016")(InternalServerError("Test body"))(fakeRequest)
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("http://www.test.com?origin=&confidenceLevel=200&completionURL=http://www.pla-frontend.gov.uk/ip16-start-page&failureURL=/pla/not-authorised")
      }

      "return a 500 on an unexpected authorisation exception" in {
        mockAuthConnector(Future.failed(new InternalError("")))
        val result = TestAuthFunction.genericAuthWithoutNino("IP2016")(Ok("Test body"))(fakeRequest)
        status(result) shouldBe 500
      }

      "redirect to IV uplift page on an insufficient enrolments exception" in {
        mockAuthConnector(Future.failed(new InsufficientEnrolments("")))
        val result = TestAuthFunction.genericAuthWithoutNino("IP2016")(InternalServerError("Test body"))(fakeRequest)
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("http://www.test.com?origin=&confidenceLevel=200&completionURL=http://www.pla-frontend.gov.uk/ip16-start-page&failureURL=/pla/not-authorised")
      }

      "redirect to gg login page on an no active session exception" in {
        mockAuthConnector(Future.failed(new SessionRecordNotFound))
        val result = TestAuthFunction.genericAuthWithoutNino("IP2016")(InternalServerError("Test body"))(fakeRequest)
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("http://www.gglogin.com?continue=http%3A%2F%2Fwww.pla-frontend.gov.uk%2Fip16-start-page&origin=origin")
      }
    }
  }

  "In AuthFunction calling the genericAuthWithNino action" when {
    val body: String => Future[Result] = nino => Ok(nino)
    "passing auth validation" should {

      "execute the body" in {
        mockAuthConnector(Future.successful(Option("AA000001A")))
        val result = await(TestAuthFunction.genericAuthWithNino("IP2016")(body)(fakeRequest))
        status(result) shouldBe 200
        bodyOf(result) shouldBe "AA000001A"
      }
    }
    "failing auth validation" should {

      "redirect to IV uplift page on an insufficient confidence level exception" in {
        mockAuthConnector(Future.failed(new InsufficientConfidenceLevel("")))
        val result = await(TestAuthFunction.genericAuthWithNino("IP2016")(body)(fakeRequest))
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("http://www.test.com?origin=&confidenceLevel=200&completionURL=http://www.pla-frontend.gov.uk/ip16-start-page&failureURL=/pla/not-authorised")
      }

      "return a 500 on an unexpected authorisation exception" in {
        mockAuthConnector(Future.failed(new InternalError("")))
        val result = await(TestAuthFunction.genericAuthWithNino("IP2016")(body)(fakeRequest))
        status(result) shouldBe 500
      }

      "redirect to IV uplift page on an insufficient enrolments exception" in {
        mockAuthConnector(Future.failed(new InsufficientEnrolments("")))
        val result = await(TestAuthFunction.genericAuthWithNino("IP2016")(body)(fakeRequest))
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("http://www.test.com?origin=&confidenceLevel=200&completionURL=http://www.pla-frontend.gov.uk/ip16-start-page&failureURL=/pla/not-authorised")
      }

      "redirect to gg login page on an no active session exception" in {
        mockAuthConnector(Future.failed(new SessionRecordNotFound))
        val result = await(TestAuthFunction.genericAuthWithNino("IP2016")(body)(fakeRequest))
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("http://www.gglogin.com?continue=http%3A%2F%2Fwww.pla-frontend.gov.uk%2Fip16-start-page&origin=origin")
      }
    }
  }

}
