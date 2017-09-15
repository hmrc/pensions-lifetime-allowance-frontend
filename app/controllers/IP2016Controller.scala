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

import auth.{AuthorisedForPLA, PLAUser}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.KeyStoreConnector
import enums.ApplicationType
import play.api.Logger
import play.api.i18n.{Lang, Messages}
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future
import forms._
import forms.PensionsTakenForm.pensionsTakenForm
import forms.PensionsTakenBeforeForm.pensionsTakenBeforeForm
import forms.PensionsTakenBetweenForm.pensionsTakenBetweenForm
import forms.OverseasPensionsForm.overseasPensionsForm
import forms.CurrentPensionsForm.currentPensionsForm
import forms.PSODetailsForm.psoDetailsForm
import forms.PensionDebitsForm.pensionDebitsForm
import models._
import play.api.data.{Form, FormError}
import play.api.mvc.{Action, AnyContent}
import views.html._
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.util.{Failure, Success, Try}

object IP2016Controller extends IP2016Controller {

    val keyStoreConnector = KeyStoreConnector
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = FrontendAuthConnector
    override lazy val postSignInRedirectUrl = FrontendAppConfig.ipStartUrl
}

trait IP2016Controller extends BaseController with AuthorisedForPLA {

    val keyStoreConnector: KeyStoreConnector

    //PENSIONS TAKEN
    val pensionsTaken = AuthorisedByAny.async { implicit user => implicit request =>
        keyStoreConnector.fetchAndGetFormData[PensionsTakenModel]("pensionsTaken").map {
            case Some(data) => Ok(pages.ip2016.pensionsTaken(pensionsTakenForm.fill(data)))
            case None => Ok(pages.ip2016.pensionsTaken(pensionsTakenForm))
        }
    }

    val submitPensionsTaken = AuthorisedByAny.async { implicit user => implicit request =>
        pensionsTakenForm.bindFromRequest.fold(
            errors => {
                val form = errors.copy(errors = errors.errors.map { er => FormError(er.key, Messages(er.message))})
                Future.successful(BadRequest(pages.ip2016.pensionsTaken(form)))
            },
            success => {
                keyStoreConnector.saveFormData("pensionsTaken", success).map{
                    _ => success.pensionsTaken.get match {
                        case "yes"  => Redirect(routes.IP2016Controller.pensionsTakenBefore())
                        case "no"   => Redirect(routes.IP2016Controller.overseasPensions())
                    }
                }
            }
        )
    }


    //PENSIONS TAKEN BEFORE
    val pensionsTakenBefore = AuthorisedByAny.async { implicit user => implicit request =>

        keyStoreConnector.fetchAndGetFormData[PensionsTakenBeforeModel]("pensionsTakenBefore").map {
            case Some(data) => Ok(pages.ip2016.pensionsTakenBefore(pensionsTakenBeforeForm.fill(data)))
            case _ => Ok(pages.ip2016.pensionsTakenBefore(pensionsTakenBeforeForm))
        }
    }

    val submitPensionsTakenBefore = AuthorisedByAny.async { implicit user => implicit request =>

        pensionsTakenBeforeForm.bindFromRequest.fold(
            errors => {
                val form = errors.copy(errors = errors.errors.map { er => FormError(er.key, Messages(er.message))})
                Future.successful(BadRequest(pages.ip2016.pensionsTakenBefore(form)))
            },
            success => {
                val validatedForm = PensionsTakenBeforeForm.validateForm(pensionsTakenBeforeForm.fill(success))
                if(validatedForm.hasErrors) {
                    Future.successful(BadRequest(pages.ip2016.pensionsTakenBefore(validatedForm)))
                } else {
                    keyStoreConnector.saveFormData("pensionsTakenBefore", success).flatMap{
                        _=> Future.successful(Redirect(routes.IP2016Controller.pensionsTakenBetween()))}

                }
            }
        )
    }


    //PENSIONS TAKEN BETWEEN
    val pensionsTakenBetween = AuthorisedByAny.async { implicit user => implicit request =>

        keyStoreConnector.fetchAndGetFormData[PensionsTakenBetweenModel]("pensionsTakenBetween").map {
            case Some(data) => Ok(pages.ip2016.pensionsTakenBetween(pensionsTakenBetweenForm.fill(data)))
            case _ => Ok(pages.ip2016.pensionsTakenBetween(pensionsTakenBetweenForm))
        }
    }

    val submitPensionsTakenBetween = AuthorisedByAny.async { implicit user => implicit request =>

        pensionsTakenBetweenForm.bindFromRequest.fold(
            errors => {
                val form = errors.copy(errors = errors.errors.map { er => FormError(er.key, Messages(er.message))})
                Future.successful(BadRequest(pages.ip2016.pensionsTakenBetween(form)))
            },
            success => {
                val validatedForm = PensionsTakenBetweenForm.validateForm(pensionsTakenBetweenForm.fill(success))
                if(validatedForm.hasErrors) {
                    Future.successful(BadRequest(pages.ip2016.pensionsTakenBetween(validatedForm)))
                } else {
                    keyStoreConnector.saveFormData("pensionsTakenBetween", success).flatMap{
                        _=> Future.successful(Redirect(routes.IP2016Controller.overseasPensions()))}
                }
            }
        )
    }


