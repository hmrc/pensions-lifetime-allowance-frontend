/*
 * Copyright 2021 HM Revenue & Customs
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

import forms.AmendmentTypeForm
import models.{AmendDisplayModel, AmendDisplayRowModel, AmendDisplaySectionModel}
import org.jsoup.Jsoup
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.amends.AmendSummaryViewSpecMessages
import uk.gov.hmrc.play.views.html.helpers.{ErrorSummary, FormWithCSRF}
import views.html.pages.amends.amendSummary

import scala.concurrent.ExecutionContext.Implicits.global

class AmendSummaryViewSpec extends CommonViewSpecHelper with AmendSummaryViewSpecMessages {

  implicit val errorSummary: ErrorSummary = app.injector.instanceOf[ErrorSummary]
  implicit val formWithCSRF: FormWithCSRF = app.injector.instanceOf[FormWithCSRF]

  lazy val tstPensionContributionPsoDisplaySections = Seq(
    AmendDisplaySectionModel("PensionsTakenBefore", Seq(
      AmendDisplayRowModel("YesNo", Some(controllers.routes.AmendsController.amendPensionsTakenBefore("ip2016", "active")), None, "Yes"),
      AmendDisplayRowModel("Amt", Some(controllers.routes.AmendsController.amendPensionsTakenBefore("ip2016", "active")), None, "£100,000")
    )
    ),
    AmendDisplaySectionModel("PensionsTakenBetween", Seq(
      AmendDisplayRowModel("YesNo", Some(controllers.routes.AmendsController.amendPensionsTakenBetween("ip2016", "active")), None, "Yes"),
      AmendDisplayRowModel("Amt", Some(controllers.routes.AmendsController.amendPensionsTakenBetween("ip2016", "active")), None, "£100,000")
    )
    ),
    AmendDisplaySectionModel("OverseasPensions", Seq(
      AmendDisplayRowModel("YesNo", Some(controllers.routes.AmendsController.amendOverseasPensions("ip2016", "active")), None, "Yes"),
      AmendDisplayRowModel("Amt", Some(controllers.routes.AmendsController.amendOverseasPensions("ip2016", "active")), None, "£100,000")
    )
    ),
    AmendDisplaySectionModel("CurrentPensions",Seq(
      AmendDisplayRowModel("Amt", Some(controllers.routes.AmendsController.amendCurrentPensions("ip2016", "active")), None, "£1,000,000.34")
    )
    ),
    AmendDisplaySectionModel("CurrentPsos", Seq(
      AmendDisplayRowModel("YesNo", None, None, "Yes"),
      AmendDisplayRowModel("Amt", None, None, "£1,000")
    )
    )
  )

  lazy val tstPsoDisplaySections = Seq(
    AmendDisplaySectionModel("pensionDebits", Seq(
      AmendDisplayRowModel("CurrentPsos-psoDetails",
        Some(controllers.routes.AmendsController.amendPsoDetails("ip2016", "open")),
        Some(controllers.routes.AmendsController.removePso("ip2016", "open")),
        "£123456", "2 March 2017")
    ))
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

  "the AmendSummaryView" should{
    val amendmentForm = AmendmentTypeForm.amendmentTypeForm.bind(Map(
      "protectionType" -> "ip2016",
      "status"         -> "open"))

    lazy val view = application.injector.instanceOf[amendSummary]
    lazy val viewWithoutPso = application.injector.instanceOf[amendSummary]

    lazy val doc = Jsoup.parse(view.apply(amendDisplayModel, "open", amendmentForm).body)
    lazy val docWithoutPso = Jsoup.parse(viewWithoutPso.apply(amendDisplayModelWithoutPso, "open", amendmentForm).body)

    lazy val form = doc.select("form")

    "have the correct title" in{
      doc.title() shouldBe plaAmendsSummaryTitle
    }

    "have the correct and properly formatted header" in{
      doc.select("h1").first().text shouldBe plaAmendsHeaderOne
      doc.select("h2").first().text shouldBe plaSummaryPensionsHeading
    }

    "have a message to cancel amendments" in{
      doc.select("p").eq(0).text() shouldBe plaAmendsCancelText
      doc.select("a#cancelLink").text() shouldBe plaAmendsCancelLinkText
      doc.select("a#cancelLink").attr("href") shouldBe plaAmendsCancelLinkLocation
    }

    "have a link to add a new Pension sharing order" in{
      docWithoutPso.select("a#addPsoLink").text() shouldBe plaAmendsAddAPensionSharingOrderText
      docWithoutPso.select("a#addPsoLink").attr("href") shouldBe controllers.routes.AmendsController.amendPsoDetails("ip2016", "open").url
    }

    "have a properly structured table" when{
      "looking at the value of pensionsTakenBefore" in{
        doc.select("td#PensionsTakenBefore-YesNo-question").text shouldBe plaSummaryQuestionsPensionsTakenBefore
        doc.select("span#PensionsTakenBeforeYesNoDisplayValue0").text shouldBe plaBaseYes
        doc.select("span#PensionsTakenBeforeAmtDisplayValue0").text shouldBe "£100,000"
        doc.select("a#PensionsTakenBefore-YesNo-change-link.bold-xsmall").text shouldBe plaBaseChange
      }

      "looking at the YesNoValue and question for overseasPensions" in{
        doc.select("td#OverseasPensions-YesNo-question").text shouldBe plaSummaryQuestionsOverseasPensions
        doc.select("span#OverseasPensionsYesNoDisplayValue0").text shouldBe plaBaseYes
        doc.select("a#OverseasPensions-YesNo-change-link.bold-xsmall").text shouldBe plaBaseChange
      }

      "looking at the overseasPensionsAmt value and question" in{
        doc.select("td#OverseasPensions-Amt-question").text shouldBe plaSummaryQuestionsOverseasPensionsAmt
        doc.select("span#OverseasPensionsAmtDisplayValue0").text shouldBe "£100,000"
        doc.select("a#OverseasPensions-Amt-change-link.bold-xsmall").text shouldBe plaBaseChange
      }

      "looking at the total value of pensions" in{
        doc.select("td#total-message.total-font.summary-text").text shouldBe plaSummaryQuestionsTotalPensionsAmt
        doc.select("td#total-amount").text shouldBe "£1,300,000.34"
      }

      "with the correct visually hidden text"in{
        doc.select("span.visuallyhidden").eq(0).text shouldBe plaAmendsVisuallyHiddenTextPensionsTakenBeforeYesNo
        doc.select("span.visuallyhidden").eq(1).text shouldBe plaAmendsVisuallyHiddenTextPensionsTakenBeforeAmt
        doc.select("span.visuallyhidden").eq(2).text shouldBe plaAmendsVisuallyHiddenTextPensionsTakenBetweenYesNo
        doc.select("span.visuallyhidden").eq(3).text shouldBe plaAmendsVisuallyHiddenTextPensionsTakenBetweenAmt
        doc.select("span.visuallyhidden").eq(4).text shouldBe plaAmendsVisuallyHiddenTextOverseasPensionsYesNo
        doc.select("span.visuallyhidden").eq(5).text shouldBe plaAmendsVisuallyHiddenTextOverseasPensionsAmt
        doc.select("span.visuallyhidden").eq(6).text shouldBe plaAmendsVisuallyHiddenTextCurrentPensionsAmt
        doc.select("span.visuallyhidden").eq(7).text shouldBe plaAmendsVisuallyHiddenTextRemoveText
        doc.select("span.visuallyhidden").eq(8).text shouldBe plaAmendsVisuallyHiddenTextChangeText
      }
    }

    "have an properly structured table for additional pension sharing orders" when {
      "looking at the pso header" in {
        doc.select("h2").eq(1).text shouldBe plaSummaryPsosHeading
      }

      "looking at the added pso details" in{
        doc.select("td#pensionDebits-CurrentPsos-psoDetails-question").text shouldBe plaSummaryQuestionsPsoDetails
        doc.select("span#pensionDebitsCurrentPsos-psoDetailsDisplayValue0").text shouldBe plaAmendsAdditionalPsoAmount
        doc.select("span#pensionDebitsCurrentPsos-psoDetailsDisplayValue1").text shouldBe plaAmendsAdditionalPsoDate
      }

      "looking at the change and remove links" in{
        doc.select("a#pensionDebits-CurrentPsos-psoDetails-remove-link").text shouldBe plaBaseRemove
        doc.select("a#pensionDebits-CurrentPsos-psoDetails-change-link").text shouldBe plaBaseChange
      }
    }

    "have a link to withdraw the protection" in{
      doc.select("a").last().text shouldBe plaAmendsWithdrawProtectionText
      doc.select("a").last().attr("href") shouldBe plaAmendsWithdrawProtectionLinkLocation

    }

    "have an explanatory declaration paragraph before the submit button" in{
      doc.select("p#declaration").text shouldBe plaAmendsDeclaration
    }

    "have a valid form submission" in{
      form.attr("method") shouldBe "POST"
      form.attr("action") shouldBe controllers.routes.AmendsController.amendProtection().url
    }

    "have a continue button" in{
      doc.select("button").text shouldBe plaAmendsSubmitButton
      doc.select("button").attr("type") shouldBe "submit"
    }
  }
}
