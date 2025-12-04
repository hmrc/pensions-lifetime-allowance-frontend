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

import generators.ModelGenerators
import models.pla.response.ProtectionStatus.{Dormant, Open}
import models.pla.response.{ProtectionStatus, ProtectionType}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ProtectionModelSpec extends AnyWordSpec with Matchers with ModelGenerators {

  val protectionModel = ProtectionModel(
    psaCheckReference = "psaCheckRef",
    identifier = 10,
    sequence = 1,
    protectionType = ProtectionType.IndividualProtection2014,
    status = Open,
    certificateDate = None,
    certificateTime = None
  )

  "ProtectionModel on isAmendable" should {

    val allAmendableCombinations = for {
      status <- Seq(Open, Dormant)
      protectionType <- Seq(
        ProtectionType.IndividualProtection2014,
        ProtectionType.IndividualProtection2014LTA,
        ProtectionType.IndividualProtection2016,
        ProtectionType.IndividualProtection2016LTA
      )
    } yield (status, protectionType)

    "return true" when
      allAmendableCombinations.foreach { case (status, protectionType) =>
        s"ProtectionModel contains status: '$status' and protectionType: '$protectionType''" in {
          protectionModel
            .copy(
              protectionType = protectionType,
              status = status
            )
            .isAmendable shouldBe true
        }
      }

    "return false" when {

      val allCombinations = for {
        status         <- ProtectionStatus.values
        protectionType <- ProtectionType.values
      } yield (status, protectionType)

      allCombinations.diff(allAmendableCombinations).foreach { case (status, protectionType) =>
        s"ProtectionModel contains status: $status and protectionType: $protectionType" in {
          protectionModel
            .copy(
              protectionType = protectionType,
              status = status
            )
            .isAmendable shouldBe false
        }
      }
    }

  }

  "ProtectionModel on isFixedProtection2016" should {

    val fixedProtectionTypes =
      Seq(ProtectionType.FixedProtection2016, ProtectionType.FixedProtection2016LTA)

    "return true" when
      fixedProtectionTypes.foreach { protectionType =>
        s"ProtectionModel contains protectionType: '$protectionType'" in {
          protectionModel.copy(protectionType = protectionType).isFixedProtection2016 shouldBe true
        }
      }

    "return false" when
      ProtectionType.values.diff(fixedProtectionTypes).foreach { protectionType =>
        s"ProtectionModel contains protectionType: '$protectionType'" in {
          protectionModel.copy(protectionType = protectionType).isFixedProtection2016 shouldBe false
        }
      }
  }

  "ProtectionModel on apply" when {

    "given a psaCheckReference and ProtectionRecord" must {

      "construct an instance of ProtectionModel with the correct fields" in
        forAll(readProtectionsResponseGen) { readProtectionsResponse =>
          val protectionRecord = readProtectionsResponse.protectionRecordsList.get.head.protectionRecord
          val result = ProtectionModel(
            readProtectionsResponse.pensionSchemeAdministratorCheckReference,
            protectionRecord
          )

          result.psaCheckReference shouldBe Some(readProtectionsResponse.pensionSchemeAdministratorCheckReference)
          result.identifier shouldBe Some(protectionRecord.identifier)
          result.certificateDate shouldBe Some(
            s"${protectionRecord.certificateDate}T${protectionRecord.certificateTime}"
          )
          result.sequence shouldBe Some(protectionRecord.sequenceNumber)
          result.protectionType shouldBe Some(protectionRecord.`type`)
          result.status shouldBe Some(protectionRecord.status)
          result.protectedAmount shouldBe protectionRecord.protectedAmount.map(_.toDouble)
          result.relevantAmount shouldBe protectionRecord.relevantAmount.map(_.toDouble)
          result.postADayBenefitCrystallisationEvents shouldBe protectionRecord.postADayBenefitCrystallisationEventAmount
            .map(_.toDouble)
          result.preADayPensionInPayment shouldBe protectionRecord.preADayPensionInPaymentAmount.map(_.toDouble)
          result.uncrystallisedRights shouldBe protectionRecord.uncrystallisedRightsAmount.map(_.toDouble)
          result.nonUKRights shouldBe protectionRecord.nonUKRightsAmount.map(_.toDouble)
          result.pensionDebitTotalAmount shouldBe protectionRecord.pensionDebitTotalAmount.map(_.toDouble)
          result.pensionDebit shouldBe None
          result.protectionReference shouldBe protectionRecord.protectionReference
        }
    }
  }

}
