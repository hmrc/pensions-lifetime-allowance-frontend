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

import controllers.helpers.FakeRequestHelper
import forms.PsoDetailsForm._
import models.PsoDetailsModel
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.{Lang, Messages, MessagesApi}
import testHelpers.FakeApplication
import testHelpers.messages.PsoDetailsErrorMessages
import utils.Constants

import java.time.LocalDate

class PsoDetailsFormSpec extends FakeApplication with PsoDetailsErrorMessages with MockitoSugar with FakeRequestHelper {

  implicit val lang: Lang = mock[Lang]

  val messagesApi: MessagesApi        = inject[MessagesApi]
  implicit val testMessages: Messages = messagesApi.preferred(fakeRequest)

  val messageKey     = "psoDetails"
  val dateMessageKey = "pso"

  "PsoDetailsForm" should {
    val validMap = Map(
      s"$dateMessageKey.day"   -> "1",
      s"$dateMessageKey.month" -> "2",
      s"$dateMessageKey.year"  -> "2017",
      "psoAmt"                 -> "0.01"
    )

    "return a valid form with additional validation" when {

      val year  = 2017
      val month = 2
      val day   = 1

      "provided with a valid model" in {
        val model  = PsoDetailsModel(LocalDate.of(year, month, day), Some(0.01))
        val result = psoDetailsForm().fill(model)

        result.data shouldBe validMap
      }

      "provided with a valid map with a value for psoAmt which has two decimal places" in {
        val result = psoDetailsForm().bind(validMap)

        result.value shouldBe Some(PsoDetailsModel(LocalDate.of(year, month, day), Some(0.01)))
      }

      "provided with a valid map with a value for psoAmt which is zero" in {
        val map    = validMap.updated("psoAmt", "0")
        val result = psoDetailsForm().bind(map)

        result.value shouldBe Some(PsoDetailsModel(LocalDate.of(year, month, day), Some(0.0)))
      }

      "provided with a valid map with a value for psoAmt which is the maximum value" in {
        val map = validMap.updated(
          "psoAmt", {
            Constants.npsMaxCurrency - 1
          }.toString
        )
        val result = psoDetailsForm().bind(map)

        result.value shouldBe Some(PsoDetailsModel(LocalDate.of(year, month, day), Some(Constants.npsMaxCurrency - 1)))
      }
    }

    "produce an invalid form has one error with the correct error message" when {

      "not provided with a value for psoDay" in {
        val map    = validMap - s"$dateMessageKey.day"
        val result = psoDetailsForm().bind(map)

        result.errors.size shouldBe 1
        result.error(s"$dateMessageKey.day").get.message shouldBe errorRequired(dateMessageKey, ".day")
      }

      "not provided with a value for psoMonth" in {
        val map    = validMap - s"$dateMessageKey.month"
        val result = psoDetailsForm().bind(map)

        result.errors.size shouldBe 1
        result.error(s"$dateMessageKey.month").get.message shouldBe errorRequired(dateMessageKey, ".month")
      }

      "not provided with a value for psoYear" in {
        val map    = validMap - s"$dateMessageKey.year"
        val result = psoDetailsForm().bind(map)

        result.errors.size shouldBe 1
        result.error(s"$dateMessageKey.year").get.message shouldBe errorRequired(dateMessageKey, ".year")
      }

      "not provided with a value for psoAmt" in {
        val map    = validMap - "psoAmt"
        val result = psoDetailsForm().bind(map)

        result.errors.size shouldBe 1
        result.error("psoAmt").get.message shouldBe errorMissingAmount(messageKey)
      }

      "provided an amount with over two decimal places" in {
        val map    = validMap.updated("psoAmt", "0.001")
        val result = psoDetailsForm().bind(map)

        result.errors.size shouldBe 1
        result.error("psoAmt").get.message shouldBe errorDecimal("psoDetails")
      }

      "provided an amount with a negative value" in {
        val map    = validMap.updated("psoAmt", "-0.01")
        val result = psoDetailsForm().bind(map)

        result.errors.size shouldBe 1
        result.error("psoAmt").get.message shouldBe errorNegative(messageKey)
      }

      "provided an amount with over the maximum value" in {
        val map    = validMap.updated("psoAmt", Constants.npsMaxCurrency.toString)
        val result = psoDetailsForm().bind(map)

        result.errors.size shouldBe 1
        result.error("psoAmt").get.message shouldBe errorMaximum(messageKey)
      }

      "provided with an empty string for psoDay" in {
        val map    = validMap.updated(s"$dateMessageKey.day", "")
        val result = psoDetailsForm().bind(map)

        result.errors.size shouldBe 1
        result.error(s"$dateMessageKey.day").get.message shouldBe errorRequired(dateMessageKey, ".day")
      }

      "provided with an empty string for psoMonth" in {
        val map    = validMap.updated(s"$dateMessageKey.month", "")
        val result = psoDetailsForm().bind(map)

        result.errors.size shouldBe 1
        result.error(s"$dateMessageKey.month").get.message shouldBe errorRequired(dateMessageKey, ".month")
      }

      "provided with an empty string for psoYear" in {
        val map    = validMap.updated(s"$dateMessageKey.year", "")
        val result = psoDetailsForm().bind(map)

        result.errors.size shouldBe 1
        result.error(s"$dateMessageKey.year").get.message shouldBe errorRequired(dateMessageKey, ".year")
      }

      "provided with a non integer equivalent string for psoDay" in {
        val map    = validMap.updated(s"$dateMessageKey.day", "a")
        val result = psoDetailsForm().bind(map)

        result.errors.size shouldBe 1
        result.error(s"$dateMessageKey.day").get.message shouldBe errorInvalid(dateMessageKey, ".day")
      }

      "provided with a non integer equivalent string for psoMonth" in {
        val map    = validMap.updated(s"$dateMessageKey.month", "b")
        val result = psoDetailsForm().bind(map)

        result.errors.size shouldBe 1
        result.error(s"$dateMessageKey.month").get.message shouldBe errorInvalid(dateMessageKey, ".month")
      }

      "provided with a non integer equivalent string for psoYear" in {
        val map    = validMap.updated(s"$dateMessageKey.year", "c")
        val result = psoDetailsForm().bind(map)

        result.errors.size shouldBe 1
        result.error(s"$dateMessageKey.year").get.message shouldBe errorInvalid(dateMessageKey, ".year")
      }
    }

    "use additional validation to invalidate a form".which {

      "has one error with the correct error message" when {

        "provided with an invalid date" in {
          val map    = validMap.updated(s"$dateMessageKey.day", "50")
          val result = psoDetailsForm().bind(map)

          result.errors.size shouldBe 1
          result.error(s"$dateMessageKey.day").get.message shouldBe errorNotReal(dateMessageKey, ".day")
        }

        "provided with a future data" in {
          val date = LocalDate.now().plusDays(2)
          val map = validMap
            .updated(s"$dateMessageKey.day", date.getDayOfMonth.toString)
            .updated(s"$dateMessageKey.month", date.getMonthValue.toString)
            .updated(s"$dateMessageKey.year", date.getYear.toString)
          val result = psoDetailsForm().bind(map)

          result.errors.size shouldBe 1
          result.error(dateMessageKey).get.message shouldBe errorRange(dateMessageKey, ".max")
        }

        "provided with a date before the minimum for ip2016" in {
          val date = Constants.minIP16PsoDate.minusDays(1)
          val map = validMap
            .updated(s"$dateMessageKey.day", date.getDayOfMonth.toString)
            .updated(s"$dateMessageKey.month", date.getMonthValue.toString)
            .updated(s"$dateMessageKey.year", date.getYear.toString)
          val result = psoDetailsForm().bind(map)

          result.errors.size shouldBe 1
          result.error(dateMessageKey).get.message shouldBe errorRange(dateMessageKey, ".min")
        }

        "provided with a date before the minimum for ip2014" in {
          val date = Constants.minIP14PsoDate.minusDays(1)
          val map = validMap
            .updated(s"$dateMessageKey.day", date.getDayOfMonth.toString)
            .updated(s"$dateMessageKey.month", date.getMonthValue.toString)
            .updated(s"$dateMessageKey.year", date.getYear.toString)
          val result = psoDetailsForm().bind(map)

          result.errors.size shouldBe 1
          result.error(dateMessageKey).get.message shouldBe errorRange(dateMessageKey, ".min")
        }
      }
    }
  }

}
