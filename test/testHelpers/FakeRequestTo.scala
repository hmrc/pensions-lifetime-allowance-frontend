/*
 * Copyright 2021 HM Revenue & Customs
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


import akka.actor.ActorSystem
import play.api.mvc.{Action, AnyContent, AnyContentAsEmpty}
import play.api.test.FakeRequest
import org.scalatest.{Matchers, OptionValues, WordSpecLike}
import org.jsoup._
import auth._
import akka.stream.{ActorMaterializer, Materializer}
import play.api.test.Helpers._
import uk.gov.hmrc.http.SessionKeys

class FakeRequestTo(url: String, controllerAction: Action[AnyContent], sessionId: Option[String], data: (String, String)*)
  extends WordSpecLike with Matchers with OptionValues {
  implicit val system = ActorSystem("test")
  implicit def mat: Materializer = ActorMaterializer()
  val fakeRequest = constructRequest(url, sessionId)
  val result = controllerAction(fakeRequest)
  val jsoupDoc = Jsoup.parse(contentAsString(result))

  def constructRequest(url: String, sessionId: Option[String]): FakeRequest[AnyContentAsEmpty.type] = {
    sessionId match {
      case Some(sessId) => FakeRequest("GET", "/protect-your-lifetime-allowance/" + url).withSession(SessionKeys.sessionId -> s"session-$sessionId")
      case None => FakeRequest("GET", "/protect-your-lifetime-allowance/" + url)
    }
  }
}

class AuthorisedFakeRequestTo(controllerAction: Action[AnyContent]) extends WordSpecLike with Matchers with OptionValues {
  implicit val system = ActorSystem("test")
  implicit def mat: Materializer = ActorMaterializer()
  val result = controllerAction(authenticatedFakeRequest())
  val jsoupDoc = Jsoup.parse(contentAsString(result))
}
