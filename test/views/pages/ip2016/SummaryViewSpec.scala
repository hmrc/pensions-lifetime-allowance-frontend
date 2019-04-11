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

import enums.ApplicationType
import models.{SummaryModel, SummaryRowModel, SummarySectionModel}
import org.jsoup.Jsoup
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.ip2016.SummaryViewMessages
import views.html.pages.ip2016.{summary => views}

class SummaryViewSpec  extends CommonViewSpecHelper with SummaryViewMessages {

  def totalPensionsAmountSummaryRow(totalAmount: String) = SummaryRowModel("totalPensionsAmt", None, None, true, totalAmount)

  val psoDetailsSummaryRow = SummaryRowModel("psoDetails", Some(controllers.routes.IP2016Controller.psoDetails), Some(controllers.routes.IP2016Controller.removePsoDetails), false, "£10,000", "1 February 2016")

  "the SummaryView" should{
    val positivePensionsTakenSummaryRow = SummaryRowModel("pensionsTaken", Some(controllers.routes.IP2016Controller.pensionsTaken()), None,  false, "Yes")
    val positivePensionsTakenBeforeSummaryRow = SummaryRowModel("pensionsTakenBefore", Some(controllers.routes.IP2016Controller.pensionsTakenBefore()), None,  false, "Yes")
    val positivePensionsTakenBeforeAmtSummaryRow = SummaryRowModel("pensionsTakenBeforeAmt", Some(controllers.routes.IP2016Controller.pensionsTakenBefore()), None,  false, "£1,001,000")
    val positivePensionsTakenBetweenSummaryRow = SummaryRowModel("pensionsTakenBetween", Some(controllers.routes.IP2016Controller.pensionsTakenBetween()), None,  false, "Yes")
    val positivePensionsTakenBetweenAmtSummaryRow = SummaryRowModel("pensionsTakenBetweenAmt", Some(controllers.routes.IP2016Controller.pensionsTakenBetween()), None,  false, "£1,100")
    val positiveOverseasPensionsSummaryRow = SummaryRowModel("overseasPensions", Some(controllers.routes.IP2016Controller.overseasPensions()), None,  false, "Yes")
    val positiveOverseasPensionsAmtSummaryRow = SummaryRowModel("overseasPensionsAmt", Some(controllers.routes.IP2016Controller.overseasPensions()), None,  false, "£1,010")
    val currentPensionsSummaryRow = SummaryRowModel("currentPensionsAmt", Some(controllers.routes.IP2016Controller.currentPensions()), None, false, "£1,001")
    val positivePensionDebitsSummaryRow = SummaryRowModel("pensionDebits", Some(controllers.routes.IP2016Controller.pensionDebits()), None,  false, "Yes")

    val positivePensionsTakenSummaryRowTwo = SummaryRowModel("pensionsTaken", Some(controllers.routes.IP2016Controller.pensionsTaken()), None,  false, "No")
    val positiveOverseasPensionsSummaryRowTwo = SummaryRowModel("overseasPensions", Some(controllers.routes.IP2016Controller.overseasPensions()), None,  false, "No")
    val currentPensionsSummaryRowTwo = SummaryRowModel("currentPensionsAmt", Some(controllers.routes.IP2016Controller.currentPensions()), None, false, "£123,456")
    val positivePensionDebitsSummaryRowTwo = SummaryRowModel("pensionDebits", Some(controllers.routes.IP2016Controller.pensionDebits()), None,  false, "No")


    val model = SummaryModel( ApplicationType.IP2016, false,
      List(
        SummarySectionModel(List(
          positivePensionsTakenSummaryRow)),
        SummarySectionModel(List(
          positivePensionsTakenBeforeSummaryRow, positivePensionsTakenBeforeAmtSummaryRow)),
        SummarySectionModel(List(
          positivePensionsTakenBetweenSummaryRow, positivePensionsTakenBetweenAmtSummaryRow)),
        SummarySectionModel(List(
          positiveOverseasPensionsSummaryRow, positiveOverseasPensionsAmtSummaryRow)),
        SummarySectionModel(List(
          currentPensionsSummaryRow)),
        SummarySectionModel(List(
          totalPensionsAmountSummaryRow("£1,004,111")))
      ),
      List(
        SummarySectionModel(List(
          positivePensionDebitsSummaryRow)),
        SummarySectionModel(List(
          psoDetailsSummaryRow))
      )
    )

    val modelTwo = SummaryModel( ApplicationType.IP2016, false,
      List(
        SummarySectionModel(List(
          positivePensionsTakenSummaryRowTwo)),
        SummarySectionModel(List(
          positiveOverseasPensionsSummaryRowTwo, positiveOverseasPensionsAmtSummaryRow)),
        SummarySectionModel(List(
          currentPensionsSummaryRowTwo)),
        SummarySectionModel(List(
          totalPensionsAmountSummaryRow("£123,456")))
      ),
      List(
        SummarySectionModel(List(
          positivePensionDebitsSummaryRowTwo)),
        SummarySectionModel(List(
          psoDetailsSummaryRow))
      )
    )

    val errorModel = SummaryModel(
      protectionType = ApplicationType.IP2016,
      invalidRelevantAmount = true,
      pensionContributionSections = List.empty,
      psoDetailsSections = List.empty
    )

    lazy val view = views(model)
    lazy val doc = Jsoup.parse(view.body)

    lazy val viewTwo = views(modelTwo)
    lazy val docTwo = Jsoup.parse(viewTwo.body)

    lazy val errorView = views(errorModel)
    lazy val errorDoc = Jsoup.parse(errorView.body)

    lazy val form = doc.select("form")
    "have the correct title" in{
      doc.title() shouldBe plaSummaryTitle
    }

    "have the correct and properly formatted header"in{
      doc.select("h1").first().text shouldBe plaSummaryPageHeading
    }

    "have a properly structured table" when{
      "looking at the value of pensions" in{
        doc.select("span#pensionsTakenQuestionText.small").text shouldBe plaSummaryQuestionsPensionsTaken
        doc.select("span#pensionsTakenDisplayValue0.medium").text shouldBe plaBaseYes
        doc.select("a#pensionsTakenChangeLink.bold-xsmall").text shouldBe plaBaseChange
      }

      "looking at the total value of pensions" in{
        doc.select("td#total-message.total-font.summary-text").text shouldBe plaSummaryQuestionsTotalPensionsAmt
        doc.select("td#totalPensionsAmtDisplayValue0").text shouldBe "£1,004,111"
      }

      "looking at the value of pension sharing orders" in{
        doc.select("span#pensionDebitsQuestionText.small").text shouldBe plaSummaryQuestionsPensionDebits
        doc.select("span#pensionDebitsDisplayValue0.medium").text shouldBe plaBaseYes
      }

      "with the correct visually hidden text"in{
        doc.select("span.visuallyhidden").eq(0).text shouldBe plaSummaryHiddenLinkTextPensionsTaken
        doc.select("span.visuallyhidden").eq(1).text shouldBe plaSummaryHiddenLinkTextPensionsTakenBefore
        doc.select("span.visuallyhidden").eq(2).text shouldBe plaSummaryHiddenLinkTextPensionsTakenBeforeAmt
        doc.select("span.visuallyhidden").eq(3).text shouldBe plaSummaryHiddenLinkTextPensionsTakenBetween
        doc.select("span.visuallyhidden").eq(4).text shouldBe plaSummaryHiddenLinkTextPensionsTakenBetweenAmt
        doc.select("span.visuallyhidden").eq(5).text shouldBe plaSummaryHiddenLinkTextOverseasPensions
        doc.select("span.visuallyhidden").eq(6).text shouldBe plaSummaryHiddenLinkTextOverseasPensionsAmt
        doc.select("span.visuallyhidden").eq(7).text shouldBe plaSummaryHiddenLinkTextCurrentPensionsAmt
        doc.select("span.visuallyhidden").eq(8).text shouldBe plaSummaryHiddenLinkTextPensionDebits
        doc.select("span.visuallyhidden").eq(10).text shouldBe plaSummaryHiddenLinkTextPsoDetails
      }

      "that works with different values" in{
        docTwo.select("span#pensionsTakenQuestionText.small").text shouldBe plaSummaryQuestionsPensionsTaken
        docTwo.select("span#pensionsTakenDisplayValue0.medium").text shouldBe plaBaseNo
        docTwo.select("a#pensionsTakenChangeLink.bold-xsmall").text shouldBe plaBaseChange
        docTwo.select("td#total-message.total-font.summary-text").text shouldBe plaSummaryQuestionsTotalPensionsAmt
        docTwo.select("td#totalPensionsAmtDisplayValue0").text shouldBe "£123,456"
        docTwo.select("span#pensionDebitsQuestionText.small").text shouldBe plaSummaryQuestionsPensionDebits
        docTwo.select("span#pensionDebitsDisplayValue0.medium").text shouldBe plaBaseNo
      }
    }

    "have the right headers" in{
      doc.select("h1").eq(1).text shouldBe plaSummaryGetIP16
      doc.select("h2").eq(0).text shouldBe plaSummaryPensionsHeading
      doc.select("h2").eq(1).text shouldBe plaSummaryPsosHeading
    }

    "have the right explanatory text" in{
      doc.select("p").first().text shouldBe plaSummaryMustAgree
      doc.select("p").eq(1).text shouldBe plaSummaryConfirmation
      doc.select("p").eq(6).text shouldBe plaSummaryConfirmation2
      doc.select("p").last().text shouldBe plaSummaryDeclaration
    }

    "have the correct bullet point messages" in{
      doc.select("li").eq(0).text shouldBe plaSummaryConfirm1
      doc.select("li").eq(1).text shouldBe plaSummaryConfirm2
      doc.select("li").eq(2).text shouldBe plaSummaryConfirmBullet1
      doc.select("li").eq(3).text shouldBe plaSummaryConfirmBullet2
    }

    "have a hidden drop menu with the correct values" in{
      doc.select("summary").text shouldBe plaSummaryHelp
      doc.select("p").eq(3).text shouldBe plaSummaryHiddenParaOne
      doc.select("p").eq(4).text shouldBe plaSummaryHiddenParaTwo
    }

    "have a help link redirecting to the right location" in{
      doc.select("a#ip16-help-link").text() shouldBe plaSummaryHiddenParaLinkText
      doc.select("a#ip16-help-link").attr("href") shouldBe plaHelpLinkLocation
    }

    "have a valid form submission" in{
      form.attr("method") shouldBe "POST"
      form.attr("action") shouldBe controllers.routes.ResultController.processIPApplication().url
    }

    "have a continue button" in{
      doc.select("button").text shouldBe plaBaseSubmitApplication
      doc.select("button").attr("type") shouldBe "submit"
    }

    "have the right error messages" in{
      errorDoc.select("h2").first().text shouldBe plaSummaryErrorSummaryLabel
    }
  }
}
