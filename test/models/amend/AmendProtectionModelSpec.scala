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

package models.amend

import generators.ModelGenerators
import models.pla.AmendableProtectionType
import models.pla.request.AmendProtectionRequestStatus
import models.pla.response.{ProtectionStatus, ProtectionType}
import models.{DateModel, PensionDebitModel, ProtectionModel, TimeModel}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class AmendProtectionModelSpec extends AnyWordSpec with Matchers with ModelGenerators {

  val protectionModel = ProtectionModel(
    psaCheckReference = "psaCheckReference",
    identifier = 10101,
    sequenceNumber = 20202,
    protectionType = ProtectionType.IndividualProtection2014,
    status = ProtectionStatus.Open,
    certificateDate = Some(DateModel.of(2025, 12, 8)),
    certificateTime = Some(TimeModel.of(12, 57, 10)),
    protectedAmount = Some(1_500_000),
    relevantAmount = Some(1_700_000),
    postADayBenefitCrystallisationEventAmount = Some(450_000),
    preADayPensionInPaymentAmount = Some(450_000),
    uncrystallisedRightsAmount = Some(450_000),
    nonUKRightsAmount = Some(450_000),
    pensionDebitTotalAmount = Some(100_000),
    protectionReference = Some("protectionReference"),
    lumpSumPercentage = None,
    lumpSumAmount = None,
    enhancementFactor = None
  )

  val amendProtectionModel = AmendProtectionModel(
    psaCheckReference = "psaCheckReference",
    identifier = 10101,
    sequenceNumber = 20202,
    protectionType = AmendableProtectionType.IndividualProtection2014,
    status = AmendProtectionRequestStatus.Open,
    pensionDebitTotalAmount = Some(100_000),
    certificateDate = Some(DateModel.of(2025, 12, 8)),
    certificateTime = Some(TimeModel.of(12, 57, 10)),
    protectionReference = Some("protectionReference"),
    protectedAmount = Some(1_500_000),
    original = AmendProtectionFields(
      postADayBenefitCrystallisationEventAmount = Some(450_000),
      preADayPensionInPaymentAmount = Some(450_000),
      uncrystallisedRightsAmount = 450_000,
      nonUKRightsAmount = Some(450_000),
      pensionDebit = None
    ),
    updated = AmendProtectionFields(
      postADayBenefitCrystallisationEventAmount = Some(450_000),
      preADayPensionInPaymentAmount = Some(450_000),
      uncrystallisedRightsAmount = 450_000,
      nonUKRightsAmount = Some(450_000),
      pensionDebit = None
    )
  )

  val pensionDebitModel = PensionDebitModel(DateModel.of(2025, 12, 25), 200)

  "tryFromProtection" should {
    val protectionTypes = Seq[(ProtectionType, AmendableProtectionType)](
      ProtectionType.IndividualProtection2014    -> AmendableProtectionType.IndividualProtection2014,
      ProtectionType.IndividualProtection2014LTA -> AmendableProtectionType.IndividualProtection2014LTA,
      ProtectionType.IndividualProtection2016    -> AmendableProtectionType.IndividualProtection2016,
      ProtectionType.IndividualProtection2016LTA -> AmendableProtectionType.IndividualProtection2016LTA
    )

    val statuses = Seq[(ProtectionStatus, AmendProtectionRequestStatus)](
      ProtectionStatus.Open    -> AmendProtectionRequestStatus.Open,
      ProtectionStatus.Dormant -> AmendProtectionRequestStatus.Dormant
    )

    val amendableCases = for {
      protectionType <- protectionTypes
      status         <- statuses
    } yield (protectionType, status)

    "return Some containing correct model" when
      amendableCases.foreach { case ((modelProtectionType, amendProtectionType), (modelStatus, amendStatus)) =>
        s"protectionType is '$modelProtectionType' and status is '$modelStatus'" in {
          val protection = protectionModel.copy(
            protectionType = modelProtectionType,
            status = modelStatus
          )

          val amendProtection = amendProtectionModel.copy(
            protectionType = amendProtectionType,
            status = amendStatus
          )

          AmendProtectionModel.tryFromProtection(protection) shouldBe Some(amendProtection)
        }
      }

    "return None" when {
      val cases = for {
        protectionType <- ProtectionType.values
        status         <- ProtectionStatus.values
      } yield (protectionType, status)

      val excluded = amendableCases.map(tuple => (tuple._1._1, tuple._2._1))

      cases.diff(excluded).foreach { case (protectionType, status) =>
        s"protectionType is '$protectionType' and status is '$status'" in {
          val protection = protectionModel.copy(
            protectionType = protectionType,
            status = status
          )

          AmendProtectionModel.tryFromProtection(protection) shouldBe None
        }
      }
    }

  }

  "hasChanges" should {
    "return true" when {
      "the preADayPensionInPaymentAmount is updated to another value" in {
        amendProtectionModel.withPreADayPensionInPaymentAmount(Some(450_001)).hasChanges shouldBe true
      }
      "the preADayPensionInPaymentAmount is removed" in {
        amendProtectionModel.withPreADayPensionInPaymentAmount(None).hasChanges shouldBe true
      }
      "the postADayBenefitCrystallisationEventsAmount is updated to another value" in {
        amendProtectionModel.withPostADayBenefitCrystallisationEventAmount(Some(450_001)).hasChanges shouldBe true
      }
      "the postADayBenefitCrystallisationEventsAmount is removed" in {
        amendProtectionModel.withPostADayBenefitCrystallisationEventAmount(None).hasChanges shouldBe true
      }
      "the nonUKRightsAmount is updated to another value" in {
        amendProtectionModel.withNonUKRightsAmount(Some(450_001)).hasChanges shouldBe true
      }
      "the nonUKRightsAmount is removed" in {
        amendProtectionModel.withNonUKRightsAmount(None).hasChanges shouldBe true
      }
      "the uncrystallisedRightsAmount is updated to another value" in
        amendProtectionModel.withUncrystallisedRightsAmount(450_001)
      "a pension sharing order is added" in {
        amendProtectionModel.withPensionDebit(Some(pensionDebitModel)).hasChanges shouldBe true
      }
    }

    "return false" when {
      "there are no changes" in {
        amendProtectionModel.hasChanges shouldBe false
      }

      "the preADayPensionInPaymentAmount is updated to its current value" in {
        val updated = amendProtectionModel.withPreADayPensionInPaymentAmount(Some(450_000))

        updated.hasChanges shouldBe false
        updated shouldBe amendProtectionModel
      }

      "the preADayPensionInPaymentAmount is updated to another value, then restored to its original value" in {
        val updated =
          amendProtectionModel
            .withPreADayPensionInPaymentAmount(Some(450_001))
            .withPreADayPensionInPaymentAmount(Some(450_000))

        updated.hasChanges shouldBe false
        updated shouldBe amendProtectionModel
      }

      "the preADayPensionInPaymentAmount is removed, then restored to its original value" in {
        val updated =
          amendProtectionModel.withPreADayPensionInPaymentAmount(None).withPreADayPensionInPaymentAmount(Some(450_000))

        updated.hasChanges shouldBe false
        updated shouldBe amendProtectionModel
      }

      "the postADayBenefitCrystallisationEventAmount is updated to its current value" in {
        val updated = amendProtectionModel.withPostADayBenefitCrystallisationEventAmount(Some(450_000))

        updated.hasChanges shouldBe false
        updated shouldBe amendProtectionModel
      }

      "the postADayBenefitCrystallisationEventAmount is updated to another value, then restored to its original value" in {
        val updated = amendProtectionModel
          .withPostADayBenefitCrystallisationEventAmount(Some(450_001))
          .withPostADayBenefitCrystallisationEventAmount(Some(450_000))

        updated.hasChanges shouldBe false
        updated shouldBe amendProtectionModel
      }

      "the postADayBenefitCrystallisationEventAmount is removed, then restored to its original value" in {
        val updated = amendProtectionModel
          .withPostADayBenefitCrystallisationEventAmount(None)
          .withPostADayBenefitCrystallisationEventAmount(Some(450_000))

        updated.hasChanges shouldBe false
        updated shouldBe amendProtectionModel
      }

      "the nonUKRightsAmount is updated to its current value" in {
        val updated = amendProtectionModel.withNonUKRightsAmount(Some(450_000))

        updated.hasChanges shouldBe false
        updated shouldBe amendProtectionModel
      }

      "the nonUKRightsAmount is updated to another value, then restored to its original value" in {
        val updated = amendProtectionModel.withNonUKRightsAmount(Some(450_001)).withNonUKRightsAmount(Some(450_000))

        updated.hasChanges shouldBe false
        updated shouldBe amendProtectionModel
      }

      "the nonUKRightsAmount is removed, then restored to its original value" in {
        val updated = amendProtectionModel.withNonUKRightsAmount(None).withNonUKRightsAmount(Some(450_000))

        updated.hasChanges shouldBe false
        updated shouldBe amendProtectionModel
      }

      "the uncrystallisedRightsAmount is updated to its current value" in {
        val updated = amendProtectionModel.withUncrystallisedRightsAmount(450_000)

        updated.hasChanges shouldBe false
        updated shouldBe amendProtectionModel
      }

      "the uncrystallisedRightsAmount is updated to another value, then restored to its original value" in {
        val updated =
          amendProtectionModel.withUncrystallisedRightsAmount(450_001).withUncrystallisedRightsAmount(450_000)

        updated.hasChanges shouldBe false
        updated shouldBe amendProtectionModel
      }

      "the (non-existent) pension sharing order is removed" in {
        val updated = amendProtectionModel.withPensionDebit(None)

        updated.hasChanges shouldBe false
        updated shouldBe amendProtectionModel
      }

      "a pension sharing order is added, then removed" in {
        val updated = amendProtectionModel.withPensionDebit(Some(pensionDebitModel)).withPensionDebit(None)

        updated.hasChanges shouldBe false
        updated shouldBe amendProtectionModel
      }

    }
  }

  "updatedRelevantAmount" should {
    "calculate correct amount" in {
      amendProtectionModel.updatedRelevantAmount shouldBe 1_700_000
    }

    "truncate decimal components before taking sum" in {
      amendProtectionModel
        .withPreADayPensionInPaymentAmount(Some(450_000.99))
        .withPostADayBenefitCrystallisationEventAmount(Some(450_000.99))
        .withNonUKRightsAmount(Some(450_000.99))
        .withUncrystallisedRightsAmount(450_000.99)
        .updatedRelevantAmount shouldBe 1_700_000
    }
  }

  "withPreADayPensionInPayment" should {
    "return correct AmendProtectionModel" when {
      "provided with None" in {
        amendProtectionModel.withPreADayPensionInPaymentAmount(None) shouldBe amendProtectionModel.copy(
          updated = amendProtectionModel.updated.copy(
            preADayPensionInPaymentAmount = None
          )
        )
      }

      "provided with Some containing new value" in {
        amendProtectionModel.withPreADayPensionInPaymentAmount(Some(450_001)) shouldBe amendProtectionModel.copy(
          updated = amendProtectionModel.updated.copy(
            preADayPensionInPaymentAmount = Some(450_001)
          )
        )
      }

      "provided with Some containing existing value" in {
        amendProtectionModel.withPreADayPensionInPaymentAmount(Some(450_000)) shouldBe amendProtectionModel
      }
    }
  }

  "withPostADayBenefitCrystallisationEvents" should {
    "return correct AmendProtectionModel" when {
      "provided with None" in {
        amendProtectionModel.withPostADayBenefitCrystallisationEventAmount(None) shouldBe amendProtectionModel.copy(
          updated = amendProtectionModel.updated.copy(
            postADayBenefitCrystallisationEventAmount = None
          )
        )
      }

      "provided with Some containing new value" in {
        amendProtectionModel.withPostADayBenefitCrystallisationEventAmount(Some(450_001)) shouldBe amendProtectionModel
          .copy(
            updated = amendProtectionModel.updated.copy(
              postADayBenefitCrystallisationEventAmount = Some(450_001)
            )
          )
      }

      "provided with Some containing existing value" in {
        amendProtectionModel.withPostADayBenefitCrystallisationEventAmount(Some(450_000)) shouldBe amendProtectionModel
      }
    }
  }

  "withNonUKRights" should {
    "return correct AmendProtectionModel" when {
      "provided with None" in {
        amendProtectionModel.withNonUKRightsAmount(None) shouldBe amendProtectionModel.copy(
          updated = amendProtectionModel.updated.copy(
            nonUKRightsAmount = None
          )
        )
      }

      "provided with Some containing new value" in {
        amendProtectionModel.withNonUKRightsAmount(Some(450_001)) shouldBe amendProtectionModel.copy(
          updated = amendProtectionModel.updated.copy(
            nonUKRightsAmount = Some(450_001)
          )
        )
      }

      "provided with Some containing existing value" in {
        amendProtectionModel.withNonUKRightsAmount(Some(450_000)) shouldBe amendProtectionModel
      }
    }
  }

  "withUncrystallisedRights" should {
    "return correct AmendProtectionModel" when {
      "provided with new value" in {
        amendProtectionModel.withUncrystallisedRightsAmount(450_001) shouldBe amendProtectionModel.copy(
          updated = amendProtectionModel.updated.copy(
            uncrystallisedRightsAmount = 450_001
          )
        )
      }

      "provided with its current value" in {
        amendProtectionModel.withUncrystallisedRightsAmount(450_000) shouldBe amendProtectionModel
      }
    }
  }

  "withPensionDebit" should {
    "return correct AmendProtectionModel" when {
      "provided with Some containing new value" in {
        amendProtectionModel.withPensionDebit(Some(pensionDebitModel)) shouldBe amendProtectionModel.copy(
          updated = amendProtectionModel.updated.copy(
            pensionDebit = Some(pensionDebitModel)
          )
        )
      }

      "provided with None" in {
        amendProtectionModel.withPensionDebit(None) shouldBe amendProtectionModel
      }
    }
  }

}
