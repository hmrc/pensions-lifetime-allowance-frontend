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

package controllers

import auth.AuthFunction
import config.wiring.PlaFormPartialRetriever
import config.{FrontendAppConfig, LocalTemplateRenderer, PlaContext}
import connectors.KeyStoreConnector
import constructors.SummaryConstructor
import enums.ApplicationType
import javax.inject.Inject
import play.api.Logger
import play.api.Play.current
import play.api.i18n.{I18nSupport, Lang}
import play.api.mvc._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html._

import scala.concurrent.ExecutionContext.Implicits.global

class SummaryController @Inject()(keyStoreConnector: KeyStoreConnector,
                                 mcc: MessagesControllerComponents,
                                  authFunction: AuthFunction)
                                 (implicit val appConfig: FrontendAppConfig,
                                  implicit val partialRetriever: PlaFormPartialRetriever,
                                  implicit val templateRenderer:LocalTemplateRenderer,
                                  implicit val plaContext: PlaContext) extends FrontendController(mcc) with I18nSupport {

  lazy val postSignInRedirectUrl = appConfig.ipStartUrl
  val summaryConstructor: SummaryConstructor = SummaryConstructor

  val summaryIP16 = Action.async { implicit request =>
    implicit val protectionType = ApplicationType.IP2016
    authFunction.genericAuthWithNino("IP2016") { nino =>
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
    implicit val lang: Lang = mcc.messagesApi.preferred(req).lang
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