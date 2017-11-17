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

import auth.AuthFunction
import config.{AuthClientConnector, FrontendAppConfig}
import play.api.mvc._

import scala.concurrent.Future
import play.api.{Configuration, Environment, Play}
import views.html.pages._
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import uk.gov.hmrc.auth.core.AuthConnector

object ConfirmationController extends ConfirmationController {
  lazy val appConfig = FrontendAppConfig
  override lazy val authConnector: AuthConnector = AuthClientConnector
  lazy val postSignInRedirectUrl = FrontendAppConfig.confirmFPUrl

  override def config: Configuration = Play.current.configuration

  override def env: Environment = Play.current.injector.instanceOf[Environment]
}

trait ConfirmationController extends BaseController with AuthFunction {

  val confirmFP = Action.async {
    implicit request =>
      genericAuthWithoutNino("FP2016") {
        Future.successful(Ok(confirmation.confirmFP()))
      }
  }

}
