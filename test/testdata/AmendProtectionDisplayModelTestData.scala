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

import models.NotificationId._
import models.display.{AmendOutcomeDisplayModel, AmendOutcomeDisplayModelNoNotificationId, AmendPrintDisplayModel}
import models.AmendedProtectionType.{
  FixedProtection2016,
  IndividualProtection2014,
  IndividualProtection2014LTA,
  IndividualProtection2016,
  IndividualProtection2016LTA
}
import models.pla.response.AmendProtectionResponseStatus.{Dormant, Open, Withdrawn}
import models.{AmendResponseModel, DateModel, Person, PersonalDetailsModel, TimeModel}

object AmendProtectionDisplayModelTestData {
  val psaCheckReference = "psaRef"

  val nino = "nino"

  val protectionReferenceIndividualProtection2014 = "IPXXXXX"
  val protectionReferenceIndividualProtection2016 = "IPXXXXX"
  val protectionReferenceFixedProtection2016      = "FPXXXXX"

  val protectedAmountIndividualProtection2014Above   = "£1,500,001"
  val protectedAmountIndividualProtection2014Max     = "£1,500,000"
  val protectedAmountIndividualProtection2014InRange = "£1,499,996"
  val protectedAmountIndividualProtection2014Below   = "£1,249,999"

  val protectedAmountIndividualProtection2016Above   = "£1,250,001"
  val protectedAmountIndividualProtection2016Max     = "£1,250,000"
  val protectedAmountIndividualProtection2016InRange = "£1,249,996"
  val protectedAmountIndividualProtection2016Below   = "£999,999"

  val personalDetailsModel = PersonalDetailsModel(
    Person(
      firstName = "Jim",
      lastName = "Davis"
    )
  )

  private val exampleAmendPrintDisplayModelIndividualProtection2014 = AmendPrintDisplayModel(
    firstName = "Jim",
    surname = "Davis",
    nino = nino,
    protectionType = IndividualProtection2014,
    status = Some(Dormant),
    psaCheckReference = Some(psaCheckReference),
    protectionReference = Some(protectionReferenceIndividualProtection2014),
    fixedProtectionReference = None,
    protectedAmount = Some(protectedAmountIndividualProtection2014InRange),
    certificateDate = Some("14 July 2015"),
    certificateTime = Some("3:14pm")
  )

  private val exampleAmendResponseModelIndividualProtection2014 = AmendResponseModel(
    identifier = 1,
    sequence = 1,
    psaCheckReference = psaCheckReference,
    protectionType = IndividualProtection2014,
    status = Dormant,
    certificateDate = Some(DateModel.of(2015, 7, 14)),
    certificateTime = Some(TimeModel.of(15, 14, 0)),
    protectionReference = Some(protectionReferenceIndividualProtection2014),
    relevantAmount = 1_499_996,
    preADayPensionInPaymentAmount = 374_999,
    postADayBenefitCrystallisationEventAmount = 374_999,
    uncrystallisedRightsAmount = 374_999,
    nonUKRightsAmount = 374_999,
    notificationId = None,
    protectedAmount = Some(1_499_996),
    pensionDebitTotalAmount = None,
    pensionDebit = None
  )

  private val exampleAmendPrintDisplayModelIndividualProtection2016: AmendPrintDisplayModel = AmendPrintDisplayModel(
    firstName = "Jim",
    surname = "Davis",
    nino = nino,
    protectionType = IndividualProtection2016,
    status = Some(Dormant),
    psaCheckReference = Some(psaCheckReference),
    protectionReference = Some(protectionReferenceIndividualProtection2016),
    fixedProtectionReference = None,
    protectedAmount = Some(protectedAmountIndividualProtection2016InRange),
    certificateDate = Some("14 July 2017"),
    certificateTime = Some("3:14pm")
  )

  private val exampleAmendResponseModelIndividualProtection2016 = AmendResponseModel(
    identifier = 1,
    sequence = 1,
    psaCheckReference = psaCheckReference,
    protectionType = IndividualProtection2016,
    status = Dormant,
    certificateDate = Some(DateModel.of(2017, 7, 14)),
    certificateTime = Some(TimeModel.of(15, 14, 0)),
    protectionReference = Some(protectionReferenceIndividualProtection2016),
    relevantAmount = 1_249_996,
    preADayPensionInPaymentAmount = 312_499,
    postADayBenefitCrystallisationEventAmount = 312_499,
    uncrystallisedRightsAmount = 312_499,
    nonUKRightsAmount = 312_499,
    notificationId = None,
    protectedAmount = Some(1_249_996),
    pensionDebitTotalAmount = None,
    pensionDebit = None
  )

