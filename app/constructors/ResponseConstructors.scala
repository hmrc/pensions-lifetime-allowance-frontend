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

package constructors

import play.api.i18n.Messages
import play.api.libs.json.{JsSuccess, JsValue, Json}
import models._
import enums.ApplicationType
import utils.Constants
import common.Display

object ResponseConstructors extends ResponseConstructors {
    
}

trait ResponseConstructors {

    def createSuccessResponseFromJson(json: JsValue)(implicit protectionType: ApplicationType.Value) : SuccessResponseModel = {
        val notificationId = (json \ "notificationId").as[Int].toString
        val protectionReference = (json \ "protectionReference").asOpt[String]
        val psaReference = (json \ "psaCheckReference").asOpt[String]
        val applicationDate = (json \ "certificateDate").asOpt[String]
        val details = if(protectionReference.isEmpty && psaReference.isEmpty && applicationDate.isEmpty) None else {
            Some(ProtectionDetailsModel(protectionReference, psaReference, applicationDate))
        }
        val protectedAmount = protectionType match {
            case ApplicationType.FP2016 => Constants.fpProtectedAmountString
            case _ => Display.currencyDisplayString(BigDecimal((json \ "protectedAmount").as[Double]))
        }
        val additionalInfo = getAdditionalInfo(notificationId)
        SuccessResponseModel(protectionType, notificationId, protectedAmount, details, additionalInfo)
    }

    def createRejectionResponseFromJson(json: JsValue): RejectionResponseModel = {
        val notificationId = (json \ "notificationId").as[Int].toString
        val additionalInfo = getAdditionalInfo(notificationId)
        RejectionResponseModel(notificationId, additionalInfo)
    }

    def getAdditionalInfo(notificationId: String): List[String] = {

        def loop(notificationId: String, i: Int = 1, paragraphs: List[String] = List.empty): List[String] = {
            val x: String = s"resultCode.$notificationId.$i"
            if(Messages(x) == x){
                paragraphs
            } else {
                loop(notificationId, i+1, paragraphs :+ i.toString)
            }
        }

        loop(notificationId)
    }

    def createExistingProtectionsModelFromJson(json: JsValue): Option[ExistingProtectionsModel] = {
        json.validate[ExistingProtectionsModel].asOpt
    }
}
