/*
 * Copyright 2022 HM Revenue & Customs
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

import forms.PensionDebitsForm._
import models.PensionDebitsModel
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Lang
import testHelpers.{CommonErrorMessages, FakeApplication}

class PensionDebitsFormSpec extends FakeApplication with CommonErrorMessages with MockitoSugar {
  implicit val lang: Lang = mock[Lang]

  "The PensionDebitsForm" should {

    "return a valid form" when {

      "supplied with a valid model" in {
        val model = PensionDebitsModel(Some("text"))
        val result = pensionDebitsForm.fill(model)

        result.data shouldBe Map("pensionDebits" -> "text")
      }

      "supplied with a valid form" in {
        val map = Map("pensionDebits" -> "text")
        val result = pensionDebitsForm.bind(map)

        result.value shouldBe Some(PensionDebitsModel(Some("text")))
      }
    }

    "return an invalid form with one error and the correct message" when {

      "not provided with a pensionDebits value" in {
        val map = Map.empty[String, String]
        val result = pensionDebitsForm.bind(map)

        result.errors.size shouldBe 1
        result.error("pensionDebits").get.message shouldBe errorQuestion
      }
    }
  }
}
