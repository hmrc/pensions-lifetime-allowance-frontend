/*
 * Copyright 2020 HM Revenue & Customs
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

import forms.AmendPSODetailsForm._
import models.amendModels.AmendPSODetailsModel
import testHelpers.PSODetailsMessages
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import utils.Constants
import common.Exceptions
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Lang

class AmendPSODetailsFormSpec extends UnitSpec with PSODetailsMessages with WithFakeApplication with MockitoSugar {
  implicit val lang: Lang = mock[Lang]

  "The AmendPensionsTakenBetweenForm" should {
    val validMap = Map(
      "psoDay" -> "1",
      "psoMonth" -> "5",
      "psoYear" -> "2016",
      "psoAmt" -> "0.0",
      "protectionType" -> "ip2016",
      "status" -> "status",
      "existingPSO" -> "true")

    "produce a valid form with additional validation" when {

      "provided with a valid model" in {
        val model = AmendPSODetailsModel(Some(1), Some(5), Some(2016), Some(0.0), "ip2016", "status", existingPSO = true)
        val result = AmendPSODetailsForm.amendPsoDetailsForm.fill(model)

        result.data shouldBe validMap
      }

      "provided with a valid map and an amount of 0" in {
        val map = Map(
          "psoDay" -> "2",
          "psoMonth" -> "6",
          "psoYear" -> "2017",
          "psoAmt" -> "0.0",
          "protectionType" -> "ip2014",
          "status" -> "anotherStatus",
          "existingPSO" -> "false")
        val result = AmendPSODetailsForm.amendPsoDetailsForm.bind(map)

        result.value shouldBe Some(
          AmendPSODetailsModel(Some(2), Some(6), Some(2017), Some(0.0), "ip2014", "anotherStatus", existingPSO = false))
      }

      "provided with a valid map and an amount below the maximum" in {
        val map = validMap.updated("psoAmt", {Constants.npsMaxCurrency - 0.01}.toString)
        val result = AmendPSODetailsForm.amendPsoDetailsForm.bind(map)

        result.value shouldBe Some(
          AmendPSODetailsModel(Some(1), Some(5), Some(2016), Some(Constants.npsMaxCurrency - 0.01), "ip2016", "status", existingPSO = true))
      }

      "provided with a valid map and an amount with two decimal places" in {
        val map = validMap.updated("psoAmt", "0.01")
        val result = AmendPSODetailsForm.amendPsoDetailsForm.bind(map)

        result.value shouldBe Some(
          AmendPSODetailsModel(Some(1), Some(5), Some(2016), Some(0.01), "ip2016", "status", existingPSO = true))
      }
    }

    "produce an invalid form" which {

      "has one error with the correct error message" when {

        "not provided with a value for psoDay" in {
          val map = validMap - "psoDay"
          val result = amendPsoDetailsForm.bind(map)

          result.errors.size shouldBe 1
          result.error("psoDay").get.message shouldBe errorMissingDay
        }

        "not provided with a value for psoMonth" in {
          val map = validMap - "psoMonth"
          val result = amendPsoDetailsForm.bind(map)

          result.errors.size shouldBe 1
          result.error("psoMonth").get.message shouldBe errorMissingMonth
        }

        "not provided with a value for psoYear" in {
          val map = validMap - "psoYear"
          val result = amendPsoDetailsForm.bind(map)

          result.errors.size shouldBe 1
          result.error("psoYear").get.message shouldBe errorMissingYear
        }

        "not provided with a value for psoAmt" in {
          val map = validMap - "psoAmt"
          val result = amendPsoDetailsForm.bind(map)

          result.errors.size shouldBe 1
          result.error("psoAmt").get.message shouldBe errorAmendPsoDetailsMissingAmount
        }

        "not provided with a value for protectionType" in {
          val map = validMap - "protectionType"

          the[Exceptions.RequiredNotFoundProtectionTypeException] thrownBy amendPsoDetailsForm.bind(map) should have message
            "Value not found for protection type in [protectionFormatter]"
        }

        "not provided with a value for status" in {
          val map = validMap - "status"
          val result = amendPsoDetailsForm.bind(map)

          result.errors.size shouldBe 1
          result.error("status").get.message shouldBe errorRequired
        }

        "provided with a negative psoAmt value" in {
          val map = validMap.updated("psoAmt", "-0.01")
          val result = amendPsoDetailsForm.bind(map)

          result.errors.size shouldBe 1
          result.error("psoAmt").get.message shouldBe errorNegative
        }

        "provided with a psoAmt value above the maximum" in {
          val map = validMap.updated("psoAmt", Constants.npsMaxCurrency.toString)
          val result = amendPsoDetailsForm.bind(map)

          result.errors.size shouldBe 1
          result.error("psoAmt").get.message shouldBe errorMaximum
        }

        "provided with a psoAmt value with more than two decimal places" in {
          val map = validMap.updated("psoAmt", "0.001")
          val result = amendPsoDetailsForm.bind(map)

          result.errors.size shouldBe 1
          result.error("psoAmt").get.message shouldBe errorDecimal
        }
      }
    }

    "use additional validation to invalidate a form" which {

      "has one error with the correct error message" when {

        "provided with an invalid date" in {
          val map = validMap.updated("psoDay", "50")
          val result = AmendPSODetailsForm.amendPsoDetailsForm.bind(map)

          result.errors.size shouldBe 1
          result.error("psoDay").get.message shouldBe errorDate
        }

        "provided with a future data" in {
          val date = LocalDate.now().plusDays(1)
          val map = validMap
            .updated("psoDay", date.getDayOfMonth.toString)
            .updated("psoMonth", date.getMonthValue.toString)
            .updated("psoYear", date.getYear.toString)
          val result = AmendPSODetailsForm.amendPsoDetailsForm.bind(map)

          result.errors.size shouldBe 1
          result.error("psoDay").get.message shouldBe errorDateRange
        }

        "provided with a date before the minimum for ip2016" in {
          val date = Constants.minIP16PSODate.minusDays(1)
          val map = validMap
            .updated("psoDay", date.getDayOfMonth.toString)
            .updated("psoMonth", date.getMonthValue.toString)
            .updated("psoYear", date.getYear.toString)
          val result = AmendPSODetailsForm.amendPsoDetailsForm.bind(map)

          result.errors.size shouldBe 1
          result.error("psoDay").get.message shouldBe errorDateRange
        }

        "provided with a date before the minimum for ip2014" in {
          val date = Constants.minIP14PSODate.minusDays(1)
          val map = validMap
            .updated("psoDay", date.getDayOfMonth.toString)
            .updated("psoMonth", date.getMonthValue.toString)
            .updated("psoYear", date.getYear.toString)
          val result = AmendPSODetailsForm.amendPsoDetailsForm.bind(map)

          result.errors.size shouldBe 1
          result.error("psoDay").get.message shouldBe errorDateRange
        }
      }
    }
  }
}
