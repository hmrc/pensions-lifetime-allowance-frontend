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
import models.amend.value.AmendPensionsTakenBetweenModel
import models.pla.AmendableProtectionType.IndividualProtection2016
import models.pla.request.AmendProtectionRequestStatus.Open
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.api.data.Form
import testHelpers.CommonViewSpecHelper
import testHelpers.messages.amends.{AmendIP14PensionsTakenBetweenViewMessages, AmendIP16PensionsUsedBetweenViewMessages}
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import views.html.pages.amends.amendIP14PensionsTakenBetween

class AmendIP14PensionsTakenBetweenViewSpec
    extends CommonViewSpecHelper
    with AmendIP14PensionsTakenBetweenViewMessages
    with AmendIP16PensionsUsedBetweenViewMessages {

  implicit val formWithCSRF: FormWithCSRF = inject[FormWithCSRF]

  val view: amendIP14PensionsTakenBetween = inject[amendIP14PensionsTakenBetween]

  val form: Form[AmendPensionsTakenBetweenModel] = AmendPensionsTakenBetweenForm
    .amendPensionsTakenBetweenForm(IndividualProtection2016)
    .bind(Map("amendedPensionsTakenBetween" -> "yes", "amendedPensionsTakenBetweenAmt" -> "12345"))

  val doc: Document = Jsoup.parse(view.apply(form, IndividualProtection2016, Open).body)

  val errorForm: Form[AmendPensionsTakenBetweenModel] =
    AmendPensionsTakenBetweenForm
      .amendPensionsTakenBetweenForm(IndividualProtection2016)
      .bind(Map.empty[String, String])

  val errorDoc: Document = Jsoup.parse(view.apply(errorForm, IndividualProtection2016, Open).body)

  "the AmendIP14PensionsTakenBetweenView" should {
    "have the correct title" in {
      doc
        .title() shouldBe s"$plaIP14PensionsTakenBetweenTitle - Check your pension protections and enhancements - GOV.UK"
    }

    "have the correct and properly formatted header" in {
      doc.select("h1").text shouldBe plaIP14PensionsTakenBetweenTitle
    }

    "have a visible menu with a correct list values" in {
      doc.select("ul.govuk-list.govuk-list--bullet li").eq(0).text shouldBe plaPensionsTakenBetweenBulletOne
      doc.select("ul.govuk-list.govuk-list--bullet li").eq(1).text shouldBe plaPensionsTakenBetweenBulletTwo
      doc.select("ul.govuk-list.govuk-list--bullet li").eq(2).text shouldBe plaPensionsTakenBetweenBulletThree
    }

    "have a valid form" in {
      val formElement: Elements = doc.select("form")

      formElement.attr("method") shouldBe "POST"
      formElement.attr("action") shouldBe controllers.routes.AmendsPensionTakenBetweenController
        .submitAmendPensionsTakenBetween(IndividualProtection2016, Open)
        .url
      formElement.select("legend.govuk-visually-hidden").text() shouldBe plaIP14PensionsTakenBetweenLegendText
    }

    "have a pair of yes/no buttons" in {
      doc.select("[for=amendedPensionsTakenBetween]").text shouldBe plaBaseYes
      doc.select("input#amendedPensionsTakenBetween").attr("type") shouldBe "radio"
      doc.select("[for=amendedPensionsTakenBetween-2]").text shouldBe plaBaseNo
      doc.select("input#amendedPensionsTakenBetween-2").attr("type") shouldBe "radio"
    }

    "have a continue button" in {
      doc.select("button").text shouldBe plaBaseChange
    }

    "display the correct errors appropriately" in {
      errorForm.hasErrors shouldBe true
      errorDoc.select(".govuk-error-summary__title").text shouldBe plaBaseErrorSummaryLabel
      errorDoc.select(".govuk-error-summary__body li").get(0).text shouldBe plaMandatoryError
    }

    "not have errors on valid pages" in {
      form.hasErrors shouldBe false
      doc.select(".govuk-error-message").text shouldBe ""
    }
  }

}
