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

package testHelpers


import play.api.mvc.{AnyContent, Action}
import play.api.test.FakeRequest
import uk.gov.hmrc.play.http.SessionKeys
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.play.frontend.auth.AuthenticationProviderIds
import uk.gov.hmrc.time.DateTimeUtils._
import org.jsoup._


class FakeRequestTo(url: String, controllerAction: Action[AnyContent], sessionId: Option[String], data: (String, String)*) extends UnitSpec {
    val fakeRequest = constructRequest(url, sessionId)
    val result = controllerAction(fakeRequest)
    val jsoupDoc = Jsoup.parse(bodyOf(result))
  
    def constructRequest(url: String, sessionId: Option[String]) = {
    	sessionId match {
    		case Some(sessId) => FakeRequest("GET", "/protect-your-lifetime-allowance/" + url).withSession(SessionKeys.sessionId -> s"session-$sessionId")
    		case None => FakeRequest("GET", "/protect-your-lifetime-allowance/" + url)
    	}
    }


}

class FakeAuthorisedRequestTo(url: String, controllerAction: Action[AnyContent], sessionId: Option[String], data: (String, String)*) extends UnitSpec {

    val mockUsername = "mockuser"
    val mockUserId = "/auth/oid/" + mockUsername

    val fakeRequest = constructRequest(url, sessionId)
    val result = controllerAction(fakeRequest)
    val jsoupDoc = Jsoup.parse(bodyOf(result))
  
    def constructRequest(url: String, sessionId: Option[String]) = {
        sessionId match {
            case Some(sessId) => FakeRequest("GET", "/protect-your-lifetime-allowance/" + url).withSession(
                SessionKeys.sessionId -> s"session-$sessionId",
                SessionKeys.lastRequestTimestamp -> now.getMillis.toString,
                SessionKeys.userId -> mockUserId,
                SessionKeys.authProvider -> AuthenticationProviderIds.VerifyProviderId)
            case None => FakeRequest("GET", "/protect-your-lifetime-allowance/" + url)
        }
    }

}
