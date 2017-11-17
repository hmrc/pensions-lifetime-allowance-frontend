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
import testHelpers.KeystoreTestHelper
import uk.gov.hmrc.auth.core.{AuthorisationException, InsufficientConfidenceLevel, PlayAuthConnector}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatestplus.play.OneAppPerSuite
import play.api.mvc.Result
import play.api.test.Helpers._

import scala.concurrent.{ExecutionContext, Future}

class AuthFunctionSpec extends UnitSpec with OneAppPerSuite with MockitoSugar with KeystoreTestHelper {

  val mockPlayAuthConnector = mock[PlayAuthConnector]
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  object TestAuthFunction extends AuthFunction  {
    lazy val appConfig = MockConfig
    override lazy val authConnector = mockPlayAuthConnector
    override def config: Configuration = mock[Configuration]
    override def env: Environment = mock[Environment]

    override def personalIVUrl = "http://www.test.com"
  }

  def mockAuthConnector[T](future: Future[T]) = {
    when(mockPlayAuthConnector.authorise[T](Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(future)
  }


  "In AuthFunction calling the genericAuthWithoutNino action" when {
    "passing auth validation" should {

      "execute the body" in {
        mockAuthConnector(Future.successful({}))
        val result = await(TestAuthFunction.genericAuthWithoutNino("IP2016")(Ok("Test body"))(fakeRequest))
        status(result) shouldBe 200
        bodyOf(result) shouldBe "Test body"
      }

      "return internal server error" in {
        mockAuthConnector(Future.failed(new InsufficientConfidenceLevel("")))
        val result = TestAuthFunction.genericAuthWithoutNino("IP2016")(InternalServerError("Test body"))(fakeRequest)
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("http://www.test.com?origin=null&confidenceLevel=200&completionURL=/pla/apply-for-ip16-pensions-taken&failureURL=/pla/not-authorised")
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

      "return internal server error" in {
        mockAuthConnector(Future.failed(new InsufficientConfidenceLevel("")))
        val result = await(TestAuthFunction.genericAuthWithNino("IP2016")(body)(fakeRequest))
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("http://www.test.com?origin=null&confidenceLevel=200&completionURL=/pla/apply-for-ip16-pensions-taken&failureURL=/pla/not-authorised")
      }
    }
  }

}
