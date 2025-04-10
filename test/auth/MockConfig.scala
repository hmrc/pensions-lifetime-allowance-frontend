/*
 * Copyright 2023 HM Revenue & Customs
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
import play.api.Configuration
import play.api.mvc.RequestHeader

object MockConfig extends AppConfig {

  val NoConfiguration = null

  override def accessibilityFrontendUrl(implicit requestHeader: RequestHeader): String = "_"
  override val ssoUrl: Option[String]                                                  = None
  override val citizenAuthHost: Option[String]                                         = None
  override val excludeCopeTab: Boolean                                                 = false
  override val identityVerification: Boolean                                           = true
  override val confirmFPUrl: String                                                    = "/pla/apply-for-fp16"
  override val ipStartUrl: String                            = "/pla/apply-for-ip16-pensions-taken"
  override val ip14StartUrl: String                          = "/pla/apply-for-ip14-pensions-taken"
  override val existingProtectionsUrl: String                = "/pla/existing-protections"
  override val notAuthorisedRedirectUrl: String              = "/pla/not-authorised"
  override val verifySignIn                                  = "/verify/login"
  override val ivUpliftUrl: String                           = "/iv/uplift"
  override val ggSignInUrl: String                           = "/gg/sign-in"
  override val ptaFrontendUrl: String                        = ""
  override val feedbackSurvey: String                        = "http://localhost:9514/feedback/PLA"
  override val validStatusMetric: String                     = ""
  override val invalidStatusMetric: String                   = ""
  override val notFoundStatusMetric: String                  = ""
  override val appName: String                               = ""
  override val sessionMissingUpliftUrlPrefix: Option[String] = None
  override val configuration: Configuration                  = NoConfiguration
  override val applyFor2016IpAndFpShutterEnabled: Boolean    = false
}
