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
import models.NotificationId._

class AmendOutcomeDisplayModelConstructorSpec extends DisplayConstructorsTestData {

  "createAmendResultDisplayModel" should {
    import testdata.AmendProtectionDisplayModelTestData._

    "correctly transform an AmendResponseModel into an AmendResultDisplayModel" when
      Seq(
        NotificationId1  -> amendResponseModelNotification1  -> amendOutcomeDisplayModelNotification1,
        NotificationId2  -> amendResponseModelNotification2  -> amendOutcomeDisplayModelNotification2,
        NotificationId3  -> amendResponseModelNotification3  -> amendOutcomeDisplayModelNotification3,
        NotificationId4  -> amendResponseModelNotification4  -> amendOutcomeDisplayModelNotification4,
        NotificationId5  -> amendResponseModelNotification5  -> amendOutcomeDisplayModelNotification5,
        NotificationId6  -> amendResponseModelNotification6  -> amendOutcomeDisplayModelNotification6,
        NotificationId7  -> amendResponseModelNotification7  -> amendOutcomeDisplayModelNotification7,
        NotificationId8  -> amendResponseModelNotification8  -> amendOutcomeDisplayModelNotification8,
        NotificationId9  -> amendResponseModelNotification9  -> amendOutcomeDisplayModelNotification9,
        NotificationId10 -> amendResponseModelNotification10 -> amendOutcomeDisplayModelNotification10,
        NotificationId11 -> amendResponseModelNotification11 -> amendOutcomeDisplayModelNotification11,
        NotificationId12 -> amendResponseModelNotification12 -> amendOutcomeDisplayModelNotification12,
        NotificationId13 -> amendResponseModelNotification13 -> amendOutcomeDisplayModelNotification13,
        NotificationId14 -> amendResponseModelNotification14 -> amendOutcomeDisplayModelNotification14
      ).foreach { case ((notificationId, amendResponseModel), amendResultDisplayModel) =>
        s"notification id is $notificationId" in {
          AmendOutcomeDisplayModelConstructor.createAmendOutcomeDisplayModel(
            amendResponseModel,
            Some(personalDetailsModel),
            nino,
            notificationId
          ) shouldBe amendResultDisplayModel
        }
      }

    "throw exception" when {
      "protectedAmount is missing" in {
        val exception =
          the[OptionNotDefinedException] thrownBy AmendOutcomeDisplayModelConstructor.createAmendOutcomeDisplayModel(
            amendResponseModelNotification1.copy(
              protectedAmount = None
            ),
            Some(personalDetailsModel),
            nino,
            NotificationId1
          )

        exception.functionName shouldBe "createAmendResultDisplayModel"
        exception.optionName shouldBe "protectedAmount"
      }
    }
  }

}
