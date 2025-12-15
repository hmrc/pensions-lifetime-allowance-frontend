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

package models.pla.request

import models.amend.AmendProtectionModel
import models.pla.response.{ProtectionStatus, ProtectionType}
import models.{DateModel, PensionDebitModel, ProtectionModel, TimeModel}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import testdata.PlaConnectorTestData._

class AmendProtectionRequestSpec extends AnyWordSpec with Matchers {

  val pensionDebit = PensionDebitModel(DateModel.of(2026, 7, 9), 25_000)

  val protectionModel = ProtectionModel(
    psaCheckReference = "psaCheckReference",
    identifier = lifetimeAllowanceIdentifier,
    sequenceNumber = lifetimeAllowanceSequenceNumber,
    protectionType = ProtectionType.IndividualProtection2014,
    status = ProtectionStatus.Dormant,
    certificateDate = Some(DateModel.of(2025, 7, 15)),
    certificateTime = Some(TimeModel.of(17, 43, 12)),
    protectionReference = Some(protectionReference),
    relevantAmount = Some(105_000),
    preADayPensionInPaymentAmount = Some(1_500.00),
    postADayBenefitCrystallisationEventAmount = Some(2_500.00),
    uncrystallisedRightsAmount = Some(75_500.00),
    nonUKRightsAmount = Some(0.00),
    protectedAmount = Some(120_000),
    pensionDebitTotalAmount = Some(40_000)
  )

  val amendProtectionModel: AmendProtectionModel =
    AmendProtectionModel.tryFromProtection(protectionModel).get.withPensionDebit(Some(pensionDebit))

  "AmendProtectionRequest on from" should {

    "return correct AmendProtectionRequest" when {

      "all mandatory fields are present" in {
        AmendProtectionRequest.from(amendProtectionModel) shouldBe amendProtectionRequest
      }

      "all mandatory fields are present and optional fields are empty" in {
        val input = amendProtectionModel.copy(
          certificateDate = None,
          certificateTime = None,
          protectedAmount = None,
          protectionReference = None,
          updated = amendProtectionModel.updated.copy(
            pensionDebit = None
          )
        )

        val expectedOutput = amendProtectionRequest.copy(
          certificateDate = None,
          certificateTime = None,
          protectionReference = None,
          pensionDebitAmount = None,
          pensionDebitEnteredAmount = None,
          notificationIdentifier = None,
          protectedAmount = None,
          pensionDebitStartDate = None
        )

        AmendProtectionRequest.from(input) shouldBe expectedOutput
      }
    }
  }

}
