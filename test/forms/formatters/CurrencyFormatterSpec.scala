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

class CurrencyFormatterSpec extends AnyWordSpec with Matchers {

  val mandatoryMessageKey = "mandatoryError"
  val invalidMessageKey   = "notReal"

  val currencyFormatter = CurrencyFormatter(
    mandatoryMessageKey = mandatoryMessageKey,
    invalidMessageKey = invalidMessageKey
  )

  val valueKey = "key"

  val mandatoryError = FormError(valueKey, mandatoryMessageKey)
  val invalidError   = FormError(valueKey, invalidMessageKey)

  "bind" should {

    "return mandatoryError" when {
      "value is missing" in {
        currencyFormatter.bind(valueKey, Map()) shouldBe Left(Seq(mandatoryError))
      }

      "value is blank" in {
        currencyFormatter.bind(valueKey, Map(valueKey -> "")) shouldBe Left(Seq(mandatoryError))
      }

      "value is only whitespace" in {
        currencyFormatter.bind(valueKey, Map(valueKey -> "             ")) shouldBe Left(Seq(mandatoryError))
      }
    }

    "return invalidError" when
      Seq(
        "g",
        "00d",
        "hello",
        "-1.00-1",
        "0.3.3.3",
        "1_000_000"
      ).foreach { value =>
        s"provided with $value" in {
          currencyFormatter.bind(valueKey, Map(valueKey -> value)) shouldBe Left(Seq(invalidError))
        }
      }

    "strip whitespace from input" when
      Seq(
        "       3000.22       " -> 3000.22,
        "3 000 000.67"          -> 3_000_000.67,
        "  1 2 3 4 5 . 6 7 "    -> 12_345.67
      ).foreach { case (string, value) =>
        s"provided with \"$string\"" in {
          currencyFormatter.bind(valueKey, Map(valueKey -> string)) shouldBe Right(Some(BigDecimal(value)))
        }
      }

    "strip leading pound sign from input" when
      Seq[(String, Double)](
        "£1,000,000" -> 1_000_000d,
        "£23.54"     -> 23.54
      ).foreach { case (string, value) =>
        s"provided with \"$string\"" in {
          currencyFormatter.bind(valueKey, Map(valueKey -> string)) shouldBe Right(Some(BigDecimal(value)))
        }
      }

    "strip commas from input" when
      Seq(
        "10,001"                                                              -> 10001,
        "10,0,0,1"                                                            -> 10001,
        ",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,0"                                   -> 0,
        ",,,,,,,,,,,1,,,,,,,,,,,,,0,,,,,,,,,0,,,,,,,,0,,,,,,,,,,,1,,,,,,,,,," -> 10001,
        ",-,,,,,,,,,,1,,,,,,,0,,,,,,,,,,22,"                                  -> -1022
      ).foreach { case (input, value) =>
        s"provided with $input" in {
          currencyFormatter.bind(valueKey, Map(valueKey -> input)) shouldBe Right(Some(BigDecimal(value)))
        }
      }

  }

  "unbind" should {
    "return empty map" when {
      "provided with no value" in {
        currencyFormatter.unbind(valueKey, None) shouldBe empty
      }
    }

    "return map with single value" when
      Seq[(Int, String)](
        1      -> "1",
        -1     -> "-1",
        200    -> "200",
        234879 -> "234879"
      ).foreach { case (value, string) =>
        s"provided with $value" in {
          currencyFormatter.unbind(valueKey, Some(value)) shouldBe Map(valueKey -> string)
        }
      }

    "format to 0 decimal places" when
      Seq[(BigDecimal, String)](
        BigDecimal("1")       -> "1",
        BigDecimal("1.0")     -> "1",
        BigDecimal("1.00")    -> "1",
        BigDecimal("0")       -> "0",
        BigDecimal("0.0")     -> "0",
        BigDecimal("0.00")    -> "0",
        BigDecimal("1000")    -> "1000",
        BigDecimal("1000.0")  -> "1000",
        BigDecimal("1000.00") -> "1000"
      ).foreach { case (value, string) =>
        s"provided with $value" in {
          currencyFormatter.unbind(valueKey, Some(value)) shouldBe Map(valueKey -> string)
        }
      }

    "format to 2 decimal places" when
      Seq[(BigDecimal, String)](
        BigDecimal("0.1")   -> "0.10",
        BigDecimal("0.10")  -> "0.10",
        BigDecimal("0.01")  -> "0.01",
        BigDecimal("10.2")  -> "10.20",
        BigDecimal("10.20") -> "10.20",
        BigDecimal("10.22") -> "10.22"
      ).foreach { case (value, string) =>
        s"provided with $value" in {
          currencyFormatter.unbind(valueKey, Some(value)) shouldBe Map(valueKey -> string)
        }
      }
  }

}
