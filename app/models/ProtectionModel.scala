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

import models.pla.response.{ProtectionRecord, ProtectionType}
import models.pla.{AmendProtectionLifetimeAllowanceType, AmendProtectionRequestStatus}
import play.api.libs.json.{Json, OFormat}

case class ProtectionModel(
    psaCheckReference: Option[String],
    protectionID: Option[Long],
    certificateDate: Option[String] = None,
    version: Option[Int] = None,
    protectionType: Option[String] = None,
    status: Option[String] = None,
    protectedAmount: Option[Double] = None,
    relevantAmount: Option[Double] = None,
    postADayBenefitCrystallisationEvents: Option[Double] = None,
    preADayPensionInPayment: Option[Double] = None,
    uncrystallisedRights: Option[Double] = None,
    nonUKRights: Option[Double] = None,
    pensionDebitAmount: Option[Double] = None,
    pensionDebitEnteredAmount: Option[Double] = None,
    pensionDebitStartDate: Option[String] = None,
    pensionDebitTotalAmount: Option[Double] = None,
    pensionDebits: Option[List[PensionDebitModel]] = None,
    notificationId: Option[Int] = None,
    protectionReference: Option[String] = None,
    withdrawnDate: Option[String] = None,
    // Play's Json.Format breaks if this case class exceeds 22 fields, so additional fields go here
    hipFieldsOption: Option[ProtectionModelHipFields] = None
) {

  def isEmpty: Boolean = this == ProtectionModel(None, None)

  def isAmendable: Boolean = {
    def isStatusAmendable: Boolean =
      status.flatMap(AmendProtectionRequestStatus.tryFrom).isDefined
    def isProtectionTypeAmendable: Boolean =
      protectionType.flatMap(AmendProtectionLifetimeAllowanceType.tryFrom).isDefined

    isStatusAmendable && isProtectionTypeAmendable
  }

  def isFixedProtection2016: Boolean =
    protectionType.exists(protection =>
      protection.toLowerCase == ProtectionType.FixedProtection2016.toString.toLowerCase
        || protection.toLowerCase == ProtectionType.FixedProtection2016LTA.toString.toLowerCase
    )

  def hipFields: ProtectionModelHipFields = ProtectionModelHipFields.fromOption(hipFieldsOption)

}

object ProtectionModel {

  def apply(psaCheckReference: String, record: ProtectionRecord): ProtectionModel =
    ProtectionModel(
      Some(psaCheckReference),
      Some(record.identifier),
      Some(buildRecordDateTime(record)),
      Some(record.sequenceNumber),
      Some(record.`type`.toString),
      Some(record.status.toString),
      record.protectedAmount.map(_.toDouble),
      record.relevantAmount.map(_.toDouble),
      record.postADayBenefitCrystallisationEventAmount.map(_.toDouble),
      record.preADayPensionInPaymentAmount.map(_.toDouble),
      record.uncrystallisedRightsAmount.map(_.toDouble),
      record.nonUKRightsAmount.map(_.toDouble),
      record.pensionDebitAmount.map(_.toDouble),
      record.pensionDebitEnteredAmount.map(_.toDouble),
      record.pensionDebitStartDate,
      record.pensionDebitTotalAmount.map(_.toDouble),
      None,
      notificationId = None,
      record.protectionReference,
      None,
      Some(
        ProtectionModelHipFields(
          record.lumpSumPercentage,
          record.lumpSumAmount,
          record.enhancementFactor
        )
      )
    )

  private def buildRecordDateTime(record: ProtectionRecord): String =
    s"${record.certificateDate}T${padCertificateTime(record.certificateTime)}"

  private[models] def padCertificateTime(certificateTimeString: String): String = {
    val padding = "0" * (6 - certificateTimeString.length).max(0)

    s"$padding$certificateTimeString"
  }

  implicit val pensionDebitFormat: OFormat[PensionDebitModel]     = PensionDebitModel.pdFormat
  implicit val hipFieldsFormat: OFormat[ProtectionModelHipFields] = ProtectionModelHipFields.format
  implicit val format: OFormat[ProtectionModel]                   = Json.format[ProtectionModel]
}

case class PensionDebitModel(startDate: String, amount: Double)

object PensionDebitModel {
  implicit val pdFormat: OFormat[PensionDebitModel] = Json.format[PensionDebitModel]
}

case class ProtectionModelHipFields(
    lumpSumPercentage: Option[Int] = None,
    lumpSumAmount: Option[Int] = None,
    enhancementFactor: Option[Double] = None
) {
  def isEmpty: Boolean = this == ProtectionModelHipFields()
}

object ProtectionModelHipFields {
  def empty = ProtectionModelHipFields()

  def fromOption(option: Option[ProtectionModelHipFields]): ProtectionModelHipFields =
    option.getOrElse(ProtectionModelHipFields.empty)

  implicit val format: OFormat[ProtectionModelHipFields] = Json.format[ProtectionModelHipFields]
}
