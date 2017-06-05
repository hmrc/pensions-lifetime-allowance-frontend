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
import forms.{PSALookupProtectionNotificationNoForm, PSALookupSchemeAdministratorReferenceForm}
import models.{PSALookupRequest, PSALookupResult}
import play.api.Play.current
import play.api.data.Form
import play.api.i18n.Messages.Implicits._
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import utils.ActionWithSessionId
import views.html.pages.lookup._

import scala.concurrent.Future

object LookupController extends LookupController {
  val keyStoreConnector = KeyStoreConnector
  val plaConnector = PLAConnector

  val psaRefForm: Form[String] = PSALookupSchemeAdministratorReferenceForm.psaRefForm
  val pnnForm: Form[String] = PSALookupProtectionNotificationNoForm.pnnForm

  val lookupRequestID = "psa-lookup-request"
  val lookupResultID = "psa-lookup-result"
}

trait LookupController extends FrontendController {

  implicit val anyContentBodyParser: BodyParser[AnyContent] = parse.anyContent

  val keyStoreConnector: KeyStoreConnector
  val plaConnector: PLAConnector

  val psaRefForm: Form[String]
  val pnnForm: Form[String]

  val lookupRequestID: String
  val lookupResultID: String

  def displaySchemeAdministratorReferenceForm: Action[AnyContent] = ActionWithSessionId.async { implicit request =>
    keyStoreConnector.fetchAndGetFormData[PSALookupRequest](lookupRequestID).flatMap {
      case Some(PSALookupRequest(psaRef)) =>
        Future.successful(Ok(psa_lookup_scheme_admin_ref_form(psaRefForm.fill(psaRef))))
      case _ =>
        Future.successful(Ok(psa_lookup_scheme_admin_ref_form(psaRefForm)))
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
      case Some(PSALookupRequest(_)) =>
        Future.successful(Ok(psa_lookup_protection_notification_no_form(pnnForm)))
      case _ =>
        Future.successful(Redirect(routes.LookupController.displaySchemeAdministratorReferenceForm()))
    }
  }

  def submitProtectionNotificationNoForm: Action[AnyContent] = ActionWithSessionId.async { implicit request =>
    pnnForm.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(psa_lookup_protection_notification_no_form(formWithErrors))),
      validFormData => {
        keyStoreConnector.fetchAndGetFormData[PSALookupRequest](lookupRequestID).flatMap {
          case Some(PSALookupRequest(psaRef)) =>
            plaConnector.psaLookup(psaRef, validFormData).flatMap {
              result =>
                result.status match {
                  case OK =>
                    val resultData = Json.fromJson[PSALookupResult](result.json).get
                    val updatedResult = resultData.copy(pnnNumber = Some(validFormData))
                    keyStoreConnector.saveFormData[PSALookupResult](lookupResultID, updatedResult).map {
                      _ => Redirect(routes.LookupController.displayLookupResults())
                    }
                }
            }
          //Need to add recover back in once the flow has been determined
          //case r: Upstream4xxResponse if r.upstreamResponseCode == NOT_FOUND => BadRequest(psa_lookup_form(notFoundLookupForm))
        }
      }
    )
  }

  def displayLookupResults: Action[AnyContent] = ActionWithSessionId.async { implicit request =>
    keyStoreConnector.fetchAndGetFormData[PSALookupResult](lookupResultID).map {
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
  //private val notFoundLookupForm = pnnForm.copy(errors = Seq(FormError(" ", "psa.lookup.form.not-found")))

}
