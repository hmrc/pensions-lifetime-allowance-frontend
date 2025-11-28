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
import forms.AmendOverseasPensionsForm
import models.amendModels.AmendOverseasPensionsModel
import models.pla.AmendProtectionLifetimeAllowanceType.IndividualProtection2016
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import testHelpers.CommonViewSpecHelper
import testHelpers.messages.amends.AmendIP16OverseasPensionsViewMessages
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import views.html.pages.amends.amendIP16OverseasPensions

class AmendIP16OverseasPensionsViewSpec extends CommonViewSpecHelper with AmendIP16OverseasPensionsViewMessages {

  implicit val formWithCSRF: FormWithCSRF = inject[FormWithCSRF]

  val view: amendIP16OverseasPensions = inject[amendIP16OverseasPensions]

  val oPensionsForm: Form[AmendOverseasPensionsModel] = AmendOverseasPensionsForm
    .amendOverseasPensionsForm(IndividualProtection2016.toString)
    .bind(Map("amendedOverseasPensions" -> "yes", "amendedOverseasPensionsAmt" -> "1234"))

  val doc: Document = Jsoup.parse(view.apply(oPensionsForm, IndividualProtection2016.toString, "open").body)

  val errorForm: Form[AmendOverseasPensionsModel] = AmendOverseasPensionsForm
    .amendOverseasPensionsForm(IndividualProtection2016.toString)
    .bind(Map("amendedOverseasPensions" -> "", "amendedOverseasPensionsAmt" -> "1234"))

  val errorDoc: Document =
    Jsoup.parse(view.apply(errorForm, IndividualProtection2016.toString, "open").body)

  "the AmendOverseasPensionsView" should {
    "have the correct title" in {
      doc.title() shouldBe plaOverseasPensionsTitleNew
    }

    "have the correct and properly formatted header" in {
      doc.select("h1.govuk-heading-xl").text shouldBe plaOverseasPensionsTitle
    }

    "have some introductory text" in {
      doc.select("p.govuk-body").first().text shouldBe plaOverseasPensionsQuestion
    }

    "have a question above the textbox" in {
      doc.select("#conditional-amendedOverseasPensions > div > label").text shouldBe plaOverseasPensionsQuestionTwo
    }

    "have a pair of yes/no buttons" in {
      doc.select("[for=amendedOverseasPensions]").text shouldBe plaBaseYes
      doc.select("input#amendedOverseasPensions").attr("type") shouldBe "radio"
      doc.select("[for=amendedOverseasPensions-2]").text shouldBe plaBaseNo
      doc.select("input#amendedOverseasPensions-2").attr("type") shouldBe "radio"
    }

    "have a valid form" in {
      val formElement = doc.select("form")

      formElement.attr("method") shouldBe "POST"
      formElement.attr("action") shouldBe controllers.routes.AmendsOverseasPensionController
        .submitAmendOverseasPensions(Strings.ProtectionTypeUrl.IndividualProtection2016, "open")
        .url
      formElement.select("legend.govuk-visually-hidden").text() shouldBe plaOverseasPensionsLegendText
    }

    "have a £ symbol present" in {
      doc.select(".govuk-input__prefix").text shouldBe "£"
    }

    "have a continue button" in {
      doc.select(".govuk-button").text shouldBe plaBaseChange
      doc.select(".govuk-button").attr("id") shouldBe "submit"
    }

    "display the correct errors appropriately" in {
      errorForm.hasErrors shouldBe true
      errorDoc.select(".govuk-error-summary__title").text shouldBe plaBaseErrorSummaryLabel
      errorDoc.select(".govuk-error-message").text shouldBe s"Error: $plaMandatoryError"
    }

    "not have errors on valid pages" in {
      oPensionsForm.hasErrors shouldBe false
      doc.select(".govuk-error-message").text shouldBe ""
    }

  }

}
