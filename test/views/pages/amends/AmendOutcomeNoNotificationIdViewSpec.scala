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

import models.display.AmendOutcomeDisplayModelNoNotificationId
import models.pla.AmendProtectionLifetimeAllowanceType
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import testHelpers.CommonViewSpecHelper
import testHelpers.messages.amends.OutcomeAmendedViewMessages
import testdata.AmendProtectionDisplayModelTestData._
import views.html.pages.amends.amendOutcomeNoNotificationId

class AmendOutcomeNoNotificationIdViewSpec extends CommonViewSpecHelper with OutcomeAmendedViewMessages {

  val view: amendOutcomeNoNotificationId = inject[amendOutcomeNoNotificationId]

  def parseDocument(amendResultDisplayModel: AmendOutcomeDisplayModelNoNotificationId): Document =
    Jsoup.parse(view(amendResultDisplayModel).body)

  "amendOutcome" when {

    "provided with AmendResultDisplayModel containing no notificationId".which {
      Seq(
        AmendProtectionLifetimeAllowanceType.IndividualProtection2014 -> amendResultDisplayModelNoNotificationIdIndividualProtection2014,
        AmendProtectionLifetimeAllowanceType.IndividualProtection2014LTA -> amendResultDisplayModelNoNotificationIdIndividualProtection2014LTA
      ).foreach { case (protectionType, amendResultDisplayModel) =>
        s"has protection type of $protectionType" should {

          val doc = parseDocument(amendResultDisplayModel)

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
            doc.select("strong#protectedAmount").text() shouldBe protectedAmountIndividualProtection2014Max
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
              tableData.get(rowIndex).text shouldBe "Active"
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

      }

      Seq(
        AmendProtectionLifetimeAllowanceType.IndividualProtection2016 -> amendResultDisplayModelNoNotificationIdIndividualProtection2016,
        AmendProtectionLifetimeAllowanceType.IndividualProtection2016LTA -> amendResultDisplayModelNoNotificationIdIndividualProtection2016LTA
      ).foreach { case (protectionType, amendResultDisplayModel) =>
        s"has protection type of $protectionType" should {

          val doc = parseDocument(amendResultDisplayModel)

          "not have back link with text back " in {
            val backButton = doc.select(".govuk-back-link").text()
            backButton.isEmpty shouldBe true
          }

          "contain title" in {
            doc.title() shouldBe title
          }

          "contain (green) govuk panel" in {
            doc.select("h1.govuk-panel__title").text() shouldBe iP16Heading
            doc.select("#amendedAllowanceText").text() shouldBe yourNewProtectedAmount
            doc.select("strong#protectedAmount").text() shouldBe protectedAmountIndividualProtection2016Max
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
              tableData.get(rowIndex).text shouldBe "Active"
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

      }
    }
  }

}
