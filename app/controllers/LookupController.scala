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

import config.FrontendAppConfig
import models.{PsaLookupRequest, PsaLookupResult}
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.ActionWithSessionId
import views.html.pages.lookup.{
  pla_protection_guidance,
  psa_lookup_not_found_results,
  psa_lookup_results,
  withdrawnPSALookupJourney
}

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalTime, ZoneId}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LookupController @Inject() (
    sessionCacheService: SessionCacheService,
    actionWithSessionId: ActionWithSessionId,
    mcc: MessagesControllerComponents,
    psa_lookup_not_found_results: psa_lookup_not_found_results,
    pla_protection_guidance: pla_protection_guidance,
    psa_lookup_results: psa_lookup_results,
    withdrawnPSALookupJourney: withdrawnPSALookupJourney
)(implicit appConfig: FrontendAppConfig, ec: ExecutionContext)
    extends FrontendController(mcc)
    with I18nSupport {

  private val lookupRequestID = "psa-lookup-request"
  private val lookupResultID  = "psa-lookup-result"

  def displayNotFoundResults: Action[AnyContent] = actionWithSessionId.async { implicit request =>
    if (appConfig.psalookupjourneyShutterEnabled) {
      Future.successful(Ok(withdrawnPSALookupJourney()))
    } else {
      sessionCacheService
        .fetchAndGetFormData[PsaLookupRequest](lookupRequestID)
        .flatMap {
          case Some(req @ PsaLookupRequest(_, Some(_))) =>
            Future.successful(Ok(psa_lookup_not_found_results(req, buildTimestamp)))
          case _ =>
            Future.successful(
              Redirect(routes.LookupSchemeAdministratorReferenceController.displaySchemeAdministratorReferenceForm)
            )
        }
    }
  }

  def displayLookupResults: Action[AnyContent] = actionWithSessionId.async { implicit request =>
    if (appConfig.psalookupjourneyShutterEnabled) {
      Future.successful(Ok(withdrawnPSALookupJourney()))
    } else {
      sessionCacheService
        .fetchAndGetFormData[PsaLookupResult](lookupResultID)
        .map {
          case Some(result) => Ok(psa_lookup_results(result, buildTimestamp))
          case None =>
            Redirect(routes.LookupSchemeAdministratorReferenceController.displaySchemeAdministratorReferenceForm)
        }
    }
  }

  def displayProtectionTypeGuidance: Action[AnyContent] =
    actionWithSessionId { implicit request =>
      if (appConfig.psalookupjourneyShutterEnabled) {
        Ok(withdrawnPSALookupJourney())
      } else {
        Ok(pla_protection_guidance())
      }
    }

  def redirectToStart: Action[AnyContent] = actionWithSessionId.async { implicit request =>
    if (appConfig.psalookupjourneyShutterEnabled) {
      Future.successful(Ok(withdrawnPSALookupJourney()))
    } else {
      sessionCacheService.remove.map { _ =>
        Redirect(routes.LookupSchemeAdministratorReferenceController.displaySchemeAdministratorReferenceForm)
      }
    }
  }

  private def buildTimestamp: String = s"${LocalDate.now.format(
      DateTimeFormatter
        .ofPattern("dd/MM/yyyy")
    )} at ${LocalTime
      .now(ZoneId.of("Europe/London"))
      .format(DateTimeFormatter.ofPattern("HH:mm:ss"))}"

}
