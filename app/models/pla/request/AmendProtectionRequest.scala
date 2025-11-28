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

import models.pla.{AmendProtectionLifetimeAllowanceType, AmendProtectionRequestStatus}
import models.{PensionDebit, ProtectionModel}
import play.api.libs.json.{Json, Writes}

case class AmendProtectionRequest(
    lifetimeAllowanceSequenceNumber: Int,
    lifetimeAllowanceType: AmendProtectionLifetimeAllowanceType,
    certificateDate: Option[String],
    certificateTime: Option[String],
    status: AmendProtectionRequestStatus,
    protectionReference: Option[String],
    relevantAmount: Int,
    preADayPensionInPaymentAmount: Int,
    postADayBenefitCrystallisationEventAmount: Int,
    uncrystallisedRightsAmount: Int,
    nonUKRightsAmount: Int,
    pensionDebitAmount: Option[Int],
    pensionDebitEnteredAmount: Option[Int],
    notificationIdentifier: Option[Int],
    protectedAmount: Option[Int],
    pensionDebitStartDate: Option[String],
    pensionDebitTotalAmount: Option[Int]
)

object AmendProtectionRequest {
  implicit val writes: Writes[AmendProtectionRequest] = Json.writes[AmendProtectionRequest]

  def from(protectionModel: ProtectionModel): AmendProtectionRequest = {

    def errorMessage(fieldName: String): String =
      s"Cannot create AmendProtectionRequest, because '$fieldName' field is empty in provided ProtectionModel"

    val lifetimeAllowanceSequenceNumber =
      protectionModel.version.getOrElse(throw new IllegalArgumentException(errorMessage("version")))
    val lifetimeAllowanceType = AmendProtectionLifetimeAllowanceType.from(
      protectionModel.protectionType.getOrElse(throw new IllegalArgumentException(errorMessage("protectionType")))
    )
    val status = AmendProtectionRequestStatus.from(
      protectionModel.status.getOrElse(throw new IllegalArgumentException(errorMessage("status")))
    )
    val relevantAmount =
      protectionModel.relevantAmount.getOrElse(throw new IllegalArgumentException(errorMessage("relevantAmount"))).toInt
    val preADayPensionInPayment =
      protectionModel.preADayPensionInPayment
        .getOrElse(throw new IllegalArgumentException(errorMessage("preADayPensionInPayment")))
        .toInt
    val postADayBenefitCrystallisationEvents =
      protectionModel.postADayBenefitCrystallisationEvents
        .getOrElse(throw new IllegalArgumentException(errorMessage("postADayBenefitCrystallisationEvents")))
        .toInt
    val uncrystallisedRights =
      protectionModel.uncrystallisedRights
        .getOrElse(throw new IllegalArgumentException(errorMessage("uncrystallisedRights")))
        .toInt
    val nonUKRights =
      protectionModel.nonUKRights.getOrElse(throw new IllegalArgumentException(errorMessage("nonUKRights"))).toInt

    val pensionDebit = extractPensionDebit(protectionModel)

    val pensionDebitStartDate = pensionDebit.map(_.startDate)

    val pensionDebitEnteredAmount = pensionDebit.map(_.amount)

    AmendProtectionRequest(
      lifetimeAllowanceSequenceNumber = lifetimeAllowanceSequenceNumber,
      lifetimeAllowanceType = lifetimeAllowanceType,
      certificateDate = extractCertificateDate(protectionModel),
      certificateTime = extractCertificateTime(protectionModel),
      status = status,
      protectionReference = protectionModel.protectionReference,
      relevantAmount = relevantAmount,
      preADayPensionInPaymentAmount = preADayPensionInPayment,
      postADayBenefitCrystallisationEventAmount = postADayBenefitCrystallisationEvents,
      uncrystallisedRightsAmount = uncrystallisedRights,
      nonUKRightsAmount = nonUKRights,
      pensionDebitAmount = protectionModel.pensionDebitAmount.map(_.toInt),
      pensionDebitEnteredAmount = pensionDebitEnteredAmount.map(_.toInt),
      notificationIdentifier = protectionModel.notificationId,
      protectedAmount = protectionModel.protectedAmount.map(_.toInt),
      pensionDebitStartDate = pensionDebitStartDate,
      pensionDebitTotalAmount = protectionModel.pensionDebitTotalAmount.map(_.toInt)
    )
  }

  private def extractCertificateDate(protectionModel: ProtectionModel): Option[String] =
    protectionModel.certificateDate.map(_.split("T")(0))

  private def extractCertificateTime(protectionModel: ProtectionModel): Option[String] =
    protectionModel.certificateDate.map(_.split("T")(1)).map(_.split("\\.")(0)).map(_.replace(":", ""))

  private def extractPensionDebit(protectionModel: ProtectionModel): Option[PensionDebit] =
    (protectionModel.pensionDebitEnteredAmount, protectionModel.pensionDebitStartDate) match {
      case (Some(enteredAmount), Some(startDate)) => Some(PensionDebit(startDate = startDate, amount = enteredAmount))
      case _                                      => None
    }

}
