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
import forms.WithdrawDateForm
import org.jsoup.Jsoup
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.i18n.{Lang, Messages}
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import testHelpers.{MockTemplateRenderer, PlaTestContext}
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.pages.withdraw.{withdrawImplications => views}

class WithdrawImplicationsSpec extends UnitSpec with WithFakeApplication {

  lazy val fakeRequest = FakeRequest()
  lazy val fakeRequestWithSession = fakeRequest.withSession((SessionKeys.sessionId, ""))

  def fakeRequestToPOSTWithSession (input: (String, String)*): FakeRequest[AnyContentAsFormUrlEncoded] =
    fakeRequestWithSession.withFormUrlEncodedBody(input: _*)

  "Withdraw Confirm view" when {
    val protectionType = "ip14"
    val status = "dormant"
    lazy val view = views(WithdrawDateForm, protectionType, status)(fakeRequest, applicationMessages, Lang.defaultLang, fakeApplication, PlaTestContext, PlaFormPartialRetriever, MockTemplateRenderer)
    lazy val doc = Jsoup.parse(view.body)

    s"have a title ${"pla.withdraw.protection.title"}" in {
      doc.title() shouldBe Messages("pla.withdraw.protection.title", s"pla.withdraw.protection.$protectionType.label")
    }

    s"have a back link with text back " in {
      doc.select("a.back-link").text() shouldBe "Back"
    }

    s"have a back link with href" in {
      doc.select("a.back-link").attr("href") shouldBe "/protect-your-lifetime-allowance/existing-protections"
    }

    s"have the question of the page ${"pla.withdraw.protection.title"}" in {
      doc.select("h1.heading-large").text shouldEqual Messages("pla.withdraw.protection.title", s"pla.withdraw.protection.$protectionType.label")
    }

    "have a grid that" should {
      "have a heading label" in {
        doc.select("div.grid > p").text() shouldBe Messages("pla.withdraw.protection.if.info", s"pla.withdraw.protection.$protectionType.label")
      }


    }

  }

}
