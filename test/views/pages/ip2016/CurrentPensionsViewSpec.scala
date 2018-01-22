/*
 * Copyright 2018 HM Revenue & Customs
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

import org.jsoup.Jsoup
import forms.CurrentPensionsForm
import views.html.pages.ip2016.{currentPensions => views}
import play.api.i18n.Messages.Implicits._
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.IP16.CurrentPensionsViewMessages

class CurrentPensionsViewSpec extends CommonViewSpecHelper with CurrentPensionsViewMessages{

  "the CurrentPensionsView" should{
    val model = CurrentPensionsForm.currentPensionsForm.bind(Map("currentPensionsAmt" -> "12000"))
    lazy val view = views(model)
    lazy val doc = Jsoup.parse(view.body)

    val errorModel = CurrentPensionsForm.currentPensionsForm.bind(Map("currentPensionsAmt" -> "a"))
    lazy val errorView = views(errorModel)
    lazy val errorDoc = Jsoup.parse(errorView.body)
    lazy val form = doc.select("form")

    "have the correct title" in{
      doc.title() shouldBe plaCurrentPensionsTitle
    }

    "have the correct and properly formatted header"in{
      doc.select("h1").text shouldBe plaCurrentPensionsTitle
      doc.select("h1").hasClass("heading-large") shouldBe true
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
      doc.getElementsByTag("a").text shouldBe plaHelpLink
      doc.getElementsByTag("a").attr("href") shouldBe plaHelpLinkExternalReference
    }

    "have a valid text box" in{
      form.attr("method") shouldBe "POST"
      form.attr("action") shouldBe controllers.routes.IP2016Controller.submitCurrentPensions().url
    }

    "have a £ symbol present" in{
      doc.select(".poundSign").text shouldBe "£"
    }

    "have a continue button" in{
      doc.select("button").text shouldBe plaBaseContinue
    }

    "display the correct errors appropriately" in{
      errorDoc.select("h2").text shouldBe plaBaseErrorSummaryLabel
      errorDoc.getElementById("currentPensionsAmt-error-summary").text shouldBe errorReal
      errorDoc.getElementById("currentPensionsAmt-error-message").text shouldBe errorReal
    }
  }


}
