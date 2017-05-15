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

package auth

import config.AppConfig

object MockConfig extends AppConfig {
  override val betaFeedbackUrl: String = ""
  override val betaFeedbackUnauthenticatedUrl = ""
  override val analyticsToken: String = ""
  override val analyticsHost: String = ""
  override val ssoUrl: Option[String] = None
  override val citizenAuthHost: Option[String] = None
  override val contactFormServiceIdentifier: String = ""
  override val contactFrontendPartialBaseUrl: String = ""
  override val reportAProblemPartialUrl: String = ""
  override val reportAProblemNonJSUrl: String = ""
  override val excludeCopeTab: Boolean = false
  override val identityVerification: Boolean = true
  override val confirmFPUrl: String = "/pla/apply-for-fp16"
  override val ipStartUrl: String = "/pla/apply-for-ip16-pensions-taken"
  override val ip14StartUrl: String = "/pla/apply-for-ip14-pensions-taken"
  override val existingProtectionsUrl: String = "/pla/existing-protections"
  override val notAuthorisedRedirectUrl: String = "/pla/not-authorised"
  override val verifySignIn = "/verify/login"
  override val ivUpliftUrl: String = "/iv/uplift"
  override val twoFactorUrl: String = "/two-step-verification/register/"
  override val ggSignInUrl: String = "/gg/sign-in"
  override val ptaFrontendUrl: String = ""
  override val feedbackSurvey: String = "http://localhost:9514/feedback-survey/?origin=PLA"
}
