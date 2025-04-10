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

import java.util.UUID

import org.apache.pekko.stream.Materializer
import javax.inject.Inject
import play.api.mvc._
import uk.gov.hmrc.http.{HeaderNames, SessionKeys}
import utils.SessionIdSupport._

import scala.concurrent.Future

/*
 * These utils provide session-id that is required by http-caching-client / keystore.
 * Normally you'd get session-id for free if you use authenticated actions but we don't
 * in this project.
 *
 * SessionIdFilter will add a header with a newly generated session-id and
 * ActionWithSessionId will make sure it stays saved in the session.
 */

object SessionIdSupport {

  def maybeSessionId(rh: RequestHeader): Option[String] =
    rh.session.get(SessionKeys.sessionId).orElse(rh.headers.get(HeaderNames.xSessionId))

  def hasSessionId(rh: RequestHeader): Boolean = maybeSessionId(rh).isDefined
}

class SessionIdFilter @Inject() (val mat: Materializer) extends Filter {

  def apply(next: RequestHeader => Future[Result])(rh: RequestHeader): Future[Result] =
    if (hasSessionId(rh)) {
      next(rh)
    } else {
      next(addNewSessionIdToHeaders(rh))
    }

  def addNewSessionIdToHeaders(request: RequestHeader): RequestHeader = {
    val newSessionId       = s"session-${UUID.randomUUID().toString}"
    val newSessionIdHeader = HeaderNames.xSessionId -> newSessionId
    val newHeaders         = request.headers.add(newSessionIdHeader)
    request.withHeaders(newHeaders)
  }

}
