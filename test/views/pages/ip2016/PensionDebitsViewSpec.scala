/*
 * Copyright 2021 HM Revenue & Customs
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

import forms.PensionDebitsForm
import org.jsoup.Jsoup
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.ip2016.PensionDebitsViewMessages
import views.html.pages.ip2016.pensionDebits
import scala.concurrent.ExecutionContext.Implicits.global

class PensionDebitsViewSpec extends CommonViewSpecHelper with PensionDebitsViewMessages {

  "the PensionDebitsView" should{
    val pensionsForm = PensionDebitsForm.pensionDebitsForm.bind(Map("pensionDebits" -> "Yes"))
    lazy val view = application.injector.instanceOf[pensionDebits]
    lazy val doc = Jsoup.parse(view.apply(pensionsForm).body)

    val errorForm = PensionDebitsForm.pensionDebitsForm.bind(Map.empty[String, String])
    lazy val errorView = application.injector.instanceOf[pensionDebits]
    lazy val errorDoc = Jsoup.parse(errorView.apply(errorForm).body)

    lazy val form = doc.select("form")

    "have the correct title" in{
      doc.title() shouldBe plaPensionDebitsTitle
    }

    "have the correct and properly formatted header"in{
      doc.select("h1").text shouldBe plaPensionDebitsTitle
    }

    "have some introductory text" in{
      doc.select("p").first().text shouldBe plaPensionDebitsParaOne
    }

    "have a pair of yes/no buttons" in{
      doc.select("[for=pensionDebits-yes]").text shouldBe plaBaseYes
      doc.select("input#pensionDebits-yes").attr("type") shouldBe "radio"
      doc.select("[for=pensionDebits-no]").text shouldBe plaBaseNo
      doc.select("input#pensionDebits-no").attr("type") shouldBe "radio"
    }

    "have a valid form" in{
      form.attr("method") shouldBe "POST"
      form.attr("action") shouldBe controllers.routes.IP2016Controller.submitPensionDebits().url
      form.select("legend.visually-hidden").text() shouldBe plaPensionsDebitLegendText
    }
    "have a continue button" in{
      doc.select("button").text shouldBe plaBaseContinue
      doc.select("button").attr("type") shouldBe "submit"
    }

    "display the correct errors appropriately" in{
      errorForm.hasErrors shouldBe true
      errorDoc.select("h2.h3-heading").text shouldBe plaBaseErrorSummaryLabel
      errorDoc.select("span.error-notification").text shouldBe plaBaseErrorsMandatoryError
    }

    "not have errors on valid pages" in{
      pensionsForm.hasErrors shouldBe false
      doc.select("span.error-notification").text shouldBe ""
    }
  }
}
