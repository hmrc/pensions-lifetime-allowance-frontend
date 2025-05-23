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

import forms.AmendOverseasPensionsForm._
import models.amendModels.AmendOverseasPensionsModel
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Lang
import testHelpers.{CommonErrorMessages, FakeApplication}
import utils.Constants

class AmendOverseasPensionsFormSpec extends FakeApplication with CommonErrorMessages with MockitoSugar {
  implicit val lang: Lang = mock[Lang]

  val messageKey = "overseasPensions"

  "The AmendOverseasPensionsForm" should {
    val validMap = Map("amendedOverseasPensions" -> "yes", "amendedOverseasPensionsAmt" -> "1000.00")

    "produce a valid form with additional validation" when {

      "provided with a valid model" in {
        val model  = AmendOverseasPensionsModel("yes", Some(1000.0))
        val result = amendOverseasPensionsForm("ip2016").fill(model)

        result.data shouldBe validMap
      }

      "provided with a valid map with no amount" in {
        val map    = Map("amendedOverseasPensions" -> "no", "amendedOverseasPensionsAmt" -> "")
        val result = amendOverseasPensionsForm("ip2016").bind(map)

        result.value shouldBe Some(AmendOverseasPensionsModel("no", None))
      }

      "provided with a valid map with the maximum amount" in {
        val map = validMap.updated(
          "amendedOverseasPensionsAmt", {
            Constants.npsMaxCurrency - 1
          }.toString
        )
        val result = amendOverseasPensionsForm("ip2016").bind(map)

        result.value shouldBe Some(AmendOverseasPensionsModel("yes", Some(Constants.npsMaxCurrency - 1)))
      }

      "provided with a valid map with a zero amount" in {
        val map    = validMap.updated("amendedOverseasPensionsAmt", "0")
        val result = amendOverseasPensionsForm("ip2016").bind(map)

        result.value shouldBe Some(AmendOverseasPensionsModel("yes", Some(0)))
      }

      "provided with a valid map with an amount with two decimal places" in {
        val map    = validMap.updated("amendedOverseasPensionsAmt", "0.01")
        val result = amendOverseasPensionsForm("ip2016").bind(map)

        result.value shouldBe Some(AmendOverseasPensionsModel("yes", Some(0.01)))
      }
    }

    "produce an invalid form".which {

      "has one error with the correct error message" when {

        "not provided with a value for amendedOverseasPensions" in {
          val map    = validMap - "amendedOverseasPensions"
          val result = amendOverseasPensionsForm("ip2016").bind(map)

          result.errors.size shouldBe 1
          result.error("amendedOverseasPensions").get.message shouldBe errorQuestion(messageKey, "ip2016")
        }

        "provided with an invalid value for amendedOverseasPensionsAmt" in {
          val map    = validMap.updated("amendedOverseasPensionsAmt", "a")
          val result = amendOverseasPensionsForm("ip2016").bind(map)

          result.errors.size shouldBe 1
          result.errors.head.message shouldBe errorReal(messageKey, "ip2016")
        }
      }
    }

    "use additional validation to invalidate a form".which {

      "has one error with the correct error message" when {

        "provided an answer of yes for amendedOverseasPensions with no value for amendedOverseasPensionsAmt" in {
          val map    = validMap.updated("amendedOverseasPensionsAmt", "")
          val result = amendOverseasPensionsForm("ip2016").bind(map)

          result.errors.size shouldBe 1
          result.errors.head.message shouldBe errorMissingAmount(messageKey, "ip2016")
        }

        "provided an answer of yes for amendedOverseasPensions with a value for amendedOverseasPensionsAmt larger than the maximum" in {
          val map    = validMap.updated("amendedOverseasPensionsAmt", s"${Constants.npsMaxCurrency + 1}")
          val result = amendOverseasPensionsForm("ip2016").bind(map)

          result.errors.size shouldBe 1
          result.errors.head.message shouldBe errorMaximum(messageKey, "ip2016")
        }

        "provided an answer of yes for amendedOverseasPensions with a value for amendedOverseasPensionsAmt that is negative" in {
          val map    = validMap.updated("amendedOverseasPensionsAmt", "-0.01")
          val result = amendOverseasPensionsForm("ip2016").bind(map)

          result.errors.size shouldBe 1
          result.errors.head.message shouldBe errorNegative(messageKey, "ip2016")
        }

        "provided an answer of yes for amendedOverseasPensions with a value for amendedOverseasPensionsAmt that has more than two decimal places" in {
          val map    = validMap.updated("amendedOverseasPensionsAmt", "0.001")
          val result = amendOverseasPensionsForm("ip2016").bind(map)

          result.errors.size shouldBe 1
          result.errors.head.message shouldBe errorDecimal(messageKey, "ip2016")
        }
      }

      "has no errors with an answer of no for amendedOverseasPensions no matter what errors are found for amendedOverseasPensionsAmt" in {
        val map = validMap
          .updated("amendedOverseasPensions", "no")
          .updated("amendedOverseasPensionsAmt", "-0.001")
        val result = amendOverseasPensionsForm("ip2016").bind(map)

        result.errors.size shouldBe 0
      }
    }
  }

}
