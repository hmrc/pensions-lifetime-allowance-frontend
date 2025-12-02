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

import forms.PensionsUsedBetweenForm._
import models.PensionsUsedBetweenModel
import models.pla.AmendableProtectionType.IndividualProtection2016
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Lang
import testHelpers.FakeApplication
import testHelpers.messages.CommonErrorMessages
import utils.Constants

class PensionsUsedBetweenFormSpec extends FakeApplication with CommonErrorMessages with MockitoSugar {
  implicit val lang: Lang = mock[Lang]

  val messageKey = "pensionsUsedBetween"

  "The PensionsUsedBetweenForm" should {
    val validMap = Map("pensionsUsedBetweenAmt" -> "1")

    "return a valid form with additional validation" when {

      "provided with a valid model" in {
        val model  = PensionsUsedBetweenModel(Some(1))
        val result = pensionsUsedBetweenForm(IndividualProtection2016.toString).fill(model)

        result.data shouldBe validMap
      }

      "provided with a valid model with no amount" in {
        val model  = PensionsUsedBetweenModel(None)
        val result = pensionsUsedBetweenForm(IndividualProtection2016.toString).fill(model)

        result.data shouldBe Map()
      }

      "provided with a valid map with an amount with two decimal places" in {
        val map    = validMap.updated("pensionsUsedBetweenAmt", "0.01")
        val result = pensionsUsedBetweenForm(IndividualProtection2016.toString).bind(map)

        result.value shouldBe Some(PensionsUsedBetweenModel(Some(0.01)))
      }

      "provided with a valid map with an amount above the maximum" in {
        val map    = validMap.updated("pensionsUsedBetweenAmt", { Constants.npsMaxCurrency - 1 }.toString)
        val result = pensionsUsedBetweenForm(IndividualProtection2016.toString).bind(map)

        result.value shouldBe Some(PensionsUsedBetweenModel(Some(Constants.npsMaxCurrency - 1)))
      }

      "provided with a valid map with a zero amount" in {
        val map    = validMap.updated("pensionsUsedBetweenAmt", "0")
        val result = pensionsUsedBetweenForm(IndividualProtection2016.toString).bind(map)

        result.value shouldBe Some(PensionsUsedBetweenModel(Some(0)))
      }
    }

    "produce an invalid form".which {

      "has only one error with the correct message" when {

        "not provided with a value for pensionsUsedBetween" in {
          val map    = validMap - "pensionsUsedBetweenAmt"
          val result = pensionsUsedBetweenForm(IndividualProtection2016.toString).bind(map)

          result.errors.size shouldBe 1
          result.error("pensionsUsedBetweenAmt").get.message shouldBe errorMissingAmount(
            messageKey,
            IndividualProtection2016.toString
          )
        }

        "provided with a non-numeric amount" in {
          val map    = validMap.updated("pensionsUsedBetweenAmt", "a")
          val result = pensionsUsedBetweenForm(IndividualProtection2016.toString).bind(map)

          result.errors.size shouldBe 1
          result.errors.head.message shouldBe errorReal(messageKey)
        }
      }

      "uses additional validation to invalidate a form".which {

        "has one error with the correct error message" when {

          "not provided with an amount with a yes answer" in {
            val map    = Map("pensionsUsedBetween" -> "", "pensionsUsedBetweenAmt" -> "")
            val result = pensionsUsedBetweenForm(IndividualProtection2016.toString).bind(map)

            result.errors.size shouldBe 1
            result.errors.head.message shouldBe errorMissingAmount(messageKey, IndividualProtection2016.toString)
          }

          "provided with an amount greater than the maximum" in {
            val maxValue = s"${Constants.npsMaxCurrency + 1}"
            val map      = Map("pensionsUsedBetween" -> "yes", "pensionsUsedBetweenAmt" -> maxValue)
            val result   = pensionsUsedBetweenForm(IndividualProtection2016.toString).bind(map)

            result.errors.size shouldBe 1
            result.errors.head.message shouldBe errorMaximum(messageKey, IndividualProtection2016.toString)
          }

          "provided with an amount with over two decimal places" in {
            val map    = validMap.updated("pensionsUsedBetweenAmt", "0.001")
            val result = pensionsUsedBetweenForm(IndividualProtection2016.toString).bind(map)

            result.errors.size shouldBe 1
            result.errors.head.message shouldBe errorDecimal(messageKey, IndividualProtection2016.toString)
          }

          "provided with a negative amount" in {
            val map    = validMap.updated("pensionsUsedBetweenAmt", "-0.01")
            val result = pensionsUsedBetweenForm(IndividualProtection2016.toString).bind(map)

            result.errors.size shouldBe 1
            result.errors.head.message shouldBe errorNegative(messageKey, IndividualProtection2016.toString)
          }
        }
      }
    }
  }

}
