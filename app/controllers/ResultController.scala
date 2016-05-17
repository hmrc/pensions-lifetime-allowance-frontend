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
import config.WSHttp
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._
import play.api.libs.json.{JsValue, Json}
import models._
import views.html._
import connectors.APIConnector



object ResultController extends ResultController with ServicesConfig {
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val postSignInRedirectUrl = FrontendAppConfig.applyUrl

  override val apiConnector = APIConnector
}

trait ResultController extends FrontendController with AuthorisedForPLA {

    val successCodes = List(22,23,24)
    val rejectCodes = List(17,18,19,20,21)
    val apiConnector : APIConnector
    val refNo: Int = 24

    val processFPApplication = AuthorisedByAny.async {
        implicit user =>  implicit request => 
            apiConnector.applyFP16(user.nino.get).map {
                case Some(response: HttpResponse) => Ok(views.html.pages.resultSuccess(createSuccessResponseFromJson(response.json)))
                case None => BadRequest(Messages("pla.api.badRequest"))
            }
    }

    private def createSuccessResponseFromJson(json: JsValue):SuccessResponseModel = {
        val notificationId = (json \ "notificationId").as[Int].toString
        val protectionReference = (json \ "protectionReference").asOpt[String]
        val psaReference = (json \ "psaReference").asOpt[String]
        val additionalInfo = getAdditionalInfo(notificationId)
        SuccessResponseModel(notificationId, protectionReference, psaReference, additionalInfo)
    }

    def getAdditionalInfo(notificationId: String): List[String] = {

        def loop(notificationId: String, i: Int = 1, paragraphs: List[String] = List.empty): List[String] = {
            val x: String = "resultCode." + notificationId + "." + i
            if(Messages(x) == x){
                paragraphs
            } else {
                loop(notificationId, i+1, paragraphs :+ i.toString)
            }
        }

        loop(notificationId)
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