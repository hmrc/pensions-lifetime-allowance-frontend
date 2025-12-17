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
  val ssoUrl: Option[String]
  val citizenAuthHost: Option[String]
  val excludeCopeTab: Boolean
  val identityVerification: Boolean
  val psalookupjourneyShutterEnabled: Boolean
  val confirmFPUrl: String
  val ipStartUrl: String
  val ip14StartUrl: String
  val existingProtectionsUrl: String
  val notAuthorisedRedirectUrl: String
  val verifySignIn = s"$citizenAuthHost/ida/login"
  val ivUpliftUrl: String
  val ggSignInUrl: String
  val ptaFrontendUrl: String
  val feedbackSurvey: String
  val validStatusMetric: String
  val invalidStatusMetric: String
  val notFoundStatusMetric: String
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

  override val ssoUrl: Option[String] = configuration.getOptional[String](s"portal.ssoUrl")

  override val excludeCopeTab: Boolean =
    configuration.getOptional[Boolean](s"microservice.services.exclusions.copetab").getOrElse(true)

  override val identityVerification: Boolean =
    configuration.getOptional[Boolean]("microservice.services.features.identityVerification").getOrElse(false)

  override val psalookupjourneyShutterEnabled: Boolean =
    configuration
      .getOptional[Boolean]("microservice.services.features.psa-lookup-journeyShutterEnabled")
      .getOrElse(false)

  override val citizenAuthHost: Option[String] = configuration.getOptional[String]("citizen-auth.host")
  override val confirmFPUrl: String            = configuration.getOptional[String]("confirmFP.url").getOrElse("")
  override val ipStartUrl: String              = configuration.getOptional[String]("ipStart.url").getOrElse("")
  override val ip14StartUrl: String            = configuration.getOptional[String]("ip14Start.url").getOrElse("")

  override val existingProtectionsUrl: String =
    configuration.getOptional[String]("existingProtections.url").getOrElse("")

  override val ptaFrontendUrl: String = configuration.getOptional[String]("pta-frontend.url").getOrElse("")

  override val notAuthorisedRedirectUrl: String = servicesConfig.getString("not-authorised-callback.url")

  override val sessionMissingUpliftUrlPrefix: Option[String] =
    configuration.getOptional[String]("login-missing-session.url.prefix")

  override val ivUpliftUrl: String =
    configuration.getOptional[String](s"identity-verification-uplift.host").getOrElse("")

  override val ggSignInUrl: String = configuration.getOptional[String](s"government-gateway-sign-in.host").getOrElse("")

  override val feedbackSurvey: String = servicesConfig.getString("feedback-frontend.url")

  override val validStatusMetric: String    = servicesConfig.getString("valid-protection-status")
  override val invalidStatusMetric: String  = servicesConfig.getString("invalid-protection-status")
  override val notFoundStatusMetric: String = servicesConfig.getString("not-found-protection-status")

  override val appName: String = loadConfig("appName")

  override def accessibilityFrontendUrl(implicit requestHeader: RequestHeader): String =
    accessibilityStatementConfig.url.getOrElse("")

  override val basGatewaySignOutUrl: String = servicesConfig.getString("bas-gateway-frontend.sign-out-url")

  override val backendUrl: String = servicesConfig.baseUrl("pensions-lifetime-allowance")

}
