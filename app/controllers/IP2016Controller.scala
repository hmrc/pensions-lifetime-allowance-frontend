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
import forms.PensionsTakenForm.pensionsTakenForm
import forms.PensionsTakenBeforeForm.pensionsTakenBeforeForm
import forms.PensionsTakenBetweenForm.pensionsTakenBetweenForm
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
        if (request.session.get(SessionKeys.sessionId).isEmpty) {
            Future.successful(Redirect(routes.IntroductionController.introduction()))
        } else {
            keyStoreConnector.fetchAndGetFormData[PensionsTakenModel]("pensionsTaken").map {
                case Some(data) => Ok(pages.ip2016.pensionsTaken(pensionsTakenForm.fill(data)))
                case None => Ok(pages.ip2016.pensionsTaken(pensionsTakenForm))
            }
        }
    }

    val submitPensionsTaken = AuthorisedByAny { implicit user => implicit request =>
        pensionsTakenForm.bindFromRequest.fold(
            errors => BadRequest(pages.ip2016.pensionsTaken(errors)),
            success => {
                keyStoreConnector.saveFormData("pensionsTaken", success)
                success.pensionsTaken.get match {
                    case "yes"  => Redirect(routes.IP2016Controller.pensionsTakenBefore())
                    case "no"   => Redirect(routes.IntroductionController.introduction())
                }
            }
        )
    }


    //PENSIONS TAKEN BEFORE
    val pensionsTakenBefore = AuthorisedByAny.async { implicit user => implicit request =>

        def routeRequest(): Future[Result] = {
            keyStoreConnector.fetchAndGetFormData[PensionsTakenBeforeModel]("pensionsTakenBefore").map {
                case Some(data) => Ok(pages.ip2016.pensionsTakenBefore(pensionsTakenBeforeForm.fill(data)))
                case _ => Ok(pages.ip2016.pensionsTakenBefore(pensionsTakenBeforeForm))
            }
        }

        for {
            finalResult <- routeRequest
        } yield finalResult
    }

    val submitPensionsTakenBefore = AuthorisedByAny.async { implicit user => implicit request =>

        def routeRequest(): Future[Result] = {
            pensionsTakenBeforeForm.bindFromRequest.fold(
                errors => Future.successful(BadRequest(pages.ip2016.pensionsTakenBefore(errors))),
                success => {
                    val validatedForm = validatePensionsTakenBeforeForm(pensionsTakenBeforeForm.fill(success))
                    if(validatedForm.hasErrors) {
                        Future.successful(BadRequest(pages.ip2016.pensionsTakenBefore(validatedForm)))
                    } else {
                        keyStoreConnector.saveFormData("pensionsTakenBefore", success)
                        success.pensionsTakenBefore match {
                            case "Yes" =>
                                success.pensionsTakenBeforeAmt match {
                                    case Some(data) if data.equals(BigDecimal(0)) => Future.successful(Redirect(routes.IP2016Controller.pensionsTakenBetween))
                                    case _ => Future.successful(Redirect(routes.IP2016Controller.pensionsTakenBetween))
                                }
                            case "No" => Future.successful(Redirect(routes.IP2016Controller.pensionsTakenBetween))
                        }
                    }
                }
            )         
        }
        for {
            finalResult <- routeRequest
        } yield finalResult
    }

    private def validatePensionsTakenBeforeForm(form: Form[PensionsTakenBeforeModel]) = {
        if(!validate(form)) form.withError("pensionsTakenBeforeAmt", Messages("pla.pensionsTakenBefore.errorQuestion"))
        else if(!validateMinimum(form)) form.withError("pensionsTakenBeforeAmt", Messages("pla.pensionsTakenBefore.errorNegative"))
        else if(!validateTwoDec(form)) form.withError("pensionsTakenBeforeAmt", Messages("pla.pensionsTakenBefore.errorDecimalPlaces"))
        else form
    }



    def validate(data: Form[PensionsTakenBeforeModel]) = {
        data("pensionsTakenBefore").value.get match {
            case "Yes" => data("pensionsTakenBeforeAmt").value.isDefined
            case "No" => true
        }
    }

    def validateMinimum(data: Form[PensionsTakenBeforeModel]) = {
        data("pensionsTakenBefore").value.get match {
            case "Yes" => isPositive(data("pensionsTakenBeforeAmt").value.getOrElse("1").toDouble)
            case "No" => true
        }
    }

    def validateTwoDec(data: Form[PensionsTakenBeforeModel]) = {
        data("pensionsTakenBefore").value.get match {
            case "Yes" => isMaxTwoDecimalPlaces(data("pensionsTakenBeforeAmt").value.getOrElse("0").toDouble)
            case "No" => true
        }
    }

    //PENSIONS TAKEN BETWEEN
    val pensionsTakenBetween = AuthorisedByAny.async { implicit user => implicit request =>

        def routeRequest(): Future[Result] = {
            keyStoreConnector.fetchAndGetFormData[PensionsTakenBetweenModel]("pensionsTakenBetween").map {
                case Some(data) => Ok(pages.ip2016.pensionsTakenBetween(pensionsTakenBetweenForm.fill(data)))
                case _ => Ok(pages.ip2016.pensionsTakenBetween(pensionsTakenBetweenForm))
            }
        }

        for {
            finalResult <- routeRequest
        } yield finalResult
    }

    val submitPensionsTakenBetween = AuthorisedByAny.async { implicit user => implicit request =>

        def routeRequest(): Future[Result] = {
            pensionsTakenBetweenForm.bindFromRequest.fold(
                errors => Future.successful(BadRequest(pages.ip2016.pensionsTakenBetween(errors))),
                success => {
                    keyStoreConnector.saveFormData("pensionsTakenBetween", success)
                    success.pensionsTakenBetween match {
                        case "Yes" =>
                            success.pensionsTakenBetweenAmt match {
                                case Some(data) if data.equals(BigDecimal(0)) => Future.successful(Redirect(routes.IntroductionController.introduction()))
                                case _ => Future.successful(Redirect(routes.IntroductionController.introduction()))
                            }
                        case "No" => Future.successful(Redirect(routes.IntroductionController.introduction()))
                    }
                }
            )         
        }
        for {
            finalResult <- routeRequest
        } yield finalResult
    }
}