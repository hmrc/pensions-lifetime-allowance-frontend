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

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalTime, ZoneId}

import connectors.{KeyStoreConnector, PLAConnector, PdfGeneratorConnector}
import forms.{PSALookupProtectionNotificationNoForm, PSALookupSchemeAdministratorReferenceForm}
import models.{PSALookupRequest, PSALookupResult}
import play.api.Play.current
import play.api.data.{Form, FormError}
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
  val pdfGeneratorConnector: PdfGeneratorConnector = PdfGeneratorConnector

  val psaRefForm: Form[String] = PSALookupSchemeAdministratorReferenceForm.psaRefForm
  val pnnForm: Form[String] = PSALookupProtectionNotificationNoForm.pnnForm

  val notFoundLookupForm: Form[String] = pnnForm.copy(errors = Seq(FormError(" ", "psa.lookup.form.not-found")))

  val lookupRequestID = "psa-lookup-request"
  val lookupResultID = "psa-lookup-result"

}

trait LookupController extends BaseController {

  implicit val anyContentBodyParser: BodyParser[AnyContent] = parse.anyContent

  val keyStoreConnector: KeyStoreConnector
  val plaConnector: PLAConnector
  val pdfGeneratorConnector: PdfGeneratorConnector

  val psaRefForm: Form[String]
  val pnnForm: Form[String]

  val notFoundLookupForm: Form[String]

  val lookupRequestID: String
  val lookupResultID: String

  def displaySchemeAdministratorReferenceForm: Action[AnyContent] = ActionWithSessionId.async { implicit request =>
    keyStoreConnector.fetchAndGetFormData[PSALookupRequest](lookupRequestID).flatMap {
      case Some(PSALookupRequest(psaRef)) => Future.successful(Ok(psa_lookup_scheme_admin_ref_form(psaRefForm.fill(psaRef))))
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
          case Some(PSALookupRequest(psaRef)) =>
            val pnn = validFormData.toUpperCase
            plaConnector.psaLookup(psaRef, pnn).flatMap {
              result =>
                result.status match {
                  case OK =>
                    val resultData = Json.fromJson[PSALookupResult](result.json).get
                    val updatedResult = resultData.copy(protectionNotificationNumber = Some(pnn))
                    keyStoreConnector.saveFormData[PSALookupResult](lookupResultID, updatedResult).map {
                      _ => Redirect(routes.LookupController.displayLookupResults())
                    }
                }
            }.recover {
              case r: Upstream4xxResponse if r.upstreamResponseCode == NOT_FOUND => BadRequest(psa_lookup_protection_notification_no_form(notFoundLookupForm))
            }
        }
      }
    )
  }

  def displayLookupResults: Action[AnyContent] = ActionWithSessionId.async { implicit request =>
    keyStoreConnector.fetchAndGetFormData[PSALookupResult](lookupResultID).map {
      case Some(result) => Ok(psa_lookup_results(result, buildTimestamp))
      case None => Redirect(routes.LookupController.displaySchemeAdministratorReferenceForm())
    }
  }

  def redirectToStart: Action[AnyContent] = ActionWithSessionId.async { implicit request =>
    keyStoreConnector.remove.map { _ => Redirect(routes.LookupController.displaySchemeAdministratorReferenceForm())
    }
  }

  def printPDF: Action[AnyContent] = ActionWithSessionId.async {
    implicit request =>
      keyStoreConnector.fetchAndGetFormData[PSALookupResult](lookupResultID).flatMap {
        case Some(result) =>
          val printPage = psa_lookup_print(result, buildTimestamp).toString
          pdfGeneratorConnector.generatePdf(printPage).map {
            response =>
              Ok(response.bodyAsBytes.toArray).as("application/pdf")
                .withHeaders("Content-Disposition" ->
                  s"attachment; filename=lookup-result-${result.protectionNotificationNumber.getOrElse("")}.pdf")
          }
      }
  }

  def buildTimestamp: String = s"${LocalDate.now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))} at ${LocalTime.now(ZoneId.of("Europe/London")).format(DateTimeFormatter.ofPattern("HH:mm:ss"))}"

}
