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
import forms.AmendPensionsTakenBeforeForm
import models.pla.AmendProtectionLifetimeAllowanceType.IndividualProtection2016
import org.jsoup.Jsoup
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.amends.AmendIP14PensionsTakenBeforeViewSpecMessages
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import views.html.pages.amends.amendIP14PensionsTakenBefore

class AmendIP14PensionsTakenBeforeViewSpec
    extends CommonViewSpecHelper
    with AmendIP14PensionsTakenBeforeViewSpecMessages {

  implicit val formWithCSRF: FormWithCSRF = app.injector.instanceOf[FormWithCSRF]

  "the AmendIP14PensionsTakenBeforeView" should {
    val pensionsForm = AmendPensionsTakenBeforeForm
      .amendPensionsTakenBeforeForm(IndividualProtection2016.toString)
      .bind(Map("amendedPensionsTakenBefore" -> "yes", "amendedPensionsTakenBeforeAmt" -> "12345"))
    lazy val view = app.injector.instanceOf[amendIP14PensionsTakenBefore]
    lazy val doc =
      Jsoup.parse(view.apply(pensionsForm, IndividualProtection2016.toString, "open").body)

    val errorForm = AmendPensionsTakenBeforeForm
      .amendPensionsTakenBeforeForm(IndividualProtection2016.toString)
      .bind(Map("amendedPensionsTakenBefore" -> "", "amendedPensionsTakenBeforeAmt" -> "12345"))
    lazy val errorView = app.injector.instanceOf[amendIP14PensionsTakenBefore]
    lazy val errorDoc =
      Jsoup.parse(errorView.apply(errorForm, IndividualProtection2016.toString, "open").body)

    lazy val form = doc.select("form")

    "have the correct title" in {
      doc.title() shouldBe plaPensionsTakenBeforeTitle
    }

    "have the correct and properly formatted header" in {
      doc.select("h1.govuk-heading-xl").text shouldBe plaPensionsTakenBeforeHeading
    }

    "have a valid form" in {
      form.attr("method") shouldBe "POST"
      form.attr("action") shouldBe controllers.routes.AmendsPensionTakenBeforeController
        .submitAmendPensionsTakenBefore(Strings.ProtectionTypeURL.IndividualProtection2016, "open")
        .url
      form.select("legend.govuk-visually-hidden").text() shouldBe plaPensionsTakenBeforeLegendText
    }

    "have a pair of yes/no buttons" in {
      doc.select("[for=amendedPensionsTakenBefore]").text shouldBe plaBaseYes
      doc.select("input#amendedPensionsTakenBefore").attr("type") shouldBe "radio"
      doc.select("[for=amendedPensionsTakenBefore-2]").text shouldBe plaBaseNo
      doc.select("input#amendedPensionsTakenBefore-2").attr("type") shouldBe "radio"
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
      pensionsForm.hasErrors shouldBe false
      doc.select(".govuk-error-message").text shouldBe ""
    }
  }

}
