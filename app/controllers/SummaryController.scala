/*
 * Copyright 2018 HM Revenue & Customs
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
import config.wiring.PlaFormPartialRetriever
import config.{AuthClientConnector, FrontendAppConfig, LocalTemplateRenderer}
import enums.ApplicationType
import play.api.{Configuration, Environment, Logger, Play}
import play.api.mvc.{Action, AnyContent, Request, Result}
import uk.gov.hmrc.http.cache.client.CacheMap
import connectors.KeyStoreConnector
import views.html._
import constructors.SummaryConstructor
import javax.inject.Inject
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import uk.gov.hmrc.auth.core.AuthConnector



class SummaryController@Inject()(keyStoreConnector: KeyStoreConnector,
                                 implicit val partialRetriever: PlaFormPartialRetriever,
                                 implicit val templateRenderer:LocalTemplateRenderer) extends BaseController with AuthFunction {
  lazy val appConfig = FrontendAppConfig
  override lazy val authConnector: AuthConnector = AuthClientConnector
  lazy val postSignInRedirectUrl = FrontendAppConfig.ipStartUrl

  val summaryConstructor: SummaryConstructor = SummaryConstructor

  override def config: Configuration = Play.current.configuration
  override def env: Environment = Play.current.injector.instanceOf[Environment]


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

object SummaryController{
  // returns true if the passed ID corresponds to a data field which requires GA monitoring
  def recordDataMetrics(rowId: String): Boolean = {
    val dataMetricsIds = List("pensionsTaken", "pensionsTakenBefore", "pensionsTakenBetween", "overseasPensions", "pensionDebits", "numberOfPSOsAmt")
    dataMetricsIds.map{_.toLowerCase}.contains(rowId.stripPrefix("ip14").toLowerCase)
  }
}