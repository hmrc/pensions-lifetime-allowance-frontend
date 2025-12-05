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

import models.NotificationId.NotificationId3
import models.{DateModel, TimeModel}
import models.pla.AmendableProtectionType
import models.pla.request.{AmendProtectionRequest, AmendProtectionRequestStatus}
import models.pla.response.{AmendProtectionResponse, AmendProtectionResponseStatus, ReadProtectionsResponse}

object PlaConnectorTestData {

  val psaCheckReference: String            = "PSA12345678A"
  val lifetimeAllowanceIdentifier: Long    = 12960000000123L
  val lifetimeAllowanceSequenceNumber: Int = 13
  val protectionReference: String          = "IP141034571625B"

  val readProtectionsResponse: ReadProtectionsResponse = ReadProtectionsResponse(
    pensionSchemeAdministratorCheckReference = psaCheckReference,
    protectionRecordsList = Some(Seq.empty)
  )

  val amendProtectionRequest: AmendProtectionRequest = AmendProtectionRequest(
    lifetimeAllowanceSequenceNumber = lifetimeAllowanceSequenceNumber,
    lifetimeAllowanceType = AmendableProtectionType.IndividualProtection2014,
    certificateDate = Some(DateModel.of(2025, 7, 15)),
    certificateTime = Some(TimeModel.of(17, 43, 12)),
    status = AmendProtectionRequestStatus.Dormant,
    protectionReference = Some(protectionReference),
    relevantAmount = 39500,
    preADayPensionInPaymentAmount = 1500,
    postADayBenefitCrystallisationEventAmount = 2500,
    uncrystallisedRightsAmount = 75500,
    nonUKRightsAmount = 0,
    pensionDebitAmount = None,
    pensionDebitEnteredAmount = Some(25000),
    notificationIdentifier = None,
    protectedAmount = Some(120000),
    pensionDebitStartDate = Some(DateModel.of(2026, 7, 9)),
    pensionDebitTotalAmount = Some(40000)
  )

  val amendProtectionResponse: AmendProtectionResponse = AmendProtectionResponse(
    lifetimeAllowanceIdentifier = lifetimeAllowanceIdentifier,
    lifetimeAllowanceSequenceNumber = lifetimeAllowanceSequenceNumber + 1,
    lifetimeAllowanceType = AmendableProtectionType.IndividualProtection2014,
    certificateDate = Some(DateModel.of(2025, 7, 15)),
    certificateTime = Some(TimeModel.of(17, 43, 12)),
    status = AmendProtectionResponseStatus.Dormant,
    protectionReference = Some(protectionReference),
    relevantAmount = 105000,
    preADayPensionInPaymentAmount = 1500,
    postADayBenefitCrystallisationEventAmount = 2500,
    uncrystallisedRightsAmount = 75500,
    nonUKRightsAmount = 0,
    pensionDebitAmount = Some(25000),
    pensionDebitEnteredAmount = Some(25000),
    notificationIdentifier = Some(NotificationId3),
    protectedAmount = Some(120000),
    pensionDebitStartDate = Some(DateModel.of(2026, 7, 9)),
    pensionDebitTotalAmount = Some(40000)
  )

}
