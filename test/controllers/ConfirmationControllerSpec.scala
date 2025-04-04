/*
 * Copyright 2023 HM Revenue & Customs
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

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import auth.AuthFunction
import config.{FrontendAppConfig, PlaContext}
import mocks.AuthMock
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsString, defaultAwaitTimeout, status}
import play.api.{Application, Configuration, Environment}
import testHelpers._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.syntax.retrieved.authSyntaxForRetrieved
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import views.html.pages.fallback.technicalError
import views.html.pages.confirmation.confirmFP
import views.html.pages.ip2016
import views.html.pages.ip2016.withdrawnAP2016

import scala.concurrent.{ExecutionContext, Future}

class ConfirmationControllerSpec extends FakeApplication with MockitoSugar with AuthMock {

    implicit val mockAppConfig: FrontendAppConfig              = mock[FrontendAppConfig]
    implicit val mockPlaContext: PlaContext                    = mock[PlaContext]
    implicit val system: ActorSystem                           = ActorSystem()
    implicit val materializer: Materializer                    = mock[Materializer]
    implicit val application: Application                      = mock[Application]
    implicit val mockTechnicalError: technicalError            = app.injector.instanceOf[technicalError]
    implicit val mockConfirmFP: confirmFP                      = app.injector.instanceOf[confirmFP]
    implicit val formWithCSRF: FormWithCSRF                    = app.injector.instanceOf[FormWithCSRF]
    implicit val executionContext: ExecutionContext            = app.injector.instanceOf[ExecutionContext]


    val mockMCC = fakeApplication().injector.instanceOf[MessagesControllerComponents]
    val mockAuthFunction = fakeApplication().injector.instanceOf[AuthFunction]
    val mockEnv = mock[Environment]
    val mockWithdrawnAp2016View:withdrawnAP2016  = app.injector.instanceOf[ip2016.withdrawnAP2016]

    def confirmationControllerRoute: String = routes.ConfirmationController.confirmFP.url

    val authFunction = new AuthFunction {
        override implicit val plaContext: PlaContext = mockPlaContext
        override implicit val appConfig: FrontendAppConfig = mockAppConfig
        override implicit val technicalError: technicalError = mockTechnicalError
        override implicit val ec: ExecutionContext = executionContext

        override def authConnector: AuthConnector = mockAuthConnector
    }


    class Setup {
        val controller = new ConfirmationController(
            mockMCC,
            authFunction,
            mockConfirmFP,
            mockWithdrawnAp2016View

        )
    }

    "when applyFor2016IPAndFpShutterEnabled is disabled calling the .confirmFP action" should {
        "return a 200 and the confirmFP view" in new Setup {
            mockAuthConnector(Future.successful({}))
            when(mockAppConfig.applyFor2016IpAndFpShutterEnabled).thenReturn(false)
            val result = controller.confirmFP(FakeRequest(GET, confirmationControllerRoute))
            status(result) shouldBe 200
            contentAsString(result) should include ("Get fixed protection 2016 for your lifetime allowance")
        }
    }

    "when applyFor2016IPAndFpShutterEnabled is enabled calling the .confirmFP action" should {
        "return a 200 and withdrawnAP2016 view" in new Setup {
            mockAuthConnector(Future.successful({}))
            when(mockAppConfig.applyFor2016IpAndFpShutterEnabled).thenReturn(true)
            val result = controller.confirmFP(FakeRequest(GET, confirmationControllerRoute))

            status(result) shouldBe 200
            contentAsString(result) should include ("Sorry, applications for 2016 protection have ended")
        }
    }
}
