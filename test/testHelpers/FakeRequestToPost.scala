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

package testHelpers

import auth._
import org.jsoup._
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.mvc.{Action, AnyContent}
import play.api.test.FakeRequest
import uk.gov.hmrc.http.SessionKeys
import play.api.test.Helpers._

class FakeRequestToPost(url: String, controllerAction: Action[AnyContent], sessionId: String, data: (String, String)*)
    extends AnyWordSpecLike
    with Matchers
    with OptionValues
    with TestConfigHelper {

  val fakeRequest = FakeRequest("POST", "/check-your-pension-protections/" + url)
    .withSession(SessionKeys.sessionId -> s"session-$sessionId")
    .withFormUrlEncodedBody(data: _*)
    .withMethod("POST")

  val result   = controllerAction(fakeRequest)
  val jsoupDoc = Jsoup.parse(contentAsString(result))
}

class AuthorisedFakeRequestToPost(controllerAction: Action[AnyContent], data: (String, String)*)
    extends TestConfigHelper {
  val result   = controllerAction(authenticatedFakeRequest().withFormUrlEncodedBody(data: _*).withMethod("POST"))
  val jsoupDoc = Jsoup.parse(contentAsString(result))

}
