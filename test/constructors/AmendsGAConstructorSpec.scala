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

package constructors

import models.amend.{AmendProtectionFields, AmendsGAModel}
import models.{DateModel, PensionDebitModel}
import testHelpers.FakeApplication

class AmendsGAConstructorSpec extends FakeApplication {

  val testAmendProtectionFields1: AmendProtectionFields = AmendProtectionFields(
    uncrystallisedRights = 100000.00,
    nonUKRights = Some(2000.00),
    preADayPensionInPayment = Some(2000.00),
    postADayBenefitCrystallisationEvents = Some(2000.00),
    pensionDebit = None
  )

  val testAmendProtectionFields2: AmendProtectionFields = AmendProtectionFields(
    uncrystallisedRights = 250000.00,
    nonUKRights = Some(500.00),
    preADayPensionInPayment = Some(1000.00),
    postADayBenefitCrystallisationEvents = Some(1000.00),
    pensionDebit = Some(PensionDebitModel(DateModel.of(2016, 10, 23), 1000.0))
  )

  val testAmendProtectionFields3: AmendProtectionFields = AmendProtectionFields(
    uncrystallisedRights = 250000.00,
    nonUKRights = Some(0.00),
    preADayPensionInPayment = Some(0.00),
    postADayBenefitCrystallisationEvents = Some(0.00),
    pensionDebit = None
  )

  "Calling the identify changes method" when {

    "The original and updated protection models are the same" in {
      val original = testAmendProtectionFields1
      val updated  = testAmendProtectionFields1

      AmendsGAConstructor.identifyAmendsChanges(updated, original) shouldBe AmendsGAModel(None, None, None, None, None)
    }

    "The original and updated protection models are different" when {

      "The values for Current Pensions, PTBefore, PTBetween and Overseas Pensions are updated" in {
        val original = testAmendProtectionFields1
        val updated  = testAmendProtectionFields2

        AmendsGAConstructor.identifyAmendsChanges(updated, original) shouldBe AmendsGAModel(
          current = Some("UpdatedValue"),
          before = Some("UpdatedValue"),
          between = Some("UpdatedValue"),
          overseas = Some("UpdatedValue"),
          pso = Some("addedPSO")
        )
      }

      "PTBefore, PTBetween and Overseas Pensions are amended from 'No' to 'Yes'" in {
        val original = testAmendProtectionFields3
        val updated  = testAmendProtectionFields1

        AmendsGAConstructor.identifyAmendsChanges(updated, original) shouldBe AmendsGAModel(
          current = Some("UpdatedValue"),
          before = Some("ChangedToYes"),
          between = Some("ChangedToYes"),
          overseas = Some("ChangedToYes"),
          pso = None
        )
      }

      "PTBefore, PTBetween and Overseas Pensions are amended from 'Yes' to 'No'" in {
        val original = testAmendProtectionFields1
        val updated  = testAmendProtectionFields3

        AmendsGAConstructor.identifyAmendsChanges(updated, original) shouldBe AmendsGAModel(
          current = Some("UpdatedValue"),
          before = Some("ChangedToNo"),
          between = Some("ChangedToNo"),
          overseas = Some("ChangedToNo"),
          pso = None
        )
      }
    }

  }

}
