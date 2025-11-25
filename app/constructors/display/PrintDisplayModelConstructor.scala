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

import common.{Display, Exceptions, Strings}
import constructors.display.ExistingProtectionsDisplayModelConstructor.{
  createDateAndTimeDisplayStrings,
  shouldDisplayEnhancementFactor,
  shouldDisplayFactor,
  shouldDisplayLumpSumAmount,
  shouldDisplayLumpSumPercentage
}
import models.display.PrintDisplayModel
import models.pla.response.ProtectionType.FixedProtection2016
import models.{PersonalDetailsModel, ProtectionModel}
import play.api.i18n.{Lang, Messages}
import utils.Constants

object PrintDisplayModelConstructor {

  def createPrintDisplayModel(
      personalDetailsModelOpt: Option[PersonalDetailsModel],
      protectionModel: ProtectionModel,
      nino: String
  )(implicit lang: Lang, messages: Messages): PrintDisplayModel = {

    val personalDetailsModel = personalDetailsModelOpt.getOrElse {
      throw Exceptions.RequiredValueNotDefinedException("createPrintDisplayModel", "personalDetailsModel")
    }

    val firstName = personalDetailsModel.person.firstName.toLowerCase.capitalize
    val surname   = personalDetailsModel.person.lastName.toLowerCase.capitalize

    val protectionType = Strings.protectionTypeString(protectionModel.protectionType)
    val status         = Strings.statusString(protectionModel.status)
    val psaCheckReference = protectionModel.psaCheckReference.getOrElse {
      throw Exceptions.RequiredValueNotDefinedException("createPrintDisplayModel", "psaCheckReference")
    }
    val protectionReference =
      protectionModel.protectionReference.getOrElse(Messages("pla.protection.protectionReference"))

    val protectedAmountOption =
      protectionModel.protectedAmount.map(amt => Display.currencyDisplayString(BigDecimal(amt)))

    val protectedAmount = protectionModel.protectionType match {
      case Some("FP2016") | Some(FixedProtection2016.toString) =>
        Some(Display.currencyDisplayString(Constants.fpProtectedAmount))
      case _ => protectedAmountOption
    }

    val (certificateDate, certificateTime) = createDateAndTimeDisplayStrings(protectionModel.certificateDate)

    val lumpSumPercentage = protectionModel.hipFields.lumpSumPercentage
      .filter(_ => shouldDisplayLumpSumPercentage(protectionType))
      .map(lumpSumPercentage => Display.percentageDisplayString(lumpSumPercentage))

    val lumpSumAmount = protectionModel.hipFields.lumpSumAmount
      .filter(_ => shouldDisplayLumpSumAmount(protectionType))
      .map(lumpSumAmount => Display.currencyDisplayString(BigDecimal(lumpSumAmount)))

    val factor = protectionModel.hipFields.enhancementFactor
      .filter(_ => shouldDisplayFactor(protectionType))
      .map(factor => Display.factorDisplayString(factor))

    val enhancementFactor = protectionModel.hipFields.enhancementFactor
      .filter(_ => shouldDisplayEnhancementFactor(protectionType))
      .map(enhancementFactor => Display.factorDisplayString(enhancementFactor))

    PrintDisplayModel(
      firstName = firstName,
      surname = surname,
      nino = nino,
      protectionType = protectionType,
      status = status,
      psaCheckReference = psaCheckReference,
      protectionReference = protectionReference,
      protectedAmount = protectedAmount,
      certificateDate = certificateDate,
      certificateTime = certificateTime,
      lumpSumPercentage = lumpSumPercentage,
      lumpSumAmount = lumpSumAmount,
      enhancementFactor = enhancementFactor,
      factor = factor
    )
  }

}
