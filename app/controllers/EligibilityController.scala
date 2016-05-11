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

import connectors.KeyStoreConnector
import play.api.mvc._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.SessionKeys
import scala.concurrent.Future
import forms.AddedToPensionForm.addedToPensionForm
import forms.AddingToPensionForm.addingToPensionForm
import forms.PensionSavingsForm.pensionSavingsForm
import models._

import views.html._

object EligibilityController extends EligibilityController {
     val keyStoreConnector = KeyStoreConnector
}

trait EligibilityController extends FrontendController {

    val keyStoreConnector: KeyStoreConnector

    // ADDING TO PENSION
    val addingToPension = Action.async { implicit request =>
        if (request.session.get(SessionKeys.sessionId).isEmpty) {
            Future.successful(Redirect(routes.IntroductionController.introduction()))
        } else {
            keyStoreConnector.fetchAndGetFormData[AddingToPensionModel]("willAddToPension").map {
                case Some(data) => Ok(pages.eligibility.addingToPension(addingToPensionForm.fill(data)))
                case None => Ok(pages.eligibility.addingToPension(addingToPensionForm))
            }
        }
    }

    val submitAddingToPension = Action { implicit request =>
        addingToPensionForm.bindFromRequest.fold(
            errors => BadRequest(pages.eligibility.addingToPension(errors)),
            success => {
                keyStoreConnector.saveFormData("willAddToPension", success)
                success.willAddToPension.get match {
                    case "yes" => Redirect(routes.EligibilityController.pensionSavings())
                    case "no"  => Redirect(routes.EligibilityController.applyFP())
                }
            }
        )
    }

    // ADDED TO PENSION
    val addedToPension = Action.async { implicit request =>
        if (request.session.get(SessionKeys.sessionId).isEmpty) {
            Future.successful(Redirect(routes.IntroductionController.introduction()))
        } else {
            keyStoreConnector.fetchAndGetFormData[AddedToPensionModel]("haveAddedToPension").map {
                case Some(data) => Ok(pages.eligibility.addedToPension(addedToPensionForm.fill(data)))
                case None => Ok(pages.eligibility.addedToPension(addedToPensionForm))
            }
        }
    }

    val submitAddedToPension = Action { implicit request =>
        addedToPensionForm.bindFromRequest.fold(
            errors => BadRequest(pages.eligibility.addedToPension(errors)),
            success => {
                success.haveAddedToPension.get match {
                    case "yes"  => Redirect(routes.EligibilityController.pensionSavings())
                    case "no"   => Redirect(routes.EligibilityController.addingToPension())
                }
            }
        )
    }

    // PENSION SAVINGS
    val pensionSavings = Action.async { implicit request =>
        if (request.session.get(SessionKeys.sessionId).isEmpty) {
            Future.successful(Redirect(routes.IntroductionController.introduction()))
        } else {
            keyStoreConnector.fetchAndGetFormData[PensionSavingsModel]("eligiblePensionSavings").map {
                case Some(data) => Ok(pages.eligibility.pensionSavings(pensionSavingsForm.fill(data)))
                case None => Ok(pages.eligibility.pensionSavings(pensionSavingsForm))
            }
        }
    }

    val submitPensionSavings = Action { implicit request =>
        pensionSavingsForm.bindFromRequest.fold(
            errors => BadRequest(pages.eligibility.pensionSavings(errors)),
            success => {
                keyStoreConnector.saveFormData("eligiblePensionSavings", success)
                success.eligiblePensionSavings.get match {
                    case "yes"  => Redirect(routes.EligibilityController.applyIP())
                    case "no"   => Redirect(routes.EligibilityController.cannotApply())
                }
            }
        )
    }

    // APPLY FP
    val applyFP = Action.async { implicit request =>
        if (request.session.get(SessionKeys.sessionId).isEmpty) {
            Future.successful(Redirect(routes.IntroductionController.introduction()))
        } else {
            Future.successful(Ok(views.html.pages.eligibility.applyFP()))
        }
    }

    // APPLY IP
    val applyIP = Action.async { implicit request =>
        if (request.session.get(SessionKeys.sessionId).isEmpty) {
            Future.successful(Redirect(routes.IntroductionController.introduction()))
        } else {
            Future.successful(Ok(views.html.pages.eligibility.applyIP()))
        }
    }

    // CANNOT APPLY
    val cannotApply = Action.async { implicit request =>
        if (request.session.get(SessionKeys.sessionId).isEmpty) {
            Future.successful(Redirect(routes.IntroductionController.introduction()))
        } else {
            Future.successful(Ok(views.html.pages.eligibility.cannotApply()))
        }
    }
}