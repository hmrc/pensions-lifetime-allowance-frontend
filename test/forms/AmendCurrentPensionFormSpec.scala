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

import forms.AmendCurrentPensionForm._
import models.amendModels.AmendCurrentPensionModel
import models.pla.AmendableProtectionType.IndividualProtection2016
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Lang
import testHelpers.FakeApplication
import testHelpers.messages.CommonErrorMessages
import utils.Constants

class AmendCurrentPensionFormSpec extends FakeApplication with CommonErrorMessages with MockitoSugar {
  implicit val lang: Lang = mock[Lang]

  val messageKey = "currentPensions"

  "Amend current pensions form" should {
    val validMap = Map(
      "amendedUKPensionAmt" -> "1"
    )

    "create a valid form" when {

      "supplied with a valid model" in {
        val model  = AmendCurrentPensionModel(Some(1))
        val result = amendCurrentPensionForm(IndividualProtection2016.toString).fillAndValidate(model)

        result.data shouldBe validMap
      }

      "supplied with a valid map with an amount with two decimal places" in {
        val map    = validMap.updated("amendedUKPensionAmt", "0.01")
        val result = amendCurrentPensionForm(IndividualProtection2016.toString).bind(map)

        result.value shouldBe Some(AmendCurrentPensionModel(Some(0.01)))
      }

      "supplied with a valid map with zero amount" in {
        val map    = validMap.updated("amendedUKPensionAmt", "0")
        val result = amendCurrentPensionForm(IndividualProtection2016.toString).bind(map)

        result.value shouldBe Some(AmendCurrentPensionModel(Some(0)))
      }

      "supplied with a valid map with the maximum amount" in {
        val map    = validMap.updated("amendedUKPensionAmt", { Constants.npsMaxCurrency - 1 }.toString)
        val result = amendCurrentPensionForm(IndividualProtection2016.toString).bind(map)

        result.value shouldBe Some(AmendCurrentPensionModel(Some(Constants.npsMaxCurrency - 1)))
      }
    }

    "create an invalid form" when {

      "not supplied with an amended amount".which {

        "has a single error with the correct error message" in {
          val map    = validMap - "amendedUKPensionAmt"
          val result = amendCurrentPensionForm(IndividualProtection2016.toString).bind(map)

          result.errors.size shouldBe 1
          result.error("amendedUKPensionAmt").get.message shouldBe errorMissingAmount(
            messageKey,
            IndividualProtection2016.toString
          )
        }
      }

      "supplied with an empty amount".which {

        "has a single error with the correct error message" in {
          val map    = validMap.updated("amendedUKPensionAmt", "")
          val result = amendCurrentPensionForm(IndividualProtection2016.toString).bind(map)

          result.errors.size shouldBe 1
          result.error("amendedUKPensionAmt").get.message shouldBe errorMissingAmount(
            messageKey,
            IndividualProtection2016.toString
          )
        }
      }

      "supplied with a non-numeric amount".which {
        "has a single error with the correct error message" in {
          val map    = validMap.updated("amendedUKPensionAmt", "a")
          val result = amendCurrentPensionForm(IndividualProtection2016.toString).bind(map)

          result.errors.size shouldBe 1
          result.error("amendedUKPensionAmt").get.message shouldBe errorReal(
            messageKey,
            IndividualProtection2016.toString
          )
        }
      }

      "supplied with a negative amount".which {
        "has a single error with the correct error message" in {
          val map    = validMap.updated("amendedUKPensionAmt", "-0.01")
          val result = amendCurrentPensionForm(IndividualProtection2016.toString).bind(map)

          result.errors.size shouldBe 1
          result.error("amendedUKPensionAmt").get.message shouldBe errorNegative(
            messageKey,
            IndividualProtection2016.toString
          )
        }
      }

      "supplied with an amount with too many decimal places" in {
        val map    = validMap.updated("amendedUKPensionAmt", "0.001")
        val result = amendCurrentPensionForm(IndividualProtection2016.toString).bind(map)

        result.errors.size shouldBe 1
        result.error("amendedUKPensionAmt").get.message shouldBe errorDecimal(
          messageKey,
          IndividualProtection2016.toString
        )
      }

      "supplied with an amount above the maximum" in {
        val map    = validMap.updated("amendedUKPensionAmt", Constants.npsMaxCurrency.toString)
        val result = amendCurrentPensionForm(IndividualProtection2016.toString).bind(map)

        result.errors.size shouldBe 1
        result.error("amendedUKPensionAmt").get.message shouldBe errorMaximum(
          messageKey,
          IndividualProtection2016.toString
        )
      }
    }
  }

}
