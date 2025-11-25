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
import enums.ApplicationType
import models.display.{ActiveAmendResultDisplayModel, ProtectionDetailsDisplayModel}
import models.pla.response.ProtectionType._
import models.{AmendResponseModel, Person, PersonalDetailsModel, ProtectionModel}

class ActiveAmendResponseModelConstructorSpec extends DisplayConstructorsTestData {

  "createActiveAmendResponseModel" should {
    val amendResponseModel = AmendResponseModel(
      ProtectionModel(
        psaCheckReference = Some("psaRef"),
        protectionID = Some(100003),
        protectionType = Some(IndividualProtection2014.toString),
        protectionReference = Some("protectionRef"),
        certificateDate = Some("2016-06-14T15:14:00"),
        protectedAmount = Some(1350000.45),
        notificationId = Some(33)
      )
    )

    val person               = Person(firstName = "Jim", lastName = "Davis")
    val personalDetailsModel = PersonalDetailsModel(person)
    val nino                 = "testNino"

    val activeAmendResultDisplayModel = ActiveAmendResultDisplayModel(
      firstName = "Jim",
      surname = "Davis",
      nino = nino,
      protectionType = ApplicationType.IP2014,
      notificationId = "33",
      protectedAmount = "£1,350,000.45",
      details = Some(
        ProtectionDetailsDisplayModel(
          protectionReference = "protectionRef",
          psaReference = "psaRef",
          applicationDate = Some("14 June 2016")
        )
      )
    )
    "correctly transform an AmendResponseModel into an ActiveAmendResultDisplayModel" in {

      ActiveAmendResultDisplayModelConstructor.createActiveAmendResponseDisplayModel(
        amendResponseModel,
        Some(personalDetailsModel),
        nino
      ) shouldBe activeAmendResultDisplayModel
    }

    "correctly transform an AmendResponseModel into an ActiveAmendResultDisplayModel with protectionReference None" in {
      val activeAmendResultDisplayModel = ActiveAmendResultDisplayModel(
        firstName = "Jim",
        surname = "Davis",
        nino = nino,
        protectionType = ApplicationType.IP2014,
        notificationId = "33",
        protectedAmount = "£1,350,000.45",
        details = Some(
          ProtectionDetailsDisplayModel(
            protectionReference = "None",
            psaReference = "psaRef",
            applicationDate = Some("14 June 2016")
          )
        )
      )
      ActiveAmendResultDisplayModelConstructor.createActiveAmendResponseDisplayModel(
        amendResponseModel.copy(protection = amendResponseModel.protection.copy(protectionReference = None)),
        Some(personalDetailsModel),
        nino
      ) shouldBe activeAmendResultDisplayModel
    }

    "throw exception" when {

      "protected amount is NOT present" in {
        val amendResponseModel = AmendResponseModel(
          ProtectionModel(
            psaCheckReference = Some("psaRef"),
            protectionID = Some(100003),
            protectionType = Some(IndividualProtection2014.toString),
            protectionReference = Some("protectionRef"),
            certificateDate = Some("2016-06-14T15:14:00"),
            protectedAmount = None,
            notificationId = Some(33)
          )
        )

        val person               = Person(firstName = "Jim", lastName = "Davis")
        val personalDetailsModel = PersonalDetailsModel(person)
        val nino                 = "testNino"

        val exc = the[OptionNotDefinedException] thrownBy
          ActiveAmendResultDisplayModelConstructor.createActiveAmendResponseDisplayModel(
            amendResponseModel,
            Some(personalDetailsModel),
            nino
          )

        exc.functionName shouldBe "createActiveAmendResponseDisplayModel"
        exc.optionName shouldBe "protectedAmount"
        exc.applicationType shouldBe IndividualProtection2014.toString
      }

      "notification ID is NOT present" in {
        val amendResponseModel = AmendResponseModel(
          ProtectionModel(
            psaCheckReference = Some("psaRef"),
            protectionID = Some(100003),
            protectionType = Some(IndividualProtection2014.toString),
            protectionReference = Some("protectionRef"),
            certificateDate = Some("2016-06-14T15:14:00"),
            protectedAmount = Some(1350000.45),
            notificationId = None
          )
        )

        val person               = Person(firstName = "Jim", lastName = "Davis")
        val personalDetailsModel = PersonalDetailsModel(person)
        val nino                 = "testNino"

        val exc = the[OptionNotDefinedException] thrownBy
          ActiveAmendResultDisplayModelConstructor.createActiveAmendResponseDisplayModel(
            amendResponseModel,
            Some(personalDetailsModel),
            nino
          )

        exc.functionName shouldBe "createActiveAmendResponseDisplayModel"
        exc.optionName shouldBe "notificationId"
        exc.applicationType shouldBe IndividualProtection2014.toString
      }
    }
  }

}
