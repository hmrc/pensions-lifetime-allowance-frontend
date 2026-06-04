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
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.{Duration, DurationInt}

@Singleton
class AppConfig @Inject() (
    val configuration: Configuration,
    val servicesConfig: ServicesConfig
) {

  val psaLookupWithdrawLinkUrl: String = servicesConfig.getString("psa.lookup.withdrawLink.url")

  val urBannerLink: String = servicesConfig.getString("user-research.banner-link")

  val notAuthorisedRedirectUrl: String = servicesConfig.getString("not-authorised-callback.url")

  val sessionMissingUpliftUrlPrefix: Option[String] =
    configuration.getOptional[String]("login-missing-session.url.prefix")

  val ivUpliftUrl: String =
    configuration.getOptional[String](s"identity-verification-uplift.host").getOrElse("")

  val ggSignInUrl: String = configuration.getOptional[String](s"government-gateway-sign-in.host").getOrElse("")

  val appName: String = servicesConfig.getString("appName")

  val backendUrl: String = servicesConfig.baseUrl("pensions-lifetime-allowance")

  val citizenDetailsBaseUrl: String = servicesConfig.baseUrl("citizen-details")

  val identityVerificationBaseUrl: String = servicesConfig.baseUrl("identity-verification")

  val stubBaseUrl: String = servicesConfig.baseUrl("pla-dynamic-stub")

  private val platformFrontendHost: Option[String] = configuration.getOptional[String]("platform.frontend.host")

  private val pertaxFrontendBaseUrl =
    platformFrontendHost
      .getOrElse {
        servicesConfig.getString("microservice.services.pertax-frontend.baseUrl")
      }

  val serviceNavigationAccountHomeUrl: String =
    pertaxFrontendBaseUrl + servicesConfig.getString("microservice.services.pertax-frontend.urls.home")

  val serviceNavigationMessagesUrl: String =
    pertaxFrontendBaseUrl + servicesConfig.getString("microservice.services.pertax-frontend.urls.messages")

  private val trackingFrontendBaseUrl =
    platformFrontendHost
      .getOrElse {
        servicesConfig.getString("microservice.services.tracking-frontend.baseUrl")
      }

  val serviceNavigationCheckProgressUrl: String =
    trackingFrontendBaseUrl + servicesConfig.getString("microservice.services.tracking-frontend.urls.home")

  val serviceNavigationProfileAndSettingsUrl: String =
    pertaxFrontendBaseUrl + servicesConfig.getString("microservice.services.pertax-frontend.urls.profile-and-settings")

  val mongoTtl: Duration = configuration.get[Int]("mongodb.timeToLiveInSeconds").seconds

  private val basGatewaySignOutUrl: String = servicesConfig.getString("bas-gateway-frontend.sign-out-url")

  private val feedbackSurvey: String = servicesConfig.getString("feedback-frontend.url")

  val fullSignOutUrl: String = s"$basGatewaySignOutUrl?continue=$feedbackSurvey"

}
