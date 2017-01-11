/*
 * Copyright 2017 HM Revenue & Customs
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
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.{SessionKeys, HeaderCarrier}
import uk.gov.hmrc.play.http.logging.SessionId
import play.api.mvc._
import scala.concurrent.Future
import play.api.i18n.Messages.Implicits._
import play.api.Play.current


object IntroductionController extends IntroductionController

trait IntroductionController extends FrontendController {

    implicit val hc = new HeaderCarrier()

    def introduction:Action[AnyContent] = Action.async { implicit request =>
        if (request.session.get(SessionKeys.sessionId).isEmpty) {
            val sessionId = UUID.randomUUID.toString
            Future.successful(Ok(views.html.pages.introduction()).withSession(request.session + (SessionKeys.sessionId -> s"session-$sessionId")))
        }
        else {
            Future.successful(Ok(views.html.pages.introduction()))
        }
    }
}
