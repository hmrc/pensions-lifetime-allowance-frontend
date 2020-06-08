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

import models.PensionsTakenBetweenModel
import testHelpers.CommonErrorMessages
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import utils.Constants
import PensionsTakenBetweenForm._
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Lang

class PensionsTakenBetweenFormSpec extends UnitSpec with CommonErrorMessages with WithFakeApplication with MockitoSugar {
  implicit val lang: Lang = mock[Lang]

  "The PensionsTakenBetweenForm" should {
    val validMap = Map("pensionsTakenBetween" -> "yes", "pensionsTakenBetweenAmt" -> "1")

    "return a valid form with additional validation" when {

      "provided with a valid model" in {
        val model = PensionsTakenBetweenModel("yes", Some(1))
        val result = pensionsTakenBetweenForm.fill(model)

        result.data shouldBe validMap
      }

      "provided with a valid map with no amount" in {
        val map = Map("pensionsTakenBetween" -> "no")
        val result = pensionsTakenBetweenForm.bind(map)

        result.value shouldBe Some(PensionsTakenBetweenModel("no", None))
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
          result.error("pensionsTakenBetween").get.message shouldBe errorRequired
        }

        "provided with a non-numeric amount" in {
          val map = validMap.updated("pensionsTakenBetweenAmt", "a")
          val result = pensionsTakenBetweenForm.bind(map)

          result.errors.size shouldBe 1
          result.error("pensionsTakenBetweenAmt").get.message shouldBe errorReal
        }
      }

      "uses additional validation to invalidate a form" which {

        "has one error with the correct error message" when {

          "not provided with an amount with a yes answer" in {
            val map = validMap - "pensionsTakenBetweenAmt"
            val result = pensionsTakenBetweenForm.bind(map)

            result.errors.size shouldBe 1
            result.error("pensionsTakenBetweenAmt").get.message shouldBe errorMissingAmount
          }

          "provided with an amount greater than the maximum" in {
            val map = validMap.updated("pensionsTakenBetweenAmt", Constants.npsMaxCurrency.toString)
            val result = pensionsTakenBetweenForm.bind(map)

            result.errors.size shouldBe 1
            result.error("pensionsTakenBetweenAmt").get.message shouldBe errorMaximum
          }

          "provided with an amount with over two decimal places" in {
            val map = validMap.updated("pensionsTakenBetweenAmt", "0.001")
            val result = pensionsTakenBetweenForm.bind(map)

            result.errors.size shouldBe 1
            result.error("pensionsTakenBetweenAmt").get.message shouldBe errorDecimal
          }

          "provided with a negative amount" in {
            val map = validMap.updated("pensionsTakenBetweenAmt", "-0.01")
            val result = pensionsTakenBetweenForm.bind(map)

            result.errors.size shouldBe 1
            result.error("pensionsTakenBetweenAmt").get.message shouldBe errorNegative
          }
        }
      }
    }
  }
}

