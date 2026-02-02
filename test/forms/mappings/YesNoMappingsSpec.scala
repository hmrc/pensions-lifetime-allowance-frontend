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

package forms.mappings

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.data.{FormError, Mapping}

class YesNoMappingsSpec extends AnyWordSpec with Matchers {

  object TestYesNoMappings extends YesNoMappings

  val mandatoryMessageKey = "mandatoryError"

  val yesNoMappings: Mapping[String] = TestYesNoMappings.yesNoMapping(
    mandatoryMessageKey = mandatoryMessageKey
  )

  val valueKey: String = yesNoMappings.key

  val mandatoryError = FormError(valueKey, mandatoryMessageKey)

  "currencyMapping.bind" should {
    "return mandatoryError" when {
      "provided with no value" in {
        yesNoMappings.bind(Map.empty) shouldBe Left(Seq(mandatoryError))
      }

      "provided with a blank value" in {
        yesNoMappings.bind(Map(valueKey -> "")) shouldBe Left(Seq(mandatoryError))
      }

      "provided with whitespace" in {
        yesNoMappings.bind(Map(valueKey -> "       ")) shouldBe Left(Seq(mandatoryError))
      }

      "provided with non-YesNo value" when {
        "invalid value is \"nope\"" in {
          yesNoMappings.bind(Map(valueKey -> "nope")) shouldBe Left(Seq(mandatoryError))
        }

        "invalid value is \"yep\"" in {
          yesNoMappings.bind(Map(valueKey -> "yep")) shouldBe Left(Seq(mandatoryError))
        }
      }
    }

    "return \"yes\"" when {
      "provided with \"yes\"" in {
        yesNoMappings.bind(Map(valueKey -> "yes")) shouldBe Right("yes")
      }

      "provided with \"   yes   \"" in {
        yesNoMappings.bind(Map(valueKey -> "   yes   ")) shouldBe Right("yes")
      }
    }

    "return \"no\"" when {
      "provided with \"no\"" in {
        yesNoMappings.bind(Map(valueKey -> "no")) shouldBe Right("no")
      }

      "provided with \"   no   \"" in {
        yesNoMappings.bind(Map(valueKey -> "   no   ")) shouldBe Right("no")
      }
    }

  }

  "isPresent" should {
    "return true" when {
      "provided with \"yes\"" in {
        TestYesNoMappings.isPresent("yes") shouldBe true
      }
      "provided with \"   yes   \"" in {
        TestYesNoMappings.isPresent("   yes   ") shouldBe true
      }
    }

    "return false" when {
      "provided with empty string" in {
        TestYesNoMappings.isPresent("") shouldBe false
      }

      "provided with only whitespace" in {
        TestYesNoMappings.isPresent("   ") shouldBe false
      }
    }
  }

  "isYesOrNo" should {
    "return true" when {
      "provided with \"yes\"" in {
        TestYesNoMappings.isYesOrNo("yes") shouldBe true
      }

      "provided with \"   yes   \"" in {
        TestYesNoMappings.isYesOrNo("   yes   ") shouldBe true
      }

      "provided with \"no\"" in {
        TestYesNoMappings.isYesOrNo("no") shouldBe true
      }

      "provided with \"   no   \"" in {
        TestYesNoMappings.isYesOrNo("   no   ") shouldBe true
      }
    }

    "return false" when {
      "provided with \"hello\"" in {
        TestYesNoMappings.isYesOrNo("hello") shouldBe false
      }

      "provided with \"   hello   \"" in {
        TestYesNoMappings.isYesOrNo("   hello   ") shouldBe false
      }
    }
  }

}
