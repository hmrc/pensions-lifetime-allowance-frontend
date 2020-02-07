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

import play.api.data.FormError
import play.api.libs.json.Json
import WithdrawDateForm.withdrawDateForm
import testHelpers.CommonErrorMessages
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}


class WithdrawDateFormSpec extends UnitSpec with WithFakeApplication with CommonErrorMessages {

  "Withdraw date form" should {
    "return no errors with valid date" in {
      val postData = Json.obj(
        "withdrawDay" -> "20",
        "withdrawMonth" -> "2",
        "withdrawYear" -> "2017"
      )
      val validatedForm = withdrawDateForm.bind(postData)
      assert(validatedForm.errors.isEmpty)
    }
    "return errors when no day provided" in {
      val postData = Json.obj(
        "withdrawDay" -> "",
        "withdrawMonth" -> "2",
        "withdrawYear" -> "2017"
      )
      val validatedForm = withdrawDateForm.bind(postData)
      assert(validatedForm.errors.size == 1)
      assert(validatedForm.errors.contains(FormError("withdrawDay",
        List(errorEmptyDay))))
    }
    "return errors when no month provided" in {
      val postData = Json.obj(
        "withdrawDay" -> "20",
        "withdrawMonth" -> "",
        "withdrawYear" -> "2017"
      )
      val validatedForm = withdrawDateForm.bind(postData)
      assert(validatedForm.errors.size == 1)
      assert(validatedForm.errors.contains(FormError("withdrawMonth",
        List(errorEmptyMonth))))
    }
    "return errors when no year provided" in {
      val postData = Json.obj(
        "withdrawDay" -> "20",
        "withdrawMonth" -> "2",
        "withdrawYear" -> ""
      )
      val validatedForm = withdrawDateForm.bind(postData)
      assert(validatedForm.errors.size == 1)
      assert(validatedForm.errors.contains(FormError("withdrawYear",
        List(errorEmptyYear))))
    }
    "return errors when day greater than 31 provided" in {
      val postData = Json.obj(
        "withdrawDay" -> "35",
        "withdrawMonth" -> "2",
        "withdrawYear" -> "2017"
      )
      val validatedForm = withdrawDateForm.bind(postData)
      assert(validatedForm.errors.size == 1)
      assert(validatedForm.errors.contains(FormError("withdrawDay",
        List(errorHighDay))))
    }
    "return errors when month greater than 12 provided" in {
      val postData = Json.obj(
        "withdrawDay" -> "20",
        "withdrawMonth" -> "62",
        "withdrawYear" -> "2017"
      )
      val validatedForm = withdrawDateForm.bind(postData)
      assert(validatedForm.errors.size == 1)
      assert(validatedForm.errors.contains(FormError("withdrawMonth",
        List(errorHighMonth))))
    }
    "return errors when day lower than 1 provided" in {
      val postData = Json.obj(
        "withdrawDay" -> "0",
        "withdrawMonth" -> "2",
        "withdrawYear" -> "2017"
      )
      val validatedForm = withdrawDateForm.bind(postData)
      assert(validatedForm.errors.size == 1)
      assert(validatedForm.errors.contains(FormError("withdrawDay",
        List(errorLowDay))))
    }
    "return errors when month lower than 1 provided" in {
      val postData = Json.obj(
        "withdrawDay" -> "20",
        "withdrawMonth" -> "0",
        "withdrawYear" -> "2017"
      )
      val validatedForm = withdrawDateForm.bind(postData)
      assert(validatedForm.errors.size == 1)
      assert(validatedForm.errors.contains(FormError("withdrawMonth",
        List(errorLowMonth))))
    }
  }
}
