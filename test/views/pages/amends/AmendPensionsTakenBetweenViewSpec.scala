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

package views.pages.amends

import forms.AmendPensionsTakenBetweenForm
import org.jsoup.Jsoup
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.ip2016.{PensionsTakenBetweenViewMessages, PensionsUsedBetweenViewMessages}
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import views.html.pages.amends.amendPensionsTakenBetween

class AmendPensionsTakenBetweenViewSpec extends CommonViewSpecHelper with PensionsTakenBetweenViewMessages with PensionsUsedBetweenViewMessages {

  implicit val formWithCSRF: FormWithCSRF = app.injector.instanceOf[FormWithCSRF]

  "the AmendPensionsTakenBetweenView" should {
    val pensionsForm = AmendPensionsTakenBetweenForm.amendPensionsTakenBetweenForm("ip2016").bind(Map("amendedPensionsTakenBetween" -> "yes",
                                                                                            "amendedPensionsTakenBetweenAmt" -> "12345"))
    lazy val view = application.injector.instanceOf[amendPensionsTakenBetween]
    lazy val doc = Jsoup.parse(view.apply(pensionsForm, "ip2016", "open").body)

    val errorForm =  AmendPensionsTakenBetweenForm.amendPensionsTakenBetweenForm("ip2016").bind(Map("amendedPensionsTakenBetween" -> "",
      "amendedPensionsTakenBetweenAmt" -> "12345",
      "protectionType" -> "ip2016",
      "status" -> "open"))
    lazy val errorView = application.injector.instanceOf[amendPensionsTakenBetween]
    lazy val errorDoc = Jsoup.parse(errorView.apply(errorForm, "ip2016", "open").body)

    lazy val form = doc.select("form")

    "have the correct title" in{
      doc.title() shouldBe plaPensionsTakenBetweenTitleNew
    }

    "have the correct and properly formatted header"in{
      doc.select("h1.govuk-heading-l").text shouldBe plaPensionsTakenBetweenTitle
    }

    "have a valid form" in{
      form.attr("method") shouldBe "POST"
      form.attr("action") shouldBe controllers.routes.AmendsPensionTakenBetweenController.submitAmendPensionsTakenBetween("ip2016", "open").url
      form.select("legend.govuk-visually-hidden").text() shouldBe plaPensionsTakenBetweenLegendText
    }

    "have a pair of yes/no buttons" in{
      doc.select("[for=amendedPensionsTakenBetween]").text shouldBe plaBaseYes
      doc.select("input#amendedPensionsTakenBetween").attr("type") shouldBe "radio"
      doc.select("[for=amendedPensionsTakenBetween-2]").text shouldBe plaBaseNo
      doc.select("input#amendedPensionsTakenBetween-2").attr("type") shouldBe "radio"
    }

    "have a continue button" in{
      doc.select(".govuk-button").text shouldBe plaBaseChange
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
