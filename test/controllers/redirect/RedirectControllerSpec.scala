/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.redirect

import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, defaultAwaitTimeout, redirectLocation, status}
import testHelpers.FakeApplication

class RedirectControllerSpec extends FakeApplication with MockitoSugar {

  private val mcc = fakeApplication().injector.instanceOf[MessagesControllerComponents]

  private val redirectController = new RedirectController(mcc)

  "RedirectController on redirectToNewServiceUrl" should {
    "redirect to '/check-your-pension-protections' url with the same path" in {
      val path    = "test-path/with-some-kind-of/id/1234567"
      val request = FakeRequest(GET, "/protect-your-lifetime-allowance")

      val result = redirectController.redirectToNewServiceUrl(path)(request)

      status(result) shouldBe 303
      redirectLocation(result).get shouldBe s"/check-your-pension-protections/$path"
    }
  }

}
