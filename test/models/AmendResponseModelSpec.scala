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

package models

import models.NotificationId.NotificationId7
import models.pla.response.{AmendProtectionResponseStatus, ProtectionStatus, ProtectionType}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import testdata.PlaConnectorTestData._

class AmendResponseModelSpec extends AnyWordSpec with Matchers {

  val amendResponseModel = AmendResponseModel(
    psaCheckReference = "testPSARef",
    identifier = lifetimeAllowanceIdentifier,
    sequence = lifetimeAllowanceSequenceNumber + 1,
    protectionType = AmendedProtectionType.IndividualProtection2014,
    certificateDate = Some(DateModel.of(2025, 7, 15)),
    certificateTime = Some(TimeModel.of(17, 43, 12)),
    status = AmendProtectionResponseStatus.Dormant,
    protectionReference = Some(protectionReference),
    relevantAmount = 105_000,
    preADayPensionInPaymentAmount = 1_500,
    postADayBenefitCrystallisationEventAmount = 2_500,
    uncrystallisedRightsAmount = 75_500,
    nonUKRightsAmount = 0,
    pensionDebit = Some(PensionDebitModel(DateModel.of(2026, 7, 9), 25_000)),
    notificationId = Some(NotificationId7),
    protectedAmount = Some(120_000),
    pensionDebitTotalAmount = Some(40_000)
  )

  val protectionModel = ProtectionModel(
    psaCheckReference = "fixedPsaRef",
    identifier = 10101,
    sequence = 20202,
    protectionType = ProtectionType.IndividualProtection2014,
    status = ProtectionStatus.Open,
    certificateDate = Some(DateModel.of(2025, 12, 8)),
    certificateTime = Some(TimeModel.of(15, 34, 45)),
    protectedAmount = None,
    relevantAmount = None,
    postADayBenefitCrystallisationEvents = None,
    preADayPensionInPayment = None,
    uncrystallisedRights = None,
    nonUKRights = None,
    pensionDebitTotalAmount = None,
    protectionReference = None,
    lumpSumPercentage = None,
    lumpSumAmount = None,
    enhancementFactor = None
  )

  "AmendResponseModel on from" should {

    "return correct AmendResponseModel" in {
      AmendResponseModel.from(amendProtectionResponse, "testPSARef") shouldBe amendResponseModel
    }
  }

  "toProtectionModel" should {
    "return the correct protection model" in {
      amendResponseModel.toProtectionModel shouldBe ProtectionModel(
        psaCheckReference = "testPSARef",
        identifier = lifetimeAllowanceIdentifier,
        sequence = lifetimeAllowanceSequenceNumber + 1,
        protectionType = ProtectionType.IndividualProtection2014,
        certificateDate = Some(DateModel.of(2025, 7, 15)),
        certificateTime = Some(TimeModel.of(17, 43, 12)),
        status = ProtectionStatus.Dormant,
        protectionReference = Some(protectionReference),
        relevantAmount = Some(105_000),
        preADayPensionInPayment = Some(1_500),
        postADayBenefitCrystallisationEvents = Some(2_500),
        uncrystallisedRights = Some(75_500),
        nonUKRights = Some(0),
        protectedAmount = Some(120_000),
        pensionDebitTotalAmount = Some(40_000)
      )
    }
  }

  "combineWithFixedProtection2016" should {
    val fixedProtectionTypes = Seq(
      ProtectionType.FixedProtection2016    -> AmendedProtectionType.FixedProtection2016,
      ProtectionType.FixedProtection2016LTA -> AmendedProtectionType.FixedProtection2016LTA
    )
    "return Some with correct fields" when
      fixedProtectionTypes.foreach { case (protectionType, amendedProtectionType) =>
        val protection = protectionModel.copy(
          protectionType = protectionType
        )

        amendResponseModel.combineWithFixedProtection2016(protection) shouldBe Some(
          amendResponseModel.copy(
            protectionType = amendedProtectionType,
            protectionReference = protectionModel.protectionReference,
            certificateDate = protectionModel.certificateDate,
            certificateTime = protectionModel.certificateTime
          )
        )
      }

    "return None" when
      ProtectionType.values.diff(fixedProtectionTypes.map(_._1)).foreach { protectionType =>
        s"provided with '$protectionType'" in {
          val protection = protectionModel.copy(
            protectionType = protectionType
          )

          amendResponseModel.combineWithFixedProtection2016(protection) shouldBe None
        }
      }
  }

}
