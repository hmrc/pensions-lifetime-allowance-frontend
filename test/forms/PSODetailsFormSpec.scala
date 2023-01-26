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

import forms.PSODetailsForm._
import models.PSODetailsModel
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Lang
import testHelpers.{FakeApplication, PSODetailsMessages}
import utils.Constants

import java.time.LocalDate

class PSODetailsFormSpec extends FakeApplication with PSODetailsMessages with MockitoSugar {
  implicit val lang: Lang = mock[Lang]

  "PSODetailsForm" should {
    val validMap = Map("pso.day" -> "1", "pso.month" -> "2", "pso.year" -> "2017", "psoAmt" -> "0.01")

    "return a valid form with additional validation" when {

      "provided with a valid model" in {
        val model = PSODetailsModel(1, 2, 2017, 0.01)
        val result = psoDetailsForm.fill(model)

        result.data shouldBe validMap
      }

      "provided with a valid map with a value for psoAmt which has two decimal places" in {
        val result = psoDetailsForm.bind(validMap)

        result.value shouldBe Some(PSODetailsModel(1, 2, 2017, 0.01))
      }

      "provided with a valid map with a value for psoAmt which is zero" in {
        val map = validMap.updated("psoAmt", "0")
        val result = psoDetailsForm.bind(map)

        result.value shouldBe Some(PSODetailsModel(1, 2, 2017, 0))
      }

      "provided with a valid map with a value for psoAmt which is the maximum value" in {
        val map = validMap.updated("psoAmt", {
          Constants.npsMaxCurrency - 1
        }.toString)
        val result = psoDetailsForm.bind(map)

        result.value shouldBe Some(PSODetailsModel(1, 2, 2017, Constants.npsMaxCurrency - 1))
      }
    }

    "produce an invalid form has one error with the correct error message" when {

      "not provided with a value for psoDay" in {
        val map = validMap - "pso.day"
        val result = psoDetailsForm.bind(map)

        result.errors.size shouldBe 1
        result.error("pso.day").get.message shouldBe errorMissingDay
      }

      "not provided with a value for psoMonth" in {
        val map = validMap - "pso.month"
        val result = psoDetailsForm.bind(map)

        result.errors.size shouldBe 1
        result.error("pso.month").get.message shouldBe errorMissingMonth
      }

      "not provided with a value for psoYear" in {
        val map = validMap - "pso.year"
        val result = psoDetailsForm.bind(map)

        result.errors.size shouldBe 1
        result.error("pso.year").get.message shouldBe errorMissingYear
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

      "provided with an empty string for psoDay" in {
        val map = validMap.updated("pso.day","")
        val result = psoDetailsForm.bind(map)

        result.errors.size shouldBe 1
        result.error("pso.day").get.message shouldBe errorMissingDay
      }

      "provided with an empty string for psoMonth" in {
        val map = validMap.updated("pso.month","")
        val result = psoDetailsForm.bind(map)

        result.errors.size shouldBe 1
        result.error("pso.month").get.message shouldBe errorMissingMonth
      }

      "provided with an empty string for psoYear" in {
        val map = validMap.updated("pso.year","")
        val result = psoDetailsForm.bind(map)

        result.errors.size shouldBe 1
        result.error("pso.year").get.message shouldBe errorMissingYear
      }

      "provided with a non integer equivalent string for psoDay" in {
        val map = validMap.updated("pso.day","a")
        val result = psoDetailsForm.bind(map)

        result.errors.size shouldBe 1
        result.error("pso.day").get.message shouldBe errorReal
      }

      "provided with a non integer equivalent string for psoMonth" in {
        val map = validMap.updated("pso.month","b")
        val result = psoDetailsForm.bind(map)

        result.errors.size shouldBe 1
        result.error("pso.month").get.message shouldBe errorReal
      }

      "provided with a non integer equivalent string for psoYear" in {
        val map = validMap.updated("pso.year","c")
        val result = psoDetailsForm.bind(map)

        result.errors.size shouldBe 1
        result.error("pso.year").get.message shouldBe errorReal
      }
    }

    "use additional validation to invalidate a form" which {

      "has one error with the correct error message" when {

        "provided with an invalid date" in {
          val map = validMap.updated("pso.day", "50")
          val result = psoDetailsForm.bind(map)

          result.errors.size shouldBe 1
          result.error("pso.day").get.message shouldBe errorDate
        }

        "provided with a future data" in {
          val date = LocalDate.now().plusDays(1)
          val map = validMap
            .updated("pso.day", date.getDayOfMonth.toString)
            .updated("pso.month", date.getMonthValue.toString)
            .updated("pso.year", date.getYear.toString)
          val result = psoDetailsForm.bind(map)

          result.errors.size shouldBe 1
          result.error("pso.day").get.message shouldBe errorDateRange
        }

        "provided with a date before the minimum for ip2016" in {
          val date = Constants.minIP16PSODate.minusDays(1)
          val map = validMap
            .updated("pso.day", date.getDayOfMonth.toString)
            .updated("pso.month", date.getMonthValue.toString)
            .updated("pso.year", date.getYear.toString)
          val result = psoDetailsForm.bind(map)

          result.errors.size shouldBe 1
          result.error("pso.day").get.message shouldBe errorDateRange
        }

        "provided with a date before the minimum for ip2014" in {
          val date = Constants.minIP14PSODate.minusDays(1)
          val map = validMap
            .updated("pso.day", date.getDayOfMonth.toString)
            .updated("pso.month", date.getMonthValue.toString)
            .updated("pso.year", date.getYear.toString)
          val result = psoDetailsForm.bind(map)

          result.errors.size shouldBe 1
          result.error("pso.day").get.message shouldBe errorDateRange
        }
      }
    }
  }
}
