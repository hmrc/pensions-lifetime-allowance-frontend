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

package views.pages.lookup

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Mockito.when
import play.api.i18n.{Messages, MessagesApi}
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import views.html.pages.lookup.withdrawnPSALookupJourney

class WithdrawnPSALookupJourneySpec extends CommonViewSpecHelper {

  def view: withdrawnPSALookupJourney = app.injector.instanceOf[withdrawnPSALookupJourney]

  implicit lazy val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(fakeRequest)

  def doc: Document = Jsoup.parse(view()(fakeRequest, messages).body)

  "withdrawnPSALookupJourney view" must {

    "display the correct title" when {

      "the HIP migration feature toggle is enabled" in {
        when(mockAppConfig.hipMigrationEnabled).thenReturn(true)

        doc.title() shouldBe s"${messages("psa.lookup.withdraw.title")} - ${messages("psa.service.name")} - GOV.UK"
      }

      "the HIP migration feature toggle is disabled" in {
        when(mockAppConfig.hipMigrationEnabled).thenReturn(false)

        doc.title() shouldBe s"${messages("psa.lookup.withdraw.title")} - ${messages("psa.service.name")} - GOV.UK"
      }
    }

    "display the correct heading" in {
      doc.getElementById("pageHeading").text() shouldBe messages("psa.lookup.withdraw.pageHeading")
    }

    "display the correct paragraph" in {
      doc.getElementById("startAgainText").text() should startWith(messages("psa.lookup.withdraw.paraOne"))
    }

    "display the correct link" in {
      when(mockAppConfig.psaLookupWithdrawLinkUrl).thenReturn(
        "http://tax.service.gov.uk/members-protections-and-enhancements/start"
      )
      doc.getElementById("link").attr("href") shouldBe mockAppConfig.psaLookupWithdrawLinkUrl
      doc
        .getElementById("link")
        .attr("href") shouldBe "http://tax.service.gov.uk/members-protections-and-enhancements/start"
      doc.getElementById("link").text() shouldBe messages("psa.lookup.withdrawLinkText")
    }
  }

}
