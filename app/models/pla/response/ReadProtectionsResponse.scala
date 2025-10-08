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

package models.pla.response

import play.api.libs.json.{Format, Json}

case class ReadProtectionsResponse(
    pensionSchemeAdministratorCheckReference: String,
    protectionRecordsList: Option[Seq[ProtectionRecordsList]]
)

object ReadProtectionsResponse {
  implicit val format: Format[ReadProtectionsResponse] = Json.format[ReadProtectionsResponse]
}

case class ProtectionRecordsList(
    protectionRecord: ProtectionRecord,
    historicaldetailsList: Option[Seq[ProtectionRecord]]
)

object ProtectionRecordsList {
  implicit val format: Format[ProtectionRecordsList] = Json.format[ProtectionRecordsList]
}

case class ProtectionRecord(
    identifier: Long,
    sequenceNumber: Int,
    `type`: ProtectionType,
    certificateDate: String,
    certificateTime: String,
    status: ProtectionStatus,
    protectionReference: Option[String] = None,
    relevantAmount: Option[Int] = None,
    preADayPensionInPaymentAmount: Option[Int] = None,
    postADayBenefitCrystallisationEventAmount: Option[Int] = None,
    uncrystallisedRightsAmount: Option[Int] = None,
    nonUKRightsAmount: Option[Int] = None,
    pensionDebitAmount: Option[Int] = None,
    pensionDebitEnteredAmount: Option[Int] = None,
    protectedAmount: Option[Int] = None,
    pensionDebitStartDate: Option[String] = None,
    pensionDebitTotalAmount: Option[Int] = None,
    lumpSumAmount: Option[Int] = None,
    lumpSumPercentage: Option[Int] = None,
    enhancementFactor: Option[Double] = None
)

object ProtectionRecord {
  implicit val format: Format[ProtectionRecord] = Json.format[ProtectionRecord]
}
