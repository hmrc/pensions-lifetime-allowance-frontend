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

import forms.PensionsTakenBeforeForm
import org.jsoup.Jsoup
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.ip2016.PensionsTakenBeforeViewMessages
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import views.html.pages.ip2016.pensionsTakenBefore

class PensionsTakenBeforeViewSpec extends CommonViewSpecHelper with PensionsTakenBeforeViewMessages {

  implicit val formWithCSRF: FormWithCSRF = app.injector.instanceOf[FormWithCSRF]

  "the PensionsTakenBeforeView" should {
    val pensionsForm = PensionsTakenBeforeForm.pensionsTakenBeforeForm("ip2016").bind(Map("pensionsTakenBefore" -> "yes"))
    lazy val view = application.injector.instanceOf[pensionsTakenBefore]
    lazy val doc = Jsoup.parse(view.apply(pensionsForm).body)

    val errorForm =  PensionsTakenBeforeForm.pensionsTakenBeforeForm("ip2016").bind(Map("pensionsTakenBefore" -> ""))
    lazy val errorView = application.injector.instanceOf[pensionsTakenBefore]
    lazy val errorDoc = Jsoup.parse(errorView.apply(errorForm).body)

    lazy val form = doc.select("form")

    "have the correct title" in{
      doc.title() shouldBe plaPensionsTakenBeforeTitle
    }

    "have the correct and properly formatted header" in {
      doc.getElementsByClass("govuk-fieldset__heading").text shouldBe plaPensionsTakenBeforeHeading
    }

    "have a valid form" in {
      form.attr("method") shouldBe "POST"
      form.attr("action") shouldBe controllers.routes.IP2016Controller.submitPensionsTakenBefore.url
    }

    "have a pair of yes/no buttons" in{
      doc.select("[for=pensionsTakenBefore]").text shouldBe plaBaseYes
      doc.select("input#pensionsTakenBefore").attr("type") shouldBe "radio"
      doc.select("[for=pensionsTakenBefore-2]").text shouldBe plaBaseNo
      doc.select("input#pensionsTakenBefore-2").attr("type") shouldBe "radio"
    }

    "have a continue button" in{
      doc.select("button").text shouldBe plaBaseContinue
      doc.getElementsByClass("govuk-button").attr("id") shouldBe "submit"
    }

    "display the correct errors appropriately" in{
      errorForm.hasErrors shouldBe true
      errorDoc.select(".govuk-error-summary__title").text shouldBe plaBaseErrorSummaryLabel
      errorDoc.select(".govuk-error-message").text shouldBe s"Error: $plaMandatoryError"
    }

    "not have errors on valid pages" in{
      pensionsForm.hasErrors shouldBe false
      doc.select("span.error-notification").text shouldBe ""
    }
  }
}
