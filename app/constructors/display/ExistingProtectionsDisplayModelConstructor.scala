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

import common.{Dates, Display, Helpers, Strings}
import models.display.{
  ExistingInactiveProtectionsByType,
  ExistingInactiveProtectionsDisplayModel,
  ExistingProtectionDisplayModel,
  ExistingProtectionsDisplayModel
}
import models.pla.response.{ProtectionStatus, ProtectionType}
import models.{ProtectionModel, TransformedReadResponseModel}
import play.api.i18n.{Lang, Messages}

object ExistingProtectionsDisplayModelConstructor {

  def createExistingProtectionsDisplayModel(
      model: TransformedReadResponseModel
  )(implicit lang: Lang, messages: Messages): ExistingProtectionsDisplayModel = {
    val activeProtection = model.activeProtection.map(createExistingProtectionDisplayModel)

    val dormantProtections      = protectionsOfStatusByType(ProtectionStatus.Dormant, model.inactiveProtections)
    val withdrawnProtections    = protectionsOfStatusByType(ProtectionStatus.Withdrawn, model.inactiveProtections)
    val unsuccessfulProtections = protectionsOfStatusByType(ProtectionStatus.Unsuccessful, model.inactiveProtections)
    val rejectedProtections     = protectionsOfStatusByType(ProtectionStatus.Rejected, model.inactiveProtections)
    val expiredProtections      = protectionsOfStatusByType(ProtectionStatus.Expired, model.inactiveProtections)

    val inactiveProtections = ExistingInactiveProtectionsDisplayModel(
      dormantProtections = dormantProtections,
      withdrawnProtections = withdrawnProtections,
      unsuccessfulProtections = unsuccessfulProtections,
      rejectedProtections = rejectedProtections,
      expiredProtections = expiredProtections
    )

    ExistingProtectionsDisplayModel(
      activeProtection = activeProtection,
      inactiveProtections = inactiveProtections
    )

  }

  private def protectionsOfStatusByType(
      status: ProtectionStatus,
      protections: Seq[ProtectionModel]
  )(implicit lang: Lang, messages: Messages): ExistingInactiveProtectionsByType = {
    val grouped = protections
      .filter(_.status.contains(status.toString))
      .map(createExistingProtectionDisplayModel)
      .groupBy(_.protectionType)
      .toSeq

    ExistingInactiveProtectionsByType(grouped).sorted
  }

  private def createExistingProtectionDisplayModel(
      model: ProtectionModel
  )(implicit lang: Lang, messages: Messages): ExistingProtectionDisplayModel = {

    val status              = Strings.statusString(model.status)
    val protectionType      = Strings.protectionTypeString(model.protectionType)
    val protectionReference = model.protectionReference.getOrElse(Messages("pla.protection.protectionReference"))

    val protectedAmount =
      model.protectedAmount.map(protectedAmount => Display.currencyDisplayString(BigDecimal(protectedAmount)))

    val (certificateDate, certificateTime) = createDateAndTimeDisplayStrings(model.certificateDate)

    val strippedPsaRef = model.psaCheckReference.map {
      _.stripPrefix(""""""").stripSuffix(""""""")
    }

    val withdrawnDate =
      model.withdrawnDate.map(wDate => Display.dateDisplayString(Dates.constructDateTimeFromAPIString(wDate)))
    val amendCall = Helpers.createAmendCallIfRequired(model)

    val factor = model.hipFields.enhancementFactor
      .filter(_ => shouldDisplayFactor(protectionType))
      .map(factor => Display.factorDisplayString(factor))

    val enhancementFactor = model.hipFields.enhancementFactor
      .filter(_ => shouldDisplayEnhancementFactor(protectionType))
      .map(factor => Display.factorDisplayString(factor))

    val lumpSumAmount = model.hipFields.lumpSumAmount
      .filter(_ => shouldDisplayLumpSumAmount(protectionType))
      .map(lumpSumAmount => Display.currencyDisplayString(BigDecimal(lumpSumAmount)))

    val lumpSumPercentage = model.hipFields.lumpSumPercentage
      .filter(_ => shouldDisplayLumpSumPercentage(protectionType))
      .map(lumpSumPercentage => Display.percentageDisplayString(lumpSumPercentage))

    ExistingProtectionDisplayModel(
      protectionType = protectionType,
      status = status,
      amendCall = amendCall,
      psaCheckReference = strippedPsaRef,
      protectionReference = protectionReference,
      protectedAmount = protectedAmount,
      certificateDate = certificateDate,
      certificateTime = certificateTime,
      withdrawnDate = withdrawnDate,
      lumpSumAmount = lumpSumAmount,
      lumpSumPercentage = lumpSumPercentage,
      enhancementFactor = enhancementFactor,
      factor = factor
    )
  }

  def shouldDisplayLumpSumPercentage(protectionType: String): Boolean =
    ProtectionType.tryFrom(protectionType) match {
      case Some(ProtectionType.EnhancedProtection)    => true
      case Some(ProtectionType.EnhancedProtectionLTA) => true
      case _                                          => false
    }

  def shouldDisplayLumpSumAmount(protectionType: String): Boolean =
    ProtectionType.tryFrom(protectionType) match {
      case Some(ProtectionType.PrimaryProtection)    => true
      case Some(ProtectionType.PrimaryProtectionLTA) => true
      case _                                         => false
    }

  def shouldDisplayEnhancementFactor(protectionType: String): Boolean =
    ProtectionType.tryFrom(protectionType) match {
      case Some(ProtectionType.PensionCreditRights)          => true
      case Some(ProtectionType.InternationalEnhancementS221) => true
      case Some(ProtectionType.InternationalEnhancementS224) => true
      case _                                                 => false
    }

  def shouldDisplayFactor(protectionType: String): Boolean =
    ProtectionType.tryFrom(protectionType) match {
      case Some(ProtectionType.PrimaryProtection)    => true
      case Some(ProtectionType.PrimaryProtectionLTA) => true
      case _                                         => false
    }

  def createDateAndTimeDisplayStrings(
      certificateDate: Option[String]
  )(implicit lang: Lang, messages: Messages): (Option[String], Option[String]) =
    certificateDate
      .map { dateString =>
        val dateTime = Dates.constructDateTimeFromAPIString(dateString)

        val certificateDate = Display.dateDisplayString(dateTime)

        val certificateTime = Display.timeDisplayString(dateTime)

        (Some(certificateDate), Some(certificateTime))
      }
      .getOrElse((None, None))

}
