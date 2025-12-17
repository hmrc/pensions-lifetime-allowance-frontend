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
import connectors.PsaLookupConnector
import forms.PsaLookupProtectionNotificationNoForm
import models.{PsaLookupRequest, PsaLookupResult}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._
import services.SessionCacheService
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.ActionWithSessionId
import views.html.pages.lookup.{psa_lookup_protection_notification_no_form, withdrawnPSALookupJourney}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LookupProtectionNotificationController @Inject() (
    sessionCacheService: SessionCacheService,
    plaConnector: PsaLookupConnector,
    actionWithSessionId: ActionWithSessionId,
    mcc: MessagesControllerComponents,
    psa_lookup_protection_notification_no_form: psa_lookup_protection_notification_no_form,
    withdrawnPSALookupJourney: withdrawnPSALookupJourney
)(implicit appConfig: FrontendAppConfig, ec: ExecutionContext)
    extends FrontendController(mcc)
    with I18nSupport {

  private def pnnForm(implicit request: Request[AnyContent]): Form[String] =
    PsaLookupProtectionNotificationNoForm.pnnForm

  def displayProtectionNotificationNoForm: Action[AnyContent] = actionWithSessionId.async { implicit request =>
    if (appConfig.psalookupjourneyShutterEnabled) {
      Future.successful(Ok(withdrawnPSALookupJourney()))
    } else {
      sessionCacheService.fetchPsaLookupRequest
        .flatMap {
          case Some(_) => Future.successful(Ok(psa_lookup_protection_notification_no_form(pnnForm)))
          case _ =>
            Future.successful(
              Redirect(routes.LookupSchemeAdministratorReferenceController.displaySchemeAdministratorReferenceForm)
            )
        }
    }
  }

  def submitProtectionNotificationNoForm: Action[AnyContent] = actionWithSessionId.async { implicit request =>
    if (appConfig.psalookupjourneyShutterEnabled) {
      Future.successful(Ok(withdrawnPSALookupJourney()))
    } else {
      pnnForm
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(psa_lookup_protection_notification_no_form(formWithErrors))),
          validFormData =>
            sessionCacheService.fetchPsaLookupRequest
              .flatMap {
                case Some(PsaLookupRequest(psaRef, _)) =>
                  val pnn = validFormData.toUpperCase
                  plaConnector
                    .psaLookup(psaRef, pnn)
                    .flatMap { result =>
                      val resultData    = Json.fromJson[PsaLookupResult](result.json).get
                      val updatedResult = resultData.copy(protectionNotificationNumber = Some(pnn))
                      sessionCacheService
                        .savePsaLookupResult(updatedResult)
                        .map(_ => Redirect(routes.LookupController.displayLookupResults))
                    }
                    .recoverWith {
                      case r: UpstreamErrorResponse if r.statusCode == NOT_FOUND =>
                        val fullResult = PsaLookupRequest(psaRef, Some(pnn))
                        sessionCacheService
                          .savePsaLookupRequest(fullResult)
                          .map(_ => Redirect(routes.LookupController.displayNotFoundResults))
                    }
                case _ =>
                  Future.successful(
                    Redirect(
                      routes.LookupSchemeAdministratorReferenceController.displaySchemeAdministratorReferenceForm
                    )
                  )
              }
        )
    }
  }

}
