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

package views.pages.lookup

import forms.PSALookupSchemeAdministratorReferenceForm
import org.jsoup.Jsoup
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.lookup.PsaLookupSchemeAdminRefFormSpecMessages
import uk.gov.hmrc.play.views.html.helpers.{ErrorSummary, FormWithCSRF}
import views.html.pages.lookup.psa_lookup_scheme_admin_ref_form

class PsaLookupSchemeAdminRefFormViewSpec extends CommonViewSpecHelper with PsaLookupSchemeAdminRefFormSpecMessages {

  "The Psa Lookup Scheme Admin Ref form view" when {
    implicit val errorSummary: ErrorSummary = app.injector.instanceOf[ErrorSummary]
    implicit val formWithCSRF: FormWithCSRF = app.injector.instanceOf[FormWithCSRF]

    "provided with no errors" should {
      lazy val form = PSALookupSchemeAdministratorReferenceForm.psaRefForm.bind(Map("pensionSchemeAdministratorCheckReference" -> "PSA12345678A"))
      lazy val view = application.injector.instanceOf[psa_lookup_scheme_admin_ref_form]
      lazy val doc = Jsoup.parse(view.apply(form).body)

      "have the correct title" in {
        doc.title() shouldBe titleText
      }

      "have the correct header" in {
        doc.select("h1").text() shouldBe headingText
      }

      "have no error summary" in {
        doc.select("div.error-summary").size() shouldBe 0
      }

      "have a form" which {

        "has the correct action" in {
          doc.select("form").attr("action") shouldBe controllers.routes.LookupController.submitSchemeAdministratorReferenceForm().url
        }

        "has the correct method" in {
          doc.select("form").attr("method") shouldBe "POST"
        }
      }

      "have a fieldset with no error classes" in {
        doc.select("fieldset div.form-field--error").size() shouldBe 0
        doc.select("fieldset div.error-notification").size() shouldBe 0
      }

      "have a hint with the correct text" in {
        doc.select("p.form-field--hint").text() shouldBe hintText
      }

      "have a text input with the data pre-populated" in {
        doc.select("input[type=text]").attr("value") shouldBe "PSA12345678A"
      }

      "have a button" which {

        "has the correct text" in {
          doc.select("button").text() shouldBe plaBaseContinue
        }

        "has the correct type" in {
          doc.select("button").attr("type") shouldBe "submit"
        }
      }
    }

    "provided with errors" should {
      lazy val form = PSALookupSchemeAdministratorReferenceForm.psaRefForm.bind(Map("pensionSchemeAdministratorCheckReference" -> "A"))
      lazy val view = application.injector.instanceOf[psa_lookup_scheme_admin_ref_form]
      lazy val doc = Jsoup.parse(view.apply(form).body)

      "have an error summary" in {
        doc.select("div.error-summary").size() shouldBe 1
      }

      "should have error summary text" in {
        doc.select("h2").text() shouldBe errorSummaryText
      }

      "have a fieldset with error classes" in {
        doc.select("fieldset div.form-field--error").size() shouldBe 1
        doc.select("fieldset div.error-notification").size() shouldBe 1
      }

      "have a text input with the data pre-populated" in {
        doc.select("input[type=text]").attr("value") shouldBe "A"
      }
    }

    "provided with an empty form" should {
      lazy val form = PSALookupSchemeAdministratorReferenceForm.psaRefForm
      lazy val view = application.injector.instanceOf[psa_lookup_scheme_admin_ref_form]
      lazy val doc = Jsoup.parse(view.apply(form).body)

      "have no error summary" in {
        doc.select("div.error-summary").size() shouldBe 0
      }

      "have a fieldset with no error classes" in {
        doc.select("fieldset div.form-field--error").size() shouldBe 0
        doc.select("fieldset div.error-notification").size() shouldBe 0
      }

      "have a text input with no data pre-populated" in {
        doc.select("input[type=text]").attr("value") shouldBe ""
      }
    }
  }
}
