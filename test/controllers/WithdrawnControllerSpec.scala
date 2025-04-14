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

package controllers

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.scalatest.Assertion
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{AnyContent, MessagesControllerComponents, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import testHelpers._
import views.html.pages.ip2014.withdrawn
import views.html.pages.ip2016.withdrawnAP2016

import scala.concurrent.Future

class WithdrawnControllerSpec extends FakeApplication with MockitoSugar {

  lazy val mockMCC: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]

  implicit lazy val view2014: withdrawn       = app.injector.instanceOf[withdrawn]
  implicit lazy val view2016: withdrawnAP2016 = app.injector.instanceOf[withdrawnAP2016]

  val controller                            = new WithdrawnController(mockMCC, view2014, view2016)
  implicit val request: Request[AnyContent] = FakeRequest("GET", "/")

  implicit lazy val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(request)

  "Withdrawn controller" should {
    "should show withdrawn page for 2014" in {
      val result = controller.showWithdrawn2014()(request)
      contentAsString(result) should include(view2014().body)
    }

    "should show withdrawn page for 2016" in {
      val result = controller.showWithdrawn2016()(request)
      contentAsString(result) should include(view2016().body)
    }
  }

  "IP14 application" should
    Seq(
      "/apply-for-ip14-pensions-taken",
      "/apply-for-ip14-pensions-taken-before",
      "/apply-for-ip14-pensions-taken-between",
      "/apply-for-ip14-overseas-pensions",
      "/apply-for-ip14-current-pensions",
      "/apply-for-ip14-pension-sharing-orders",
      "/apply-for-ip14-pension-sharing-order-details",
      "/apply-for-ip14-remove-pension-sharing-order-details",
      "/apply-for-ip14-submit-your-application"
    ).foreach { path =>
      s"show withdrawn page for /check-your-pension-protections$path" in
        assertRendersWithdrawnAppsPage(
          controller.showWithdrawn2014()(FakeRequest(GET, s"/check-your-pension-protections$path")),
          view2014()
        )
    }

  "IP16 application" should
    Seq(
      "/apply-for-ip16-pensions-taken",
      "/apply-for-ip16-pensions-taken-before",
      "/apply-for-ip16-pensions-worth-before",
      "/apply-for-ip16-pensions-taken-between",
      "/apply-for-ip16-pensions-used-between",
      "/apply-for-ip16-overseas-pensions",
      "/apply-for-ip16-current-pensions",
      "/apply-for-ip16-pension-sharing-orders",
      "/apply-for-ip16-pension-sharing-order-details",
      "/apply-for-ip16-remove-pension-sharing-order-details",
      "/apply-for-ip16-submit-your-application"
    ).foreach { path =>
      s"show withdrawn page for /check-your-pension-protections$path" in
        assertRendersWithdrawnAppsPage(
          controller.showWithdrawn2016()(FakeRequest(GET, s"/check-your-pension-protections$path")),
          view2016()
        )
    }

  private def assertRendersWithdrawnAppsPage(res: Future[Result], expected: Html): Assertion = {

    val (result, view) = (Jsoup.parse(contentAsString(res)), Jsoup.parse(expected.body))

    def headingOf(doc: Document): String      = doc.getElementById("pageHeading").text()
    def mainContentOf(doc: Document): Element = doc.getElementById("startAgainText")

    result.title() shouldBe view.title()
    headingOf(result) shouldBe headingOf(view)
    mainContentOf(result).text() shouldBe mainContentOf(view).text()
    mainContentOf(result).attr("href") shouldBe mainContentOf(view).attr("href")
  }

}
