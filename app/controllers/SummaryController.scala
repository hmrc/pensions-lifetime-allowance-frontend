/*
 * Copyright 2016 HM Revenue & Customs
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

import auth.{PLAUser, AuthorisedForPLA}
import config.{FrontendAppConfig,FrontendAuthConnector}
import enums.ApplicationType
import play.api.Logger
import play.api.mvc.{Result, AnyContent, Request}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.frontend.controller.FrontendController
import connectors.KeyStoreConnector
import views.html._
import constructors.SummaryConstructor
import play.api.i18n.Messages.Implicits._
import play.api.Play.current


object SummaryController extends SummaryController {
  // $COVERAGE-OFF$
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val postSignInRedirectUrl = FrontendAppConfig.ipStartUrl

  val keyStoreConnector = KeyStoreConnector
  val summaryConstructor = SummaryConstructor
  // $COVERAGE-ON$
}

trait SummaryController extends FrontendController with AuthorisedForPLA {

  val keyStoreConnector : KeyStoreConnector
  val summaryConstructor: SummaryConstructor

  val summaryIP16 = AuthorisedByAny.async { implicit user => implicit request =>
    implicit val protectionType = ApplicationType.IP2016
    keyStoreConnector.fetchAllUserData.map {
      case Some(data) => routeIP2016SummaryFromUserData(data)
      case None => {
        Logger.error(s"unable to fetch summary IP16 data from keystore for user nino ${user.nino}")
        InternalServerError(views.html.pages.fallback.technicalError(protectionType.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
      }
    }
  }

  private def routeIP2016SummaryFromUserData(data: CacheMap)(implicit protectionType: ApplicationType.Value, req: Request[AnyContent], user: PLAUser) : Result = {
    summaryConstructor.createSummaryData(data).map {
      summaryModel => Ok(pages.ip2016.summary(summaryModel))
    }.getOrElse {
      Logger.error(s"Unable to create IP16 summary model from summary data for user nino ${user.nino}")
      InternalServerError(views.html.pages.fallback.technicalError(protectionType.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
    }
  }

  val summaryIP14 = AuthorisedByAny.async { implicit user => implicit request =>
    implicit val protectionType = ApplicationType.IP2014
    keyStoreConnector.fetchAllUserData.map {
      case Some(data) => routeIP2014SummaryFromUserData(data)
      case None => {
        Logger.error(s"unable to fetch summary IP14 data from keystore for user nino ${user.nino}")
        InternalServerError(views.html.pages.fallback.technicalError(protectionType.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
      }
    }
  }

  private def routeIP2014SummaryFromUserData(data: CacheMap)(implicit protectionType: ApplicationType.Value, req: Request[AnyContent], user: PLAUser) : Result = {
    summaryConstructor.createSummaryData(data).map {
      summaryModel => Ok(pages.ip2014.ip14Summary(summaryModel))
    }.getOrElse{
      Logger.error(s"Unable to create IP14 summary model from summary data for user nino ${user.nino}")
      InternalServerError(views.html.pages.fallback.technicalError(protectionType.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
    }
  }

  // returns true if the passed ID corresponds to a data field which requires GA monitoring
  def recordDataMetrics(rowId: String): Boolean = {
    val dataMetricsIds = List("pensionsTaken", "pensionsTakenBefore", "pensionsTakenBetween", "overseasPensions", "pensionDebits", "numberOfPSOsAmt")
    dataMetricsIds.map{_.toLowerCase}.contains(rowId.stripPrefix("ip14").toLowerCase)
  }

}
