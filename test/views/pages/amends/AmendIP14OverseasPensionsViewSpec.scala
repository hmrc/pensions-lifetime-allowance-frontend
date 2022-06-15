/*
 * Copyright 2022 HM Revenue & Customs
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
import uk.gov.hmrc.play.views.html.helpers.{ErrorSummary, FormWithCSRF}
import views.html.pages.amends.amendIP14OverseasPensions

class AmendIP14OverseasPensionsViewSpec extends CommonViewSpecHelper with AmendIP14OverseasPensionsViewSpecMessages{

  implicit val errorSummary: ErrorSummary = app.injector.instanceOf[ErrorSummary]
  implicit val formWithCSRF: FormWithCSRF = app.injector.instanceOf[FormWithCSRF]

  "the AmendIP14OverseasPensionsView" should{
    val oPensionsForm = AmendOverseasPensionsForm.amendOverseasPensionsForm.bind(Map("amendedOverseasPensions" -> "Yes",
      "amendedOverseasPensionsAmt" -> "1234",
      "protectionType" -> "ip2014",
      "status" -> "open"))
    lazy val view = application.injector.instanceOf[amendIP14OverseasPensions]
    lazy val doc = Jsoup.parse(view.apply(oPensionsForm).body)

    val errorForm = AmendOverseasPensionsForm.amendOverseasPensionsForm.bind(Map.empty[String, String])
    lazy val errorView = application.injector.instanceOf[amendIP14OverseasPensions]
    lazy val errorDoc = Jsoup.parse(errorView.apply(errorForm).body)

    lazy val form = doc.select("form")
    "have the correct title" in{
      doc.title() shouldBe plaOverseasPensionsTitle
    }

    "have the correct and properly formatted header"in{
      doc.select("h1").text shouldBe plaOverseasPensionsTitle
    }

    "have some introductory text" in{
      doc.select("p").first().text shouldBe plaIP14OverseasPensionsQuestion
    }

    "have a question above the textbox"in{
      doc.select("h2").text shouldBe plaOverseasPensionsQuestionTwo
    }

    "have a pair of yes/no buttons" in{
      doc.select("[for=amendedOverseasPensions-yes]").text shouldBe plaBaseYes
      doc.select("input#amendedOverseasPensions-yes").attr("type") shouldBe "radio"
      doc.select("[for=amendedOverseasPensions-no]").text shouldBe plaBaseNo
      doc.select("input#amendedOverseasPensions-no").attr("type") shouldBe "radio"
    }

    "have a valid form" in{
      form.attr("method") shouldBe "POST"
      form.attr("action") shouldBe controllers.routes.AmendsController.submitAmendOverseasPensions.url
      form.select("legend.visually-hidden").text() shouldBe plaIP14OverseasPensionsLegendText
    }

    "have a £ symbol present" in{
      doc.select(".poundSign").text shouldBe "£"
    }

    "have a continue button" in{
      doc.select("button").text shouldBe plaBaseChange
      doc.select("button").attr("type") shouldBe "submit"
    }

    "display the correct errors appropriately" in{
      errorForm.hasErrors shouldBe true
      errorDoc.select("h2.h3-heading").text shouldBe plaBaseErrorSummaryLabel
      errorDoc.select("span.error-notification").text shouldBe errorRequired
    }

    "not have errors on valid pages" in{
      oPensionsForm.hasErrors shouldBe false
      doc.select("span.error-notification").text shouldBe ""
    }

  }
}
