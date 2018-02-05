/*
 * Copyright 2018 HM Revenue & Customs
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
import models.{ProtectionDetailsDisplayModel, SuccessDisplayModel}
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.result.ResultSuccess
import views.html.pages.result.{resultSuccess => views}
import org.jsoup.Jsoup
import play.api.i18n.Messages.Implicits._

class ResultSuccessSpec extends CommonViewSpecHelper with ResultSuccess {

  "The Result Success page" should {

    lazy val protectionmodel = ProtectionDetailsDisplayModel(Some(""), "", Some(""))
    lazy val protectionmodel2 = ProtectionDetailsDisplayModel(Some("None"), "PSA33456789", Some("15/07/2015"))
    lazy val testmodel = SuccessDisplayModel(ApplicationType.IP2016, "24", "100.00", true, Some(protectionmodel), Seq("1", "2"))
    lazy val testmodel2 = SuccessDisplayModel(ApplicationType.FP2016, "24", "100.00", false, Some(protectionmodel2), Seq("1", "2"))
    lazy val view = views(testmodel)
    lazy val view2 = views(testmodel2)
    lazy val doc = Jsoup.parse(view.body)
    lazy val doc2 = Jsoup.parse(view2.body)


    "have the correct title" in {
      doc.title() shouldBe plaResultSuccessTitle
    }

    "have a results section which" should {

      lazy val firstPara = doc.select("p").get(0)
      lazy val protecAmount = doc.select("span").get(1)

      "have an initial heading with the text" in {
        doc.select("span").get(0).text shouldBe "You've added fixed protection 2016"
      }

      "have an initial heading with the id" in {
        doc.select("span").get(0).attr("id") shouldBe "resultOutcome"
      }

      "have a paragraph with the text" in {
        firstPara.text shouldBe plaResultSuccessAllowanceSubHeading
      }

      "have the correct paragraph id + class" in {
        firstPara.attr("id") shouldBe "resultAllowanceText"
      }

      "have a span of protected amount" in {
        protecAmount.text shouldBe "100.00"
      }

      "have the correct id + class" in {
        protecAmount.attr("id") shouldBe "protectedAmount"
      }
    }

    "has a result paragraph with code which" should {

      "have the text" in {
        doc.body.select("div p").get(1).text shouldBe "As you already have individual protection 2014 in place, fixed protection 2016 will only become active if you lose individual protection 2014."
      }

      "have the Id" in {
        doc.select("div p").get(1).attr("id") shouldBe "additionalInfo1"
      }

    }

    "has a sub-heading with paragraph which" should {

      s"have the heading text $plaResultSuccessProtectionDetails" in {
        doc.select("h2").get(0).text shouldBe plaResultSuccessProtectionDetails
      }

      s"have the paragraph text $plaResultSuccessDetailsContent" in {
        doc.select("div p").get(3).text shouldBe plaResultSuccessDetailsContent
      }
    }

    "have a list of result details which are mapped and" should {

      "have a name line(protection ref defined)" in {
        doc2.select("ul li").get(0).attr("id") shouldBe "yourFullName"
        doc2.select("ul li").get(0).text shouldBe plaResultSuccessYourName
      }

      "have a nino line(protection ref defined)" in {
        doc2.select("ul li").get(1).attr("id") shouldBe "yourNino"
        doc2.select("ul li").get(1).text shouldBe plaResultSuccessYourNino
      }

      "have a protection reference line(protection ref defined)" in {
        doc2.select("ul li").get(2).attr("id") shouldBe "protectionRef"
        doc2.select("ul li").get(2).text shouldBe s"$plaResultSuccessProtectionRef: None"
      }

      "have a psa line(protection ref defined)" in {
        doc2.select("ul li").get(3).attr("id") shouldBe "psaRef"
        doc2.select("ul li").get(3).text shouldBe s"$plaResultSuccessPsaRef: PSA33456789"
      }

      "have an application date line(protection ref defined)" in {
        doc2.select("ul li").get(4).attr("id") shouldBe "applicationDate"
        doc2.select("ul li").get(4).text shouldBe s"$plaResultSuccessApplicationDate: 15/07/2015"
      }


      "have a name line(protection ref not defined)" in {
        doc.select("ul li").get(0).attr("id") shouldBe "yourFullName"
        doc.select("ul li").get(0).text shouldBe plaResultSuccessYourName
      }

      "have a nino line(protection ref not defined)" in {
        doc.select("ul li").get(1).attr("id") shouldBe "yourNino"
        doc.select("ul li").get(1).text shouldBe plaResultSuccessYourNino
      }

      "have a psa line(protection ref not defined)" in {
        doc.select("ul li").get(3).attr("id") shouldBe "psaRef"
        doc.select("ul li").get(3).text shouldBe s"$plaResultSuccessPsaRef:"
      }

    }

    "have a print page link which" should {

      lazy val printLink = doc.body.select("p a")
      lazy val linkPara = doc.body.select("div p").get(4)

      "have the paragraph class" in {
        linkPara.attr("class") shouldBe "print-link"
      }

      s"have the link text $plaResultSuccessPrint ($plaBaseNewWindow)" in {
        linkPara.text shouldBe s"$plaResultSuccessPrint ($plaBaseNewWindow)"
      }

      "have the destination" in {
        doc.select("p a").get(1).attr("href") shouldBe "/protect-your-lifetime-allowance/print-protection"
      }

      "have the link id" in {
        printLink.attr("id") shouldBe "printPage"
      }

      "should specify a blank target" in {
        printLink.attr("target") shouldBe "_blank"
      }
    }

    "has a second subheading which" should {
      s"have the heading text $plaResultSuccessIPChangeDetails" in {
        doc.select("h2").get(1).text shouldBe plaResultSuccessIPChangeDetails
      }
    }

    "have a dynamic result success paragraph which" should {

      "have a IP Pension sharing paragraph which" should {

        "have the text" in {
          doc.body.select("p").get(5).text shouldBe plaResultSuccessIPPensionSharing
        }

        "have the correct Id" in {
          doc.body.select("p").get(5).attr("id") shouldBe "ipPensionSharing"
        }
      }

      "have a FP Add To Pension paragraph which" should {

        "have the text" in {
          doc2.select("p").get(5).text shouldBe plaResultSuccessFPAddToPension
        }

        "have the correct Id" in {
          doc2.select("p").get(5).attr("id") shouldBe "fpAddToPension"
        }
      }
    }

    "has a paragraph which" should {

      lazy val detailsLink = doc.select("p a").get(3)

      s"have the paragraph text $plaResultSuccessViewDetails" in {
        doc.select("p").get(6).text shouldBe s"$plaResultSuccessViewDetails $plaResultSuccessViewDetailsLinkText."
      }

      "have the destination" in {
        detailsLink.attr("href") shouldBe "/protect-your-lifetime-allowance/existing-protections"
      }

      "have the link id" in {
        detailsLink.attr("id") shouldBe "existingProtectionsLink"
      }

      "have the link text" in {
        detailsLink.text shouldBe plaResultSuccessViewDetailsLinkText
      }

    }

    "has a third subheading which" should {
      s"have the heading text $plaResultSuccessGiveFeedback" in {
        doc.select("h2").get(2).text shouldBe plaResultSuccessGiveFeedback
      }
    }

    "have a Exit Survey paragraph which" should {

      lazy val exitLink = doc.select("p a").get(4)

      "have the text" in {
        doc.body.select("p").get(7).text shouldBe plaResultSuccessExitSurveyCombined
      }

      "have the link text" in {
        exitLink.text shouldBe plaResultSuccessExitSurveyLinkText
      }

      "have the link destination" in {
        exitLink.attr("href") shouldBe "/protect-your-lifetime-allowance/exit"
      }

    }

  }
}