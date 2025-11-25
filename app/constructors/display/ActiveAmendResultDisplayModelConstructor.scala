/*
 * Copyright 2025 HM Revenue & Customs
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

package constructors.display

import common.{Dates, Display, Exceptions}
import enums.ApplicationType
import models.display.{ActiveAmendResultDisplayModel, ProtectionDetailsDisplayModel}
import models.{AmendResponseModel, PersonalDetailsModel, ProtectionModel}
import play.api.i18n.{Lang, Messages}

object ActiveAmendResultDisplayModelConstructor {

  def createActiveAmendResponseDisplayModel(
      model: AmendResponseModel,
      personalDetailsModelOpt: Option[PersonalDetailsModel],
      nino: String
  )(implicit lang: Lang, messages: Messages): ActiveAmendResultDisplayModel = {
    val protectionDetails = createProtectionDetailsFromProtection(model.protection)
    val protectionType    = getProtectionTypeFromProtection(model.protection)

    val personalDetailsModel = personalDetailsModelOpt.getOrElse {
      throw Exceptions.RequiredValueNotDefinedException("createPrintDisplayModel", "personalDetailsModel")
    }

    val firstName = personalDetailsModel.person.firstName.toLowerCase.capitalize
    val surname   = personalDetailsModel.person.lastName.toLowerCase.capitalize

    val protectedAmount = model.protection.protectedAmount.getOrElse {
      throw Exceptions.OptionNotDefinedException(
        "createActiveAmendResponseDisplayModel",
        "protectedAmount",
        model.protection.protectionType.getOrElse("No protection type in response")
      )
    }
    val protectedAmountString = Display.currencyDisplayString(BigDecimal(protectedAmount))

    val notificationId = model.protection.notificationId.getOrElse(
      throw Exceptions.OptionNotDefinedException(
        "createActiveAmendResponseDisplayModel",
        "notificationId",
        model.protection.protectionType.getOrElse("No protection type in response")
      )
    )

    ActiveAmendResultDisplayModel(
      firstName,
      surname,
      nino,
      protectionType,
      notificationId.toString,
      protectedAmountString,
      Some(protectionDetails)
    )
  }

  private def createProtectionDetailsFromProtection(
      protection: ProtectionModel
  )(implicit lang: Lang, messages: Messages): ProtectionDetailsDisplayModel = {
    val protectionReference =
      protection.protectionReference.getOrElse(Messages("pla.protection.protectionReference"))
    val psaReference = protection.psaCheckReference.getOrElse(
      throw Exceptions.OptionNotDefinedException(
        "createProtectionDetailsFromModel",
        "psaCheckReference",
        protection.protectionType.getOrElse("No protection type in response")
      )
    )
    val applicationDate =
      protection.certificateDate.map(dt => Display.dateDisplayString(Dates.constructDateTimeFromAPIString(dt)))

    ProtectionDetailsDisplayModel(protectionReference, psaReference, applicationDate)
  }

  private def getProtectionTypeFromProtection(protection: ProtectionModel): ApplicationType.Value = {
    val protectionTypeString = protection.protectionType.getOrElse(
      throw Exceptions.RequiredValueNotDefinedException("getProtectionTypeFromProtection", "protectionType")
    )
    ApplicationType.fromString(protectionTypeString).getOrElse {
      throw new Exception(s"Invalid protection type passed to getProtectionTypeFromProtection: $protectionTypeString")
    }
  }

}
