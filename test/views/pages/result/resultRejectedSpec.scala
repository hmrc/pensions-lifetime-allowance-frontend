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
import enums.ApplicationType.IP2016
import models.RejectionDisplayModel
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import testHelpers.ViewSpecHelpers.{CommonMessages, CommonViewSpecHelper}
import testHelpers.ViewSpecHelpers.result.resultRejected
import views.html.pages.result.{resultRejected => views}


class resultRejectedSpec extends CommonViewSpecHelper with resultRejected with CommonMessages {

  "The Result Rejected Page" should {

    lazy val model = RejectionDisplayModel("24", Seq(""), ApplicationType.IP2016)
    lazy val view = views(model)
    lazy val doc = Jsoup.parse(view.body)

    "have the correct title" in {
      doc.title() shouldBe plaResultRejectionTitle
    }

    "have the first heading which" should {

      lazy val h1Tag = doc.select("h1")

      s"have the heading text $plaResultRejectionPageHeading" in {
        h1Tag.text shouldBe plaResultRejectionPageHeading
      }

      "have the heading-large class" in {
        h1Tag.hasClass("heading-large") shouldBe true
      }

      "have the correct Id" in {
        h1Tag.attr("id") shouldBe "resultPageHeading"
      }
    }

    "have the result outcome paragraph which" should {

      "contain the text" in {
        //doc.select("p").get(0).text shouldBe Messages(s"$resultCode.'+res.notificationId+'.heading")
      }

      "have the correct Id" in {
        //doc.select("p").get(0) shouldBe "resultOutcome"
      }
    }

    "have the additional info paragraph which" should {

      "contain the text" in {
        //doc.select("p").get(1).text shouldBe Messages(s"$resultCode.' + res.notificationId+ '." + infoNum)
      }

      "have the correct Id" in {
        //doc.select("p").get(1) shouldBe Messages(s"additionalInfo$infoNum")
      }
    }

    "have a second heading which" should {

      lazy val h2Tag0 = doc.select("h2").get(0)

      s"have the heading text $plaResultRejectionPageHeading" in {
        h2Tag0.text shouldBe plaResultRejectionPageHeading
      }
    }

    "have a rejection details paragraph which" should {

      s"contain the message $plaResultRejectionViewDetails" in {
        doc.select("p").get(2).text shouldBe plaResultRejectionViewDetails
      }

      "have a link Id" in {
        doc.select("p a").get(0).attr("Id") shouldBe "existingProtectionsLink"
      }

      "harbour a link with the destination $" in {
        doc.select("p a").get(0).attr("href") shouldBe controllers.routes.ReadProtectionsController.currentProtections
      }

      "have link text" in {
        doc.select("p a").get(0).text shouldBe plaResultRejectionViewDetailsLinkText
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
        doc.select("p").get(3) shouldBe plaResultSuccessExitSurvey
      }

      "harbour a link with the destination $" in {
        doc.select("p a").get(1).attr("href") shouldBe controllers.routes.ExitSurveyController.exitSurvey
      }

      "have link text" in {
        doc.select("p a").get(1).text shouldBe plaResultSuccessExitSurveyLinkText
      }
    }
  }
}

//@(res: RejectionDisplayModel)(implicit request: Request[_], messages: Messages, lang: Lang, application: Application, context: config.PlaContext, partialRetriever: uk.gov.hmrc.play.partials.FormPartialRetriever, templateRenderer: uk.gov.hmrc.renderer.TemplateRenderer)
//
//@lc = @{Application.instanceCache[PlaLanguageController].apply(application) }
//
//@views.html.main_template(title = Messages("pla.resultRejection.title"), bodyClasses = None) {
//
//<h1 class="heading-large" id="resultPageHeading">@Messages("pla.resultRejection.pageHeading")</h1>
//
//<p id="resultOutcome">@Messages("resultCode."+res.notificationId+".heading")</p>
//
//@for(infoNum <- res.additionalInfo) {
//<p id=@{s"additionalInfo$infoNum"}>@Html(Messages("resultCode." + res.notificationId+ "." + infoNum))</p>
//}
//
//<h2>@Messages("pla.resultSuccess.IPChangeDetails")</h2>
//
//<p>@Html(Messages("pla.resultRejection.viewDetails")) <a id="existingProtectionsLink" href=@controllers.routes.ReadProtectionsController.currentProtections>@Messages("pla.resultRejection.viewDetailsLinkText")</a>.</p>
//
//<h2>@Messages("pla.resultSuccess.giveFeedback")</h2>
//
//<p><a href=@controllers.routes.ExitSurveyController.exitSurvey>@Messages("pla.resultSuccess.exitSurveyLinkText")</a> @Html(Messages("pla.resultSuccess.exitSurvey"))</p>
//}