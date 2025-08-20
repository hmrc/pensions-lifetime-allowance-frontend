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

import models.{AmendDisplayModel, AmendDisplayRowModel, AmendDisplaySectionModel}
import org.jsoup.Jsoup
import org.mockito.Mockito.when
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.amends.AmendSummaryViewSpecMessages
import views.html.pages.amends.amendSummary

class AmendSummaryViewSpec extends CommonViewSpecHelper with AmendSummaryViewSpecMessages {

  lazy val tstPensionContributionPsoDisplaySections = Seq(
    AmendDisplaySectionModel(
      "PensionsTakenBefore",
      Seq(
        AmendDisplayRowModel(
          "YesNo",
          Some(controllers.routes.AmendsPensionTakenBeforeController.amendPensionsTakenBefore("ip2016", "active")),
          None,
          "Yes"
        ),
        AmendDisplayRowModel(
          "Amt",
          Some(controllers.routes.AmendsPensionTakenBeforeController.amendPensionsTakenBefore("ip2016", "active")),
          None,
          "£100,000"
        )
      )
    ),
    AmendDisplaySectionModel(
      "PensionsTakenBetween",
      Seq(
        AmendDisplayRowModel(
          "YesNo",
          Some(controllers.routes.AmendsPensionTakenBetweenController.amendPensionsTakenBetween("ip2016", "active")),
          None,
          "Yes"
        ),
        AmendDisplayRowModel(
          "Amt",
          Some(controllers.routes.AmendsPensionTakenBetweenController.amendPensionsTakenBetween("ip2016", "active")),
          None,
          "£100,000"
        )
      )
    ),
    AmendDisplaySectionModel(
      "OverseasPensions",
      Seq(
        AmendDisplayRowModel(
          "YesNo",
          Some(controllers.routes.AmendsOverseasPensionController.amendOverseasPensions("ip2016", "active")),
          None,
          "Yes"
        ),
        AmendDisplayRowModel(
          "Amt",
          Some(controllers.routes.AmendsOverseasPensionController.amendOverseasPensions("ip2016", "active")),
          None,
          "£100,000"
        )
      )
    ),
    AmendDisplaySectionModel(
      "CurrentPensions",
      Seq(
        AmendDisplayRowModel(
          "Amt",
          Some(controllers.routes.AmendsCurrentPensionController.amendCurrentPensions("ip2016", "active")),
          None,
          "£1,000,000.34"
        )
      )
    ),
    AmendDisplaySectionModel(
      "CurrentPsos",
      Seq(
        AmendDisplayRowModel("YesNo", None, None, "Yes"),
        AmendDisplayRowModel("Amt", None, None, "£1,000")
      )
    )
  )

  lazy val tstPsoDisplaySections = Seq(
    AmendDisplaySectionModel(
      "pensionDebits",
      Seq(
        AmendDisplayRowModel(
          "CurrentPsos-psoDetails",
          Some(controllers.routes.AmendsPensionSharingOrderController.amendPsoDetails("ip2016", "open")),
          Some(controllers.routes.AmendsRemovePensionSharingOrderController.removePso("ip2016", "open")),
          "£123456",
          "2 March 2017"
        )
      )
    )
  )

  val amendDisplayModel = AmendDisplayModel(
    protectionType = "IP2016",
    amended = true,
    pensionContributionSections = tstPensionContributionPsoDisplaySections,
    psoAdded = true,
    psoSections = tstPsoDisplaySections,
    totalAmount = "£1,300,000.34"
  )

  val amendDisplayModelWithoutPso = AmendDisplayModel(
    protectionType = "IP2016",
    amended = false,
    pensionContributionSections = tstPensionContributionPsoDisplaySections,
    psoAdded = false,
    psoSections = Seq(),
    totalAmount = "£1,100,000.34"
  )

