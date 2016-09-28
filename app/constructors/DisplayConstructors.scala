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

import common.{Helpers, Dates, Display, Strings}
import enums.ApplicationType
import models._
import play.api.i18n.Messages
import utils.Constants

object DisplayConstructors extends DisplayConstructors

trait DisplayConstructors {

  class OptionNotDefinedException(val functionName: String, optionName: String, applicationType: String) extends Exception(
    s"Option not found for $optionName in $functionName for application type $applicationType"
  )

  class RequiredValueNotDefinedException(val functionName: String, optionName: String) extends Exception(
    s"Value not found for $optionName in $functionName"
  )

  // PRINT PAGE
  def createPrintDisplayModel(personalDetailsModelOpt: Option[PersonalDetailsModel], protectionModelOpt: Option[ProtectionModel], nino: String): PrintDisplayModel = {

    val personalDetailsModel = personalDetailsModelOpt.getOrElse{throw new RequiredValueNotDefinedException("createPrintDisplayModel", "personalDetailsModel")}
    val protectionModel = protectionModelOpt.getOrElse{throw new RequiredValueNotDefinedException("createPrintDisplayModel", "protectionModel")}

    val firstName = personalDetailsModel.person.firstName.toLowerCase.capitalize
    val surname = personalDetailsModel.person.lastName.toLowerCase.capitalize

    val protectionType = Strings.protectionTypeString(protectionModel.protectionType)
    val status = Strings.statusString(protectionModel.status)
    val psaCheckReference = protectionModel.psaCheckReference.getOrElse{throw new RequiredValueNotDefinedException("createPrintDisplayModel", "psaCheckReference")}
    val protectionReference = protectionModel.protectionReference.getOrElse{throw new RequiredValueNotDefinedException("createPrintDisplayModel", "protectionReference")}

    val protectedAmount = protectionModel.protectedAmount.map { amt =>
      Display.currencyDisplayString(BigDecimal(amt))
    }

    val certificateDate = protectionModel.certificateDate.map { cDate =>
      Display.dateDisplayString(Dates.constructDateFromAPIString(cDate))
    }

    PrintDisplayModel(
      firstName, surname, nino,
      protectionType,
      status,
      psaCheckReference,
      protectionReference,
      protectedAmount,
      certificateDate
    )
  }

  // EXISTING PROTECTIONS
  def createExistingProtectionsDisplayModel(model: TransformedReadResponseModel): ExistingProtectionsDisplayModel = {
    val activeProtection = model.activeProtection.map(createExistingProtectionDisplayModel)
    val otherProtectionsList = model.inactiveProtections.map(createExistingProtectionDisplayModel).sortWith(sortByStatus)

    ExistingProtectionsDisplayModel(activeProtection, otherProtectionsList)

  }

  def sortByStatus(s1: models.ExistingProtectionDisplayModel, s2: models.ExistingProtectionDisplayModel): Boolean = {
    if(s1.status == s2.status){
      val typeMap: Map[String, Int] = Map("IP2014" -> 1,"FP2016" -> 2, "IP2016" -> 3,"primary" -> 4,"enhanced" -> 5,"fixed" -> 6,"FP2014" -> 7)
      if(typeMap(s1.protectionType) < typeMap(s2.protectionType)) true else false
    }
    else {
      val statusMap: Map[String, Int] = Map("dormant" -> 1,"withdrawn" -> 2,"unsuccessful" -> 3,"rejected" -> 4,"expired" -> 5)
      if(statusMap(s1.status) < statusMap(s2.status)) true else false
    }
  }

  def createExistingProtectionDisplayModel(model: ProtectionModel): ExistingProtectionDisplayModel = {

    val status = Strings.statusString(model.status)
    val protectionType = Strings.protectionTypeString(model.protectionType)
    val protectionReference = model.protectionReference.getOrElse(Messages("pla.protection.protectionReference"))

    val protectedAmount = model.protectedAmount.map { amt =>
      Display.currencyDisplayString(BigDecimal(amt))
    }

    val certificateDate = model.certificateDate.map { cDate =>
      Display.dateDisplayString(Dates.constructDateFromAPIString(cDate))
    }

    val strippedPsaRef = model.psaCheckReference.map{_.stripPrefix(""""""").stripSuffix(""""""")}

    val amendCall = Helpers.createAmendCallIfRequired(model)

    ExistingProtectionDisplayModel(
      protectionType,
      status,
      amendCall,
      strippedPsaRef,
      protectionReference,
      protectedAmount,
      certificateDate)
  }

  // AMENDS

  def createAmendDisplayModel(model: AmendProtectionModel): AmendDisplayModel = {
    val amended = modelsDiffer(model.originalProtection, model.updatedProtection)
    val totalAmount = Display.currencyDisplayString(BigDecimal(model.updatedProtection.relevantAmount.getOrElse(0.0)))
    val rows = createAmendRowsFromProtection(model.updatedProtection)

    AmendDisplayModel (
      amended = amended,
      rows =  Seq.empty,
      totalAmount = totalAmount
    )
  }

  def createAmendRowsFromProtection(protection: ProtectionModel): Seq[AmendDisplayRowModel] = {

    Seq.empty
  }

  def currencyOrNo(moneyOption: Option[BigDecimal]): String = {

    moneyOption.map {
      amt => {
        if (amt == BigDecimal(0.0)) {
          "No"
        } else
        {
          Display.currencyDisplayString(amt)
        }
      }
    }.getOrElse("No")
  }

  def modelsDiffer(modelA: ProtectionModel, modelB: ProtectionModel): Boolean = {
    modelA match {
      case `modelB` => false
      case _ => true
    }
  }

  // SUCCESSFUL APPLICATION RESPONSE
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

  // REJECTED APPLICATION RESPONSE
  def createRejectionDisplayModel(model: ApplyResponseModel)(implicit protectionType: ApplicationType.Value): RejectionDisplayModel = {
    val notificationId = model.protection.notificationId.getOrElse(throw new OptionNotDefinedException("CreateRejectionDisplayModel", "notification ID", protectionType.toString))
    val additionalInfo = getAdditionalInfo(notificationId)
    RejectionDisplayModel(notificationId.toString, additionalInfo, protectionType)
  }

  private def createProtectionDetailsFromModel(model: ApplyResponseModel)(implicit protectionType: ApplicationType.Value): ProtectionDetailsDisplayModel = {
    val protectionReference = model.protection.protectionReference
    val psaReference = model.protection.psaCheckReference.getOrElse(throw new OptionNotDefinedException("createProtectionDetailsFromModel", "psaCheckReference", protectionType.toString))
    val applicationDate = model.protection.certificateDate.map{ dt => Display.dateDisplayString(Dates.constructDateFromAPIString(dt))}
    ProtectionDetailsDisplayModel(protectionReference, psaReference, applicationDate)
  }


  // HELPER FUNCTIONS
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
}
