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

import config.{AuthClientConnector, FrontendAppConfig}
import java.util.UUID

import auth.AuthFunction
import play.api.mvc._
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future
import forms.ExitSurveyForm.exitSurveyForm
import models._
import play.api.{Configuration, Environment, Play}
import play.api.Play.configuration
import views.html._
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import uk.gov.hmrc.auth.core.retrieve.Retrievals
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions, Enrolment}
import uk.gov.hmrc.play.frontend.config.AuthRedirects

object ExitSurveyController extends ExitSurveyController {
    lazy val appConfig = FrontendAppConfig
    override lazy val authConnector: AuthConnector = AuthClientConnector
    lazy val postSignInRedirectUrl = FrontendAppConfig.ipStartUrl

    override def config: Configuration = Play.current.configuration

    override def env: Environment = Play.current.injector.instanceOf[Environment]
}

trait ExitSurveyController extends BaseController with AuthFunction {

    val exitSurvey = Action.async { implicit request =>
            Future.successful(Ok(pages.exitSurvey.exitSurvey(exitSurveyForm)))

    }
}
