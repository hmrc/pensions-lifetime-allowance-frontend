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

package views.pages.ip2016

import forms.OverseasPensionsForm
import org.jsoup.Jsoup
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.ip2016.OverseasPensionsViewMessages
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import views.html.pages.ip2016.overseasPensions

class OverseasPensionsViewSpec extends CommonViewSpecHelper with OverseasPensionsViewMessages {

  implicit val formWithCSRF: FormWithCSRF = app.injector.instanceOf[FormWithCSRF]

  "the OverseasPensionsView" should{
    val oPensionsForm = OverseasPensionsForm.overseasPensionsForm.bind(Map("overseasPensions" -> "yes", "overseasPensionsAmt" -> "1234"))
    lazy val view = application.injector.instanceOf[overseasPensions]
    lazy val doc = Jsoup.parse(view.apply(oPensionsForm).body)

    val errorForm = OverseasPensionsForm.overseasPensionsForm.bind(Map("overseasPensions" -> "", "overseasPensionsAmt" -> "123"))
    lazy val errorView = application.injector.instanceOf[overseasPensions]
    lazy val errorDoc = Jsoup.parse(errorView.apply(errorForm).body)

    lazy val form = doc.select("form")
    "have the correct title" in{
      doc.title() shouldBe plaOverseasPensionsTitleNew
    }

    "have the correct and properly formatted header"in{
      doc.select("h1.govuk-heading-xl").text shouldBe plaOverseasPensionsTitle
    }

    "have some introductory text" in{
      doc.select("p.govuk-body").first().text shouldBe plaOverseasPensionsQuestion
    }

    "have a question above the textbox"in{
      doc.select("#conditional-overseasPensions > div > label").text shouldBe plaOverseasPensionsQuestionTwo
    }

    "have a pair of yes/no buttons" in{
      doc.select("[for=overseasPensions]").text shouldBe plaBaseYes
      doc.select("input#overseasPensions").attr("type") shouldBe "radio"
      doc.select("[for=overseasPensions-2]").text shouldBe plaBaseNo
      doc.select("input#overseasPensions-2").attr("type") shouldBe "radio"
    }

    "have a valid form" in{
      form.attr("method") shouldBe "POST"
      form.attr("action") shouldBe controllers.routes.IP2016Controller.submitOverseasPensions.url
      form.select("legend.govuk-visually-hidden").text() shouldBe plaOverseasPensionsLegendText
    }

    "have a £ symbol present" in{
      doc.select(".govuk-input__prefix").text shouldBe "£"
    }

    "have a continue button" in{
      doc.select(".govuk-button").text shouldBe plaBaseContinue
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
