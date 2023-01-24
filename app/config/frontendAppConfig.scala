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

import javax.inject.Inject
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import play.api.mvc.RequestHeader

trait AppConfig {
  val betaFeedbackUrl: String
  val betaFeedbackUnauthenticatedUrl: String
  val analyticsToken: String
  val analyticsHost: String
  val ssoUrl: Option[String]
  val citizenAuthHost: Option[String]
  val contactFormServiceIdentifier: String
  val contactFrontendPartialBaseUrl: String
  val reportAProblemPartialUrl: String
  val reportAProblemNonJSUrl: String
  val excludeCopeTab: Boolean
  val identityVerification: Boolean
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
  val appName : String
  val frontendTemplatePath: String
  val sessionMissingUpliftUrlPrefix: Option[String]
  val configuration: Configuration
  def accessibilityFrontendUrl(implicit requestHeader: RequestHeader): String

}

class FrontendAppConfig @Inject()(val configuration: Configuration, val servicesConfig: ServicesConfig, accessibilityStatementConfig: AccessibilityStatementConfig) extends AppConfig {

  private def loadConfig(key: String) = configuration.getOptional[String](key).getOrElse(throw new Exception(s"Missing key: $key"))

  private val contactFrontendService = servicesConfig.baseUrl("contact-frontend")
  private val contactHost = servicesConfig.getConfString("contact-frontend.www", "")
  private val baseUrl = "protect-your-lifetime-allowance"
  lazy val signOutUrl = "/protect-your-lifetime-allowance/sign-out"

  lazy val urBannerLink = "https://signup.take-part-in-research.service.gov.uk/?utm_campaign=PLA_success&utm_source=Survey_Banner&utm_medium=other&t=HMRC&id=113"
  override lazy val betaFeedbackUrl = s"$baseUrl/feedback"
  override lazy val betaFeedbackUnauthenticatedUrl = betaFeedbackUrl
  override lazy val analyticsToken: String = configuration.getOptional[String](s"google-analytics.token").getOrElse("")
  override lazy val analyticsHost: String = configuration.getOptional[String](s"google-analytics.host").getOrElse("auto")
  override lazy val ssoUrl: Option[String] = configuration.getOptional[String](s"portal.ssoUrl")

  lazy val ptaBaseUrl: String = servicesConfig.baseUrl("pertax-frontend")
  lazy val ptaHomeUrl: String = s"$ptaBaseUrl${servicesConfig.getConfString("pertax-frontend.urls.home","/personal-account")}"
  lazy val messagesUrl: String = s"$ptaBaseUrl${servicesConfig.getConfString("pertax-frontend.urls.messages","/messages")}"
  lazy val paperlessSettingsUrl = s"$ptaBaseUrl${servicesConfig.getConfString("pertax-frontend.urls.paperlessSettings","/preferences")}"
  lazy val personalDetailsUrl = s"$ptaBaseUrl${servicesConfig.getConfString("pertax-frontend.urls.personalDetails","/personal-details")}"
  lazy val trackBaseUrl: String = servicesConfig.baseUrl("tracking-frontend")
  lazy val trackingHomeUrl = s"$trackBaseUrl${servicesConfig.getConfString("tracking-frontend.urls.home","/track")}"

  override val contactFormServiceIdentifier = "PLA"
  override lazy val contactFrontendPartialBaseUrl = s"$contactFrontendService"
  override lazy val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  override lazy val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  override val excludeCopeTab: Boolean = configuration.getOptional[Boolean](s"microservice.services.exclusions.copetab").getOrElse(true)
  override val identityVerification: Boolean = configuration.getOptional[Boolean]("microservice.services.features.identityVerification").getOrElse(false)

  override lazy val citizenAuthHost = configuration.getOptional[String]("citizen-auth.host")
  override lazy val confirmFPUrl = configuration.getOptional[String]("confirmFP.url").getOrElse("")
  override lazy val ipStartUrl = configuration.getOptional[String]("ipStart.url").getOrElse("")
  override lazy val ip14StartUrl = configuration.getOptional[String]("ip14Start.url").getOrElse("")
  override lazy val existingProtectionsUrl = configuration.getOptional[String]("existingProtections.url").getOrElse("")
  override lazy val ptaFrontendUrl = configuration.getOptional[String]("pta-frontend.url").getOrElse("")

  override lazy val notAuthorisedRedirectUrl = servicesConfig.getString("not-authorised-callback.url")
  override lazy val sessionMissingUpliftUrlPrefix = configuration.getOptional[String]("login-missing-session.url.prefix")
  override val ivUpliftUrl: String = configuration.getOptional[String](s"identity-verification-uplift.host").getOrElse("")
  override val ggSignInUrl: String = configuration.getOptional[String](s"government-gateway-sign-in.host").getOrElse("")

  override val feedbackSurvey: String = servicesConfig.getString("feedback-frontend.url")

  override val validStatusMetric: String = servicesConfig.getString("valid-protection-status")
  override val invalidStatusMetric: String = servicesConfig.getString("invalid-protection-status")
  override val notFoundStatusMetric: String = servicesConfig.getString("not-found-protection-status")

  override lazy val appName: String = loadConfig("appName")

  lazy val frontendTemplatePath: String = configuration.getOptional[String]("microservice.services.frontend-template-provider.path")
    .getOrElse("/template/mustache")

  override def accessibilityFrontendUrl(implicit requestHeader: RequestHeader): String = accessibilityStatementConfig.url.getOrElse("")
}

