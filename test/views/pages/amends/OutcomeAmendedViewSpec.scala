/*
 * Copyright 2025 HM Revenue & Customs
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

import enums.ApplicationType
import models.{AmendResultDisplayModel, PrintDisplayModel}
import org.jsoup.Jsoup
import play.api.i18n.Messages
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.amends.OutcomeAmendedViewSpecMessages
import views.html.pages.amends.outcomeAmended

class OutcomeAmendedViewSpec extends CommonViewSpecHelper with OutcomeAmendedViewSpecMessages {

  val amendsActiveResultModelIP16 = AmendResultDisplayModel(
    protectionType = ApplicationType.IP2016,
    notificationId = 12,
    protectedAmount = "£1,350,000.45",
    details = Some(
      PrintDisplayModel(
        firstName = "Jim",
        surname = "Davis",
        nino = "nino",
        protectionType = "IP2016",
        status = "dormant",
        psaCheckReference = "psaRef",
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = Some("1,350,000.45"),
        certificateDate = Some("14/07/2017"),
        notificationId = 12
      )
    )
  )

  val amendsActiveResultModelIP14 = AmendResultDisplayModel(
    protectionType = ApplicationType.IP2014,
    notificationId = 5,
    protectedAmount = "£1,350,000.11",
    details = Some(
      PrintDisplayModel(
        firstName = "Jim",
        surname = "Davis",
        nino = "nino",
        protectionType = "IP2014",
        status = "dormant",
        psaCheckReference = "psaRef",
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = Some("1,350,000.11"),
        certificateDate = Some("14/07/2017"),
        notificationId = 5
      )
    )
  )

  lazy val outcomeAmendedView = fakeApplication().injector.instanceOf[outcomeAmended]
  lazy val viewIP16           = outcomeAmendedView(amendsActiveResultModelIP16)
  lazy val docIP16            = Jsoup.parse(viewIP16.body)

  lazy val viewIP14 = outcomeAmendedView(amendsActiveResultModelIP14)
  lazy val docIP14  = Jsoup.parse(viewIP14.body)

  "the OutcomeAmendView" should {
    "have the correct title" in {
      docIP16.title() shouldBe plaAmendTitle
    }

    "have the right success message displayed for IP16" in {
      docIP16.select("h1.govuk-panel__title").text() shouldBe plaAmendIP16Heading
      docIP16.select("#amendedAllowanceText").text() shouldBe plaAmendAllowanceSubHeading
      docIP16.select("strong#protectedAmount").text() shouldBe "£1,350,000.45"
    }

    "have the right inset Text for IP16" in {
      docIP16
        .select("div.govuk-inset-text")
        .text() shouldBe "Your individual protection 2016 is currently not active because you already hold fixed protection 2016."
    }

    "have a properly structured 'Your protection details' section" when {
      "looking at the header" in {
        docIP16.select("h2.govuk-heading-m").eq(0).text() shouldBe plaAmendProtectionDetails
      }

      "contain a table which" should {

        lazy val tableHeading = docIP16.select("tr th")

        "contain the following title message information" in {
          tableHeading.get(0).text shouldBe plaAmendName
          tableHeading.get(1).text shouldBe plaAmendNino
          tableHeading.get(2).text shouldBe plaAmendProtectionType
          tableHeading.get(3).text shouldBe plaAmendApplicationDate
          tableHeading.get(4).text shouldBe plaStatus
        }

        lazy val tableData = docIP16.select("tr td")

        "contain the following id information" in {
          tableData.get(0).attr("id") shouldBe "yourFullName"
          tableData.get(1).attr("id") shouldBe "yourNino"
          tableData.get(2).attr("id") shouldBe "protectionType"
          tableData.get(3).attr("id") shouldBe "applicationDate"
          tableData.get(4).attr("id") shouldBe "dormantStatus"
        }

        "contain the following table information" in {
          tableData.get(0).text shouldBe "Jim Davis"
          tableData.get(1).text shouldBe "nino"
          tableData.get(2).text shouldBe "Individual protection 2016"
          tableData.get(3).text shouldBe "14/07/2017"
          tableData.get(4).text shouldBe "Dormant"
        }
      }

      "have the right print message" in {
        docIP16.select("a#printPage").text() shouldBe plaAmendPrintNew
        docIP16.select("a#printPage").attr("href") shouldBe controllers.routes.PrintController.printView.url
      }
    }

    "have a properly structured 'Changing your protection details' section" when {
      "looking at the header" in {
        docIP16.select("h2.govuk-heading-m").eq(1).text() shouldBe plaAmendIPChangeDetails
      }

      "looking at the changeDetails paragraph" in {

        docIP16
          .select("#main-content > div > div > p:nth-child(8)")
          .text() shouldBe "If you lose your fixed protection 2016, your individual protection 2016 will become active and HMRC will issue a protection reference number and a pension scheme administrator check reference."
      }

      "looking at the changeDetails contact paragraph" in {

        docIP16
          .select("#main-content > div > div > p:nth-child(9)")
          .text() shouldBe "You must contact HMRC Pension Scheme services within 60 days if:"
      }

      "should contain a list of other contact options which" should {

        lazy val listOption = docIP16.select("#main-content > div > div > ul")

        s"include the list option fixedProtection2016" in {
          listOption.select("li:nth-child(1)").text shouldBe "you lose your fixed protection 2016"
        }

        s"include the list option pensionIsShared" in {
          listOption.select("li:nth-child(2)").text shouldBe "your pension is shared in a divorce or civil partnership"
        }
      }

      "looking at the whatToDo paragraph" in {

        docIP16
          .select("#main-content > div > div > p:nth-child(11)")
          .text() shouldBe "What to do if you lose your protection (opens in new tab)"
      }

      "looking at the explanatory paragraph" in {

        docIP16.select("#main-content > div > div > p:nth-child(12)").text() shouldBe plaResultSuccessViewDetails
      }

      "using the links" in {
        docIP16
          .select("#main-content > div > div > p:nth-child(12) > a")
          .text() shouldBe plaResultSuccessViewDetailsLinkText
        docIP16
          .select("#main-content > div > div > p:nth-child(12) > a")
          .attr("href") shouldBe controllers.routes.ReadProtectionsController.currentProtections.url
      }
    }

    "have a properly structured 'Give us feedback' section" when {
      "looking at the header" in {
        docIP16.select("h2.govuk-heading-m").eq(2).text() shouldBe plaResultSuccessGiveFeedback
      }
      "looking at the explanatory paragraph" in {
        docIP16.select("#main-content > div > div > p:nth-child(14)").text() shouldBe plaResultSuccessExitSurvey
      }
      "using the feedback link" in {
        docIP16.select("#submit-survey-button").text() shouldBe plaResultSuccessExitSurveyLinkText
      }
    }

  }

  "the OutcomeAmendView for IP14" should {
    "have the correct title for IP14" in {
      docIP14.title() shouldBe plaAmendTitle
    }

    "have the right success message displayed for IP14 for IP14" in {
      docIP14.select("h1.govuk-panel__title").text() shouldBe plaAmendIP14Heading
      docIP14.select("#amendedAllowanceText").text() shouldBe plaAmendAllowanceSubHeading
      docIP14.select("strong#protectedAmount").text() shouldBe "£1,350,000.11"
    }

    "have a properly structured 'Your protection details' section for IP14" when {
      "looking at the header for IP14" in {
        docIP14.select("h2.govuk-heading-m").eq(0).text() shouldBe plaAmendProtectionDetails
      }

      "contain a table which for IP14" should {

        lazy val tableHeading = docIP14.select("tr th")

        "contain the following title message information for IP14" in {
          tableHeading.get(0).text shouldBe plaAmendName
          tableHeading.get(1).text shouldBe plaAmendNino
          tableHeading.get(2).text shouldBe plaAmendProtectionType
          tableHeading.get(3).text shouldBe plaAmendProtectionRef
          tableHeading.get(4).text shouldBe plaAmendPsaRef
          tableHeading.get(5).text shouldBe plaAmendApplicationDate
        }

        lazy val tableData = docIP14.select("tr td")

        "contain the following id information for IP14" in {
          tableData.get(0).attr("id") shouldBe "yourFullName"
          tableData.get(1).attr("id") shouldBe "yourNino"
          tableData.get(2).attr("id") shouldBe "protectionType"
          tableData.get(3).attr("id") shouldBe "protectionRefNum"
          tableData.get(4).attr("id") shouldBe "psaCheckRef"
          tableData.get(5).attr("id") shouldBe "applicationDate"
        }

        "contain the following table information for IP14" in {
          tableData.get(0).text shouldBe "Jim Davis"
          tableData.get(1).text shouldBe "nino"
          tableData.get(2).text shouldBe "Individual protection 2014"
          tableData.get(3).text shouldBe Messages("pla.protection.protectionReference")
          tableData.get(4).text shouldBe "psaRef"
          tableData.get(5).text shouldBe "14/07/2017"
        }
      }

      "have the right print message for IP14" in {
        docIP14.select("a#printPage").text() shouldBe plaAmendPrintNew
        docIP14.select("a#printPage").attr("href") shouldBe controllers.routes.PrintController.printView.url
      }
    }

    "have a properly structured 'Changing your protection details' section for IP14" when {
      "looking at the header for IP14" in {
        docIP14.select("h2.govuk-heading-m").eq(1).text() shouldBe plaAmendIPChangeDetails
      }

      "looking at the suggestion paragraph for IP14" in {

        docIP14.select("#main-content > div > div > p:nth-child(7)").text() shouldBe plaAmendIPPensionSharing
      }

      "looking at the explanatory paragraph for IP14" in {

        docIP14.select("#main-content > div > div > p:nth-child(8)").text() shouldBe plaResultSuccessViewDetails
      }

      "using the links for IP14" in {
        docIP14
          .select("#main-content > div > div > p:nth-child(8) > a")
          .text() shouldBe plaResultSuccessViewDetailsLinkText
        docIP14
          .select("#main-content > div > div > p:nth-child(8) > a")
          .attr("href") shouldBe controllers.routes.ReadProtectionsController.currentProtections.url
      }
    }

    "have a properly structured 'Give us feedback' section for IP14" when {
      "looking at the header for IP14" in {
        docIP14.select("h2.govuk-heading-m").eq(2).text() shouldBe plaResultSuccessGiveFeedback
      }
      "looking at the explanatory paragraph for IP14" in {
        docIP14.select("#main-content > div > div > p:nth-child(10)").text() shouldBe plaResultSuccessExitSurvey
      }
      "using the feedback link for IP14" in {
        docIP14.select("#submit-survey-button").text() shouldBe plaResultSuccessExitSurveyLinkText
      }
    }

  }

}
