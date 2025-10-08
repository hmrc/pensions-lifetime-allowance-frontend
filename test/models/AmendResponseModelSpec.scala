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

import models.pla.response.AmendProtectionResponse
import models.pla.{AmendProtectionLifetimeAllowanceType, AmendProtectionResponseStatus}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import testdata.PlaV2TestData._

class AmendResponseModelSpec extends AnyWordSpec with Matchers {

  "AmendResponseModel on from" should {

    "return correct AmendResponseModel" in {
      val expectedResult = AmendResponseModel(
        ProtectionModel(
          psaCheckReference = Some("testPSARef"),
          protectionID = Some(lifetimeAllowanceIdentifier),
          version = Some(lifetimeAllowanceSequenceNumber + 1),
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
      )

      AmendResponseModel.from(amendProtectionResponse, Some("testPSARef")) shouldBe expectedResult
    }

    "handle response with missing leading zeros in certificateTime" in {
      val psaCheckReference = "PSA000001"

      val amendProtectionResponse = AmendProtectionResponse(
        lifetimeAllowanceIdentifier = 1,
        lifetimeAllowanceSequenceNumber = 1,
        lifetimeAllowanceType = AmendProtectionLifetimeAllowanceType.IndividualProtection2014,
        certificateDate = Some("2025-10-08"),
        certificateTime = Some("93010"),
        status = AmendProtectionResponseStatus.Open,
        relevantAmount = 0,
        preADayPensionInPaymentAmount = 0,
        postADayBenefitCrystallisationEventAmount = 0,
        uncrystallisedRightsAmount = 0,
        nonUKRightsAmount = 0
      )

      val amendResponseModel = AmendResponseModel(
        ProtectionModel(
          psaCheckReference = Some(psaCheckReference),
          protectionID = Some(1),
          version = Some(1),
          protectionType = Some(AmendProtectionLifetimeAllowanceType.IndividualProtection2014.toString),
          status = Some(AmendProtectionResponseStatus.Open.toString),
          certificateDate = Some("2025-10-08T093010"),
          relevantAmount = Some(0),
          nonUKRights = Some(0),
          uncrystallisedRights = Some(0),
          postADayBenefitCrystallisationEvents = Some(0),
          preADayPensionInPayment = Some(0)
        )
      )

      AmendResponseModel.from(amendProtectionResponse, Some(psaCheckReference)) shouldBe amendResponseModel
    }
  }

}
