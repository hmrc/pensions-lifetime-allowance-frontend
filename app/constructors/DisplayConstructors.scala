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

import common.{Dates, Display}
import models.{PrintDisplayModel, ProtectionModel, PersonalDetailsModel}

object DisplayConstructors {

  class RequiredValueNotDefinedException(val functionName: String, optionName: String) extends Exception(
    s"Value not found for $optionName in $functionName"
  )

  def createPrintDisplayModel(personalDetailsModelOpt: Option[PersonalDetailsModel], protectionModelOpt: Option[ProtectionModel], nino: String): PrintDisplayModel = {

    val personalDetailsModel = personalDetailsModelOpt.getOrElse{throw new RequiredValueNotDefinedException("createPrintDisplayModel", "personalDetailsModel")}
    val protectionModel = protectionModelOpt.getOrElse{throw new RequiredValueNotDefinedException("createPrintDisplayModel", "protectionModel")}

    val firstName = personalDetailsModel.person.firstName.toLowerCase.capitalize
    val surname = personalDetailsModel.person.lastName.toLowerCase.capitalize

    val protectionType = protectionTypeString(protectionModel.protectionType)
    val status = statusString(protectionModel.status)
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
}
