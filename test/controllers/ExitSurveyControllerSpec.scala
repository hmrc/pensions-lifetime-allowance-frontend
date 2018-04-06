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
import mocks.AuthMock
import models._
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.{Configuration, Environment}
import play.api.Play.current
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import testHelpers._
import uk.gov.hmrc.auth.core.PlayAuthConnector
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, Retrievals}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.renderer.TemplateRenderer

import scala.concurrent.Future

class ExitSurveyControllerSpec extends UnitSpec with WithFakeApplication with MockitoSugar with AuthMock {
    override def bindModules = Seq(new PlayModule)

    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()

    val sessionId = UUID.randomUUID.toString
    val fakeRequest = FakeRequest("GET", "/protect-your-lifetime-allowance/")
    object TestExitSurveyController extends ExitSurveyController {
        lazy val appConfig = MockConfig
        override lazy val authConnector = mockAuthConnector
        lazy val postSignInRedirectUrl = "http://localhost:9012/protect-your-lifetime-allowance/apply-ip"
        override def config: Configuration = mock[Configuration]
        override def env: Environment = mock[Environment]
        override implicit val templateRenderer: TemplateRenderer = MockTemplateRenderer
    }

    "ExitSurveyController should be correctly initialised" in {
        ExitSurveyController.authConnector shouldBe AuthClientConnector
    }

    "Calling the .exitSurvey action" when {

        "not supplied with a pre-existing stored model" should {
            mockAuthConnector(Future.successful({}))
            lazy val result = await(TestExitSurveyController.exitSurvey(fakeRequest))
            lazy val jsoupDoc = Jsoup.parse(bodyOf(result))
            "return a 200" in {
                status(result) shouldBe 200
            }

            "take user to the exit survey page" in {
                jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.exitSurvey.pageHeading")
            }
        }

        "supplied with a pre-existing stored model" should {
            lazy val result = await(TestExitSurveyController.exitSurvey(fakeRequest))
            lazy val jsoupDoc = Jsoup.parse(bodyOf(result))
            val testModel = new ExitSurveyModel(Some("no"), Some("no"),Some("no"),Some("no"),Some("no"))
            "return a 200" in {
                status(result) shouldBe 200
            }

            "take user to the pension savings page" in {
                jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.exitSurvey.pageHeading")
            }

            "return some HTML that" should {

                "contain some text and use the character set utf-8" in {
                    contentType(result) shouldBe Some("text/html")
                    charset(result) shouldBe Some("utf-8")
                }
            }
        }
    }
}
