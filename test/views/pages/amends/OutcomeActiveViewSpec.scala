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

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.amends.OutcomeActiveViewSpecMessages
import testdata.AmendProtectionOutcomeViewsTestData.{
  amendsActiveResultModelIP14,
  amendsActiveResultModelIP16,
  amendsActiveResultModelIP16WithNoneProtectionReference,
  amendsGAModel
}
import views.html.pages.amends.outcomeActive

class OutcomeActiveViewSpec extends CommonViewSpecHelper with OutcomeActiveViewSpecMessages {

  private val view = app.injector.instanceOf[outcomeActive]

  lazy val docIP16: Document =
    Jsoup.parse(view.apply(amendsActiveResultModelIP16, Some(amendsGAModel), mockAppConfig).body)

  lazy val docIP14: Document =
    Jsoup.parse(view.apply(amendsActiveResultModelIP14, Some(amendsGAModel), mockAppConfig).body)

  private val NameHeader            = "Full name"
  private val NinoHeader            = "National Insurance number"
  private val ProtectionRefHeader   = "Protection reference number"
  private val PsaRefHeader          = "Pension scheme administrator check reference"
  private val ApplicationDateHeader = "Application date"

  "the OutcomeActiveView" should {

    "have the right success message displayed for IP14" in {
      val docIP14HipMigrationEnabled =
        Jsoup.parse(view.apply(amendsActiveResultModelIP14, Some(amendsGAModel), mockAppConfig).body)
      docIP14HipMigrationEnabled
        .select("#amendedAllowanceTextHip")
        .text() shouldBe plaResultSuccessAllowanceSubHeadingHip
    }

    "have the right success message displayed for IP16" in {
      docIP16
        .select("#amendedAllowanceTextHip")
        .text() shouldBe plaResultSuccessAllowanceSubHeadingHip
    }

    "have a properly structured 'Your protection details' section" when {
      "looking at the header" in {
        docIP16.select("h2.govuk-heading-m").eq(0).text() shouldBe plaResultSuccessProtectionDetails
      }

      "have the right print message" in {
        docIP16.select("a#printPage").text() shouldBe plaResultSuccessPrintNew
        docIP16
          .select("a#printPage")
          .attr("href") shouldBe controllers.routes.PrintController.printView.url
      }

      "not have back link with text back " in {
        val backButton = docIP16.select(".govuk-back-link").text()
        backButton.isEmpty shouldBe true
      }
    }

    "have a properly structured 'Changing your protection details' section" when {

      "ActiveAmendResultDisplayModel contains non-empty details" when {

        val tableHeadings = docIP16.select("tr th")
        val tableData     = docIP16.select("tr td")

        "not have back link with text back " in {
          val backButton = docIP16.select(".govuk-back-link").text()
          backButton.isEmpty shouldBe true
        }

        "looking at name row" in {
          val rowIndex = 0
          tableHeadings.get(rowIndex).text shouldBe NameHeader
          tableData.get(rowIndex).attr("id") shouldBe "yourFullName"
          tableData.get(rowIndex).text shouldBe "Jim Davis"
        }

        "looking at NINo row" in {
          val rowIndex = 1
          tableHeadings.get(rowIndex).text shouldBe NinoHeader
          tableData.get(rowIndex).attr("id") shouldBe "yourNino"
          tableData.get(rowIndex).text shouldBe "nino"
        }

        "looking at Protection Reference row" in {
          val rowIndex = 2
          tableHeadings.get(rowIndex).text shouldBe ProtectionRefHeader
          tableData.get(rowIndex).attr("id") shouldBe "protectionRef"
          tableData.get(rowIndex).text shouldBe "protectionRef"
        }

        "looking at PSA Reference row" in {
          val rowIndex = 3
          tableHeadings.get(rowIndex).text shouldBe PsaRefHeader
          tableData.get(rowIndex).attr("id") shouldBe "psaRef"
          tableData.get(rowIndex).text shouldBe "psaRef"
        }

        "looking at Application Date row" in {
          val rowIndex = 4
          tableHeadings.get(rowIndex).text shouldBe ApplicationDateHeader
          tableData.get(rowIndex).attr("id") shouldBe "applicationDate"
          tableData.get(rowIndex).text shouldBe "14 June 2017"
        }
      }

      "ActiveAmendResultDisplayModel contains non-empty details with None protectionRef" when {

        val docIP16 =
          Jsoup.parse(
            view
              .apply(amendsActiveResultModelIP16WithNoneProtectionReference, Some(amendsGAModel), mockAppConfig)
              .body
          )
        val tableHeadings = docIP16.select("tr th")
        val tableData     = docIP16.select("tr td")

        "looking at name row" in {
          val rowIndex = 0
          tableHeadings.get(rowIndex).text shouldBe NameHeader
          tableData.get(rowIndex).attr("id") shouldBe "yourFullName"
          tableData.get(rowIndex).text shouldBe "Jim Davis"
        }

        "looking at NINo row" in {
          val rowIndex = 1
          tableHeadings.get(rowIndex).text shouldBe NinoHeader
          tableData.get(rowIndex).attr("id") shouldBe "yourNino"
          tableData.get(rowIndex).text shouldBe "nino"
        }

        "looking at Protection Reference row" in {
          val rowIndex = 2
          tableHeadings.get(rowIndex).text shouldBe ProtectionRefHeader
          tableData.get(rowIndex).attr("id") shouldBe "protectionRef"
          tableData.get(rowIndex).text shouldBe "None"
        }

        "looking at PSA Reference row" in {
          val rowIndex = 3
          tableHeadings.get(rowIndex).text shouldBe PsaRefHeader
          tableData.get(rowIndex).attr("id") shouldBe "psaRef"
          tableData.get(rowIndex).text shouldBe "psaRef"
        }

        "looking at Application Date row" in {
          val rowIndex = 4
          tableHeadings.get(rowIndex).text shouldBe ApplicationDateHeader
          tableData.get(rowIndex).attr("id") shouldBe "applicationDate"
          tableData.get(rowIndex).text shouldBe "14 June 2017"
        }
      }

      "ActiveAmendResultDisplayModel contains empty details" when {

        val docIP16 =
          Jsoup.parse(
            view.apply(amendsActiveResultModelIP16.copy(details = None), Some(amendsGAModel), mockAppConfig).body
          )
        val tableHeadings = docIP16.select("tr th")
        val tableData     = docIP16.select("tr td")

        "looking at name row" in {
          val rowIndex = 0
          tableHeadings.get(rowIndex).text shouldBe NameHeader
          tableData.get(rowIndex).attr("id") shouldBe "yourFullName"
          tableData.get(rowIndex).text shouldBe "Jim Davis"
        }

        "looking at NINo row" in {
          val rowIndex = 1
          tableHeadings.get(rowIndex).text shouldBe NinoHeader
          tableData.get(rowIndex).attr("id") shouldBe "yourNino"
          tableData.get(rowIndex).text shouldBe "nino"
        }

        "looking at the whole table" in {
          tableHeadings.size() shouldBe 2
          tableData.size() shouldBe 2
        }
      }

    }

    "have a properly structured 'Give us feedback' section" when {

      "looking at the header" in {
        docIP16.select("h2.govuk-heading-m").eq(2).text() shouldBe plaResultSuccessGiveFeedback
      }

      "looking at the explanatory paragraph" in {
        docIP16
          .select("#main-content > div > div > p:nth-child(15)")
          .text() shouldBe plaResultSuccessExitSurvey
      }

      "using the feedback link" in {
        docIP16.select("#submit-survey-button").text() shouldBe plaResultSuccessExitSurveyLinkText
      }
    }

  }

}
