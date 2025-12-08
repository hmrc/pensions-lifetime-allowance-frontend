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

import models.NotificationId.NotificationId1
import models.amend.AmendProtectionModel
import models.{DateModel, PensionDebitModel, TransformedReadResponseModel}
import play.api.i18n.{Lang, MessagesApi}
import testdata.AmendProtectionDisplayModelTestData.amendResponseModelNotification1

class DisplayConstructorsSpec extends DisplayConstructorsTestData {

  val langEnglish: Lang = Lang.defaultLang
  val langWelsh: Lang   = Lang("cy")

  implicit val messagesApi: MessagesApi = inject[MessagesApi]

  val displayConstructors = new DisplayConstructors()(messagesApi)

  "createPrintDisplayModel" should {
    "use correct messages localisation" when {
      "provided with English language" in {
        val result =
          displayConstructors.createPrintDisplayModel(Some(tstPersonalDetailsModel), tstProtectionModel, tstNino)(
            langEnglish
          )

        result.certificateDate shouldBe Some("17 April 2016")
      }

      "provided with Welsh language" in {
        val result =
          displayConstructors.createPrintDisplayModel(Some(tstPersonalDetailsModel), tstProtectionModel, tstNino)(
            langWelsh
          )

        result.certificateDate shouldBe Some("17 Ebrill 2016")
      }
    }
  }

  "createExistingProtectionsDisplayModel" should {
    "use correct messages localisation" when {

      val tstTransformedReadResponseModel = TransformedReadResponseModel(Some(tstProtectionModel), Seq.empty)

      "provided with English language" in {
        val result =
          displayConstructors.createExistingProtectionsDisplayModel(tstTransformedReadResponseModel)(langEnglish)

        result.activeProtection.get.certificateDate shouldBe Some("17 April 2016")
      }

      "provided with Welsh language" in {
        val result =
          displayConstructors.createExistingProtectionsDisplayModel(tstTransformedReadResponseModel)(langWelsh)

        result.activeProtection.get.certificateDate shouldBe Some("17 Ebrill 2016")
      }
    }
  }

  "createAmendDisplayModel" should {
    "use correct messages localisation" when {

      val protectionModel = tstProtectionModel.copy(
        uncrystallisedRights = Some(100_000)
      )

      val tstAmendProtectionModel = AmendProtectionModel
        .tryFromProtection(protectionModel)
        .get
        .withPensionDebit(
          Some(
            PensionDebitModel(
              startDate = DateModel.of(2016, 4, 17),
              amount = 100
            )
          )
        )

      "provided with English language" in {
        val result = displayConstructors.createAmendDisplayModel(tstAmendProtectionModel)(langEnglish)

        result.psoSections.head.rows.head.displayValue shouldBe Seq("£100", "17 April 2016")
      }

      "provided with Welsh language" in {
        val result = displayConstructors.createAmendDisplayModel(tstAmendProtectionModel)(langWelsh)

        result.psoSections.head.rows.head.displayValue shouldBe Seq("£100", "17 Ebrill 2016")
      }
    }
  }

  "createAmendResultDisplayModel" should {
    "use correct messages localisation" when {

      "provided with English language" in {
        val result = displayConstructors.createAmendOutcomeDisplayModel(
          amendResponseModelNotification1,
          Some(tstPersonalDetailsModel),
          tstNino,
          NotificationId1
        )(langEnglish)

        result.details.get.certificateDate shouldBe Some("14 July 2015")
      }

      "provided with Welsh language" in {
        val result = displayConstructors.createAmendOutcomeDisplayModel(
          amendResponseModelNotification1,
          Some(tstPersonalDetailsModel),
          tstNino,
          NotificationId1
        )(langWelsh)

        result.details.get.certificateDate shouldBe Some("14 Gorffennaf 2015")
      }
    }
  }

  "createAmendOutcomeDisplayModelNoNotificationId" should {
    "use correct messages localisation" when {

      "provided with English language" in {
        val result = displayConstructors.createAmendOutcomeDisplayModelNoNotificationId(
          amendResponseModelNotification1,
          Some(tstPersonalDetailsModel),
          tstNino
        )(langEnglish)

        result.details.get.certificateDate shouldBe Some("14 July 2015")
      }

      "provided with Welsh language" in {
        val result = displayConstructors.createAmendOutcomeDisplayModelNoNotificationId(
          amendResponseModelNotification1,
          Some(tstPersonalDetailsModel),
          tstNino
        )(langWelsh)

        result.details.get.certificateDate shouldBe Some("14 Gorffennaf 2015")
      }
    }
  }

}
