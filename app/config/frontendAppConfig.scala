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

package config

import play.api.Configuration
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.Inject

trait AppConfig {
  val citizenAuthHost: Option[String]
  val notAuthorisedRedirectUrl: String
  val ivUpliftUrl: String
  val ggSignInUrl: String
  val feedbackSurvey: String
  val appName: String
  val sessionMissingUpliftUrlPrefix: Option[String]
  val configuration: Configuration
  val basGatewaySignOutUrl: String
  val backendUrl: String
  def accessibilityFrontendUrl(implicit requestHeader: RequestHeader): String

  def fullSignOutUrl: String = s"$basGatewaySignOutUrl?continue=$feedbackSurvey"

}

class FrontendAppConfig @Inject() (
    val configuration: Configuration,
    val servicesConfig: ServicesConfig,
    accessibilityStatementConfig: AccessibilityStatementConfig
) extends AppConfig {

  private def loadConfig(key: String) =
    configuration.getOptional[String](key).getOrElse(throw new Exception(s"Missing key: $key"))

  val signOutUrl = "/check-your-pension-protections-and-enhancements/sign-out"

  val psaLookupWithdrawLinkUrl: String = configuration.get[String]("psa.lookup.withdrawLink.url")

  val urBannerLink =
    "https://signup.take-part-in-research.service.gov.uk/?utm_campaign=PLA_success&utm_source=Survey_Banner&utm_medium=other&t=HMRC&id=113"

  override val citizenAuthHost: Option[String] = configuration.getOptional[String]("citizen-auth.host")

  override val notAuthorisedRedirectUrl: String = servicesConfig.getString("not-authorised-callback.url")

  override val sessionMissingUpliftUrlPrefix: Option[String] =
    configuration.getOptional[String]("login-missing-session.url.prefix")

  override val ivUpliftUrl: String =
    configuration.getOptional[String](s"identity-verification-uplift.host").getOrElse("")

  override val ggSignInUrl: String = configuration.getOptional[String](s"government-gateway-sign-in.host").getOrElse("")

  override val feedbackSurvey: String = servicesConfig.getString("feedback-frontend.url")

  override val appName: String = loadConfig("appName")

  override def accessibilityFrontendUrl(implicit requestHeader: RequestHeader): String =
    accessibilityStatementConfig.url.getOrElse("")

  override val basGatewaySignOutUrl: String = servicesConfig.getString("bas-gateway-frontend.sign-out-url")

  override val backendUrl: String = servicesConfig.baseUrl("pensions-lifetime-allowance")

}
