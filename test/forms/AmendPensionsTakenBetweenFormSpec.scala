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

import models.amendModels.AmendPensionsTakenBetweenModel
import org.scalatestplus.play.OneAppPerSuite
import testHelpers.CommonMessages
import uk.gov.hmrc.play.test.UnitSpec
import AmendPensionsTakenBetweenForm._
import utils.Constants

class AmendPensionsTakenBetweenFormSpec extends UnitSpec with CommonMessages with OneAppPerSuite {

  "The AmendPensionsTakenBetweenForm" should {
    val validMap = Map("amendedPensionsTakenBetween" -> "yes", "amendedPensionsTakenBetweenAmt" -> "1000.0", "protectionType" -> "type", "status" -> "status")

    "produce a valid form with additional validation" when {

      "provided with a valid model" in {
        val model = AmendPensionsTakenBetweenModel("yes", Some(1000.0), "type", "status")
        val result = amendPensionsTakenBetweenForm.fill(model)

        result.data shouldBe validMap
      }

      "provided with a valid map with no amount" in {
        val map = Map("amendedPensionsTakenBetween" -> "no", "protectionType" -> "anotherType", "status" -> "anotherStatus")
        val result = amendPensionsTakenBetweenForm.bind(map)

        result.value shouldBe Some(AmendPensionsTakenBetweenModel("no", None, "anotherType", "anotherStatus"))
      }

      "provided with a valid map with an amount equal to the maximum" in {
        val map = validMap.updated("amendedPensionsTakenBetweenAmt", {Constants.npsMaxCurrency - 1}.toString)
        val result = amendPensionsTakenBetweenForm.bind(map)

        result.value shouldBe Some(AmendPensionsTakenBetweenModel("yes", Some(Constants.npsMaxCurrency - 1), "type", "status"))
      }

      "provided with a valid map with an amount equal zero" in {
        val map = validMap.updated("amendedPensionsTakenBetweenAmt", "0")
        val result = amendPensionsTakenBetweenForm.bind(map)

        result.value shouldBe Some(AmendPensionsTakenBetweenModel("yes", Some(0), "type", "status"))
      }

      "provided with a valid map with an amount with two decimal places" in {
        val map = validMap.updated("amendedPensionsTakenBetweenAmt", "0.01")
        val result = amendPensionsTakenBetweenForm.bind(map)

        result.value shouldBe Some(AmendPensionsTakenBetweenModel("yes", Some(0.01), "type", "status"))
      }
    }

    "produce an invalid form" which {

      "has one error with the correct error message" when {

        "not provided with a value for amendedPensionsTakenBetween" in {
          val map = validMap - "amendedPensionsTakenBetween"
          val result = amendPensionsTakenBetweenForm.bind(map)

          result.errors.size shouldBe 1
          result.error("amendedPensionsTakenBetween").get.message shouldBe errorRequired
        }

        "not provided with a value for protectionType" in {
          val map = validMap - "protectionType"
          val result = amendPensionsTakenBetweenForm.bind(map)

          result.errors.size shouldBe 1
          result.error("protectionType").get.message shouldBe errorRequired
        }

        "not provided with a value for status" in {
          val map = validMap - "status"
          val result = amendPensionsTakenBetweenForm.bind(map)

          result.errors.size shouldBe 1
          result.error("status").get.message shouldBe errorRequired
        }

        "provided with an invalid value for amendedPensionsTakenBetweenAmt" in {
          val map = validMap.updated("amendedPensionsTakenBetweenAmt", "a")
          val result = amendPensionsTakenBetweenForm.bind(map)

          result.errors.size shouldBe 1
          result.error("amendedPensionsTakenBetweenAmt").get.message shouldBe errorReal
        }
      }
    }

    "use additional validation to invalidate a form" which {

      "has one error with the correct error message" when {

        "provided an answer of yes for amendedPensionsTakenBetween with no value for amendedPensionsTakenBetweenAmt" in {
          val map = validMap - "amendedPensionsTakenBetweenAmt"
          val result = amendPensionsTakenBetweenForm.bind(map)

          result.errors.size shouldBe 1
          result.error("amendedPensionsTakenBetweenAmt").get.message shouldBe errorMissingAmount
        }

        "provided an answer of yes for amendedPensionsTakenBetween with a value for amendedPensionsTakenBetweenAmt larger than the maximum" in {
          val map = validMap.updated("amendedPensionsTakenBetweenAmt", Constants.npsMaxCurrency.toString)
          val result = amendPensionsTakenBetweenForm.bind(map)

          result.errors.size shouldBe 1
          result.error("amendedPensionsTakenBetweenAmt").get.message shouldBe errorMaximum
        }

        "provided an answer of yes for amendedPensionsTakenBetween with a value for amendedPensionsTakenBetweenAmt that is negative" in {
          val map = validMap.updated("amendedPensionsTakenBetweenAmt", "-0.01")
          val result = amendPensionsTakenBetweenForm.bind(map)

          result.errors.size shouldBe 1
          result.error("amendedPensionsTakenBetweenAmt").get.message shouldBe errorNegative
        }

        "provided an answer of yes for amendedPensionsTakenBetween with a value for amendedPensionsTakenBetweenAmt that has more than two decimal places" in {
          val map = validMap.updated("amendedPensionsTakenBetweenAmt", "0.001")
          val result = amendPensionsTakenBetweenForm.bind(map)

          result.errors.size shouldBe 1
          result.error("amendedPensionsTakenBetweenAmt").get.message shouldBe errorDecimal
        }
      }

      "has no errors with an answer of no for amendedPensionsTakenBetween no matter what errors are found for amendedPensionsTakenBetweenAmt" in {
        val map = validMap
          .updated("amendedPensionsTakenBetweenAmt", "-0.001")
          .updated("amendedPensionsTakenBetween", "no")
        val result = amendPensionsTakenBetweenForm.bind(map)

        result.errors.size shouldBe 0
      }
    }
  }
}
