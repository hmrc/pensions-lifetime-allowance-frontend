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

package models

import models.pla.AmendedProtectionType
import models.pla.response.{AmendProtectionResponse, AmendProtectionResponseStatus}
import play.api.libs.json.{Format, Json}

case class AmendedProtectionModel(
    identifier: Long,
    sequence: Int,
    protectionType: AmendedProtectionType,
    certificateDate: Option[DateModel],
    certificateTime: Option[TimeModel],
    status: AmendProtectionResponseStatus,
    protectionReference: Option[String],
    relevantAmount: Int,
    preADayPensionInPaymentAmount: Int,
    postADayBenefitCrystallisationEventAmount: Int,
    uncrystallisedRightsAmount: Int,
    nonUKRightsAmount: Int,
    notificationIdentifier: Option[NotificationId],
    protectedAmount: Option[Int],
    pensionDebitTotalAmount: Option[Int],
    pensionDebit: Option[PensionDebit],
    psaCheckReference: Option[String]
) {

  def toProtectionModel = ProtectionModel(
    psaCheckReference = psaCheckReference,
    identifier = Some(identifier),
    certificateDate = certificateDate,
    certificateTime = certificateTime,
    sequence = Some(sequence),
    protectionType = protectionType.toProtectionType,
    status = status.toProtectionStatus,
    protectedAmount = protectedAmount.map(_.toDouble),
    relevantAmount = Some(relevantAmount),
    postADayBenefitCrystallisationEvents = Some(postADayBenefitCrystallisationEventAmount),
    preADayPensionInPayment = Some(preADayPensionInPaymentAmount),
    uncrystallisedRights = Some(uncrystallisedRightsAmount),
    nonUKRights = Some(nonUKRightsAmount),
    pensionDebit = pensionDebit,
    pensionDebitTotalAmount = pensionDebitTotalAmount.map(_.toDouble),
    notificationId = notificationIdentifier,
    protectionReference = protectionReference,
    lumpSumPercentage = None,
    lumpSumAmount = None,
    enhancementFactor = None
  )

  def combineWithFixedProtection2016(protectionModel: ProtectionModel): Option[AmendedProtectionModel] =
    AmendedProtectionType.tryFrom(protectionModel.protectionType) match {
      case Some(protectionType) if protectionType.isFixedProtection2016 =>
        Some(
          copy(
            protectionType = protectionType,
            protectionReference = protectionModel.protectionReference,
            certificateDate = protectionModel.certificateDate,
            certificateTime = protectionModel.certificateTime
          )
        )
      case _ => None
    }

}

object AmendedProtectionModel {

  implicit val format: Format[AmendedProtectionModel] = Json.format[AmendedProtectionModel]

  def from(amendResponse: AmendProtectionResponse, psaCheckReference: Option[String]): AmendedProtectionModel = {
    val pensionDebit = amendResponse.pensionDebitStartDate.zip(amendResponse.pensionDebitEnteredAmount).map {
      case (startDate, enteredAmount) => PensionDebit(startDate, enteredAmount)
    }

    AmendedProtectionModel(
      identifier = amendResponse.lifetimeAllowanceIdentifier,
      sequence = amendResponse.lifetimeAllowanceSequenceNumber,
      protectionType = AmendedProtectionType.from(amendResponse.lifetimeAllowanceType),
      certificateDate = amendResponse.certificateDate,
      certificateTime = amendResponse.certificateTime,
      status = amendResponse.status,
      protectionReference = amendResponse.protectionReference,
      relevantAmount = amendResponse.relevantAmount,
      preADayPensionInPaymentAmount = amendResponse.preADayPensionInPaymentAmount,
      postADayBenefitCrystallisationEventAmount = amendResponse.postADayBenefitCrystallisationEventAmount,
      uncrystallisedRightsAmount = amendResponse.uncrystallisedRightsAmount,
      nonUKRightsAmount = amendResponse.nonUKRightsAmount,
      notificationIdentifier = amendResponse.notificationIdentifier,
      protectedAmount = amendResponse.protectedAmount,
      pensionDebitTotalAmount = amendResponse.pensionDebitTotalAmount,
      pensionDebit = pensionDebit,
      psaCheckReference = psaCheckReference
    )
  }

}
