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
import org.jsoup.Jsoup
import play.api.Play.current
import play.api.i18n.Messages.Implicits.applicationMessages
import play.api.i18n.{Lang, Messages}
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import testHelpers.{MockTemplateRenderer, PlaTestContext}
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.pages.withdraw.{withdrawImplicationMessage => views}

class WithdrawImplicationMessage extends UnitSpec with WithFakeApplication {

  lazy val fakeRequest = FakeRequest()
  lazy val fakeRequestWithSession = fakeRequest.withSession((SessionKeys.sessionId, ""))

  def fakeRequestToPOSTWithSession (input: (String, String)*): FakeRequest[AnyContentAsFormUrlEncoded] =
    fakeRequestWithSession.withFormUrlEncodedBody(input: _*)

  "Withdraw Implication Message view" when {
    lazy val protectionType = "ip14"
    lazy val view = views(protectionType, "dormant")(fakeRequest, applicationMessages, Lang.defaultLang, fakeApplication, PlaTestContext, PlaFormPartialRetriever, MockTemplateRenderer)
    lazy val doc = Jsoup.parse(view.body)

    s"have a alert of ${"pla.withdraw.implication.info"}" in {
      doc.select("div").text() shouldBe Messages("pla.withdraw.implication.info", s"pla.withdraw.protection.$protectionType.label")
    }

    "have a div tag size" in {
      doc.select("div").size() shouldBe 1
    }

    "have the class 'alert alert'" in {
      doc.hasClass("alert alert") shouldBe true
    }

  }

}
