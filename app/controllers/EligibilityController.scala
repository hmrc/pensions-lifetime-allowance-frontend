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

import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import scala.concurrent.Future
import forms.AddingToPensionForm.addingToPensionForm

import views.html._

object EligibilityController extends EligibilityController

trait EligibilityController extends FrontendController {

    val addingToPension = Action.async { implicit request =>
        Future.successful(Ok(pages.eligibility.addingToPension(addingToPensionForm)))
    }

    val submitAddingToPension = Action { implicit request =>
        addingToPensionForm.bindFromRequest.fold(
            errors => BadRequest(pages.eligibility.addingToPension(errors)),
            success => {
                success.willAddToPension.get match {
                    case "yes" => Redirect(routes.PensionSavingsController.pensionSavings)
                    case "no"  => Redirect(routes.ApplyFPController.applyFP)
                }
            }
        )
    }
}