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

import forms.PensionsUsedBetweenForm
import org.jsoup.Jsoup
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.ip2016.PensionsUsedBetweenViewMessages
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import views.html.pages.ip2016.pensionsUsedBetween

class PensionsUsedBetweenViewSpec extends CommonViewSpecHelper with PensionsUsedBetweenViewMessages {

  implicit val formWithCSRF: FormWithCSRF = app.injector.instanceOf[FormWithCSRF]

  "the PensionsUsedBetweenView" should {
    val pensionsForm = PensionsUsedBetweenForm.pensionsUsedBetweenForm("ip2016").bind(Map("pensionsUsedBetweenAmt" -> "12"))
    lazy val view = application.injector.instanceOf[pensionsUsedBetween]
    lazy val doc = Jsoup.parse(view.apply(pensionsForm).body)

    val errorForm =  PensionsUsedBetweenForm.pensionsUsedBetweenForm("ip2016").bind(Map("pensionsUsedBetweenAmt" -> ""))
    lazy val errorView = application.injector.instanceOf[pensionsUsedBetween]
    lazy val errorDoc = Jsoup.parse(errorView.apply(errorForm).body)

    lazy val form = doc.select("form")

    "have the correct title" in{
      doc.title() shouldBe plaPensionsUsedBetweenTitle
    }

    "have the correct and properly formatted header"in{
      doc.select("h1.govuk-heading-xl").text shouldBe plaPensionsUsedBetweenHeader
    }

    "have the right headers and summary text" in{
      doc.select("h1.govuk-heading-xl").text shouldBe plaPensionsUsedBetweenHeader
      doc.select(".govuk-details__summary-text").text shouldBe plaPensionsUsedBetweenHelp
    }

    "have the right explanatory paragraphs" in{
      doc.select("#main-content > div > div > p").text shouldBe plaPensionsUsedBetweenParaOne
      doc.select("#ip16-pensions-used-between-help > div > p:nth-child(1)").text shouldBe plaPensionsUsedBetweenParaTwo
      doc.select("#ip16-pensions-used-between-help > div > p:nth-child(3)").text shouldBe plaPensionsUsedBetweenParaThreeNew
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
      errorDoc.select(".govuk-error-message").text shouldBe s"Error: $plaPensionUsedBetweenMandatoryAmount"
    }

    "not have errors on valid pages" in{
      pensionsForm.hasErrors shouldBe false
      doc.select(".govuk-error-message").text shouldBe ""
    }
  }
}
