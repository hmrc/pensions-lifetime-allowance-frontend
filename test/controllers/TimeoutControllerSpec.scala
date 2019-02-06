/*
 * Copyright 2019 HM Revenue & Customs
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
import org.scalatest.mockito.MockitoSugar
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import testHelpers._
import auth._
import config.wiring.PlaFormPartialRetriever
import javax.inject.Inject
import org.jsoup.Jsoup
import play.api.i18n.Messages

class TimeoutControllerSpec extends UnitSpec with MockitoSugar with WithFakeApplication {

    implicit val mockPartialRetriever = mock[PlaFormPartialRetriever]
    implicit val mockTemplateRenderer = MockTemplateRenderer.renderer

    val controller = new TimeoutController

    "Calling the .timeout action" when {

        "navigated to " should {
            lazy val result = await(controller.timeout(FakeRequest()))
            "return a 200" in {
                status(result) shouldBe 200
            }
        }
    }
}
