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

import forms.PensionsTakenForm
import org.jsoup.Jsoup
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.ip2016.PensionsTakenViewMessages
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import views.html.pages.ip2016.pensionsTaken

class PensionsTakenViewSpec extends CommonViewSpecHelper with PensionsTakenViewMessages {

  implicit val formWithCSRF: FormWithCSRF = app.injector.instanceOf[FormWithCSRF]

  "the PensionsTakenView" should{
    val pensionsForm = PensionsTakenForm.pensionsTakenForm("ip2016").bind(Map("pensionsTaken" -> "Yes"))
    lazy val view = application.injector.instanceOf[pensionsTaken]
    lazy val doc = Jsoup.parse(view.apply(pensionsForm).body)

    val errorForm =  PensionsTakenForm.pensionsTakenForm("ip2016").bind(Map.empty[String, String])
    lazy val errorView = application.injector.instanceOf[pensionsTaken]
    lazy val errorDoc = Jsoup.parse(errorView.apply(errorForm).body)

    lazy val form = doc.select("form")
    "have the correct title" in{
      doc.title() shouldBe plaPensionsTakenTitle
    }

    "have a valid form" in{
      form.attr("method") shouldBe "POST"
      form.attr("action") shouldBe controllers.routes.IP2016Controller.submitPensionsTaken.url
      doc.getElementsByClass("govuk-fieldset__heading").text shouldBe plaPensionsTakenLegendText

    }

    "have a pair of yes/no buttons" in{
      doc.select("[for=pensionsTaken]").text shouldBe plaBaseYes
      doc.select("input#pensionsTaken").attr("type") shouldBe "radio"
      doc.select("[for=pensionsTaken-2]").text shouldBe plaBaseNo
      doc.select("input#pensionsTaken-2").attr("type") shouldBe "radio"
    }

    "have a continue button" in{
      doc.select(".govuk-button").text shouldBe plaBaseContinue
      doc.select(".govuk-button").attr("type") shouldBe "submit"
    }

    "display the correct errors appropriately" in{
      errorForm.hasErrors shouldBe true
      errorDoc.select(".govuk-error-summary").size() shouldBe 1
      errorDoc.select(".govuk-error-message").size() shouldBe 1
    }

    "not have errors on valid pages" in{
      pensionsForm.hasErrors shouldBe false
      doc.select(".govuk-error-summary").text shouldBe ""
    }
  }
}
