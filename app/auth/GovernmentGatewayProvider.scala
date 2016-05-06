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

package auth

import java.net.URLEncoder

import play.api.mvc._
import play.api.mvc.Results.Redirect
import config.FrontendAppConfig
import uk.gov.hmrc.play.frontend.auth.GovernmentGateway
import controllers.routes

import scala.concurrent.Future

class GovernmentGatewayProvider(postSignInRedirectUrl: String) extends GovernmentGateway {
  override def handleSessionTimeout(implicit request: Request[_]): Future[FailureResult] =
    GovernmentGatewayProvider.handleSessionTimeout(request)
  override def additionalLoginParameters = GovernmentGatewayProvider.additionalLoginParameters
  override def continueURL = URLEncoder.encode(postSignInRedirectUrl, "UTF-8")
  override def loginURL: String = GovernmentGatewayProvider.loginURL
}

object GovernmentGatewayProvider {
  def handleSessionTimeout(implicit request: Request[_]) = Future.successful(Redirect(routes.HelloWorld.timeout().url))
  val additionalLoginParameters = Map("accountType" -> Seq("individual"))
  val loginURL: String = FrontendAppConfig.ggSignInUrl
}

