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

import common.Exceptions.OptionNotDefinedException

class AmendOutcomeDisplayModelNoNotificationIdConstructorSpec extends DisplayConstructorsTestData {

  "createAmendOutcomeDisplayModelNoNotificationId" should {
    import testdata.AmendProtectionDisplayModelTestData._

    "correctly transform AmendResponseModel into amendOutcomeDisplayModelNoNotificationId" in {
      AmendOutcomeDisplayModelNoNotificationIdConstructor.createAmendOutcomeDisplayModelNoNotificationId(
        amendResponseModelNoNotificationIdIndividualProtection2014,
        Some(personalDetailsModel),
        nino
      ) shouldBe amendOutcomeDisplayModelNoNotificationIdIndividualProtection2014
    }

    "throw exception" when {
      "protectedAmount is missing" in {
        val exception =
          the[OptionNotDefinedException] thrownBy AmendOutcomeDisplayModelNoNotificationIdConstructor
            .createAmendOutcomeDisplayModelNoNotificationId(
              amendResponseModelNoNotificationIdIndividualProtection2016.copy(
                protectedAmount = None
              ),
              Some(personalDetailsModel),
              nino
            )

        exception.functionName shouldBe "createAmendOutcomeDisplayModelNoNotificationId"
        exception.optionName shouldBe "protectedAmount"
      }
    }
  }

}
