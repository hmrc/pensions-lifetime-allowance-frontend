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

import models.display.InactiveAmendResultDisplayModel
import models.{AmendResponseModel, ProtectionModel}

class InactiveAmendResponseModelConstructorSpec extends DisplayConstructorsTestData {

  "createInactiveAmendResponseModel" should {
    "correctly transform an AmendResponseModel into an InActiveAmendResultDisplayModel" in {
      val tstAmendsResponseModel = AmendResponseModel(
        ProtectionModel(
          psaCheckReference = None,
          protectionID = None,
          notificationId = Some(30)
        )
      )
      val tstInactiveAmendsResultDisplayModel = InactiveAmendResultDisplayModel(
        notificationId = 30,
        additionalInfo = Seq("1")
      )

      InactiveAmendResultDisplayModelConstructor.createInactiveAmendResponseDisplayModel(
        tstAmendsResponseModel
      ) shouldBe tstInactiveAmendsResultDisplayModel

    }
  }

}
