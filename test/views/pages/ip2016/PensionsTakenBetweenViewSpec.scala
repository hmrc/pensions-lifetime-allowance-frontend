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

import forms.PensionsTakenBetweenForm
import org.jsoup.Jsoup
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.ip2016.PensionsTakenBetweenViewMessages
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import views.html.pages.ip2016.pensionsTakenBetween

class PensionsTakenBetweenViewSpec extends CommonViewSpecHelper with PensionsTakenBetweenViewMessages {

  implicit val formWithCSRF: FormWithCSRF = app.injector.instanceOf[FormWithCSRF]

  "the PensionsTakenBetweenView" should {
    val pensionsForm = PensionsTakenBetweenForm.pensionsTakenBetweenForm.bind(Map("pensionsTakenBetween" -> "yes", "pensionsTakenBetweenAmt" -> "12345"))
    lazy val view = application.injector.instanceOf[pensionsTakenBetween]
    lazy val doc = Jsoup.parse(view.apply(pensionsForm).body)

    val errorForm =  PensionsTakenBetweenForm.pensionsTakenBetweenForm.bind(Map("pensionsTakenBetween" -> "", "pensionsTakenBetweenAmt" -> "12345"))
    lazy val errorView = application.injector.instanceOf[pensionsTakenBetween]
    lazy val errorDoc = Jsoup.parse(errorView.apply(errorForm).body)

    lazy val form = doc.select("form")

    "have the correct title" in{
      doc.title() shouldBe plaPensionsTakenBetweenTitleNew
    }

    "have the correct and properly formatted header"in{
      doc.select("h1.govuk-heading-xl").text shouldBe plaPensionsTakenBetweenTitle
    }

//    "have the right sub-headers and summary text" in{
//      doc.select("h2.govuk-heading-m").text shouldBe plaPensionsUsedBetweenQuestion
//      doc.select(".govuk-details__summary-text").text shouldBe plaPensionsTakenBetweenHelp
//    }

//    "have the right explanatory paragraphs" in{
//      doc.select("#conditional-pensionsTakenBetween > p").text shouldBe plaPensionsTakenBetweenParaOne
//      doc.select("#ip16-pensions-taken-between-help > div > p:nth-child(1)").text shouldBe plaPensionsTakenBetweenParaTwo
//      doc.select("#ip16-pensions-taken-between-help > div > p:nth-child(3)").text shouldBe plaPensionsTakenBetweenParaThreeNew
//    }
//
//    "have a visible menu with a correct list values" in{
//      doc.select("#main-content > div > div > form > ul > li:nth-child(1)").text shouldBe plaPensionsTakenBetweenBulletOne
//      doc.select("#main-content > div > div > form > ul > li:nth-child(2)").text shouldBe plaPensionsTakenBetweenBulletTwo
//      doc.select("#main-content > div > div > form > ul > li:nth-child(3)").text shouldBe plaPensionsTakenBetweenBulletThree
//    }
//
//    "have a hidden drop-down menu with the correct list values" in{
//      doc.select("#ip16-pensions-taken-between-help > div > ol > li:nth-child(1)").text shouldBe plaPensionsTakenBetweenStepOne
//      doc.select("#ip16-pensions-taken-between-help > div > ol > li:nth-child(2)").text shouldBe plaPensionsTakenBetweenStepTwo
//      doc.select("#ip16-pensions-taken-between-help > div > ol > li:nth-child(3)").text shouldBe plaPensionsTakenBetweenStepThree
//      doc.select("#ip16-pensions-taken-between-help > div > ol > li:nth-child(4)").text shouldBe plaPensionsTakenBetweenStepFour
//    }
//
//    "have a help link redirecting to the right location" in{
//      doc.select("a#ip16-pensions-taken-between-help-link").text shouldBe plaPensionsTakenBetweenHelpLinkTextNew
//      doc.select("a#ip16-pensions-taken-between-help-link").attr("href") shouldBe plaPensionsTakenBetweenHelpLinkLocation
//    }
//
//    "have a valid form" in{
//      form.attr("method") shouldBe "POST"
//      form.attr("action") shouldBe controllers.routes.IP2016Controller.submitPensionsTakenBetween.url
//      form.select("legend.govuk-visually-hidden").text() shouldBe plaPensionsTakenBetweenLegendText
//    }
//
//    "have a £ symbol present" in{
//      doc.select(".govuk-input__prefix").text shouldBe "£"
//    }

    "have a pair of yes/no buttons" in{
      doc.select("[for=pensionsTakenBetween]").text shouldBe plaBaseYes
      doc.select("input#pensionsTakenBetween").attr("type") shouldBe "radio"
      doc.select("[for=pensionsTakenBetween-2]").text shouldBe plaBaseNo
      doc.select("input#pensionsTakenBetween-2").attr("type") shouldBe "radio"
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
      pensionsForm.hasErrors shouldBe false
      doc.select(".govuk-error-message").text shouldBe ""
    }
  }
}
