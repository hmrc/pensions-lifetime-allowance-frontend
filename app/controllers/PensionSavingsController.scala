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

import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import scala.concurrent.Future
import forms.PensionSavingsForm.pensionSavingsForm

import views.html._

object PensionSavingsController extends PensionSavingsController

trait PensionSavingsController extends FrontendController {

  	val pensionSavings = Action.async { implicit request =>
		Future.successful(Ok(pages.eligibility.pensionSavings(pensionSavingsForm)))
  	}

  	val submitPensionSavings = Action { implicit request =>
	    pensionSavingsForm.bindFromRequest.fold(
	        errors => BadRequest(pages.eligibility.pensionSavings(errors)),
	        success => {
	            success.eligiblePensionSavings.get match {
	                case "yes"  => Redirect(routes.ApplyIPController.applyIP)
	                case "no"   => Redirect(routes.CannotApplyController.cannotApply)
	            }
	        }
	    )
	}

}
