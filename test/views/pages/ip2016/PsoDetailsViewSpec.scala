/*
 * Copyright 2019 HM Revenue & Customs
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
import views.html.pages.ip2016.{psoDetails => views}

class PsoDetailsViewSpec extends CommonViewSpecHelper with PsoDetailsViewMessages {

  "the PsoDetailsView" should{
    val pensionsForm = PSODetailsForm.psoDetailsForm.bind(Map(
      "psoDay" -> "1",
      "psoMonth" -> "2",
      "psoYear" -> "2017",
      "psoAmt" -> "12345"))

    lazy val view = views(pensionsForm)
    lazy val doc = Jsoup.parse(view.body)

    val errorForm =  PSODetailsForm.psoDetailsForm.bind(Map.empty[String, String])
    lazy val errorView = views(errorForm)
    lazy val errorDoc = Jsoup.parse(errorView.body)

    lazy val form = doc.select("form")

    "have the correct title" in{
      doc.title() shouldBe plaPsoDetailsTitle
    }

    "have the correct and properly formatted header"in{
      doc.select("h1").text shouldBe plaPsoDetailsTitle
    }

    "have the right headers for the PSO date and PSO amount" in{
      doc.select("h2").eq(0).text shouldBe plaPsoDetailsDateQuestionText
      doc.select("h2").eq(1).text shouldBe plaPsoDetailsPsoAmountQuestion
    }

    "have the right date hint message" in{
      doc.select("span.form-hint").text shouldBe plaPsoDetailsDateHintText
    }

    "have the right explanatory paragraph" in{
      doc.select("p").text shouldBe plaPsoDetailsVisitPTA
    }

    "have the right text above each textbox" in{
      doc.select("[for=psoDay]").text shouldBe plaBaseDateFieldsDay
      doc.select("[for=psoMonth]").text shouldBe plaBaseDateFieldsMonth
      doc.select("[for=psoYear]").text shouldBe plaBaseDateFieldsYear
    }

    "have a valid form" in{
      form.attr("method") shouldBe "POST"
      form.attr("action") shouldBe controllers.routes.IP2016Controller.submitPSODetails().url
    }

    "have a £ symbol present" in{
      doc.select(".poundSign").text shouldBe "£"
    }

    "have a continue button" in{
      doc.select("button").text shouldBe plaBaseContinue
      doc.select("button").attr("type") shouldBe "submit"
    }

    "display the correct errors appropriately" in{
      errorForm.hasErrors shouldBe true
      errorDoc.select("h2.h3-heading").text shouldBe plaBaseErrorSummaryLabel
      errorDoc.select("span.error-notification").eq(0).text shouldBe plaBaseErrorsDayEmpty
      errorDoc.select("span.error-notification").eq(1).text shouldBe plaBaseErrorsMonthEmpty
      errorDoc.select("span.error-notification").eq(2).text shouldBe plaBaseErrorsYearEmpty
      errorDoc.select("span.error-notification").eq(3).text shouldBe errorRequired
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
