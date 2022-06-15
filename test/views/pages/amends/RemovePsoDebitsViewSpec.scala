/*
 * Copyright 2022 HM Revenue & Customs
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

import forms.AmendmentTypeForm
import org.jsoup.Jsoup
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.ip2016.RemovePsoDetailsViewMessages
import uk.gov.hmrc.play.views.html.helpers.{ErrorSummary, FormWithCSRF}
import views.html.pages.amends.removePsoDebits

class RemovePsoDebitsViewSpec extends CommonViewSpecHelper with RemovePsoDetailsViewMessages{

  implicit val errorSummary: ErrorSummary = app.injector.instanceOf[ErrorSummary]
  implicit val formWithCSRF: FormWithCSRF = app.injector.instanceOf[FormWithCSRF]

  "the RemovePsoDetailsView" should{
    val amendmentForm = AmendmentTypeForm.amendmentTypeForm.bind(Map(
      "protectionType" -> "ip2016",
      "status"         -> "open"))

    lazy val view = application.injector.instanceOf[removePsoDebits]
    lazy val doc = Jsoup.parse(view.apply(amendmentForm).body)
    lazy val form = doc.select("form")

    "have the correct title" in{
      doc.title() shouldBe plaPsoDetailsTitle
    }

    "have the right explanatory paragraph" in{
      doc.select("p").eq(0).text shouldBe plaPsoDetailsRemovePso
    }

    "have the correct and properly formatted header"in{
      doc.select("h1").text shouldBe plaPsoDetailsTitle
    }

    "have a valid form" in{
      form.attr("method") shouldBe "POST"
      form.attr("action") shouldBe controllers.routes.AmendsController.submitRemovePso.url
    }

    "have a functional cancellation link" in{
      doc.select("a").text shouldBe plaPsoDetailsCancelRemove
      doc.select("a").attr("href") shouldBe plaAmendsPsoDetailsCancellationLink
    }

    "have a remove button" in{
      doc.select("button").text shouldBe plaBaseRemove
      doc.select("button").attr("type") shouldBe "submit"
    }
  }
}
