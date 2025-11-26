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

import forms.CurrentPensionsForm._
import models.CurrentPensionsModel
import models.pla.AmendProtectionLifetimeAllowanceType.IndividualProtection2016
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Lang
import testHelpers.FakeApplication
import testHelpers.messages.CommonErrorMessages
import utils.Constants

class CurrentPensionsFormSpec extends FakeApplication with CommonErrorMessages with MockitoSugar {
  implicit val lang: Lang = mock[Lang]

  val messageKey = "currentPensions"

  "The CurrentPensionsForm" should {

    "produce a valid form " when {

      "provided with a valid model" in {
        val model  = CurrentPensionsModel(Some(1))
        val result = currentPensionsForm(IndividualProtection2016.toString).fill(model)

        result.data shouldBe Map("currentPensionsAmt" -> "1")
      }

      "provided with a valid form with an amount with two decimal places" in {
        val map    = Map("currentPensionsAmt" -> "0.01")
        val result = currentPensionsForm(IndividualProtection2016.toString).bind(map)

        result.value shouldBe Some(CurrentPensionsModel(Some(0.01)))
      }

      "provided with a valid form with a zero amount" in {
        val map    = Map("currentPensionsAmt" -> "0")
        val result = currentPensionsForm(IndividualProtection2016.toString).bind(map)

        result.value shouldBe Some(CurrentPensionsModel(Some(0)))
      }

      "provided with a valid form with the maximum amount" in {
        val map    = Map("currentPensionsAmt" -> { Constants.npsMaxCurrency - 1 }.toString)
        val result = currentPensionsForm(IndividualProtection2016.toString).bind(map)

        result.value shouldBe Some(CurrentPensionsModel(Some(Constants.npsMaxCurrency - 1)))
      }
    }

    "produce an invalid form".which {

      "has only one error with the correct message" when {

        "provided with no amount value" in {
          val map    = Map.empty[String, String]
          val result = currentPensionsForm(IndividualProtection2016.toString).bind(map)

          result.errors.size shouldBe 1
          result.error("currentPensionsAmt").get.message shouldBe errorMissingAmount(
            messageKey,
            IndividualProtection2016.toString
          )
        }

        "provided with an amount value with over two decimal places" in {
          val map    = Map("currentPensionsAmt" -> "0.001")
          val result = currentPensionsForm(IndividualProtection2016.toString).bind(map)

          result.errors.size shouldBe 1
          result.error("currentPensionsAmt").get.message shouldBe errorDecimal(
            messageKey,
            IndividualProtection2016.toString
          )
        }

        "provided with a negative amount value" in {
          val map    = Map("currentPensionsAmt" -> "-0.01")
          val result = currentPensionsForm(IndividualProtection2016.toString).bind(map)

          result.errors.size shouldBe 1
          result.error("currentPensionsAmt").get.message shouldBe errorNegative(
            messageKey,
            IndividualProtection2016.toString
          )
        }

        "provided with an amount value above the maximum" in {
          val map    = Map("currentPensionsAmt" -> Constants.npsMaxCurrency.toString)
          val result = currentPensionsForm(IndividualProtection2016.toString).bind(map)

          result.errors.size shouldBe 1
          result.error("currentPensionsAmt").get.message shouldBe errorMaximum(
            messageKey,
            IndividualProtection2016.toString
          )
        }
      }
    }
  }

}
