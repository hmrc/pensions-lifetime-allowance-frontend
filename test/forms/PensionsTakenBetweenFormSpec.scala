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

import forms.PensionsTakenBetweenForm._
import models.PensionsTakenBetweenModel
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Lang
import testHelpers.{CommonErrorMessages, FakeApplication}
import utils.Constants

class PensionsTakenBetweenFormSpec extends FakeApplication with CommonErrorMessages with MockitoSugar {
  implicit val lang: Lang = mock[Lang]

  val messageKey = "pensionsTakenBetween"

  "The PensionsTakenBetweenForm" should {
    val validMap = Map("pensionsTakenBetween" -> "yes", "pensionsTakenBetweenAmt" -> "1")

    "return a valid form with additional validation" when {

      "provided with a valid model" in {
        val model = PensionsTakenBetweenModel("yes", Some(1))
        val result = pensionsTakenBetweenForm.fill(model)

        result.data shouldBe validMap
      }

      "provided with a valid model with no amount" in {
        val model = PensionsTakenBetweenModel("no", None)
        val result = pensionsTakenBetweenForm.fill(model)

        result.data shouldBe Map("pensionsTakenBetween" -> "no", "pensionsTakenBetweenAmt" -> "")
      }

      "provided with a valid map with an amount with two decimal places" in {
        val map = validMap.updated("pensionsTakenBetweenAmt", "0.01")
        val result = pensionsTakenBetweenForm.bind(map)

        result.value shouldBe Some(PensionsTakenBetweenModel("yes", Some(0.01)))
      }

      "provided with a valid map with an amount above the maximum" in {
        val map = validMap.updated("pensionsTakenBetweenAmt", {Constants.npsMaxCurrency - 1}.toString)
        val result = pensionsTakenBetweenForm.bind(map)

        result.value shouldBe Some(PensionsTakenBetweenModel("yes", Some(Constants.npsMaxCurrency - 1)))
      }

      "provided with a valid map with a zero amount" in {
        val map = validMap.updated("pensionsTakenBetweenAmt", "0")
        val result = pensionsTakenBetweenForm.bind(map)

        result.value shouldBe Some(PensionsTakenBetweenModel("yes", Some(0)))
      }
    }

    "produce an invalid form" which {

      "has only one error with the correct message" when {

        "not provided with a value for pensionsTakenBetween" in {
          val map = validMap - "pensionsTakenBetween"
          val result = pensionsTakenBetweenForm.bind(map)

          result.errors.size shouldBe 1
          result.error("pensionsTakenBetween").get.message shouldBe errorQuestion(messageKey)
        }

        "provided with a non-numeric amount" in {
          val map = validMap.updated("pensionsTakenBetweenAmt", "a")
          val result = pensionsTakenBetweenForm.bind(map)

          result.errors.size shouldBe 1
          result.errors.head.message  shouldBe errorMissingAmount(messageKey)
        }
      }

      "uses additional validation to invalidate a form" which {

        "has one error with the correct error message" when {

          "not provided with an amount with a yes answer" in {
            val map = Map("pensionsTakenBetween" -> "", "pensionsTakenBetweenAmt" -> "")
            val result = pensionsTakenBetweenForm.bind(map)

            result.errors.size shouldBe 1
            result.errors.head.message shouldBe errorQuestion(messageKey)
          }

          "provided with an amount greater than the maximum" in {
            val maxValue = Constants.npsMaxCurrency+1.toString
            val map = Map("pensionsTakenBetween" -> "yes", "pensionsTakenBetweenAmt" -> maxValue)
            val result = pensionsTakenBetweenForm.bind(map)

            result.errors.size shouldBe 1
            result.errors.head.message shouldBe errorMaximum(messageKey)
          }

          "provided with an amount with over two decimal places" in {
            val map = validMap.updated("pensionsTakenBetweenAmt", "0.001")
            val result = pensionsTakenBetweenForm.bind(map)

            result.errors.size shouldBe 1
            result.errors.head.message  shouldBe errorDecimal(messageKey)
          }

          "provided with a negative amount" in {
            val map = validMap.updated("pensionsTakenBetweenAmt", "-0.01")
            val result = pensionsTakenBetweenForm.bind(map)

            result.errors.size shouldBe 1
            result.errors.head.message  shouldBe errorNegative(messageKey)
          }
        }
      }
    }
  }
}

