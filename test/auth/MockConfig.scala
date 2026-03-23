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
import play.api.mvc.RequestHeader

object MockConfig extends AppConfig {

  override def accessibilityFrontendUrl(implicit requestHeader: RequestHeader): String = "_"
  override val notAuthorisedRedirectUrl: String                                        = "/pla/not-authorised"
  override val ivUpliftUrl: String                                                     = "/iv/uplift"
  override val ggSignInUrl: String                                                     = "/gg/sign-in"
  override val feedbackSurvey: String                        = "http://localhost:9514/feedback/PLA"
  override val appName: String                               = "check-your-pension-protections-and-enhancements"
  override val sessionMissingUpliftUrlPrefix: Option[String] = None
  override val basGatewaySignOutUrl: String = "http://localhost:9553/bas-gateway/sign-out-without-state"
  override val backendUrl: String           = "http://localhost:9011"

}
