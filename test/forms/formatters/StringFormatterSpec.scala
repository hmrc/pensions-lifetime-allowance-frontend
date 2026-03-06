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

package forms.formatters

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.data.FormError

class StringFormatterSpec extends AnyWordSpec with Matchers {

  val mandatoryMessageKey = "mandatoryError"

  val valueKey = "key"

  val mandatoryError = FormError(valueKey, mandatoryMessageKey)

  val stringFormatter = StringFormatter(mandatoryMessageKey)

  "bind" should {
    "return mandatoryError" when {
      "provided with no value" in {
        stringFormatter.bind(valueKey, Map()) shouldBe Left(Seq(mandatoryError))
      }

      "provided with blank value" in {
        stringFormatter.bind(valueKey, Map(valueKey -> "")) shouldBe Left(Seq(mandatoryError))
      }

      "provided with only whitespace" in {
        stringFormatter.bind(valueKey, Map(valueKey -> "          ")) shouldBe Left(Seq(mandatoryError))
      }
    }

    "trim input" in {
      stringFormatter.bind(valueKey, Map(valueKey -> "               value           ")) shouldBe Right("value")
    }
  }

  "unbind" should {
    "return single element map containing value" in {
      stringFormatter.unbind(valueKey, "value") shouldBe Map(valueKey -> "value")
    }
  }

}
