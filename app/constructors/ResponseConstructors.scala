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

    class OptionNotDefinedException(val functionName: String, optionName: String, applicationType: String) extends Exception(
        s"Option not found for $optionName in $functionName for application type $applicationType"
    )

    def createApplyResponseModelFromJson(json: JsValue)(implicit protectionType: ApplicationType.Value): Option[ApplyResponseModel] = {
        val protection = json.validate[ProtectionModel]
        val psaReference = (json \ "psaCheckReference").asOpt[String]

        psaReference.map {
            psaCheckReference => protection.fold(
                errors => None,
                success => Some(ApplyResponseModel(psaCheckReference, success))
                )
        }.getOrElse(None)
    }

    def createSuccessDisplayModel(model: ApplyResponseModel)(implicit protectionType: ApplicationType.Value): SuccessDisplayModel = {
        val notificationId = model.protection.notificationId.getOrElse(throw new OptionNotDefinedException("CreateSuccessDisplayModel", "notification ID", protectionType.toString))
        val protectedAmount = model.protection.protectedAmount.getOrElse(throw new OptionNotDefinedException("ApplyResponseModel", "protected amount", protectionType.toString))
        val printable = Constants.activeProtectionCodes.contains(notificationId)

        val details = if(Constants.successCodesRequiringProtectionInfo.contains(notificationId)) {
            Some(createProtectionDetailsFromModel(model))
        } else None

        val protectedAmountString = Display.currencyDisplayString(BigDecimal(protectedAmount))

        val additionalInfo = getAdditionalInfo(notificationId)

        SuccessDisplayModel(protectionType, notificationId.toString, protectedAmountString, printable, details, additionalInfo)
    }

    def createRejectionDisplayModel(model: ApplyResponseModel)(implicit protectionType: ApplicationType.Value): RejectionDisplayModel = {
        val notificationId = model.protection.notificationId.getOrElse(throw new OptionNotDefinedException("CreateRejectionDisplayModel", "notification ID", protectionType.toString))
        val additionalInfo = getAdditionalInfo(notificationId)
        RejectionDisplayModel(notificationId.toString, additionalInfo, protectionType)
    }

    private def createProtectionDetailsFromModel(model: ApplyResponseModel): ProtectionDetailsModel = {
        val protectionReference = model.protection.protectionReference
        val psaReference = model.psaCheckReference
        val applicationDate = model.protection.certificateDate.map{ dt => Display.dateDisplayString(Dates.constructDateFromAPIString(dt))}
        ProtectionDetailsModel(protectionReference, psaReference, applicationDate)
    }

//    def createSuccessResponseFromJson(json: JsValue)(implicit protectionType: ApplicationType.Value) : SuccessResponseModel = {
//        val notificationId = (json \ "notificationId").as[Int].toString
//        val printable = Constants.activeProtectionCodes.contains(notificationId.toInt)
//
//        val details = if(Constants.successCodesRequiringProtectionInfo.contains(notificationId.toInt)) {
//            Some(createResponseDetailsFromJson(json))
//        } else None
//
//        val protectedAmount = protectionType match {
//            case ApplicationType.FP2016 => Constants.fpProtectedAmountString
//            case _ => Display.currencyDisplayString(BigDecimal((json \ "protectedAmount").as[Double]))
//        }
//        val additionalInfo = getAdditionalInfo(notificationId)
//
//        SuccessResponseModel(protectionType, notificationId, protectedAmount, printable, details, additionalInfo)
//    }

//    private def createResponseDetailsFromJson(json: JsValue): ProtectionDetailsModel = {
//        val protectionReference = (json \ "protectionReference").asOpt[String]
//        val psaReference = (json \ "psaCheckReference").asOpt[String]
//        val applicationDate = (json \ "certificateDate").asOpt[String].map{ dt => Display.dateDisplayString(Dates.constructDateFromAPIString(dt))}
//        ProtectionDetailsModel(protectionReference, psaReference, applicationDate)
//    }

//    def createRejectionResponseFromJson(json: JsValue)(implicit protectionType: ApplicationType.Value): RejectionResponseModel = {
//        val notificationId = (json \ "notificationId").as[Int].toString
//        val additionalInfo = getAdditionalInfo(notificationId)
//        RejectionResponseModel(notificationId, additionalInfo, protectionType)
//    }

    def getAdditionalInfo(notificationId: Int): List[String] = {

        def loop(notificationId: Int, i: Int = 1, paragraphs: List[String] = List.empty): List[String] = {
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
