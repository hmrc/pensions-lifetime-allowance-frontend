/*
 * Copyright 2018 HM Revenue & Customs
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
import auth._
import com.kenshoo.play.metrics.PlayModule
import config.AuthClientConnector
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import mocks.AuthMock
import org.scalatest.mockito.MockitoSugar
import play.api.{Configuration, Environment}
import play.api.Play.current
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.FakeRequest
import testHelpers._
import uk.gov.hmrc.auth.core.PlayAuthConnector
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.renderer.TemplateRenderer

import scala.concurrent.Future

class ConfirmationControllerSpec extends UnitSpec with WithFakeApplication with MockitoSugar with AuthMock {
    override def bindModules = Seq(new PlayModule)

    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()

    object TestConfirmationController extends ConfirmationController {
        lazy val appConfig = MockConfig
        override lazy val authConnector = mockAuthConnector
        lazy val postSignInRedirectUrl = "http://localhost:9012/protect-your-lifetime-allowance/apply-for-fp16"
        override def config: Configuration = mock[Configuration]
        override def env: Environment = mock[Environment]
        override implicit val templateRenderer: TemplateRenderer = MockTemplateRenderer
    }

    val sessionId = UUID.randomUUID.toString
    val fakeRequest = FakeRequest()

    val mockUsername = "mockuser"
    val mockUserId = "/auth/oid/" + mockUsername


    "ConfirmationController should be correctly initialised" in {
        ConfirmationController.authConnector shouldBe AuthClientConnector
    }

    "Calling the .confirmFP action" when {

        "navigated to " should {
            mockAuthConnector(Future.successful({}))
            lazy val result = await(TestConfirmationController.confirmFP(fakeRequest))
            lazy val jsoupDoc = Jsoup.parse(bodyOf(result))
            "return a 200" in {
                status(result) shouldBe 200
            }

            "take user to the Confirm FP page" in {
                jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.confirmFP16.pageHeading")
            }
        }
    }
}
