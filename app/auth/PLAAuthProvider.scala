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

import play.api.mvc.Request
import uk.gov.hmrc.play.frontend.auth.{AnyAuthenticationProvider, GovernmentGateway, Verify}

import scala.concurrent.Future

class PLAAuthProvider(postSignInRedirectUrl: String, verifyProviderUrl: String, ggLoginUrl: String) extends AnyAuthenticationProvider {
  val ggwProvider = new GovernmentGatewayProvider(postSignInRedirectUrl, ggLoginUrl)
  val verifyProvider = new VerifyProvider(postSignInRedirectUrl, verifyProviderUrl)

  override def ggwAuthenticationProvider: GovernmentGateway = ggwProvider
  override def verifyAuthenticationProvider: Verify = verifyProvider
  override def login: String = s"${ggwProvider.loginURL}?" + s"continue=${ggwProvider.continueURL}&accountType=individual"
  override def handleSessionTimeout(implicit request: Request[_]): Future[FailureResult] = GovernmentGatewayProvider.handleSessionTimeout
}