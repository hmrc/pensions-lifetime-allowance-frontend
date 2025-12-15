/*
 * Copyright 2023 HM Revenue & Customs
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

import models.pla.AmendableProtectionType
import models.pla.response.{ProtectionRecord, ProtectionStatus, ProtectionType}
import models.pla.request.AmendProtectionRequestStatus
import play.api.libs.json.{Json, OFormat}

case class ProtectionModel(
    psaCheckReference: String,
    identifier: Long,
    sequenceNumber: Int,
    protectionType: ProtectionType,
    status: ProtectionStatus,
    certificateDate: Option[DateModel],
    certificateTime: Option[TimeModel],
    protectedAmount: Option[Double] = None,
    relevantAmount: Option[Double] = None,
    postADayBenefitCrystallisationEventAmount: Option[Double] = None,
    preADayPensionInPaymentAmount: Option[Double] = None,
    uncrystallisedRightsAmount: Option[Double] = None,
    nonUKRightsAmount: Option[Double] = None,
    pensionDebitTotalAmount: Option[Double] = None,
    protectionReference: Option[String] = None,
    lumpSumPercentage: Option[Int] = None,
    lumpSumAmount: Option[Int] = None,
    enhancementFactor: Option[Double] = None
) {

  private def amendableProtectionType = AmendableProtectionType.tryFromProtectionType(protectionType)

  private def amendableStatus = AmendProtectionRequestStatus.tryFromProtectionStatus(status)

  def isAmendable: Boolean = {
    def isStatusAmendable: Boolean =
      amendableStatus.isDefined

    def isProtectionTypeAmendable: Boolean =
      amendableProtectionType.isDefined

    isStatusAmendable && isProtectionTypeAmendable
  }

  def asAmendable: Option[(AmendableProtectionType, AmendProtectionRequestStatus)] =

    amendableProtectionType.zip(amendableStatus)

  def isFixedProtection2016: Boolean =
    protectionType.isFixedProtection2016

}

object ProtectionModel {

  def apply(psaCheckReference: String, record: ProtectionRecord): ProtectionModel =
    ProtectionModel(
      psaCheckReference = psaCheckReference,
      identifier = record.identifier,
      certificateDate = Some(record.certificateDate),
      certificateTime = Some(record.certificateTime),
      sequenceNumber = record.sequenceNumber,
      protectionType = record.`type`,
      status = record.status,
      protectedAmount = record.protectedAmount.map(_.toDouble),
      relevantAmount = record.relevantAmount.map(_.toDouble),
      postADayBenefitCrystallisationEventAmount = record.postADayBenefitCrystallisationEventAmount.map(_.toDouble),
      preADayPensionInPaymentAmount = record.preADayPensionInPaymentAmount.map(_.toDouble),
      uncrystallisedRightsAmount = record.uncrystallisedRightsAmount.map(_.toDouble),
      nonUKRightsAmount = record.nonUKRightsAmount.map(_.toDouble),
      pensionDebitTotalAmount = record.pensionDebitTotalAmount.map(_.toDouble),
      protectionReference = record.protectionReference,
      lumpSumPercentage = record.lumpSumPercentage,
      lumpSumAmount = record.lumpSumAmount,
      enhancementFactor = record.enhancementFactor
    )

  implicit val format: OFormat[ProtectionModel] = Json.format[ProtectionModel]
}
