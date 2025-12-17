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

import forms.AmendPensionsUsedBetweenForm._
import models.amend.value.AmendPensionsUsedBetweenModel
import models.pla.AmendableProtectionType.IndividualProtection2016
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Lang
import testHelpers.FakeApplication
import testHelpers.messages.CommonErrorMessages
import utils.Constants

class AmendPensionsUsedBetweenFormSpec extends FakeApplication with CommonErrorMessages with MockitoSugar {
  implicit val lang: Lang = mock[Lang]

  val messageKey = "pensionsUsedBetween"

  "The AmendPensionsUsedBetweenForm" should {
    val validMap = Map("amendedPensionsUsedBetweenAmt" -> "1000.0")

    "produce a valid form with additional validation" when {

      "provided with a valid model" in {
        val model  = AmendPensionsUsedBetweenModel(Some(1000.0))
        val result = amendPensionsUsedBetweenForm(IndividualProtection2016).fill(model)

        result.data shouldBe validMap
      }

      "provided with a valid map with an amount equal to the maximum" in {
        val map    = validMap.updated("amendedPensionsUsedBetweenAmt", { Constants.npsMaxCurrency - 1 }.toString)
        val result = amendPensionsUsedBetweenForm(IndividualProtection2016).bind(map)

        result.value shouldBe Some(AmendPensionsUsedBetweenModel(Some(Constants.npsMaxCurrency - 1)))
      }

      "provided with a valid map with an amount equal zero" in {
        val map    = validMap.updated("amendedPensionsUsedBetweenAmt", "0")
        val result = amendPensionsUsedBetweenForm(IndividualProtection2016).bind(map)

        result.value shouldBe Some(AmendPensionsUsedBetweenModel(Some(0)))
      }

      "provided with a valid map with an amount with two decimal places" in {
        val map    = validMap.updated("amendedPensionsUsedBetweenAmt", "0.01")
        val result = amendPensionsUsedBetweenForm(IndividualProtection2016).bind(map)

        result.value shouldBe Some(AmendPensionsUsedBetweenModel(Some(0.01)))
      }
    }

    "produce an invalid form".which {

      "has one error with the correct error message" when {
        "provided with an invalid value for amendedPensionsUsedBetweenAmt" in {
          val map    = validMap.updated("amendedPensionsUsedBetweenAmt", "-1")
          val result = amendPensionsUsedBetweenForm(IndividualProtection2016).bind(map)

          result.errors.size shouldBe 1
          result.errors.head.message shouldBe errorNegative(messageKey, IndividualProtection2016.toString)
        }
      }
    }

    "use additional validation to invalidate a form".which {

      "has one error with the correct error message" when {

        "provided an answer of yes for amendedPensionsUsedBetween with no value for amendedPensionsUsedBetweenAmt" in {
          val map    = validMap.updated("amendedPensionsUsedBetweenAmt", "")
          val result = amendPensionsUsedBetweenForm(IndividualProtection2016).bind(map)

          result.errors.size shouldBe 1
          result.errors.head.message shouldBe errorMissingAmount(messageKey, IndividualProtection2016.toString)
        }

        "provided an answer of yes for amendedPensionsUsedBetween with a value for amendedPensionsUsedBetweenAmt larger than the maximum" in {
          val map = validMap.updated("amendedPensionsUsedBetweenAmt", s"${Constants.npsMaxCurrency + 1}")

          val result = amendPensionsUsedBetweenForm(IndividualProtection2016).bind(map)

          result.errors.size shouldBe 1
          result.errors.head.message shouldBe errorMaximum(messageKey, IndividualProtection2016.toString)
        }

        "provided an answer of yes for amendedPensionsUsedBetween with a value for amendedPensionsUsedBetweenAmt that is negative" in {
          val map    = validMap.updated("amendedPensionsUsedBetweenAmt", "-0.01")
          val result = amendPensionsUsedBetweenForm(IndividualProtection2016).bind(map)

          result.errors.size shouldBe 1
          result.errors.head.message shouldBe errorNegative(messageKey, IndividualProtection2016.toString)
        }

        "provided an answer of yes for amendedPensionsUsedBetween with a value for amendedPensionsUsedBetweenAmt that has more than two decimal places" in {
          val map    = validMap.updated("amendedPensionsUsedBetweenAmt", "0.001")
          val result = amendPensionsUsedBetweenForm(IndividualProtection2016).bind(map)

          result.errors.size shouldBe 1
          result.errors.head.message shouldBe errorDecimal(messageKey, IndividualProtection2016.toString)
        }
      }
    }
  }

}
