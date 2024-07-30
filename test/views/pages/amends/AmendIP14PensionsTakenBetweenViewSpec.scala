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
import testHelpers.ViewSpecHelpers.amends.AmendIP14PensionsTakenBetweenViewSpecMessages
import testHelpers.ViewSpecHelpers.ip2016.PensionsUsedBetweenViewMessages
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import views.html.pages.amends.amendIP14PensionsTakenBetween

class AmendIP14PensionsTakenBetweenViewSpec extends CommonViewSpecHelper with AmendIP14PensionsTakenBetweenViewSpecMessages with PensionsUsedBetweenViewMessages {

  implicit val formWithCSRF: FormWithCSRF = app.injector.instanceOf[FormWithCSRF]

  "the AmendIP14PensionsTakenBetweenView" should {
    val pensionsForm = AmendPensionsTakenBetweenForm.amendPensionsTakenBetweenForm.bind(Map("amendedPensionsTakenBetween" -> "yes",
      "amendedPensionsTakenBetweenAmt" -> "12345"))
    lazy val view = application.injector.instanceOf[amendIP14PensionsTakenBetween]
    lazy val doc = Jsoup.parse(view.apply(pensionsForm, "ip2016", "open").body)

    val errorForm =  AmendPensionsTakenBetweenForm.amendPensionsTakenBetweenForm.bind(Map.empty[String, String])
    lazy val errorView = application.injector.instanceOf[amendIP14PensionsTakenBetween]
    lazy val errorDoc = Jsoup.parse(errorView.apply(errorForm, "ip2016", "open").body)

    lazy val form = doc.select("form")

    "have the correct title" in{
      doc.title() shouldBe s"$plaIP14PensionsTakenBetweenTitle - Protect your lifetime allowance - GOV.UK"
    }

    "have the correct and properly formatted header"in{
      doc.select("h1").text shouldBe plaIP14PensionsTakenBetweenTitle
    }

    "have a visible menu with a correct list values" in{
      doc.select("ul.govuk-list.govuk-list--bullet li").eq(0).text shouldBe plaPensionsTakenBetweenBulletOne
      doc.select("ul.govuk-list.govuk-list--bullet li").eq(1).text shouldBe plaPensionsTakenBetweenBulletTwo
      doc.select("ul.govuk-list.govuk-list--bullet li").eq(2).text shouldBe plaPensionsTakenBetweenBulletThree
    }

    "have a valid form" in{
      form.attr("method") shouldBe "POST"
      form.attr("action") shouldBe controllers.routes.AmendsPensionTakenBetweenController.submitAmendPensionsTakenBetween("ip2016", "open").url
      form.select("legend.govuk-visually-hidden").text() shouldBe plaIP14PensionsTakenBetweenLegendText
    }

    "have a pair of yes/no buttons" in{
      doc.select("[for=amendedPensionsTakenBetween]").text shouldBe plaBaseYes
      doc.select("input#amendedPensionsTakenBetween").attr("type") shouldBe "radio"
      doc.select("[for=amendedPensionsTakenBetween-2]").text shouldBe plaBaseNo
      doc.select("input#amendedPensionsTakenBetween-2").attr("type") shouldBe "radio"
    }

    "have a continue button" in{
      doc.select("button").text shouldBe plaBaseChange
    }

    "display the correct errors appropriately" in{
      errorForm.hasErrors shouldBe true
      errorDoc.select(".govuk-error-summary__title").text shouldBe plaBaseErrorSummaryLabel
      errorDoc.select(".govuk-error-summary__body li").get(0).text shouldBe plaMandatoryError
    }

    "not have errors on valid pages" in{
      pensionsForm.hasErrors shouldBe false
      doc.select(".govuk-error-message").text shouldBe ""
    }
  }
}
