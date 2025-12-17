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

import forms.AmendPensionsWorthBeforeForm
import models.amend.value.AmendPensionsWorthBeforeModel
import models.pla.AmendableProtectionType.IndividualProtection2016
import models.pla.request.AmendProtectionRequestStatus.Open
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import testHelpers.CommonViewSpecHelper
import testHelpers.messages.amends.AmendIP16PensionsWorthBeforeViewMessages
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import views.html.pages.amends.amendIP16PensionsWorthBefore

class AmendIP16PensionsWorthBeforeViewSpec extends CommonViewSpecHelper with AmendIP16PensionsWorthBeforeViewMessages {

  implicit val formWithCSRF: FormWithCSRF = inject[FormWithCSRF]

  val view: amendIP16PensionsWorthBefore = inject[amendIP16PensionsWorthBefore]

  val form: Form[AmendPensionsWorthBeforeModel] = AmendPensionsWorthBeforeForm
    .amendPensionsWorthBeforeForm(IndividualProtection2016)
    .bind(Map("amendedPensionsWorthBefore" -> "yes", "amendedPensionsTakenBeforeAmt" -> "12345"))

  val doc: Document =
    Jsoup.parse(view.apply(form, IndividualProtection2016, Open).body)

  "the AmendPensionsWorthBeforeView" should {
    "have the correct title" in {
      doc.title() shouldBe plaPensionsWorthBeforeTitle
    }

    "have the correct and properly formatted header" in {
      doc.select("h1.govuk-heading-xl").text shouldBe plaPensionsWorthBeforeHeader
    }

    "have the right summary text" in {
      doc.select(".govuk-details__summary-text").text shouldBe plaPensionsWorthBeforeHelp
    }

    "have a hidden drop-down menu with the correct paragraph and  list values" in {
      doc
        .select("#ip16-amend-pensions-taken-before-help > div > p:nth-child(1)")
        .text shouldBe plaPensionsWorthBeforeParaOne
      doc
        .select("#ip16-amend-pensions-taken-before-help > div > ol > li:nth-child(1)")
        .text shouldBe plaPensionsWorthBeforeStepOne
      doc
        .select("#ip16-amend-pensions-taken-before-help > div > ol > li:nth-child(2)")
        .text shouldBe plaPensionsWorthBeforeStepTwo
      doc
        .select("#ip16-amend-pensions-taken-before-help > div > ol > li:nth-child(3)")
        .text shouldBe plaPensionsWorthBeforeStepThree
      doc
        .select("#ip16-amend-pensions-taken-before-help > div > p:nth-child(3)")
        .text shouldBe plaPensionsWorthBeforeParaTwo
      doc
        .select("#ip16-amend-pensions-taken-before-help > div > ul > li:nth-child(1)")
        .text shouldBe plaPensionsWorthBeforeStepFour
      doc
        .select("#ip16-amend-pensions-taken-before-help > div > ul > li:nth-child(2)")
        .text shouldBe plaPensionsWorthBeforeStepFive
    }

    "have a help link redirecting to the right location" in {
      doc.select("a#ip16-amend-pensions-worth-before-help-link").text shouldBe plaPensionsWorthBeforeHelpLinkText
      doc
        .select("a#ip16-amend-pensions-worth-before-help-link")
        .attr("href") shouldBe plaPensionsWorthBeforeHelpLinkLocation
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
