/*
 * Copyright 2025 HM Revenue & Customs
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

package views.pages.ip2016

import config.FrontendAppConfig
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import views.html.pages.ip2016.withdrawnAP2016

class WithdrawnAP2016ViewSpec
    extends AnyWordSpecLike
    with Matchers
    with MockitoSugar
    with BeforeAndAfterEach
    with GuiceOneAppPerSuite {

  val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

  override implicit lazy val app: Application = GuiceApplicationBuilder()
    .overrides(bind[FrontendAppConfig].toInstance(mockAppConfig))
    .build()

  val fakeRequest = FakeRequest()

  def view: withdrawnAP2016            = app.injector.instanceOf[withdrawnAP2016]
  implicit lazy val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(fakeRequest)

  def doc: Document = Jsoup.parse(view()(fakeRequest, messages).body)

  "WithdrawnAP2016 view" must {

    "display the correct title" when {

      "HIP migration feature toggle is enabled" in {
        when(mockAppConfig.hipMigrationEnabled).thenReturn(true)
        doc.title() shouldBe s"${messages("pla.withdrawn.ap2016.title")} - ${messages("service.name")} - GOV.UK"
      }

      "HIP migration feature toggle is disabled" in {
        when(mockAppConfig.hipMigrationEnabled).thenReturn(false)
        doc.title() shouldBe s"${messages("pla.withdrawn.ap2016.title")} - ${messages("psa.service.name")} - GOV.UK"
      }
    }

    "display the correct heading" in {
      doc.getElementById("pageHeading").text() shouldBe messages("pla.withdrawn.ap2016.pageHeading")
    }

    "display the correct paragraph" in {
      doc.getElementById("startAgainText").text() should startWith(messages("pla.withdrawn.ap2016.paraOne"))
    }

    "display the correct link" in {

      val link = doc.getElementById("startAgainText").firstElementChild()

      link.attr("href") shouldBe controllers.routes.ReadProtectionsController.currentProtections.url
      link.text() shouldBe messages("pla.withdrawn.ap2016.paraOne.linkText")
    }
  }

}
