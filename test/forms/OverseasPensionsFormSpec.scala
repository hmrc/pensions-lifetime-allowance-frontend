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

import forms.OverseasPensionsForm._
import models.OverseasPensionsModel
import models.pla.AmendableProtectionType.IndividualProtection2016
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Lang
import testHelpers.FakeApplication
import testHelpers.messages.CommonErrorMessages
import utils.Constants

class OverseasPensionsFormSpec extends FakeApplication with CommonErrorMessages with MockitoSugar {
  implicit val lang: Lang = mock[Lang]

  val messageKey = "overseasPensions"

  "The OverseasPensionsForm" should {
    val validMap = Map("overseasPensions" -> "yes", "overseasPensionsAmt" -> "1")

    "return a valid form with additional validation" when {

      "provided with a valid model" in {
        val model  = OverseasPensionsModel("yes", Some(1))
        val result = overseasPensionsForm(IndividualProtection2016.toString).fill(model)

        result.data shouldBe validMap
      }

      "provided with a valid map with no amount" in {
        val map    = Map("overseasPensions" -> "no", "overseasPensionsAmt" -> "")
        val result = overseasPensionsForm(IndividualProtection2016.toString).bind(map)

        result.value shouldBe Some(OverseasPensionsModel("no", None))
      }

      "provided with a valid map with an amount with two decimal places" in {
        val map    = validMap.updated("overseasPensionsAmt", "0.01")
        val result = overseasPensionsForm(IndividualProtection2016.toString).bind(map)

        result.value shouldBe Some(OverseasPensionsModel("yes", Some(0.01)))
      }

      "provided with a valid map with an amount above the maximum" in {
        val map    = validMap.updated("overseasPensionsAmt", { Constants.npsMaxCurrency - 1 }.toString)
        val result = overseasPensionsForm(IndividualProtection2016.toString).bind(map)

        result.value shouldBe Some(OverseasPensionsModel("yes", Some(Constants.npsMaxCurrency - 1)))
      }

      "provided with a valid map with a zero amount" in {
        val map    = validMap.updated("overseasPensionsAmt", "0")
        val result = overseasPensionsForm(IndividualProtection2016.toString).bind(map)

        result.value shouldBe Some(OverseasPensionsModel("yes", Some(0)))
      }
    }

    "produce an invalid form".which {

      "has only one error with the correct message" when {

        "not provided with a value for overseasPensions" in {
          val map    = validMap - "overseasPensions"
          val result = overseasPensionsForm(IndividualProtection2016.toString).bind(map)

          result.errors.size shouldBe 1
          result.error("overseasPensions").get.message shouldBe errorQuestion(
            messageKey,
            IndividualProtection2016.toString
          )
        }

        "provided with a non-numeric amount" in {
          val map    = validMap.updated("overseasPensionsAmt", "a")
          val result = overseasPensionsForm(IndividualProtection2016.toString).bind(map)

          result.errors.size shouldBe 1
          result.errors.head.message shouldBe errorReal(messageKey, IndividualProtection2016.toString)
        }
      }

      "uses additional validation to invalidate a form".which {

        "has one error with the correct error message" when {

          "not provided with an amount with a yes answer" in {
            val map    = validMap.updated("overseasPensionsAmt", "")
            val result = overseasPensionsForm(IndividualProtection2016.toString).bind(map)

            result.errors.size shouldBe 1
            result.errors.head.message shouldBe errorMissingAmount(messageKey, IndividualProtection2016.toString)
          }

          "provided with an amount greater than the maximum" in {
            val map    = validMap.updated("overseasPensionsAmt", s"${Constants.npsMaxCurrency + 1}")
            val result = overseasPensionsForm(IndividualProtection2016.toString).bind(map)

            result.errors.size shouldBe 1
            result.errors.head.message shouldBe errorMaximum(messageKey, IndividualProtection2016.toString)
          }

          "provided with an amount with over two decimal places" in {
            val map    = validMap.updated("overseasPensionsAmt", "0.001")
            val result = overseasPensionsForm(IndividualProtection2016.toString).bind(map)

            result.errors.size shouldBe 1
            result.errors.head.message shouldBe errorDecimal(messageKey, IndividualProtection2016.toString)
          }

          "provided with a negative amount" in {
            val map    = validMap.updated("overseasPensionsAmt", "-0.01")
            val result = overseasPensionsForm(IndividualProtection2016.toString).bind(map)

            result.errors.size shouldBe 1
            result.errors.head.message shouldBe errorNegative(messageKey, IndividualProtection2016.toString)
          }
        }
      }
    }
  }

}
