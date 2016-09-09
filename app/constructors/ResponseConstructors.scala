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

import connectors.KeyStoreConnector
import play.api.i18n.Messages
import play.api.libs.json.{JsSuccess, JsValue, Json}
import models._
import enums.ApplicationType
import utils.Constants
import common.{Dates,Display}

object ResponseConstructors extends ResponseConstructors {
}

trait ResponseConstructors {

    def createSuccessResponseFromJson(json: JsValue)(implicit protectionType: ApplicationType.Value) : SuccessResponseModel = {
        val notificationId = (json \ "notificationId").as[Int].toString

        val details = if(Constants.successCodesRequiringProtectionInfo.contains(notificationId.toInt)) {
            Some(createResponseDetailsFromJson(json))
        } else None

        val protectedAmount = protectionType match {
            case ApplicationType.FP2016 => Constants.fpProtectedAmountString
            case _ => Display.currencyDisplayString(BigDecimal((json \ "protectedAmount").as[Double]))
        }
        val additionalInfo = getAdditionalInfo(notificationId)

        SuccessResponseModel(protectionType, notificationId, protectedAmount, details, additionalInfo)
    }

    private def createResponseDetailsFromJson(json: JsValue): ProtectionDetailsModel = {
        val protectionReference = (json \ "protectionReference").asOpt[String]
        val psaReference = (json \ "psaCheckReference").asOpt[String]
        val applicationDate = (json \ "certificateDate").asOpt[String].map{ dt => Display.dateDisplayString(Dates.constructDateFromAPIString(dt))}
        ProtectionDetailsModel(protectionReference, psaReference, applicationDate)
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


    // TODO: Get it working
    /*
    def createDisplayModelFromSuccessResponse(resp: SuccessResponseModel): ProtectionDisplayModel ={
        val protectionType =
    }



    case class ProtectionDisplayModel(
                                       protectionType: String,
                                       status: String,
                                       psaCheckReference: String,
                                       protectionReference: String,
                                       protectedAmount: Option[String],
                                       certificateDate: Option[String]
                                       )


    case class SuccessResponseModel(protectionType: ApplicationType.Value,
                                    notificationId: String,
                                    protectedAmount: String,
                                    details: Option[ProtectionDetailsModel],
                                    additionalInfo: Seq[String])

    case class ProtectionDetailsModel(protectionReference: Option[String],
                                      psaReference: Option[String],
                                      applicationDate: Option[String])
    */
}
