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

import auth.AuthorisedForPLA
import config.{FrontendAppConfig,FrontendAuthConnector}

import java.util.UUID
import play.api.mvc._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.SessionKeys
import uk.gov.hmrc.play.http.logging.SessionId
import scala.concurrent.Future
import forms.ExitSurveyForm.exitSurveyForm
import models._

import views.html._

object ExitSurveyController extends ExitSurveyController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = FrontendAuthConnector
    override lazy val postSignInRedirectUrl = FrontendAppConfig.ipStartUrl
}

trait ExitSurveyController extends FrontendController with AuthorisedForPLA{

    val exitSurvey = AuthorisedByAny.async { implicit user => implicit request =>
        Future.successful(Ok(pages.exitSurvey.exitSurvey(exitSurveyForm)))
    }
}
