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

package controllers

import auth.AuthFunction
import config.{FrontendAppConfig, PlaContext}
import constructors.SummaryConstructor
import enums.ApplicationType
import models.cache.CacheMap
import play.api.{Application, Logging}
import play.api.i18n.{I18nSupport, Lang}
import play.api.mvc._
import services.SessionCacheService
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SummaryController @Inject()(sessionCacheService: SessionCacheService,
                                 mcc: MessagesControllerComponents,
                                  authFunction: AuthFunction,
                                  technicalError: views.html.pages.fallback.technicalError,
                                  summary: pages.ip2016.summary,
                                  withdrawnIP2016: pages.ip2016.withdrawnAP2016
                                 )
                                 (implicit val appConfig: FrontendAppConfig,
                                  implicit val plaContext: PlaContext,
                                  implicit val formWithCSRF: FormWithCSRF,
                                  implicit val application: Application,
                                  implicit val ec: ExecutionContext)
extends FrontendController(mcc) with I18nSupport with Logging {

  val summaryConstructor: SummaryConstructor = SummaryConstructor

  val summaryIP16: Action[AnyContent] = Action.async { implicit request =>
    if (appConfig.applyFor2016IpAndFpShutterEnabled) {
      Future.successful(Ok(withdrawnIP2016()))
    }else {
      implicit val protectionType: ApplicationType.Value = ApplicationType.IP2016
      authFunction.genericAuthWithNino("IP2016") { nino =>
        sessionCacheService.fetchAllUserData.map {
          case Some(data) => routeIP2016SummaryFromUserData(data, nino)
          case None => {
            logger.warn(s"unable to fetch summary IP16 data from cache for user nino $nino")
            InternalServerError(technicalError(protectionType.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
          }
        }
      }
    }
  }

  private def routeIP2016SummaryFromUserData(data: CacheMap, nino: String)
                                            (implicit protectionType: ApplicationType.Value, req: Request[AnyContent]) : Result = {
    implicit val lang: Lang = mcc.messagesApi.preferred(req).lang
    summaryConstructor.createSummaryData(data).map {
        summaryModel => Ok(summary(summaryModel))
      }.getOrElse {
        logger.warn(s"Unable to create IP16 summary model from summary data for user nino $nino")
        InternalServerError(technicalError(protectionType.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
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