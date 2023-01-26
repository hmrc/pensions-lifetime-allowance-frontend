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

import play.api.data.{Form, FormError}
import play.api.i18n.Messages
import play.api.libs.json.Json
import testHelpers.FakeApplication

import javax.inject.Inject

class PSARefFormSpec @Inject()(implicit val messages: Messages) extends FakeApplication {

  private val form = PSALookupSchemeAdministratorReferenceForm.psaRefForm

  "PSA ref form" must {
    "return no errors with valid data" in {
      val postData = Json.obj(
        "pensionSchemeAdministratorCheckReference" -> "PSA12345678A"
      )
      val validatedForm = form.bind(postData, Form.FromJsonMaxChars)

      assert(validatedForm.errors.isEmpty)
    }

    "return no errors with valid lowercase letters" in {
      val postData = Json.obj(
        "pensionSchemeAdministratorCheckReference" -> "psa12345678a"
      )
      val validatedForm = form.bind(postData, Form.FromJsonMaxChars)

      assert(validatedForm.errors.isEmpty)
    }

    "return errors when no data provided" in {
      val postData = Json.obj(
        "pensionSchemeAdministratorCheckReference" -> ""
      )
      val validatedForm = form.bind(postData, Form.FromJsonMaxChars)

      assert(validatedForm.errors.size == 1)
      assert(validatedForm.errors.contains(FormError("pensionSchemeAdministratorCheckReference",
        List(Messages("psa.lookup.form.psaref.required")))))
    }

    "return errors with invalid psa reference" in {
      val postData = Json.obj(
        "pensionSchemeAdministratorCheckReference" -> "psa"
      )
      val validatedForm = form.bind(postData, Form.FromJsonMaxChars)

      assert(validatedForm.errors.size == 1)
      assert(validatedForm.errors.contains(FormError("pensionSchemeAdministratorCheckReference",
        List(Messages("psa.lookup.form.psaref.invalid")))))
    }
  }
}
