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

import enums.ApplicationType
import models.amendModels.AmendsGAModel
import models.{ActiveAmendResultDisplayModel, ProtectionDetailsDisplayModel}
import org.jsoup.Jsoup
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.amends.OutcomeActiveViewSpecMessages
import views.html.pages.amends.outcomeActive

class OutcomeActiveViewSpec extends CommonViewSpecHelper with OutcomeActiveViewSpecMessages {

  val amendsGAModel = AmendsGAModel(
    current = Some("current"),
    before = Some("before"),
    between = Some("between"),
    overseas = Some("overseas"),
    pso = Some("pso")
  )

  val amendsActiveResultModelIP16 = ActiveAmendResultDisplayModel(
    protectionType = ApplicationType.IP2016,
    notificationId = "44",
    protectedAmount = "£1,350,000.45",
    details = Some(ProtectionDetailsDisplayModel(
      protectionReference = Some("protectionRef"),
      psaReference = "psaRef",
      applicationDate = Some("14 June 2017")
    ))
  )

  val amendsActiveResultModelIP14 = ActiveAmendResultDisplayModel(
    protectionType = ApplicationType.IP2014,
    notificationId = "33",
    protectedAmount = "£1,350,000.11",
    details = Some(ProtectionDetailsDisplayModel(
      protectionReference = Some("protectionRef"),
      psaReference = "psaRef",
      applicationDate = Some("14 June 2017")
    ))
  )

  lazy val viewIP16 = application.injector.instanceOf[outcomeActive]
  lazy val docIP16 = Jsoup.parse(viewIP16.apply(amendsActiveResultModelIP16,Some(amendsGAModel)).body)

  lazy val viewIP14 = application.injector.instanceOf[outcomeActive]
  lazy val docIP14 = Jsoup.parse(viewIP14.apply(amendsActiveResultModelIP14, Some(amendsGAModel)).body)

  "the OutcomeActiveView" should{
    "have the correct title" in{
      docIP16.title() shouldBe plaResultSuccessOutcomeActiveTitle
    }

    "have the right success message displayed for IP16" in{
      docIP16.select("span#amendmentOutcome").text() shouldBe plaResultSuccessIP16Heading
      docIP16.select("p#amendedAllowanceText").text() shouldBe plaResultSuccessAllowanceSubHeading
      docIP16.select("span#protectedAmount").text() shouldBe "£1,350,000.45"
    }

    "have the right success message displayed for IP14" in{
      docIP14.select("span#amendmentOutcome").text() shouldBe plaResultSuccessIP14Heading
      docIP14.select("p#amendedAllowanceText").text() shouldBe plaResultSuccessAllowanceSubHeading
      docIP14.select("span#protectedAmount").text() shouldBe "£1,350,000.11"
    }

    "have a properly structured 'Your protection details' section" when{
      "looking at the header" in{
        docIP16.select("h2").eq(0).text() shouldBe plaResultSuccessProtectionDetails
      }

      "looking at the explanatory paragraph" in{
        docIP16.select("p").eq(1).text() shouldBe plaResultSuccessDetailsContent
      }

      "looking at the bullet point list" in{
        val details = amendsActiveResultModelIP16.details.get
        docIP16.select("li#yourFullName").text() shouldBe plaResultSuccessYourName
        docIP16.select("li#yourNino").text() shouldBe plaResultSuccessYourNino
        docIP16.select("li#protectionRef").text() shouldBe plaResultSuccessProtectionRef + s": ${details.protectionReference.get}"
        docIP16.select("li#psaRef").text() shouldBe plaResultSuccessPsaRef + s": ${details.psaReference}"
        docIP16.select("li#applicationDate").text() shouldBe plaResultSuccessApplicationDate + s": ${details.applicationDate.get}"
      }

      "have the right print message" in{
        docIP16.select("a#printPage").text() shouldBe plaResultSuccessPrint
        docIP16.select("a#printPage").attr("href") shouldBe controllers.routes.PrintController.printView().url
      }
    }

    "have a properly structured 'Changing your protection details' section" when{
      "looking at the header" in{
        docIP16.select("h2").eq(1).text() shouldBe plaResultSuccessIPChangeDetails
      }

      "looking at the explanatory paragraph" in{
        docIP16.select("p").eq(3).text() shouldBe plaResultSuccessIPPensionSharing
        docIP16.select("p").eq(4).text() shouldBe plaResultSuccessViewDetails
      }

      "using the links" in{
        docIP16.select("a").eq(1).text() shouldBe plaResultSuccessIPPensionSharingLinkText
        docIP16.select("a").eq(1).attr("href") shouldBe plaResultSuccessIPPensionsSharingLink
        docIP16.select("a").eq(2).text() shouldBe plaResultSuccessViewDetailsLinkText
        docIP16.select("a").eq(2).attr("href") shouldBe controllers.routes.ReadProtectionsController.currentProtections().url
      }
    }

    "have a properly structured 'Give us feedback' section" when{
      "looking at the header" in{
        docIP16.select("h2").eq(2).text() shouldBe plaResultSuccessGiveFeedback
      }
      "looking at the explanatory paragraph" in{
        docIP16.select("p").eq(5).text() shouldBe plaResultSuccessExitSurvey
      }
      "using the feedback link" in{
        docIP16.select("a").eq(3).text() shouldBe plaResultSuccessExitSurveyLinkText
      }
    }

  }
}
