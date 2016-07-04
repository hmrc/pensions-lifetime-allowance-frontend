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
    val otherProtectionsList = model.otherProtections().map(createProtectionDisplayModel(_, model.psaCheckReference))

    ExistingProtectionsDisplayModel(activeProtectionsList, otherProtectionsList)

  }

  private def createProtectionDisplayModel(model: ProtectionModel, psaCheckReference: String): ProtectionDisplayModel = {
    
    val status = statusString(model.status)

    val protectionType = protectionTypeString(model.protectionType)

    val protectionReference = model.protectionReference.getOrElse(Messages("pla.protection.protectionReference"))

    val relevantAmount = model.relevantAmount match {
      case Some(amount) => Some(Display.currencyDisplayString(BigDecimal(amount)))
      case _ => None
    }

    val certificateDate = model.certificateDate match {
      case Some(date) => Some(Display.dateDisplayString(Dates.constructDateFromAPIString(date)))
      case _ => None
    }

    ProtectionDisplayModel(
              protectionType,
              status,
              psaCheckReference,
              protectionReference,
              relevantAmount,
              certificateDate)
  }

  def statusString(modelStatus: Option[String]): String = {
    modelStatus match {
      case Some("Open") => Messages("pla.protection.statuses.open")
      case Some("Dormant") => Messages("pla.protection.statuses.dormant")
      case Some("Withdrawn") => Messages("pla.protection.statuses.withdrawn")
      case Some("Expired") => Messages("pla.protection.statuses.expired")
      case Some("Unsuccessful") => Messages("pla.protection.statuses.unsuccessful")
      case Some("Rejected") => Messages("pla.protection.statuses.rejected")
      case _ => Messages("pla.protection.statuses.notRecorded")
    }
  }

  def protectionTypeString(modelProtectionType: Option[String]) = {
    modelProtectionType match {
      case Some("FP2016") => Messages("pla.protection.types.FP2016")
      case Some("IP2014") => Messages("pla.protection.types.IP2014")
      case Some("IP2016") => Messages("pla.protection.types.IP2016")
      case Some("Primary") => Messages("pla.protection.types.primary")
      case Some("Enhanced") => Messages("pla.protection.types.enhanced")
      case Some("Fixed") => Messages("pla.protection.types.fixed")
      case Some("FP2014") => Messages("pla.protection.types.FP2014")
      case _ => Messages("pla.protection.types.notRecorded")
    }
  }

}