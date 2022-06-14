/*
 * Copyright 2022 HM Revenue & Customs
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

import config.{FrontendAppConfig, LocalTemplateRenderer, PlaContext}
import connectors.{KeyStoreConnector, PLAConnector}
import forms.{PSALookupProtectionNotificationNoForm, PSALookupSchemeAdministratorReferenceForm}
import javax.inject.Inject
import models.{PSALookupRequest, PSALookupResult}
import play.api.Application
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.http.Upstream4xxResponse
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever
import uk.gov.hmrc.play.views.html.helpers.{ErrorSummary, FormWithCSRF}
import utils.ActionWithSessionId

import scala.concurrent.{ExecutionContext, Future}

class LookupController @Inject()(val keyStoreConnector: KeyStoreConnector,
                                 val plaConnector: PLAConnector,
                                 val actionWithSessionId: ActionWithSessionId,
                                 mcc: MessagesControllerComponents,
                                 psa_lookup_not_found_results: views.html.pages.lookup.psa_lookup_not_found_results,
                                 pla_protection_guidance: views.html.pages.lookup.pla_protection_guidance,
                                 psa_lookup_protection_notification_no_form: views.html.pages.lookup.psa_lookup_protection_notification_no_form,
                                 psa_lookup_results: views.html.pages.lookup.psa_lookup_results,
                                 psa_lookup_scheme_admin_ref_form: views.html.pages.lookup.psa_lookup_scheme_admin_ref_form)(
                                 implicit val partialRetriever: FormPartialRetriever,
                                 implicit val templateRenderer:LocalTemplateRenderer,
                                 implicit val context: PlaContext,
                                 implicit val appConfig: FrontendAppConfig,
                                 implicit val errorSummary: ErrorSummary,
                                 implicit val formWithCSRF: FormWithCSRF,
                                 implicit val application: Application) extends FrontendController(mcc) with I18nSupport {

  implicit val executionContext: ExecutionContext = mcc.executionContext
  implicit val parser: BodyParser[AnyContent] = mcc.parsers.defaultBodyParser

  def psaRefForm(implicit request: Request[AnyContent]): Form[String] = {
    PSALookupSchemeAdministratorReferenceForm.psaRefForm
  }
  def pnnForm(implicit request: Request[AnyContent]): Form[String] = {
    PSALookupProtectionNotificationNoForm.pnnForm
  }

  val lookupRequestID = "psa-lookup-request"
  val lookupResultID = "psa-lookup-result"

  def displaySchemeAdministratorReferenceForm: Action[AnyContent] = actionWithSessionId.async { implicit request =>
    keyStoreConnector.fetchAndGetFormData[PSALookupRequest](lookupRequestID).flatMap {
      case Some(PSALookupRequest(psaRef, _)) => Future.successful(Ok(psa_lookup_scheme_admin_ref_form(psaRefForm.fill(psaRef))))
      case _ => Future.successful(Ok(psa_lookup_scheme_admin_ref_form(psaRefForm)))
    }(executionContext)
  }

  def submitSchemeAdministratorReferenceForm: Action[AnyContent] = actionWithSessionId.async { implicit request =>
    psaRefForm.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(psa_lookup_scheme_admin_ref_form(formWithErrors))),
      validFormData => {
        keyStoreConnector.saveFormData[PSALookupRequest](lookupRequestID, PSALookupRequest(validFormData)).map {
          _ => Redirect(routes.LookupController.displayProtectionNotificationNoForm)
        }(executionContext)
      }
    )
  }

  def displayProtectionNotificationNoForm: Action[AnyContent] = actionWithSessionId.async { implicit request =>
    keyStoreConnector.fetchAndGetFormData[PSALookupRequest](lookupRequestID).flatMap {
      case Some(_) => Future.successful(Ok(psa_lookup_protection_notification_no_form(pnnForm)))
      case _ => Future.successful(Redirect(routes.LookupController.displaySchemeAdministratorReferenceForm))
    }(executionContext)
  }

  def submitProtectionNotificationNoForm: Action[AnyContent] = actionWithSessionId.async { implicit request =>
    pnnForm.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(psa_lookup_protection_notification_no_form(formWithErrors))),
      validFormData => {
        keyStoreConnector.fetchAndGetFormData[PSALookupRequest](lookupRequestID).flatMap {
          case Some(PSALookupRequest(psaRef, _)) =>
            val pnn = validFormData.toUpperCase
            plaConnector.psaLookup(psaRef, pnn)(hc, executionContext).flatMap {
              result =>
                val resultData = Json.fromJson[PSALookupResult](result.json).get
                val updatedResult = resultData.copy(protectionNotificationNumber = Some(pnn))
                keyStoreConnector.saveFormData[PSALookupResult](lookupResultID, updatedResult).map {
                  _ => Redirect(routes.LookupController.displayLookupResults)
                }(executionContext)
            }((executionContext)).recoverWith {
              case r: Upstream4xxResponse if r.upstreamResponseCode == NOT_FOUND =>
                val fullResult = PSALookupRequest(psaRef, Some(pnn))
                keyStoreConnector.saveFormData[PSALookupRequest](lookupRequestID, fullResult).map {
                  _ => Redirect(routes.LookupController.displayNotFoundResults)
                }((executionContext))
            }(executionContext)
          case _ =>
            Future.successful(Redirect(routes.LookupController.displaySchemeAdministratorReferenceForm))
        }(executionContext)
      }
    )
  }

  def displayNotFoundResults: Action[AnyContent] = actionWithSessionId.async { implicit request =>
    keyStoreConnector.fetchAndGetFormData[PSALookupRequest](lookupRequestID).flatMap {
      case Some(req@PSALookupRequest(_, Some(_))) => Future.successful(Ok(psa_lookup_not_found_results(req, buildTimestamp)))
      case _ => Future.successful(Redirect(routes.LookupController.displaySchemeAdministratorReferenceForm))
    }(executionContext)
  }

  def displayLookupResults: Action[AnyContent] = actionWithSessionId.async { implicit request =>
    keyStoreConnector.fetchAndGetFormData[PSALookupResult](lookupResultID).map {
      case Some(result) => Ok(psa_lookup_results(result, buildTimestamp))
      case None => Redirect(routes.LookupController.displaySchemeAdministratorReferenceForm)
    }(executionContext)
  }

  def displayProtectionTypeGuidance: Action[AnyContent] = actionWithSessionId { implicit request =>
    Ok(pla_protection_guidance())
  }

  def redirectToStart: Action[AnyContent] = actionWithSessionId.async { implicit request =>
    keyStoreConnector.remove.map { _ => Redirect(routes.LookupController.displaySchemeAdministratorReferenceForm)
    }(executionContext)
  }

  def buildTimestamp: String = s"${LocalDate.now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))} at ${LocalTime.now(ZoneId.of("Europe/London")).format(DateTimeFormatter.ofPattern("HH:mm:ss"))}"

}