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

package utils

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import uk.gov.hmrc.http.SessionKeys
import utils.SessionIdSupport.maybeSessionId

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ActionWithSessionImpl @Inject()(controllerComponents: ControllerComponents) extends ActionWithSessionId {
  override def parser: BodyParser[AnyContent]               = controllerComponents.parsers.defaultBodyParser
  override val executionContext: ExecutionContext = controllerComponents.executionContext
}

trait ActionWithSessionId extends ActionBuilder[Request, AnyContent] {
  override implicit val executionContext: ExecutionContext

  def invokeBlock[A](request: Request[A],
                     block: Request[A] => Future[Result]): Future[Result] = {

    maybeSessionId(request).map { sessionId =>
      block(request).map(addSessionIdToSession(request, sessionId))
    }.getOrElse {
      throw SessionIdNotFoundException()
    }
  }

  def addSessionIdToSession[A](request: Request[A], sessionId: String)
                              (result: Result): Result =
    result.withSession(request.session + (SessionKeys.sessionId -> sessionId))

  case class SessionIdNotFoundException() extends Exception(
    "Session id not found in headers or session as expected. Have you enabled SessionIdFilter?"
  )
}