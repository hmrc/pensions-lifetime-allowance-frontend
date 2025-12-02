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
import models.pla.AmendableProtectionType._
import models.pla.response.ProtectionType
import models.pla.AmendableProtectionType
import models.pla.request.AmendProtectionRequestStatus
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ProtectionModelSpec extends AnyWordSpec with Matchers with ModelGenerators {

  "ProtectionModel on isAmendable" should {

    "return true" when {

      val amendableStatuses = AmendProtectionRequestStatus.values.map(_.toString)
      val amendableProtectionTypes = Seq(
        "ip2014",
        "ip2016"
      ) ++ AmendableProtectionType.values.map(_.toString)

      val allAmendableCombinations = for {
        status         <- amendableStatuses
        protectionType <- amendableProtectionTypes
      } yield (status, protectionType)

      allAmendableCombinations.foreach { case (status, protectionType) =>
        s"ProtectionModel contains status: '$status' and protectionType: '$protectionType''" in {
          val protectionModel = ProtectionModel(
            psaCheckReference = None,
            identifier = None,
            status = Some(status),
            protectionType = Some(protectionType)
          )

          protectionModel.isAmendable shouldBe true
        }
      }
    }

    "return false" when {

      val testScenarios = Seq(
        Some("open")    -> Some("other"),
        Some("dormant") -> Some("other"),
        Some("open")    -> None,
        Some("dormant") -> None,
        Some("OPEN")    -> Some("other"),
        Some("DORMANT") -> Some("other"),
        Some("OPEN")    -> None,
        Some("DORMANT") -> None,
        Some("other")   -> Some("ip2014"),
        Some("other")   -> Some("ip2016"),
        None            -> Some("ip2014"),
        None            -> Some("ip2016"),
        Some("other")   -> Some(IndividualProtection2014.toString),
        Some("other")   -> Some(IndividualProtection2016.toString),
        Some("other")   -> Some(IndividualProtection2014LTA.toString),
        Some("other")   -> Some(IndividualProtection2016LTA.toString),
        None            -> Some(IndividualProtection2014.toString),
        None            -> Some(IndividualProtection2016.toString),
        None            -> Some(IndividualProtection2014LTA.toString),
        None            -> Some(IndividualProtection2016LTA.toString)
      )

      testScenarios.foreach { case (status, protectionType) =>
        s"ProtectionModel contains status: $status and protectionType: $protectionType" in {
          val protectionModel = ProtectionModel(
            psaCheckReference = None,
            identifier = None,
            status = status,
            protectionType = protectionType
          )

          protectionModel.isAmendable shouldBe false
        }
      }
    }

  }

  "ProtectionModel on isFixedProtection2016" should {

    "return true" when {

      val fixedProtectionTypes =
        Seq(ProtectionType.FixedProtection2016.toString, ProtectionType.FixedProtection2016LTA.toString, "FP2016")

      fixedProtectionTypes.foreach { protectionType =>
        s"ProtectionModel contains protectionType: '$protectionType'" in {
          val protectionModel = ProtectionModel(
            psaCheckReference = None,
            identifier = None,
            status = Some("open"),
            protectionType = Some(protectionType)
          )

          protectionModel.isFixedProtection2016 shouldBe true
        }
      }
    }

    "return false" when {

      val testScenarios = Seq(
        "IP2014",
        "IP2016",
        IndividualProtection2014.toString,
        IndividualProtection2016.toString,
        IndividualProtection2014LTA.toString,
        IndividualProtection2016LTA.toString
      )

      testScenarios.foreach { protectionType =>
        s"ProtectionModel contains protectionType: '$protectionType'" in {
          val protectionModel = ProtectionModel(
            psaCheckReference = None,
            identifier = None,
            status = Some("open"),
            protectionType = Some(protectionType)
          )

          protectionModel.isFixedProtection2016 shouldBe false
        }
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
          result.protectionType shouldBe Some(protectionRecord.`type`.toString)
          result.status shouldBe Some(protectionRecord.status.toString)
          result.protectedAmount shouldBe protectionRecord.protectedAmount.map(_.toDouble)
          result.relevantAmount shouldBe protectionRecord.relevantAmount.map(_.toDouble)
          result.postADayBenefitCrystallisationEvents shouldBe protectionRecord.postADayBenefitCrystallisationEventAmount
            .map(_.toDouble)
          result.preADayPensionInPayment shouldBe protectionRecord.preADayPensionInPaymentAmount.map(_.toDouble)
          result.uncrystallisedRights shouldBe protectionRecord.uncrystallisedRightsAmount.map(_.toDouble)
          result.nonUKRights shouldBe protectionRecord.nonUKRightsAmount.map(_.toDouble)
          result.pensionDebitAmount shouldBe protectionRecord.pensionDebitAmount.map(_.toDouble)
          result.pensionDebitEnteredAmount shouldBe protectionRecord.pensionDebitEnteredAmount.map(_.toDouble)
          result.pensionDebitStartDate shouldBe protectionRecord.pensionDebitStartDate
          result.pensionDebitTotalAmount shouldBe protectionRecord.pensionDebitTotalAmount.map(_.toDouble)
          result.pensionDebits shouldBe None
          result.notificationId shouldBe None
          result.protectionReference shouldBe protectionRecord.protectionReference
          result.withdrawnDate shouldBe None
        }
    }
  }

}
