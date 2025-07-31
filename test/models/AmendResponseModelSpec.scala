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

import models.pla.{AmendProtectionLifetimeAllowanceType, AmendProtectionResponseStatus}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import testdata.PlaV2TestData.{amendProtectionResponse, lifetimeAllowanceIdentifier, lifetimeAllowanceSequenceNumber, protectionReference}

class AmendResponseModelSpec extends AnyWordSpec with Matchers {

  "AmendResponseModel on from" should {

    "return correct AmendResponseModel" in {
      val expectedResult = AmendResponseModel(ProtectionModel(
        psaCheckReference = None,
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
      ))

      AmendResponseModel.from(amendProtectionResponse) shouldBe expectedResult
    }
  }

}
