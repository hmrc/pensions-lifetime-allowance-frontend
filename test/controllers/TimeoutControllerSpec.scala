/*
 * Copyright 2022 HM Revenue & Customs
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


import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import config.wiring.PlaFormPartialRetriever
import config.{FrontendAppConfig, LocalTemplateRenderer, PlaContext}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import testHelpers._
import views.html.pages.timeout

class TimeoutControllerSpec extends FakeApplication with MockitoSugar {

    val mockMCC = fakeApplication.injector.instanceOf[MessagesControllerComponents]

    implicit val templateRenderer: LocalTemplateRenderer = MockTemplateRenderer.renderer
    implicit val partialRetriever: PlaFormPartialRetriever = mock[PlaFormPartialRetriever]
    implicit val mockAppConfig: FrontendAppConfig = fakeApplication.injector.instanceOf[FrontendAppConfig]
    implicit val mockPlaContext: PlaContext = mock[PlaContext]
    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val application = mock[Application]
    implicit val mockTimeout: timeout = app.injector.instanceOf[timeout]

    val controller = new TimeoutController(mockMCC, mockTimeout)

    "Calling the .timeout action" when {

        "navigated to " should {
            lazy val result = controller.timeout(FakeRequest())
            "return a 200" in {
                status(result) shouldBe 200
            }
        }
    }
}
