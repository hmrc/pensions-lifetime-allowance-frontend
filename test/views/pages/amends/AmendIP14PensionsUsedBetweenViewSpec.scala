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
import models.pla.AmendProtectionLifetimeAllowanceType.IndividualProtection2016
import org.jsoup.Jsoup
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.amends.AmendIP14PensionsTakenBetweenViewSpecMessages
import testHelpers.ViewSpecHelpers.ip2016.PensionsUsedBetweenViewMessages
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import views.html.pages.amends.amendIP14PensionsUsedBetween

class AmendIP14PensionsUsedBetweenViewSpec
    extends CommonViewSpecHelper
    with AmendIP14PensionsTakenBetweenViewSpecMessages
    with PensionsUsedBetweenViewMessages {

  implicit val formWithCSRF: FormWithCSRF = app.injector.instanceOf[FormWithCSRF]

  "the AmendIP14PensionsUsedBetweenView" should {
    val pensionsForm = AmendPensionsUsedBetweenForm
      .amendPensionsUsedBetweenForm(IndividualProtection2016.toString)
      .bind(Map("amendedPensionsUsedBetweenAmt" -> "12345"))
    lazy val view = app.injector.instanceOf[amendIP14PensionsUsedBetween]
    lazy val doc  = Jsoup.parse(view.apply(pensionsForm, IndividualProtection2016.toString, "open").body)

    val errorForm = AmendPensionsUsedBetweenForm
      .amendPensionsUsedBetweenForm(IndividualProtection2016.toString)
      .bind(Map.empty[String, String])
    lazy val errorView = app.injector.instanceOf[amendIP14PensionsUsedBetween]
    lazy val errorDoc  = Jsoup.parse(errorView.apply(errorForm, IndividualProtection2016.toString, "open").body)

    lazy val form = doc.select("form")

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
      form.attr("method") shouldBe "POST"
      form.attr("action") shouldBe controllers.routes.AmendsPensionUsedBetweenController
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
      pensionsForm.hasErrors shouldBe false
      doc.select(".govuk-error-message").text shouldBe ""
    }
  }

}
