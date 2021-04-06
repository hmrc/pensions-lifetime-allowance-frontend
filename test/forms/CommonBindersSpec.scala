/*
 * Copyright 2021 HM Revenue & Customs
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

import forms.WithdrawDateForm.withdrawDateForm
import org.joda.time.LocalDate
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.{Form, FormError}
import play.api.i18n.Lang
import play.api.libs.json.Json
import testHelpers.{FakeApplication, PSODetailsMessages}

class CommonBindersSpec extends FakeApplication with PSODetailsMessages with MockitoSugar {
  implicit val lang: Lang = mock[Lang]
  object testForm extends CommonBinders

  "stringToOptionalIntFormatter form binder" should{
    "return a form error" when{
      "given an incorrect data type" in{
        val testMap = Map(
          "psoDay" -> "P",
          "psoMonth" -> "5",
          "psoYear" -> "2016",
          "psoAmt" -> "0.0",
          "protectionType" -> "ip2016",
          "status" -> "status",
          "existingPSO" -> "true")


        val result = AmendPSODetailsForm.amendPsoDetailsForm.bind(testMap)

        result.errors.size shouldBe 1
        result.error("psoDay").get.message shouldBe errorReal
      }
    }
  }

  "withdrawDateValidationFormatter form binder" should {
    "return a form error" when {
      "given an invalid key" in {
        val postData = Json.obj(
          "withdrawDay" -> "20",
          "withdrawMonth" -> "2",
          "incorrectKey" -> "2017"
        )
        val validatedForm = withdrawDateForm.bind(postData, Form.FromJsonMaxChars)
        assert(validatedForm.errors.size == 1)
        assert(validatedForm.errors.contains(FormError("withdrawYear",
          List(errorEmptyYear))))
      }

      "given an incorrect data type" in {
        val postData = Json.obj(
          "withdrawDay" -> "20",
          "withdrawMonth" -> "2",
          "withdrawYear" -> "X"
        )
        val validatedForm = withdrawDateForm.bind(postData, Form.FromJsonMaxChars)
        assert(validatedForm.errors.size == 1)
        assert(validatedForm.errors.contains(FormError("withdrawYear",
          List(errorReal))))
      }
    }

    "validateCompleteDate" should {
      "return a form error" when {
        "given an future date" in {
          val postData = Json.obj(
            "withdrawDay" -> "20",
            "withdrawMonth" -> "2",
            "withdrawYear" -> s"${LocalDate.now.getYear + 1}"
          )
          val validatedForm = withdrawDateForm.bind(postData, Form.FromJsonMaxChars)
          assert(validatedForm.errors.size == 1)
          assert(validatedForm.errors.contains(FormError("withdrawDay",
            List(errorFutureDate))))
        }

        "given an invalid date" in {
          val postData = Json.obj(
            "withdrawDay" -> "20",
            "withdrawMonth" -> "2",
            "withdrawYear" -> "0000"
          )
          val validatedForm = withdrawDateForm.bind(postData, Form.FromJsonMaxChars)
          assert(validatedForm.errors.size == 1)
          assert(validatedForm.errors.contains(FormError("withdrawDay",
            List(errorDate))))
        }
      }
    }
  }

  "Testing form unbinds" should{
    "return a valid Map for the optionalBigDecimalFormatter" in{
      testForm.optionalBigDecimalFormatter.unbind("testKey",Some(BigDecimal(100))) shouldBe Map("testKey" -> "100")
      testForm.optionalBigDecimalFormatter.unbind("testKey",None) shouldBe Map()

    }

    "return a valid Map for the withdrawDateValidationFormatter" in{
      testForm.withdrawDateValidationFormatter("errorLabel").unbind("testWithdrawDay", Some(31)) shouldBe Map("testWithdrawDay" -> "31")
      testForm.withdrawDateValidationFormatter("errorLabel").unbind("testWithdrawMonth", Some(10)) shouldBe Map("testWithdrawMonth" -> "10")
      testForm.withdrawDateValidationFormatter("errorLabel").unbind("testWithdrawYear", Some(2016)) shouldBe Map("testWithdrawYear" -> "2016")
      testForm.withdrawDateValidationFormatter("errorLabel").unbind("testWithdrawYear", None) shouldBe Map("testWithdrawYear" -> "")
    }

    "return a valid Map for the withdrawDateStringToIntFormatter" in{
      testForm.withdrawDateStringToIntFormatter.unbind("testWithdrawDay", Some(31)) shouldBe Map("testWithdrawDay" -> "31")
      testForm.withdrawDateStringToIntFormatter.unbind("testWithdrawMonth", Some(10)) shouldBe Map("testWithdrawMonth" -> "10")
      testForm.withdrawDateStringToIntFormatter.unbind("testWithdrawYear", Some(2016)) shouldBe Map("testWithdrawYear" -> "2016")
      testForm.withdrawDateStringToIntFormatter.unbind("testWithdrawYear", None) shouldBe Map("testWithdrawYear" -> "")
    }
  }
}
