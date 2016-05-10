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
import play.api.test.FakeRequest
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.http._
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.play.test.{WithFakeApplication, UnitSpec}
import testHelpers._


class IntroductionControllerSpec extends UnitSpec with WithFakeApplication{

  val sessionId = UUID.randomUUID.toString
  val fakeRequest = FakeRequest("GET", "/")

  "navigating to introduction with no session ID" should {
    "return 200" in {
      val result = IntroductionController.introduction(fakeRequest)
      status(result) shouldBe Status.OK
    }

    "return HTML" in {
      val result = IntroductionController.introduction(fakeRequest)
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }
  }

  "navigating to introduction with a session already in place" should {
    object DataItem extends FakeRequestTo("/", IntroductionController.introduction, sessionId)
    "return 200" in {
      status(DataItem.result) shouldBe Status.OK
    }

    "return HTML" in {
      contentType(DataItem.result) shouldBe Some("text/html")
      charset(DataItem.result) shouldBe Some("utf-8")
    }
  }


}
