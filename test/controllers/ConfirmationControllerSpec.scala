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

import config.LocalTemplateRenderer
import config.wiring.PlaFormPartialRetriever
import mocks.AuthMock
import org.scalatest.mockito.MockitoSugar
import play.api.test.FakeRequest
import testHelpers._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class ConfirmationControllerSpec extends UnitSpec with MockitoSugar with AuthMock with WithFakeApplication {

    implicit val templateRenderer: LocalTemplateRenderer = MockTemplateRenderer.renderer
    implicit val partialRetriever: PlaFormPartialRetriever = mock[PlaFormPartialRetriever]

    object TestConfirmationController extends ConfirmationController {
      override val authConnector: AuthConnector = mockAuthConnector
    }

    "Calling the .confirmFP action" should {
        "return a 200" in {
            mockAuthConnector(Future.successful({}))

            val result = await(TestConfirmationController.confirmFP(FakeRequest()))
            status(result) shouldBe 200
        }
    }
}
