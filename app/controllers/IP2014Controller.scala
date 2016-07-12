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
import enums.ApplicationType
import play.api.mvc._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import scala.concurrent.Future
import forms._
import forms.PensionsTakenForm.pensionsTakenForm
import forms.PensionsTakenBeforeForm.pensionsTakenBeforeForm
import forms.PensionsTakenBetweenForm.pensionsTakenBetweenForm
import forms.OverseasPensionsForm.overseasPensionsForm
import forms.CurrentPensionsForm.currentPensionsForm
import forms.PensionDebitsForm.pensionDebitsForm
import forms.NumberOfPSOsForm.numberOfPSOsForm
import forms.IP14PSODetailsForm.IP14PsoDetailsForm
import models._

import views.html._

object IP2014Controller extends IP2014Controller {
    val keyStoreConnector = KeyStoreConnector
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = FrontendAuthConnector
    override lazy val postSignInRedirectUrl = FrontendAppConfig.ip14StartUrl
}

trait IP2014Controller extends FrontendController with AuthorisedForPLA {

    val keyStoreConnector: KeyStoreConnector

    //IP14 PENSIONS TAKEN
    val ip14PensionsTaken = AuthorisedByAny.async { implicit user => implicit request =>
        keyStoreConnector.fetchAndGetFormData[PensionsTakenModel]("ip14PensionsTaken").map {
            case Some(data) => Ok(pages.ip2014.ip14PensionsTaken(pensionsTakenForm.fill(data)))
            case None => Ok(pages.ip2014.ip14PensionsTaken(pensionsTakenForm))
        }
    }

    val submitIP14PensionsTaken = AuthorisedByAny { implicit user => implicit request =>
        pensionsTakenForm.bindFromRequest.fold(
            errors => BadRequest(pages.ip2014.ip14PensionsTaken(errors)),
            success => {
                keyStoreConnector.saveFormData("ip14PensionsTaken", success)
                success.pensionsTaken.get match {
                    case "yes"  => Redirect(routes.IP2014Controller.ip14PensionsTakenBefore())
                    case "no"   => Redirect(routes.IP2014Controller.ip14OverseasPensions())
                }
            }
        )
    }


    //IP14 PENSIONS TAKEN BEFORE
    val ip14PensionsTakenBefore = AuthorisedByAny.async { implicit user => implicit request =>

        keyStoreConnector.fetchAndGetFormData[PensionsTakenBeforeModel]("ip14PensionsTakenBefore").map {
            case Some(data) => Ok(pages.ip2014.ip14PensionsTakenBefore(pensionsTakenBeforeForm.fill(data)))
            case _ => Ok(pages.ip2014.ip14PensionsTakenBefore(pensionsTakenBeforeForm))
        }
    }

    val submitIP14PensionsTakenBefore = AuthorisedByAny.async { implicit user => implicit request =>

        pensionsTakenBeforeForm.bindFromRequest.fold(
            errors => Future.successful(BadRequest(pages.ip2014.ip14PensionsTakenBefore(errors))),
            success => {
                val validatedForm = PensionsTakenBeforeForm.validateForm(pensionsTakenBeforeForm.fill(success))
                if(validatedForm.hasErrors) {
                    Future.successful(BadRequest(pages.ip2014.ip14PensionsTakenBefore(validatedForm)))
                } else {
                    keyStoreConnector.saveFormData("ip14PensionsTakenBefore", success)
                    Future.successful(Redirect(routes.IP2014Controller.ip14PensionsTakenBetween()))
                }
            }
        )
    }


    //IP14 PENSIONS TAKEN BETWEEN
    val ip14PensionsTakenBetween = AuthorisedByAny.async { implicit user => implicit request =>

        keyStoreConnector.fetchAndGetFormData[PensionsTakenBetweenModel]("ip14PensionsTakenBetween").map {
            case Some(data) => Ok(pages.ip2014.ip14PensionsTakenBetween(pensionsTakenBetweenForm.fill(data)))
            case _ => Ok(pages.ip2014.ip14PensionsTakenBetween(pensionsTakenBetweenForm))
        }
    }

