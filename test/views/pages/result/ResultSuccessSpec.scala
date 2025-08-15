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

package views.pages.result

import enums.ApplicationType
import models.{ProtectionDetailsDisplayModel, SuccessDisplayModel}
import org.jsoup.Jsoup
import org.mockito.Mockito.when
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.result.ResultSuccess
import views.html.pages.result.resultSuccess

class ResultSuccessSpec extends CommonViewSpecHelper with ResultSuccess {

  "The Result Success page" should {

    lazy val protectionModelWithNoDetails = ProtectionDetailsDisplayModel(Some(""), "", Some(""))
    lazy val protectionModelWithDetails = ProtectionDetailsDisplayModel(Some("None"), "PSA33456789", Some("15/07/2015"))

    lazy val ip2016Model = SuccessDisplayModel(
      ApplicationType.IP2016,
      "24",
      "100.00",
      true,
      Some(protectionModelWithNoDetails),
      Seq("1", "2")
    )
    lazy val fp2016Model = SuccessDisplayModel(
      ApplicationType.FP2016,
      "24",
      "100.00",
      false,
      Some(protectionModelWithDetails),
      Seq("1", "2")
    )

    def view = app.injector.instanceOf[resultSuccess]

    def ip2016Doc = Jsoup.parse(view.apply(ip2016Model, false).body)
    def fp2016Doc = Jsoup.parse(view.apply(fp2016Model, false).body)

    "have the correct title" when {

      "HIP migration feature toggle is enabled" in {
        when(mockAppConfig.hipMigrationEnabled).thenReturn(true)
        ip2016Doc.title() shouldBe plaResultSuccessTitleHip
      }

      "HIP migration feature toggle is disabled" in {
        when(mockAppConfig.hipMigrationEnabled).thenReturn(false)
        ip2016Doc.title() shouldBe plaResultSuccessTitle
      }
    }

    "have a results section which" should {

      lazy val firstPara    = ip2016Doc.select("p").get(0)
      lazy val protecAmount = ip2016Doc.select("strong")

      "have an initial heading with the text" in {
        ip2016Doc.select("h1").text shouldBe "You've added fixed protection 2016"
      }

      "have an initial heading with the id" in {
        ip2016Doc.select("h1").attr("id") shouldBe "resultOutcome"
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
        ip2016Doc.body
          .select("div p")
          .get(1)
          .text shouldBe "As you already have individual protection 2014 in place, fixed protection 2016 will only become active if you lose individual protection 2014."
      }

      "have the Id" in {
        ip2016Doc.select("div p").get(1).attr("id") shouldBe "additionalInfo1"
      }

    }

    "has a sub-heading with paragraph which" should {

      s"have the heading text $plaResultSuccessProtectionDetails" in {
        ip2016Doc.select("h2").get(0).text shouldBe plaResultSuccessProtectionDetails
      }

      s"have the paragraph text $plaResultSuccessDetailsContent" in {
        ip2016Doc.select("div p").get(3).text shouldBe plaResultSuccessDetailsContent
      }
    }

    "have a list of result details which are mapped and" should {

      "have a name line(protection ref defined)" in {
        fp2016Doc.select("#main-content > div > div > ul >li:nth-child(1)").attr("id") shouldBe "yourFullName"
        fp2016Doc.select("#main-content > div > div > ul >li:nth-child(1)").text shouldBe plaResultSuccessYourName
      }

      "have a nino line(protection ref defined)" in {
        fp2016Doc.select("#main-content > div > div > ul >li:nth-child(2)").attr("id") shouldBe "yourNino"
        fp2016Doc.select("#main-content > div > div > ul >li:nth-child(2)").text shouldBe plaResultSuccessYourNino
      }

      "have a protection reference line(protection ref defined)" in {
        fp2016Doc.select("#main-content > div > div > ul >li:nth-child(3)").attr("id") shouldBe "protectionRef"
        fp2016Doc
          .select("#main-content > div > div > ul >li:nth-child(3)")
          .text shouldBe s"$plaResultSuccessProtectionRef: None"
      }

      "have a psa line(protection ref defined)" in {
        fp2016Doc.select("#main-content > div > div > ul >li:nth-child(4)").attr("id") shouldBe "psaRef"
        fp2016Doc
          .select("#main-content > div > div > ul >li:nth-child(4)")
          .text shouldBe s"$plaResultSuccessPsaRef: PSA33456789"
      }

      "have an application date line(protection ref defined)" in {
        fp2016Doc.select("#main-content > div > div > ul >li:nth-child(5)").attr("id") shouldBe "applicationDate"
        fp2016Doc
          .select("#main-content > div > div > ul >li:nth-child(5)")
          .text shouldBe s"$plaResultSuccessApplicationDate: 15/07/2015"
      }

      "have a name line(protection ref not defined)" in {
        ip2016Doc.select("#main-content > div > div > ul >li:nth-child(1)").attr("id") shouldBe "yourFullName"
        ip2016Doc.select("#main-content > div > div > ul >li:nth-child(1)").text shouldBe plaResultSuccessYourName
      }

      "have a nino line(protection ref not defined)" in {
        ip2016Doc.select("#main-content > div > div > ul >li:nth-child(2)").attr("id") shouldBe "yourNino"
        ip2016Doc.select("#main-content > div > div > ul >li:nth-child(2)").text shouldBe plaResultSuccessYourNino
      }

      "have a psa line(protection ref not defined)" in {
        ip2016Doc.select("#main-content > div > div > ul >li:nth-child(4)").attr("id") shouldBe "psaRef"
        ip2016Doc.select("#main-content > div > div > ul >li:nth-child(4)").text shouldBe s"$plaResultSuccessPsaRef:"
      }

    }

    "have a print page link which" should {

      lazy val printLink = ip2016Doc.body.select("#printPage")

      "have the paragraph class" in {
        printLink.attr("class") shouldBe "govuk-link"
      }

      s"have the link text $plaResultSuccessPrint ($plaBaseNewTab)" in {
        printLink.text shouldBe s"$plaResultSuccessPrint ($plaBaseNewTab)"
      }

      "have the destination" in {
        printLink.attr("href") shouldBe "/check-your-pension-protections/print-protection"
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
        ip2016Doc.select("h2").get(1).text shouldBe plaResultSuccessIPChangeDetails
      }
    }

    "have a dynamic result success paragraph which" should {

      "have a IP Pension sharing paragraph which" should {

        "have the text" in {
          ip2016Doc.body.select("p").get(5).text shouldBe plaResultSuccessIPPensionSharing
        }

        "have the correct Id" in {
          ip2016Doc.body.select("p").get(5).attr("id") shouldBe "ipPensionSharing"
        }
      }

      "have a FP Add To Pension paragraph which" should {

        "have the text" in {
          fp2016Doc.select("p").get(5).text shouldBe plaResultSuccessFPAddToPension
        }

        "have the correct Id" in {
          fp2016Doc.select("p").get(5).attr("id") shouldBe "fpAddToPension"
        }
      }
    }

    "has a paragraph which" should {

      lazy val detailsLink = ip2016Doc.select("p a").get(3)

      s"have the paragraph text $plaResultSuccessViewDetails" in {
        ip2016Doc.select("p").get(6).text shouldBe s"$plaResultSuccessViewDetails"
      }

      "have the destination" in {
        detailsLink.attr("href") shouldBe "/check-your-pension-protections/existing-protections"
      }

      "have the link text" in {
        detailsLink.text shouldBe plaResultSuccessViewDetailsLinkText
      }

    }

    "has a third subheading which" should {
      s"have the heading text $plaResultSuccessGiveFeedback" in {
        ip2016Doc.select("h2").get(2).text shouldBe plaResultSuccessGiveFeedback
      }
    }
    "have a Exit Survey paragraph which" should {
      lazy val exitLink = ip2016Doc.select("p a").get(4)
      "have the text" in {
        ip2016Doc.body.select("p").get(7).text shouldBe plaResultSuccessExitSurveyCombined
      }
      "have the link text" in {
        exitLink.text shouldBe plaResultSuccessExitSurveyLinkText
      }
      "have the link destination" in {
        exitLink.attr("href") shouldBe plaResultSuccessExitSurveyLink
      }
    }

  }

}
