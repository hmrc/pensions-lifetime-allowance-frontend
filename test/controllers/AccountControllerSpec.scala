/*
 * Copyright 2018 HM Revenue & Customs
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

import auth.MockConfig
import com.kenshoo.play.metrics.PlayModule
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import testHelpers._
import uk.gov.hmrc.play.config.RunMode
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}


class AccountControllerSpec extends UnitSpec with WithFakeApplication with RunMode {
  override def bindModules = Seq(new PlayModule)

  val sessionId = UUID.randomUUID.toString
  val fakeRequest = FakeRequest("GET", "/")

  "navigating to signout with an existing session" should {
    object DataItem extends FakeRequestTo("/", AccountController.signOut, Some(sessionId))
    "return 303" in {
      status(DataItem.result) shouldBe Status.SEE_OTHER
    }

    "redirect to the feedback survey with the origin token PLA" in {
      redirectLocation(DataItem.result).get shouldBe (MockConfig.feedbackSurvey)
    }
  }
}
