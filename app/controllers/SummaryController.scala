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

package controllers

import auth.AuthFunction
import config.{AuthClientConnector, FrontendAppConfig}
import enums.ApplicationType
import play.api.{Configuration, Environment, Logger, Play}
import play.api.mvc.{Action, AnyContent, Request, Result}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.frontend.controller.FrontendController
import connectors.KeyStoreConnector
import views.html._
import constructors.SummaryConstructor
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import uk.gov.hmrc.auth.core.retrieve.Retrievals
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions, Enrolment}
import uk.gov.hmrc.play.frontend.config.AuthRedirects

import scala.concurrent.Future


object SummaryController extends SummaryController {
  // $COVERAGE-OFF$
  lazy val appConfig = FrontendAppConfig
  override lazy val authConnector: AuthConnector = AuthClientConnector
  lazy val postSignInRedirectUrl = FrontendAppConfig.ipStartUrl

  val keyStoreConnector = KeyStoreConnector
  val summaryConstructor = SummaryConstructor

  override def config: Configuration = Play.current.configuration
  override def env: Environment = Play.current.injector.instanceOf[Environment]
  // $COVERAGE-ON$
}

trait SummaryController extends BaseController with AuthFunction {

  val keyStoreConnector : KeyStoreConnector
  val summaryConstructor: SummaryConstructor

  val summaryIP16 = Action.async { implicit request =>
    implicit val protectionType = ApplicationType.IP2016
    genericAuthWithNino("IP2016") { nino =>
      keyStoreConnector.fetchAllUserData.map {
        case Some(data) => routeIP2016SummaryFromUserData(data, nino)
        case None => {
          Logger.warn(s"unable to fetch summary IP16 data from keystore for user nino $nino")
          InternalServerError(views.html.pages.fallback.technicalError(protectionType.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
        }
      }
    }
  }

  private def routeIP2016SummaryFromUserData(data: CacheMap, nino: String)(implicit protectionType: ApplicationType.Value, req: Request[AnyContent]) : Result = {
      summaryConstructor.createSummaryData(data).map {
        summaryModel => Ok(pages.ip2016.summary(summaryModel))
      }.getOrElse {
        Logger.warn(s"Unable to create IP16 summary model from summary data for user nino $nino")
        InternalServerError(views.html.pages.fallback.technicalError(protectionType.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
      }
    }

  // returns true if the passed ID corresponds to a data field which requires GA monitoring
  def recordDataMetrics(rowId: String): Boolean = {
    val dataMetricsIds = List("pensionsTaken", "pensionsTakenBefore", "pensionsTakenBetween", "overseasPensions", "pensionDebits", "numberOfPSOsAmt")
    dataMetricsIds.map{_.toLowerCase}.contains(rowId.stripPrefix("ip14").toLowerCase)
  }

}
