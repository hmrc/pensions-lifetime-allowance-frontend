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

package testHelpers


import akka.stream.Materializer
import play.api.mvc.{Action, AnyContent}
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.UnitSpec
import org.jsoup._
import auth._
import play.api.Play
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import uk.gov.hmrc.http.SessionKeys


class FakeRequestToPost(url: String, controllerAction: Action[AnyContent], sessionId: String, data: (String, String)*)  extends UnitSpec with TestConfigHelper {
  val fakeRequest = FakeRequest("POST", "/protect-your-lifetime-allowance/" + url)
    .withSession(SessionKeys.sessionId -> s"session-$sessionId")
    .withFormUrlEncodedBody(data:_*)
  val result = controllerAction(fakeRequest)
  val jsoupDoc = Jsoup.parse(bodyOf(result))
}

class AuthorisedFakeRequestToPost(controllerAction: Action[AnyContent], data: (String, String)*) extends TestConfigHelper {
    val result = controllerAction(authenticatedFakeRequest().withFormUrlEncodedBody(data:_*))
    val jsoupDoc = Jsoup.parse(bodyOf(result))
  
}