    val submitIP14PensionsTakenBetween = AuthorisedByAny.async { implicit user => implicit request =>

        pensionsTakenBetweenForm.bindFromRequest.fold(
            errors => Future.successful(BadRequest(pages.ip2014.ip14PensionsTakenBetween(errors))),
            success => {
                val validatedForm = PensionsTakenBetweenForm.validateForm(pensionsTakenBetweenForm.fill(success))
                if(validatedForm.hasErrors) {
                    Future.successful(BadRequest(pages.ip2014.ip14PensionsTakenBetween(validatedForm)))
                } else {
                    keyStoreConnector.saveFormData("ip14PensionsTakenBetween", success)
                    Future.successful(Redirect(routes.IP2014Controller.ip14OverseasPensions()))
                }
            }
        )
    }


    //IP14 OVERSEAS PENSIONS
    val ip14OverseasPensions = AuthorisedByAny.async { implicit user => implicit request =>

        keyStoreConnector.fetchAndGetFormData[OverseasPensionsModel]("ip14OverseasPensions").map {
            case Some(data) => Ok(pages.ip2014.ip14OverseasPensions(overseasPensionsForm.fill(data)))
            case _ => Ok(pages.ip2014.ip14OverseasPensions(overseasPensionsForm))
        }
    }

    val submitIP14OverseasPensions = AuthorisedByAny.async { implicit user => implicit request =>

        overseasPensionsForm.bindFromRequest.fold(
            errors => Future.successful(BadRequest(pages.ip2014.ip14OverseasPensions(errors))),
            success => {
                val validatedForm = OverseasPensionsForm.validateForm(overseasPensionsForm.fill(success))
                if(validatedForm.hasErrors) {
                    Future.successful(BadRequest(pages.ip2014.ip14OverseasPensions(validatedForm)))
                } else {
                    keyStoreConnector.saveFormData("ip14OverseasPensions", success)
                    Future.successful(Redirect(routes.IP2014Controller.ip14CurrentPensions()))
                }
            }
        )
    }


    //IP14 CURRENT PENSIONS
    val ip14CurrentPensions = AuthorisedByAny.async { implicit user => implicit request =>

        keyStoreConnector.fetchAndGetFormData[CurrentPensionsModel]("ip14CurrentPensions").map {
            case Some(data) => Ok(pages.ip2014.ip14CurrentPensions(currentPensionsForm.fill(data)))
            case _ => Ok(pages.ip2014.ip14CurrentPensions(currentPensionsForm))
        }
    }

    val submitIP14CurrentPensions = AuthorisedByAny.async { implicit user => implicit request =>

        currentPensionsForm.bindFromRequest.fold(
            errors => Future.successful(BadRequest(pages.ip2014.ip14CurrentPensions(errors))),
            success => {
                keyStoreConnector.saveFormData("ip14CurrentPensions", success)
                Future.successful(Redirect(routes.IP2014Controller.ip14PensionDebits()))
            }
        )
    }
    

    //IP14 PENSION DEBITS
    val ip14PensionDebits = AuthorisedByAny.async { implicit user => implicit request =>
        keyStoreConnector.fetchAndGetFormData[PensionDebitsModel]("ip14PensionDebits").map {
            case Some(data) => Ok(pages.ip2014.ip14PensionDebits(pensionDebitsForm.fill(data)))
            case None => Ok(pages.ip2014.ip14PensionDebits(pensionDebitsForm))
        }
    }

    val submitIP14PensionDebits = AuthorisedByAny { implicit user => implicit request =>
        pensionDebitsForm.bindFromRequest.fold(
            errors => BadRequest(pages.ip2014.ip14PensionDebits(errors)),
            success => {
                keyStoreConnector.saveFormData("ip14PensionDebits", success)
                success.pensionDebits.get match {
                    case "yes"  => Redirect(routes.IP2014Controller.ip14NumberOfPSOs())
                    case "no"   => Redirect(routes.SummaryController.summaryIP14())
                }
            }
        )
    }


