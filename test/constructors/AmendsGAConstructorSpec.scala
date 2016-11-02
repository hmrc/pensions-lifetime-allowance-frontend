/*
 * Copyright 2016 HM Revenue & Customs
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

import models.{PensionDebitModel, ProtectionModel}
import models.amendModels.AmendsGAModel
import uk.gov.hmrc.play.test.{WithFakeApplication, UnitSpec}

class AmendsGAConstructorSpec extends UnitSpec with WithFakeApplication{

  val testProtectionModel1: ProtectionModel = ProtectionModel(
    psaCheckReference = Some("testPSARef"),
    uncrystallisedRights = Some(100000.00),
    nonUKRights = Some(2000.00),
    preADayPensionInPayment = Some(2000.00),
    postADayBenefitCrystallisationEvents = Some(2000.00),
    notificationId = Some(12),
    protectionID = Some(12345),
    protectionType = Some("IP2016"),
    status = Some("dormant"),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456"),
    pensionDebits = None)

  val testProtectionModel2: ProtectionModel = ProtectionModel(
    psaCheckReference = Some("testPSARef"),
    uncrystallisedRights = Some(250000.00),
    nonUKRights = Some(500.00),
    preADayPensionInPayment = Some(1000.00),
    postADayBenefitCrystallisationEvents = Some(1000.00),
    notificationId = Some(12),
    protectionID = Some(12345),
    protectionType = Some("IP2016"),
    status = Some("dormant"),
    certificateDate = Some("2016-05-21"),
    protectedAmount = Some(1000000),
    protectionReference = Some("PSA123456"),
    pensionDebits = Some(List(PensionDebitModel("2016-10-23", 1000.0))))

  val testProtectionModel3: ProtectionModel = ProtectionModel(
    psaCheckReference = Some("testPSARef"),
    uncrystallisedRights = Some(250000.00),
    nonUKRights = Some(0.00),
    preADayPensionInPayment = Some(0.00),
    postADayBenefitCrystallisationEvents = Some(0.00),
    notificationId = Some(12),
    protectionID = Some(12345),
    protectionType = Some("IP2016"),
    status = Some("dormant"),
    certificateDate = Some("2016-05-21"),
    protectedAmount = Some(1000000),
    protectionReference = Some("PSA123456"),
    pensionDebits = None)

  "Calling the identify changes method" when {

    "The original and updated protection models are the same" in {
      val original = testProtectionModel1
      val updated = testProtectionModel1

      AmendsGAConstructor.identifyAmendsChanges(updated,original) shouldBe AmendsGAModel(None,None,None,None,None)
    }

    "The original and updated protection models are different" when {

      "The values for Current Pensions, PTBefore, PTBetween and Overseas Pensions are updated" in {
        val original = testProtectionModel1
        val updated = testProtectionModel2

        AmendsGAConstructor.identifyAmendsChanges(updated, original) shouldBe AmendsGAModel(Some("UpdatedValue"), Some("UpdatedValue"), Some("UpdatedValue"), Some("UpdatedValue"), Some("addedPSO"))
      }

      "PTBefore, PTBetween and Overseas Pensions are amended from 'No' to 'Yes'" in {
        val original = testProtectionModel3
        val updated = testProtectionModel1

        AmendsGAConstructor.identifyAmendsChanges(updated, original) shouldBe AmendsGAModel(Some("UpdatedValue"), Some("ChangedToYes"), Some("ChangedToYes"), Some("ChangedToYes"), None)
      }

      "PTBefore, PTBetween and Overseas Pensions are amended from 'Yes' to 'No'" in {
        val original = testProtectionModel1
        val updated = testProtectionModel3

        AmendsGAConstructor.identifyAmendsChanges(updated, original) shouldBe AmendsGAModel(Some("UpdatedValue"), Some("ChangedToNo"), Some("ChangedToNo"), Some("ChangedToNo"), None)
      }
    }

  }

}
