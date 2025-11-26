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

import forms.AmendPensionsTakenBeforeForm._
import models.amendModels.AmendPensionsTakenBeforeModel
import models.pla.AmendProtectionLifetimeAllowanceType.IndividualProtection2016
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Lang
import testHelpers.FakeApplication
import testHelpers.messages.CommonErrorMessages

class AmendPensionsTakenBeforeFormSpec extends FakeApplication with CommonErrorMessages with MockitoSugar {
  implicit val lang: Lang = mock[Lang]

  val messageKey       = "pensionsTakenBefore"
  val amountMessageKey = "pensionsWorthBefore"

  "The AmendPensionsTakenBeforeForm" should {
    val validMap = Map("amendedPensionsTakenBefore" -> "yes")

    "produce a valid form with additional validation" when {

      "provided with a valid model" in {
        val model  = AmendPensionsTakenBeforeModel("yes")
        val result = amendPensionsTakenBeforeForm(IndividualProtection2016.toString).fill(model)

        result.data shouldBe validMap
      }

      "provided with a valid form with no amount" in {
        val model  = AmendPensionsTakenBeforeModel("no")
        val result = amendPensionsTakenBeforeForm(IndividualProtection2016.toString).fill(model)

        result.data shouldBe Map("amendedPensionsTakenBefore" -> "no")
      }

      "provided with a valid map with a zero amount" in {
        val map    = validMap.updated("amendedPensionsTakenBeforeAmt", "0")
        val result = amendPensionsTakenBeforeForm(IndividualProtection2016.toString).bind(map)

        result.value shouldBe Some(AmendPensionsTakenBeforeModel("yes"))
      }

      "provided with a valid map with an amount with two decimal places" in {
        val map    = validMap.updated("amendedPensionsTakenBeforeAmt", "0.01")
        val result = amendPensionsTakenBeforeForm(IndividualProtection2016.toString).bind(map)

        result.value shouldBe Some(AmendPensionsTakenBeforeModel("yes"))
      }
    }

    "produce an invalid form".which {

      "has one error with the correct error message" when {

        "not provided with a value for amendedPensionsTakenBefore" in {
          val map    = validMap - "amendedPensionsTakenBefore"
          val result = amendPensionsTakenBeforeForm(IndividualProtection2016.toString).bind(map)

          result.errors.size shouldBe 1
          result.error("amendedPensionsTakenBefore").get.message shouldBe errorQuestion(
            messageKey,
            IndividualProtection2016.toString
          )
        }
      }
    }
  }

}