    //IP14 NUMBER OF PENSION SHARING ORDERS
    val ip14NumberOfPSOs = AuthorisedByAny.async { implicit user => implicit request =>

        keyStoreConnector.fetchAndGetFormData[PensionDebitsModel]("ip14PensionDebits").flatMap(pensionDebitsModel => {
            pensionDebitsModel.map {
                completedModel => routeIP14NumberOfPSOs(completedModel.pensionDebits.get, request)
            }.getOrElse(
                Future.successful(Redirect(routes.FallbackController.technicalError(ApplicationType.IP2014.toString)))
            )
        })
    }

    private def routeIP14NumberOfPSOs(havePSOs: String, req: Request[AnyContent]): Future[Result] = {
        implicit val request = req
        havePSOs match {
            case "no"  => Future.successful(Redirect(routes.FallbackController.technicalError(ApplicationType.IP2014.toString)))
            case "yes" => keyStoreConnector.fetchAndGetFormData[NumberOfPSOsModel]("ip14NumberOfPSOs").map {
                            case Some(data) => Ok(pages.ip2014.ip14NumberOfPSOs(numberOfPSOsForm.fill(data)))
                            case _ => Ok(pages.ip2014.ip14NumberOfPSOs(numberOfPSOsForm))
                          }
        }
    }

    val submitIP14NumberOfPSOs = AuthorisedByAny.async { implicit user => implicit request =>

        numberOfPSOsForm.bindFromRequest.fold(
            errors => Future.successful(BadRequest(pages.ip2014.ip14NumberOfPSOs(errors))),
            success => {
                keyStoreConnector.saveFormData("ip14NumberOfPSOs", success)
                Future.successful(Redirect(routes.IP2014Controller.ip14PsoDetails("1")))
            }
        )
    }


    //PENSION SHARING ORDER DETAILS
    def ip14PsoDetails(psoNumber:String): Action[AnyContent] = AuthorisedByAny.async { implicit user => implicit request =>

        val psoNum = psoNumber.toInt
        keyStoreConnector.fetchAndGetFormData[NumberOfPSOsModel]("ip14NumberOfPSOs").flatMap(numberOfPSOsModel => {
            numberOfPSOsModel.map {
                completedModel => routePSODetails(completedModel.numberOfPSOs.get.toInt, psoNum, request)
            }.getOrElse(
                Future.successful(Redirect(routes.FallbackController.technicalError(ApplicationType.IP2014.toString)))
            )

        })
    }

    private def routePSODetails(totalPSOs: Int, psoNum: Int, req: Request[AnyContent]): Future[Result] = {
        implicit val request = req
        if (psoNum > totalPSOs) {
            Future.successful(Redirect(routes.SummaryController.summaryIP14()))
        } else {
            keyStoreConnector.fetchAndGetFormData[PSODetailsModel](s"ip14PsoDetails$psoNum").map {
                case Some(storedData) => Ok(pages.ip2014.ip14PsoDetails(IP14PsoDetailsForm.fill(storedData), psoNum))
                case _ => Ok(pages.ip2014.ip14PsoDetails(IP14PsoDetailsForm, psoNum))
            }
        }
    }

    val submitIP14PSODetails = AuthorisedByAny.async { implicit user => implicit request =>

            IP14PsoDetailsForm.bindFromRequest.fold(
                errors => Future.successful(BadRequest(pages.ip2014.ip14PsoDetails(IP14PSODetailsForm.validateForm(errors), errors("psoNumber").value.get.toInt))),
                form => {
                    val validatedForm = IP14PSODetailsForm.validateForm(IP14PsoDetailsForm.fill(form))
                    if(validatedForm.hasErrors) {
                        Future.successful(BadRequest(pages.ip2014.ip14PsoDetails(validatedForm, form.psoNumber)))
                    } else {
                        keyStoreConnector.saveFormData(s"ip14PsoDetails${form.psoNumber}", form)
                        Future.successful(Redirect(routes.IP2014Controller.ip14PsoDetails(form.psoNumber.+(1).toString)))
                    }
                }
            )
    }

}
