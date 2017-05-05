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

import javax.inject.{Inject, Singleton}

import connectors.{KeyStoreConnector, PLAConnector}
import forms.PSALookupRequestForm.pSALookupRequestForm
import models.{PSALookupRequest, PSALookupResult}
import play.api.Application
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import utils.ActionWithSessionId
import views.html._

import scala.concurrent.Future

@Singleton
class LookupController @Inject()(val messagesApi: MessagesApi,
                                 implicit val application: Application) extends FrontendController with play.api.i18n.I18nSupport {

  implicit val anyContentBodyParser: BodyParser[AnyContent] = parse.anyContent

  def displayLookupForm: Action[AnyContent] = ActionWithSessionId.async { implicit request =>
    KeyStoreConnector.fetchAndGetFormData[PSALookupRequest]("psa-lookup-request").map {
      case Some(result) =>
        val form = pSALookupRequestForm.fill(result)
        Ok(pages.lookup.psa_lookup_form(form))
      case None => Ok(pages.lookup.psa_lookup_form(pSALookupRequestForm))
    }
  }

  def submitLookupRequest: Action[AnyContent] = ActionWithSessionId.async { implicit request =>
    pSALookupRequestForm.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(pages.lookup.psa_lookup_form(formWithErrors))),
      validFormData => {
        KeyStoreConnector.saveFormData[PSALookupRequest]("psa-lookup-request", validFormData).flatMap {
          _ =>
            PLAConnector.psaLookup(validFormData.pensionSchemeAdministratorCheckReference, validFormData.lifetimeAllowanceReference).flatMap {
              result =>
                result.status match {
                  case OK =>
                    KeyStoreConnector.saveFormData[PSALookupResult]("psa-lookup-result", Json.fromJson[PSALookupResult](result.json).get).map {
                      _ => Redirect(routes.LookupController.displayLookupResults())
                    }
                }
            }
        }
      }
    )
  }

  def displayLookupResults: Action[AnyContent] = ActionWithSessionId.async { implicit request =>
    KeyStoreConnector.fetchAndGetFormData[PSALookupResult]("psa-lookup-result").map {
      case Some(result) => Ok(pages.lookup.psa_lookup_results(result))
      case None => Redirect(routes.LookupController.displayLookupForm())
    }
  }


}
