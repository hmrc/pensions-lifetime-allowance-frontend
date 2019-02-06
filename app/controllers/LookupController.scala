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

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalTime, ZoneId}

import config.LocalTemplateRenderer
import config.wiring.PlaFormPartialRetriever
import connectors.{KeyStoreConnector, PLAConnector}
import forms.{PSALookupProtectionNotificationNoForm, PSALookupSchemeAdministratorReferenceForm}
import javax.inject.Inject
import models.{PSALookupRequest, PSALookupResult}
import play.api.Play.current
import play.api.data.Form
import play.api.i18n.Messages.Implicits._
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.http.Upstream4xxResponse
import utils.ActionWithSessionId
import views.html.pages.lookup._

import scala.concurrent.Future

class LookupController @Inject()(val keyStoreConnector: KeyStoreConnector,
                                 val plaConnector: PLAConnector,
                                 implicit val partialRetriever: PlaFormPartialRetriever,
                                 implicit val templateRenderer:LocalTemplateRenderer) extends BaseController {

  val psaRefForm: Form[String] = PSALookupSchemeAdministratorReferenceForm.psaRefForm
  val pnnForm: Form[String] = PSALookupProtectionNotificationNoForm.pnnForm

  val lookupRequestID = "psa-lookup-request"
  val lookupResultID = "psa-lookup-result"

  def displaySchemeAdministratorReferenceForm: Action[AnyContent] = ActionWithSessionId.async { implicit request =>
    keyStoreConnector.fetchAndGetFormData[PSALookupRequest](lookupRequestID).flatMap {
      case Some(PSALookupRequest(psaRef, _)) => Future.successful(Ok(psa_lookup_scheme_admin_ref_form(psaRefForm.fill(psaRef))))
      case _ => Future.successful(Ok(psa_lookup_scheme_admin_ref_form(psaRefForm)))
    }
  }

  def submitSchemeAdministratorReferenceForm: Action[AnyContent] = ActionWithSessionId.async { implicit request =>
    psaRefForm.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(psa_lookup_scheme_admin_ref_form(formWithErrors))),
      validFormData => {
        keyStoreConnector.saveFormData[PSALookupRequest](lookupRequestID, PSALookupRequest(validFormData)).map {
          _ => Redirect(routes.LookupController.displayProtectionNotificationNoForm())
        }
      }
    )
  }

  def displayProtectionNotificationNoForm: Action[AnyContent] = ActionWithSessionId.async { implicit request =>
    keyStoreConnector.fetchAndGetFormData[PSALookupRequest](lookupRequestID).flatMap {
      case Some(_) => Future.successful(Ok(psa_lookup_protection_notification_no_form(pnnForm)))
      case _ => Future.successful(Redirect(routes.LookupController.displaySchemeAdministratorReferenceForm()))
    }
  }

  def submitProtectionNotificationNoForm: Action[AnyContent] = ActionWithSessionId.async { implicit request =>
    pnnForm.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(psa_lookup_protection_notification_no_form(formWithErrors))),
      validFormData => {
        keyStoreConnector.fetchAndGetFormData[PSALookupRequest](lookupRequestID).flatMap {
          case Some(PSALookupRequest(psaRef, _)) =>
            val pnn = validFormData.toUpperCase
            plaConnector.psaLookup(psaRef, pnn).flatMap {
              result =>
                val resultData = Json.fromJson[PSALookupResult](result.json).get
                val updatedResult = resultData.copy(protectionNotificationNumber = Some(pnn))
                keyStoreConnector.saveFormData[PSALookupResult](lookupResultID, updatedResult).map {
                  _ => Redirect(routes.LookupController.displayLookupResults())
                }
            }.recoverWith {
              case r: Upstream4xxResponse if r.upstreamResponseCode == NOT_FOUND =>
                val fullResult = PSALookupRequest(psaRef, Some(pnn))
                keyStoreConnector.saveFormData[PSALookupRequest](lookupRequestID, fullResult).map {
                  _ => Redirect(routes.LookupController.displayNotFoundResults())
                }
            }
          case _ =>
            Future.successful(Redirect(routes.LookupController.displaySchemeAdministratorReferenceForm()))
        }
      }
    )
  }

  def displayNotFoundResults: Action[AnyContent] = ActionWithSessionId.async { implicit request =>
    keyStoreConnector.fetchAndGetFormData[PSALookupRequest](lookupRequestID).flatMap {
      case Some(req@PSALookupRequest(_, Some(_))) => Future.successful(Ok(psa_lookup_not_found_results(req, buildTimestamp)))
      case _ => Future.successful(Redirect(routes.LookupController.displaySchemeAdministratorReferenceForm()))
    }
  }

  def displayLookupResults: Action[AnyContent] = ActionWithSessionId.async { implicit request =>
    keyStoreConnector.fetchAndGetFormData[PSALookupResult](lookupResultID).map {
      case Some(result) => Ok(psa_lookup_results(result, buildTimestamp))
      case None => Redirect(routes.LookupController.displaySchemeAdministratorReferenceForm())
    }
  }

  def displayProtectionTypeGuidance: Action[AnyContent] = ActionWithSessionId { implicit request =>
    Ok(pla_protection_guidance())
  }

  def redirectToStart: Action[AnyContent] = ActionWithSessionId.async { implicit request =>
    keyStoreConnector.remove.map { _ => Redirect(routes.LookupController.displaySchemeAdministratorReferenceForm())
    }
  }

  def buildTimestamp: String = s"${LocalDate.now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))} at ${LocalTime.now(ZoneId.of("Europe/London")).format(DateTimeFormatter.ofPattern("HH:mm:ss"))}"

}