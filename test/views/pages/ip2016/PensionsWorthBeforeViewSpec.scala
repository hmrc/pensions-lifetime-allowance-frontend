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

import forms.PensionsWorthBeforeForm
import org.jsoup.Jsoup
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.ip2016.PensionsTakenBeforeViewMessages
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import views.html.pages.ip2016.pensionsWorthBefore

class PensionsWorthBeforeViewSpec extends CommonViewSpecHelper with PensionsTakenBeforeViewMessages {

  implicit val formWithCSRF: FormWithCSRF = app.injector.instanceOf[FormWithCSRF]

  "the PensionsTakenBeforeView" should {
    val pensionsForm = PensionsWorthBeforeForm.pensionsWorthBeforeForm("ip2016").bind(Map("pensionsWorthBeforeAmt" -> "1"))
    lazy val view = application.injector.instanceOf[pensionsWorthBefore]
    lazy val doc = Jsoup.parse(view.apply(pensionsForm).body)

    val errorForm =  PensionsWorthBeforeForm.pensionsWorthBeforeForm("ip2016").bind(Map("pensionsWorthBeforeAmt" -> ""))
    lazy val errorView = application.injector.instanceOf[pensionsWorthBefore]
    lazy val errorDoc = Jsoup.parse(errorView.apply(errorForm).body)

    lazy val form = doc.select("form")

    "have the correct title" in {
      doc.title() shouldBe plaPensionsWorthBeforeTitle
    }

    "have the correct and properly formatted header" in {
      doc.getElementsByClass("govuk-heading-xl").text shouldBe plaPensionsTakenBeforeQuestion
    }

    "have the right explanatory messages" in {
      doc.select("summary").text shouldBe plaPensionsTakenBeforeHelp
      doc.select("#ip16-pensions-worth-before-help > div > p:nth-child(1)").text shouldBe plaPensionsTakenBeforeParaOne
      doc.select("#ip16-pensions-worth-before-help > div > p:nth-child(3)").text shouldBe plaPensionsTakenBeforeParaTwo
      doc.select("#ip16-pensions-worth-before-help > div > p:nth-child(5)").text shouldBe plaPensionsTakenBeforeParaThreeNew
    }

    "have a hidden menu with the correct list values" in {
      doc.select("#ip16-pensions-worth-before-help > div > ol > li:nth-child(1)").text shouldBe plaPensionsTakenBeforeStepOne
      doc.select("#ip16-pensions-worth-before-help > div > ol > li:nth-child(2)").text shouldBe plaPensionsTakenBeforeStepTwo
      doc.select("#ip16-pensions-worth-before-help > div > ol > li:nth-child(3)").text shouldBe plaPensionsTakenBeforeStepThree
      doc.select("#ip16-pensions-worth-before-help > div > ul > li:nth-child(1)").text shouldBe plaPensionsTakenBeforeBulletOne
      doc.select("#ip16-pensions-worth-before-help > div > ul > li:nth-child(2)").text shouldBe plaPensionsTakenBeforeBulletTwo
    }

    "have a help link redirecting to the right place" in{
      doc.select("#ip16-pensions-worth-before-help-link").text shouldBe plaPensionsTakenBeforeHelpLinkTextNew
      doc.select("#ip16-pensions-worth-before-help-link").attr("href") shouldBe plaPensionsTakenBeforeHelpLinkLocation
    }

    "have a valid form" in {
      form.attr("method") shouldBe "POST"
      form.attr("action") shouldBe controllers.routes.IP2016Controller.submitPensionsWorthBefore.url
    }

    "have a £ symbol present" in {
      doc.select(".govuk-input__prefix").text shouldBe "£"
    }

    "have a continue button" in {
      doc.select("button").text shouldBe plaBaseContinue
      doc.getElementsByClass("govuk-button").attr("id") shouldBe "submit"
    }

    "display the correct errors appropriately" in {
      errorForm.hasErrors shouldBe true
      errorDoc.select(".govuk-error-summary__title").text shouldBe plaBaseErrorSummaryLabel
      errorDoc.select(".govuk-error-message").text shouldBe s"Error: $plaMandatoryAmountError"
    }

    "not have errors on valid pages" in{
      pensionsForm.hasErrors shouldBe false
      doc.select("span.error-notification").text shouldBe ""
    }
  }

}
