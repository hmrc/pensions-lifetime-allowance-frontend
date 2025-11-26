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

package testHelpers

import auth.authenticatedFakeRequest
import play.api.mvc.{Action, AnyContent, AnyContentAsEmpty, AnyContentAsFormUrlEncoded, Result}
import play.api.test.FakeRequest
import uk.gov.hmrc.http.SessionKeys

import scala.concurrent.Future

object FakeRequests {

  def get(url: String, controllerAction: Action[AnyContent], sessionId: Option[String]): Future[Result] = {
    def constructRequest(url: String, sessionId: Option[String]): FakeRequest[AnyContentAsEmpty.type] =
      sessionId match {
        case Some(sessId) =>
          FakeRequest("GET", "/check-your-pension-protections-and-enhancements/" + url).withSession(
            SessionKeys.sessionId -> s"session-$sessId"
          )
        case None => FakeRequest("GET", "/check-your-pension-protections-and-enhancements/" + url)
      }

    controllerAction(constructRequest(url, sessionId))

  }

  def authorisedGet(controllerAction: Action[AnyContent]): Future[Result] =
    controllerAction(authenticatedFakeRequest())

  def post(
      url: String,
      controllerAction: Action[AnyContent],
      sessionId: String,
      data: (String, String)*
  ): Future[Result] = {

    val fakeRequest: FakeRequest[AnyContentAsFormUrlEncoded] =
      FakeRequest("POST", "/check-your-pension-protections-and-enhancements/" + url)
        .withSession(SessionKeys.sessionId -> s"session-$sessionId")
        .withFormUrlEncodedBody(data: _*)
        .withMethod("POST")

    controllerAction(fakeRequest)
  }

  def authorisedPost(controllerAction: Action[AnyContent], data: (String, String)*): Future[Result] =
    controllerAction(authenticatedFakeRequest().withFormUrlEncodedBody(data: _*).withMethod("POST"))

}
