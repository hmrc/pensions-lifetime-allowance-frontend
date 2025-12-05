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

import models.NotificationId.NotificationId3
import models.pla.response.AmendProtectionResponseStatus
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import testdata.PlaConnectorTestData._

class AmendResponseModelSpec extends AnyWordSpec with Matchers {

  "AmendResponseModel on from" should {

    "return correct AmendResponseModel" in {
      val expectedResult = AmendResponseModel(
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
        notificationId = Some(NotificationId3),
        protectedAmount = Some(120_000),
        pensionDebitTotalAmount = Some(40_000)
      )

      AmendResponseModel.from(amendProtectionResponse, "testPSARef") shouldBe expectedResult
    }
  }

}
