/*
 * Copyright 2020 HM Revenue & Customs
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

import models.CurrentPensionsModel
import testHelpers.CommonErrorMessages
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import CurrentPensionsForm._
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Lang
import utils.Constants

class CurrentPensionsFormSpec extends UnitSpec with WithFakeApplication with CommonErrorMessages with MockitoSugar {
  implicit val lang: Lang = mock[Lang]

  "The CurrentPensionsForm" should {

    "produce a valid form " when {

      "provided with a valid model" in {
        val model = CurrentPensionsModel(Some(1))
        val result = currentPensionsForm.fill(model)

        result.data shouldBe Map("currentPensionsAmt" -> "1")
      }

      "provided with a valid form with an amount with two decimal places" in {
        val map = Map("currentPensionsAmt" -> "0.01")
        val result = currentPensionsForm.bind(map)

        result.value shouldBe Some(CurrentPensionsModel(Some(0.01)))
      }

      "provided with a valid form with a zero amount" in {
        val map = Map("currentPensionsAmt" -> "0")
        val result = currentPensionsForm.bind(map)

        result.value shouldBe Some(CurrentPensionsModel(Some(0)))
      }

      "provided with a valid form with the maximum amount" in {
        val map = Map("currentPensionsAmt" -> {Constants.npsMaxCurrency - 1}.toString)
        val result = currentPensionsForm.bind(map)

        result.value shouldBe Some(CurrentPensionsModel(Some(Constants.npsMaxCurrency - 1)))
      }
    }

    "produce an invalid form" which {

      "has only one error with the correct message" when {

        "provided with no amount value" in {
          val map = Map.empty[String, String]
          val result = currentPensionsForm.bind(map)

          result.errors.size shouldBe 1
          result.error("currentPensionsAmt").get.message shouldBe errorMissingAmount
        }

        "provided with an amount value with over two decimal places" in {
          val map = Map("currentPensionsAmt" -> "0.001")
          val result = currentPensionsForm.bind(map)

          result.errors.size shouldBe 1
          result.error("currentPensionsAmt").get.message shouldBe errorDecimal
        }

        "provided with a negative amount value" in {
          val map = Map("currentPensionsAmt" -> "-0.01")
          val result = currentPensionsForm.bind(map)

          result.errors.size shouldBe 1
          result.error("currentPensionsAmt").get.message shouldBe errorNegative
        }

        "provided with an amount value above the maximum" in {
          val map = Map("currentPensionsAmt" -> Constants.npsMaxCurrency.toString)
          val result = currentPensionsForm.bind(map)

          result.errors.size shouldBe 1
          result.error("currentPensionsAmt").get.message shouldBe errorMaximum
        }
      }
    }
  }
}
