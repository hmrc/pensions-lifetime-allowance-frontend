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

import models.ProtectionModel
import models.pla.{AmendProtectionLifetimeAllowanceType, AmendProtectionRequestStatus}
import play.api.libs.json.{Format, Json}

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
  implicit val format: Format[AmendProtectionRequest] = Json.format[AmendProtectionRequest]

  def from(protectionModel: ProtectionModel): AmendProtectionRequest = {

    def errorMessage(fieldName: String): String =
      s"Cannot create AmendProtectionRequest, because '$fieldName' field is empty in provided ProtectionModel"

    val lifetimeAllowanceSequenceNumber =
      protectionModel.version.getOrElse(throw new IllegalStateException(errorMessage("version")))
    val lifetimeAllowanceType = AmendProtectionLifetimeAllowanceType.from(
      protectionModel.protectionType.getOrElse(throw new IllegalStateException(errorMessage("protectionType")))
    )
    val status = AmendProtectionRequestStatus.from(
      protectionModel.status.getOrElse(throw new IllegalStateException(errorMessage("status")))
    )
    val relevantAmount =
      protectionModel.relevantAmount.getOrElse(throw new IllegalStateException(errorMessage("relevantAmount"))).toInt
    val preADayPensionInPayment =
      protectionModel.preADayPensionInPayment
        .getOrElse(throw new IllegalStateException(errorMessage("preADayPensionInPayment")))
        .toInt
    val postADayBenefitCrystallisationEvents =
      protectionModel.postADayBenefitCrystallisationEvents
        .getOrElse(throw new IllegalStateException(errorMessage("postADayBenefitCrystallisationEvents")))
        .toInt
    val uncrystallisedRights =
      protectionModel.uncrystallisedRights
        .getOrElse(throw new IllegalStateException(errorMessage("uncrystallisedRights")))
        .toInt
    val nonUKRights =
      protectionModel.nonUKRights.getOrElse(throw new IllegalStateException(errorMessage("nonUKRights"))).toInt

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
      pensionDebitEnteredAmount = protectionModel.pensionDebitEnteredAmount.map(_.toInt),
      notificationIdentifier = protectionModel.notificationId,
      protectedAmount = protectionModel.protectedAmount.map(_.toInt),
      pensionDebitStartDate = protectionModel.pensionDebitStartDate,
      pensionDebitTotalAmount = protectionModel.pensionDebitTotalAmount.map(_.toInt)
    )
  }

  private def extractCertificateDate(protectionModel: ProtectionModel): Option[String] =
    protectionModel.certificateDate.map(_.split("T")(0))

  private def extractCertificateTime(protectionModel: ProtectionModel): Option[String] =
    protectionModel.certificateDate.map(_.split("T")(1))

}
