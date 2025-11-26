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

import common.Strings
import forms.AmendPensionsTakenBetweenForm
import models.amendModels.AmendPensionsTakenBetweenModel
import models.pla.AmendProtectionLifetimeAllowanceType.IndividualProtection2016
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import testHelpers.CommonViewSpecHelper
import testHelpers.messages.amends.{AmendIP16PensionsTakenBetweenViewMessages, AmendIP16PensionsUsedBetweenViewMessages}
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import views.html.pages.amends.amendIP16PensionsTakenBetween

class AmendIP16PensionsTakenBetweenViewSpec
    extends CommonViewSpecHelper
    with AmendIP16PensionsTakenBetweenViewMessages
    with AmendIP16PensionsUsedBetweenViewMessages {

  implicit val formWithCSRF: FormWithCSRF = inject[FormWithCSRF]

  val view: amendIP16PensionsTakenBetween = inject[amendIP16PensionsTakenBetween]

  val form: Form[AmendPensionsTakenBetweenModel] = AmendPensionsTakenBetweenForm
    .amendPensionsTakenBetweenForm(IndividualProtection2016.toString)
    .bind(Map("amendedPensionsTakenBetween" -> "yes", "amendedPensionsTakenBetweenAmt" -> "12345"))

  val doc: Document = Jsoup.parse(view.apply(form, IndividualProtection2016.toString, "open").body)

  val errorForm: Form[AmendPensionsTakenBetweenModel] = AmendPensionsTakenBetweenForm
    .amendPensionsTakenBetweenForm(IndividualProtection2016.toString)
    .bind(
      Map(
        "amendedPensionsTakenBetween"    -> "",
        "amendedPensionsTakenBetweenAmt" -> "12345",
        "protectionType"                 -> IndividualProtection2016.toString,
        "status"                         -> "open"
      )
    )

  val errorDoc: Document = Jsoup.parse(view.apply(errorForm, IndividualProtection2016.toString, "open").body)

  "the AmendPensionsTakenBetweenView" should {
    "have the correct title" in {
      doc.title() shouldBe plaPensionsTakenBetweenTitleNew
    }

    "have the correct and properly formatted header" in {
      doc.select("h1.govuk-heading-l").text shouldBe plaPensionsTakenBetweenTitle
    }

    "have a valid form" in {
      val formElement = doc.select("form")

      formElement.attr("method") shouldBe "POST"
      formElement.attr("action") shouldBe controllers.routes.AmendsPensionTakenBetweenController
        .submitAmendPensionsTakenBetween(Strings.ProtectionTypeUrl.IndividualProtection2016, "open")
        .url
      formElement.select("legend.govuk-visually-hidden").text() shouldBe plaPensionsTakenBetweenLegendText
    }

    "have a pair of yes/no buttons" in {
      doc.select("[for=amendedPensionsTakenBetween]").text shouldBe plaBaseYes
      doc.select("input#amendedPensionsTakenBetween").attr("type") shouldBe "radio"
      doc.select("[for=amendedPensionsTakenBetween-2]").text shouldBe plaBaseNo
      doc.select("input#amendedPensionsTakenBetween-2").attr("type") shouldBe "radio"
    }

    "have a continue button" in {
      doc.select(".govuk-button").text shouldBe plaBaseChange
      doc.select(".govuk-button").attr("id") shouldBe "submit"
    }

    "display the correct errors appropriately" in {
      errorForm.hasErrors shouldBe true
      errorDoc.select(".govuk-error-summary__title").text shouldBe plaBaseErrorSummaryLabel
      errorDoc.select(".govuk-error-message").text shouldBe s"Error: $plaMandatoryError"
    }

    "not have errors on valid pages" in {
      form.hasErrors shouldBe false
      doc.select(".govuk-error-message").text shouldBe ""
    }
  }

}
