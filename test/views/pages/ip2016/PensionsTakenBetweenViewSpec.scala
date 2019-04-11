/*
 * Copyright 2019 HM Revenue & Customs
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

import forms.PensionsTakenBetweenForm
import org.jsoup.Jsoup
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.ip2016.PensionsTakenBetweenViewMessages
import views.html.pages.ip2016.{pensionsTakenBetween => views}

class PensionsTakenBetweenViewSpec extends CommonViewSpecHelper with PensionsTakenBetweenViewMessages {

  "the PensionsTakenBetweenView" should {
    val pensionsForm = PensionsTakenBetweenForm.pensionsTakenBetweenForm.bind(Map("pensionsTakenBetween" -> "Yes", "pensionsTakenBetweenAmt" -> "12345"))
    lazy val view = views(pensionsForm)
    lazy val doc = Jsoup.parse(view.body)

    val errorForm =  PensionsTakenBetweenForm.pensionsTakenBetweenForm.bind(Map.empty[String, String])
    lazy val errorView = views(errorForm)
    lazy val errorDoc = Jsoup.parse(errorView.body)

    lazy val form = doc.select("form")

    "have the correct title" in{
      doc.title() shouldBe plaPensionsTakenBetweenTitle
    }

    "have the correct and properly formatted header"in{
      doc.select("h1").text shouldBe plaPensionsTakenBetweenTitle
    }

    "have the right sub-headers and summary text" in{
      doc.select("h2").text shouldBe plaPensionsTakenBetweenQuestionTwo
      doc.select("summary").text shouldBe plaPensionsTakenBetweenHelp
    }

    "have the right explanatory paragraphs" in{
      doc.select("p").eq(0).text shouldBe plaPensionsTakenBetweenParaOne
      doc.select("p").eq(2).text shouldBe plaPensionsTakenBetweenParaTwo
      doc.select("p").eq(3).text shouldBe plaPensionsTakenBetweenParaThree
    }

    "have a visible menu with a correct list values" in{
      doc.select("li").eq(0).text shouldBe plaPensionsTakenBetweenBulletOne
      doc.select("li").eq(1).text shouldBe plaPensionsTakenBetweenBulletTwo
      doc.select("li").eq(2).text shouldBe plaPensionsTakenBetweenBulletThree
    }

    "have a hidden drop-down menu with the correct list values" in{
      doc.select("li").eq(3).text shouldBe plaPensionsTakenBetweenStepOne
      doc.select("li").eq(4).text shouldBe plaPensionsTakenBetweenStepTwo
      doc.select("li").eq(5).text shouldBe plaPensionsTakenBetweenStepThree
      doc.select("li").eq(6).text shouldBe plaPensionsTakenBetweenStepFour
    }

    "have a help link redirecting to the right location" in{
      doc.select("a").text shouldBe plaPensionsTakenBetweenHelpLinkText
      doc.select("a").attr("href") shouldBe plaPensionsTakenBetweenHelpLinkLocation
    }

    "have a valid form" in{
      form.attr("method") shouldBe "POST"
      form.attr("action") shouldBe controllers.routes.IP2016Controller.submitPensionsTakenBetween().url
      form.select("legend.visually-hidden").text() shouldBe plaPensionsTakenBetweenLegendText
    }

    "have a £ symbol present" in{
      doc.select(".poundSign").text shouldBe "£"
    }

    "have a pair of yes/no buttons" in{
      doc.select("[for=pensionsTakenBetween-yes]").text shouldBe plaBaseYes
      doc.select("input#pensionsTakenBetween-yes").attr("type") shouldBe "radio"
      doc.select("[for=pensionsTakenBetween-no]").text shouldBe plaBaseNo
      doc.select("input#pensionsTakenBetween-no").attr("type") shouldBe "radio"
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
      pensionsForm.hasErrors shouldBe false
      doc.select("span.error-notification").text shouldBe ""
    }
  }
}
