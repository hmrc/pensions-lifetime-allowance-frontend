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

import common.Strings
import forms.AmendPensionsTakenBeforeForm
import models.amendModels.AmendPensionsTakenBeforeModel
import models.pla.AmendableProtectionType.IndividualProtection2016
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import testHelpers.CommonViewSpecHelper
import testHelpers.messages.amends.AmendIP14PensionsTakenBeforeViewMessages
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import views.html.pages.amends.amendIP16PensionsTakenBefore

class AmendIP16PensionsTakenBeforeViewSpec extends CommonViewSpecHelper with AmendIP14PensionsTakenBeforeViewMessages {

  implicit val formWithCSRF: FormWithCSRF = inject[FormWithCSRF]

  val view: amendIP16PensionsTakenBefore = inject[amendIP16PensionsTakenBefore]

  val form: Form[AmendPensionsTakenBeforeModel] = AmendPensionsTakenBeforeForm
    .amendPensionsTakenBeforeForm(IndividualProtection2016.toString)
    .bind(Map("amendedPensionsTakenBefore" -> "yes", "amendedPensionsTakenBeforeAmt" -> "12345"))

  val doc: Document = Jsoup.parse(view.apply(form, IndividualProtection2016.toString, "open").body)

  val errorForm: Form[AmendPensionsTakenBeforeModel] = AmendPensionsTakenBeforeForm
    .amendPensionsTakenBeforeForm(IndividualProtection2016.toString)
    .bind(Map("amendedPensionsTakenBefore" -> "", "amendedPensionsTakenBeforeAmt" -> "12345"))

  val errorDoc: Document = Jsoup.parse(view.apply(errorForm, IndividualProtection2016.toString, "open").body)

  "the AmendPensionsTakenBeforeView" should {
    "have the correct title" in {
      doc.title() shouldBe plaPensionsTakenBeforeTitle
    }

    "have the correct and properly formatted header" in {
      doc.getElementsByClass("govuk-heading-xl").text shouldBe plaPensionsTakenBeforeHeading
    }

    "have a valid form" in {
      val formElement = doc.select("form")

      formElement.attr("method") shouldBe "POST"
      formElement.attr("action") shouldBe controllers.routes.AmendsPensionTakenBeforeController
        .submitAmendPensionsTakenBefore(Strings.ProtectionTypeUrl.IndividualProtection2016, "open")
        .url
    }

    "have a pair of yes/no buttons" in {
      doc.select("[for=amendedPensionsTakenBefore]").text shouldBe plaBaseYes
      doc.select("input#amendedPensionsTakenBefore").attr("type") shouldBe "radio"
      doc.select("[for=amendedPensionsTakenBefore-2]").text shouldBe plaBaseNo
      doc.select("input#amendedPensionsTakenBefore-2").attr("type") shouldBe "radio"
    }

    "have a continue button" in {
      doc.select("button").text shouldBe plaBaseChange
      doc.getElementsByClass("govuk-button").attr("id") shouldBe "submit"
    }

    "display the correct errors appropriately" in {
      errorForm.hasErrors shouldBe true
      errorDoc.select(".govuk-error-summary__title").text shouldBe plaBaseErrorSummaryLabel
      errorDoc.select(".govuk-error-message").text shouldBe s"Error: $plaMandatoryError"
    }

    "not have errors on valid pages" in {
      form.hasErrors shouldBe false
      doc.select("span.error-notification").text shouldBe ""
    }
  }

}
