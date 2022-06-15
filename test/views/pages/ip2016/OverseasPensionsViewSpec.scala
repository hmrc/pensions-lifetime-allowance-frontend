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

package views.pages.ip2016

import forms.OverseasPensionsForm
import org.jsoup.Jsoup
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.ip2016.OverseasPensionsViewMessages
import uk.gov.hmrc.play.views.html.helpers.{ErrorSummary, FormWithCSRF}
import views.html.pages.ip2016.overseasPensions

class OverseasPensionsViewSpec extends CommonViewSpecHelper with OverseasPensionsViewMessages {

  implicit val errorSummary: ErrorSummary = app.injector.instanceOf[ErrorSummary]
  implicit val formWithCSRF: FormWithCSRF = app.injector.instanceOf[FormWithCSRF]

  "the OverseasPensionsView" should{
    val oPensionsForm = OverseasPensionsForm.overseasPensionsForm.bind(Map("overseasPensions" -> "Yes", "overseasPensionsAmt" -> "1234"))
    lazy val view = application.injector.instanceOf[overseasPensions]
    lazy val doc = Jsoup.parse(view.apply(oPensionsForm).body)

    val errorForm = OverseasPensionsForm.overseasPensionsForm.bind(Map.empty[String, String])
    lazy val errorView = application.injector.instanceOf[overseasPensions]
    lazy val errorDoc = Jsoup.parse(errorView.apply(errorForm).body)

    lazy val form = doc.select("form")
    "have the correct title" in{
      doc.title() shouldBe plaOverseasPensionsTitle
    }

    "have the correct and properly formatted header"in{
      doc.select("h1").text shouldBe plaOverseasPensionsTitle
    }

    "have some introductory text" in{
      doc.select("p").first().text shouldBe plaOverseasPensionsQuestion
    }

    "have a question above the textbox"in{
      doc.select("h2").text shouldBe plaOverseasPensionsQuestionTwo
    }

    "have a pair of yes/no buttons" in{
      doc.select("[for=overseasPensions-yes]").text shouldBe plaBaseYes
      doc.select("input#overseasPensions-yes").attr("type") shouldBe "radio"
      doc.select("[for=overseasPensions-no]").text shouldBe plaBaseNo
      doc.select("input#overseasPensions-no").attr("type") shouldBe "radio"
    }

    "have a valid form" in{
      form.attr("method") shouldBe "POST"
      form.attr("action") shouldBe controllers.routes.IP2016Controller.submitOverseasPensions.url
      form.select("legend.visually-hidden").text() shouldBe plaOverseasPensionsLegendText
    }

    "have a £ symbol present" in{
      doc.select(".poundSign").text shouldBe "£"
    }

    "have a continue button" in{
      doc.select("button").text shouldBe plaBaseContinue
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
