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

class PNNFormSpec extends PlaySpec with GuiceOneAppPerSuite {

  private val form = PSALookupProtectionNotificationNoForm.pnnForm

  "PNN ref form" must {
    "return no errors with valid nps reference" in {
      val postData = Json.obj(
        "lifetimeAllowanceReference" -> "IP141000000000A"
      )
      val validatedForm = form.bind(postData)

      assert(validatedForm.errors.isEmpty)
    }

    "return no errors with valid tpss reference" in {
      val postData = Json.obj(
        "lifetimeAllowanceReference" -> "A123456A"
      )
      val validatedForm = form.bind(postData)

      assert(validatedForm.errors.isEmpty)
    }

    "return no errors with valid lowercase letters for nps reference" in {
      val postData = Json.obj(
        "lifetimeAllowanceReference" -> "ip141000000000a"
      )
      val validatedForm = form.bind(postData)

      assert(validatedForm.errors.isEmpty)
    }

    "return no errors with valid lowercase letters for tpss reference" in {
      val postData = Json.obj(
        "lifetimeAllowanceReference" -> "a123456a"
      )
      val validatedForm = form.bind(postData)

      assert(validatedForm.errors.isEmpty)
    }


    "return errors when no data provided" in {
      val postData = Json.obj(
        "lifetimeAllowanceReference" -> ""
      )
      val validatedForm = form.bind(postData)

      assert(validatedForm.errors.size == 1)
      assert(validatedForm.errors.contains(FormError("lifetimeAllowanceReference",
        List(Messages("psa.lookup.form.pnn.required")))))
    }

    "return errors with invalid psa reference" in {
      val postData = Json.obj(
        "lifetimeAllowanceReference" -> "ip14"
      )
      val validatedForm = form.bind(postData)

      assert(validatedForm.errors.size == 1)
      assert(validatedForm.errors.contains(FormError("lifetimeAllowanceReference",
        List(Messages("psa.lookup.form.pnn.invalid")))))
    }
  }
}
