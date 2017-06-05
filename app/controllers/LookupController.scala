/*
 * Copyright 2017 HM Revenue & Customs
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

import connectors.{KeyStoreConnector, PLAConnector}
import forms.PSALookupProtectionNotificationNoForm.pSALookupProtectionNotificationNoForm
import forms.PSALookupRequestForm.pSALookupRequestForm
import forms.PSALookupSchemeAdministratorReferenceForm.pSALookupSchemeAdministratorReferenceForm
import models.{PSALookupResult, PSALookupSchemeAdministratorReferenceRequest}
import play.api.Play.current
import play.api.data.FormError
import play.api.i18n.Messages.Implicits._
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.Upstream4xxResponse
import utils.ActionWithSessionId
import views.html.pages.lookup._

import scala.concurrent.Future

object LookupController extends LookupController {
  val keyStoreConnector = KeyStoreConnector
  val plaConnector = PLAConnector
}

trait LookupController extends FrontendController {

  implicit val anyContentBodyParser: BodyParser[AnyContent] = parse.anyContent

  val keyStoreConnector: KeyStoreConnector
  val plaConnector: PLAConnector

  def displaySchemeAdministratorReferenceForm: Action[AnyContent] = ActionWithSessionId.async { implicit request =>
    keyStoreConnector.fetchAndGetFormData[PSALookupSchemeAdministratorReferenceRequest]("pensionSchemeAdministratorCheckReference").flatMap {
      case Some(stored) =>
        Future.successful(Ok(psa_lookup_scheme_admin_ref_form(pSALookupSchemeAdministratorReferenceForm.fill(stored))))
      case _ =>
        Future.successful(Ok(psa_lookup_scheme_admin_ref_form(pSALookupSchemeAdministratorReferenceForm)))
    }
  }

  def submitSchemeAdministratorReferenceForm: Action[AnyContent] = ActionWithSessionId.async { implicit request =>
    pSALookupSchemeAdministratorReferenceForm.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(psa_lookup_scheme_admin_ref_form(formWithErrors))),
      validFormData => {
        keyStoreConnector.saveFormData[PSALookupSchemeAdministratorReferenceRequest]("pensionSchemeAdministratorCheckReference", validFormData).map {
          _ => Redirect(routes.LookupController.displayProtectionNotificationNoForm())
        }
      }
    )
  }

  def displayProtectionNotificationNoForm: Action[AnyContent] = ActionWithSessionId.async { implicit request =>
    keyStoreConnector.fetchAndGetFormData[PSALookupSchemeAdministratorReferenceRequest]("pensionSchemeAdministratorCheckReference").flatMap {
      case Some(_) =>
        Future.successful(Ok(psa_lookup_protection_notification_no_form(pSALookupProtectionNotificationNoForm)))
      case _ =>
        Future.successful(Redirect(routes.LookupController.displaySchemeAdministratorReferenceForm()))
    }
  }

  def submitProtectionNotificationNoForm: Action[AnyContent] = ActionWithSessionId.async { implicit request =>
    pSALookupProtectionNotificationNoForm.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(psa_lookup_protection_notification_no_form(formWithErrors))),
      validFormData => {
        keyStoreConnector.fetchAndGetFormData[PSALookupSchemeAdministratorReferenceRequest]("pensionSchemeAdministratorCheckReference").flatMap {
          case Some(stored) =>
            plaConnector.psaLookup(stored.pensionSchemeAdministratorCheckReference, validFormData.lifetimeAllowanceReference).flatMap {
              result =>
                result.status match {
                  case OK =>
                    val resultData = Json.fromJson[PSALookupResult](result.json).get
                    //if (resultData.psaCheckResult == 0) Future.successful(BadRequest(psa_lookup_form(notFoundLookupForm)))
                    keyStoreConnector.saveFormData[PSALookupResult]("psa-lookup-result", resultData).map {
                      _ => Redirect(routes.LookupController.displayLookupResults())
                    }
                }
            }
            //Need to add recover back in once the flow has been determined
              //case r: Upstream4xxResponse if r.upstreamResponseCode == NOT_FOUND => BadRequest(psa_lookup_form(notFoundLookupForm))
          case _ =>
            throw new RuntimeException("unable to get the pensionSchemeAdministratorCheckReference from the keyStoreConnector")
        }
      }
    )
  }

  def displayLookupResults: Action[AnyContent] = ActionWithSessionId.async { implicit request =>
    keyStoreConnector.fetchAndGetFormData[PSALookupResult]("psa-lookup-result").map {
      case Some(result) => Ok(psa_lookup_results(result))
      case None => Redirect(routes.LookupController.displaySchemeAdministratorReferenceForm())
    }
  }

  /**
    *
    * Creates the form errors to be displayed when a 404 is returned from DES
    *
    * @return
    */
  private val notFoundLookupForm = pSALookupRequestForm.copy(errors = Seq(FormError(" ", "psa.lookup.form.not-found")))

}
