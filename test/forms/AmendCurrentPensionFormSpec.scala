/*
 * Copyright 2018 HM Revenue & Customs
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
import testHelpers.CommonErrorMessages
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import utils.Constants

class AmendCurrentPensionFormSpec extends UnitSpec with WithFakeApplication with CommonErrorMessages {

  "Amend current pensions form" should {
    val validMap = Map(
      "amendedUKPensionAmt" -> "1",
      "protectionType" -> "type",
      "status" -> "status"
    )

    "create a valid form" when {

      "supplied with a valid model" in {
        val model = AmendCurrentPensionModel(Some(1), "type", "status")
        val result = amendCurrentPensionForm.fillAndValidate(model)

        result.data shouldBe validMap
      }

      "supplied with a valid map with an amount with two decimal places" in {
        val map = validMap.updated("amendedUKPensionAmt", "0.01")
        val result = amendCurrentPensionForm.bind(map)

        result.value shouldBe Some(AmendCurrentPensionModel(Some(0.01), "type", "status"))
      }

      "supplied with a valid map with zero amount" in {
        val map = validMap.updated("amendedUKPensionAmt", "0")
        val result = amendCurrentPensionForm.bind(map)

        result.value shouldBe Some(AmendCurrentPensionModel(Some(0), "type", "status"))
      }

      "supplied with a valid map with the maximum amount" in {
        val map = validMap.updated("amendedUKPensionAmt", {Constants.npsMaxCurrency - 1}.toString)
        val result = amendCurrentPensionForm.bind(map)

        result.value shouldBe Some(AmendCurrentPensionModel(Some(Constants.npsMaxCurrency - 1), "type", "status"))
      }
    }

    "create an invalid form" when {

      "not supplied with an amended amount" which {

        "has a single error with the correct error message" in {
          val map = validMap - "amendedUKPensionAmt"
          val result = amendCurrentPensionForm.bind(map)

          result.errors.size shouldBe 1
          result.error("amendedUKPensionAmt").get.message shouldBe errorMissingAmount
        }
      }

      "supplied with an empty amount" which {

        "has a single error with the correct error message" in {
          val map = validMap.updated("amendedUKPensionAmt", "")
          val result = amendCurrentPensionForm.bind(map)

          result.errors.size shouldBe 1
          result.error("amendedUKPensionAmt").get.message shouldBe errorMissingAmount
        }
      }

      "supplied with a non-numeric amount" which {
        "has a single error with the correct error message" in {
          val map = validMap.updated("amendedUKPensionAmt", "a")
          val result = amendCurrentPensionForm.bind(map)

          result.errors.size shouldBe 1
          result.error("amendedUKPensionAmt").get.message shouldBe errorReal
        }
      }

      "supplied with a negative amount" which {
        "has a single error with the correct error message" in {
          val map = validMap.updated("amendedUKPensionAmt", "-0.01")
          val result = amendCurrentPensionForm.bind(map)

          result.errors.size shouldBe 1
          result.error("amendedUKPensionAmt").get.message shouldBe errorNegative
        }
      }

      "supplied with an amount with too many decimal places" in {
        val map = validMap.updated("amendedUKPensionAmt", "0.001")
        val result = amendCurrentPensionForm.bind(map)

        result.errors.size shouldBe 1
        result.error("amendedUKPensionAmt").get.message shouldBe errorDecimal
      }

      "supplied with an amount above the maximum" in {
        val map = validMap.updated("amendedUKPensionAmt", Constants.npsMaxCurrency.toString)
        val result = amendCurrentPensionForm.bind(map)

        result.errors.size shouldBe 1
        result.error("amendedUKPensionAmt").get.message shouldBe errorMaximum
      }

      "not supplied with a type" which {

        "has a single error with the correct error message" in {
          val map = validMap - "protectionType"
          val result = amendCurrentPensionForm.bind(map)

          result.errors.size shouldBe 1
          result.error("protectionType").get.message shouldBe errorRequired
        }
      }

      "not supplied with a status" which {

        "has a single error with the correct error message" in {
          val map = validMap - "status"
          val result = amendCurrentPensionForm.bind(map)

          result.errors.size shouldBe 1
          result.error("status").get.message shouldBe errorRequired
        }
      }
    }
  }
}
