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

package views.pages.result

import enums.ApplicationType
import models.RejectionDisplayModel
import org.jsoup.Jsoup
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.result.ResultRejected
import views.html.pages.result.{resultRejected => views}


class ResultRejectedSpec extends CommonViewSpecHelper with ResultRejected {

  "The Result Rejected Page" should {

    lazy val model = RejectionDisplayModel("24", Seq("1", "2"), ApplicationType.IP2016)
    lazy val view = views(model, showUserResearchPanel = false)
    lazy val doc = Jsoup.parse(view.body)

    "have the correct title" in {
      doc.title() shouldBe plaResultRejectionTitle
    }

    "have the first heading which" should {

      lazy val h1Tag = doc.select("h1")

      s"have the heading text $plaResultRejectionPageHeading" in {
        h1Tag.text shouldBe plaResultRejectionPageHeading
      }

      "have the correct Id" in {
        h1Tag.attr("id") shouldBe "resultPageHeading"
      }
    }

    "have the result outcome paragraph which" should {

      "contain the text" in {
        doc.select("p").get(0).text shouldBe "You've added fixed protection 2016"
      }

      "have the correct Id" in {
        doc.select("p").get(0).attr("id") shouldBe "resultOutcome"
      }
    }

    "have the additional info paragraph which" should {

      "contain the text" in {
        doc.select("p").get(1).text shouldBe "As you already have individual protection 2014 in place, fixed protection 2016 will only become active if you lose individual protection 2014."
      }

      "have the correct Id" in {
        doc.select("p").get(1).attr("id") shouldBe "additionalInfo1"
      }
    }

    "have a second heading which" should {

      lazy val h2Tag0 = doc.select("h2").get(0)

      s"have the heading text $plaResultSuccessIPChangeDetails" in {
        h2Tag0.text shouldBe plaResultSuccessIPChangeDetails
      }
    }

    "have a rejection details paragraph which" should {

      s"contain the message $plaResultRejectionViewDetails" in {
        doc.select("p").get(3).text shouldBe s"$plaResultRejectionViewDetails"
      }

      "harbour a link with the destination $" in {
        doc.select("p a").get(1).attr("href") shouldBe "/protect-your-lifetime-allowance/existing-protections"
      }

      "have link text" in {
        doc.select("p a").get(1).text shouldBe plaResultRejectionViewDetailsLinkText
      }
    }

    "have a third heading which" should {

      lazy val h2Tag1 = doc.select("h2").get(1)

      s"have the heading text of $plaResultSuccessGiveFeedback" in {
        h2Tag1.text shouldBe plaResultSuccessGiveFeedback
      }
    }
    "have an exit survey paragraph which" should {
      s"contain the message of $plaResultSuccessExitSurvey" in {
        doc.select("p").get(4).text shouldBe s"$plaResultSuccessExitSurveyLinkText $plaResultSuccessExitSurvey."
      }
      "harbour a link with the destination $" in {
        doc.select("p a").get(2).attr("href") shouldBe plaResultSuccessExitSurveyLink
      }
      "have link text" in {
        doc.select("p a").get(2).text shouldBe plaResultSuccessExitSurveyLinkText
      }
    }

  }
}
