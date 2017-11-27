/*
 * Copyright 2017 HM Revenue & Customs
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

import java.time.LocalDate

import forms.PSODetailsForm._
import models.PSODetailsModel
import org.scalatestplus.play.OneAppPerSuite
import testHelpers.PSODetailsMessages
import uk.gov.hmrc.play.test.UnitSpec
import utils.Constants

class PSODetailsFormSpec extends UnitSpec with PSODetailsMessages with OneAppPerSuite {

  "PSODetailsForm" should {
    val validMap = Map("psoDay" -> "1", "psoMonth" -> "2", "psoYear" -> "2017", "psoAmt" -> "0.01")

    "return a valid form with additional validation" when {

      "provided with a valid model" in {
        val model = PSODetailsModel(1, 2, 2017, 0.01)
        val result = PSODetailsForm.validateForm(psoDetailsForm.fill(model))

        result.data shouldBe validMap
      }

      "provided with a valid map with a value for psoAmt which has two decimal places" in {
        val result = PSODetailsForm.validateForm(psoDetailsForm.bind(validMap))

        result.value shouldBe Some(PSODetailsModel(1, 2, 2017, 0.01))
      }

      "provided with a valid map with a value for psoAmt which is zero" in {
        val map = validMap.updated("psoAmt", "0")
        val result = PSODetailsForm.validateForm(psoDetailsForm.bind(map))

        result.value shouldBe Some(PSODetailsModel(1, 2, 2017, 0))
      }

      "provided with a valid map with a value for psoAmt which is the maximum value" in {
        val map = validMap.updated("psoAmt", {
          Constants.npsMaxCurrency - 1
        }.toString)
        val result = PSODetailsForm.validateForm(psoDetailsForm.bind(map))

        result.value shouldBe Some(PSODetailsModel(1, 2, 2017, Constants.npsMaxCurrency - 1))
      }
    }

    "produce an invalid form has one error with the correct error message" when {

      "not provided with a value for psoDay" in {
        val map = validMap - "psoDay"
        val result = psoDetailsForm.bind(map)

        result.errors.size shouldBe 1
        result.error("psoDay").get.message shouldBe errorMissingDay
      }

      "not provided with a value for psoMonth" in {
        val map = validMap - "psoMonth"
        val result = psoDetailsForm.bind(map)

        result.errors.size shouldBe 1
        result.error("psoMonth").get.message shouldBe errorMissingMonth
      }

      "not provided with a value for psoYear" in {
        val map = validMap - "psoYear"
        val result = psoDetailsForm.bind(map)

        result.errors.size shouldBe 1
        result.error("psoYear").get.message shouldBe errorMissingYear
      }

      "not provided with a value for psoAmt" in {
        val map = validMap - "psoAmt"
        val result = psoDetailsForm.bind(map)

        result.errors.size shouldBe 1
        result.error("psoAmt").get.message shouldBe errorRequired
      }

      "provided an amount with over two decimal places" in {
        val map = validMap.updated("psoAmt", "0.001")
        val result = psoDetailsForm.bind(map)

        result.errors.size shouldBe 1
        result.error("psoAmt").get.message shouldBe errorDecimal
      }

      "provided an amount with a negative value" in {
        val map = validMap.updated("psoAmt", "-0.01")
        val result = psoDetailsForm.bind(map)

        result.errors.size shouldBe 1
        result.error("psoAmt").get.message shouldBe errorNegative
      }

      "provided an amount with over the maximum value" in {
        val map = validMap.updated("psoAmt", Constants.npsMaxCurrency.toString)
        val result = psoDetailsForm.bind(map)

        result.errors.size shouldBe 1
        result.error("psoAmt").get.message shouldBe errorMaximum
      }
    }

    "use additional validation to invalidate a form" which {

      "has one error with the correct error message" when {

        "provided with an invalid date" in {
          val map = validMap.updated("psoDay", "50")
          val result = PSODetailsForm.validateForm(psoDetailsForm.bind(map))

          result.errors.size shouldBe 1
          result.error("psoDay").get.message shouldBe errorDate
        }

        "provided with a future data" in {
          val date = LocalDate.now().plusDays(1)
          val map = validMap
            .updated("psoDay", date.getDayOfMonth.toString)
            .updated("psoMonth", date.getMonthValue.toString)
            .updated("psoYear", date.getYear.toString)
          val result = PSODetailsForm.validateForm(psoDetailsForm.bind(map))

          result.errors.size shouldBe 1
          result.error("psoDay").get.message shouldBe errorDateRange
        }

        "provided with a date before the minimum for ip2016" in {
          val date = Constants.minIP16PSODate.minusDays(1)
          val map = validMap
            .updated("psoDay", date.getDayOfMonth.toString)
            .updated("psoMonth", date.getMonthValue.toString)
            .updated("psoYear", date.getYear.toString)
          val result = PSODetailsForm.validateForm(psoDetailsForm.bind(map))

          result.errors.size shouldBe 1
          result.error("psoDay").get.message shouldBe errorDateRange
        }

        "provided with a date before the minimum for ip2014" in {
          val date = Constants.minIP14PSODate.minusDays(1)
          val map = validMap
            .updated("psoDay", date.getDayOfMonth.toString)
            .updated("psoMonth", date.getMonthValue.toString)
            .updated("psoYear", date.getYear.toString)
          val result = PSODetailsForm.validateForm(psoDetailsForm.bind(map))

          result.errors.size shouldBe 1
          result.error("psoDay").get.message shouldBe errorDateRange
        }
      }
    }
  }
}
