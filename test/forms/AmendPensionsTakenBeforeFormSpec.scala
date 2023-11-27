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
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Lang
import testHelpers.{CommonErrorMessages, FakeApplication}
import utils.Constants

class AmendPensionsTakenBeforeFormSpec extends FakeApplication
  with CommonErrorMessages
  with MockitoSugar {
  implicit val lang: Lang = mock[Lang]

  val messageKey = "pensionsTakenBefore"
  val amountMessageKey = "pensionsWorthBefore"

  "The AmendPensionsTakenBeforeForm" should {
    val validMap = Map("amendedPensionsTakenBefore" -> "yes", "amendedPensionsTakenBeforeAmt" -> "1000.00", "protectionType" -> "type", "status" -> "status")

    "produce a valid form with additional validation" when {

      "provided with a valid model" in {
        val model = AmendPensionsTakenBeforeModel("yes", Some(1000.0), "type", "status")
        val result = amendPensionsTakenBeforeForm.fill(model)

        result.data shouldBe validMap
      }

      "provided with a valid form with no amount" in {
        val model = AmendPensionsTakenBeforeModel("no", None, "anotherType", "anotherStatus")
        val result = amendPensionsTakenBeforeForm.fill(model)

        result.data shouldBe Map("amendedPensionsTakenBefore" -> "no", "amendedPensionsTakenBeforeAmt" -> "", "protectionType" -> "anotherType", "status" -> "anotherStatus")
      }

      "provided with a valid map with the maximum amount" in {
        val map = validMap.updated("amendedPensionsTakenBeforeAmt", {
          Constants.npsMaxCurrency - 1
        }.toString)
        val result = amendPensionsTakenBeforeForm.bind(map)

        result.value shouldBe Some(AmendPensionsTakenBeforeModel("yes", Some(Constants.npsMaxCurrency - 1), "type", "status"))
      }

      "provided with a valid map with a zero amount" in {
        val map = validMap.updated("amendedPensionsTakenBeforeAmt", "0")
        val result = amendPensionsTakenBeforeForm.bind(map)

        result.value shouldBe Some(AmendPensionsTakenBeforeModel("yes", Some(0), "type", "status"))
      }

      "provided with a valid map with an amount with two decimal places" in {
        val map = validMap.updated("amendedPensionsTakenBeforeAmt", "0.01")
        val result = amendPensionsTakenBeforeForm.bind(map)

        result.value shouldBe Some(AmendPensionsTakenBeforeModel("yes", Some(0.01), "type", "status"))
      }
    }

    "produce an invalid form" which {

      "has one error with the correct error message" when {

        "not provided with a value for amendedPensionsTakenBefore" in {
          val map = validMap - "amendedPensionsTakenBefore"
          val result = amendPensionsTakenBeforeForm.bind(map)

          result.errors.size shouldBe 1
          result.error("amendedPensionsTakenBefore").get.message shouldBe errorQuestion(messageKey)
        }

        "not provided with a value for protectionType" in {
          val map = validMap - "protectionType"
          val result = amendPensionsTakenBeforeForm.bind(map)

          result.errors.size shouldBe 1
          result.error("protectionType").get.message shouldBe errorRequired
        }

        "not provided with a value for status" in {
          val map = validMap - "status"
          val result = amendPensionsTakenBeforeForm.bind(map)

          result.errors.size shouldBe 1
          result.error("status").get.message shouldBe errorRequired
        }

        "provided with an invalid value for amendedPensionsTakenBeforeAmt" in {
          val map = validMap.updated("amendedPensionsTakenBeforeAmt", "a")
          val result = amendPensionsTakenBeforeForm.bind(map)

          result.errors.size shouldBe 1
          result.errors.head.message shouldBe errorMissingAmount(amountMessageKey)
        }
      }
    }

    "use additional validation to invalidate a form" which {

      "has one error with the correct error message" when {

        "provided an answer of yes for amendedPensionsTakenBefore with no value for amendedPensionsTakenBeforeAmt" in {
          val map = validMap.updated("amendedPensionsTakenBeforeAmt", "")
          val result = amendPensionsTakenBeforeForm.bind(map)

          result.errors.size shouldBe 1
          result.errors.head.message shouldBe errorMissingAmount(amountMessageKey)
        }

        "provided an answer of yes for amendedPensionsTakenBefore with a value for amendedPensionsTakenBeforeAmt larger than the maximum" in {
          val map = validMap.updated("amendedPensionsTakenBeforeAmt", s"${Constants.npsMaxCurrency+1}")
          val result = amendPensionsTakenBeforeForm.bind(map)

          result.errors.size shouldBe 1
          result.errors.head.message shouldBe errorMaximum(amountMessageKey)
        }

        "provided an answer of yes for amendedPensionsTakenBefore with a value for amendedPensionsTakenBeforeAmt that is negative" in {
          val map = validMap.updated("amendedPensionsTakenBeforeAmt", "-0.01")
          val result = amendPensionsTakenBeforeForm.bind(map)

          result.errors.size shouldBe 1
          result.errors.head.message shouldBe errorNegative(amountMessageKey)
        }

        "provided an answer of yes for amendedPensionsTakenBefore with a value for amendedPensionsTakenBeforeAmt that has more than two decimal places" in {
          val map = validMap.updated("amendedPensionsTakenBeforeAmt", "0.001")

          val result = amendPensionsTakenBeforeForm.bind(map)

          result.errors.size shouldBe 1
          result.errors.head.message shouldBe errorDecimal(amountMessageKey)
        }
      }

      "has no errors with an answer of no for amendedPensionsTakenBefore no matter what errors are found for amendedPensionsTakenBeforeAmt" in {
        val map = validMap
          .updated("amendedPensionsTakenBefore", "no")
          .updated("amendedPensionsTakenBeforeAmt", "-0.001")
        val result = amendPensionsTakenBeforeForm.bind(map)

        result.errors.size shouldBe 0
      }
    }
  }
}

