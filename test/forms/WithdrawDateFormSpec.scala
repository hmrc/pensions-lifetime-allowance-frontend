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
import play.api.data.{Form, FormError}
import play.api.libs.json.Json
import testHelpers.{CommonErrorMessages, FakeApplication}

class WithdrawDateFormSpec extends FakeApplication with CommonErrorMessages {

  "Withdraw date form" should {
    "return no errors with valid date" in {
      val postData = Json.obj(
        "withdrawDate.day" -> "20",
        "withdrawDate.month" -> "2",
        "withdrawDate.year" -> "2017"
      )
      val validatedForm = withdrawDateForm.bind(postData, Form.FromJsonMaxChars)
      assert(validatedForm.errors.isEmpty)
    }
    "return errors when no day provided" in {
      val postData = Json.obj(
        "withdrawDate.day" -> "",
        "withdrawDate.month" -> "2",
        "withdrawDate.year" -> "2017"
      )
      val validatedForm = withdrawDateForm.bind(postData, Form.FromJsonMaxChars)
      assert(validatedForm.errors.size == 1)
      assert(validatedForm.errors.contains(FormError("withdrawDate.day",
        List(errorEmptyDay))))
    }
    "return errors when no month provided" in {
      val postData = Json.obj(
        "withdrawDate.day" -> "20",
        "withdrawDate.month" -> "",
        "withdrawDate.year" -> "2017"
      )
      val validatedForm = withdrawDateForm.bind(postData, Form.FromJsonMaxChars)
      assert(validatedForm.errors.size == 1)
      assert(validatedForm.errors.contains(FormError("withdrawDate.month",
        List(errorEmptyMonth))))
    }
    "return errors when no year provided" in {
      val postData = Json.obj(
        "withdrawDate.day" -> "20",
        "withdrawDate.month" -> "2",
        "withdrawDate.year" -> ""
      )
      val validatedForm = withdrawDateForm.bind(postData, Form.FromJsonMaxChars)
      assert(validatedForm.errors.size == 1)
      assert(validatedForm.errors.contains(FormError("withdrawDate.year",
        List(errorEmptyYear))))
    }
    "return errors when day greater than 31 provided" in {
      val postData = Json.obj(
        "withdrawDate.day" -> "35",
        "withdrawDate.month" -> "2",
        "withdrawDate.year" -> "2017"
      )
      val validatedForm = withdrawDateForm.bind(postData, Form.FromJsonMaxChars)
      assert(validatedForm.errors.size == 1)
      assert(validatedForm.errors.contains(FormError("withdrawDate.day",
        List(errorHighDay))))
    }
    "return errors when month greater than 12 provided" in {
      val postData = Json.obj(
        "withdrawDate.day" -> "20",
        "withdrawDate.month" -> "62",
        "withdrawDate.year" -> "2017"
      )
      val validatedForm = withdrawDateForm.bind(postData, Form.FromJsonMaxChars)
      assert(validatedForm.errors.size == 1)
      assert(validatedForm.errors.contains(FormError("withdrawDate.month",
        List(errorHighMonth))))
    }
    "return errors when day lower than 1 provided" in {
      val postData = Json.obj(
        "withdrawDate.day" -> "0",
        "withdrawDate.month" -> "2",
        "withdrawDate.year" -> "2017"
      )
      val validatedForm = withdrawDateForm.bind(postData, Form.FromJsonMaxChars)
      assert(validatedForm.errors.size == 1)
      assert(validatedForm.errors.contains(FormError("withdrawDate.day",
        List(errorLowDay))))
    }
    "return errors when month lower than 1 provided" in {
      val postData = Json.obj(
        "withdrawDate.day" -> "20",
        "withdrawDate.month" -> "0",
        "withdrawDate.year" -> "2017"
      )
      val validatedForm = withdrawDateForm.bind(postData, Form.FromJsonMaxChars)
      assert(validatedForm.errors.size == 1)
      assert(validatedForm.errors.contains(FormError("withdrawDate.month",
        List(errorLowMonth))))
    }
  }
}