    //OVERSEAS PENSIONS
    val overseasPensions = AuthorisedByAny.async { implicit user => implicit request =>

        keyStoreConnector.fetchAndGetFormData[OverseasPensionsModel]("overseasPensions").map {
            case Some(data) => Ok(pages.ip2016.overseasPensions(overseasPensionsForm.fill(data)))
            case _ => Ok(pages.ip2016.overseasPensions(overseasPensionsForm))
        }
    }

    val submitOverseasPensions: Action[AnyContent] = AuthorisedByAny.async { implicit user =>implicit request =>

        overseasPensionsForm.bindFromRequest.fold(
            errors => {
                val form = errors.copy(errors = errors.errors.map { er => FormError(er.key, Messages(er.message))})
                Future.successful(BadRequest(pages.ip2016.overseasPensions(form)))},
            success => {
                val validatedForm = OverseasPensionsForm.validateForm(overseasPensionsForm.fill(success))
                if(validatedForm.hasErrors) {
                    Future.successful(BadRequest(pages.ip2016.overseasPensions(validatedForm)))
                } else {
                    keyStoreConnector.saveFormData("overseasPensions", success).flatMap{
                        _=> Future.successful(Redirect(routes.IP2016Controller.currentPensions()))}
                }
            }
        )
    }


    //CURRENT PENSIONS
    val currentPensions = AuthorisedByAny.async { implicit user => implicit request =>

        keyStoreConnector.fetchAndGetFormData[CurrentPensionsModel]("currentPensions").map {
            case Some(data) => Ok(pages.ip2016.currentPensions(currentPensionsForm.fill(data)))
            case _ => Ok(pages.ip2016.currentPensions(currentPensionsForm))
        }
    }

    val submitCurrentPensions = AuthorisedByAny.async { implicit user => implicit request =>

        currentPensionsForm.bindFromRequest.fold(
            errors => {
                val form = errors.copy(errors = errors.errors.map { er => FormError(er.key, Messages(er.message))})
                Future.successful(BadRequest(pages.ip2016.currentPensions(form)))},
            success => {
                keyStoreConnector.saveFormData("currentPensions", success).flatMap{
                    _=> Future.successful(Redirect(routes.IP2016Controller.pensionDebits()))}
            }
        )
    }


    //PENSION DEBITS
    val pensionDebits = AuthorisedByAny.async { implicit user => implicit request =>
        keyStoreConnector.fetchAndGetFormData[PensionDebitsModel]("pensionDebits").map {
            case Some(data) => Ok(pages.ip2016.pensionDebits(pensionDebitsForm.fill(data)))
            case None => Ok(pages.ip2016.pensionDebits(pensionDebitsForm))
        }
    }

    val submitPensionDebits = AuthorisedByAny.async { implicit user => implicit request =>
        pensionDebitsForm.bindFromRequest.fold(
            errors => {
                val form = errors.copy(errors = errors.errors.map { er => FormError(er.key, Messages(er.message))})
                Future.successful(BadRequest(pages.ip2016.pensionDebits(form)))
            },
            success => {
                keyStoreConnector.saveFormData("pensionDebits", success).map{
                    _ => success.pensionDebits.get match {
                    case "yes" => Redirect (routes.IP2016Controller.psoDetails () )
                    case "no" => Redirect (routes.SummaryController.summaryIP16 () )

                    }
                }
            }
        )
}

    //PENSION SHARING ORDER DETAILS
    val psoDetails = AuthorisedByAny.async { implicit user => implicit request =>
        keyStoreConnector.fetchAndGetFormData[PSODetailsModel]("psoDetails").map {
            case Some(data) => Ok(pages.ip2016.psoDetails(psoDetailsForm.fill(data)))
            case _          => Ok(pages.ip2016.psoDetails(psoDetailsForm))
        }
    }

    val submitPSODetails = AuthorisedByAny.async { implicit user => implicit request =>

            psoDetailsForm.bindFromRequest.fold(
                errors => {
                    val form = errors.copy(errors = errors.errors.map { er => FormError(er.key, Messages(er.message))})
                    Future.successful(BadRequest(pages.ip2016.psoDetails(PSODetailsForm.validateForm(form))))},
                form => {
                    val validatedForm =
                        PSODetailsForm.validateForm(psoDetailsForm.fill(form))
                    if (validatedForm.hasErrors) {
                        Future.successful(BadRequest(pages.ip2016.psoDetails(validatedForm)))
                    } else {
                        keyStoreConnector.saveFormData(s"psoDetails", form).flatMap{
                            _=> Future.successful(Redirect(routes.SummaryController.summaryIP16()))}
                    }
                }
            )
    }

    val removePsoDetails = AuthorisedByAny.async {implicit user => implicit request =>
        Future(Ok(pages.ip2016.removePsoDetails()))
    }

    val submitRemovePsoDetails = AuthorisedByAny.async { implicit user => implicit request =>
      val updatedModel = PensionDebitsModel(Some("no"))
      keyStoreConnector.saveData[PensionDebitsModel]("pensionDebits", updatedModel)
      Future(Redirect(routes.SummaryController.summaryIP16()))
    }

}
