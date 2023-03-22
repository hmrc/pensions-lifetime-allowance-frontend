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

import auth.MockConfig
import config.FrontendAppConfig
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import testHelpers._

class AccountControllerSpec extends FakeApplication with MockitoSugar {

  val mockAppConfig = fakeApplication().injector.instanceOf[FrontendAppConfig]
  val mockMCC = fakeApplication().injector.instanceOf[MessagesControllerComponents]

  class Setup {
    val controller = new AccountController(mockAppConfig, mockMCC)
  }

  "navigating to signout with an existing session" in new Setup {

    object DataItem extends FakeRequestTo("/", controller.signOut, Some("sessionId"))

    status(DataItem.result) shouldBe Status.SEE_OTHER
  }

    "redirect to the feedback survey with the origin token PLA" in new Setup {
      object DataItem extends FakeRequestTo("/", controller.signOut, Some("sessionId"))

      redirectLocation(DataItem.result).get shouldBe (MockConfig.feedbackSurvey)
    }
}
