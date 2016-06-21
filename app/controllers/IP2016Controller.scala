/*
 * Copyright 2016 HM Revenue & Customs
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

import auth.AuthorisedForPLA
import config.{FrontendAppConfig,FrontendAuthConnector}

import connectors.KeyStoreConnector
import play.api.mvc._
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.Messages
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.SessionKeys
import scala.concurrent.Future
import forms._
import forms.PensionsTakenForm.pensionsTakenForm
import forms.PensionsTakenBeforeForm.pensionsTakenBeforeForm
import forms.PensionsTakenBetweenForm.pensionsTakenBetweenForm
import forms.OverseasPensionsForm.overseasPensionsForm
import forms.CurrentPensionsForm.currentPensionsForm
import forms.NumberOfPSOsForm.numberOfPSOsForm
import forms.PSODetailsForm.psoDetailsForm
import forms.PensionDebitsForm.pensionDebitsForm
import models._
import common.Validation._

import views.html._

object IP2016Controller extends IP2016Controller {
    val keyStoreConnector = KeyStoreConnector
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = FrontendAuthConnector
    override lazy val postSignInRedirectUrl = FrontendAppConfig.ipStartUrl
}

trait IP2016Controller extends FrontendController with AuthorisedForPLA {

    val keyStoreConnector: KeyStoreConnector

    //PENSIONS TAKEN
    val pensionsTaken = AuthorisedByAny.async { implicit user => implicit request =>
        keyStoreConnector.fetchAndGetFormData[PensionsTakenModel]("pensionsTaken").map {
            case Some(data) => Ok(pages.ip2016.pensionsTaken(pensionsTakenForm.fill(data)))
            case None => Ok(pages.ip2016.pensionsTaken(pensionsTakenForm))
        }
    }

    val submitPensionsTaken = AuthorisedByAny { implicit user => implicit request =>
        pensionsTakenForm.bindFromRequest.fold(
            errors => BadRequest(pages.ip2016.pensionsTaken(errors)),
            success => {
                keyStoreConnector.saveFormData("pensionsTaken", success)
                success.pensionsTaken.get match {
                    case "yes"  => Redirect(routes.IP2016Controller.pensionsTakenBefore())
                    case "no"   => Redirect(routes.IP2016Controller.overseasPensions())
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
            errors => Future.successful(BadRequest(pages.ip2016.pensionsTakenBefore(errors))),
            success => {
                val validatedForm = PensionsTakenBeforeForm.validateForm(pensionsTakenBeforeForm.fill(success))
                if(validatedForm.hasErrors) {
                    Future.successful(BadRequest(pages.ip2016.pensionsTakenBefore(validatedForm)))
                } else {
                    keyStoreConnector.saveFormData("pensionsTakenBefore", success)
                    Future.successful(Redirect(routes.IP2016Controller.pensionsTakenBetween))
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
            errors => Future.successful(BadRequest(pages.ip2016.pensionsTakenBetween(errors))),
            success => {
                val validatedForm = PensionsTakenBetweenForm.validateForm(pensionsTakenBetweenForm.fill(success))
                if(validatedForm.hasErrors) {
                    Future.successful(BadRequest(pages.ip2016.pensionsTakenBetween(validatedForm)))
                } else {
                    keyStoreConnector.saveFormData("pensionsTakenBetween", success)
                    Future.successful(Redirect(routes.IP2016Controller.overseasPensions()))
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

    val submitOverseasPensions = AuthorisedByAny.async { implicit user => implicit request =>

        overseasPensionsForm.bindFromRequest.fold(
            errors => Future.successful(BadRequest(pages.ip2016.overseasPensions(errors))),
            success => {
                val validatedForm = OverseasPensionsForm.validateForm(overseasPensionsForm.fill(success))
                if(validatedForm.hasErrors) {
                    Future.successful(BadRequest(pages.ip2016.overseasPensions(validatedForm)))
                } else {
                    keyStoreConnector.saveFormData("overseasPensions", success)
                    Future.successful(Redirect(routes.IP2016Controller.currentPensions()))
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
            errors => Future.successful(BadRequest(pages.ip2016.currentPensions(errors))),
            success => {
                keyStoreConnector.saveFormData("currentPensions", success)
                Future.successful(Redirect(routes.IP2016Controller.pensionDebits()))
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

    val submitPensionDebits = AuthorisedByAny { implicit user => implicit request =>
        pensionDebitsForm.bindFromRequest.fold(
            errors => BadRequest(pages.ip2016.pensionDebits(errors)),
            success => {
                keyStoreConnector.saveFormData("pensionDebits", success)
                success.pensionDebits.get match {
                    case "yes"  => Redirect(routes.IP2016Controller.numberOfPSOs())
                                // TODO: redirect to summary
                    case "no"   => Redirect(routes.IntroductionController.introduction())
                }
            }
        )
    }


    //NUMBER OF PENSION SHARING ORDERS
    val numberOfPSOs = AuthorisedByAny.async { implicit user => implicit request =>

        keyStoreConnector.fetchAndGetFormData[PensionDebitsModel]("pensionDebits").flatMap(pensionDebitsModel => {
            pensionDebitsModel.map {
                completedModel => routeNumberOfPSOs(completedModel.pensionDebits.get, request)
            }.getOrElse(
                // TODO: redirect to summary
                Future.successful(Redirect(routes.IntroductionController.introduction()))
            )
        })
    }

    private def routeNumberOfPSOs(havePSOs: String, req: Request[AnyContent]): Future[Result] = {
        implicit val request = req
        havePSOs match {
            case "no"  => Future.successful(Redirect(routes.IntroductionController.introduction())) // TODO: redirect to summary
            case "yes" => keyStoreConnector.fetchAndGetFormData[NumberOfPSOsModel]("numberOfPSOs").map {
                            case Some(data) => Ok(pages.ip2016.numberOfPSOs(numberOfPSOsForm.fill(data)))
                            case _ => Ok(pages.ip2016.numberOfPSOs(numberOfPSOsForm))
                          }
        }
    }

    val submitNumberOfPSOs = AuthorisedByAny.async { implicit user => implicit request =>

        numberOfPSOsForm.bindFromRequest.fold(
            errors => Future.successful(BadRequest(pages.ip2016.numberOfPSOs(errors))),
            success => {
                keyStoreConnector.saveFormData("numberOfPSOs", success)
                Future.successful(Redirect(routes.IP2016Controller.psoDetails("1")))
            }
        )
    }


    //PENSION SHARING ORDER DETAILS
    def psoDetails(psoNumber:String): Action[AnyContent] = AuthorisedByAny.async { implicit user => implicit request =>

        val psoNum = psoNumber.toInt
        keyStoreConnector.fetchAndGetFormData[NumberOfPSOsModel]("numberOfPSOs").flatMap(numberOfPSOsModel => {
            numberOfPSOsModel.map {
                completedModel => routePSODetails(completedModel.numberOfPSOs.get.toInt, psoNum, request)
            }.getOrElse(
                // TODO: redirect to Summary
                Future.successful(Redirect(routes.IntroductionController.introduction()))
            )

        })
    }

    private def routePSODetails(totalPSOs: Int, psoNum: Int, req: Request[AnyContent]): Future[Result] = {
        implicit val request = req
        if (psoNum > totalPSOs) {
            // TODO: redirect to Summary
            Future.successful(Redirect(routes.IntroductionController.introduction()))
        } else {
            keyStoreConnector.fetchAndGetFormData[PSODetailsModel](s"psoDetails$psoNum").map {
                case Some(storedData) => Ok(pages.ip2016.psoDetails(psoDetailsForm.fill(storedData), psoNum))
                case _ => Ok(pages.ip2016.psoDetails(psoDetailsForm, psoNum))
            }
        }
    }

    val submitPSODetails = AuthorisedByAny.async { implicit user => implicit request =>

            psoDetailsForm.bindFromRequest.fold(
                errors => Future.successful(BadRequest(pages.ip2016.psoDetails(errors, errors("psoNumber").value.get.toInt))),
                form => {
                    val validatedForm = PSODetailsForm.validateForm(psoDetailsForm.fill(form))
                    if(validatedForm.hasErrors) {
                        Future.successful(BadRequest(pages.ip2016.psoDetails(validatedForm, form.psoNumber)))
                    } else {
                        keyStoreConnector.saveFormData(s"psoDetails${form.psoNumber}", form)
                        Future.successful(Redirect(routes.IP2016Controller.psoDetails(form.psoNumber.+(1).toString)))
                    }
                }
            )
    }
}
