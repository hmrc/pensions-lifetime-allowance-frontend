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

import forms.PSODetailsForm
import org.jsoup.Jsoup
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.ip2016.PsoDetailsViewMessages
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import views.html.pages.ip2016.psoDetails

class PsoDetailsViewSpec extends CommonViewSpecHelper with PsoDetailsViewMessages {

  implicit val formWithCSRF: FormWithCSRF = app.injector.instanceOf[FormWithCSRF]

  "the PsoDetailsView" should{
    val pensionsForm = PSODetailsForm.psoDetailsForm.bind(Map(
      "pso.day" -> "1",
      "pso.month" -> "2",
      "pso.year" -> "2017",
      "psoAmt" -> "12345"))

    lazy val view = application.injector.instanceOf[psoDetails]
    lazy val doc = Jsoup.parse(view.apply(pensionsForm).body)

    val errorForm =  PSODetailsForm.psoDetailsForm.bind(Map.empty[String, String])
    lazy val errorView = application.injector.instanceOf[psoDetails]
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
    }

    "have the right explanatory paragraph" in{
      doc.select("p").text shouldBe plaPsoDetailsVisitPTA
    }

    "have the right text above each textbox" in{
      doc.select("[for=pso.day]").text shouldBe plaBaseDateFieldsDay
      doc.select("[for=pso.month]").text shouldBe plaBaseDateFieldsMonth
      doc.select("[for=pso.year]").text shouldBe plaBaseDateFieldsYear
    }

    "have a valid form" in{
      form.attr("method") shouldBe "POST"
      form.attr("action") shouldBe controllers.routes.IP2016Controller.submitPSODetails.url
    }

    "have a £ symbol present" in{
      doc.select(".govuk-input__prefix").text shouldBe "£"
    }

    "have a continue button" in {
      doc.select("#submit").text shouldBe plaBaseContinue
    }

    s"display the correct errors appropriately ${errorForm.errors}" in{
      errorForm.hasErrors shouldBe true
      errorDoc.select(".govuk-error-summary__title").text shouldBe plaBaseErrorSummaryLabel
      errorDoc.select(".govuk-error-summary__list li").eq(0).text shouldBe plaErrorRequired
      errorDoc.select(".govuk-error-summary__list li").eq(1).text shouldBe plaMandatoryError
    }

    "not have errors on valid pages" in{
      pensionsForm.hasErrors shouldBe false
      doc.select("span.error-notification").eq(0).text shouldBe ""
      doc.select("span.error-notification").eq(1).text shouldBe ""
      doc.select("span.error-notification").eq(2).text shouldBe ""
      doc.select("span.error-notification").eq(3).text shouldBe ""
    }
  }
}
