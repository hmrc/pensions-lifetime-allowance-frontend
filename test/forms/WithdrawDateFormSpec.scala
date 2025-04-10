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

import controllers.helpers.FakeRequestHelper
import forms.WithdrawDateForm.withdrawDateForm
import play.api.data.{Form, FormError}
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.Json
import testHelpers.{CommonErrorMessages, FakeApplication}

import java.time.LocalDate

class WithdrawDateFormSpec extends FakeApplication with CommonErrorMessages with FakeRequestHelper {

  val messagesApi: MessagesApi        = fakeApplication().injector.instanceOf[MessagesApi]
  implicit val testMessages: Messages = messagesApi.preferred(fakeRequest)

  val messageKey = "withdrawDate"

  "Withdraw date form" should {
    "return no errors with valid date" in {
      val postData = Json.obj(
        s"$messageKey.day"   -> "20",
        s"$messageKey.month" -> "2",
        s"$messageKey.year"  -> "2017"
      )
      val validatedForm = withdrawDateForm(LocalDate.of(2016, 2, 20)).bind(postData, Form.FromJsonMaxChars)
      assert(validatedForm.errors.isEmpty)
    }
    "return errors when no day provided" in {
      val postData = Json.obj(
        s"$messageKey.day"   -> "",
        s"$messageKey.month" -> "2",
        s"$messageKey.year"  -> "2017"
      )
      val validatedForm = withdrawDateForm(LocalDate.now()).bind(postData, Form.FromJsonMaxChars)
      assert(validatedForm.errors.size == 1)
      assert(validatedForm.errors.contains(FormError("withdrawDate.day", List(errorRequired(messageKey, ".day")))))
    }
    "return errors when no month provided" in {
      val postData = Json.obj(
        s"$messageKey.day"   -> "20",
        s"$messageKey.month" -> "",
        s"$messageKey.year"  -> "2017"
      )
      val validatedForm = withdrawDateForm(LocalDate.now()).bind(postData, Form.FromJsonMaxChars)
      assert(validatedForm.errors.size == 1)
      assert(validatedForm.errors.contains(FormError(s"$messageKey.month", List(errorRequired(messageKey, ".month")))))
    }
    "return errors when no year provided" in {
      val postData = Json.obj(
        s"$messageKey.day"   -> "20",
        s"$messageKey.month" -> "2",
        s"$messageKey.year"  -> ""
      )
      val validatedForm = withdrawDateForm(LocalDate.now()).bind(postData, Form.FromJsonMaxChars)
      assert(validatedForm.errors.size == 1)
      assert(validatedForm.errors.contains(FormError("withdrawDate.year", List(errorRequired(messageKey, ".year")))))
    }
    "return errors when day greater than 31 provided" in {
      val postData = Json.obj(
        s"$messageKey.day"   -> "35",
        s"$messageKey.month" -> "2",
        s"$messageKey.year"  -> "2017"
      )
      val validatedForm = withdrawDateForm(LocalDate.now()).bind(postData, Form.FromJsonMaxChars)
      assert(validatedForm.errors.size == 1)
      assert(validatedForm.errors.contains(FormError(s"$messageKey.day", List(errorNotReal(messageKey, ".day")))))
    }
    "return errors when month greater than 12 provided" in {
      val postData = Json.obj(
        s"$messageKey.day"   -> "20",
        s"$messageKey.month" -> "62",
        s"$messageKey.year"  -> "2017"
      )
      val validatedForm = withdrawDateForm(LocalDate.now()).bind(postData, Form.FromJsonMaxChars)
      assert(validatedForm.errors.size == 1)
      assert(validatedForm.errors.contains(FormError(s"$messageKey.month", List(errorNotReal(messageKey, ".month")))))
    }
    "return errors when day lower than 1 provided" in {
      val postData = Json.obj(
        s"$messageKey.day"   -> "0",
        s"$messageKey.month" -> "2",
        s"$messageKey.year"  -> "2017"
      )
      val validatedForm = withdrawDateForm(LocalDate.now()).bind(postData, Form.FromJsonMaxChars)
      assert(validatedForm.errors.size == 1)
      assert(validatedForm.errors.contains(FormError(s"$messageKey.day", List(errorNotReal(messageKey, ".day")))))
    }
    "return errors when month lower than 1 provided" in {
      val postData = Json.obj(
        s"$messageKey.day"   -> "20",
        s"$messageKey.month" -> "0",
        s"$messageKey.year"  -> "2017"
      )
      val validatedForm = withdrawDateForm(LocalDate.now()).bind(postData, Form.FromJsonMaxChars)
      assert(validatedForm.errors.size == 1)
      assert(validatedForm.errors.contains(FormError(s"$messageKey.month", List(errorNotReal(messageKey, ".month")))))
    }
  }

}
