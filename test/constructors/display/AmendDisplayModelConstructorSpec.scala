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

package constructors.display

import common.Strings
import models.amendModels.AmendProtectionModel
import models.display.{AmendDisplayModel, AmendDisplayRowModel, AmendDisplaySectionModel}
import models.pla.response.ProtectionStatus.Open
import models.pla.response.ProtectionType.IndividualProtection2016
import models.{PensionDebitModel, ProtectionModel}

class AmendDisplayModelConstructorSpec extends DisplayConstructorsTestData {

  "createAmendDisplayModel" should {
    "correctly transform an AmendProtectionModel into an AmendDisplayModel without Psos" in {

      AmendDisplayModelConstructor.createAmendDisplayModel(tstNoPsoAmendProtectionModel) shouldBe AmendDisplayModel(
        protectionType = IndividualProtection2016.toString,
        amended = false,
        pensionContributionSections = tstPensionContributionNoPsoDisplaySections,
        psoAdded = false,
        psoSections = tstNoPsoDisplaySections,
        totalAmount = "£1,100,000.34"
      )
    }

    "correctly transform an AmendProtectionModel into an AmendDisplayModel with PSOs" in {
      AmendDisplayModelConstructor.createAmendDisplayModel(tstWithPsoAmendProtectionModel) shouldBe AmendDisplayModel(
        protectionType = IndividualProtection2016.toString,
        amended = false,
        pensionContributionSections = tstPensionContributionPsoDisplaySections,
        psoAdded = false,
        psoSections = tstNoPsoDisplaySections,
        totalAmount = "£1,100,000.34"
      )
    }

    "return no current PSO's when not supplied with an amountOption" in {
      AmendDisplayModelConstructor.createAmendDisplayModel(tstWithPsoAmendProtectionModel) shouldBe AmendDisplayModel(
        protectionType = IndividualProtection2016.toString,
        amended = false,
        pensionContributionSections = tstPensionContributionPsoDisplaySections,
        psoAdded = false,
        psoSections = tstNoPsoDisplaySections,
        totalAmount = "£1,100,000.34"
      )
    }

    "return no current PSO's when amountOption is 0" in {
      val tstNoPsoAmountProtection = ProtectionModel(
        psaCheckReference = Some("psaRef"),
        protectionID = Some(100001),
        protectionType = Some(IndividualProtection2016.toString),
        status = Some(Open.toString),
        protectedAmount = Some(1100000.34),
        relevantAmount = Some(1100000.34),
        preADayPensionInPayment = None,
        postADayBenefitCrystallisationEvents = None,
        pensionDebitTotalAmount = Some(0.00),
        nonUKRights = Some(100000.0),
        uncrystallisedRights = Some(1000000.34)
      )

      val amendModel = AmendProtectionModel(tstNoPsoAmountProtection, tstNoPsoAmountProtection)

      AmendDisplayModelConstructor.createAmendDisplayModel(amendModel) shouldBe AmendDisplayModel(
        protectionType = IndividualProtection2016.toString,
        amended = false,
        pensionContributionSections = tstPensionContributionNoPsoDisplaySections,
        psoAdded = false,
        psoSections = tstNoPsoDisplaySections,
        totalAmount = "£1,100,000.34"
      )
    }

    "correctly produce a display section for any current PSO's" in {
      val tstNewPsoAmountProtection = ProtectionModel(
        psaCheckReference = Some("psaRef"),
        protectionID = Some(100001),
        protectionType = Some(IndividualProtection2016.toString),
        status = Some(Open.toString),
        protectedAmount = Some(1100000.34),
        relevantAmount = Some(1100000.34),
        preADayPensionInPayment = None,
        postADayBenefitCrystallisationEvents = None,
        pensionDebits = Some(List(PensionDebitModel("2017-03-02T15:14:00", 1000.0))),
        pensionDebitTotalAmount = Some(0.0),
        nonUKRights = Some(100000.0),
        uncrystallisedRights = Some(1000000.34)
      )

      val amendModel = AmendProtectionModel(tstNewPsoAmountProtection, tstNewPsoAmountProtection)

      val tstPsoAddedSection = Seq(
        AmendDisplaySectionModel(
          "pensionDebits",
          Seq(
            AmendDisplayRowModel(
              "CurrentPsos-psoDetails",
              changeLinkCall = Some(
                controllers.routes.AmendsPensionSharingOrderController.amendPsoDetails(
                  Strings.ProtectionTypeUrl.IndividualProtection2016,
                  Strings.StatusUrl.Open
                )
              ),
              removeLinkCall = Some(
                controllers.routes.AmendsRemovePensionSharingOrderController.removePso(
                  Strings.ProtectionTypeUrl.IndividualProtection2016,
                  Strings.StatusUrl.Open
                )
              ),
              "£1,000",
              "2 March 2017"
            )
          )
        )
      )

      AmendDisplayModelConstructor.createAmendDisplayModel(amendModel) shouldBe AmendDisplayModel(
        protectionType = IndividualProtection2016.toString,
        amended = false,
        pensionContributionSections = tstPensionContributionNoPsoDisplaySections,
        psoAdded = true,
        psoSections = tstPsoAddedSection,
        totalAmount = "£1,100,000.34"
      )

    }

    "should not create a display section for current PSO's if there is more than one pension debit in the model" in {
      val tstNewPsoAmountProtection = ProtectionModel(
        psaCheckReference = Some("psaRef"),
        protectionID = Some(100001),
        protectionType = Some(IndividualProtection2016.toString),
        status = Some(Open.toString),
        protectedAmount = Some(1000000.34),
        relevantAmount = Some(1000000.34),
        preADayPensionInPayment = None,
        postADayBenefitCrystallisationEvents = None,
        pensionDebits = Some(
          List(PensionDebitModel("2017-03-02T15:14:00", 1000.0), PensionDebitModel("2017-03-02T15:12:00", 2000.0))
        ),
        pensionDebitTotalAmount = Some(0.0),
        nonUKRights = Some(100000.0),
        uncrystallisedRights = Some(1000000.34)
      )

      val amendModel = AmendProtectionModel(tstNewPsoAmountProtection, tstNewPsoAmountProtection)

      AmendDisplayModelConstructor.createAmendDisplayModel(amendModel) shouldBe AmendDisplayModel(
        protectionType = IndividualProtection2016.toString,
        amended = false,
        pensionContributionSections = tstPensionContributionNoPsoDisplaySections,
        psoAdded = true,
        psoSections = tstNoPsoDisplaySections,
        totalAmount = "£1,000,000.34"
      )

    }
  }

