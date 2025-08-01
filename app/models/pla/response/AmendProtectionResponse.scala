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

import models.pla.{AmendProtectionLifetimeAllowanceType, AmendProtectionResponseStatus}
import play.api.libs.json.{Format, Json}

case class AmendProtectionResponse(
    lifetimeAllowanceIdentifier: Int,
    lifetimeAllowanceSequenceNumber: Int,
    lifetimeAllowanceType: AmendProtectionLifetimeAllowanceType,
    certificateDate: Option[String],
    certificateTime: Option[String],
    status: AmendProtectionResponseStatus,
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

object AmendProtectionResponse {
  implicit val format: Format[AmendProtectionResponse] = Json.format[AmendProtectionResponse]
}
