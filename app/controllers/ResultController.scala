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

import play.api.i18n.Messages
import auth.AuthorisedForPLA
import config.{FrontendAppConfig,FrontendAuthConnector}
import connectors.KeyStoreConnector
import play.api.mvc._
import play.api.data.Forms._
import play.api.data._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.SessionKeys
import scala.concurrent.Future
import forms.AddedToPensionForm.addedToPensionForm
import forms.AddingToPensionForm.addingToPensionForm
import forms.PensionSavingsForm.pensionSavingsForm
import models._
import scalaj.http.Http
import views.html._

object ResultController extends ResultController {
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val postSignInRedirectUrl = FrontendAppConfig.applyUrl
}

trait ResultController extends FrontendController with AuthorisedForPLA {

    val refNo: Int = 24

    val processFPApplication = AuthorisedByAny.async {
        implicit user =>  implicit request =>
        //  val postUrl = "https://localhost:9012/individuals/"+user.nino.get+"/protections"
        //  val result = Http(postUrl).postData("""{"Protection":{"Type":"FP2016"}}""")
              // .header("Content-Type", "application/json")

              //result.asString.toString

        Future.successful(Ok(views.html.pages.resultSuccess(otherParagraphs(refNo), referenceNumbers(refNo))))
    }

    def otherParagraphs(number: Int, i: Int = 1, paragraphs: String = ""): String = {
        val x: String = "resultCode." + number.toString() + "." + i.toString()
        if(Messages(x) == x){
            paragraphs
        } else {
            otherParagraphs(number, i+1, paragraphs + "<p>" + Messages(x) + "</p>")
        }
    }

    def referenceNumbers(number: Int): String = {
        val x: String = "resultCode." + number.toString() + ".ref"
        val y: String = "resultCode." + number.toString() + ".psa"
        if(Messages(x) == x && Messages(y) == y){
            ""
        } else if(Messages(x) == x){
            "<p>" + Messages("pla.successFP16.paraOne") + "</p><p>" + Messages(y) + "</p>"
        } else if(Messages(y) == y){
            "<p>" + Messages("pla.successFP16.paraOne") + "</p><p>" + Messages(x) + "</p>"
        } else {
            "<p>" + Messages("pla.successFP16.paraOne") + "</p><p>" + Messages(x) + "</p><p>" + Messages(y) + "</p>"
        }
    }

}
