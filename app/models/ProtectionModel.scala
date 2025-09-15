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

import models.pla.response.ProtectionRecord
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
    withdrawnDate: Option[String] = None
) {

  def isEmpty: Boolean = this == ProtectionModel(None, None)

  def isAmendable: Boolean = {
    def isStatusAmendable: Boolean = {
      val amendableStatuses = AmendProtectionRequestStatus.allValues.map(_.toString).map(Some(_))

      amendableStatuses.contains(status.map(_.toUpperCase))
    }
    def isProtectionTypeAmendable: Boolean = {
      val amendableProtectionTypes = AmendProtectionLifetimeAllowanceType.allValues.map(_.toString).map(Some(_))

      amendableProtectionTypes.contains(protectionType.map(_.toUpperCase))
    }

    isStatusAmendable && isProtectionTypeAmendable
  }

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
      None,
      record.protectionReference,
      None
    )

  private def buildRecordDateTime(record: ProtectionRecord): String =
    s"${record.certificateDate}T${record.certificateTime}"

  implicit val pensionDebitFormat: OFormat[PensionDebitModel] = PensionDebitModel.pdFormat
  implicit val format: OFormat[ProtectionModel]               = Json.format[ProtectionModel]
}

case class PensionDebitModel(startDate: String, amount: Double)

object PensionDebitModel {
  implicit val pdFormat: OFormat[PensionDebitModel] = Json.format[PensionDebitModel]
}
