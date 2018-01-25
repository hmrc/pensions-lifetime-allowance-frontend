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
import testHelpers.ViewSpecHelpers.{CommonMessages, CommonViewSpecHelper}
import testHelpers.ViewSpecHelpers.result.{resultSuccess, resultSuccessInactive}
import views.html.pages.result.{resultSuccess => views}
import org.jsoup.Jsoup
import play.api.i18n.Messages.Implicits._


class resultSuccessSpec extends CommonViewSpecHelper with CommonMessages with resultSuccess {

  "The Result Success page" should {

    lazy val protectionmodel = ProtectionDetailsDisplayModel(Some(""), "", Some(""))
    lazy val testmodel = SuccessDisplayModel(ApplicationType.IP2016, "24", "100.00", false, Some(protectionmodel), Seq(""))
    lazy val view = views(testmodel)
    lazy val doc = Jsoup.parse(view.body)

    "have the correct title" in {
      doc.title() shouldBe plaResultSuccessTitle
    }

    "have a results section which" should {

      //      "have a heading with the text" in {
      //        doc.select("div div span").text shouldBe Messages(s"resultCode.${res.notificationId}.heading")
      //      }

      "have a paragraph with the text" in {
        doc.select("p").get(0).text shouldBe plaResultSuccessAllowanceSubHeading
      }

            "have the correct paragraph id + class" in {
              doc.select("div div span p").attr("id") shouldBe "resultAllowanceText"
              doc.select("div div span p").hasClass("medium") shouldBe true
            }

            "have a span of protected amount" in {
//              doc.select("div div span span").text shouldBe res.protectedAmount
            }

            "have the correct id + class" in {
              doc.select("div div span span").attr("id") shouldBe "protectedAmount"
              doc.select("div div span span").hasClass("bold-medium") shouldBe true
            }
          }
      //
      //    "has a result paragraph with code which" should {
      //
      //      "have the text" in {
      //        doc.select("div div span p").text shouldBe Messages(s"$resultCode." + res.notificationId + ".$infoNum")
      //      }
      //
      //      "have the Id" in {
      //        doc.select("div div span p").attr("id") shouldBe s"additionalInfo$infoNum"
      //      }
      //
      //    }

      "has a sub-heading with paragraph which" should {

        s"have the heading text $plaResultSuccessProtectionDetails" in {
          doc.select("h2").get(0).text shouldBe plaResultSuccessProtectionDetails
        }

        s"have the paragraph text $plaResultSuccessDetailsContent" in {
          doc.select("p").get(2).text shouldBe plaResultSuccessDetailsContent
        }
      }

      "has a print page link which" should {

        val printLink = doc.select("p a").get(0)
        val linkPara = doc.select("p").get(3)

        "have the paragraph class" in {
          linkPara.getClass shouldBe "print-link"
        }

        s"have the link text $plaResultSuccessPrint($plaBaseNewWindow)" in {
          linkPara.text shouldBe s"$plaResultSuccessPrint($plaBaseNewWindow)"
        }

        "have the destination" in {
          printLink.attr("href") shouldBe controllers.routes.PrintController.printView
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

      "have a IP Pension sharing paragraph which" should {

        "have the text" in {
          doc.select("p").get(4).text shouldBe plaResultSuccessIPPensionSharing
        }

        "have the correct Id" in {
          doc.select("p").get(4).attr("id") shouldBe "ipPensionSharing"
        }
      }


      "have a FP Add To Pension paragraph which" should {

        "have the text" in {
          doc.select("p").get(5).text shouldBe plaResultSuccessFPAddToPension
        }

        "have the correct Id" in {
          doc.select("p").get(5).attr("id") shouldBe "fpAddToPension"
        }

      }

      "has a paragraph which" should {

        val detailsLink = doc.select("p a").get(1)

        s"have the paragraph text $plaResultSuccessViewDetails" in {
          doc.select("p").get(6).text shouldBe plaResultSuccessViewDetails
        }

        "have the destination" in {
          detailsLink.attr("href") shouldBe controllers.routes.ReadProtectionsController.currentProtections
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

        lazy val exitLink = doc.select("p a").get(2)

        "have the text" in {
          doc.select("p").get(7).text shouldBe plaResultSuccessExitSurvey
        }

        "have the link text" in {
          exitLink.text shouldBe plaResultSuccessExitSurveyLinkText
        }

        "have the link destination" in {
          exitLink.attr("href") shouldBe controllers.routes.ExitSurveyController.exitSurvey
        }

      }

    }
}

//@views.html.main_template(title = Messages("pla.resultSuccess.title"), bodyClasses = None) {
//
//<div class="grid-row">
//<div class="transaction-banner--complete">
//<span class="heading-large" id="resultOutcome">@Messages(s"resultCode.${res.notificationId}.heading")</span>
//<p class="medium" id="resultAllowanceText">@Messages("pla.resultSuccess.allowanceSubHeading")</p>
//<span class="bold-medium" id="protectedAmount">@res.protectedAmount</span>
//</div>
//
//@for(infoNum <- res.additionalInfo) {
//<p id=@{s"additionalInfo$infoNum"}>@Html(Messages(s"resultCode.${res.notificationId}.$infoNum"))</p>
//}
//
//<h2>@Messages("pla.resultSuccess.protectionDetails")</h2>
//<p>@Messages("pla.resultSuccess.detailsContent")</p>
//
//@res.details.map{ details =>
//@resultDetails(details)
//}
//
//@if(res.printable) {
//<p class="print-link"><a id="printPage" href="@controllers.routes.PrintController.printView" target="_blank" onclick="ga('send', 'event', 'print-active-protection', 'print-@{res.protectionType.toString}', 'result');">@Messages("pla.resultSuccess.print") (@Messages("pla.base.newWindow"))</a></p>
//}
//
//<h2>@Messages("pla.resultSuccess.IPChangeDetails")</h2>
//
//@if(res.protectionType == ApplicationType.IP2016 || res.protectionType == ApplicationType.IP2014 || Constants.fpShowPensionSharing.contains(res.notificationId.toInt)) {
//<p id="ipPensionSharing">@Html(Messages("pla.resultSuccess.IPPensionSharing"))</p>
//}
//
//@if(res.protectionType == ApplicationType.FP2016 || Constants.ipShowAddToPension.contains(res.notificationId.toInt)) {
//<p id="fpAddToPension">@Html(Messages("pla.resultSuccess.FPAddToPension"))</p>
//}
//
//<p>@Html(Messages("pla.resultSuccess.viewDetails")) <a id="existingProtectionsLink" href=@controllers.routes.ReadProtectionsController.currentProtections>@Messages("pla.resultSuccess.viewDetailsLinkText")</a>.</p>
//
//<h2>@Messages("pla.resultSuccess.giveFeedback")</h2>
//
//<p><a href=@controllers.routes.ExitSurveyController.exitSurvey>@Messages("pla.resultSuccess.exitSurveyLinkText")</a> @Messages("pla.resultSuccess.exitSurvey")</p>
//
//</div>