  "modelsDiffer" should {

    "return false for the same model" in {
      val tstModel = ProtectionModel(psaCheckReference = Some("psaRef"), protectionID = Some(10000))
      AmendDisplayModelConstructor.modelsDiffer(tstModel, tstModel) shouldBe false
    }

    "return false for two models with the same properties" in {
      val tstModel1 = ProtectionModel(
        psaCheckReference = Some("psaRef"),
        protectionID = Some(10000),
        preADayPensionInPayment = Some(23412.87)
      )
      val tstModel2 = ProtectionModel(
        psaCheckReference = Some("psaRef"),
        preADayPensionInPayment = Some(23412.87),
        protectionID = Some(10000)
      )
      AmendDisplayModelConstructor.modelsDiffer(tstModel1, tstModel2) shouldBe false
    }

    "return true for two models with different properties" in {
      val tstModel1 = ProtectionModel(
        psaCheckReference = Some("psaRef"),
        protectionID = Some(100001),
        preADayPensionInPayment = Some(23412.87)
      )
      val tstModel2 = ProtectionModel(
        psaCheckReference = Some("psaRef"),
        protectionID = Some(10000),
        preADayPensionInPayment = Some(23412.87)
      )
      AmendDisplayModelConstructor.modelsDiffer(tstModel1, tstModel2) shouldBe true
    }

    "return true for two models with different number of properties" in {
      val tstModel1 = ProtectionModel(
        psaCheckReference = Some("psaRef"),
        protectionID = Some(100001),
        preADayPensionInPayment = Some(23412.87),
        version = Some(4)
      )
      val tstModel2 = ProtectionModel(
        psaCheckReference = Some("psaRef"),
        protectionID = Some(10000),
        preADayPensionInPayment = Some(23412.87)
      )
      AmendDisplayModelConstructor.modelsDiffer(tstModel1, tstModel2) shouldBe true
    }

  }

}
