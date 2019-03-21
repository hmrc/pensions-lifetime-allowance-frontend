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

import java.time.{LocalDate, LocalTime, ZoneId}
import java.time.format.DateTimeFormatter

import config.{LocalTemplateRenderer, PlaContext, PlaContextImpl}
import config.wiring.PlaFormPartialRetriever
import connectors.{KeyStoreConnector, PdfGeneratorConnector}
import javax.inject.Inject
import models.{PSALookupRequest, PSALookupResult}
import play.api.Logger
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import utils.ActionWithSessionId
import views.html.pages.lookup.{psa_lookup_not_found_print, psa_lookup_results_print}
import play.api.i18n.{I18nSupport, Lang}
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PrintPdfController@Inject()(val keyStoreConnector: KeyStoreConnector,
                                  pdfGeneratorConnector: PdfGeneratorConnector,
                                  http: DefaultHttpClient,
                                  mcc: MessagesControllerComponents) extends FrontendController(mcc) with I18nSupport{
  val lookupRequestID = "psa-lookup-request"
  val lookupResultID = "psa-lookup-result"

  def printResultsPDF= ActionWithSessionId.async {
    implicit request =>
      keyStoreConnector.fetchAndGetFormData[PSALookupResult](lookupResultID).flatMap {
        case Some(result) =>
          val printPage = psa_lookup_results_print(result, buildTimestamp).toString
          pdfGeneratorConnector.generatePdf(printPage).map {
            response =>
              createPdfResult(response)
                .withHeaders("Content-Disposition" ->
                  s"attachment; filename=lookup-result-${result.protectionNotificationNumber.getOrElse("")}.pdf")
          }
        case None =>
          Logger.warn("[PrintPdfController]: Unable to print ResultsPDF. Redirected to displaySchemeAdministratorReferenceForm")
          Future.successful(Redirect(routes.LookupController.displaySchemeAdministratorReferenceForm()))
      }
  }

  def createPdfResult(response: WSResponse): Result = {
    Ok(response.bodyAsBytes.toArray).as("application/pdf")
  }

  def printNotFoundPDF = ActionWithSessionId.async {
    implicit request =>
      keyStoreConnector.fetchAndGetFormData[PSALookupRequest](lookupRequestID).flatMap {
        case Some(req@PSALookupRequest(_, Some(_))) =>
          val printPage = psa_lookup_not_found_print(req, buildTimestamp).toString

          pdfGeneratorConnector.generatePdf(printPage).map {
            response =>
              Ok(response.bodyAsBytes.toArray).as("application/pdf")
                .withHeaders("Content-Disposition" ->
                  "attachment; filename=lookup-not-found.pdf")
          }

        case Some(req@PSALookupRequest(_, None)) =>
          Logger.warn("[PrintPdfController]: lifetimeAllowanceReference is not defined, unable to print NotFound PDF. Redirected to displaySchemeAdministratorReferenceForm")
          Future.successful(Redirect(routes.LookupController.displaySchemeAdministratorReferenceForm()))
        case None =>
          Logger.warn("[PrintPdfController]: Unable to print NotFound PDF. Redirected to displaySchemeAdministratorReferenceForm")
          Future.successful(Redirect(routes.LookupController.displaySchemeAdministratorReferenceForm()))
      }
  }

  def buildTimestamp: String = s"${LocalDate.now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))} at ${LocalTime.now(ZoneId.of("Europe/London")).format(DateTimeFormatter.ofPattern("HH:mm:ss"))}"

}