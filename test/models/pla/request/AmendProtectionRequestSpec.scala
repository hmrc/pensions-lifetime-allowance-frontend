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

import models.ProtectionModel
import models.pla.{AmendProtectionLifetimeAllowanceType, AmendProtectionResponseStatus}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import testdata.PlaConnectorTestData._

class AmendProtectionRequestSpec extends AnyWordSpec with Matchers {

  "AmendProtectionRequest on from" should {

    val inputProtectionModel = ProtectionModel(
      psaCheckReference = None,
      protectionID = Some(lifetimeAllowanceIdentifier),
      version = Some(lifetimeAllowanceSequenceNumber),
      protectionType = Some(AmendProtectionLifetimeAllowanceType.IndividualProtection2014.toString),
      certificateDate = Some("2025-07-15T174312"),
      status = Some(AmendProtectionResponseStatus.Dormant.toString),
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

    "return correct AmendProtectionRequest" when {

      "all mandatory fields are present" in {
        AmendProtectionRequest.from(inputProtectionModel) shouldBe amendProtectionRequest
      }

      "all mandatory fields are present and optional fields are empty" in {
        val input = inputProtectionModel.copy(
          certificateDate = None,
          protectionReference = None,
          pensionDebitAmount = None,
          pensionDebitEnteredAmount = None,
          notificationId = None,
          protectedAmount = None,
          pensionDebitStartDate = None,
          pensionDebitTotalAmount = None
        )
        val expectedOutput = amendProtectionRequest.copy(
          certificateDate = None,
          certificateTime = None,
          protectionReference = None,
          pensionDebitAmount = None,
          pensionDebitEnteredAmount = None,
          notificationIdentifier = None,
          protectedAmount = None,
          pensionDebitStartDate = None,
          pensionDebitTotalAmount = None
        )

        AmendProtectionRequest.from(input) shouldBe expectedOutput
      }

      "provided with NPS certificateTime" in {
        val input = inputProtectionModel.copy(certificateDate = Some("2025-07-15T17:43:12.00234020"))

        AmendProtectionRequest.from(input) shouldBe amendProtectionRequest
      }

      "provided with pension debit start date but not pension debit entered amount" in {
        val input = inputProtectionModel.copy(
          pensionDebitEnteredAmount = None
        )

        val expectedOutput = amendProtectionRequest.copy(
          pensionDebitStartDate = None,
          pensionDebitEnteredAmount = None
        )

        AmendProtectionRequest.from(input) shouldBe expectedOutput
      }

      "provided with pension debit entered amount but not pension debit start date" in {
        val input = inputProtectionModel.copy(
          pensionDebitStartDate = None
        )

        val expectedOutput = amendProtectionRequest.copy(
          pensionDebitStartDate = None,
          pensionDebitEnteredAmount = None
        )

        AmendProtectionRequest.from(input) shouldBe expectedOutput
      }
    }

    "throw IllegalArgumentException" when {

      "provided with ProtectionModel containing empty 'version' field" in {
        val input = inputProtectionModel.copy(version = None)

        val exc = the[IllegalArgumentException] thrownBy AmendProtectionRequest.from(input)

        exc.getMessage shouldBe "Cannot create AmendProtectionRequest, because 'version' field is empty in provided ProtectionModel"
      }

      "provided with ProtectionModel containing empty 'protectionType' field" in {
        val input = inputProtectionModel.copy(protectionType = None)

        val exc = the[IllegalArgumentException] thrownBy AmendProtectionRequest.from(input)

        exc.getMessage shouldBe "Cannot create AmendProtectionRequest, because 'protectionType' field is empty in provided ProtectionModel"
      }

      "provided with ProtectionModel containing empty 'status' field" in {
        val input = inputProtectionModel.copy(status = None)

        val exc = the[IllegalArgumentException] thrownBy AmendProtectionRequest.from(input)

        exc.getMessage shouldBe "Cannot create AmendProtectionRequest, because 'status' field is empty in provided ProtectionModel"
      }

      "provided with ProtectionModel containing empty 'relevantAmount' field" in {
        val input = inputProtectionModel.copy(relevantAmount = None)

        val exc = the[IllegalArgumentException] thrownBy AmendProtectionRequest.from(input)

        exc.getMessage shouldBe "Cannot create AmendProtectionRequest, because 'relevantAmount' field is empty in provided ProtectionModel"
      }

      "provided with ProtectionModel containing empty 'preADayPensionInPayment' field" in {
        val input = inputProtectionModel.copy(preADayPensionInPayment = None)

        val exc = the[IllegalArgumentException] thrownBy AmendProtectionRequest.from(input)

        exc.getMessage shouldBe "Cannot create AmendProtectionRequest, because 'preADayPensionInPayment' field is empty in provided ProtectionModel"
      }

      "provided with ProtectionModel containing empty 'postADayBenefitCrystallisationEvents' field" in {
        val input = inputProtectionModel.copy(postADayBenefitCrystallisationEvents = None)

        val exc = the[IllegalArgumentException] thrownBy AmendProtectionRequest.from(input)

        exc.getMessage shouldBe "Cannot create AmendProtectionRequest, because 'postADayBenefitCrystallisationEvents' field is empty in provided ProtectionModel"
      }

      "provided with ProtectionModel containing empty 'uncrystallisedRights' field" in {
        val input = inputProtectionModel.copy(uncrystallisedRights = None)

        val exc = the[IllegalArgumentException] thrownBy AmendProtectionRequest.from(input)

        exc.getMessage shouldBe "Cannot create AmendProtectionRequest, because 'uncrystallisedRights' field is empty in provided ProtectionModel"
      }

      "provided with ProtectionModel containing empty 'nonUKRights' field" in {
        val input = inputProtectionModel.copy(nonUKRights = None)

        val exc = the[IllegalArgumentException] thrownBy AmendProtectionRequest.from(input)

        exc.getMessage shouldBe "Cannot create AmendProtectionRequest, because 'nonUKRights' field is empty in provided ProtectionModel"
      }
    }
  }

}
