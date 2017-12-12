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


import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.scalatest.mock.MockitoSugar
import java.util.UUID
import uk.gov.hmrc.renderer.TemplateRenderer

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.i18n.Messages
import testHelpers._
import auth._
import com.kenshoo.play.metrics.PlayModule
import org.jsoup.Jsoup
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class TimeoutControllerSpec extends UnitSpec with WithFakeApplication with MockitoSugar {
    override def bindModules = Seq(new PlayModule)

    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()

    object TestTimeoutController extends TimeoutController {
        override implicit val templateRenderer: TemplateRenderer = MockTemplateRenderer
    }

    "Calling the .timeout action" when {

        "navigated to " should {
            lazy val result = await(TestTimeoutController.timeout(fakeRequest))
            lazy val jsoupDoc = Jsoup.parse(bodyOf(result))
            "return a 200" in {
                status(result) shouldBe 200
            }

            "take user to the Timeout page" in {
                jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.timeout.pageHeading")
            }
        }
    }
}
