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

package models.pla.request

import models.pla.AmendableProtectionType
import models.{DateModel, NotificationId, ProtectionModel, TimeModel}
import play.api.libs.json.{Json, Writes}

case class AmendProtectionRequest(
    lifetimeAllowanceSequenceNumber: Int,
    lifetimeAllowanceType: AmendableProtectionType,
    certificateDate: Option[DateModel],
    certificateTime: Option[TimeModel],
    status: AmendProtectionRequestStatus,
    protectionReference: Option[String],
    relevantAmount: Int,
    preADayPensionInPaymentAmount: Int,
    postADayBenefitCrystallisationEventAmount: Int,
    uncrystallisedRightsAmount: Int,
    nonUKRightsAmount: Int,
    pensionDebitAmount: Option[Int],
    pensionDebitEnteredAmount: Option[Int],
    notificationIdentifier: Option[NotificationId],
    protectedAmount: Option[Int],
    pensionDebitStartDate: Option[DateModel],
    pensionDebitTotalAmount: Option[Int]
)

object AmendProtectionRequest {
  implicit val writes: Writes[AmendProtectionRequest] = Json.writes[AmendProtectionRequest]

  def from(protectionModel: ProtectionModel): AmendProtectionRequest = {

    def errorMessage(fieldName: String): String =
      s"Cannot create AmendProtectionRequest, because '$fieldName' field is empty in provided ProtectionModel"

    val lifetimeAllowanceSequenceNumber =
      protectionModel.sequence.getOrElse(throw new IllegalArgumentException(errorMessage("version")))

    val lifetimeAllowanceType = AmendableProtectionType.fromProtectionType(protectionModel.protectionType)
    val status                = AmendProtectionRequestStatus.fromProtectionStatus(protectionModel.status)

    val relevantAmount =
      protectionModel.relevantAmount.getOrElse(throw new IllegalArgumentException(errorMessage("relevantAmount")))
    val preADayPensionInPayment =
      protectionModel.preADayPensionInPayment
        .getOrElse(throw new IllegalArgumentException(errorMessage("preADayPensionInPayment")))
    val postADayBenefitCrystallisationEvents =
      protectionModel.postADayBenefitCrystallisationEvents
        .getOrElse(throw new IllegalArgumentException(errorMessage("postADayBenefitCrystallisationEvents")))
    val uncrystallisedRights =
      protectionModel.uncrystallisedRights
        .getOrElse(throw new IllegalArgumentException(errorMessage("uncrystallisedRights")))
    val nonUKRights =
      protectionModel.nonUKRights.getOrElse(throw new IllegalArgumentException(errorMessage("nonUKRights")))

    val pensionDebitStartDate = protectionModel.pensionDebit.map(_.startDate)

    val pensionDebitEnteredAmount = protectionModel.pensionDebit.map(_.amount)

    AmendProtectionRequest(
      lifetimeAllowanceSequenceNumber = lifetimeAllowanceSequenceNumber,
      lifetimeAllowanceType = lifetimeAllowanceType,
      certificateDate = protectionModel.certificateDate,
      certificateTime = protectionModel.certificateTime,
      status = status,
      protectionReference = protectionModel.protectionReference,
      relevantAmount = relevantAmount.toInt,
      preADayPensionInPaymentAmount = preADayPensionInPayment.toInt,
      postADayBenefitCrystallisationEventAmount = postADayBenefitCrystallisationEvents.toInt,
      uncrystallisedRightsAmount = uncrystallisedRights.toInt,
      nonUKRightsAmount = nonUKRights.toInt,
      pensionDebitAmount = None,
      pensionDebitEnteredAmount = pensionDebitEnteredAmount.map(_.toInt),
      notificationIdentifier = protectionModel.notificationId,
      protectedAmount = protectionModel.protectedAmount.map(_.toInt),
      pensionDebitStartDate = pensionDebitStartDate,
      pensionDebitTotalAmount = protectionModel.pensionDebitTotalAmount.map(_.toInt)
    )
  }

}
