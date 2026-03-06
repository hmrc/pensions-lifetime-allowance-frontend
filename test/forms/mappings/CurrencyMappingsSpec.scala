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
import utils.Constants.npsMaxCurrency

class CurrencyMappingsSpec extends AnyWordSpec with Matchers {

  object TestCurrencyMappings extends CurrencyMappings

  val mandatoryMessageKey            = "mandatoryError"
  val invalidMessageKey              = "notReal"
  val tooManyDecimalPlacesMessageKey = "decimal"
  val negativeMessageKey             = "negative"
  val tooHighMessageKey              = "max"

  val currencyMappings: Mapping[Option[BigDecimal]] = TestCurrencyMappings.currencyMapping(
    mandatoryMessageKey = mandatoryMessageKey,
    invalidMessageKey = invalidMessageKey,
    tooHighMessageKey = tooHighMessageKey,
    negativeMessageKey = negativeMessageKey,
    tooManyDecimalPlacesMessageKey = tooManyDecimalPlacesMessageKey
  )

  val valueKey: String = currencyMappings.key

  val mandatoryError            = FormError(valueKey, mandatoryMessageKey)
  val invalidError              = FormError(valueKey, invalidMessageKey)
  val tooManyDecimalPlacesError = FormError(valueKey, tooManyDecimalPlacesMessageKey)
  val negativeError             = FormError(valueKey, negativeMessageKey)
  val tooHighError              = FormError(valueKey, tooHighMessageKey)

  "currencyMapping.bind" should {
    "return mandatoryError" when {
      "provided with no value" in {
        currencyMappings.bind(Map.empty) shouldBe Left(Seq(mandatoryError))
      }

      "provided with a blank value" in {
        currencyMappings.bind(Map(valueKey -> "")) shouldBe Left(Seq(mandatoryError))
      }

      "provided with whitespace" in {
        currencyMappings.bind(Map(valueKey -> "       ")) shouldBe Left(Seq(mandatoryError))
      }
    }

    "return invalidError" when {
      "provided with invalid input" in {
        currencyMappings.bind(Map(valueKey -> "g")) shouldBe Left(Seq(invalidError))
      }
    }

    "return tooManyDecimalPlacesError" when
      Seq(
        "0.000",
        "0.001",
        "3.149265",
        "0.0000000000000000000000000001"
      ).foreach { value =>
        s"provided with $value" in {
          currencyMappings.bind(Map(valueKey -> value)) shouldBe Left(Seq(tooManyDecimalPlacesError))
        }
      }

    "return negativeError" when
      Seq(
        "-1",
        "-1234",
        "-0.01",
        "-1,999,999"
      ).foreach { value =>
        s"provided with $value" in {
          currencyMappings.bind(Map(valueKey -> value)) shouldBe Left(Seq(negativeError))
        }
      }

    "return tooHighError" when
      Seq(
        (npsMaxCurrency + 1).toString,
        10_000_000_000L.toString
      ).foreach { value =>
        s"provided with $value" in {
          currencyMappings.bind(Map(valueKey -> value)) shouldBe Left(Seq(tooHighError))
        }
      }

    "return correct value" when
      Seq[(String, BigDecimal)](
        "0"                     -> BigDecimal(0),
        "1"                     -> BigDecimal(1),
        "10"                    -> BigDecimal(10),
        "3.14"                  -> BigDecimal(3.14),
        "43,345,762.22"         -> BigDecimal(43_345_762.22),
        npsMaxCurrency.toString -> BigDecimal(npsMaxCurrency)
      ).foreach { case (string, value) =>
        s"provided with $string" in {
          currencyMappings.bind(Map(valueKey -> string)) shouldBe Right(Some(value))
        }
      }
  }

  "isMaxTwoDecimalPlaces" should {

    "return true" when {
      "provided with 1" in {
        TestCurrencyMappings.isMaxTwoDecimalPlaces(1) shouldBe true
      }
      "provided with 0.5" in {
        TestCurrencyMappings.isMaxTwoDecimalPlaces(0.5) shouldBe true
      }
      "provided with 17.34" in {
        TestCurrencyMappings.isMaxTwoDecimalPlaces(17.34) shouldBe true
      }
      "provided with 10.34000" in {
        TestCurrencyMappings.isMaxTwoDecimalPlaces(10.34000) shouldBe true
      }
      "provided with .25" in {
        TestCurrencyMappings.isMaxTwoDecimalPlaces(.25) shouldBe true
      }
    }

    "return false" when {
      "provided with 0.222" in {
        TestCurrencyMappings.isMaxTwoDecimalPlaces(0.222) shouldBe false
      }
      "provided with 3.004" in {
        TestCurrencyMappings.isMaxTwoDecimalPlaces(3.004) shouldBe false
      }
    }

  }

  "isPositive" should {

    "return true" when {
      "provided with 1" in {
        TestCurrencyMappings.isPositive(1) shouldBe true
      }
      "provided with 0" in {
        TestCurrencyMappings.isPositive(0) shouldBe true
      }
      "provided with 0.0000001" in {
        TestCurrencyMappings.isPositive(0.0000001) shouldBe true
      }
    }

    "return false" when {
      "provided with -1" in {
        TestCurrencyMappings.isPositive(-1) shouldBe false
      }
      "provided with -0.0000001" in {
        TestCurrencyMappings.isPositive(-0.0000001) shouldBe false
      }
    }

  }

  "isBelowMax" should {
    "return true" when
      Seq(
        10,
        npsMaxCurrency,
        0,
        -1
      ).foreach { value =>
        s"provided with $value" in {
          TestCurrencyMappings.isBelowMax(value) shouldBe true
        }
      }

    "return false" when
      Seq(
        npsMaxCurrency + 0.01,
        10_000_000_000L,
        Int.MaxValue
      ).foreach { value =>
        s"provided with $value" in {
          TestCurrencyMappings.isBelowMax(value) shouldBe false
        }
      }
  }

}