  "the AmendSummaryView" should {
    def view = app.injector.instanceOf[amendSummary]

    def doc           = Jsoup.parse(view.apply(amendDisplayModel, "ip2016", "open").body)
    def docWithoutPso = Jsoup.parse(view.apply(amendDisplayModelWithoutPso, "ip2016", "open").body)

    lazy val form = doc.select("form")

    "have the correct title" when {

      "HIP migration feature toggle is enabled" in {
        when(mockAppConfig.hipMigrationEnabled).thenReturn(true)
        doc.title() shouldBe plaAmendsSummaryTitleHip
      }
      "HIP migration feature toggle is disabled" in {
        when(mockAppConfig.hipMigrationEnabled).thenReturn(false)
        doc.title() shouldBe plaAmendsSummaryTitle
      }
    }

    "have the correct and properly formatted header" in {
      doc.select("h1").first().text shouldBe plaAmendsHeaderOne
      doc.select("h2").first().text shouldBe plaSummaryPensionsHeading
    }

    "have a message to cancel amendments" in {
      doc.select("p").eq(0).text() shouldBe plaAmendsCancelText
      doc.select("a#cancelLink").text() shouldBe plaAmendsCancelLinkText
      doc.select("a#cancelLink").attr("href") shouldBe plaAmendsCancelLinkLocation
    }

    "have a link to add a new Pension sharing order" in {
      docWithoutPso.select("a#addPsoLink").text() shouldBe plaAmendsAddAPensionSharingOrderText
      docWithoutPso.select("a#addPsoLink").attr("href") shouldBe controllers.routes.AmendsPensionSharingOrderController
        .amendPsoDetails("ip2016", "open")
        .url
    }

    "have a properly structured table" when {
      "looking at the value of pensionsTakenBefore" in {
        doc.select(".govuk-summary-list__key").eq(0).text shouldBe plaSummaryQuestionsPensionsTakenBefore
        doc.select(".govuk-summary-list__value").eq(0).text shouldBe plaBaseYes
        doc.select(".govuk-summary-list__value").eq(1).text shouldBe "£100,000"
        doc
          .select(".govuk-summary-list__actions")
          .eq(0)
          .text shouldBe s"$plaBaseChange $plaSummaryQuestionsPensionsTakenBefore"
      }

      "looking at the YesNoValue and question for overseasPensions" in {
        doc.select(".govuk-summary-list__key").eq(4).text shouldBe plaSummaryQuestionsOverseasPensions
        doc.select(".govuk-summary-list__value").eq(4).text shouldBe plaBaseYes
        doc
          .select(".govuk-summary-list__actions")
          .eq(4)
          .text shouldBe s"$plaBaseChange $plaSummaryQuestionsOverseasPensions"
      }

      "looking at the overseasPensionsAmt value and question" in {
        doc.select(".govuk-summary-list__key").eq(5).text shouldBe plaSummaryQuestionsOverseasPensionsAmt
        doc.select(".govuk-summary-list__value").eq(5).text shouldBe "£100,000"
        doc
          .select(".govuk-summary-list__actions")
          .eq(5)
          .text shouldBe s"$plaBaseChange $plaSummaryQuestionsOverseasPensionsAmt"
      }

      "looking at the total value of pensions" in {
        doc.select(".govuk-summary-list__key").eq(9).text shouldBe plaSummaryQuestionsTotalPensionsAmt
        doc.select(".govuk-summary-list__value").eq(9).text shouldBe "£1,300,000.34"
      }

    }

    "have an properly structured table for additional pension sharing orders" when {
      "looking at the pso header" in {
        doc.select("h2").eq(1).text shouldBe plaSummaryPsosHeading
      }

      "looking at the added pso details" in {
        doc.select(".govuk-summary-list__key").eq(10).text shouldBe plaSummaryQuestionsPsoDetails
        doc.select(".govuk-summary-list__value").eq(10).text().contains(plaAmendsAdditionalPsoAmount) shouldBe true
        doc.select(".govuk-summary-list__value").eq(10).text().contains(plaAmendsAdditionalPsoDate) shouldBe true
      }

      "looking at the change and remove links" in {
        doc
          .select("a#pensionDebits-CurrentPsos-psoDetails-remove-link")
          .text shouldBe s"$plaBaseRemove $plaSummaryQuestionsPsoDetails"
        doc
          .select("a#pensionDebits-CurrentPsos-psoDetails-change-link")
          .text shouldBe s"$plaBaseChange $plaSummaryQuestionsPsoDetails"
      }
    }

    "have link to withdraw the protection for non-HIP flow" in {

      when(mockAppConfig.hipMigrationEnabled).thenReturn(false)

      lazy val view = app.injector.instanceOf[amendSummary]
      lazy val doc  = Jsoup.parse(view.apply(amendDisplayModelWithoutPso, "ip2016", "open").body)

      doc.select("p.govuk-body a.govuk-link").first().text shouldBe plaAmendsWithdrawProtectionText
      doc.select("p.govuk-body a.govuk-link").first().attr("href") shouldBe plaAmendsWithdrawProtectionLinkLocation
    }

    "have no link to withdraw the protection for HIP flow" in {

      when(mockAppConfig.hipMigrationEnabled).thenReturn(true)

      lazy val view = app.injector.instanceOf[amendSummary]
      lazy val doc  = Jsoup.parse(view.apply(amendDisplayModelWithoutPso, "ip2016", "open").body)

      doc.select("p.govuk-body a.govuk-link").last().text shouldBe plaAmendsAddAPensionSharingOrderText
      doc.select("p.govuk-body a.govuk-link").last().attr("href") shouldBe plaAmendsAddAPensionSharingOrderTextLink
    }

    "have an explanatory declaration paragraph before the submit button" in {
      doc.select("#declaration").text shouldBe plaAmendsDeclaration
    }

    "have a valid form submission" in {
      form.attr("method") shouldBe "POST"
      form.attr("action") shouldBe controllers.routes.AmendsController.amendProtection("ip2016", "open").url
    }

    "have a continue button" in {
      doc.select("button").text shouldBe plaAmendsSubmitButton
    }
  }

}
