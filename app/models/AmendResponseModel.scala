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

import models.pla.response.{AmendProtectionResponse, AmendProtectionResponseStatus}
import play.api.libs.json.{Format, Json}

case class AmendResponseModel(
    identifier: Long,
    sequence: Int,
    status: AmendProtectionResponseStatus,
    protectionType: AmendedProtectionType,
    certificateDate: Option[DateModel],
    certificateTime: Option[TimeModel],
    protectionReference: Option[String],
    psaCheckReference: String,
    relevantAmount: Int,
    preADayPensionInPaymentAmount: Int,
    postADayBenefitCrystallisationEventAmount: Int,
    uncrystallisedRightsAmount: Int,
    nonUKRightsAmount: Int,
    notificationId: Option[NotificationId],
    protectedAmount: Option[Int],
    pensionDebitTotalAmount: Option[Int],
    pensionDebit: Option[PensionDebitModel]
) {

  def toProtectionModel = ProtectionModel(
    psaCheckReference = psaCheckReference,
    identifier = identifier,
    certificateDate = certificateDate,
    certificateTime = certificateTime,
    sequence = sequence,
    protectionType = protectionType.toProtectionType,
    status = status.toProtectionStatus,
    protectedAmount = protectedAmount.map(_.toDouble),
    relevantAmount = Some(relevantAmount),
    postADayBenefitCrystallisationEvents = Some(postADayBenefitCrystallisationEventAmount),
    preADayPensionInPayment = Some(preADayPensionInPaymentAmount),
    uncrystallisedRights = Some(uncrystallisedRightsAmount),
    nonUKRights = Some(nonUKRightsAmount),
    pensionDebitTotalAmount = pensionDebitTotalAmount.map(_.toDouble),
    protectionReference = protectionReference,
    lumpSumPercentage = None,
    lumpSumAmount = None,
    enhancementFactor = None
  )

  def combineWithFixedProtection2016(protectionModel: ProtectionModel): Option[AmendResponseModel] =
    AmendedProtectionType.tryFrom(protectionModel.protectionType) match {
      case Some(amendedProtectionType) if amendedProtectionType.isFixedProtection2016 =>
        Some(
          copy(
            protectionType = amendedProtectionType,
            protectionReference = protectionModel.protectionReference,
            certificateDate = protectionModel.certificateDate,
            certificateTime = protectionModel.certificateTime
          )
        )
      case _ => None
    }

}

object AmendResponseModel {

  implicit val format: Format[AmendResponseModel] = Json.format[AmendResponseModel]

  def from(amendResponse: AmendProtectionResponse, psaCheckReference: String): AmendResponseModel = {
    val pensionDebit = amendResponse.pensionDebitStartDate.zip(amendResponse.pensionDebitEnteredAmount).map {
      case (startDate, enteredAmount) => PensionDebitModel(startDate, enteredAmount)
    }

    AmendResponseModel(
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
      notificationId = amendResponse.notificationIdentifier,
      protectedAmount = amendResponse.protectedAmount,
      pensionDebitTotalAmount = amendResponse.pensionDebitTotalAmount,
      pensionDebit = pensionDebit,
      psaCheckReference = psaCheckReference
    )
  }

}
