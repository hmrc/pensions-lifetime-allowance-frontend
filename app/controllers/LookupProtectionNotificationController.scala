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

import config.{FrontendAppConfig, PlaContext}
import connectors.PLAConnector
import forms.PSALookupProtectionNotificationNoForm
import models.{PSALookupRequest, PSALookupResult}
import play.api.Application
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._
import services.SessionCacheService
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever
import utils.ActionWithSessionId

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LookupProtectionNotificationController @Inject()(val sessionCacheService: SessionCacheService,
                                                       val plaConnector: PLAConnector,
                                                       val actionWithSessionId: ActionWithSessionId,
                                                       mcc: MessagesControllerComponents,
                                                       psa_lookup_protection_notification_no_form: views.html.pages.lookup.psa_lookup_protection_notification_no_form)(
                                 implicit val partialRetriever: FormPartialRetriever,
                                 implicit val context: PlaContext,
                                 implicit val appConfig: FrontendAppConfig,
                                 implicit val formWithCSRF: FormWithCSRF,
                                 implicit val application: Application) extends FrontendController(mcc) with I18nSupport {

  implicit val executionContext: ExecutionContext = mcc.executionContext
  implicit val parser: BodyParser[AnyContent] = mcc.parsers.defaultBodyParser

  def pnnForm(implicit request: Request[AnyContent]): Form[String] = {
    PSALookupProtectionNotificationNoForm.pnnForm
  }

  val lookupRequestID = "psa-lookup-request"
  val lookupResultID = "psa-lookup-result"


  def displayProtectionNotificationNoForm: Action[AnyContent] = actionWithSessionId.async { implicit request =>
    sessionCacheService.fetchAndGetFormData[PSALookupRequest](lookupRequestID).flatMap {
      case Some(_) => Future.successful(Ok(psa_lookup_protection_notification_no_form(pnnForm)))
      case _ => Future.successful(Redirect(routes.LookupSchemeAdministratorReferenceController.displaySchemeAdministratorReferenceForm))
    }(executionContext)
  }

  def submitProtectionNotificationNoForm: Action[AnyContent] = actionWithSessionId.async { implicit request =>
    pnnForm.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(psa_lookup_protection_notification_no_form(formWithErrors))),
      validFormData => {
        sessionCacheService.fetchAndGetFormData[PSALookupRequest](lookupRequestID).flatMap {
          case Some(PSALookupRequest(psaRef, _)) =>
            val pnn = validFormData.toUpperCase
            plaConnector.psaLookup(psaRef, pnn)(hc, executionContext).flatMap {
              result =>
                val resultData = Json.fromJson[PSALookupResult](result.json).get
                val updatedResult = resultData.copy(protectionNotificationNumber = Some(pnn))
                sessionCacheService.saveFormData[PSALookupResult](lookupResultID, updatedResult).map {
                  _ => Redirect(routes.LookupController.displayLookupResults)
                }(executionContext)
            }(executionContext).recoverWith {
              case r: UpstreamErrorResponse if r.statusCode == NOT_FOUND =>
                val fullResult = PSALookupRequest(psaRef, Some(pnn))
                sessionCacheService.saveFormData[PSALookupRequest](lookupRequestID, fullResult).map {
                  _ => Redirect(routes.LookupController.displayNotFoundResults)
                }(executionContext)
            }(executionContext)
          case _ =>
            Future.successful(Redirect(routes.LookupSchemeAdministratorReferenceController.displaySchemeAdministratorReferenceForm))
        }(executionContext)
      }
    )
  }

}