/*
 * Copyright 2016 HM Revenue & Customs
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

import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._
import scala.concurrent.Future

object HelloWorld extends HelloWorld {
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val postSignInRedirectUrl = FrontendAppConfig.applyUrl
}

trait HelloWorld extends FrontendController with AuthorisedForPLA {

  val helloWorld = AuthorisedByAny.async {
    implicit user =>  implicit request => Future.successful(Ok(views.html.helloworld.hello_user(user.nino.getOrElse(("NOBODY")))))
  }

  // TODO just a placeholder at the moment
  val timeout = Action.async { implicit request =>
    Future.successful(Ok(views.html.helloworld.hello_world()))
  }
}
