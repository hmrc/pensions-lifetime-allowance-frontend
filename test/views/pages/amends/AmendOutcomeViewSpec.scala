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

import models.display.AmendOutcomeDisplayModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import testHelpers.CommonViewSpecHelper
import testdata.AmendProtectionDisplayModelTestData._
import testHelpers.messages.amends.OutcomeAmendedViewMessages
import views.html.pages.amends.amendOutcome

class AmendOutcomeViewSpec extends CommonViewSpecHelper with OutcomeAmendedViewMessages {

  val view: amendOutcome = inject[amendOutcome]

  def parseDocument(amendResultDisplayModel: AmendOutcomeDisplayModel): Document =
    Jsoup.parse(view(amendResultDisplayModel).body)

  "amendOutcome" when {

    "provided with AmendResultDisplayModel containing notificationId: 1" should {

      val doc = parseDocument(amendOutcomeDisplayModelNotification1)

      "not have back link with text back " in {
        val backButton = doc.select(".govuk-back-link").text()
        backButton.isEmpty shouldBe true
      }

      "contain title" in {
        doc.title() shouldBe title
      }

      "contain (green) govuk panel" in {
        doc.select("h1.govuk-panel__title").text() shouldBe iP14Heading
        doc.select("#amendedAllowanceText").text() shouldBe yourNewProtectedAmount
        doc.select("strong#protectedAmount").text() shouldBe protectedAmountIndividualProtection2014InRange
      }

      "contain h2 element" in {
        doc.select("h2.govuk-heading-m").get(0).text() shouldBe yourProtectionDetails
      }

      "contain a table".which {

        val tableHeadings = doc.select("tr th")
        val tableData     = doc.select("tr td")

        "contains row for name" in {
          val rowIndex = 0
          tableHeadings.get(rowIndex).text shouldBe tableAmendName
          tableData.get(rowIndex).attr("id") shouldBe "yourFullName"
          tableData.get(rowIndex).text shouldBe "Jim Davis"
        }

        "contains row for National Insurance Number" in {
          val rowIndex = 1
          tableHeadings.get(rowIndex).text shouldBe tableAmendNino
          tableData.get(rowIndex).attr("id") shouldBe "yourNino"
          tableData.get(rowIndex).text shouldBe "nino"
        }

        "contains row for Protection Type" in {
          val rowIndex = 2
          tableHeadings.get(rowIndex).text shouldBe tableAmendProtectionType
          tableData.get(rowIndex).attr("id") shouldBe "protectionType"
          tableData.get(rowIndex).text shouldBe "Individual protection 2014"
        }

        "contains row for Protection Reference Number" in {
          val rowIndex = 3
          tableHeadings.get(rowIndex).text shouldBe tableAmendProtectionRef
          tableData.get(rowIndex).attr("id") shouldBe "protectionRefNum"
          tableData.get(rowIndex).text shouldBe protectionReferenceIndividualProtection2014
        }

        "contains row for PSA Check Reference" in {
          val rowIndex = 4
          tableHeadings.get(rowIndex).text shouldBe tableAmendPsaRef
          tableData.get(rowIndex).attr("id") shouldBe "psaCheckRef"
          tableData.get(rowIndex).text shouldBe psaCheckReference
        }

        "contains row for Application Date" in {
          val rowIndex = 5
          tableHeadings.get(rowIndex).text shouldBe tableAmendApplicationDate
          tableData.get(rowIndex).attr("id") shouldBe "applicationDate"
          tableData.get(rowIndex).text shouldBe "14 July 2015"
        }

        "contains row for Application Time" in {
          val rowIndex = 6
          tableHeadings.get(rowIndex).text shouldBe tableAmendApplicationTime
          tableData.get(rowIndex).attr("id") shouldBe "applicationTime"
          tableData.get(rowIndex).text shouldBe "3:14pm"
        }
      }

      "contain print info section" in {
        doc.select("p.govuk-body").get(0).text() shouldBe printGuidanceParagraph
        doc.select("a#printPage").text() shouldBe reviewAndPrintProtectionDetails
        doc.select("a#printPage").attr("href") shouldBe controllers.routes.PrintController.printView.url
      }

      "contain 'Changing your protection details' section" in {
        doc.select("h2.govuk-heading-m").get(1).text() shouldBe changingProtectionDetailsHeadingText
        doc.select("p.govuk-body").get(2).text() shouldBe changingProtectionDetailsText
        val expectedLink = "https://www.gov.uk/find-hmrc-contacts/pension-schemes-general-enquiries"
        doc.select("p.govuk-body").get(2).select("a").attr("href") shouldBe expectedLink
      }

      "contain 'view or change details of your protection' paragraph" in {
        doc.select("#view-or-change-protection-details").text() shouldBe viewOrChangeProtectionDetailsText
        doc.select("#view-or-change-protection-details > a").text() shouldBe viewOrChangeProtectionDetailsLinkText
        val expectedLink = controllers.routes.ReadProtectionsController.currentProtections.url
        doc.select("#view-or-change-protection-details > a").attr("href") shouldBe expectedLink
      }

      "contain 'Give us feedback' section" in {
        doc.select("h2.govuk-heading-m").get(2).text() shouldBe giveUsFeedback
        doc.select("#survey-info").text() shouldBe exitSurveyText
        doc.select("#submit-survey-button").text() shouldBe exitSurveyLinkText
        doc.select("#submit-survey-button").attr("href") shouldBe controllers.routes.AccountController.signOut.url
      }
    }

    "provided with AmendResultDisplayModel containing notificationId: 2" should {

      val doc = parseDocument(amendOutcomeDisplayModelNotification2)

      "contain title" in {
        doc.title() shouldBe title
      }

      "contain (green) govuk panel" in {
        doc.select("h1.govuk-panel__title").text() shouldBe iP14Heading
        doc.select("#amendedAllowanceText").text() shouldBe yourNewProtectedAmount
        doc.select("strong#protectedAmount").text() shouldBe protectedAmountIndividualProtection2014InRange
      }

      "contain inset text" in {
        doc.select("div.govuk-inset-text").text() shouldBe NotificationId_2.insetText
      }

      "contain h2 element" in {
        doc.select("h2.govuk-heading-m").get(0).text() shouldBe yourProtectionDetails
      }

      "contain a table".which {

        val tableHeadings = doc.select("tr th")
        val tableData     = doc.select("tr td")

        "contains row for name" in {
          val rowIndex = 0
          tableHeadings.get(rowIndex).text shouldBe tableAmendName
          tableData.get(rowIndex).attr("id") shouldBe "yourFullName"
          tableData.get(rowIndex).text shouldBe "Jim Davis"
        }

        "contains row for National Insurance Number" in {
          val rowIndex = 1
          tableHeadings.get(rowIndex).text shouldBe tableAmendNino
          tableData.get(rowIndex).attr("id") shouldBe "yourNino"
          tableData.get(rowIndex).text shouldBe "nino"
        }

        "contains row for Protection Type" in {
          val rowIndex = 2
          tableHeadings.get(rowIndex).text shouldBe tableAmendProtectionType
          tableData.get(rowIndex).attr("id") shouldBe "protectionType"
          tableData.get(rowIndex).text shouldBe "Individual protection 2014"
        }

        "contains row for Application Date" in {
          val rowIndex = 3
          tableHeadings.get(rowIndex).text shouldBe tableAmendApplicationDate
          tableData.get(rowIndex).attr("id") shouldBe "applicationDate"
          tableData.get(rowIndex).text shouldBe "14 July 2015"
        }

        "contains row for Application Time" in {
          val rowIndex = 4
          tableHeadings.get(rowIndex).text shouldBe tableAmendApplicationTime
          tableData.get(rowIndex).attr("id") shouldBe "applicationTime"
          tableData.get(rowIndex).text shouldBe "3:14pm"
        }

        "contains row for Status" in {
          val rowIndex = 5
          tableHeadings.get(rowIndex).text shouldBe tableStatus
          tableData.get(rowIndex).attr("id") shouldBe "status"
          tableData.get(rowIndex).text shouldBe "Dormant"
        }
      }

      "contain print info section" in {
        doc.select("p.govuk-body").get(0).text() shouldBe printGuidanceParagraph
        doc.select("a#printPage").text() shouldBe reviewAndPrintProtectionDetails
        doc.select("a#printPage").attr("href") shouldBe controllers.routes.PrintController.printView.url
      }

      "contain 'Changing your protection details' section".which {

        "contains header" in {
          doc.select("h2.govuk-heading-m").get(1).text() shouldBe changingProtectionDetailsHeadingText
        }

        "contains first paragraph" in {
          doc.select("p.govuk-body").get(2).text() shouldBe NotificationId_2.changingProtectionDetailsText
        }

        "contains paragraph with bullet points" in {
          doc.select("p.govuk-body").get(3).text() shouldBe changingProtectionDetailsContactHmrcText
          doc.select("p.govuk-body").get(3).select("a").text() shouldBe changingProtectionDetailsContactHmrcLinkText
          val expectedLink = "https://www.gov.uk/find-hmrc-contacts/pension-schemes-general-enquiries"
          doc.select("p.govuk-body").get(3).select("a").attr("href") shouldBe expectedLink

          val expectedBulletPointText_1 = NotificationId_2.changingProtectionDetailsBulletPointText_1
          val expectedBulletPointText_2 = NotificationId_2.changingProtectionDetailsBulletPointText_2
          doc.select("ul.govuk-list > li").get(0).text() shouldBe expectedBulletPointText_1
          doc.select("ul.govuk-list > li").get(1).text() shouldBe expectedBulletPointText_2
        }

        "contains 'What to do if you lose your protection' link" in {
          doc.select("p.govuk-body").get(4).select("a").text() shouldBe whatToDoIfYouLoseProtection
          val expectedLink = "https://www.gov.uk/guidance/losing-your-lifetime-allowance-protection"
          doc.select("p.govuk-body").get(4).select("a").attr("href") shouldBe expectedLink
        }
      }

      "contain 'view or change details of your protection' paragraph" in {
        doc.select("#view-or-change-protection-details").text() shouldBe viewOrChangeProtectionDetailsText
        doc.select("#view-or-change-protection-details > a").text() shouldBe viewOrChangeProtectionDetailsLinkText
        val expectedLink = controllers.routes.ReadProtectionsController.currentProtections.url
        doc.select("#view-or-change-protection-details > a").attr("href") shouldBe expectedLink
      }

      "contain 'Give us feedback' section" in {
        doc.select("h2.govuk-heading-m").get(2).text() shouldBe giveUsFeedback
        doc.select("#survey-info").text() shouldBe exitSurveyText
        doc.select("#submit-survey-button").text() shouldBe exitSurveyLinkText
        doc.select("#submit-survey-button").attr("href") shouldBe controllers.routes.AccountController.signOut.url
      }
    }

    "provided with AmendResultDisplayModel containing notificationId: 3" should {

      val doc = parseDocument(amendOutcomeDisplayModelNotification3)

      "contain title" in {
        doc.title() shouldBe title
      }

      "contain (green) govuk panel" in {
        doc.select("h1.govuk-panel__title").text() shouldBe iP14Heading
        doc.select("#amendedAllowanceText").text() shouldBe yourNewProtectedAmount
        doc.select("strong#protectedAmount").text() shouldBe protectedAmountIndividualProtection2014InRange
      }

      "contain inset text" in {
        doc.select("div.govuk-inset-text").text() shouldBe NotificationId_3.insetText
      }

      "contain h2 element" in {
        doc.select("h2.govuk-heading-m").get(0).text() shouldBe yourProtectionDetails
      }

      "contain a table".which {

        val tableHeadings = doc.select("tr th")
        val tableData     = doc.select("tr td")

        "contains row for name" in {
          val rowIndex = 0
          tableHeadings.get(rowIndex).text shouldBe tableAmendName
          tableData.get(rowIndex).attr("id") shouldBe "yourFullName"
          tableData.get(rowIndex).text shouldBe "Jim Davis"
        }

        "contains row for National Insurance Number" in {
          val rowIndex = 1
          tableHeadings.get(rowIndex).text shouldBe tableAmendNino
          tableData.get(rowIndex).attr("id") shouldBe "yourNino"
          tableData.get(rowIndex).text shouldBe "nino"
        }

        "contains row for Protection Type" in {
          val rowIndex = 2
          tableHeadings.get(rowIndex).text shouldBe tableAmendProtectionType
          tableData.get(rowIndex).attr("id") shouldBe "protectionType"
          tableData.get(rowIndex).text shouldBe "Individual protection 2014"
        }

        "contains row for Application Date" in {
          val rowIndex = 3
          tableHeadings.get(rowIndex).text shouldBe tableAmendApplicationDate
          tableData.get(rowIndex).attr("id") shouldBe "applicationDate"
          tableData.get(rowIndex).text shouldBe "14 July 2015"
        }

        "contains row for Application Time" in {
          val rowIndex = 4
          tableHeadings.get(rowIndex).text shouldBe tableAmendApplicationTime
          tableData.get(rowIndex).attr("id") shouldBe "applicationTime"
          tableData.get(rowIndex).text shouldBe "3:14pm"
        }

        "contains row for Status" in {
          val rowIndex = 5
          tableHeadings.get(rowIndex).text shouldBe tableStatus
          tableData.get(rowIndex).attr("id") shouldBe "status"
          tableData.get(rowIndex).text shouldBe "Dormant"
        }
      }

      "contain print info section" in {
        doc.select("p.govuk-body").get(0).text() shouldBe printGuidanceParagraph
        doc.select("a#printPage").text() shouldBe reviewAndPrintProtectionDetails
        doc.select("a#printPage").attr("href") shouldBe controllers.routes.PrintController.printView.url
      }

      "contain 'Changing your protection details' section".which {

        "contains header" in {
          doc.select("h2.govuk-heading-m").get(1).text() shouldBe changingProtectionDetailsHeadingText
        }

        "contains first paragraph" in {
          doc.select("p.govuk-body").get(2).text() shouldBe NotificationId_3.changingProtectionDetailsText
        }

        "contains paragraph with bullet points" in {
          doc.select("p.govuk-body").get(3).text() shouldBe changingProtectionDetailsContactHmrcText
          doc.select("p.govuk-body").get(3).select("a").text() shouldBe changingProtectionDetailsContactHmrcLinkText
          val expectedLink = "https://www.gov.uk/find-hmrc-contacts/pension-schemes-general-enquiries"
          doc.select("p.govuk-body").get(3).select("a").attr("href") shouldBe expectedLink

          val expectedBulletPointText_1 = NotificationId_3.changingProtectionDetailsBulletPointText_1
          val expectedBulletPointText_2 = NotificationId_3.changingProtectionDetailsBulletPointText_2
          doc.select("ul.govuk-list > li").get(0).text() shouldBe expectedBulletPointText_1
          doc.select("ul.govuk-list > li").get(1).text() shouldBe expectedBulletPointText_2
        }

        "contains 'What to do if you lose your protection' link" in {
          doc.select("p.govuk-body").get(4).select("a").text() shouldBe whatToDoIfYouLoseProtection
          val expectedLink = "https://www.gov.uk/guidance/losing-your-lifetime-allowance-protection"
          doc.select("p.govuk-body").get(4).select("a").attr("href") shouldBe expectedLink
        }
      }

      "contain 'view or change details of your protection' paragraph" in {
        doc.select("#view-or-change-protection-details").text() shouldBe viewOrChangeProtectionDetailsText
        doc.select("#view-or-change-protection-details > a").text() shouldBe viewOrChangeProtectionDetailsLinkText
        val expectedLink = controllers.routes.ReadProtectionsController.currentProtections.url
        doc.select("#view-or-change-protection-details > a").attr("href") shouldBe expectedLink
      }

      "contain 'Give us feedback' section" in {
        doc.select("h2.govuk-heading-m").get(2).text() shouldBe giveUsFeedback
        doc.select("#survey-info").text() shouldBe exitSurveyText
        doc.select("#submit-survey-button").text() shouldBe exitSurveyLinkText
        doc.select("#submit-survey-button").attr("href") shouldBe controllers.routes.AccountController.signOut.url
      }
    }

    "provided with AmendResultDisplayModel containing notificationId: 4" should {

      val doc = parseDocument(amendOutcomeDisplayModelNotification4)

      "contain title" in {
        doc.title() shouldBe title
      }

      "contain (green) govuk panel" in {
        doc.select("h1.govuk-panel__title").text() shouldBe iP14Heading
        doc.select("#amendedAllowanceText").text() shouldBe yourNewProtectedAmount
        doc.select("strong#protectedAmount").text() shouldBe protectedAmountIndividualProtection2014InRange
      }

      "contain inset text" in {
        doc.select("div.govuk-inset-text").text() shouldBe NotificationId_4.insetText
      }

      "contain h2 element" in {
        doc.select("h2.govuk-heading-m").get(0).text() shouldBe yourProtectionDetails
      }

      "contain a table".which {

        val tableHeadings = doc.select("tr th")
        val tableData     = doc.select("tr td")

        "contains row for name" in {
          val rowIndex = 0
          tableHeadings.get(rowIndex).text shouldBe tableAmendName
          tableData.get(rowIndex).attr("id") shouldBe "yourFullName"
          tableData.get(rowIndex).text shouldBe "Jim Davis"
        }

        "contains row for National Insurance Number" in {
          val rowIndex = 1
          tableHeadings.get(rowIndex).text shouldBe tableAmendNino
          tableData.get(rowIndex).attr("id") shouldBe "yourNino"
          tableData.get(rowIndex).text shouldBe "nino"
        }

        "contains row for Protection Type" in {
          val rowIndex = 2
          tableHeadings.get(rowIndex).text shouldBe tableAmendProtectionType
          tableData.get(rowIndex).attr("id") shouldBe "protectionType"
          tableData.get(rowIndex).text shouldBe "Individual protection 2014"
        }

        "contains row for Application Date" in {
          val rowIndex = 3
          tableHeadings.get(rowIndex).text shouldBe tableAmendApplicationDate
          tableData.get(rowIndex).attr("id") shouldBe "applicationDate"
          tableData.get(rowIndex).text shouldBe "14 July 2015"
        }

        "contains row for Application Time" in {
          val rowIndex = 4
          tableHeadings.get(rowIndex).text shouldBe tableAmendApplicationTime
          tableData.get(rowIndex).attr("id") shouldBe "applicationTime"
          tableData.get(rowIndex).text shouldBe "3:14pm"
        }

        "contains row for Status" in {
          val rowIndex = 5
          tableHeadings.get(rowIndex).text shouldBe tableStatus
          tableData.get(rowIndex).attr("id") shouldBe "status"
          tableData.get(rowIndex).text shouldBe "Dormant"
        }
      }

      "contain print info section" in {
        doc.select("p.govuk-body").get(0).text() shouldBe printGuidanceParagraph
        doc.select("a#printPage").text() shouldBe reviewAndPrintProtectionDetails
        doc.select("a#printPage").attr("href") shouldBe controllers.routes.PrintController.printView.url
      }

      "contain 'Changing your protection details' section".which {

        "contains header" in {
          doc.select("h2.govuk-heading-m").get(1).text() shouldBe changingProtectionDetailsHeadingText
        }

        "contains first paragraph" in {
          doc.select("p.govuk-body").get(2).text() shouldBe NotificationId_4.changingProtectionDetailsText
        }

        "contains paragraph with bullet points" in {
          doc.select("p.govuk-body").get(3).text() shouldBe changingProtectionDetailsContactHmrcText
          doc.select("p.govuk-body").get(3).select("a").text() shouldBe changingProtectionDetailsContactHmrcLinkText
          val expectedLink = "https://www.gov.uk/find-hmrc-contacts/pension-schemes-general-enquiries"
          doc.select("p.govuk-body").get(3).select("a").attr("href") shouldBe expectedLink

          val expectedBulletPointText_1 = NotificationId_4.changingProtectionDetailsBulletPointText_1
          val expectedBulletPointText_2 = NotificationId_4.changingProtectionDetailsBulletPointText_2
          doc.select("ul.govuk-list > li").get(0).text() shouldBe expectedBulletPointText_1
          doc.select("ul.govuk-list > li").get(1).text() shouldBe expectedBulletPointText_2
        }

        "contains 'What to do if you lose your protection' link" in {
          doc.select("p.govuk-body").get(4).select("a").text() shouldBe whatToDoIfYouLoseProtection
          val expectedLink = "https://www.gov.uk/guidance/losing-your-lifetime-allowance-protection"
          doc.select("p.govuk-body").get(4).select("a").attr("href") shouldBe expectedLink
        }
      }

      "contain 'view or change details of your protection' paragraph" in {
        doc.select("#view-or-change-protection-details").text() shouldBe viewOrChangeProtectionDetailsText
        doc.select("#view-or-change-protection-details > a").text() shouldBe viewOrChangeProtectionDetailsLinkText
        val expectedLink = controllers.routes.ReadProtectionsController.currentProtections.url
        doc.select("#view-or-change-protection-details > a").attr("href") shouldBe expectedLink
      }

      "contain 'Give us feedback' section" in {
        doc.select("h2.govuk-heading-m").get(2).text() shouldBe giveUsFeedback
        doc.select("#survey-info").text() shouldBe exitSurveyText
        doc.select("#submit-survey-button").text() shouldBe exitSurveyLinkText
        doc.select("#submit-survey-button").attr("href") shouldBe controllers.routes.AccountController.signOut.url
      }
    }

    "provided with AmendResultDisplayModel containing notificationId: 5" should {

      val doc = parseDocument(amendOutcomeDisplayModelNotification5)

      "contain title" in {
        doc.title() shouldBe title
      }

      "contain (green) govuk panel" in {
        doc.select("h1.govuk-panel__title").text() shouldBe iP14Heading
        doc.select("#amendedAllowanceText").text() shouldBe yourNewProtectedAmount
        doc.select("strong#protectedAmount").text() shouldBe protectedAmountIndividualProtection2014InRange
      }

      "contain h2 element" in {
        doc.select("h2.govuk-heading-m").get(0).text() shouldBe yourProtectionDetails
      }

      "contain a table".which {

        val tableHeadings = doc.select("tr th")
        val tableData     = doc.select("tr td")

        "contains row for name" in {
          val rowIndex = 0
          tableHeadings.get(rowIndex).text shouldBe tableAmendName
          tableData.get(rowIndex).attr("id") shouldBe "yourFullName"
          tableData.get(rowIndex).text shouldBe "Jim Davis"
        }

        "contains row for National Insurance Number" in {
          val rowIndex = 1
          tableHeadings.get(rowIndex).text shouldBe tableAmendNino
          tableData.get(rowIndex).attr("id") shouldBe "yourNino"
          tableData.get(rowIndex).text shouldBe "nino"
        }

        "contains row for Protection Type" in {
          val rowIndex = 2
          tableHeadings.get(rowIndex).text shouldBe tableAmendProtectionType
          tableData.get(rowIndex).attr("id") shouldBe "protectionType"
          tableData.get(rowIndex).text shouldBe "Individual protection 2014"
        }

        "contains row for Protection Reference Number" in {
          val rowIndex = 3
          tableHeadings.get(rowIndex).text shouldBe tableAmendProtectionRef
          tableData.get(rowIndex).attr("id") shouldBe "protectionRefNum"
          tableData.get(rowIndex).text shouldBe protectionReferenceIndividualProtection2014
        }

        "contains row for PSA Check Reference" in {
          val rowIndex = 4
          tableHeadings.get(rowIndex).text shouldBe tableAmendPsaRef
          tableData.get(rowIndex).attr("id") shouldBe "psaCheckRef"
          tableData.get(rowIndex).text shouldBe psaCheckReference
        }

        "contains row for Application Date" in {
          val rowIndex = 5
          tableHeadings.get(rowIndex).text shouldBe tableAmendApplicationDate
          tableData.get(rowIndex).attr("id") shouldBe "applicationDate"
          tableData.get(rowIndex).text shouldBe "14 July 2015"
        }

        "contains row for Application Time" in {
          val rowIndex = 6
          tableHeadings.get(rowIndex).text shouldBe tableAmendApplicationTime
          tableData.get(rowIndex).attr("id") shouldBe "applicationTime"
          tableData.get(rowIndex).text shouldBe "3:14pm"
        }
      }

      "contain print info section" in {
        doc.select("p.govuk-body").get(0).text() shouldBe printGuidanceParagraph
        doc.select("a#printPage").text() shouldBe reviewAndPrintProtectionDetails
        doc.select("a#printPage").attr("href") shouldBe controllers.routes.PrintController.printView.url
      }

      "contain 'Changing your protection details' section" in {
        doc.select("h2.govuk-heading-m").get(1).text() shouldBe changingProtectionDetailsHeadingText
        doc.select("p.govuk-body").get(2).text() shouldBe changingProtectionDetailsText
        val expectedLink = "https://www.gov.uk/find-hmrc-contacts/pension-schemes-general-enquiries"
        doc.select("p.govuk-body").get(2).select("a").attr("href") shouldBe expectedLink
      }

      "contain 'view or change details of your protection' paragraph" in {
        doc.select("#view-or-change-protection-details").text() shouldBe viewOrChangeProtectionDetailsText
        doc.select("#view-or-change-protection-details > a").text() shouldBe viewOrChangeProtectionDetailsLinkText
        val expectedLink = controllers.routes.ReadProtectionsController.currentProtections.url
        doc.select("#view-or-change-protection-details > a").attr("href") shouldBe expectedLink
      }

      "contain 'Give us feedback' section" in {
        doc.select("h2.govuk-heading-m").get(2).text() shouldBe giveUsFeedback
        doc.select("#survey-info").text() shouldBe exitSurveyText
        doc.select("#submit-survey-button").text() shouldBe exitSurveyLinkText
        doc.select("#submit-survey-button").attr("href") shouldBe controllers.routes.AccountController.signOut.url
      }
    }

    "provided with AmendResultDisplayModel containing notificationId: 6" should {

      val doc = parseDocument(amendOutcomeDisplayModelNotification6)

      "contain title" in {
        doc.title() shouldBe title
      }

      "contain (green) govuk panel" in {
        doc.select("h1.govuk-panel__title").text() shouldBe iP14Heading
        doc.select("#amendedAllowanceText").text() shouldBe yourNewProtectedAmount
        doc.select("strong#protectedAmount").text() shouldBe protectedAmountIndividualProtection2014Below
      }

      "contain inset text" in {
        doc.select("div.govuk-inset-text").text() shouldBe NotificationId_6.insetText
      }

      "contain h2 element" in {
        doc.select("h2.govuk-heading-m").get(0).text() shouldBe yourProtectionDetails
      }

      "contain a table".which {

        val tableHeadings = doc.select("tr th")
        val tableData     = doc.select("tr td")

        "contains row for name" in {
          val rowIndex = 0
          tableHeadings.get(rowIndex).text shouldBe tableAmendName
          tableData.get(rowIndex).attr("id") shouldBe "yourFullName"
          tableData.get(rowIndex).text shouldBe "Jim Davis"
        }

        "contains row for National Insurance Number" in {
          val rowIndex = 1
          tableHeadings.get(rowIndex).text shouldBe tableAmendNino
          tableData.get(rowIndex).attr("id") shouldBe "yourNino"
          tableData.get(rowIndex).text shouldBe "nino"
        }

        "contains row for Protection Type" in {
          val rowIndex = 2
          tableHeadings.get(rowIndex).text shouldBe tableAmendProtectionType
          tableData.get(rowIndex).attr("id") shouldBe "protectionType"
          tableData.get(rowIndex).text shouldBe "Individual protection 2014"
        }

        "contains row for Application Date" in {
          val rowIndex = 3
          tableHeadings.get(rowIndex).text shouldBe tableAmendApplicationDate
          tableData.get(rowIndex).attr("id") shouldBe "applicationDate"
          tableData.get(rowIndex).text shouldBe "14 July 2015"
        }

        "contains row for Application Time" in {
          val rowIndex = 4
          tableHeadings.get(rowIndex).text shouldBe tableAmendApplicationTime
          tableData.get(rowIndex).attr("id") shouldBe "applicationTime"
          tableData.get(rowIndex).text shouldBe "3:14pm"
        }

        "contains row for Status" in {
          val rowIndex = 5
          tableHeadings.get(rowIndex).text shouldBe tableStatus
          tableData.get(rowIndex).attr("id") shouldBe "status"
          tableData.get(rowIndex).text shouldBe "Withdrawn"
        }
      }

      "contain print info section" in {
        doc.select("p.govuk-body").get(0).text() shouldBe printGuidanceParagraph
        doc.select("a#printPage").text() shouldBe reviewAndPrintProtectionDetails
        doc.select("a#printPage").attr("href") shouldBe controllers.routes.PrintController.printView.url
      }

      "contain 'Changing your protection details' section" in {
        doc.select("h2.govuk-heading-m").get(1).text() shouldBe changingProtectionDetailsHeadingText
        doc.select("p.govuk-body").get(2).text() shouldBe changingProtectionDetailsText
        val expectedLink = "https://www.gov.uk/find-hmrc-contacts/pension-schemes-general-enquiries"
        doc.select("p.govuk-body").get(2).select("a").attr("href") shouldBe expectedLink
      }

      "contain 'view or change details of your protection' paragraph" in {
        doc.select("#view-or-change-protection-details").text() shouldBe viewOrChangeProtectionDetailsText
        doc.select("#view-or-change-protection-details > a").text() shouldBe viewOrChangeProtectionDetailsLinkText
        val expectedLink = controllers.routes.ReadProtectionsController.currentProtections.url
        doc.select("#view-or-change-protection-details > a").attr("href") shouldBe expectedLink
      }

      "contain 'Give us feedback' section" in {
        doc.select("h2.govuk-heading-m").get(2).text() shouldBe giveUsFeedback
        doc.select("#survey-info").text() shouldBe exitSurveyText
        doc.select("#submit-survey-button").text() shouldBe exitSurveyLinkText
        doc.select("#submit-survey-button").attr("href") shouldBe controllers.routes.AccountController.signOut.url
      }
    }

    "provided with AmendResultDisplayModel containing notificationId: 7" should {

      val doc = parseDocument(amendOutcomeDisplayModelNotification7)

      "contain title" in {
        doc.title() shouldBe title
      }

      "contain (green) govuk panel" in {
        doc.select("h1.govuk-panel__title").text() shouldBe iP14Heading
        doc.select("#amendedAllowanceText").text() shouldBe yourNewProtectedAmount
        doc.select("strong#protectedAmount").text() shouldBe protectedAmountIndividualProtection2014Below
      }

      "contain inset text" in {
        doc.select("div.govuk-inset-text").text() should include(NotificationId_7.insetText_1)
        doc.select("div.govuk-inset-text > p").text() should include(NotificationId_7.insetText_2)
      }

      "contain h2 element" in {
        doc.select("h2.govuk-heading-m").get(0).text() shouldBe yourProtectionDetails
      }

      "contain a table".which {

        val tableHeadings = doc.select("tr th")
        val tableData     = doc.select("tr td")

        "contains row for name" in {
          val rowIndex = 0
          tableHeadings.get(rowIndex).text shouldBe tableAmendName
          tableData.get(rowIndex).attr("id") shouldBe "yourFullName"
          tableData.get(rowIndex).text shouldBe "Jim Davis"
        }

        "contains row for National Insurance Number" in {
          val rowIndex = 1
          tableHeadings.get(rowIndex).text shouldBe tableAmendNino
          tableData.get(rowIndex).attr("id") shouldBe "yourNino"
          tableData.get(rowIndex).text shouldBe "nino"
        }

        "contains row for Protection Type" in {
          val rowIndex = 2
          tableHeadings.get(rowIndex).text shouldBe tableAmendProtectionType
          tableData.get(rowIndex).attr("id") shouldBe "protectionType"
          tableData.get(rowIndex).text shouldBe "Fixed protection 2016"
        }

        "contains row for Fixed Protection 2016 Reference Number" in {
          val rowIndex = 3
          tableHeadings.get(rowIndex).text shouldBe tableAmendFixedProtectionRef
          tableData.get(rowIndex).attr("id") shouldBe "fixedProtectionRefNum"
          tableData.get(rowIndex).text shouldBe protectionReferenceFixedProtection2016
        }

        "contains row for Application Date" in {
          val rowIndex = 4
          tableHeadings.get(rowIndex).text shouldBe tableAmendApplicationDate
          tableData.get(rowIndex).attr("id") shouldBe "applicationDate"
          tableData.get(rowIndex).text shouldBe "14 July 2015"
        }

        "contains row for Application Time" in {
          val rowIndex = 5
          tableHeadings.get(rowIndex).text shouldBe tableAmendApplicationTime
          tableData.get(rowIndex).attr("id") shouldBe "applicationTime"
          tableData.get(rowIndex).text shouldBe "3:14pm"
        }
      }

      "contain print info section" in {
        doc.select("p.govuk-body").get(0).text() shouldBe printGuidanceParagraph
        doc.select("a#printPage").text() shouldBe reviewAndPrintProtectionDetails
        doc.select("a#printPage").attr("href") shouldBe controllers.routes.PrintController.printView.url
      }

      "contain 'Changing your protection details' section".which {

        "contains header" in {
          doc.select("h2.govuk-heading-m").get(1).text() shouldBe changingProtectionDetailsHeadingText
        }

        "contains first paragraph" in {
          doc.select("p.govuk-body").get(2).text() shouldBe NotificationId_7.changingProtectionDetailsText
        }

        "contains paragraph with bullet points" in {
          doc.select("p.govuk-body").get(3).text() shouldBe changingProtectionDetailsContactHmrcText
          doc.select("p.govuk-body").get(3).select("a").text() shouldBe changingProtectionDetailsContactHmrcLinkText
          val expectedLink = "https://www.gov.uk/find-hmrc-contacts/pension-schemes-general-enquiries"
          doc.select("p.govuk-body").get(3).select("a").attr("href") shouldBe expectedLink

          val expectedBulletPointText_1 = NotificationId_7.changingProtectionDetailsBulletPointText_1
          val expectedBulletPointText_2 = NotificationId_7.changingProtectionDetailsBulletPointText_2
          doc.select("ul.govuk-list > li").get(0).text() shouldBe expectedBulletPointText_1
          doc.select("ul.govuk-list > li").get(1).text() shouldBe expectedBulletPointText_2
        }

        "contains 'What to do if you lose your protection' link" in {
          doc.select("p.govuk-body").get(4).select("a").text() shouldBe whatToDoIfYouLoseProtection
          val expectedLink = "https://www.gov.uk/guidance/losing-your-lifetime-allowance-protection"
          doc.select("p.govuk-body").get(4).select("a").attr("href") shouldBe expectedLink
        }
      }

      "contain 'view or change details of your protection' paragraph" in {
        doc.select("#view-or-change-protection-details").text() shouldBe viewOrChangeProtectionDetailsText
        doc.select("#view-or-change-protection-details > a").text() shouldBe viewOrChangeProtectionDetailsLinkText
        val expectedLink = controllers.routes.ReadProtectionsController.currentProtections.url
        doc.select("#view-or-change-protection-details > a").attr("href") shouldBe expectedLink
      }

      "contain 'Give us feedback' section" in {
        doc.select("h2.govuk-heading-m").get(2).text() shouldBe giveUsFeedback
        doc.select("#survey-info").text() shouldBe exitSurveyText
        doc.select("#submit-survey-button").text() shouldBe exitSurveyLinkText
        doc.select("#submit-survey-button").attr("href") shouldBe controllers.routes.AccountController.signOut.url
      }
    }

    "provided with AmendResultDisplayModel containing notificationId: 8" should {

      val doc = parseDocument(amendOutcomeDisplayModelNotification8)

      "contain title" in {
        doc.title() shouldBe title
      }

      "contain (green) govuk panel" in {
        doc.select("h1.govuk-panel__title").text() shouldBe iP16Heading
        doc.select("#amendedAllowanceText").text() shouldBe yourNewProtectedAmount
        doc.select("strong#protectedAmount").text() shouldBe protectedAmountIndividualProtection2016InRange
      }

      "contain h2 element" in {
        doc.select("h2.govuk-heading-m").get(0).text() shouldBe yourProtectionDetails
      }

      "contain a table".which {

        val tableHeadings = doc.select("tr th")
        val tableData     = doc.select("tr td")

        "contains row for name" in {
          val rowIndex = 0
          tableHeadings.get(rowIndex).text shouldBe tableAmendName
          tableData.get(rowIndex).attr("id") shouldBe "yourFullName"
          tableData.get(rowIndex).text shouldBe "Jim Davis"
        }

        "contains row for National Insurance Number" in {
          val rowIndex = 1
          tableHeadings.get(rowIndex).text shouldBe tableAmendNino
          tableData.get(rowIndex).attr("id") shouldBe "yourNino"
          tableData.get(rowIndex).text shouldBe "nino"
        }

        "contains row for Protection Type" in {
          val rowIndex = 2
          tableHeadings.get(rowIndex).text shouldBe tableAmendProtectionType
          tableData.get(rowIndex).attr("id") shouldBe "protectionType"
          tableData.get(rowIndex).text shouldBe "Individual protection 2016"
        }

        "contains row for Protection Reference Number" in {
          val rowIndex = 3
          tableHeadings.get(rowIndex).text shouldBe tableAmendProtectionRef
          tableData.get(rowIndex).attr("id") shouldBe "protectionRefNum"
          tableData.get(rowIndex).text shouldBe protectionReferenceIndividualProtection2016
        }

        "contains row for PSA Check Reference" in {
          val rowIndex = 4
          tableHeadings.get(rowIndex).text shouldBe tableAmendPsaRef
          tableData.get(rowIndex).attr("id") shouldBe "psaCheckRef"
          tableData.get(rowIndex).text shouldBe "psaRef"
        }

        "contains row for Application Date" in {
          val rowIndex = 5
          tableHeadings.get(rowIndex).text shouldBe tableAmendApplicationDate
          tableData.get(rowIndex).attr("id") shouldBe "applicationDate"
          tableData.get(rowIndex).text shouldBe "14 July 2017"
        }

        "contains row for Application Time" in {
          val rowIndex = 6
          tableHeadings.get(rowIndex).text shouldBe tableAmendApplicationTime
          tableData.get(rowIndex).attr("id") shouldBe "applicationTime"
          tableData.get(rowIndex).text shouldBe "3:14pm"
        }
      }

      "contain print info section" in {
        doc.select("p.govuk-body").get(0).text() shouldBe printGuidanceParagraph
        doc.select("a#printPage").text() shouldBe reviewAndPrintProtectionDetails
        doc.select("a#printPage").attr("href") shouldBe controllers.routes.PrintController.printView.url
      }

      "contain 'Changing your protection details' section" in {
        doc.select("h2.govuk-heading-m").get(1).text() shouldBe changingProtectionDetailsHeadingText
        doc.select("p.govuk-body").get(2).text() shouldBe changingProtectionDetailsText
        val expectedLink = "https://www.gov.uk/find-hmrc-contacts/pension-schemes-general-enquiries"
        doc.select("p.govuk-body").get(2).select("a").attr("href") shouldBe expectedLink
      }

      "contain 'view or change details of your protection' paragraph" in {
        doc.select("#view-or-change-protection-details").text() shouldBe viewOrChangeProtectionDetailsText
        doc.select("#view-or-change-protection-details > a").text() shouldBe viewOrChangeProtectionDetailsLinkText
        val expectedLink = controllers.routes.ReadProtectionsController.currentProtections.url
        doc.select("#view-or-change-protection-details > a").attr("href") shouldBe expectedLink
      }

      "contain 'Give us feedback' section" in {
        doc.select("h2.govuk-heading-m").get(2).text() shouldBe giveUsFeedback
        doc.select("#survey-info").text() shouldBe exitSurveyText
        doc.select("#submit-survey-button").text() shouldBe exitSurveyLinkText
        doc.select("#submit-survey-button").attr("href") shouldBe controllers.routes.AccountController.signOut.url
      }
    }

    "provided with AmendResultDisplayModel containing notificationId: 9" should {

      val doc = parseDocument(amendOutcomeDisplayModelNotification9)

      "contain title" in {
        doc.title() shouldBe title
      }

      "contain (green) govuk panel" in {
        doc.select("h1.govuk-panel__title").text() shouldBe iP16Heading
        doc.select("#amendedAllowanceText").text() shouldBe yourNewProtectedAmount
        doc.select("strong#protectedAmount").text() shouldBe protectedAmountIndividualProtection2016InRange
      }

      "contain inset text" in {
        doc.select("div.govuk-inset-text").text() should include(NotificationId_9.insetText)
      }

      "contain h2 element" in {
        doc.select("h2.govuk-heading-m").get(0).text() shouldBe yourProtectionDetails
      }

      "contain a table".which {

        val tableHeadings = doc.select("tr th")
        val tableData     = doc.select("tr td")

        "contains row for name" in {
          val rowIndex = 0
          tableHeadings.get(rowIndex).text shouldBe tableAmendName
          tableData.get(rowIndex).attr("id") shouldBe "yourFullName"
          tableData.get(rowIndex).text shouldBe "Jim Davis"
        }

        "contains row for National Insurance Number" in {
          val rowIndex = 1
          tableHeadings.get(rowIndex).text shouldBe tableAmendNino
          tableData.get(rowIndex).attr("id") shouldBe "yourNino"
          tableData.get(rowIndex).text shouldBe "nino"
        }

        "contains row for Protection Type" in {
          val rowIndex = 2
          tableHeadings.get(rowIndex).text shouldBe tableAmendProtectionType
          tableData.get(rowIndex).attr("id") shouldBe "protectionType"
          tableData.get(rowIndex).text shouldBe "Individual protection 2016"
        }

        "contains row for Application Date" in {
          val rowIndex = 3
          tableHeadings.get(rowIndex).text shouldBe tableAmendApplicationDate
          tableData.get(rowIndex).attr("id") shouldBe "applicationDate"
          tableData.get(rowIndex).text shouldBe "14 July 2017"
        }

        "contains row for Application Time" in {
          val rowIndex = 4
          tableHeadings.get(rowIndex).text shouldBe tableAmendApplicationTime
          tableData.get(rowIndex).attr("id") shouldBe "applicationTime"
          tableData.get(rowIndex).text shouldBe "3:14pm"
        }

        "contains row for Status" in {
          val rowIndex = 5
          tableHeadings.get(rowIndex).text shouldBe tableStatus
          tableData.get(rowIndex).attr("id") shouldBe "status"
          tableData.get(rowIndex).text shouldBe "Dormant"
        }
      }

      "contain print info section" in {
        doc.select("p.govuk-body").get(0).text() shouldBe printGuidanceParagraph
        doc.select("a#printPage").text() shouldBe reviewAndPrintProtectionDetails
        doc.select("a#printPage").attr("href") shouldBe controllers.routes.PrintController.printView.url
      }

      "contain 'Changing your protection details' section".which {

        "contains header" in {
          doc.select("h2.govuk-heading-m").get(1).text() shouldBe changingProtectionDetailsHeadingText
        }

        "contains first paragraph" in {
          doc.select("p.govuk-body").get(2).text() shouldBe NotificationId_9.changingProtectionDetailsText
        }

        "contains paragraph with bullet points" in {
          doc.select("p.govuk-body").get(3).text() shouldBe changingProtectionDetailsContactHmrcText
          doc.select("p.govuk-body").get(3).select("a").text() shouldBe changingProtectionDetailsContactHmrcLinkText
          val expectedLink = "https://www.gov.uk/find-hmrc-contacts/pension-schemes-general-enquiries"
          doc.select("p.govuk-body").get(3).select("a").attr("href") shouldBe expectedLink

          val expectedBulletPointText_1 = NotificationId_9.changingProtectionDetailsBulletPointText_1
          val expectedBulletPointText_2 = NotificationId_9.changingProtectionDetailsBulletPointText_2
          doc.select("ul.govuk-list > li").get(0).text() shouldBe expectedBulletPointText_1
          doc.select("ul.govuk-list > li").get(1).text() shouldBe expectedBulletPointText_2
        }

        "contains 'What to do if you lose your protection' link" in {
          doc.select("p.govuk-body").get(4).select("a").text() shouldBe whatToDoIfYouLoseProtection
          val expectedLink = "https://www.gov.uk/guidance/losing-your-lifetime-allowance-protection"
          doc.select("p.govuk-body").get(4).select("a").attr("href") shouldBe expectedLink
        }
      }

      "contain 'view or change details of your protection' paragraph" in {
        doc.select("#view-or-change-protection-details").text() shouldBe viewOrChangeProtectionDetailsText
        doc.select("#view-or-change-protection-details > a").text() shouldBe viewOrChangeProtectionDetailsLinkText
        val expectedLink = controllers.routes.ReadProtectionsController.currentProtections.url
        doc.select("#view-or-change-protection-details > a").attr("href") shouldBe expectedLink
      }

      "contain 'Give us feedback' section" in {
        doc.select("h2.govuk-heading-m").get(2).text() shouldBe giveUsFeedback
        doc.select("#survey-info").text() shouldBe exitSurveyText
        doc.select("#submit-survey-button").text() shouldBe exitSurveyLinkText
        doc.select("#submit-survey-button").attr("href") shouldBe controllers.routes.AccountController.signOut.url
      }
    }

    "provided with AmendResultDisplayModel containing notificationId: 10" should {

      val doc = parseDocument(amendOutcomeDisplayModelNotification10)

      "contain title" in {
        doc.title() shouldBe title
      }

      "contain (green) govuk panel" in {
        doc.select("h1.govuk-panel__title").text() shouldBe iP16Heading
        doc.select("#amendedAllowanceText").text() shouldBe yourNewProtectedAmount
        doc.select("strong#protectedAmount").text() shouldBe protectedAmountIndividualProtection2016InRange
      }

      "contain inset text" in {
        doc.select("div.govuk-inset-text").text() should include(NotificationId_10.insetText)
      }

      "contain h2 element" in {
        doc.select("h2.govuk-heading-m").get(0).text() shouldBe yourProtectionDetails
      }

      "contain a table".which {

        val tableHeadings = doc.select("tr th")
        val tableData     = doc.select("tr td")

        "contains row for name" in {
          val rowIndex = 0
          tableHeadings.get(rowIndex).text shouldBe tableAmendName
          tableData.get(rowIndex).attr("id") shouldBe "yourFullName"
          tableData.get(rowIndex).text shouldBe "Jim Davis"
        }

        "contains row for National Insurance Number" in {
          val rowIndex = 1
          tableHeadings.get(rowIndex).text shouldBe tableAmendNino
          tableData.get(rowIndex).attr("id") shouldBe "yourNino"
          tableData.get(rowIndex).text shouldBe "nino"
        }

        "contains row for Protection Type" in {
          val rowIndex = 2
          tableHeadings.get(rowIndex).text shouldBe tableAmendProtectionType
          tableData.get(rowIndex).attr("id") shouldBe "protectionType"
          tableData.get(rowIndex).text shouldBe "Individual protection 2016"
        }

        "contains row for Application Date" in {
          val rowIndex = 3
          tableHeadings.get(rowIndex).text shouldBe tableAmendApplicationDate
          tableData.get(rowIndex).attr("id") shouldBe "applicationDate"
          tableData.get(rowIndex).text shouldBe "14 July 2017"
        }

        "contains row for Application Time" in {
          val rowIndex = 4
          tableHeadings.get(rowIndex).text shouldBe tableAmendApplicationTime
          tableData.get(rowIndex).attr("id") shouldBe "applicationTime"
          tableData.get(rowIndex).text shouldBe "3:14pm"
        }

        "contains row for Status" in {
          val rowIndex = 5
          tableHeadings.get(rowIndex).text shouldBe tableStatus
          tableData.get(rowIndex).attr("id") shouldBe "status"
          tableData.get(rowIndex).text shouldBe "Dormant"
        }
      }

      "contain print info section" in {
        doc.select("p.govuk-body").get(0).text() shouldBe printGuidanceParagraph
        doc.select("a#printPage").text() shouldBe reviewAndPrintProtectionDetails
        doc.select("a#printPage").attr("href") shouldBe controllers.routes.PrintController.printView.url
      }

      "contain 'Changing your protection details' section".which {

        "contains header" in {
          doc.select("h2.govuk-heading-m").get(1).text() shouldBe changingProtectionDetailsHeadingText
        }

        "contains first paragraph" in {
          doc.select("p.govuk-body").get(2).text() shouldBe NotificationId_10.changingProtectionDetailsText
        }

        "contains paragraph with bullet points" in {
          doc.select("p.govuk-body").get(3).text() shouldBe changingProtectionDetailsContactHmrcText
          doc.select("p.govuk-body").get(3).select("a").text() shouldBe changingProtectionDetailsContactHmrcLinkText
          val expectedLink = "https://www.gov.uk/find-hmrc-contacts/pension-schemes-general-enquiries"
          doc.select("p.govuk-body").get(3).select("a").attr("href") shouldBe expectedLink

          val expectedBulletPointText_1 = NotificationId_10.changingProtectionDetailsBulletPointText_1
          val expectedBulletPointText_2 = NotificationId_10.changingProtectionDetailsBulletPointText_2
          doc.select("ul.govuk-list > li").get(0).text() shouldBe expectedBulletPointText_1
          doc.select("ul.govuk-list > li").get(1).text() shouldBe expectedBulletPointText_2
        }

        "contains 'What to do if you lose your protection' link" in {
          doc.select("p.govuk-body").get(4).select("a").text() shouldBe whatToDoIfYouLoseProtection
          val expectedLink = "https://www.gov.uk/guidance/losing-your-lifetime-allowance-protection"
          doc.select("p.govuk-body").get(4).select("a").attr("href") shouldBe expectedLink
        }
      }

      "contain 'view or change details of your protection' paragraph" in {
        doc.select("#view-or-change-protection-details").text() shouldBe viewOrChangeProtectionDetailsText
        doc.select("#view-or-change-protection-details > a").text() shouldBe viewOrChangeProtectionDetailsLinkText
        val expectedLink = controllers.routes.ReadProtectionsController.currentProtections.url
        doc.select("#view-or-change-protection-details > a").attr("href") shouldBe expectedLink
      }

      "contain 'Give us feedback' section" in {
        doc.select("h2.govuk-heading-m").get(2).text() shouldBe giveUsFeedback
        doc.select("#survey-info").text() shouldBe exitSurveyText
        doc.select("#submit-survey-button").text() shouldBe exitSurveyLinkText
        doc.select("#submit-survey-button").attr("href") shouldBe controllers.routes.AccountController.signOut.url
      }
    }

    "provided with AmendResultDisplayModel containing notificationId: 11" should {

      val doc = parseDocument(amendOutcomeDisplayModelNotification11)

      "contain title" in {
        doc.title() shouldBe title
      }

      "contain (green) govuk panel" in {
        doc.select("h1.govuk-panel__title").text() shouldBe iP16Heading
        doc.select("#amendedAllowanceText").text() shouldBe yourNewProtectedAmount
        doc.select("strong#protectedAmount").text() shouldBe protectedAmountIndividualProtection2016InRange
      }

      "contain inset text" in {
        doc.select("div.govuk-inset-text").text() should include(NotificationId_11.insetText)
      }

      "contain h2 element" in {
        doc.select("h2.govuk-heading-m").get(0).text() shouldBe yourProtectionDetails
      }

      "contain a table".which {

        val tableHeadings = doc.select("tr th")
        val tableData     = doc.select("tr td")

        "contains row for name" in {
          val rowIndex = 0
          tableHeadings.get(rowIndex).text shouldBe tableAmendName
          tableData.get(rowIndex).attr("id") shouldBe "yourFullName"
          tableData.get(rowIndex).text shouldBe "Jim Davis"
        }

        "contains row for National Insurance Number" in {
          val rowIndex = 1
          tableHeadings.get(rowIndex).text shouldBe tableAmendNino
          tableData.get(rowIndex).attr("id") shouldBe "yourNino"
          tableData.get(rowIndex).text shouldBe "nino"
        }

        "contains row for Protection Type" in {
          val rowIndex = 2
          tableHeadings.get(rowIndex).text shouldBe tableAmendProtectionType
          tableData.get(rowIndex).attr("id") shouldBe "protectionType"
          tableData.get(rowIndex).text shouldBe "Individual protection 2016"
        }

        "contains row for Application Date" in {
          val rowIndex = 3
          tableHeadings.get(rowIndex).text shouldBe tableAmendApplicationDate
          tableData.get(rowIndex).attr("id") shouldBe "applicationDate"
          tableData.get(rowIndex).text shouldBe "14 July 2017"
        }

        "contains row for Application Time" in {
          val rowIndex = 4
          tableHeadings.get(rowIndex).text shouldBe tableAmendApplicationTime
          tableData.get(rowIndex).attr("id") shouldBe "applicationTime"
          tableData.get(rowIndex).text shouldBe "3:14pm"
        }

        "contains row for Status" in {
          val rowIndex = 5
          tableHeadings.get(rowIndex).text shouldBe tableStatus
          tableData.get(rowIndex).attr("id") shouldBe "status"
          tableData.get(rowIndex).text shouldBe "Dormant"
        }
      }

      "contain print info section" in {
        doc.select("p.govuk-body").get(0).text() shouldBe printGuidanceParagraph
        doc.select("a#printPage").text() shouldBe reviewAndPrintProtectionDetails
        doc.select("a#printPage").attr("href") shouldBe controllers.routes.PrintController.printView.url
      }

      "contain 'Changing your protection details' section".which {

        "contains header" in {
          doc.select("h2.govuk-heading-m").get(1).text() shouldBe changingProtectionDetailsHeadingText
        }

        "contains first paragraph" in {
          doc.select("p.govuk-body").get(2).text() shouldBe NotificationId_11.changingProtectionDetailsText
        }

        "contains paragraph with bullet points" in {
          doc.select("p.govuk-body").get(3).text() shouldBe changingProtectionDetailsContactHmrcText
          doc.select("p.govuk-body").get(3).select("a").text() shouldBe changingProtectionDetailsContactHmrcLinkText
          val expectedLink = "https://www.gov.uk/find-hmrc-contacts/pension-schemes-general-enquiries"
          doc.select("p.govuk-body").get(3).select("a").attr("href") shouldBe expectedLink

          val expectedBulletPointText_1 = NotificationId_11.changingProtectionDetailsBulletPointText_1
          val expectedBulletPointText_2 = NotificationId_11.changingProtectionDetailsBulletPointText_2
          doc.select("ul.govuk-list > li").get(0).text() shouldBe expectedBulletPointText_1
          doc.select("ul.govuk-list > li").get(1).text() shouldBe expectedBulletPointText_2
        }

        "contains 'What to do if you lose your protection' link" in {
          doc.select("p.govuk-body").get(4).select("a").text() shouldBe whatToDoIfYouLoseProtection
          val expectedLink = "https://www.gov.uk/guidance/losing-your-lifetime-allowance-protection"
          doc.select("p.govuk-body").get(4).select("a").attr("href") shouldBe expectedLink
        }
      }

      "contain 'view or change details of your protection' paragraph" in {
        doc.select("#view-or-change-protection-details").text() shouldBe viewOrChangeProtectionDetailsText
        doc.select("#view-or-change-protection-details > a").text() shouldBe viewOrChangeProtectionDetailsLinkText
        val expectedLink = controllers.routes.ReadProtectionsController.currentProtections.url
        doc.select("#view-or-change-protection-details > a").attr("href") shouldBe expectedLink
      }

      "contain 'Give us feedback' section" in {
        doc.select("h2.govuk-heading-m").get(2).text() shouldBe giveUsFeedback
        doc.select("#survey-info").text() shouldBe exitSurveyText
        doc.select("#submit-survey-button").text() shouldBe exitSurveyLinkText
        doc.select("#submit-survey-button").attr("href") shouldBe controllers.routes.AccountController.signOut.url
      }
    }

    "provided with AmendResultDisplayModel containing notificationId: 12" should {

      val doc = parseDocument(amendOutcomeDisplayModelNotification12)

      "contain title" in {
        doc.title() shouldBe title
      }

      "contain (green) govuk panel" in {
        doc.select("h1.govuk-panel__title").text() shouldBe iP16Heading
        doc.select("#amendedAllowanceText").text() shouldBe yourNewProtectedAmount
        doc.select("strong#protectedAmount").text() shouldBe protectedAmountIndividualProtection2016InRange
      }

      "contain inset text" in {
        doc.select("div.govuk-inset-text").text() should include(NotificationId_12.insetText)
      }

      "contain h2 element" in {
        doc.select("h2.govuk-heading-m").get(0).text() shouldBe yourProtectionDetails
      }

      "contain a table".which {

        val tableHeadings = doc.select("tr th")
        val tableData     = doc.select("tr td")

        "contains row for name" in {
          val rowIndex = 0
          tableHeadings.get(rowIndex).text shouldBe tableAmendName
          tableData.get(rowIndex).attr("id") shouldBe "yourFullName"
          tableData.get(rowIndex).text shouldBe "Jim Davis"
        }

        "contains row for National Insurance Number" in {
          val rowIndex = 1
          tableHeadings.get(rowIndex).text shouldBe tableAmendNino
          tableData.get(rowIndex).attr("id") shouldBe "yourNino"
          tableData.get(rowIndex).text shouldBe "nino"
        }

        "contains row for Protection Type" in {
          val rowIndex = 2
          tableHeadings.get(rowIndex).text shouldBe tableAmendProtectionType
          tableData.get(rowIndex).attr("id") shouldBe "protectionType"
          tableData.get(rowIndex).text shouldBe "Individual protection 2016"
        }

        "contains row for Application Date" in {
          val rowIndex = 3
          tableHeadings.get(rowIndex).text shouldBe tableAmendApplicationDate
          tableData.get(rowIndex).attr("id") shouldBe "applicationDate"
          tableData.get(rowIndex).text shouldBe "14 July 2017"
        }

        "contains row for Application Time" in {
          val rowIndex = 4
          tableHeadings.get(rowIndex).text shouldBe tableAmendApplicationTime
          tableData.get(rowIndex).attr("id") shouldBe "applicationTime"
          tableData.get(rowIndex).text shouldBe "3:14pm"
        }

        "contains row for Status" in {
          val rowIndex = 5
          tableHeadings.get(rowIndex).text shouldBe tableStatus
          tableData.get(rowIndex).attr("id") shouldBe "status"
          tableData.get(rowIndex).text shouldBe "Dormant"
        }
      }

      "contain print info section" in {
        doc.select("p.govuk-body").get(0).text() shouldBe printGuidanceParagraph
        doc.select("a#printPage").text() shouldBe reviewAndPrintProtectionDetails
        doc.select("a#printPage").attr("href") shouldBe controllers.routes.PrintController.printView.url
      }

      "contain 'Changing your protection details' section".which {

        "contains header" in {
          doc.select("h2.govuk-heading-m").get(1).text() shouldBe changingProtectionDetailsHeadingText
        }

        "contains first paragraph" in {
          doc.select("p.govuk-body").get(2).text() shouldBe NotificationId_12.changingProtectionDetailsText
        }

        "contains paragraph with bullet points" in {
          doc.select("p.govuk-body").get(3).text() shouldBe changingProtectionDetailsContactHmrcText
          doc.select("p.govuk-body").get(3).select("a").text() shouldBe changingProtectionDetailsContactHmrcLinkText
          val expectedLink = "https://www.gov.uk/find-hmrc-contacts/pension-schemes-general-enquiries"
          doc.select("p.govuk-body").get(3).select("a").attr("href") shouldBe expectedLink

          val expectedBulletPointText_1 = NotificationId_12.changingProtectionDetailsBulletPointText_1
          val expectedBulletPointText_2 = NotificationId_12.changingProtectionDetailsBulletPointText_2
          doc.select("ul.govuk-list > li").get(0).text() shouldBe expectedBulletPointText_1
          doc.select("ul.govuk-list > li").get(1).text() shouldBe expectedBulletPointText_2
        }

        "contains 'What to do if you lose your protection' link" in {
          doc.select("p.govuk-body").get(4).select("a").text() shouldBe whatToDoIfYouLoseProtection
          val expectedLink = "https://www.gov.uk/guidance/losing-your-lifetime-allowance-protection"
          doc.select("p.govuk-body").get(4).select("a").attr("href") shouldBe expectedLink
        }
      }

      "contain 'view or change details of your protection' paragraph" in {
        doc.select("#view-or-change-protection-details").text() shouldBe viewOrChangeProtectionDetailsText
        doc.select("#view-or-change-protection-details > a").text() shouldBe viewOrChangeProtectionDetailsLinkText
        val expectedLink = controllers.routes.ReadProtectionsController.currentProtections.url
        doc.select("#view-or-change-protection-details > a").attr("href") shouldBe expectedLink
      }

      "contain 'Give us feedback' section" in {
        doc.select("h2.govuk-heading-m").get(2).text() shouldBe giveUsFeedback
        doc.select("#survey-info").text() shouldBe exitSurveyText
        doc.select("#submit-survey-button").text() shouldBe exitSurveyLinkText
        doc.select("#submit-survey-button").attr("href") shouldBe controllers.routes.AccountController.signOut.url
      }
    }

    "provided with AmendResultDisplayModel containing notificationId: 13" should {

      val doc = parseDocument(amendOutcomeDisplayModelNotification13)

      "contain title" in {
        doc.title() shouldBe title
      }

      "contain (green) govuk panel" in {
        doc.select("h1.govuk-panel__title").text() shouldBe iP16Heading
        doc.select("#amendedAllowanceText").text() shouldBe yourNewProtectedAmount
        doc.select("strong#protectedAmount").text() shouldBe protectedAmountIndividualProtection2016Below
      }

      "contain inset text" in {
        doc.select("div.govuk-inset-text").text() should include(NotificationId_13.insetText)
      }

      "contain h2 element" in {
        doc.select("h2.govuk-heading-m").get(0).text() shouldBe yourProtectionDetails
      }

      "contain a table".which {

        val tableHeadings = doc.select("tr th")
        val tableData     = doc.select("tr td")

        "contains row for name" in {
          val rowIndex = 0
          tableHeadings.get(rowIndex).text shouldBe tableAmendName
          tableData.get(rowIndex).attr("id") shouldBe "yourFullName"
          tableData.get(rowIndex).text shouldBe "Jim Davis"
        }

        "contains row for National Insurance Number" in {
          val rowIndex = 1
          tableHeadings.get(rowIndex).text shouldBe tableAmendNino
          tableData.get(rowIndex).attr("id") shouldBe "yourNino"
          tableData.get(rowIndex).text shouldBe "nino"
        }

        "contains row for Protection Type" in {
          val rowIndex = 2
          tableHeadings.get(rowIndex).text shouldBe tableAmendProtectionType
          tableData.get(rowIndex).attr("id") shouldBe "protectionType"
          tableData.get(rowIndex).text shouldBe "Individual protection 2016"
        }

        "contains row for Application Date" in {
          val rowIndex = 3
          tableHeadings.get(rowIndex).text shouldBe tableAmendApplicationDate
          tableData.get(rowIndex).attr("id") shouldBe "applicationDate"
          tableData.get(rowIndex).text shouldBe "14 July 2017"
        }

        "contains row for Application Time" in {
          val rowIndex = 4
          tableHeadings.get(rowIndex).text shouldBe tableAmendApplicationTime
          tableData.get(rowIndex).attr("id") shouldBe "applicationTime"
          tableData.get(rowIndex).text shouldBe "3:14pm"
        }

        "contains row for Status" in {
          val rowIndex = 5
          tableHeadings.get(rowIndex).text shouldBe tableStatus
          tableData.get(rowIndex).attr("id") shouldBe "status"
          tableData.get(rowIndex).text shouldBe "Withdrawn"
        }
      }

      "contain print info section" in {
        doc.select("p.govuk-body").get(0).text() shouldBe printGuidanceParagraph
        doc.select("a#printPage").text() shouldBe reviewAndPrintProtectionDetails
        doc.select("a#printPage").attr("href") shouldBe controllers.routes.PrintController.printView.url
      }

      "contain 'Changing your protection details' section" in {
        doc.select("h2.govuk-heading-m").get(1).text() shouldBe changingProtectionDetailsHeadingText
        doc.select("p.govuk-body").get(2).text() shouldBe changingProtectionDetailsText
        val expectedLink = "https://www.gov.uk/find-hmrc-contacts/pension-schemes-general-enquiries"
        doc.select("p.govuk-body").get(2).select("a").attr("href") shouldBe expectedLink
      }

      "contain 'view or change details of your protection' paragraph" in {
        doc.select("#view-or-change-protection-details").text() shouldBe viewOrChangeProtectionDetailsText
        doc.select("#view-or-change-protection-details > a").text() shouldBe viewOrChangeProtectionDetailsLinkText
        val expectedLink = controllers.routes.ReadProtectionsController.currentProtections.url
        doc.select("#view-or-change-protection-details > a").attr("href") shouldBe expectedLink
      }

      "contain 'Give us feedback' section" in {
        doc.select("h2.govuk-heading-m").get(2).text() shouldBe giveUsFeedback
        doc.select("#survey-info").text() shouldBe exitSurveyText
        doc.select("#submit-survey-button").text() shouldBe exitSurveyLinkText
        doc.select("#submit-survey-button").attr("href") shouldBe controllers.routes.AccountController.signOut.url
      }
    }

    "provided with AmendResultDisplayModel containing notificationId: 14" should {

      val doc = parseDocument(amendOutcomeDisplayModelNotification14)

      "contain title" in {
        doc.title() shouldBe title
      }

      "contain (green) govuk panel" in {
        doc.select("h1.govuk-panel__title").text() shouldBe iP16Heading
        doc.select("#amendedAllowanceText").text() shouldBe yourNewProtectedAmount
        doc.select("strong#protectedAmount").text() shouldBe protectedAmountIndividualProtection2016Below
      }

      "contain inset text" in {
        doc.select("div.govuk-inset-text").text() should include(NotificationId_14.insetText_1)
        doc.select("div.govuk-inset-text > p").text() should include(NotificationId_14.insetText_2)
      }

      "contain h2 element" in {
        doc.select("h2.govuk-heading-m").get(0).text() shouldBe yourProtectionDetails
      }

      "contain a table".which {

        val tableHeadings = doc.select("tr th")
        val tableData     = doc.select("tr td")

        "contains row for name" in {
          val rowIndex = 0
          tableHeadings.get(rowIndex).text shouldBe tableAmendName
          tableData.get(rowIndex).attr("id") shouldBe "yourFullName"
          tableData.get(rowIndex).text shouldBe "Jim Davis"
        }

        "contains row for National Insurance Number" in {
          val rowIndex = 1
          tableHeadings.get(rowIndex).text shouldBe tableAmendNino
          tableData.get(rowIndex).attr("id") shouldBe "yourNino"
          tableData.get(rowIndex).text shouldBe "nino"
        }

        "contains row for Protection Type" in {
          val rowIndex = 2
          tableHeadings.get(rowIndex).text shouldBe tableAmendProtectionType
          tableData.get(rowIndex).attr("id") shouldBe "protectionType"
          tableData.get(rowIndex).text shouldBe "Fixed protection 2016"
        }

        "contains row for Fixed Protection 2016 Reference Number" in {
          val rowIndex = 3
          tableHeadings.get(rowIndex).text shouldBe tableAmendFixedProtectionRef
          tableData.get(rowIndex).attr("id") shouldBe "fixedProtectionRefNum"
          tableData.get(rowIndex).text shouldBe protectionReferenceFixedProtection2016
        }

        "contains row for Application Date" in {
          val rowIndex = 4
          tableHeadings.get(rowIndex).text shouldBe tableAmendApplicationDate
          tableData.get(rowIndex).attr("id") shouldBe "applicationDate"
          tableData.get(rowIndex).text shouldBe "14 July 2017"
        }

        "contains row for Application Time" in {
          val rowIndex = 5
          tableHeadings.get(rowIndex).text shouldBe tableAmendApplicationTime
          tableData.get(rowIndex).attr("id") shouldBe "applicationTime"
          tableData.get(rowIndex).text shouldBe "3:14pm"
        }
      }

      "contain print info section" in {
        doc.select("p.govuk-body").get(0).text() shouldBe printGuidanceParagraph
        doc.select("a#printPage").text() shouldBe reviewAndPrintProtectionDetails
        doc.select("a#printPage").attr("href") shouldBe controllers.routes.PrintController.printView.url
      }

      "contain 'Changing your protection details' section".which {

        "contains header" in {
          doc.select("h2.govuk-heading-m").get(1).text() shouldBe changingProtectionDetailsHeadingText
        }

        "contains first paragraph" in {
          doc.select("p.govuk-body").get(2).text() shouldBe NotificationId_14.changingProtectionDetailsText
        }

        "contains paragraph with bullet points" in {
          doc.select("p.govuk-body").get(3).text() shouldBe changingProtectionDetailsContactHmrcText
          doc.select("p.govuk-body").get(3).select("a").text() shouldBe changingProtectionDetailsContactHmrcLinkText
          val expectedLink = "https://www.gov.uk/find-hmrc-contacts/pension-schemes-general-enquiries"
          doc.select("p.govuk-body").get(3).select("a").attr("href") shouldBe expectedLink

          val expectedBulletPointText_1 = NotificationId_14.changingProtectionDetailsBulletPointText_1
          val expectedBulletPointText_2 = NotificationId_14.changingProtectionDetailsBulletPointText_2
          doc.select("ul.govuk-list > li").get(0).text() shouldBe expectedBulletPointText_1
          doc.select("ul.govuk-list > li").get(1).text() shouldBe expectedBulletPointText_2
        }

        "contains 'What to do if you lose your protection' link" in {
          doc.select("p.govuk-body").get(4).select("a").text() shouldBe whatToDoIfYouLoseProtection
          val expectedLink = "https://www.gov.uk/guidance/losing-your-lifetime-allowance-protection"
          doc.select("p.govuk-body").get(4).select("a").attr("href") shouldBe expectedLink
        }
      }

      "contain 'view or change details of your protection' paragraph" in {
        doc.select("#view-or-change-protection-details").text() shouldBe viewOrChangeProtectionDetailsText
        doc.select("#view-or-change-protection-details > a").text() shouldBe viewOrChangeProtectionDetailsLinkText
        val expectedLink = controllers.routes.ReadProtectionsController.currentProtections.url
        doc.select("#view-or-change-protection-details > a").attr("href") shouldBe expectedLink
      }

      "contain 'Give us feedback' section" in {
        doc.select("h2.govuk-heading-m").get(2).text() shouldBe giveUsFeedback
        doc.select("#survey-info").text() shouldBe exitSurveyText
        doc.select("#submit-survey-button").text() shouldBe exitSurveyLinkText
        doc.select("#submit-survey-button").attr("href") shouldBe controllers.routes.AccountController.signOut.url
      }
    }
  }

}
