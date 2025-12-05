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

import models.display.{AmendDisplayModel, AmendDisplayRowModel, AmendDisplaySectionModel}
import models.pla.AmendableProtectionType
import models.pla.request.AmendProtectionRequestStatus

class AmendDisplayModelConstructorSpec extends DisplayConstructorsTestData {

  "createAmendDisplayModel" should {
    "correctly transform an AmendProtectionModel into an AmendDisplayModel without Psos" in {

      AmendDisplayModelConstructor.createAmendDisplayModel(tstNoPsoAmendProtectionModel) shouldBe AmendDisplayModel(
        protectionType = AmendableProtectionType.IndividualProtection2016,
        amended = false,
        pensionContributionSections = tstPensionContributionNoPsoDisplaySections,
        psoAdded = false,
        psoSections = tstNoPsoDisplaySections,
        totalAmount = "£1,100,000"
      )
    }

    "correctly transform an AmendProtectionModel into an AmendDisplayModel with PSOs" in {
      AmendDisplayModelConstructor.createAmendDisplayModel(
        tstWithExistingPsoAmendProtectionModel
      ) shouldBe AmendDisplayModel(
        protectionType = AmendableProtectionType.IndividualProtection2016,
        amended = false,
        pensionContributionSections = tstPensionContributionPsoDisplaySections,
        psoAdded = false,
        psoSections = tstNoPsoDisplaySections,
        totalAmount = "£1,099,000"
      )
    }

    "return no current PSO's when not supplied with an amountOption" in {
      AmendDisplayModelConstructor.createAmendDisplayModel(
        tstWithExistingPsoAmendProtectionModel
      ) shouldBe AmendDisplayModel(
        protectionType = AmendableProtectionType.IndividualProtection2016,
        amended = false,
        pensionContributionSections = tstPensionContributionPsoDisplaySections,
        psoAdded = false,
        psoSections = tstNoPsoDisplaySections,
        totalAmount = "£1,099,000"
      )
    }

    "return no current PSO's when amountOption is 0" in {
      val amendProtectionModel = tstNoPsoAmendProtectionModel.copy(
        pensionDebitTotalAmount = Some(0.0)
      )

      AmendDisplayModelConstructor.createAmendDisplayModel(amendProtectionModel) shouldBe AmendDisplayModel(
        protectionType = AmendableProtectionType.IndividualProtection2016,
        amended = false,
        pensionContributionSections = tstPensionContributionNoPsoDisplaySections,
        psoAdded = false,
        psoSections = tstNoPsoDisplaySections,
        totalAmount = "£1,100,000"
      )
    }

    "correctly produce a display section for any current PSO's" in {
      val tstPsoAddedSection = Seq(
        AmendDisplaySectionModel(
          "pensionDebits",
          Seq(
            AmendDisplayRowModel(
              "CurrentPsos-psoDetails",
              changeLinkCall = Some(
                controllers.routes.AmendsPensionSharingOrderController.amendPsoDetails(
                  AmendableProtectionType.IndividualProtection2016,
                  AmendProtectionRequestStatus.Open
                )
              ),
              removeLinkCall = Some(
                controllers.routes.AmendsRemovePensionSharingOrderController.removePso(
                  AmendableProtectionType.IndividualProtection2016,
                  AmendProtectionRequestStatus.Open
                )
              ),
              "£1,000",
              "2 March 2017"
            )
          )
        )
      )

      AmendDisplayModelConstructor.createAmendDisplayModel(
        tstWithPsoAmendProtectionModel
      ) shouldBe AmendDisplayModel(
        protectionType = AmendableProtectionType.IndividualProtection2016,
        amended = true,
        pensionContributionSections = tstPensionContributionNoPsoDisplaySections,
        psoAdded = true,
        psoSections = tstPsoAddedSection,
        totalAmount = "£1,100,000"
      )

    }
  }

}
