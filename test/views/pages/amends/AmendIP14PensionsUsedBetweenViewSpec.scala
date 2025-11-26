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
import forms.AmendPensionsUsedBetweenForm
import models.amendModels.AmendPensionsUsedBetweenModel
import models.pla.AmendProtectionLifetimeAllowanceType.IndividualProtection2016
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.api.data.Form
import testHelpers.CommonViewSpecHelper
import testHelpers.messages.amends.{AmendIP14PensionsTakenBetweenViewMessages, AmendIP16PensionsUsedBetweenViewMessages}
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import views.html.pages.amends.amendIP14PensionsUsedBetween

class AmendIP14PensionsUsedBetweenViewSpec
    extends CommonViewSpecHelper
    with AmendIP14PensionsTakenBetweenViewMessages
    with AmendIP16PensionsUsedBetweenViewMessages {

  implicit val formWithCSRF: FormWithCSRF = inject[FormWithCSRF]

  val view: amendIP14PensionsUsedBetween = inject[amendIP14PensionsUsedBetween]

  val form: Form[AmendPensionsUsedBetweenModel] = AmendPensionsUsedBetweenForm
    .amendPensionsUsedBetweenForm(IndividualProtection2016.toString)
    .bind(Map("amendedPensionsUsedBetweenAmt" -> "12345"))

  val doc: Document = Jsoup.parse(view.apply(form, IndividualProtection2016.toString, "open").body)

  val errorForm: Form[AmendPensionsUsedBetweenModel] = AmendPensionsUsedBetweenForm
    .amendPensionsUsedBetweenForm(IndividualProtection2016.toString)
    .bind(Map.empty[String, String])

  val errorDoc: Document = Jsoup.parse(view.apply(errorForm, IndividualProtection2016.toString, "open").body)

  "the AmendIP14PensionsUsedBetweenView" should {
    "have the correct title" in {
      doc.title() shouldBe s"$plaPensionsUsedBetweenHeader - Check your pension protections and enhancements - GOV.UK"
    }

    "have the correct and properly formatted header" in {
      doc.select(".govuk-heading-xl").text shouldBe plaPensionsUsedBetweenHeader
    }

    "have the right summary text" in {
      doc.select("summary").text shouldBe plaPensionsUsedBetweenHelp
    }

    "have the right explanatory paragraphs" in {
      doc.select("p").eq(0).text shouldBe plaPensionsUsedBetweenParaOne
      doc
        .select("#ip14-amend-pensions-used-between-help p.govuk-body")
        .eq(0)
        .text shouldBe plaIP14PensionsTakenBetweenParaTwo
      doc
        .select("#ip14-amend-pensions-used-between-help p.govuk-body")
        .eq(1)
        .text shouldBe plaPensionsUsedBetweenParaThreeNew
    }

    "have a hidden drop-down menu with the correct list values" in {
      doc.select("ol.govuk-list.govuk-list--number li").eq(0).text shouldBe plaIP14PensionsTakenBetweenStepOne
      doc.select("ol.govuk-list.govuk-list--number li").eq(1).text shouldBe plaPensionsUsedBetweenStepTwo
      doc.select("ol.govuk-list.govuk-list--number li").eq(2).text shouldBe plaPensionsUsedBetweenStepThree
      doc.select("ol.govuk-list.govuk-list--number li").eq(3).text shouldBe plaPensionsUsedBetweenStepFour
    }

    "have a help link redirecting to the right location" in {
      val linkText = doc.select("#ip14-amend-pensions-taken-between-help a").text
      plaPensionsUsedBetweenHelpLinkTextNew.contains(linkText) shouldBe true
      doc
        .select("#ip14-amend-pensions-used-between-help a")
        .attr("href") shouldBe plaPensionsUsedBetweenHelpLinkLocation
    }

    "have a valid form" in {
      val formElement: Elements = doc.select("form")

      formElement.attr("method") shouldBe "POST"
      formElement.attr("action") shouldBe controllers.routes.AmendsPensionUsedBetweenController
        .submitAmendPensionsUsedBetween(Strings.ProtectionTypeUrl.IndividualProtection2016, "open")
        .url
    }

    "have a £ symbol present" in {
      doc.select(".govuk-input__prefix").text shouldBe "£"
    }

    "have a continue button" in {
      doc.select("button").text shouldBe plaBaseChange
    }

    "display the correct errors appropriately" in {
      errorForm.hasErrors shouldBe true
      errorDoc.select(".govuk-error-summary__title").text shouldBe plaBaseErrorSummaryLabel
      errorDoc
        .select(".govuk-error-summary__body li")
        .get(0)
        .text shouldBe "Enter how much lifetime allowance you have used"
    }

    "not have errors on valid pages" in {
      form.hasErrors shouldBe false
      doc.select(".govuk-error-message").text shouldBe ""
    }
  }

}
