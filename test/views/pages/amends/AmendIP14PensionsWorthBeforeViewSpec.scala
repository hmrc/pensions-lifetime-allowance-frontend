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
import org.jsoup.Jsoup
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.amends.AmendIP14PensionsWorthBeforeViewMessages
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import views.html.pages.amends.amendIP14PensionsWorthBefore

class AmendIP14PensionsWorthBeforeViewSpec extends CommonViewSpecHelper with AmendIP14PensionsWorthBeforeViewMessages {

  implicit val formWithCSRF: FormWithCSRF = app.injector.instanceOf[FormWithCSRF]

  "the AmendIP14PensionsWorthBeforeView" should {
    val pensionsForm = AmendPensionsWorthBeforeForm.amendPensionsWorthBeforeForm("ip2016").bind(Map("amendedPensionsWorthBefore" -> "yes",
      "amendedPensionsTakenBeforeAmt" -> "12345"))
    lazy val view = application.injector.instanceOf[amendIP14PensionsWorthBefore]
    lazy val doc = Jsoup.parse(view.apply(pensionsForm, "ip2016", "open").body)

    val errorForm =  AmendPensionsWorthBeforeForm.amendPensionsWorthBeforeForm("ip2016").bind(Map("amendedPensionsWorthBefore" -> "",
      "amendedPensionsTakenBeforeAmt" -> "12345"))
    lazy val errorView = application.injector.instanceOf[amendIP14PensionsWorthBefore]
    lazy val errorDoc = Jsoup.parse(errorView.apply(errorForm, "ip2016", "open").body)

    lazy val form = doc.select("form")

    "have the correct title" in {
      doc.title() shouldBe plaIP14PensionsWorthBeforeTitle
    }

    "have the correct and properly formatted header" in {
      doc.select("h1.govuk-heading-xl").text shouldBe plaIP14PensionsWorthBeforeHeader
    }

    "have the right summary text" in {
      doc.select(".govuk-details__summary-text").text shouldBe plaIP14PensionsWorthBeforeHelp
    }

    "have a hidden drop-down menu with the correct paragraph and  list values" in {
      doc.select("#ip14-amend-pensions-taken-before-help > div > p:nth-child(1)").text shouldBe plaIP14PensionsWorthBeforeParaOne
      doc.select("#ip14-amend-pensions-taken-before-help > div > ol > li:nth-child(1)").text shouldBe plaIP14PensionsWorthBeforeStepOne
      doc.select("#ip14-amend-pensions-taken-before-help > div > ol > li:nth-child(2)").text shouldBe plaIP14PensionsWorthBeforeStepTwo
      doc.select("#ip14-amend-pensions-taken-before-help > div > ol > li:nth-child(3)").text shouldBe plaIP14PensionsWorthBeforeStepThree
      doc.select("#ip14-amend-pensions-taken-before-help > div > p:nth-child(3)").text shouldBe plaIP14PensionsWorthBeforeParaTwo
      doc.select("#ip14-amend-pensions-taken-before-help > div > ul > li:nth-child(1)").text shouldBe plaIP14PensionsWorthBeforeStepFour
      doc.select("#ip14-amend-pensions-taken-before-help > div > ul > li:nth-child(2)").text shouldBe plaIP14PensionsWorthBeforeStepFive
    }

    "have a help link redirecting to the right location" in {
      doc.select("a#ip14-amend-pensions-taken-before-help-link").text shouldBe plaIP14PensionsWorthBeforeHelpLinkText
      doc.select("a#ip14-amend-pensions-taken-before-help-link").attr("href") shouldBe plaIP14PensionsWorthBeforeHelpLinkLocation
    }

    "have a £ symbol present" in {
      doc.select(".govuk-input__prefix").text shouldBe "£"
    }

    "have a continue button" in {
      doc.select(".govuk-button").text shouldBe plaBaseChange
      doc.select(".govuk-button").attr("id") shouldBe "submit"
    }

    "not have errors on valid pages" in {
      pensionsForm.hasErrors shouldBe false
      doc.select(".govuk-error-message").text shouldBe ""
    }
  }
}
