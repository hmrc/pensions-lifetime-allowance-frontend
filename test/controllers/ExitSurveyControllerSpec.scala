/*
 * Copyright 2016 HM Revenue & Customs
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
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.http._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.mvc.{AnyContent, Action}
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.play.test.{WithFakeApplication, UnitSpec}
import org.jsoup._
import testHelpers._
import org.mockito.Matchers
import org.mockito.Mockito._
import scala.concurrent.Future
import models._


class ExitSurveyControllerSpec extends UnitSpec with WithFakeApplication with MockitoSugar {


    val sessionId = UUID.randomUUID.toString
    val fakeRequest = FakeRequest("GET", "/protect-your-lifetime-allowance/")
    val TestExitSurveyController = new ExitSurveyController {

    }

    "Calling the .exitSurvey action" when {

        "visited directly with no session ID" should {
            object DataItem extends FakeRequestTo("exit", TestExitSurveyController.exitSurvey, None)

            "return 200" in {
                status(DataItem.result) shouldBe 200
            }

            "redirect to exit survey page" in {
                redirectLocation(DataItem.result) shouldBe Some(s"${routes.ExitSurveyController.exitSurvey()}")
            }
        }

        "not supplied with a pre-existing stored model" should {
            object DataItem extends FakeRequestTo("exit", TestExitSurveyController.exitSurvey, Some(sessionId))
            "return a 200" in {
                status(DataItem.result) shouldBe 200
            }

            "take user to the exit survey page" in {
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.exitSurvey.pageHeading")
            }
        }

        "supplied with a pre-existing stored model" should {
            object DataItem extends FakeRequestTo("exit", TestExitSurveyController.exitSurvey, Some(sessionId))
            val testModel = new ExitSurveyModel(Some("no"), Some("no"),Some("no"),Some("no"),Some("no"),Some("no"))
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

                "have the radio option `no` selected by default" in {
                    DataItem.jsoupDoc.body.getElementById("phoneOrWrite-no").parent.classNames().contains("selected") shouldBe true
                }
            }
        }
    }
}
