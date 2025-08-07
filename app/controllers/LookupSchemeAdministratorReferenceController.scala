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
import forms.PSALookupSchemeAdministratorReferenceForm
import models.PSALookupRequest
import play.api.Application
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.SessionCacheService
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.ActionWithSessionId
import views.html._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LookupSchemeAdministratorReferenceController @Inject() (
    val sessionCacheService: SessionCacheService,
    val plaConnector: PLAConnector,
    val actionWithSessionId: ActionWithSessionId,
    mcc: MessagesControllerComponents,
    psa_lookup_scheme_admin_ref_form: views.html.pages.lookup.psa_lookup_scheme_admin_ref_form,
    withdrawnPSALookupJourney: pages.lookup.withdrawnPSALookupJourney
)(
    implicit val context: PlaContext,
    implicit val appConfig: FrontendAppConfig,
    implicit val formWithCSRF: FormWithCSRF,
    implicit val application: Application
) extends FrontendController(mcc)
    with I18nSupport {

  implicit val executionContext: ExecutionContext = mcc.executionContext
  implicit val parser: BodyParser[AnyContent]     = mcc.parsers.defaultBodyParser

  def psaRefForm(implicit request: Request[AnyContent]): Form[String] =
    PSALookupSchemeAdministratorReferenceForm.psaRefForm

  val lookupRequestID = "psa-lookup-request"
  val lookupResultID  = "psa-lookup-result"

  def displaySchemeAdministratorReferenceForm: Action[AnyContent] = actionWithSessionId.async { implicit request =>
    if (appConfig.psalookupjourneyShutterEnabled) {
      Future.successful(Ok(withdrawnPSALookupJourney()))
    } else {
      sessionCacheService
        .fetchAndGetFormData[PSALookupRequest](lookupRequestID)
        .flatMap {
          case Some(PSALookupRequest(psaRef, _)) =>
            Future.successful(Ok(psa_lookup_scheme_admin_ref_form(psaRefForm.fill(psaRef))))
          case _ => Future.successful(Ok(psa_lookup_scheme_admin_ref_form(psaRefForm)))
        }(executionContext)
    }
  }

  def submitSchemeAdministratorReferenceForm: Action[AnyContent] = actionWithSessionId.async { implicit request =>
    psaRefForm
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(psa_lookup_scheme_admin_ref_form(formWithErrors))),
        validFormData =>
          sessionCacheService
            .saveFormData[PSALookupRequest](lookupRequestID, PSALookupRequest(validFormData))
            .map(_ => Redirect(routes.LookupProtectionNotificationController.displayProtectionNotificationNoForm))(
              executionContext
            )
      )
  }

}
