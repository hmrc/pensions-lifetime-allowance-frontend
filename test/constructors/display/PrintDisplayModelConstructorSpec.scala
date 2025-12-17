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

import models.display.PrintDisplayModel
import models.pla.response.ProtectionStatus._
import models.pla.response.ProtectionType._
import models.{DateModel, Person, PersonalDetailsModel, ProtectionModel, TimeModel}

class PrintDisplayModelConstructorSpec extends DisplayConstructorsTestData {

  "createPrintDisplayModel" should {
    val tstPerson               = Person(firstName = "Testy", lastName = "McTestface")
    val tstPersonalDetailsModel = PersonalDetailsModel(tstPerson)
    val tstProtectionModel = ProtectionModel(
      psaCheckReference = tstPsaCheckRef,
      identifier = 12345,
      sequenceNumber = 1,
      protectionType = IndividualProtection2014,
      status = Open,
      certificateDate = Some(DateModel.of(2016, 4, 17)),
      certificateTime = Some(TimeModel.of(15, 14, 0)),
      protectedAmount = Some(1250000),
      protectionReference = Some("PSA123456")
    )
    val nino = "testNino"

    val tstResultPrintDisplayModel = PrintDisplayModel(
      firstName = "Testy",
      surname = "Mctestface",
      nino = nino,
      protectionType = IndividualProtection2014,
      status = Open,
      psaCheckReference = tstPsaCheckRef,
      protectionReference = "PSA123456",
      protectedAmount = Some("£1,250,000"),
      certificateDate = Some("17 April 2016"),
      certificateTime = Some("3:14pm")
    )
    "create a Print Display model" in {

      PrintDisplayModelConstructor.createPrintDisplayModel(
        Some(tstPersonalDetailsModel),
        tstProtectionModel,
        nino
      ) shouldBe tstResultPrintDisplayModel
    }

    "create a Print Display model with None protectionReference" in {
      PrintDisplayModelConstructor.createPrintDisplayModel(
        Some(tstPersonalDetailsModel),
        tstProtectionModel.copy(protectionReference = None),
        nino
      ) shouldBe tstResultPrintDisplayModel.copy(protectionReference = "None")
    }
  }

}
