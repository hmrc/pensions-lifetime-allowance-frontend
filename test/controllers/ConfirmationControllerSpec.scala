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

package controllers

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import auth.AuthFunction
import config.wiring.PlaFormPartialRetriever
import config.{FrontendAppConfig, LocalTemplateRenderer, PlaContext}
import mocks.AuthMock
import org.scalatest.mockito.MockitoSugar
import play.api.{Application, Configuration, Environment}
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import testHelpers._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class ConfirmationControllerSpec extends UnitSpec with MockitoSugar with AuthMock with WithFakeApplication {

    implicit val mockTemplateRenderer: LocalTemplateRenderer   = MockTemplateRenderer.renderer
    implicit val mockPartialRetriever: PlaFormPartialRetriever = mock[PlaFormPartialRetriever]
    implicit val mockAppConfig: FrontendAppConfig              = fakeApplication.injector.instanceOf[FrontendAppConfig]
    implicit val mockPlaContext: PlaContext                    = mock[PlaContext]
    implicit val system: ActorSystem                           = ActorSystem()
    implicit val materializer: ActorMaterializer               = ActorMaterializer()
    implicit val application: Application                      = mock[Application]

    val mockMCC = fakeApplication.injector.instanceOf[MessagesControllerComponents]
    val mockAuthFunction = fakeApplication.injector.instanceOf[AuthFunction]
    val mockEnv = mock[Environment]

    val authFunction = new AuthFunction {
        override implicit val partialRetriever: PlaFormPartialRetriever = mockPartialRetriever
        override implicit val templateRenderer: LocalTemplateRenderer = mockTemplateRenderer
        override implicit val plaContext: PlaContext = mockPlaContext
        override implicit val appConfig: FrontendAppConfig = mockAppConfig
        override def authConnector: AuthConnector = mockAuthConnector
        override def config: Configuration = mockAppConfig.configuration
        override def env: Environment = mockEnv
    }


    class Setup {
        val controller = new ConfirmationController(
            mockMCC,
            authFunction
        )
    }

    "Calling the .confirmFP action" should {
        "return a 200" in new Setup {
            mockAuthConnector(Future.successful({}))

            val result = await(controller.confirmFP(FakeRequest()))
            status(result) shouldBe 200
        }
    }
}
