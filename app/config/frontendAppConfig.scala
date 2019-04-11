/*
 * Copyright 2019 HM Revenue & Customs
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
import play.api.Play._
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

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
}

class FrontendAppConfig @Inject()(val servicesConfig: ServicesConfig) extends AppConfig with PlaConfig {

  private def loadConfig(key: String) = servicesConfig.getString(key)

  private val contactFrontendService = s"$baseUrl/contact-frontend"
  private val contactHost = servicesConfig.getString(s"contact-frontend.host")
  private val baseUrl = "protect-your-lifetime-allowance"

  override lazy val betaFeedbackUrl = s"$baseUrl/feedback"
  override lazy val betaFeedbackUnauthenticatedUrl = betaFeedbackUrl
  override lazy val analyticsToken: String = servicesConfig.getString(s"google-analytics.token")
  override lazy val analyticsHost: String = servicesConfig.getString(s"google-analytics.host")
  override lazy val ssoUrl: Option[String] = Some(servicesConfig.getString(s"portal.ssoUrl"))

  override val contactFormServiceIdentifier = "PLA"
  override lazy val contactFrontendPartialBaseUrl = s"$contactFrontendService"
  override lazy val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  override lazy val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  override val excludeCopeTab: Boolean = servicesConfig.getBoolean(s"microservice.services.exclusions.copetab")
  override val identityVerification: Boolean = servicesConfig.getBoolean("microservice.services.features.identityVerification")

  override lazy val citizenAuthHost = Some(servicesConfig.getString("citizen-auth.host"))
  override lazy val confirmFPUrl = servicesConfig.getString("confirmFP.url")
  override lazy val ipStartUrl = servicesConfig.getString("ipStart.url")
  override lazy val ip14StartUrl = servicesConfig.getString("ip14Start.url")
  override lazy val existingProtectionsUrl = servicesConfig.getString("existingProtections.url")
  override lazy val ptaFrontendUrl = servicesConfig.getString("pta-frontend.url")

  override lazy val notAuthorisedRedirectUrl = servicesConfig.getString("not-authorised-callback.url")
  override val ivUpliftUrl: String = servicesConfig.getString(s"identity-verification-uplift.host")
  override val ggSignInUrl: String = servicesConfig.getString(s"government-gateway-sign-in.host")

  override val feedbackSurvey: String = servicesConfig.getString(s"feedback-survey-frontend.url")

  override val validStatusMetric: String = servicesConfig.getString("valid-protection-status")
  override val invalidStatusMetric: String = servicesConfig.getString("invalid-protection-status")
  override val notFoundStatusMetric: String = servicesConfig.getString("not-found-protection-status")

  override lazy val appName: String = loadConfig("appName")
  lazy val frontendTemplatePath: String = servicesConfig.getString("microservice.services.frontend-template-provider.path")

}
