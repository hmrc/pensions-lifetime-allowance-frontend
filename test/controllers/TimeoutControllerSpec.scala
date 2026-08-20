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

import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.{MessagesControllerComponents, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, status}
import testHelpers.*
import views.html.pages.timeout

import scala.concurrent.Future

class TimeoutControllerSpec extends FakeApplication with MockitoSugar {

  private val mcc: MessagesControllerComponents = inject[MessagesControllerComponents]

  private val mockTimeout: timeout = inject[timeout]

  private val controller = new TimeoutController(mcc, mockTimeout)

  "Calling the .timeout action" should {
    "return a 200" in {
      val result: Future[Result] = controller.timeout(FakeRequest())

      status(result) shouldBe 200
    }
  }

}
