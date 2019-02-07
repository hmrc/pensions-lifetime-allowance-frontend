/*
 * Copyright 2019 HM Revenue & Customs
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

package views.lookup

import forms.PSALookupProtectionNotificationNoForm
import org.jsoup.Jsoup
import play.api.i18n.Messages.Implicits._
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.lookup.PsaLookupProtectionNotificationNoFormSpecMessages
import views.html.pages.lookup.psa_lookup_protection_notification_no_form


class PsaLookupProtectionNotificationNoFormViewSpec extends CommonViewSpecHelper with PsaLookupProtectionNotificationNoFormSpecMessages {

  "The Psa Lookup Protection Notification No Form view" when {

    "provided with a form without errors" should {
      lazy val form = PSALookupProtectionNotificationNoForm.pnnForm.bind(Map("lifetimeAllowanceReference" -> "IP141000000000A"))
      lazy val view = psa_lookup_protection_notification_no_form(form)
      lazy val doc = Jsoup.parse(view.body)

      "have the correct title" in {
        doc.title() shouldBe titleText
      }

      "have the correct header" in {
        doc.select("h1").text() shouldBe headingText
      }

      "not have an error summary" in {
        doc.select("div.error-summary").size() shouldBe 0
      }

      "have a form" which {

        "has the correct method" in {
          doc.select("form").attr("method") shouldBe "POST"
        }

        "has the correct action" in {
          doc.select("form").attr("action") shouldBe controllers.routes.LookupController.submitProtectionNotificationNoForm().url
        }
      }

      "have a fieldset with a div without error classes" in {
        doc.select("fieldset div").first().attr("class") shouldBe "form-field"
      }

      "not have error notifications" in {
        doc.select("fieldset div.error-notification").size() shouldBe 0
      }

      "have a hint with the correct text" in {
        doc.select("p.form-field--hint").text() shouldBe hintText
      }

      "have a text input with the data pre-populated" in {
        doc.select("input[type=text]").attr("value") shouldBe "IP141000000000A"
      }

      "have a button" which {

        "has the correct text" in {
          doc.select("button").text() shouldBe buttonText
        }

        "has the correct type" in {
          doc.select("button").attr("type") shouldBe "submit"
        }
      }

      "have a back-link" which {

        "has the correct text" in {
          doc.select("a.back-link").text() shouldBe plaBaseBack
        }

        "has the correct destination" in {
          doc.select("a.back-link").attr("href") shouldBe controllers.routes.LookupController.displaySchemeAdministratorReferenceForm().url
        }
      }
    }

    "provided with a form with errors" should {
      lazy val form = PSALookupProtectionNotificationNoForm.pnnForm.bind(Map("lifetimeAllowanceReference" -> "A"))
      lazy val view = psa_lookup_protection_notification_no_form(form)
      lazy val doc = Jsoup.parse(view.body)

      "have an error summary" in {
        doc.select("div.error-summary").size() shouldBe 1
      }

      "have error notifications" in {
        doc.select("fieldset div.error-notification").size() shouldBe 1
      }

      "have a text input with the data pre-populated" in {
        doc.select("input[type=text]").attr("value") shouldBe "A"
      }
    }

    "provided with an empty form" should {
      lazy val form = PSALookupProtectionNotificationNoForm.pnnForm
      lazy val view = psa_lookup_protection_notification_no_form(form)
      lazy val doc = Jsoup.parse(view.body)

      "not have an error summary" in {
        doc.select("div.error-summary").size() shouldBe 0
      }

      "not have error notifications" in {
        doc.select("fieldset div.error-notification").size() shouldBe 0
      }

      "have a text input with no data pre-populated" in {
        doc.select("input[type=text]").attr("value") shouldBe ""
      }
    }
  }

}
