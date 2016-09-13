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

import models.{ExistingProtectionsModel, ProtectionModel, ExistingProtectionsDisplayModel, ProtectionDisplayModel}
import common.{Display, Dates}
import play.api.i18n.Messages

object ExistingProtectionsConstructor {
  def createExistingProtectionsDisplayModel(model: ExistingProtectionsModel): ExistingProtectionsDisplayModel = {
    val activeProtectionsList = model.activeProtections().map(createProtectionDisplayModel(_, model.psaCheckReference))
    val otherProtectionsList = model.otherProtections().map(createProtectionDisplayModel(_, model.psaCheckReference)).sortWith(sortByStatus)

    ExistingProtectionsDisplayModel(activeProtectionsList, otherProtectionsList)

  }

  def sortByStatus(s1: models.ProtectionDisplayModel, s2: models.ProtectionDisplayModel): Boolean = {
    if(s1.status == s2.status){
      val typeMap: Map[String, Int] = Map("IP2014" -> 1,"FP2016" -> 2, "IP2016" -> 3,"primary" -> 4,"enhanced" -> 5,"fixed" -> 6,"FP2014" -> 7)
      if(typeMap(s1.protectionType) < typeMap(s2.protectionType)) true else false
    }
    else {
      val statusMap: Map[String, Int] = Map("dormant" -> 1,"withdrawn" -> 2,"unsuccessful" -> 3,"rejected" -> 4,"expired" -> 5)
      if(statusMap(s1.status) < statusMap(s2.status)) true else false
    }
  }

  def createProtectionDisplayModel(model: ProtectionModel, psaCheckReference: String): ProtectionDisplayModel = {
    
    val status = statusString(model.status)

    val protectionType = protectionTypeString(model.protectionType)

    val protectionReference = model.protectionReference.getOrElse(Messages("pla.protection.protectionReference"))

    val protectedAmount = model.protectedAmount match {
      case Some(amount) => Some(Display.currencyDisplayString(BigDecimal(amount)))
      case _ => None
    }

    val certificateDate = model.certificateDate match {
      case Some(date) => Some(Display.dateDisplayString(Dates.constructDateFromAPIString(date)))
      case _ => None
    }

    val strippedPsaRef = psaCheckReference.stripPrefix(""""""").stripSuffix(""""""")

    ProtectionDisplayModel(
              protectionType,
              status,
              strippedPsaRef,
              protectionReference,
              protectedAmount,
              certificateDate)
  }

  def statusString(modelStatus: Option[String]): String = {
    modelStatus match {
      case Some("Open") => "open"
      case Some("Dormant") => "dormant"
      case Some("Withdrawn") => "withdrawn"
      case Some("Expired") => "expired"
      case Some("Unsuccessful") => "unsuccessful"
      case Some("Rejected") => "rejected"
      case _ => "notRecorded"
    }
  }

  def protectionTypeString(modelProtectionType: Option[String]) = {
    modelProtectionType match {
      case Some("FP2016") => "FP2016"
      case Some("IP2014") => "IP2014"
      case Some("IP2016") => "IP2016"
      case Some("Primary") => "primary"
      case Some("Enhanced") => "enhanced"
      case Some("Fixed") => "fixed"
      case Some("FP2014") => "FP2014"
      case _ => "notRecorded"
    }
  }

}
