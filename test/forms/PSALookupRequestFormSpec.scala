/*
 * Copyright 2017 HM Revenue & Customs
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

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.libs.json.Json

class PSALookupRequestFormSpec extends PlaySpec with GuiceOneAppPerSuite {

  private val form = PSALookupRequestForm.pSALookupRequestForm

  "PSA lookup form" must {
    "return no errors with valid npsref IP14 data" in {
      val postData = Json.obj(
        "pensionSchemeAdministratorCheckReference" -> "PSA12345678A",
        "lifetimeAllowanceReference" -> "IP141000000000A"
      )
      val validatedForm = form.bind(postData)
      assert(validatedForm.errors.isEmpty)
    }

    "return no errors with valid npsref IP16 data" in {
      val postData = Json.obj(
        "pensionSchemeAdministratorCheckReference" -> "PSA12345678A",
        "lifetimeAllowanceReference" -> "IP161000000000A"
      )
      val validatedForm = form.bind(postData)
      assert(validatedForm.errors.isEmpty)
    }

    "return no errors with valid npsref FP16 data" in {
      val postData = Json.obj(
        "pensionSchemeAdministratorCheckReference" -> "PSA12345678A",
        "lifetimeAllowanceReference" -> "FP161000000000A"
      )
      val validatedForm = form.bind(postData)
      assert(validatedForm.errors.isEmpty)
    }

    "return no errors with valid tpss data" in {
      val postData = Json.obj(
        "pensionSchemeAdministratorCheckReference" -> "PSA12345678A",
        "lifetimeAllowanceReference" -> "A123456A"
      )
      val validatedForm = form.bind(postData)
      assert(validatedForm.errors.isEmpty)
    }

    "return errors for empty psa reference" in {
      val postData = Json.obj(
        "pensionSchemeAdministratorCheckReference" -> "",
        "lifetimeAllowanceReference" -> "A123456A"
      )
      val validatedForm = form.bind(postData)
      assert(validatedForm.errors.contains(FormError("pensionSchemeAdministratorCheckReference",
        List(Messages("psa.lookup.form.psaref.required")))))
    }

    "return errors for invalid psa reference" in {
      val postData = Json.obj(
        "pensionSchemeAdministratorCheckReference" -> "PSA",
        "lifetimeAllowanceReference" -> "A123456A"
      )
      val validatedForm = form.bind(postData)
      assert(validatedForm.errors.contains(FormError("pensionSchemeAdministratorCheckReference",
        List(Messages("psa.lookup.form.psaref.invalid")))))
    }

    "return errors for empty lta reference" in {
      val postData = Json.obj(
        "pensionSchemeAdministratorCheckReference" -> "PSA12345678A",
        "lifetimeAllowanceReference" -> ""
      )
      val validatedForm = form.bind(postData)
      assert(validatedForm.errors.contains(FormError("lifetimeAllowanceReference",
        List(Messages("psa.lookup.form.ltaref.required")))))
    }

    "return errors for invalid nps lta reference" in {
      val postData = Json.obj(
        "pensionSchemeAdministratorCheckReference" -> "PSA12345678A",
        "lifetimeAllowanceReference" -> "FP151000000000A"
      )
      val validatedForm = form.bind(postData)
      assert(validatedForm.errors.contains(FormError("lifetimeAllowanceReference",
        List(Messages("psa.lookup.form.ltaref.invalid")))))
    }

    "return errors for invalid tpss psa reference" in {
      val postData = Json.obj(
        "pensionSchemeAdministratorCheckReference" -> "PSA",
        "lifetimeAllowanceReference" -> "A123456G"
      )
      val validatedForm = form.bind(postData)
      assert(validatedForm.errors.contains(FormError("lifetimeAllowanceReference",
        List(Messages("psa.lookup.form.ltaref.invalid")))))
    }
  }
}
