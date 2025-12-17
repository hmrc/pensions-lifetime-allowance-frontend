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

import common.Exceptions.RequiredValueNotDefinedException

class AmendPrintDisplayModelConstructorSpec extends DisplayConstructorsTestData {

  "createAmendPrintDisplayModel" should {
    "create AmendPrintDisplayModel" in {
      AmendPrintDisplayModelConstructor.createAmendPrintDisplayModel(
        Some(tstPersonalDetailsModel),
        amendResponseModel,
        tstNino
      ) shouldBe expectedAmendPrintDisplayModel
    }

    "create AmendPrintDisplayModel with empty protectionReference" in {

      AmendPrintDisplayModelConstructor.createAmendPrintDisplayModel(
        Some(tstPersonalDetailsModel),
        amendResponseModel.copy(protectionReference = None),
        tstNino
      ) shouldBe expectedAmendPrintDisplayModel.copy(protectionReference = Some("None"))
    }

    "throw exception" when {

      "provided with empty PersonalDetailsModel" in {
        val exc = the[RequiredValueNotDefinedException] thrownBy
          AmendPrintDisplayModelConstructor.createAmendPrintDisplayModel(None, amendResponseModel, tstNino)

        exc.functionName shouldBe "createPrintDisplayModel"
        exc.optionName shouldBe "personalDetailsModel"
      }

    }
  }

}