  val amendResponseModelNoNotificationIdIndividualProtection2014: AmendResponseModel =
    exampleAmendResponseModelIndividualProtection2014.copy(
      relevantAmount = 1_500_001,
      protectedAmount = Some(1_500_000),
      uncrystallisedRightsAmount = 375_004,
      status = Open
    )

  val amendPrintDisplayModelNoNotificationIdIndividualProtection2014: AmendPrintDisplayModel =
    exampleAmendPrintDisplayModelIndividualProtection2014.copy(
      protectionReference = None,
      psaCheckReference = None,
      protectedAmount = Some(protectedAmountIndividualProtection2014Max),
      status = Some(Open)
    )

  val amendOutcomeDisplayModelNoNotificationIdIndividualProtection2014 = AmendOutcomeDisplayModelNoNotificationId(
    protectionType = IndividualProtection2014,
    protectedAmount = protectedAmountIndividualProtection2014Max,
    details = Some(amendPrintDisplayModelNoNotificationIdIndividualProtection2014)
  )

  val amendOutcomeDisplayModelNoNotificationIdIndividualProtection2014LTA = AmendOutcomeDisplayModelNoNotificationId(
    protectionType = IndividualProtection2014LTA,
    protectedAmount = protectedAmountIndividualProtection2014Max,
    details = Some(
      amendPrintDisplayModelNoNotificationIdIndividualProtection2014.copy(
        protectionType = IndividualProtection2014LTA
      )
    )
  )

  val amendResponseModelNoNotificationIdIndividualProtection2016: AmendResponseModel =
    exampleAmendResponseModelIndividualProtection2016.copy(
      relevantAmount = 1_250_001,
      protectedAmount = Some(1_250_000),
      uncrystallisedRightsAmount = 312_504,
      status = Open
    )

  val amendPrintDisplayModelNoNotificationIdIndividualProtection2016: AmendPrintDisplayModel =
    exampleAmendPrintDisplayModelIndividualProtection2016.copy(
      protectionReference = None,
      psaCheckReference = None,
      protectedAmount = Some(protectedAmountIndividualProtection2016Max),
      status = Some(Open)
    )

  val amendOutcomeDisplayModelNoNotificationIdIndividualProtection2016 = AmendOutcomeDisplayModelNoNotificationId(
    protectionType = IndividualProtection2016,
    protectedAmount = protectedAmountIndividualProtection2016Max,
    details = Some(amendPrintDisplayModelNoNotificationIdIndividualProtection2016)
  )

  val amendOutcomeDisplayModelNoNotificationIdIndividualProtection2016LTA = AmendOutcomeDisplayModelNoNotificationId(
    protectionType = IndividualProtection2016LTA,
    protectedAmount = protectedAmountIndividualProtection2016Max,
    details = Some(
      amendPrintDisplayModelNoNotificationIdIndividualProtection2016.copy(
        protectionType = IndividualProtection2016LTA
      )
    )
  )

  val amendResponseModelNotification1: AmendResponseModel = exampleAmendResponseModelIndividualProtection2014.copy(
    notificationId = Some(NotificationId1),
    status = Open
  )

  val amendPrintDisplayModelNotification1: AmendPrintDisplayModel =
    exampleAmendPrintDisplayModelIndividualProtection2014.copy(
      status = None
    )

  val amendOutcomeDisplayModelNotification1 = AmendOutcomeDisplayModel(
    notificationId = NotificationId1,
    protectedAmount = protectedAmountIndividualProtection2014InRange,
    details = Some(amendPrintDisplayModelNotification1)
  )

  val amendResponseModelNotification2: AmendResponseModel = exampleAmendResponseModelIndividualProtection2014.copy(
    notificationId = Some(NotificationId2)
  )

  val amendPrintDisplayModelNotification2: AmendPrintDisplayModel =
    exampleAmendPrintDisplayModelIndividualProtection2014.copy(
      psaCheckReference = None,
      protectionReference = None
    )

  val amendOutcomeDisplayModelNotification2 = AmendOutcomeDisplayModel(
    notificationId = NotificationId2,
    protectedAmount = protectedAmountIndividualProtection2014InRange,
    details = Some(amendPrintDisplayModelNotification2)
  )

  val amendResponseModelNotification3: AmendResponseModel = exampleAmendResponseModelIndividualProtection2014.copy(
    notificationId = Some(NotificationId3)
  )

