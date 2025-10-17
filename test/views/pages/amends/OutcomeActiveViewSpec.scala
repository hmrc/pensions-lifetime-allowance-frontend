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
import org.mockito.Mockito.when
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

  private val docIP16HipMigrationDisabled = {
    when(mockAppConfig.hipMigrationEnabled).thenReturn(false)
    Jsoup.parse(view.apply(amendsActiveResultModelIP16, Some(amendsGAModel), mockAppConfig).body)
  }

  lazy val docIP16 = Jsoup.parse(view.apply(amendsActiveResultModelIP16, Some(amendsGAModel), mockAppConfig).body)

  lazy val docIP14 = Jsoup.parse(view.apply(amendsActiveResultModelIP14, Some(amendsGAModel), mockAppConfig).body)

  private val NameHeader            = "Full name"
  private val NinoHeader            = "National Insurance number"
  private val ProtectionRefHeader   = "Protection reference number"
  private val PsaRefHeader          = "Pension scheme administrator check reference"
  private val ApplicationDateHeader = "Application date"
  private val ApplicationTimeHeader = "Application time"

  "the OutcomeActiveView" should {

    "have the correct title" in {
      docIP16HipMigrationDisabled.title() shouldBe plaResultSuccessOutcomeActiveTitleNew
    }

    "have the right success message displayed for IP16" in {
      docIP16HipMigrationDisabled.select("h1.govuk-panel__title").text() shouldBe plaResultSuccessIP16Heading
      docIP16HipMigrationDisabled.select("#amendedAllowanceText").text() shouldBe plaResultSuccessAllowanceSubHeading
      docIP16HipMigrationDisabled.select("strong#protectedAmount").text() shouldBe "£1,350,000.45"
    }

    "have the right success message displayed for IP14" in {
      val docIP14HipMigrationDisabled = {
        when(mockAppConfig.hipMigrationEnabled).thenReturn(false)
        Jsoup.parse(view.apply(amendsActiveResultModelIP14, Some(amendsGAModel), mockAppConfig).body)
      }

      docIP14HipMigrationDisabled.select("h1.govuk-panel__title").text() shouldBe plaResultSuccessIP14Heading
      docIP14HipMigrationDisabled.select("#amendedAllowanceText").text() shouldBe plaResultSuccessAllowanceSubHeading
      docIP14HipMigrationDisabled.select("strong#protectedAmount").text() shouldBe "£1,350,000.11"
    }

    "have the right success message displayed for IP14  when hip enabled" in {
      val docIP14HipMigrationEnabled = {
        when(mockAppConfig.hipMigrationEnabled).thenReturn(true)
        Jsoup.parse(view.apply(amendsActiveResultModelIP14, Some(amendsGAModel), mockAppConfig).body)
      }
      docIP14HipMigrationEnabled
        .select("#amendedAllowanceTextHip")
        .text() shouldBe plaResultSuccessAllowanceSubHeadingHip
    }

    "have the right success message displayed for IP16 when hip enabled " in {
      val docIP16HipMigrationEnabled = {
        when(mockAppConfig.hipMigrationEnabled).thenReturn(true)
        Jsoup.parse(view.apply(amendsActiveResultModelIP16, Some(amendsGAModel), mockAppConfig).body)
      }
      docIP16HipMigrationEnabled
        .select("#amendedAllowanceTextHip")
        .text() shouldBe plaResultSuccessAllowanceSubHeadingHip
    }

    "have a properly structured 'Your protection details' section" when {
      "looking at the header" in {
        docIP16HipMigrationDisabled.select("h2.govuk-heading-m").eq(0).text() shouldBe plaResultSuccessProtectionDetails
      }

      "looking at the explanatory paragraph" in {
        docIP16HipMigrationDisabled
          .select("#main-content > div > div > p:nth-child(8)")
          .text() shouldBe plaResultSuccessDetailsContent
      }

      "looking at the bullet point list" in {
        val details = amendsActiveResultModelIP16.details.get
        docIP16HipMigrationDisabled.select("li#yourFullName").text() shouldBe plaResultSuccessYourName
        docIP16HipMigrationDisabled.select("li#yourNino").text() shouldBe plaResultSuccessYourNino
        docIP16HipMigrationDisabled
          .select("li#protectionRef")
          .text() shouldBe plaResultSuccessProtectionRef + s": ${details.protectionReference}"
        docIP16HipMigrationDisabled
          .select("li#psaRef")
          .text() shouldBe plaResultSuccessPsaRef + s": ${details.psaReference}"
        docIP16HipMigrationDisabled
          .select("li#applicationDate")
          .text() shouldBe plaResultSuccessApplicationDate + s": ${details.applicationDate.get}"
      }
      "have the right print message" in {
        docIP16HipMigrationDisabled.select("a#printPage").text() shouldBe plaResultSuccessPrintNew
        docIP16HipMigrationDisabled
          .select("a#printPage")
          .attr("href") shouldBe controllers.routes.PrintController.printView.url
      }

      "not have back link with text back " in {
        val backButton = docIP16HipMigrationDisabled.select(".govuk-back-link").text()
        backButton.isEmpty shouldBe true
      }
    }

    "have a properly structured 'Changing your protection details' section" when {

      "FrontendAppConfig.hipMigrationEnabled returns false" when {

        "looking at the header" in {
          docIP16HipMigrationDisabled.select("h2.govuk-heading-m").eq(1).text() shouldBe plaResultSuccessIPChangeDetails
        }

        "looking at the explanatory paragraph" in {
          docIP16HipMigrationDisabled.select("p#ipPensionSharing").text() shouldBe plaResultSuccessIPPensionSharing
          docIP16HipMigrationDisabled
            .select("#main-content > div > div > p:nth-child(13)")
            .text() shouldBe plaResultSuccessViewDetails
        }

        "using the links" in {
          docIP16HipMigrationDisabled
            .select("#ipPensionSharing > a")
            .text() shouldBe plaResultSuccessIPPensionSharingLinkText
          docIP16HipMigrationDisabled
            .select("#ipPensionSharing > a")
            .attr("href") shouldBe plaResultSuccessIPPensionsSharingLink
          docIP16HipMigrationDisabled
            .select("#main-content > div > div > p:nth-child(13) > a")
            .text() shouldBe plaResultSuccessViewDetailsLinkText
          docIP16HipMigrationDisabled
            .select("#main-content > div > div > p:nth-child(13) > a")
            .attr("href") shouldBe controllers.routes.ReadProtectionsController.currentProtections.url
        }
      }

      "FrontendAppConfig.hipMigrationEnabled returns true" when {

        "ActiveAmendResultDisplayModel contains non-empty details" when {

          val docIP16HipMigrationEnabled = {
            when(mockAppConfig.hipMigrationEnabled).thenReturn(true)
            Jsoup.parse(view.apply(amendsActiveResultModelIP16, Some(amendsGAModel), mockAppConfig).body)
          }
          val tableHeadings = docIP16HipMigrationEnabled.select("tr th")
          val tableData     = docIP16HipMigrationEnabled.select("tr td")

          "not have back link with text back " in {
            val backButton = docIP16HipMigrationEnabled.select(".govuk-back-link").text()
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

          val docIP16HipMigrationEnabled = {
            when(mockAppConfig.hipMigrationEnabled).thenReturn(true)
            Jsoup.parse(
              view
                .apply(amendsActiveResultModelIP16WithNoneProtectionReference, Some(amendsGAModel), mockAppConfig)
                .body
            )
          }
          val tableHeadings = docIP16HipMigrationEnabled.select("tr th")
          val tableData     = docIP16HipMigrationEnabled.select("tr td")

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

          val docIP16HipMigrationEnabled = {
            when(mockAppConfig.hipMigrationEnabled).thenReturn(true)
            Jsoup.parse(
              view.apply(amendsActiveResultModelIP16.copy(details = None), Some(amendsGAModel), mockAppConfig).body
            )
          }
          val tableHeadings = docIP16HipMigrationEnabled.select("tr th")
          val tableData     = docIP16HipMigrationEnabled.select("tr td")

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
    }

    "have a properly structured 'Give us feedback' section" when {

      "looking at the header" in {
        docIP16HipMigrationDisabled.select("h2.govuk-heading-m").eq(2).text() shouldBe plaResultSuccessGiveFeedback
      }

      "looking at the explanatory paragraph" in {
        docIP16HipMigrationDisabled
          .select("#main-content > div > div > p:nth-child(15)")
          .text() shouldBe plaResultSuccessExitSurvey
      }

      "using the feedback link" in {
        docIP16HipMigrationDisabled.select("#submit-survey-button").text() shouldBe plaResultSuccessExitSurveyLinkText
      }
    }

  }

}
