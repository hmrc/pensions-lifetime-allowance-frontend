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
import forms.AmendPsoDetailsForm._
import models.amendModels.AmendPsoDetailsModel
import models.pla.AmendProtectionLifetimeAllowanceType.{IndividualProtection2014, IndividualProtection2016}
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.{Messages, MessagesApi}
import testHelpers.FakeApplication
import testHelpers.messages.PsoDetailsErrorMessages
import utils.Constants

import java.time.LocalDate

class AmendPsoDetailsFormSpec
    extends FakeApplication
    with PsoDetailsErrorMessages
    with MockitoSugar
    with FakeRequestHelper {
  val messagesApi: MessagesApi        = inject[MessagesApi]
  implicit val testMessages: Messages = messagesApi.preferred(fakeRequest)

  val messageKey = "psoDetails"

  "The AmendPensionsTakenBetweenForm" should {
    val validMap = Map(
      "pso.day"   -> "1",
      "pso.month" -> "5",
      "pso.year"  -> "2016",
      "psoAmt"    -> "0.0"
    )

    "produce a valid form with additional validation" when {

      "provided with a valid model" in {
        val model = AmendPsoDetailsModel(LocalDate.of(2016, 5, 1), Some(0.0))
        val result =
          AmendPsoDetailsForm.amendPsoDetailsForm(IndividualProtection2016.toString).fill(model)

        result.data shouldBe validMap
      }

      "provided with a valid map and an amount of 0" in {
        val map = Map(
          "pso.day"   -> "2",
          "pso.month" -> "6",
          "pso.year"  -> "2017",
          "psoAmt"    -> "0.0"
        )
        val result =
          AmendPsoDetailsForm.amendPsoDetailsForm(IndividualProtection2014.toString).bind(map)

        result.value shouldBe Some(AmendPsoDetailsModel(LocalDate.of(2017, 6, 2), Some(0.0)))
      }

      "provided with a valid map and an amount below the maximum" in {
        val map = validMap.updated("psoAmt", { Constants.npsMaxCurrency - 0.01 }.toString)
        val result =
          AmendPsoDetailsForm.amendPsoDetailsForm(IndividualProtection2016.toString).bind(map)

        result.value shouldBe Some(
          AmendPsoDetailsModel(LocalDate.of(2016, 5, 1), Some(Constants.npsMaxCurrency - 0.01))
        )
      }

      "provided with a valid map and an amount with two decimal places" in {
        val map    = validMap.updated("psoAmt", "0.01")
        val result = AmendPsoDetailsForm.amendPsoDetailsForm(IndividualProtection2016.toString).bind(map)

        result.value shouldBe Some(AmendPsoDetailsModel(LocalDate.of(2016, 5, 1), Some(0.01)))
      }
    }

    "produce an invalid form".which {

      "has one error with the correct error message" when {

        "not provided with a value for psoDay" in {
          val map    = validMap - "pso.day"
          val result = amendPsoDetailsForm(IndividualProtection2016.toString).bind(map)

          result.errors.size shouldBe 1
          result.error("pso.day").get.message shouldBe errorPsoDay
        }

        "not provided with a value for psoMonth" in {
          val map    = validMap - "pso.month"
          val result = amendPsoDetailsForm(IndividualProtection2016.toString).bind(map)

          result.errors.size shouldBe 1
          result.error("pso.month").get.message shouldBe errorPsoMonth
        }

        "not provided with a value for psoYear" in {
          val map    = validMap - "pso.year"
          val result = amendPsoDetailsForm(IndividualProtection2016.toString).bind(map)

          result.errors.size shouldBe 1
          result.error("pso.year").get.message shouldBe errorPsoYear
        }

        "not provided with a value for psoAmt" in {
          val map    = validMap - "psoAmt"
          val result = amendPsoDetailsForm(IndividualProtection2016.toString).bind(map)

          result.errors.size shouldBe 1
          result.error("psoAmt").get.message shouldBe errorAmendPsoDetailsMissingAmount
        }

        "provided with a negative psoAmt value" in {
          val map    = validMap.updated("psoAmt", "-0.01")
          val result = amendPsoDetailsForm(IndividualProtection2016.toString).bind(map)

          result.errors.size shouldBe 1
          result.error("psoAmt").get.message shouldBe errorNegative(messageKey)
        }

        "provided with a psoAmt value above the maximum" in {
          val map    = validMap.updated("psoAmt", Constants.npsMaxCurrency.toString)
          val result = amendPsoDetailsForm(IndividualProtection2016.toString).bind(map)

          result.errors.size shouldBe 1
          result.error("psoAmt").get.message shouldBe errorMaximum(messageKey)
        }

        "provided with a psoAmt value with more than two decimal places" in {
          val map    = validMap.updated("psoAmt", "0.001")
          val result = amendPsoDetailsForm(IndividualProtection2016.toString).bind(map)

          result.errors.size shouldBe 1
          result.error("psoAmt").get.message shouldBe errorDecimal(messageKey)
        }
      }
    }

    "use additional validation to invalidate a form".which {

      "has one error with the correct error message" when {

        "provided with an invalid date" in {
          val map = validMap.updated("pso.day", "50")
          val result =
            AmendPsoDetailsForm.amendPsoDetailsForm(IndividualProtection2016.toString).bind(map)

          result.errors.size shouldBe 1
          result.error("pso.day").get.message shouldBe errorPsoNotRealDay
        }

        "provided with a future date" in {
          val date = LocalDate.now().plusDays(1)
          val map = validMap
            .updated("pso.day", date.getDayOfMonth.toString)
            .updated("pso.month", date.getMonthValue.toString)
            .updated("pso.year", date.getYear.toString)
          val result =
            AmendPsoDetailsForm.amendPsoDetailsForm(IndividualProtection2016.toString).bind(map)

          result.errors.size shouldBe 1
          result.error("pso").get.message shouldBe errorPsoDateInFuture
        }

        "provided with a date before the minimum for ip2016" in {
          val date = Constants.minIP16PsoDate.minusDays(1)
          val map = validMap
            .updated("pso.day", date.getDayOfMonth.toString)
            .updated("pso.month", date.getMonthValue.toString)
            .updated("pso.year", date.getYear.toString)
          val result =
            AmendPsoDetailsForm.amendPsoDetailsForm(IndividualProtection2016.toString).bind(map)

          result.errors.size shouldBe 1
          result.error("pso").get.message shouldBe errorPsoDateBeforeMin
        }

        "provided with a date before the minimum for ip2014" in {
          val date = Constants.minIP14PsoDate.minusDays(1)
          val map = validMap
            .updated("pso.day", date.getDayOfMonth.toString)
            .updated("pso.month", date.getMonthValue.toString)
            .updated("pso.year", date.getYear.toString)
          val result =
            AmendPsoDetailsForm.amendPsoDetailsForm(IndividualProtection2014.toString).bind(map)

          result.errors.size shouldBe 1
          result.error("pso").get.message shouldBe errorPsoDateBeforeMin
        }
      }
    }
  }

}
