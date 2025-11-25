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
import models.display.AmendPrintDisplayModel
import models.pla.response.ProtectionStatus._
import models.pla.response.ProtectionType._
import models.{Person, PersonalDetailsModel, ProtectionModel}

class AmendPrintDisplayModelConstructorSpec extends DisplayConstructorsTestData {

  "createAmendPrintDisplayModel" should {

    val person               = Person(firstName = "Testy", lastName = "McTestface")
    val personalDetailsModel = PersonalDetailsModel(person)
    val protectionModel = ProtectionModel(
      psaCheckReference = Some(tstPSACheckRef),
      protectionID = Some(12345),
      protectionType = Some(IndividualProtection2014.toString),
      status = Some(Open.toString),
      certificateDate = Some("2016-04-17T15:14:00"),
      protectedAmount = Some(1250000),
      protectionReference = Some("PSA123456"),
      notificationId = Some(1)
    )
    val nino = "testNino"

    val expectedAmendPrintDisplayModel = AmendPrintDisplayModel(
      firstName = "Testy",
      surname = "Mctestface",
      nino = nino,
      protectionType = IndividualProtection2014.toString,
      status = None,
      psaCheckReference = Some(tstPSACheckRef),
      protectionReference = Some("PSA123456"),
      fixedProtectionReference = None,
      protectedAmount = Some("£1,250,000"),
      certificateDate = Some("17 April 2016"),
      certificateTime = Some("3:14pm")
    )

    "create AmendPrintDisplayModel" in {
      AmendPrintDisplayModelConstructor.createAmendPrintDisplayModel(
        Some(personalDetailsModel),
        protectionModel,
        nino
      ) shouldBe expectedAmendPrintDisplayModel
    }

    "create AmendPrintDisplayModel with empty protectionReference" in {

      AmendPrintDisplayModelConstructor.createAmendPrintDisplayModel(
        Some(personalDetailsModel),
        protectionModel.copy(protectionReference = None),
        nino
      ) shouldBe expectedAmendPrintDisplayModel.copy(protectionReference = Some("None"))
    }

    "throw exception" when {

      "provided with empty PersonalDetailsModel" in {
        val exc = the[RequiredValueNotDefinedException] thrownBy
          AmendPrintDisplayModelConstructor.createAmendPrintDisplayModel(None, protectionModel, nino)

        exc.functionName shouldBe "createPrintDisplayModel"
        exc.optionName shouldBe "personalDetailsModel"
      }

      "provided with empty psaCheckReference" in {
        val exc =
          the[RequiredValueNotDefinedException] thrownBy AmendPrintDisplayModelConstructor.createAmendPrintDisplayModel(
            Some(personalDetailsModel),
            protectionModel.copy(psaCheckReference = None),
            nino
          )

        exc.functionName shouldBe "createPrintDisplayModel"
        exc.optionName shouldBe "psaCheckReference"
      }
    }
  }

}
