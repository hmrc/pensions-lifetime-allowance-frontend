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

import forms.OverseasPensionsForm
import org.jsoup.Jsoup
import play.api.i18n.Messages.Implicits._
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import views.html.pages.ip2016.{overseasPensions => views}
import testHelpers.ViewSpecHelpers.IP16.OverseasPensionsViewMessages

class OverseasPensionsViewSpec extends CommonViewSpecHelper with OverseasPensionsViewMessages {

  "the OverseasPensionsView" should{
    val oPensionsForm = OverseasPensionsForm.overseasPensionsForm.bind(Map("overseasPensions" -> "Yes", "overseasPensionsAmt" -> "1234"))
    lazy val view = views(oPensionsForm)
    lazy val doc = Jsoup.parse(view.body)

    val errorForm = OverseasPensionsForm.overseasPensionsForm.bind(Map.empty[String, String])
    lazy val errorView = views(errorForm)
    lazy val errorDoc = Jsoup.parse(errorView.body)

    lazy val form = doc.select("form")
    "have the correct title" in{
      doc.title() shouldBe plaOverseasPensionsTitle
    }

    "have the correct and properly formatted header"in{
      doc.select("h1").text shouldBe plaOverseasPensionsTitle
      doc.select("h1").hasClass("heading-large") shouldBe true
    }

    "have some introductory text" in{
      doc.select("p").first().text shouldBe plaOverseasPensionsQuestion
    }

    "have a question above the textbox"in{
      doc.select("h2").get(1).text shouldBe plaOverseasPensionsQuestionTwo
    }

    "have a pair of yes/no buttons" in{
      doc.select("[for=overseasPensions-yes]").text shouldBe plaBaseYes
      doc.select("[for=overseasPensions-no]").text shouldBe plaBaseNo
    }

    "have a valid form" in{
      form.attr("method") shouldBe "POST"
      form.attr("action") shouldBe controllers.routes.IP2016Controller.submitOverseasPensions().url
    }

    "have a £ symbol present" in{
      doc.select(".poundSign").text shouldBe "£"
    }

    "have a continue button" in{
      doc.select("button").text shouldBe plaBaseContinue
    }

    "display the correct errors appropriately" in{
      errorForm.hasErrors shouldBe true
      errorDoc.select("h2.h3-heading").text shouldBe plaBaseErrorSummaryLabel
      errorDoc.select("span.error-notification").text shouldBe errorRequired
    }
  }
}
