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

import forms.AmendCurrentPensionForm
import org.jsoup.Jsoup
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.ip2016.CurrentPensionsViewMessages
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import views.html.pages.amends.amendCurrentPensions

class AmendCurrentPensionsViewSpec extends CommonViewSpecHelper with CurrentPensionsViewMessages {

  implicit val formWithCSRF: FormWithCSRF = app.injector.instanceOf[FormWithCSRF]

  "the AmendCurrentPensionsView" should {
    val amendCurrentPensionsForm =
      AmendCurrentPensionForm.amendCurrentPensionForm("ip2016").bind(Map("amendedUKPensionAmt" -> "12000"))
    lazy val view = application.injector.instanceOf[amendCurrentPensions]
    lazy val doc  = Jsoup.parse(view.apply(amendCurrentPensionsForm, "ip2016", "open").body)

    val errorForm = AmendCurrentPensionForm.amendCurrentPensionForm("ip2016").bind(Map("amendedUKPensionAmt" -> "a"))
    lazy val errorView = application.injector.instanceOf[amendCurrentPensions]
    lazy val errorDoc  = Jsoup.parse(errorView.apply(errorForm, "ip2016", "open").body)
    lazy val form      = doc.select("form")

    "have the correct title" in {
      doc.title() shouldBe plaCurrentPensionsTitle
    }

    "have the correct and properly formatted header" in {
      doc.select("h1.govuk-heading-xl").text shouldBe plaCurrentPensionsHeading
    }

    "have some introductory text" in {
      doc.select("p").first().text shouldBe plaCurrentPensionsQuestion
    }

    "have a hidden menu with the correct values" in {
      doc.select(".govuk-details__summary-text").text shouldBe plaCurrentPensionsHiddenLink
      doc.select("#ip16-amend-current-pensions-help > div > ol > li:nth-child(1)").text shouldBe plaHiddenMenuItemOne
      doc.select("#ip16-amend-current-pensions-help > div > ol > li:nth-child(2)").text shouldBe plaHiddenMenuItemTwo
      doc.select("#ip16-amend-current-pensions-help > div > ol > li:nth-child(3)").text shouldBe plaHiddenMenuItemThree
      doc.select("#ip16-amend-current-pensions-help > div > ol > li:nth-child(4)").text shouldBe plaHiddenMenuItemFour
    }

    "have a help link redirecting to the right location" in {
      doc.select("#ip16-amend-current-pensions-help > div > p:nth-child(3)").text shouldBe plaHelpLinkCompleteMessageNew
      doc.select("#ip16-amend-current-pensions-help-link").text shouldBe plaHelpLinkNew
      doc.select("#ip16-amend-current-pensions-help-link").attr("href") shouldBe plaHelpLinkExternalReference
    }

    "has a valid form" in {
      form.attr("method") shouldBe "POST"
      form.attr("action") shouldBe controllers.routes.AmendsCurrentPensionController
        .submitAmendCurrentPension("ip2016", "open")
        .url
      form.select("div.govuk-form-group > label").text() shouldBe plaCurrentPensionsLegendText
    }

    "have a £ symbol present" in {
      doc.select(".govuk-input__prefix").text shouldBe "£"
    }

    "have a continue button" in {
      doc.select(".govuk-button").text shouldBe plaBaseChange
      doc.select(".govuk-button").attr("id") shouldBe "submit"
    }

    "display the correct errors appropriately" in {
      errorDoc.select(".govuk-error-summary__title").text shouldBe plaBaseErrorSummaryLabel
      errorDoc.select(".govuk-error-summary__list li").eq(0).text shouldBe plaErrorRequiredNumber
      errorDoc.select(".govuk-error-message").text shouldBe s"Error: $plaErrorRequiredNumber"
    }

    "not have errors on valid pages" in {
      amendCurrentPensionsForm.hasErrors shouldBe false
      doc.select(".govuk-list govuk-error-summary__list").text shouldBe ""
      doc.select(".currentPensionsAmt-error").text shouldBe ""
    }
  }

}
