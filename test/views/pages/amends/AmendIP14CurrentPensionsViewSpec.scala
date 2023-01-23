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

import forms.AmendCurrentPensionForm
import org.jsoup.Jsoup
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.amends.AmendIP14CurrentPensionsViewSpecMessages
import uk.gov.hmrc.play.views.html.helpers.{ErrorSummary, FormWithCSRF}
import views.html.pages.amends.amendIP14CurrentPensions

class AmendIP14CurrentPensionsViewSpec extends CommonViewSpecHelper with AmendIP14CurrentPensionsViewSpecMessages {

  implicit val errorSummary: ErrorSummary = app.injector.instanceOf[ErrorSummary]
  implicit val formWithCSRF: FormWithCSRF = app.injector.instanceOf[FormWithCSRF]

  "the AmendIP14CurrentPensionsView" should{
    val amendCurrentPensionsForm = AmendCurrentPensionForm.amendCurrentPensionForm.bind(Map("amendedUKPensionAmt" -> "12000",
      "protectionType" -> "ip2014",
      "status" -> "open"))
    lazy val view = application.injector.instanceOf[amendIP14CurrentPensions]
    lazy val doc = Jsoup.parse(view.apply(amendCurrentPensionsForm).body)

    val errorForm = AmendCurrentPensionForm.amendCurrentPensionForm.bind(Map("amendedUKPensionAmt" -> "a",
      "protectionType" -> "ip2014",
      "status" -> "open"))
    lazy val errorView = application.injector.instanceOf[amendIP14CurrentPensions]
    lazy val errorDoc = Jsoup.parse(errorView.apply(errorForm).body)
    lazy val form = doc.select("form")

    "have the correct title" in{
      doc.title() shouldBe plaIp14CurrentPensionsTitleNew
    }

    "have the correct and properly formatted header"in{
      doc.getElementsByClass("govuk-heading-xl").text shouldBe plaIp14CurrentPensionsTitle
    }

    "have some introductory text" in{
      doc.select("p").first().text shouldBe plaCurrentPensionsQuestion
    }

    "have a hidden menu with the correct values" in{
      doc.select("summary").text shouldBe plaCurrentPensionsHiddenLink
      doc.select("#ip14-amend-current-pensions-help > div > p:nth-child(1)").text shouldBe plaIp14CurrentPensionsHiddenTextPara
      doc.select("#ip14-amend-current-pensions-help > div > ul > li:nth-child(1)").text shouldBe plaHiddenMenuItemOne
      doc.select("#ip14-amend-current-pensions-help > div > ul > li:nth-child(2)").text shouldBe plaHiddenMenuItemTwo
      doc.select("#ip14-amend-current-pensions-help > div > ul > li:nth-child(3)").text shouldBe plaHiddenMenuItemThree
      doc.select("#ip14-amend-current-pensions-help > div > ul > li:nth-child(4)").text shouldBe plaHiddenMenuItemFour
    }

    "have a help link redirecting to the right location" in{
      doc.select("#ip14-amend-current-pensions-help > div > p:nth-child(3)").text shouldBe plaHelpLinkCompleteMessageNew
      doc.select("#ip14-amend-current-pensions-help-link").text shouldBe plaHelpLinkNew
      doc.select("#ip14-amend-current-pensions-help-link").attr("href") shouldBe plaHelpLinkExternalReference
    }

    "has a valid form" in{
      form.attr("method") shouldBe "POST"
      form.attr("action") shouldBe controllers.routes.AmendsController.submitAmendCurrentPension.url
      doc.getElementsByClass("govuk-visually-hidden").eq(1).text() shouldBe plaIp14CurrentPensionsTitle
    }

    "have a £ symbol present" in{
      doc.select(".govuk-input__prefix").text shouldBe "£"
    }

    "have a continue button" in{
      doc.getElementsByClass("govuk-button").text shouldBe plaBaseChange
      doc.getElementsByClass("govuk-button").attr("id") shouldBe "submit"
    }

    "display the correct errors appropriately" in{
      errorDoc.select("#error-summary-title").text shouldBe plaBaseErrorSummaryLabel
    }

    "not have errors on valid pages" in{
      amendCurrentPensionsForm.hasErrors shouldBe false
      doc.select("a#amendedUKPensionAmt-error-summary").text shouldBe ""
      doc.select("span#amendedUKPensionAmt-error-message.error-notification").text shouldBe ""
    }
  }
}
