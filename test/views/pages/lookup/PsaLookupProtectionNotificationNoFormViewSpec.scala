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

package views.pages.lookup

import forms.PSALookupProtectionNotificationNoForm
import org.jsoup.Jsoup
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.lookup.PsaLookupProtectionNotificationNoFormSpecMessages
import views.html.pages.lookup.psa_lookup_protection_notification_no_form

class PsaLookupProtectionNotificationNoFormViewSpec extends CommonViewSpecHelper with PsaLookupProtectionNotificationNoFormSpecMessages {

  "The Psa Lookup Protection Notification No Form view" when {

    "provided with a form without errors" should {
      lazy val form = PSALookupProtectionNotificationNoForm.pnnForm.bind(Map("lifetimeAllowanceReference" -> "IP141000000000A"))
      lazy val view = application.injector.instanceOf[psa_lookup_protection_notification_no_form]
      lazy val doc = Jsoup.parse(view.apply(form).body)

      "have the correct title" in {
        doc.title() shouldBe titleText
      }

      "have the correct header" in {
        doc.select("h1").text() shouldBe headingText
      }

      "not have an error summary" in {
        doc.select("div.govuk-error-summary").size() shouldBe 0
      }

      "have a form" which {

        "has the correct method" in {
          doc.select("form").attr("method") shouldBe "POST"
        }

        "has the correct action" in {
          doc.select("form").attr("action") shouldBe controllers.routes.LookupProtectionNotificationController.submitProtectionNotificationNoForm.url
        }
      }

      "not have error notifications" in {
        doc.select("#lifetimeAllowanceReference-error").size() shouldBe 0
      }

      "have a hint with the correct text" in {
        doc.select("div.govuk-hint").text() shouldBe hintText
      }

      "have a text input with the data pre-populated" in {
        doc.select("input[type=text]").attr("value") shouldBe "IP141000000000A"
      }

      "have a button" which {

        "has the correct text" in {
          doc.select("button.govuk-button").text() shouldBe buttonText
        }

        "has the correct type" in {
          doc.select("button.govuk-button").attr("id") shouldBe "submit"
        }
      }
    }

    "provided with a form with errors" should {
      lazy val form = PSALookupProtectionNotificationNoForm.pnnForm.bind(Map("lifetimeAllowanceReference" -> "A"))
      lazy val view = application.injector.instanceOf[psa_lookup_protection_notification_no_form]
      lazy val doc = Jsoup.parse(view.apply(form).body)

      "have an error summary" in {
        doc.select("div.govuk-error-summary").size() shouldBe 1
      }

      "should have error summary text" in {
        doc.select("h2.govuk-error-summary__title").text() shouldBe errorSummaryText
      }

      "have error notifications" in {
        doc.select(".govuk-error-message").size() shouldBe 1
      }

      "have a text input with the data pre-populated" in {
        doc.select("input[type=text]").attr("value") shouldBe "A"
      }
    }

    "provided with an empty form" should {
      lazy val form = PSALookupProtectionNotificationNoForm.pnnForm
      lazy val view = application.injector.instanceOf[psa_lookup_protection_notification_no_form]
      lazy val doc = Jsoup.parse(view.apply(form).body)

      "not have an error summary" in {
        doc.select("div.govuk-error-summary").size() shouldBe 0
      }

      "not have error notifications" in {
        doc.select(".govuk-error-message").size() shouldBe 0
      }

      "have a text input with no data pre-populated" in {
        doc.select("input[type=text]").attr("value") shouldBe ""
      }
    }
  }

}
