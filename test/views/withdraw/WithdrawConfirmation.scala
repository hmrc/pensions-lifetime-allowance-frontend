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

package views.withdraw

import config.wiring.PlaFormPartialRetriever
import controllers.routes
import org.jsoup.Jsoup
import play.api.Play.current
import play.api.i18n.Messages.Implicits.applicationMessages
import play.api.i18n.{Lang, Messages}
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.twirl.api.Html
import testHelpers.{MockTemplateRenderer, PlaTestContext}
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.pages.withdraw.{withdrawConfirmation => views}


class WithdrawConfirmation extends UnitSpec with WithFakeApplication {
  lazy val fakeRequest = FakeRequest()
  lazy val fakeRequestWithSession = fakeRequest.withSession((SessionKeys.sessionId, ""))

  def fakeRequestToPOSTWithSession (input: (String, String)*): FakeRequest[AnyContentAsFormUrlEncoded] =
    fakeRequestWithSession.withFormUrlEncodedBody(input: _*)

  private def getIntepretedText(htmlString: String): String = {
    Jsoup.parse(s"<div>$htmlString</div>").select("div").get(0).text
  }

  "Withdraw Confirmation view" when {
    lazy val protectionType = "ip14"
    lazy val view = views(protectionType)(fakeRequest, applicationMessages, Lang.defaultLang, fakeApplication, PlaTestContext, PlaFormPartialRetriever, MockTemplateRenderer)
    lazy val doc = Jsoup.parse(view.body)

    s"have a title ${"pla.withdraw.confirmation.message"}" in {
      doc.title() shouldBe Messages("pla.withdraw.confirmation.message",  s"pla.withdraw.protection.$protectionType.label")
    }

    s"have a question of ${"pla.withdraw.confirmation.message"}" in {
      doc.select("div.grid-row > div.transaction-banner--complete > span.heading-large").text() shouldBe Messages("pla.withdraw.confirmation.message", Messages(s"pla.withdraw.protection.${protectionType}.label"))
    }

    "have a grid that" should {
      lazy val grid = doc.select("div.grid-row > div.grid")

      "have the class 'grid'" in {
        grid.hasClass("grid") shouldBe true
      }

      s"has the first paragraph of ${"pla.withdraw.confirmation.check.details"}" in {

        val msg = getIntepretedText(Messages("pla.withdraw.confirmation.check.details", routes.ReadProtectionsController.currentProtections()))

        grid.select("p").get(0).text shouldBe msg
//        grid.select("p").get(0).text shouldBe Html(Messages("pla.withdraw.confirmation.check.details", routes.ReadProtectionsController.currentProtections())).toString().replaceAll("<a href=\"/protect-your-lifetime-allowance/existing-protections\">", "").replaceAll("</a>", "")
      }

      s"has the second paragraph of ${"pla.withdraw.confirmation.contact.you.if.needed"}" in {
        grid.select("p").get(1).text shouldBe Messages("pla.withdraw.confirmation.contact.you.if.needed")
      }

      "have a div tag size" in {
        grid.size() shouldBe 1
      }
    }

    s"have a message of ${"pla.withdraw.confirm.feedback-heading"}" in {
      doc.select("div.grid-row > h2").text shouldBe Messages("pla.withdraw.confirm.feedback-heading")
    }

    s"feedback message of ${"pla.withdraw.confirm.feedback-text"}" in {
      doc.select("div.grid-row > p").text shouldBe Html(Messages("pla.withdraw.confirm.feedback-text", routes.ExitSurveyController.exitSurvey())).toString().replaceAll("</a>", "").replaceAll("<a href=\"/protect-your-lifetime-allowance/exit\">", "")
    }

    "have a div tag" in {
      doc.select("div").size() shouldBe 3
    }


  }

}
