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

import forms.AmendPSODetailsForm
import org.jsoup.Jsoup
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.ip2016.PsoDetailsViewMessages
import uk.gov.hmrc.play.views.html.helpers.{ErrorSummary, FormWithCSRF}
import views.html.pages.amends.amendPsoDetails

class AmendPsoDetailsViewSpec extends CommonViewSpecHelper with PsoDetailsViewMessages {

  implicit val errorSummary: ErrorSummary = app.injector.instanceOf[ErrorSummary]
  implicit val formWithCSRF: FormWithCSRF = app.injector.instanceOf[FormWithCSRF]

  "the AmendPsoDetailsView" should{
    val pensionsForm = AmendPSODetailsForm.amendPsoDetailsForm.bind(Map(
      "pso.day" -> "1",
      "pso.month" -> "2",
      "pso.year" -> "2017",
      "psoAmt" -> "12345",
      "protectionType" -> "ip2016",
      "status"         -> "open",
      "existingPSO"    -> "true"))

    lazy val view = application.injector.instanceOf[amendPsoDetails]
    lazy val doc = Jsoup.parse(view.apply(pensionsForm).body)

    lazy val errorForm =  AmendPSODetailsForm.amendPsoDetailsForm.bind(Map(
      "pso.day" -> "",
      "pso.month" -> "",
      "pso.year" -> "",
      "psoAmt" -> "a",
      "protectionType" -> "ip2016",
      "status"         -> "",
      "existingPSO"    -> "false"))

    lazy val errorView = application.injector.instanceOf[amendPsoDetails]
    lazy val errorDoc = Jsoup.parse(errorView.apply(errorForm).body)
    lazy val pageTitle = s"$plaPsoDetailsTitle - $plaBaseAppName - GOV.UK"

    lazy val form = doc.select("form")

    "have the correct title" in{
      doc.title() shouldBe pageTitle
    }

    "have the correct and properly formatted header"in{
      doc.select("h1").text shouldBe plaPsoDetailsTitle
    }

    "have the right headers for the PSO date and PSO amount" in{
      doc.select(".govuk-label--m").eq(0).text shouldBe plaPsoDetailsDateQuestionText
      doc.select(".govuk-label--m").eq(1).text shouldBe plaPsoDetailsPsoAmountQuestion
    }

    "have the right date hint message" in{

      doc.select("#pso-hint").text shouldBe plaPsoDetailsDateHintText
      errorDoc.select("#pso-hint").text shouldBe plaPsoDetailsDateHintText
    }

    "have the right text above each textbox" in{
      doc.select("[for=pso.day]").text shouldBe plaBaseDateFieldsDay
      doc.select("[for=pso.month]").text shouldBe plaBaseDateFieldsMonth
      doc.select("[for=pso.year]").text shouldBe plaBaseDateFieldsYear
    }

    "have a valid form" in{
      form.attr("method") shouldBe "POST"
      form.attr("action") shouldBe controllers.routes.AmendsController.submitAmendPsoDetails.url
    }

    "have a £ symbol present" in{
      doc.select(".govuk-input__prefix").text shouldBe "£"
    }

    "have a continue button" in{
      doc.select(".govuk-button").text shouldBe plaBaseUpdate
      doc.select(".govuk-button").attr("id") shouldBe "submit"
    }

    "display the correct errors appropriately" in{
      errorForm.hasErrors shouldBe true
      errorDoc.select("#error-summary-title").text shouldBe plaBaseErrorSummaryLabel
      errorDoc.select(".govuk-button").text shouldBe plaBaseAdd
      errorDoc.select(".govuk-error-summary__list li").eq(0).text shouldBe plaBaseErrorsDayEmpty
      errorDoc.select(".govuk-error-summary__list li").eq(1).text shouldBe plaBaseErrorsMonthEmpty
      errorDoc.select(".govuk-error-summary__list li").eq(2).text shouldBe plaBaseErrorsYearEmpty
      errorDoc.select(".govuk-error-summary__list li").eq(3).text shouldBe errorReal
    }

    "not have errors on valid pages" in{
      pensionsForm.hasErrors shouldBe false
      doc.select(".govuk-error-summary__list li").eq(0).text shouldBe ""
      doc.select(".govuk-error-summary__list li").eq(1).text shouldBe ""
      doc.select(".govuk-error-summary__list li").eq(2).text shouldBe ""
      doc.select(".govuk-error-summary__list li").eq(3).text shouldBe ""
    }
  }
}