  val amendPrintDisplayModelNotification3: AmendPrintDisplayModel =
    exampleAmendPrintDisplayModelIndividualProtection2014.copy(
      psaCheckReference = None,
      protectionReference = None
    )

  val amendOutcomeDisplayModelNotification3 = AmendOutcomeDisplayModel(
    notificationId = NotificationId3,
    protectedAmount = protectedAmountIndividualProtection2014InRange,
    details = Some(amendPrintDisplayModelNotification3)
  )

  val amendResponseModelNotification4: AmendResponseModel = exampleAmendResponseModelIndividualProtection2014.copy(
    notificationId = Some(NotificationId4)
  )

  val amendPrintDisplayModelNotification4: AmendPrintDisplayModel =
    exampleAmendPrintDisplayModelIndividualProtection2014.copy(
      psaCheckReference = None,
      protectionReference = None
    )

  val amendOutcomeDisplayModelNotification4 = AmendOutcomeDisplayModel(
    notificationId = NotificationId4,
    protectedAmount = protectedAmountIndividualProtection2014InRange,
    details = Some(amendPrintDisplayModelNotification4)
  )

  val amendResponseModelNotification5: AmendResponseModel = exampleAmendResponseModelIndividualProtection2014.copy(
    notificationId = Some(NotificationId5),
    status = Open
  )

  val amendPrintDisplayModelNotification5: AmendPrintDisplayModel =
    exampleAmendPrintDisplayModelIndividualProtection2014.copy(
      status = None
    )

  val amendOutcomeDisplayModelNotification5 = AmendOutcomeDisplayModel(
    notificationId = NotificationId5,
    protectedAmount = protectedAmountIndividualProtection2014InRange,
    details = Some(amendPrintDisplayModelNotification5)
  )

  val amendResponseModelNotification6: AmendResponseModel = exampleAmendResponseModelIndividualProtection2014.copy(
    notificationId = Some(NotificationId6),
    status = Withdrawn,
    protectedAmount = Some(0),
    relevantAmount = 1_249_999,
    uncrystallisedRightsAmount = 125_002
  )

  val amendPrintDisplayModelNotification6: AmendPrintDisplayModel =
    exampleAmendPrintDisplayModelIndividualProtection2014.copy(
      psaCheckReference = None,
      protectionReference = None,
      status = Some(Withdrawn),
      protectedAmount = Some(protectedAmountIndividualProtection2014Below)
    )

  val amendOutcomeDisplayModelNotification6 = AmendOutcomeDisplayModel(
    notificationId = NotificationId6,
    protectedAmount = protectedAmountIndividualProtection2014Below,
    details = Some(amendPrintDisplayModelNotification6)
  )

  val amendResponseModelNotification7: AmendResponseModel = exampleAmendResponseModelIndividualProtection2014.copy(
    notificationId = Some(NotificationId7),
    protectionType = FixedProtection2016,
    status = Withdrawn,
    protectionReference = Some(protectionReferenceFixedProtection2016),
    protectedAmount = Some(0),
    relevantAmount = 1_249_999,
    uncrystallisedRightsAmount = 125_002
  )

  val amendPrintDisplayModelNotification7: AmendPrintDisplayModel =
    exampleAmendPrintDisplayModelIndividualProtection2014.copy(
      protectionType = FixedProtection2016,
      status = None,
      psaCheckReference = None,
      protectionReference = None,
      protectedAmount = Some(protectedAmountIndividualProtection2014Below),
      fixedProtectionReference = Some(protectionReferenceFixedProtection2016)
    )

  val amendOutcomeDisplayModelNotification7 = AmendOutcomeDisplayModel(
    notificationId = NotificationId7,
    protectedAmount = protectedAmountIndividualProtection2014Below,
    details = Some(amendPrintDisplayModelNotification7)
  )

  val amendResponseModelNotification8: AmendResponseModel = exampleAmendResponseModelIndividualProtection2016.copy(
    notificationId = Some(NotificationId8)
  )

  val amendPrintDisplayModelNotification8: AmendPrintDisplayModel =
    exampleAmendPrintDisplayModelIndividualProtection2016.copy(
      status = None
    )

  val amendOutcomeDisplayModelNotification8 = AmendOutcomeDisplayModel(
    notificationId = NotificationId8,
    protectedAmount = protectedAmountIndividualProtection2016InRange,
    details = Some(amendPrintDisplayModelNotification8)
  )

  val amendResponseModelNotification9: AmendResponseModel = exampleAmendResponseModelIndividualProtection2016.copy(
    notificationId = Some(NotificationId9)
  )

