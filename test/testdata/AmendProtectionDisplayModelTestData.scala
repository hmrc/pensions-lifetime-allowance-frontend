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

import models.{AmendResponseModel, Person, PersonalDetailsModel, ProtectionModel}
import models.display.{AmendOutcomeDisplayModel, AmendOutcomeDisplayModelNoNotificationId, AmendPrintDisplayModel}
import models.pla.response.ProtectionStatus.{Dormant, Open, Withdrawn}
import models.pla.response.ProtectionType.{
  FixedProtection2016,
  IndividualProtection2014,
  IndividualProtection2014LTA,
  IndividualProtection2016,
  IndividualProtection2016LTA
}

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
    protectionType = IndividualProtection2014.toString,
    status = Some(Dormant.toString),
    psaCheckReference = Some(psaCheckReference),
    protectionReference = Some(protectionReferenceIndividualProtection2014),
    fixedProtectionReference = None,
    protectedAmount = Some(protectedAmountIndividualProtection2014InRange),
    certificateDate = Some("14 July 2015"),
    certificateTime = Some("3:14pm")
  )

  private val exampleAmendResponseProtectionModelIndividualProtection2014 = ProtectionModel(
    protectionID = Some(1),
    version = Some(1),
    psaCheckReference = Some(psaCheckReference),
    protectionType = Some(IndividualProtection2014.toString),
    certificateDate = Some("2015-07-14T151400"),
    status = Some(Dormant.toString),
    protectionReference = Some(protectionReferenceIndividualProtection2014),
    relevantAmount = Some(1_499_996),
    preADayPensionInPayment = Some(374_999),
    postADayBenefitCrystallisationEvents = Some(374_999),
    uncrystallisedRights = Some(374_999),
    nonUKRights = Some(374_999),
    pensionDebitAmount = None,
    pensionDebitEnteredAmount = None,
    notificationId = None,
    protectedAmount = Some(1_499_996),
    pensionDebitStartDate = None,
    pensionDebitTotalAmount = None
  )

  private val exampleAmendPrintDisplayModelIndividualProtection2016: AmendPrintDisplayModel = AmendPrintDisplayModel(
    firstName = "Jim",
    surname = "Davis",
    nino = nino,
    protectionType = IndividualProtection2016.toString,
    status = Some(Dormant.toString),
    psaCheckReference = Some(psaCheckReference),
    protectionReference = Some(protectionReferenceIndividualProtection2016),
    fixedProtectionReference = None,
    protectedAmount = Some(protectedAmountIndividualProtection2016InRange),
    certificateDate = Some("14 July 2017"),
    certificateTime = Some("3:14pm")
  )

  private val exampleAmendResponseProtectionModelIndividualProtection2016 = ProtectionModel(
    protectionID = Some(1),
    version = Some(1),
    psaCheckReference = Some(psaCheckReference),
    protectionType = Some(IndividualProtection2016.toString),
    certificateDate = Some("2017-07-14T151400"),
    status = Some(Dormant.toString),
    protectionReference = Some(protectionReferenceIndividualProtection2016),
    relevantAmount = Some(1_249_996),
    preADayPensionInPayment = Some(312_499),
    postADayBenefitCrystallisationEvents = Some(312_499),
    uncrystallisedRights = Some(312_499),
    nonUKRights = Some(312_499),
    pensionDebitAmount = None,
    pensionDebitEnteredAmount = None,
    notificationId = None,
    protectedAmount = Some(1_249_996),
    pensionDebitStartDate = None,
    pensionDebitTotalAmount = None
  )

  val amendResponseModelNoNotificationIdIndividualProtection2014 = AmendResponseModel(
    exampleAmendResponseProtectionModelIndividualProtection2014.copy(
      relevantAmount = Some(1_500_001),
      protectedAmount = Some(1_500_000),
      uncrystallisedRights = Some(375_004),
      status = Some(Open.toString)
    )
  )

  val amendPrintDisplayModelNoNotificationIdIndividualProtection2014: AmendPrintDisplayModel =
    exampleAmendPrintDisplayModelIndividualProtection2014.copy(
      protectionReference = None,
      psaCheckReference = None,
      protectedAmount = Some(protectedAmountIndividualProtection2014Max),
      status = Some(Open.toString)
    )

  val amendResultDisplayModelNoNotificationIdIndividualProtection2014 = AmendOutcomeDisplayModelNoNotificationId(
    protectionType = IndividualProtection2014.toString,
    protectedAmount = protectedAmountIndividualProtection2014Max,
    details = Some(amendPrintDisplayModelNoNotificationIdIndividualProtection2014)
  )

  val amendResultDisplayModelNoNotificationIdIndividualProtection2014LTA = AmendOutcomeDisplayModelNoNotificationId(
    protectionType = IndividualProtection2014LTA.toString,
    protectedAmount = protectedAmountIndividualProtection2014Max,
    details = Some(
      amendPrintDisplayModelNoNotificationIdIndividualProtection2014.copy(
        protectionType = IndividualProtection2014LTA.toString
      )
    )
  )

  val amendResponseModelNoNotificationIdIndividualProtection2016 = AmendResponseModel(
    exampleAmendResponseProtectionModelIndividualProtection2016.copy(
      relevantAmount = Some(1_250_001),
      protectedAmount = Some(1_250_000),
      uncrystallisedRights = Some(312_504),
      status = Some(Open.toString)
    )
  )

  val amendPrintDisplayModelNoNotificationIdIndividualProtection2016: AmendPrintDisplayModel =
    exampleAmendPrintDisplayModelIndividualProtection2016.copy(
      protectionReference = None,
      psaCheckReference = None,
      protectedAmount = Some(protectedAmountIndividualProtection2016Max),
      status = Some(Open.toString)
    )

  val amendResultDisplayModelNoNotificationIdIndividualProtection2016 = AmendOutcomeDisplayModelNoNotificationId(
    protectionType = IndividualProtection2016.toString,
    protectedAmount = protectedAmountIndividualProtection2016Max,
    details = Some(amendPrintDisplayModelNoNotificationIdIndividualProtection2016)
  )

  val amendResultDisplayModelNoNotificationIdIndividualProtection2016LTA = AmendOutcomeDisplayModelNoNotificationId(
    protectionType = IndividualProtection2016LTA.toString,
    protectedAmount = protectedAmountIndividualProtection2016Max,
    details = Some(
      amendPrintDisplayModelNoNotificationIdIndividualProtection2016.copy(
        protectionType = IndividualProtection2016LTA.toString
      )
    )
  )

  val amendResponseModelNotification1 = AmendResponseModel(
    exampleAmendResponseProtectionModelIndividualProtection2014.copy(
      notificationId = Some(1),
      status = Some(Open.toString)
    )
  )

  val amendPrintDisplayModelNotification1: AmendPrintDisplayModel =
    exampleAmendPrintDisplayModelIndividualProtection2014.copy(
      status = None
    )

  val amendResultDisplayModelNotification1 = AmendOutcomeDisplayModel(
    notificationId = 1,
    protectedAmount = protectedAmountIndividualProtection2014InRange,
    details = Some(amendPrintDisplayModelNotification1)
  )

  val amendResponseModelNotification2 = AmendResponseModel(
    exampleAmendResponseProtectionModelIndividualProtection2014.copy(
      notificationId = Some(2)
    )
  )

  val amendPrintDisplayModelNotification2: AmendPrintDisplayModel =
    exampleAmendPrintDisplayModelIndividualProtection2014.copy(
      psaCheckReference = None,
      protectionReference = None
    )

  val amendResultDisplayModelNotification2 = AmendOutcomeDisplayModel(
    notificationId = 2,
    protectedAmount = protectedAmountIndividualProtection2014InRange,
    details = Some(amendPrintDisplayModelNotification2)
  )

  val amendResponseModelNotification3 = AmendResponseModel(
    exampleAmendResponseProtectionModelIndividualProtection2014.copy(
      notificationId = Some(3)
    )
  )

  val amendPrintDisplayModelNotification3: AmendPrintDisplayModel =
    exampleAmendPrintDisplayModelIndividualProtection2014.copy(
      psaCheckReference = None,
      protectionReference = None
    )

  val amendResultDisplayModelNotification3 = AmendOutcomeDisplayModel(
    notificationId = 3,
    protectedAmount = protectedAmountIndividualProtection2014InRange,
    details = Some(amendPrintDisplayModelNotification3)
  )

  val amendResponseModelNotification4 = AmendResponseModel(
    exampleAmendResponseProtectionModelIndividualProtection2014.copy(
      notificationId = Some(4)
    )
  )

  val amendPrintDisplayModelNotification4: AmendPrintDisplayModel =
    exampleAmendPrintDisplayModelIndividualProtection2014.copy(
      psaCheckReference = None,
      protectionReference = None
    )

  val amendResultDisplayModelNotification4 = AmendOutcomeDisplayModel(
    notificationId = 4,
    protectedAmount = protectedAmountIndividualProtection2014InRange,
    details = Some(amendPrintDisplayModelNotification4)
  )

  val amendResponseModelNotification5 = AmendResponseModel(
    exampleAmendResponseProtectionModelIndividualProtection2014.copy(
      notificationId = Some(5),
      status = Some(Open.toString)
    )
  )

  val amendPrintDisplayModelNotification5: AmendPrintDisplayModel =
    exampleAmendPrintDisplayModelIndividualProtection2014.copy(
      status = None
    )

  val amendResultDisplayModelNotification5 = AmendOutcomeDisplayModel(
    notificationId = 5,
    protectedAmount = protectedAmountIndividualProtection2014InRange,
    details = Some(amendPrintDisplayModelNotification5)
  )

  val amendResponseModelNotification6 = AmendResponseModel(
    exampleAmendResponseProtectionModelIndividualProtection2014.copy(
      notificationId = Some(6),
      status = Some(Withdrawn.toString),
      protectedAmount = Some(0),
      relevantAmount = Some(1_249_999),
      uncrystallisedRights = Some(125_002)
    )
  )

  val amendPrintDisplayModelNotification6: AmendPrintDisplayModel =
    exampleAmendPrintDisplayModelIndividualProtection2014.copy(
      psaCheckReference = None,
      protectionReference = None,
      status = Some(Withdrawn.toString),
      protectedAmount = Some(protectedAmountIndividualProtection2014Below)
    )

  val amendResultDisplayModelNotification6 = AmendOutcomeDisplayModel(
    notificationId = 6,
    protectedAmount = protectedAmountIndividualProtection2014Below,
    details = Some(amendPrintDisplayModelNotification6)
  )

  val amendResponseModelNotification7 = AmendResponseModel(
    exampleAmendResponseProtectionModelIndividualProtection2014.copy(
      notificationId = Some(7),
      protectionType = Some(FixedProtection2016.toString),
      status = Some(Withdrawn.toString),
      protectionReference = Some(protectionReferenceFixedProtection2016),
      protectedAmount = Some(0),
      relevantAmount = Some(1_249_999),
      uncrystallisedRights = Some(125_002)
    )
  )

  val amendPrintDisplayModelNotification7: AmendPrintDisplayModel =
    exampleAmendPrintDisplayModelIndividualProtection2014.copy(
      protectionType = FixedProtection2016.toString,
      status = None,
      psaCheckReference = None,
      protectionReference = None,
      protectedAmount = Some(protectedAmountIndividualProtection2014Below),
      fixedProtectionReference = Some(protectionReferenceFixedProtection2016)
    )

  val amendResultDisplayModelNotification7 = AmendOutcomeDisplayModel(
    notificationId = 7,
    protectedAmount = protectedAmountIndividualProtection2014Below,
    details = Some(amendPrintDisplayModelNotification7)
  )

  val amendResponseModelNotification8 = AmendResponseModel(
    exampleAmendResponseProtectionModelIndividualProtection2016.copy(
      notificationId = Some(8)
    )
  )

  val amendPrintDisplayModelNotification8: AmendPrintDisplayModel =
    exampleAmendPrintDisplayModelIndividualProtection2016.copy(
      status = None
    )

  val amendResultDisplayModelNotification8 = AmendOutcomeDisplayModel(
    notificationId = 8,
    protectedAmount = protectedAmountIndividualProtection2016InRange,
    details = Some(amendPrintDisplayModelNotification8)
  )

  val amendResponseModelNotification9 = AmendResponseModel(
    exampleAmendResponseProtectionModelIndividualProtection2016.copy(
      notificationId = Some(9)
    )
  )

  val amendPrintDisplayModelNotification9: AmendPrintDisplayModel =
    exampleAmendPrintDisplayModelIndividualProtection2016.copy(
      protectionReference = None,
      psaCheckReference = None
    )

  val amendResultDisplayModelNotification9 = AmendOutcomeDisplayModel(
    notificationId = 9,
    protectedAmount = protectedAmountIndividualProtection2016InRange,
    details = Some(amendPrintDisplayModelNotification9)
  )

  val amendResponseModelNotification10 = AmendResponseModel(
    exampleAmendResponseProtectionModelIndividualProtection2016.copy(
      notificationId = Some(10)
    )
  )

  val amendPrintDisplayModelNotification10: AmendPrintDisplayModel =
    exampleAmendPrintDisplayModelIndividualProtection2016.copy(
      protectionReference = None,
      psaCheckReference = None
    )

  val amendResultDisplayModelNotification10 = AmendOutcomeDisplayModel(
    notificationId = 10,
    protectedAmount = protectedAmountIndividualProtection2016InRange,
    details = Some(amendPrintDisplayModelNotification10)
  )

  val amendResponseModelNotification11 = AmendResponseModel(
    exampleAmendResponseProtectionModelIndividualProtection2016.copy(
      notificationId = Some(11)
    )
  )

  val amendPrintDisplayModelNotification11: AmendPrintDisplayModel =
    exampleAmendPrintDisplayModelIndividualProtection2016.copy(
      protectionReference = None,
      psaCheckReference = None
    )

  val amendResultDisplayModelNotification11 = AmendOutcomeDisplayModel(
    notificationId = 11,
    protectedAmount = protectedAmountIndividualProtection2016InRange,
    details = Some(amendPrintDisplayModelNotification11)
  )

  val amendResponseModelNotification12 = AmendResponseModel(
    exampleAmendResponseProtectionModelIndividualProtection2016.copy(
      notificationId = Some(12)
    )
  )

  val amendPrintDisplayModelNotification12: AmendPrintDisplayModel =
    exampleAmendPrintDisplayModelIndividualProtection2016.copy(
      protectionReference = None,
      psaCheckReference = None
    )

  val amendResultDisplayModelNotification12 = AmendOutcomeDisplayModel(
    notificationId = 12,
    protectedAmount = protectedAmountIndividualProtection2016InRange,
    details = Some(amendPrintDisplayModelNotification12)
  )

  val amendResponseModelNotification13 = AmendResponseModel(
    exampleAmendResponseProtectionModelIndividualProtection2016.copy(
      notificationId = Some(13),
      status = Some(Withdrawn.toString),
      protectedAmount = Some(0),
      relevantAmount = Some(999_999),
      uncrystallisedRights = Some(62_502)
    )
  )

  val amendPrintDisplayModelNotification13: AmendPrintDisplayModel =
    exampleAmendPrintDisplayModelIndividualProtection2016.copy(
      protectionReference = None,
      psaCheckReference = None,
      status = Some(Withdrawn.toString),
      protectedAmount = Some(protectedAmountIndividualProtection2016Below)
    )

  val amendResultDisplayModelNotification13 = AmendOutcomeDisplayModel(
    notificationId = 13,
    protectedAmount = protectedAmountIndividualProtection2016Below,
    details = Some(amendPrintDisplayModelNotification13)
  )

  val amendResponseModelNotification14 = AmendResponseModel(
    exampleAmendResponseProtectionModelIndividualProtection2016.copy(
      notificationId = Some(14),
      protectionType = Some(FixedProtection2016.toString),
      status = Some(Withdrawn.toString),
      protectionReference = Some(protectionReferenceFixedProtection2016),
      protectedAmount = Some(0),
      relevantAmount = Some(999_999),
      uncrystallisedRights = Some(62_502)
    )
  )

  val amendPrintDisplayModelNotification14: AmendPrintDisplayModel =
    exampleAmendPrintDisplayModelIndividualProtection2016.copy(
      protectionType = FixedProtection2016.toString,
      status = None,
      psaCheckReference = None,
      protectionReference = None,
      protectedAmount = Some(protectedAmountIndividualProtection2016Below),
      fixedProtectionReference = Some(protectionReferenceFixedProtection2016)
    )

  val amendResultDisplayModelNotification14 = AmendOutcomeDisplayModel(
    notificationId = 14,
    protectedAmount = protectedAmountIndividualProtection2016Below,
    details = Some(amendPrintDisplayModelNotification14)
  )

}
