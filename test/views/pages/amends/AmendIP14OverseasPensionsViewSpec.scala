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

import forms.AmendOverseasPensionsForm
import org.jsoup.Jsoup
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.amends.AmendIP14OverseasPensionsViewSpecMessages
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import views.html.pages.amends.amendIP14OverseasPensions

class AmendIP14OverseasPensionsViewSpec extends CommonViewSpecHelper with AmendIP14OverseasPensionsViewSpecMessages{

  implicit val formWithCSRF: FormWithCSRF = app.injector.instanceOf[FormWithCSRF]

  "the AmendIP14OverseasPensionsView" should{
    val oPensionsForm = AmendOverseasPensionsForm.amendOverseasPensionsForm("ip2016").bind(Map("amendedOverseasPensions" -> "yes",
      "amendedOverseasPensionsAmt" -> "1234"))
    lazy val view = application.injector.instanceOf[amendIP14OverseasPensions]
    lazy val doc = Jsoup.parse(view.apply(oPensionsForm, "ip2016", "open").body)

    val errorForm = AmendOverseasPensionsForm.amendOverseasPensionsForm("ip2016").bind(Map("amendedOverseasPensions" -> "",
      "amendedOverseasPensionsAmt" -> "1234"))
    lazy val errorView = application.injector.instanceOf[amendIP14OverseasPensions]
    lazy val errorDoc = Jsoup.parse(errorView.apply(errorForm, "ip2016", "open").body)

    lazy val form = doc.select("form")
    "have the correct title" in{
      doc.title() shouldBe plaOverseasPensionsTitleNew
    }

    "have the correct and properly formatted header"in{
      doc.select("h1.govuk-heading-xl").text shouldBe plaOverseasPensionsTitle
    }

    "have some introductory text" in{
      doc.select("p.govuk-body").first().text shouldBe plaIP14OverseasPensionsQuestion
    }

    "have a question above the textbox"in{
      doc.select("#conditional-amendedOverseasPensions > div > label").text shouldBe plaOverseasPensionsQuestionTwo
    }

    "have a pair of yes/no buttons" in{
      doc.select("[for=amendedOverseasPensions]").text shouldBe plaBaseYes
      doc.select("input#amendedOverseasPensions").attr("type") shouldBe "radio"
      doc.select("[for=amendedOverseasPensions-2]").text shouldBe plaBaseNo
      doc.select("input#amendedOverseasPensions-2").attr("type") shouldBe "radio"
    }

    "have a valid form" in{
      form.attr("method") shouldBe "POST"
      form.attr("action") shouldBe controllers.routes.AmendsOverseasPensionController.submitAmendOverseasPensions("ip2016", "open").url
      form.select("legend.govuk-visually-hidden").text() shouldBe plaIP14OverseasPensionsLegendText
    }

    "have a £ symbol present" in{
      doc.select(".govuk-input__prefix").text shouldBe "£"
    }

    "have a continue button" in{
      doc.select(".govuk-button").text shouldBe plaBaseChange
      doc.select(".govuk-button").attr("id") shouldBe "submit"
    }

    "display the correct errors appropriately" in{
      errorForm.hasErrors shouldBe true
      errorDoc.select(".govuk-error-summary__title").text shouldBe plaBaseErrorSummaryLabel
      errorDoc.select(".govuk-error-message").text shouldBe s"Error: $plaMandatoryError"
    }

    "not have errors on valid pages" in{
      oPensionsForm.hasErrors shouldBe false
      doc.select(".govuk-error-message").text shouldBe ""
    }

  }
}
