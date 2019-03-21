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

package controllers

import config.{LocalTemplateRenderer, PlaContext, PlaContextImpl}
import config.wiring.PlaFormPartialRetriever
import javax.inject.Inject
import play.api.mvc._

import scala.concurrent.Future
import models._
import views.html._
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.i18n.{I18nSupport, Lang}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

class TimeoutController @Inject()(implicit val partialRetriever: PlaFormPartialRetriever,
                                  implicit val templateRenderer:LocalTemplateRenderer,
                                  implicit val context: PlaContext = PlaContextImpl,
                                  implicit val lang: Lang,
                                  mcc: MessagesControllerComponents) extends FrontendController(mcc) with I18nSupport {

  val timeout = Action.async { implicit request =>
    Future.successful(Ok(views.html.pages.timeout()))
  }
}
