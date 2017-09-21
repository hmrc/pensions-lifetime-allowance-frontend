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

package controllers

import java.util.UUID

import auth._
import com.kenshoo.play.metrics.PlayModule
import config.FrontendAuthConnector
import models._
import org.scalatest.mock.MockitoSugar
import play.api.Play.current
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import testHelpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.renderer.TemplateRenderer

class ExitSurveyControllerSpec extends UnitSpec with WithFakeApplication with MockitoSugar {
    override def bindModules = Seq(new PlayModule)


    val sessionId = UUID.randomUUID.toString
    val fakeRequest = FakeRequest("GET", "/protect-your-lifetime-allowance/")
    object TestExitSurveyController extends ExitSurveyController {
        override lazy val applicationConfig = MockConfig
        override lazy val authConnector = MockAuthConnector
        override lazy val postSignInRedirectUrl = "http://localhost:9012/protect-your-lifetime-allowance/apply-ip"
        override implicit val templateRenderer: TemplateRenderer = MockTemplateRenderer
    }

    "ExitSurveyController should be correctly initialised" in {
        ExitSurveyController.authConnector shouldBe FrontendAuthConnector
    }

    "Calling the .exitSurvey action" when {

        "visited directly with no session ID" should {
            object DataItem extends FakeRequestTo("exit", TestExitSurveyController.exitSurvey, None)

            "return 303" in {
                status(DataItem.result) shouldBe 303
            }
        }

        "not supplied with a pre-existing stored model" should {
            object DataItem extends AuthorisedFakeRequestTo(TestExitSurveyController.exitSurvey)
            "return a 200" in {
                status(DataItem.result) shouldBe 200
            }

            "take user to the exit survey page" in {
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.exitSurvey.pageHeading")
            }
        }

        "supplied with a pre-existing stored model" should {
            object DataItem extends AuthorisedFakeRequestTo(TestExitSurveyController.exitSurvey)
            val testModel = new ExitSurveyModel(Some("no"), Some("no"),Some("no"),Some("no"),Some("no"))
            "return a 200" in {
                status(DataItem.result) shouldBe 200
            }

            "take user to the pension savings page" in {
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.exitSurvey.pageHeading")
            }

            "return some HTML that" should {

                "contain some text and use the character set utf-8" in {
                    contentType(DataItem.result) shouldBe Some("text/html")
                    charset(DataItem.result) shouldBe Some("utf-8")
                }
            }
        }
    }
}
