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

import forms.AmendPensionsTakenBeforeForm
import org.jsoup.Jsoup
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.amends.AmendIP14PensionsTakenBeforeViewSpecMessages
import uk.gov.hmrc.play.views.html.helpers.{ErrorSummary, FormWithCSRF}
import views.html.pages.amends.amendIP14PensionsTakenBefore

class AmendIP14PensionsTakenBeforeViewSpec extends CommonViewSpecHelper with AmendIP14PensionsTakenBeforeViewSpecMessages{

  implicit val errorSummary: ErrorSummary = app.injector.instanceOf[ErrorSummary]
  implicit val formWithCSRF: FormWithCSRF = app.injector.instanceOf[FormWithCSRF]

  "the AmendIP14PensionsTakenBeforeView" should{
    val pensionsForm = AmendPensionsTakenBeforeForm.amendPensionsTakenBeforeForm.bind(Map("amendedPensionsTakenBefore" -> "Yes",
      "amendedPensionsTakenBeforeAmt" -> "12345",
      "protectionType" -> "ip2014",
      "status" -> "open"))
    lazy val view = application.injector.instanceOf[amendIP14PensionsTakenBefore]
    lazy val doc = Jsoup.parse(view.apply(pensionsForm).body)

    val errorForm =  AmendPensionsTakenBeforeForm.amendPensionsTakenBeforeForm.bind(Map.empty[String, String])
    lazy val errorView = application.injector.instanceOf[amendIP14PensionsTakenBefore]
    lazy val errorDoc = Jsoup.parse(errorView.apply(errorForm).body)

    lazy val form = doc.select("form")

    "have the correct title" in{
      doc.title() shouldBe plaPensionsTakenBeforeTitle
    }

    "have the correct and properly formatted header"in{
      doc.select("h1").text shouldBe plaPensionsTakenBeforeTitle
    }

    "have the right explanatory messages" in{
      doc.select("h2").text shouldBe plaIP14PensionsTakenBeforeQuestion
      doc.select("summary").text shouldBe plaPensionsTakenBeforeHelp
      doc.select("p").eq(1).text shouldBe plaPensionsTakenBeforeParaOne
      doc.select("p").eq(2).text shouldBe plaPensionsTakenBeforeParaTwo
      doc.select("p").eq(3).text shouldBe plaPensionsTakenBeforeParaThree
    }

    "have a hidden menu with the correct list values" in{
      doc.select("li").eq(0).text shouldBe plaPensionsTakenBeforeStepOne
      doc.select("li").eq(1).text shouldBe plaIP14PensionsTakenBeforeStepTwo
      doc.select("li").eq(2).text shouldBe plaPensionsTakenBeforeStepThree
      doc.select("li").eq(3).text shouldBe plaPensionsTakenBeforeBulletOne
      doc.select("li").eq(4).text shouldBe plaPensionsTakenBeforeBulletTwo
    }

    "have a help link redirecting to the right place" in{
      doc.getElementsByTag("a").text shouldBe plaPensionsTakenBeforeHelpLinkText
      doc.getElementsByTag("a").attr("href") shouldBe plaPensionsTakenBeforeHelpLinkLocation
    }

    "have a valid form" in{
      form.attr("method") shouldBe "POST"
      form.attr("action") shouldBe controllers.routes.AmendsController.submitAmendPensionsTakenBefore.url
      form.select("legend.visually-hidden").text() shouldBe plaPensionsTakenBeforeLegendText
    }

    "have a £ symbol present" in{
      doc.select(".poundSign").text shouldBe "£"
    }

    "have a pair of yes/no buttons" in{
      doc.select("[for=amendedPensionsTakenBefore-yes]").text shouldBe plaBaseYes
      doc.select("input#amendedPensionsTakenBefore-yes").attr("type") shouldBe "radio"
      doc.select("[for=amendedPensionsTakenBefore-no]").text shouldBe plaBaseNo
      doc.select("input#amendedPensionsTakenBefore-no").attr("type") shouldBe "radio"
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
      pensionsForm.hasErrors shouldBe false
      doc.select("span.error-notification").text shouldBe ""
    }
  }
}
