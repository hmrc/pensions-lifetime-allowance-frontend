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

package testdata

import models.ProtectionModel
import models.pla.request.AmendProtectionRequest
import models.pla.{AmendProtectionLifetimeAllowanceType, AmendProtectionRequestStatus, AmendProtectionResponseStatus}
import models.pla.response.AmendProtectionResponse

object PlaV2TestData {

  val lifetimeAllowanceIdentifier: Int     = 42
  val lifetimeAllowanceSequenceNumber: Int = 13
  val protectionReference: String          = "IP141034571625B"

  val amendProtectionRequest: AmendProtectionRequest = AmendProtectionRequest(
    lifetimeAllowanceSequenceNumber = lifetimeAllowanceSequenceNumber,
    lifetimeAllowanceType = AmendProtectionLifetimeAllowanceType.IndividualProtection2014,
    certificateDate = Some("2025-07-15"),
    certificateTime = Some("174312"),
    status = AmendProtectionRequestStatus.Dormant,
    protectionReference = Some(protectionReference),
    relevantAmount = 105000,
    preADayPensionInPaymentAmount = 1500,
    postADayBenefitCrystallisationEventAmount = 2500,
    uncrystallisedRightsAmount = 75500,
    nonUKRightsAmount = 0,
    pensionDebitAmount = Some(25000),
    pensionDebitEnteredAmount = Some(25000),
    notificationIdentifier = Some(3),
    protectedAmount = Some(120000),
    pensionDebitStartDate = Some("2026-07-09"),
    pensionDebitTotalAmount = Some(40000)
  )
  val amendProtectionResponse: AmendProtectionResponse = AmendProtectionResponse(
    lifetimeAllowanceIdentifier = lifetimeAllowanceIdentifier,
    lifetimeAllowanceSequenceNumber = lifetimeAllowanceSequenceNumber + 1,
    lifetimeAllowanceType = AmendProtectionLifetimeAllowanceType.IndividualProtection2014,
    certificateDate = Some("2025-07-15"),
    certificateTime = Some("174312"),
    status = AmendProtectionResponseStatus.Dormant,
    protectionReference = Some(protectionReference),
    relevantAmount = 105000,
    preADayPensionInPaymentAmount = 1500,
    postADayBenefitCrystallisationEventAmount = 2500,
    uncrystallisedRightsAmount = 75500,
    nonUKRightsAmount = 0,
    pensionDebitAmount = Some(25000),
    pensionDebitEnteredAmount = Some(25000),
    notificationIdentifier = Some(3),
    protectedAmount = Some(120000),
    pensionDebitStartDate = Some("2026-07-09"),
    pensionDebitTotalAmount = Some(40000)
  )

  val amendProtectionInputProtectionModel: ProtectionModel = ProtectionModel(
    psaCheckReference = None,
    protectionID = Some(lifetimeAllowanceIdentifier),
    version = Some(lifetimeAllowanceSequenceNumber),
    protectionType = Some("IP2014"),
    certificateDate = Some("2025-07-15T174312"),
    status = Some("dormant"),
    protectionReference = Some(protectionReference),
    relevantAmount = Some(105000),
    preADayPensionInPayment = Some(1500.00),
    postADayBenefitCrystallisationEvents = Some(2500.00),
    uncrystallisedRights = Some(75500.00),
    nonUKRights = Some(0.00),
    pensionDebitAmount = Some(25000),
    pensionDebitEnteredAmount = Some(25000),
    notificationId = Some(3),
    protectedAmount = Some(120000),
    pensionDebitStartDate = Some("2026-07-09"),
    pensionDebitTotalAmount = Some(40000)
  )

}
