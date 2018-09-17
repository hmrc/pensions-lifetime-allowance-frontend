/*
 * Copyright 2018 HM Revenue & Customs
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
import config.wiring.PlaFormPartialRetriever
import config.{AuthClientConnector, FrontendAppConfig, LocalTemplateRenderer}
import javax.inject.Inject
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc._
import play.api.{Configuration, Environment, Play}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.pages._

import scala.concurrent.Future

class ConfirmationController @Inject()(implicit val partialRetriever: PlaFormPartialRetriever,
                                       implicit val templateRenderer: LocalTemplateRenderer) extends BaseController with AuthFunction {
  lazy val appConfig = FrontendAppConfig
  val authConnector: AuthConnector = AuthClientConnector
  lazy val postSignInRedirectUrl = appConfig.confirmFPUrl

  override def config: Configuration = Play.current.configuration

  override def env: Environment = Play.current.injector.instanceOf[Environment]

  val confirmFP = Action.async {
    implicit request =>
      genericAuthWithoutNino("FP2016") {
        Future.successful(Ok(confirmation.confirmFP()))
      }
  }

}
