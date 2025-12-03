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

import models.amend.AmendProtectionModel
import models.pla.AmendableProtectionType
import models.{DateModel, NotificationId, TimeModel}
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

  def from(protectionModel: AmendProtectionModel): AmendProtectionRequest = {
    val lifetimeAllowanceSequenceNumber = protectionModel.sequence

    val lifetimeAllowanceType = protectionModel.protectionType
    val status                = protectionModel.status

    val relevantAmount =
      protectionModel.updatedRelevantAmount

    val preADayPensionInPayment =
      protectionModel.updated.preADayPensionInPayment.getOrElse[Double](0)

    val postADayBenefitCrystallisationEvents =
      protectionModel.updated.postADayBenefitCrystallisationEvents.getOrElse[Double](0)

    val uncrystallisedRights =
      protectionModel.updated.uncrystallisedRights

    val nonUKRights =
      protectionModel.updated.nonUKRights.getOrElse[Double](0)

    val pensionDebitStartDate = protectionModel.updated.pensionDebit.map(_.startDate)

    val pensionDebitEnteredAmount = protectionModel.updated.pensionDebit.map(_.amount)

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
      notificationIdentifier = None,
      protectedAmount = protectionModel.protectedAmount.map(_.toInt),
      pensionDebitStartDate = pensionDebitStartDate,
      pensionDebitTotalAmount = protectionModel.pensionDebitTotalAmount.map(_.toInt)
    )
  }

}
