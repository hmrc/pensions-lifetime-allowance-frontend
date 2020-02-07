/*
 * Copyright 2020 HM Revenue & Customs
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

import models.InactiveAmendResultDisplayModel
import models.amendModels.AmendsGAModel
import org.jsoup.Jsoup
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.amends.OutcomeInactiveViewSpecMessages
import views.html.pages.amends.{outcomeInactive => views}

class OutcomeInactiveViewSpec extends CommonViewSpecHelper with OutcomeInactiveViewSpecMessages {

  lazy val amendsGAModel = AmendsGAModel(
    current = Some("current"),
    before = Some("before"),
    between = Some("between"),
    overseas = Some("overseas"),
    pso = Some("pso")
  )

  lazy val amendsInactiveResultModelIP16 = InactiveAmendResultDisplayModel(
    notificationId = "43",
    additionalInfo = Seq("1","2")
  )

  lazy val amendsInactiveResultModelIP14 = InactiveAmendResultDisplayModel(
    notificationId = "32",
    additionalInfo = Seq("1","2")
  )

  lazy val viewIP16 = views(amendsInactiveResultModelIP16, Some(amendsGAModel))
  lazy val docIP16 = Jsoup.parse(viewIP16.body)

  lazy val viewIP14 = views(amendsInactiveResultModelIP14, Some(amendsGAModel))
  lazy val docIP14 = Jsoup.parse(viewIP14.body)

  "the OutcomeInactiveView" should{
    "have the correct title" in{
      docIP16.title() shouldBe plaResultSuccessOutcomeActiveTitle
    }

    "have the correct header for IP16" in{
      docIP16.select("h1").text() shouldBe plaResultSuccessIP16Heading
    }

    "have the correct header for IP14" in{
      docIP14.select("h1").text() shouldBe plaResultSuccessIP14Heading
    }

    "have the correct structure for IP16" when{
      "looking at the explanatory paragraph displayed" in{
        docIP16.select("p#additionalInfo1").text() shouldBe plaAmendResultCodeIP16AdditionalInfoOne
        docIP16.select("p#additionalInfo2").text() shouldBe plaAmendResultCodeAdditionalInfoTwo
      }

      "checking the 'HMRC Pensions Schemes Services' link" in{
        docIP16.select("a").eq(0).text() shouldBe plaResultSuccessIPPensionSharingLinkText
        docIP16.select("a").attr("href") shouldBe plaResultSuccessIPPensionsSharingLink
      }
    }

    "have the correct structure for IP14" when{
      "looking at the explanatory paragraph displayed" in{
        docIP14.select("p#additionalInfo1").text() shouldBe plaAmendResultCodeIP14AdditionalInfoOne
        docIP14.select("p#additionalInfo2").text() shouldBe plaAmendResultCodeAdditionalInfoTwo
      }

      "checking the 'HMRC Pensions Schemes Services' link" in{
        docIP14.select("a").eq(0).text() shouldBe plaResultSuccessIPPensionSharingLinkText
        docIP14.select("a").attr("href") shouldBe plaResultSuccessIPPensionsSharingLink
      }
    }

    "have a properly structured 'Changing your protection details' section" when{
      "looking at the header" in{
        docIP16.select("h2").eq(0).text() shouldBe plaResultSuccessIPChangeDetails
      }

      "looking at the explanatory paragraph" in{
        docIP16.select("p").eq(2).text() shouldBe plaResultSuccessIPPensionSharing
        docIP16.select("p").eq(3).text() shouldBe plaResultSuccessViewDetailInactive
      }

      "using the links" in{
        docIP16.select("a").eq(0).text() shouldBe plaResultSuccessIPPensionSharingLinkText
        docIP16.select("a").eq(0).attr("href") shouldBe plaResultSuccessIPPensionsSharingLink
        docIP16.select("a").eq(2).text() shouldBe plaResultSuccessViewDetailsLinkText
        docIP16.select("a").eq(2).attr("href") shouldBe controllers.routes.ReadProtectionsController.currentProtections().url
      }
    }
    "have a properly structured 'Give us feedback' section" when{
      "looking at the header" in{
        docIP16.select("h2").eq(1).text() shouldBe plaResultSuccessGiveFeedback
      }
      "looking at the explanatory paragraph" in{
        docIP16.select("p").eq(4).text() shouldBe plaResultSuccessExitSurvey
      }
      "using the feedback link" in{
        docIP16.select("a").eq(3).text() shouldBe plaResultSuccessExitSurveyLinkText
      }
    }
  }
}
