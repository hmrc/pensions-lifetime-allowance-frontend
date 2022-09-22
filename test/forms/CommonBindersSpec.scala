/*
 * Copyright 2022 HM Revenue & Customs
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
          "pso.day" -> "P",
          "pso.month" -> "5",
          "pso.year" -> "2016",
          "psoAmt" -> "0.0",
          "protectionType" -> "ip2016",
          "status" -> "status",
          "existingPSO" -> "true")


        val result = AmendPSODetailsForm.amendPsoDetailsForm.bind(testMap)

        result.errors.size shouldBe 1
        result.error("pso.day").get.message shouldBe errorReal
      }
    }
  }

  "withdrawDateValidationFormatter form binder" should {
    "return a form error" when {
      "given an invalid key" in {
        val postData = Json.obj(
          "withdrawDate.day" -> "20",
          "withdrawDate.month" -> "2",
          "incorrectKey" -> "2017"
        )
        val validatedForm = withdrawDateForm.bind(postData, Form.FromJsonMaxChars)
        assert(validatedForm.errors.size == 1)
        assert(validatedForm.errors.contains(FormError("withdrawDate.year",
          List(errorEmptyYear))))
      }

      "given an incorrect data type" in {
        val postData = Json.obj(
          "withdrawDate.day" -> "20",
          "withdrawDate.month" -> "2",
          "withdrawDate.year" -> "X"
        )
        val validatedForm = withdrawDateForm.bind(postData, Form.FromJsonMaxChars)
        assert(validatedForm.errors.size == 1)
        assert(validatedForm.errors.contains(FormError("withdrawDate.year",
          List(errorReal))))
      }
    }

    "validateCompleteDate" should {
      "return a form error" when {
        "given an future date" in {
          val postData = Json.obj(
            "withdrawDate.day" -> "20",
            "withdrawDate.month" -> "2",
            "withdrawDate.year" -> s"${LocalDate.now.getYear + 1}"
          )
          val validatedForm = withdrawDateForm.bind(postData, Form.FromJsonMaxChars)
          assert(validatedForm.errors.size == 1)
          assert(validatedForm.errors.contains(FormError("withdrawDate.day",
            List(errorFutureDate))))
        }

        "given an invalid date" in {
          val postData = Json.obj(
            "withdrawDate.day" -> "20",
            "withdrawDate.month" -> "2",
            "withdrawDate.year" -> "0000"
          )
          val validatedForm = withdrawDateForm.bind(postData, Form.FromJsonMaxChars)
          assert(validatedForm.errors.size == 1)
          assert(validatedForm.errors.contains(FormError("withdrawDate.day",
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
      testForm.withdrawDateValidationFormatter("errorLabel").unbind("testwithdrawDate.day", Some(31)) shouldBe Map("testwithdrawDate.day" -> "31")
      testForm.withdrawDateValidationFormatter("errorLabel").unbind("testwithdrawDate.month", Some(10)) shouldBe Map("testwithdrawDate.month" -> "10")
      testForm.withdrawDateValidationFormatter("errorLabel").unbind("testwithdrawDate.year", Some(2016)) shouldBe Map("testwithdrawDate.year" -> "2016")
      testForm.withdrawDateValidationFormatter("errorLabel").unbind("testwithdrawDate.year", None) shouldBe Map("testwithdrawDate.year" -> "")
    }

    "return a valid Map for the withdrawDateStringToIntFormatter" in{
      testForm.withdrawDateStringToIntFormatter.unbind("testwithdrawDate.day", Some(31)) shouldBe Map("testwithdrawDate.day" -> "31")
      testForm.withdrawDateStringToIntFormatter.unbind("testwithdrawDate.month", Some(10)) shouldBe Map("testwithdrawDate.month" -> "10")
      testForm.withdrawDateStringToIntFormatter.unbind("testwithdrawDate.year", Some(2016)) shouldBe Map("testwithdrawDate.year" -> "2016")
      testForm.withdrawDateStringToIntFormatter.unbind("testwithdrawDate.year", None) shouldBe Map("testwithdrawDate.year" -> "")
    }
  }
}

//List(FormError(withdrawDate.day,List(pla.withdraw.date-input.form.date-in-future),List()))
