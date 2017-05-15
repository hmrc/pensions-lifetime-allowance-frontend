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

import config.{AppConfig, FrontendAppConfig}
import play.api.mvc._
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

object AccountController extends AccountController {
  val applicationConfig: AppConfig = FrontendAppConfig
}

trait AccountController extends FrontendController {

  val applicationConfig: AppConfig

  def signOut: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Redirect(applicationConfig.feedbackSurvey).withNewSession)
  }
}
