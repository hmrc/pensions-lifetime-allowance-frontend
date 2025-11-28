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
import models.AmendResponseModel

class AmendOutcomeDisplayModelConstructorSpec extends DisplayConstructorsTestData {

  "createAmendResultDisplayModel" should {
    import testdata.AmendProtectionDisplayModelTestData._

    "correctly transform an AmendResponseModel into an AmendResultDisplayModel" when
      Seq(
        1  -> amendResponseModelNotification1  -> amendResultDisplayModelNotification1,
        2  -> amendResponseModelNotification2  -> amendResultDisplayModelNotification2,
        3  -> amendResponseModelNotification3  -> amendResultDisplayModelNotification3,
        4  -> amendResponseModelNotification4  -> amendResultDisplayModelNotification4,
        5  -> amendResponseModelNotification5  -> amendResultDisplayModelNotification5,
        6  -> amendResponseModelNotification6  -> amendResultDisplayModelNotification6,
        7  -> amendResponseModelNotification7  -> amendResultDisplayModelNotification7,
        8  -> amendResponseModelNotification8  -> amendResultDisplayModelNotification8,
        9  -> amendResponseModelNotification9  -> amendResultDisplayModelNotification9,
        10 -> amendResponseModelNotification10 -> amendResultDisplayModelNotification10,
        11 -> amendResponseModelNotification11 -> amendResultDisplayModelNotification11,
        12 -> amendResponseModelNotification12 -> amendResultDisplayModelNotification12,
        13 -> amendResponseModelNotification13 -> amendResultDisplayModelNotification13,
        14 -> amendResponseModelNotification14 -> amendResultDisplayModelNotification14
      ).foreach { case ((notificationId, amendResponseModel), amendResultDisplayModel) =>
        s"notification id is $notificationId" in {
          AmendOutcomeDisplayModelConstructor.createAmendOutcomeDisplayModel(
            amendResponseModel,
            Some(personalDetailsModel),
            nino
          ) shouldBe amendResultDisplayModel
        }
      }

    "throw exception" when {
      "notificationId is missing" in {
        val exception =
          the[OptionNotDefinedException] thrownBy AmendOutcomeDisplayModelConstructor.createAmendOutcomeDisplayModel(
            AmendResponseModel(
              amendResponseModelNotification1.protection.copy(
                notificationId = None
              )
            ),
            Some(personalDetailsModel),
            nino
          )

        exception.functionName shouldBe "createAmendResultDisplayModel"
        exception.optionName shouldBe "notificationId"
      }

      "protectedAmount is missing" in {
        val exception =
          the[OptionNotDefinedException] thrownBy AmendOutcomeDisplayModelConstructor.createAmendOutcomeDisplayModel(
            AmendResponseModel(
              amendResponseModelNotification1.protection.copy(
                protectedAmount = None
              )
            ),
            Some(personalDetailsModel),
            nino
          )

        exception.functionName shouldBe "createAmendResultDisplayModel"
        exception.optionName shouldBe "protectedAmount"
      }
    }
  }

}
