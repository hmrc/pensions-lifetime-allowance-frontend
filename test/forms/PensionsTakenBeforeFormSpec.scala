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

import forms.PensionsTakenBeforeForm._
import models.PensionsTakenBeforeModel
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Lang
import testHelpers.{CommonErrorMessages, FakeApplication}
import utils.Constants

class PensionsTakenBeforeFormSpec extends FakeApplication with CommonErrorMessages with MockitoSugar {
  implicit val lang: Lang = mock[Lang]

  val messageKey = "pensionsTakenBefore"

  "The PensionsTakenBeforeForm" should {
    val validMap = Map("pensionsTakenBefore" -> "yes", "pensionsTakenBeforeAmt" -> "1")

    "return a valid form with additional validation" when {

      "provided with a valid model" in {
        val model = PensionsTakenBeforeModel("yes", Some(1))
        val result = pensionsTakenBeforeForm.fill(model)

        result.data shouldBe validMap
      }

      "provided with a valid map with no amount" in {
        val model = PensionsTakenBeforeModel("yes", None)
        val result = pensionsTakenBeforeForm.fill(model)

        result.data shouldBe Map("pensionsTakenBefore" -> "yes", "pensionsTakenBeforeAmt" -> "")
      }

      "provided with a valid map with an amount with two decimal places" in {
        val map = validMap.updated("pensionsTakenBeforeAmt", "0.01")
        val result = pensionsTakenBeforeForm.bind(map)

        result.value shouldBe Some(PensionsTakenBeforeModel("yes", Some(0.01)))
      }

      "provided with a valid map with an amount above the maximum" in {
        val map = validMap.updated("pensionsTakenBeforeAmt", {Constants.npsMaxCurrency - 1}.toString)
        val result = pensionsTakenBeforeForm.bind(map)

        result.value shouldBe Some(PensionsTakenBeforeModel("yes", Some(Constants.npsMaxCurrency - 1)))
      }

      "provided with a valid map with a zero amount" in {
        val map = validMap.updated("pensionsTakenBeforeAmt", "0")
        val result = pensionsTakenBeforeForm.bind(map)

        result.value shouldBe Some(PensionsTakenBeforeModel("yes", Some(0)))
      }
    }

    "produce an invalid form" which {

      "has only one error with the correct message" when {

        "not provided with a value for pensionsTakenBefore" in {
          val map = validMap.updated("pensionsTakenBefore", "")
          val result = pensionsTakenBeforeForm.bind(map)

          result.errors.size shouldBe 1
          result.errors.head.message shouldBe errorQuestion(messageKey)
        }
      }

      "uses additional validation to invalidate a form" which {

        "has one error with the correct error message" when {

          "not provided with an amount with a yes answer" in {
            val map = validMap.updated("pensionsTakenBeforeAmt", "")
            val result = pensionsTakenBeforeForm.bind(map)

            result.errors.size shouldBe 1
            result.errors.head.message  shouldBe errorMissingAmount(messageKey)
          }

          "provided with an amount greater than the maximum" in {
            val map = validMap.updated("pensionsTakenBeforeAmt", Constants.npsMaxCurrency+1.toString)
            val result = pensionsTakenBeforeForm.bind(map)

            result.errors.size shouldBe 1
            result.errors.head.message shouldBe errorMaximum(messageKey)
          }

          "provided with an amount with over two decimal places" in {
            val map = validMap.updated("pensionsTakenBeforeAmt", "0.001")
            val result = pensionsTakenBeforeForm.bind(map)

            result.errors.size shouldBe 1
            result.errors.head.message shouldBe errorDecimal(messageKey)
          }

          "provided with a negative amount" in {
            val map = validMap.updated("pensionsTakenBeforeAmt", "-0.01")
            val result = pensionsTakenBeforeForm.bind(map)

            result.errors.size shouldBe 1
            result.errors.head.message shouldBe errorNegative(messageKey)
          }
        }
      }
    }
  }
}
