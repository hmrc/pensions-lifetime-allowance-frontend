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

package helpers

import play.api.i18n.Messages
import play.api.libs.json.{JsValue, Json}
import models._

object ModelMakers extends ModelMakers {
    
}

trait ModelMakers {

    def createSuccessResponseFromJson(json: JsValue): SuccessResponseModel = {
        val notificationId = (json \ "notificationId").as[Int].toString
        val protectionReference = (json \ "protectionReference").asOpt[String]
        val psaReference = (json \ "psaReference").asOpt[String]
        val additionalInfo = getAdditionalInfo(notificationId)
        SuccessResponseModel(notificationId, protectionReference, psaReference, additionalInfo)
    }

    def createRejectionResponseFromJson(json: JsValue): RejectionResponseModel = {
        val notificationId = (json \ "notificationId").as[Int].toString
        val additionalInfo = getAdditionalInfo(notificationId)
        RejectionResponseModel(notificationId, additionalInfo)
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
}
