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

import forms.AmendPensionsUsedBetweenForm
import models.amendModels.AmendPensionsUsedBetweenModel
import models.pla.AmendableProtectionType.IndividualProtection2016
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import testHelpers.CommonViewSpecHelper
import testHelpers.messages.amends.{AmendIP16PensionsTakenBetweenViewMessages, AmendIP16PensionsUsedBetweenViewMessages}
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import views.html.pages.amends.amendIP16PensionsUsedBetween

class AmendIP16PensionsUsedBetweenViewSpec
    extends CommonViewSpecHelper
    with AmendIP16PensionsTakenBetweenViewMessages
    with AmendIP16PensionsUsedBetweenViewMessages {

  implicit val formWithCSRF: FormWithCSRF = inject[FormWithCSRF]

  val view: amendIP16PensionsUsedBetween = inject[amendIP16PensionsUsedBetween]

  val form: Form[AmendPensionsUsedBetweenModel] = AmendPensionsUsedBetweenForm
    .amendPensionsUsedBetweenForm(IndividualProtection2016.toString)
    .bind(Map("amendedPensionsUsedBetween" -> "yes", "amendedPensionsUsedBetweenAmt" -> "12345"))

  val doc: Document = Jsoup.parse(view.apply(form, IndividualProtection2016.toString, "open").body)

  "the AmendPensionsUsedBetweenView" should {
    "have the correct title" in {
      doc.title() shouldBe plaPensionsUsedBetweenTitle
    }

    "have the correct and properly formatted header" in {
      doc.select("h1.govuk-heading-xl").text shouldBe plaPensionsUsedBetweenHeader
    }

    "have the right summary text" in {
      doc.select(".govuk-details__summary-text").text shouldBe plaPensionsUsedBetweenHelp
    }

    "have the right explanatory paragraphs" in {
      doc.select("#main-content > div > div > p").text shouldBe plaPensionsUsedBetweenParaOne
      doc
        .select("#ip16-amend-pensions-used-between-help > div > p:nth-child(1)")
        .text shouldBe plaPensionsUsedBetweenParaTwo
      doc
        .select("#ip16-amend-pensions-used-between-help > div > p:nth-child(3)")
        .text shouldBe plaPensionsUsedBetweenParaThreeNew
    }

    "have a hidden drop-down menu with the correct list values" in {
      doc
        .select("#ip16-amend-pensions-used-between-help > div > ol > li:nth-child(1)")
        .text shouldBe plaPensionsUsedBetweenStepOne
      doc
        .select("#ip16-amend-pensions-used-between-help > div > ol > li:nth-child(2)")
        .text shouldBe plaPensionsUsedBetweenStepTwo
      doc
        .select("#ip16-amend-pensions-used-between-help > div > ol > li:nth-child(3)")
        .text shouldBe plaPensionsUsedBetweenStepThree
      doc
        .select("#ip16-amend-pensions-used-between-help > div > ol > li:nth-child(4)")
        .text shouldBe plaPensionsUsedBetweenStepFour
    }

    "have a help link redirecting to the right location" in {
      doc.select("a#ip16-amend-pensions-used-between-help-link").text shouldBe plaPensionsUsedBetweenHelpLinkTextNew
      doc
        .select("a#ip16-amend-pensions-used-between-help-link")
        .attr("href") shouldBe plaPensionsUsedBetweenHelpLinkLocation
    }

    "have a £ symbol present" in {
      doc.select(".govuk-input__prefix").text shouldBe "£"
    }

    "have a continue button" in {
      doc.select(".govuk-button").text shouldBe plaBaseChange
      doc.select(".govuk-button").attr("id") shouldBe "submit"
    }

    "not have errors on valid pages" in {
      form.hasErrors shouldBe false
      doc.select(".govuk-error-message").text shouldBe ""
    }
  }

}
