/*
 * Copyright 2020 HM Revenue & Customs
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

import forms.PensionsTakenForm
import org.jsoup.Jsoup
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.ip2016.PensionsTakenViewMessages
import views.html.pages.ip2016.{pensionsTaken => views}

class PensionsTakenViewSpec extends CommonViewSpecHelper with PensionsTakenViewMessages {

  "the PensionsTakenView" should{
    val pensionsForm = PensionsTakenForm.pensionsTakenForm.bind(Map("pensionsTaken" -> "Yes"))
    lazy val view = views(pensionsForm)
    lazy val doc = Jsoup.parse(view.body)

    val errorForm =  PensionsTakenForm.pensionsTakenForm.bind(Map.empty[String, String])
    lazy val errorView = views(errorForm)
    lazy val errorDoc = Jsoup.parse(errorView.body)

    lazy val form = doc.select("form")
    "have the correct title" in{
      doc.title() shouldBe plaPensionsTakenTitle
    }

    "have the correct and properly formatted header"in{
      doc.select("h1").text shouldBe plaPensionsTakenTitle
    }

    "have a valid form" in{
      form.attr("method") shouldBe "POST"
      form.attr("action") shouldBe controllers.routes.IP2016Controller.submitPensionsTaken().url
      form.select("legend.visually-hidden").text() shouldBe plaPensionsTakenLegendText

    }

    "have some introductory text" in{
      doc.select("li").eq(0).text shouldBe plaPensionsTakenBulletOne
      doc.select("li").eq(1).text shouldBe plaPensionsTakenBulletTwo
      doc.select("li").eq(2).text shouldBe plaPensionsTakenBulletThree
    }

    "have a pair of yes/no buttons" in{
      doc.select("[for=pensionsTaken-yes]").text shouldBe plaBaseYes
      doc.select("input#pensionsTaken-yes").attr("type") shouldBe "radio"
      doc.select("[for=pensionsTaken-no]").text shouldBe plaBaseNo
      doc.select("input#pensionsTaken-no").attr("type") shouldBe "radio"
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
