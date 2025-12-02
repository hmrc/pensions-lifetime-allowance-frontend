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

package views.pages.amends

import common.Strings
import models.pla.AmendableProtectionType.IndividualProtection2016
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import testHelpers.CommonViewSpecHelper
import testHelpers.messages.amends.RemovePsoDetailsViewMessages
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import views.html.pages.amends.removePsoDebits

class RemovePsoDebitsViewSpec extends CommonViewSpecHelper with RemovePsoDetailsViewMessages {

  implicit val formWithCSRF: FormWithCSRF = inject[FormWithCSRF]

  val view: removePsoDebits = inject[removePsoDebits]
  val doc: Document         = Jsoup.parse(view.apply(IndividualProtection2016.toString, "open").body)

  "the RemovePsoDetailsView" should {
    "have the correct title" in {
      doc.title() shouldBe plaPsoDetailsTitleNew
    }

    "have the right explanatory paragraph" in {
      doc.select("p").eq(0).text shouldBe plaPsoDetailsRemovePso
    }

    "have the correct and properly formatted header" in {
      doc.select("h1.govuk-heading-xl").text shouldBe plaPsoDetailsTitle
    }

    "have a valid form" in {
      val formElement: Elements = doc.select("form")

      formElement.attr("method") shouldBe "POST"
      formElement.attr("action") shouldBe controllers.routes.AmendsRemovePensionSharingOrderController
        .submitRemovePso(Strings.ProtectionTypeUrl.IndividualProtection2016, "open")
        .url
    }

    "have a functional cancellation link" in {
      doc.select("#cancel-link").text shouldBe plaPsoDetailsCancelRemove
      doc.select("#cancel-link").attr("href") shouldBe plaAmendsPsoDetailsCancellationLink
    }

    "have a remove button" in {
      doc.select(".govuk-button").text shouldBe plaBaseRemove
      doc.select(".govuk-button").attr("id") shouldBe "submit"
    }
  }

}
