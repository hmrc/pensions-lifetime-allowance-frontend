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

package controllers

import config.wiring.PlaFormPartialRetriever
import config.{FrontendAppConfig, LocalTemplateRenderer, PlaContext}
import javax.inject.Inject
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.Future


class WithdrawnController @Inject()(mcc: MessagesControllerComponents)
                                   (implicit val partialRetriever: PlaFormPartialRetriever,
                                    implicit val templateRenderer:LocalTemplateRenderer,
                                    implicit val appConfig: FrontendAppConfig,
                                    implicit val plaContext: PlaContext) extends FrontendController(mcc) {

  def showWithdrawn(): Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(views.html.pages.ip2014.withdrawn()))
  }
}
