/*
 * Copyright 2018 HM Revenue & Customs
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

import forms.ExitSurveyForm._
import models.ExitSurveyModel
import uk.gov.hmrc.play.test.UnitSpec

class ExitSurveyFormSpec extends UnitSpec {

  "The ExitSurveyForm" should {

    "produce a valid form" when {

      "provided with a valid model" in {
        val model = ExitSurveyModel(Some("phone"), Some("phoneNow"), Some("else"), Some("recommend"), Some("satisfaction"))
        val result = exitSurveyForm.fill(model)

        result.data shouldBe Map(
          "phoneOrWrite" -> "phone",
          "phoneOrWriteNow" -> "phoneNow",
          "anythingElse" -> "else",
          "recommend" -> "recommend",
          "satisfaction" -> "satisfaction")
      }

      "provided with a valid map with all parameters" in {
        val map = Map(
          "phoneOrWrite" -> "phone",
          "phoneOrWriteNow" -> "phoneNow",
          "anythingElse" -> "else",
          "recommend" -> "recommend",
          "satisfaction" -> "satisfaction")
        val result = exitSurveyForm.bind(map)

        result.value shouldBe Some(ExitSurveyModel(Some("phone"), Some("phoneNow"), Some("else"), Some("recommend"), Some("satisfaction")))
      }

      "provided with a valid map with no parameters" in {
        val map = Map.empty[String, String]
        val result = exitSurveyForm.bind(map)

        result.value shouldBe Some(ExitSurveyModel(None, None, None, None, None))
      }
    }
  }
}
