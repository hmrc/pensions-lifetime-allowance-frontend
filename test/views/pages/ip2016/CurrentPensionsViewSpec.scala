/*
 * Copyright 2021 HM Revenue & Customs
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

import forms.CurrentPensionsForm
import org.jsoup.Jsoup
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.ip2016.CurrentPensionsViewMessages
import uk.gov.hmrc.play.views.html.helpers.{ErrorSummary, FormWithCSRF}
import views.html.pages.ip2016.currentPensions

class CurrentPensionsViewSpec extends CommonViewSpecHelper with CurrentPensionsViewMessages{

  implicit val errorSummary: ErrorSummary = app.injector.instanceOf[ErrorSummary]
  implicit val formWithCSRF: FormWithCSRF = app.injector.instanceOf[FormWithCSRF]

  "the CurrentPensionsView" should{
    val currentPensionsForm = CurrentPensionsForm.currentPensionsForm.bind(Map("currentPensionsAmt" -> "12000"))
    lazy val view = application.injector.instanceOf[currentPensions]
    lazy val doc = Jsoup.parse(view.apply(currentPensionsForm).body)

    val errorForm = CurrentPensionsForm.currentPensionsForm.bind(Map("currentPensionsAmt" -> "a"))
    lazy val errorView = application.injector.instanceOf[currentPensions]
    lazy val errorDoc = Jsoup.parse(errorView.apply(errorForm).body)
    lazy val form = doc.select("form")

    "have the correct title" in{
      doc.title() shouldBe plaCurrentPensionsTitle
    }

    "have the correct and properly formatted header"in{
      doc.select("h1").text shouldBe plaCurrentPensionsTitle
    }

    "have some introductory text" in{
      doc.select("p").first().text shouldBe plaCurrentPensionsQuestion
    }

    "have a hidden menu with the correct values" in{
      doc.select("summary").text shouldBe plaCurrentPensionsHiddenLink
      doc.select("li").eq(0).text shouldBe plaHiddenMenuItemOne
      doc.select("li").eq(1).text shouldBe plaHiddenMenuItemTwo
      doc.select("li").eq(2).text shouldBe plaHiddenMenuItemThree
      doc.select("li").eq(3).text shouldBe plaHiddenMenuItemFour
    }

    "have a help link redirecting to the right location" in{
      doc.select("p").eq(3).text shouldBe plaHelpLinkCompleteMessage
      doc.select("a").text shouldBe plaHelpLink
      doc.select("a").attr("href") shouldBe plaHelpLinkExternalReference
    }

    "has a valid form" in{
      form.attr("method") shouldBe "POST"
      form.attr("action") shouldBe controllers.routes.IP2016Controller.submitCurrentPensions().url
      form.select("legend.visually-hidden").text() shouldBe plaCurrentPensionsLegendText
    }

    "have a £ symbol present" in{
      doc.select(".poundSign").text shouldBe "£"
    }

    "have a continue button" in{
      doc.select("button").text shouldBe plaBaseContinue
      doc.select("button").attr("type") shouldBe "submit"
    }

    "display the correct errors appropriately" in{
      errorDoc.select("h2").text shouldBe plaBaseErrorSummaryLabel
      errorDoc.select("a#currentPensionsAmt-error-summary").text shouldBe errorReal
      errorDoc.select("span#currentPensionsAmt-error-message.error-notification").text shouldBe errorReal
    }

    "not have errors on valid pages" in{
      currentPensionsForm.hasErrors shouldBe false
      doc.select("a#currentPensionsAmt-error-summary").text shouldBe ""
      doc.select("span#currentPensionsAmt-error-message.error-notification").text shouldBe ""
    }
  }
}