  val amendPrintDisplayModelNotification9: AmendPrintDisplayModel =
    exampleAmendPrintDisplayModelIndividualProtection2016.copy(
      protectionReference = None,
      psaCheckReference = None
    )

  val amendOutcomeDisplayModelNotification9 = AmendOutcomeDisplayModel(
    notificationId = NotificationId9,
    protectedAmount = protectedAmountIndividualProtection2016InRange,
    details = Some(amendPrintDisplayModelNotification9)
  )

  val amendResponseModelNotification10: AmendResponseModel = exampleAmendResponseModelIndividualProtection2016.copy(
    notificationId = Some(NotificationId10)
  )

  val amendPrintDisplayModelNotification10: AmendPrintDisplayModel =
    exampleAmendPrintDisplayModelIndividualProtection2016.copy(
      protectionReference = None,
      psaCheckReference = None
    )

  val amendOutcomeDisplayModelNotification10 = AmendOutcomeDisplayModel(
    notificationId = NotificationId10,
    protectedAmount = protectedAmountIndividualProtection2016InRange,
    details = Some(amendPrintDisplayModelNotification10)
  )

  val amendResponseModelNotification11: AmendResponseModel = exampleAmendResponseModelIndividualProtection2016.copy(
    notificationId = Some(NotificationId11)
  )

  val amendPrintDisplayModelNotification11: AmendPrintDisplayModel =
    exampleAmendPrintDisplayModelIndividualProtection2016.copy(
      protectionReference = None,
      psaCheckReference = None
    )

  val amendOutcomeDisplayModelNotification11 = AmendOutcomeDisplayModel(
    notificationId = NotificationId11,
    protectedAmount = protectedAmountIndividualProtection2016InRange,
    details = Some(amendPrintDisplayModelNotification11)
  )

  val amendResponseModelNotification12: AmendResponseModel = exampleAmendResponseModelIndividualProtection2016.copy(
    notificationId = Some(NotificationId12)
  )

  val amendPrintDisplayModelNotification12: AmendPrintDisplayModel =
    exampleAmendPrintDisplayModelIndividualProtection2016.copy(
      protectionReference = None,
      psaCheckReference = None
    )

  val amendOutcomeDisplayModelNotification12 = AmendOutcomeDisplayModel(
    notificationId = NotificationId12,
    protectedAmount = protectedAmountIndividualProtection2016InRange,
    details = Some(amendPrintDisplayModelNotification12)
  )

  val amendResponseModelNotification13: AmendResponseModel = exampleAmendResponseModelIndividualProtection2016.copy(
    notificationId = Some(NotificationId13),
    status = Withdrawn,
    protectedAmount = Some(0),
    relevantAmount = 999_999,
    uncrystallisedRightsAmount = 62_502
  )

  val amendPrintDisplayModelNotification13: AmendPrintDisplayModel =
    exampleAmendPrintDisplayModelIndividualProtection2016.copy(
      protectionReference = None,
      psaCheckReference = None,
      status = Some(Withdrawn),
      protectedAmount = Some(protectedAmountIndividualProtection2016Below)
    )

  val amendOutcomeDisplayModelNotification13 = AmendOutcomeDisplayModel(
    notificationId = NotificationId13,
    protectedAmount = protectedAmountIndividualProtection2016Below,
    details = Some(amendPrintDisplayModelNotification13)
  )

  val amendResponseModelNotification14: AmendResponseModel = exampleAmendResponseModelIndividualProtection2016.copy(
    notificationId = Some(NotificationId14),
    protectionType = FixedProtection2016,
    status = Withdrawn,
    protectionReference = Some(protectionReferenceFixedProtection2016),
    protectedAmount = Some(0),
    relevantAmount = 999_999,
    uncrystallisedRightsAmount = 62_502
  )

  val amendPrintDisplayModelNotification14: AmendPrintDisplayModel =
    exampleAmendPrintDisplayModelIndividualProtection2016.copy(
      protectionType = FixedProtection2016,
      status = None,
      psaCheckReference = None,
      protectionReference = None,
      protectedAmount = Some(protectedAmountIndividualProtection2016Below),
      fixedProtectionReference = Some(protectionReferenceFixedProtection2016)
    )

  val amendOutcomeDisplayModelNotification14 = AmendOutcomeDisplayModel(
    notificationId = NotificationId14,
    protectedAmount = protectedAmountIndividualProtection2016Below,
    details = Some(amendPrintDisplayModelNotification14)
  )

}
