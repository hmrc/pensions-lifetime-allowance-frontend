/*
 * Copyright 2023 HM Revenue & Customs
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

package forms

import forms.PensionsTakenBetweenForm._
import models.PensionsTakenBetweenModel
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Lang
import testHelpers.{CommonErrorMessages, FakeApplication}

class PensionsTakenBetweenFormSpec extends FakeApplication with CommonErrorMessages with MockitoSugar {
  implicit val lang: Lang = mock[Lang]

  "The PensionsTakenBetweenForm" should {

    "return a valid form" when {

      "supplied with a valid model" in {
        val model = PensionsTakenBetweenModel("text")
        val result = pensionsTakenBetweenForm.fill(model)

        result.data shouldBe Map("pensionsTakenBetween" -> "text")
      }

      "supplied with a valid form" in {
        val map = Map("pensionsTakenBetween" -> "yes")
        val result = pensionsTakenBetweenForm.bind(map)

        result.value shouldBe Some(PensionsTakenBetweenModel("yes"))
      }
    }

    "return an invalid form with one error and the correct message" when {

      "not provided with a pensionsTakenBetween value" in {
        val map = Map.empty[String, String]
        val result = pensionsTakenBetweenForm.bind(map)

        result.errors.size shouldBe 1
        result.error("pensionsTakenBetween").get.message shouldBe errorQuestion("pensionsTakenBetween")
      }

      "provided with a invalid pensionsTakenBetween value" in {
        val map = Map("pensionsTakenBetween" -> "some-value")
        val result = pensionsTakenBetweenForm.bind(map)

        result.errors.size shouldBe 1
        result.error("pensionsTakenBetween").get.message shouldBe errorQuestion("pensionsTakenBetween")
      }
    }
  }
}