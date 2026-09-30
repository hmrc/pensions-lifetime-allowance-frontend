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

package views.pages.amends

import forms.RemovePensionSharingForm
import models.amend.value.RemovePensionSharingModel
import models.pla.AmendableProtectionType.IndividualProtection2016
import models.pla.request.AmendProtectionRequestStatus.Open
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import testHelpers.CommonViewSpecHelper
import testHelpers.messages.amends.RemovePensionSharingOrderViewMessages
import views.html.pages.amends.removePensionSharingOrder

class RemovePensionSharingOrderViewSpec extends CommonViewSpecHelper with RemovePensionSharingOrderViewMessages {

  private val view: removePensionSharingOrder = inject[removePensionSharingOrder]

  private val form: Form[RemovePensionSharingModel] = RemovePensionSharingForm.removePensionSharingForm(IndividualProtection2016)
    .bind(Map("removePensionSharing" -> "yes"))

  private val doc: Document = Jsoup.parse(view.apply(form, IndividualProtection2016, Open).body)

  private val errorForm: Form[RemovePensionSharingModel] = RemovePensionSharingForm
    .removePensionSharingForm(IndividualProtection2016)
    .bind(Map("removePensionSharing" -> ""))

  private val errorDoc: Document = Jsoup.parse(view.apply(errorForm, IndividualProtection2016, Open).body)

  "the RemovePensionSharingView" should {
    "have the correct title" in {
      doc.title() shouldBe plaRemovePensionSharingtitle
    }

    "have the correct and properly formatted header" in {
      doc.select("h1.govuk-fieldset__heading").text shouldBe plaRemovePensionSharing
    }

    "have a valid form" in {
      val formElement = doc.select("form")

      formElement.attr("method") shouldBe "POST"
      formElement.attr("action") shouldBe controllers.routes.removePensionSharingOrderController.submitRemovePso(IndividualProtection2016, Open)
        .url
    }

    "have a pair of yes/no buttons" in {
      doc.select("[for=removePensionSharing]").text shouldBe plaBaseYes
      doc.select("input#removePensionSharing").attr("type") shouldBe "radio"
      doc.select("[for=removePensionSharing-2]").text shouldBe plaBaseNo
      doc.select("input#removePensionSharing-2").attr("type") shouldBe "radio"
    }

    "have a continue button" in {
      doc.select("button").text shouldBe plaContinue
      doc.getElementsByClass("govuk-button").attr("id") shouldBe "submit"
    }

//    "display the correct errors appropriately" in {
//      errorForm.hasErrors shouldBe true
//      errorDoc.select(".govuk-error-summary__title").text shouldBe plaBaseErrorSummaryLabel
//      errorDoc.select(".govuk-error-message").text shouldBe s"Error: $plaRemovePensionMandatoryError"
//    }

    "not have errors on valid pages" in {
      form.hasErrors shouldBe false
      doc.select("span.error-notification").text shouldBe ""
    }
  }

}
