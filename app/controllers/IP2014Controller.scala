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
import forms.PensionDebitsForm.pensionDebitsForm
import models._
import common.Validation._

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
                    Future.successful(Redirect(routes.IntroductionController.introduction()))
                }
            }
        )
    }


    //PENSION DEBITS
    val pensionDebits = AuthorisedByAny.async { implicit user => implicit request =>
        keyStoreConnector.fetchAndGetFormData[PensionDebitsModel]("ip14PensionDebits").map {
            case Some(data) => Ok(pages.ip2014.ip14PensionDebits(pensionDebitsForm.fill(data)))
            case None => Ok(pages.ip2014.ip14PensionDebits(pensionDebitsForm))
        }
    }

    val submitPensionDebits = AuthorisedByAny { implicit user => implicit request =>
        pensionDebitsForm.bindFromRequest.fold(
            errors => BadRequest(pages.ip2014.ip14PensionDebits(errors)),
            success => {
                keyStoreConnector.saveFormData("ip14PensionDebits", success)
                success.pensionDebits.get match {
                                // TODO: redirect to number of PSOs
                    case "yes"  => Redirect(routes.IntroductionController.introduction())
                                // TODO: redirect to summary
                    case "no"   => Redirect(routes.IntroductionController.introduction())
                }
            }
        )
    }

}