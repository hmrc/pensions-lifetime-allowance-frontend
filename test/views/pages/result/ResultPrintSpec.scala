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

package views.pages.result

import models.PrintDisplayModel
import org.jsoup.Jsoup
import play.api.i18n.Messages
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.result.ResultPrint
import views.html.pages.result.resultPrint

class ResultPrintSpec extends CommonViewSpecHelper with ResultPrint {

  "The Print Result Page" should {

    lazy val model = PrintDisplayModel("Jim", "Davis", "nino", "IP2016", "active", "PSA33456789", Messages("pla.protection.protectionReference"), Some("100.00"), Some("23/02/2015"))
    lazy val resultPrintView = fakeApplication().injector.instanceOf[resultPrint]
    lazy val view = resultPrintView(model)
    lazy val doc = Jsoup.parse(view.body)
    lazy val model2 = PrintDisplayModel("Jim", "Davis", "nino", "FP2014", "dormant", "PSA33456789", Messages("pla.protection.protectionReference"), Some("100.00"), Some("23/02/2015"))
    lazy val view2 = resultPrintView(model2)
    lazy val doc2 = Jsoup.parse(view2.body)

    "have the correct title" in {
      doc.title() shouldBe plaPrintTitle
    }

    "have a service name which" should {

      lazy val serviceName = doc.getElementsByClass("govuk-header__link govuk-header__link--service-name")

      "contain the text" in {
        serviceName.text shouldBe plaPrintServiceName
      }
  }

    "have a second heading which" should {

      lazy val h1Tag = doc.select("h1")
      lazy val p1 = doc.select("p").get(0)

      s"contain the text" in {
        h1Tag.text shouldBe "Jim Davis"
      }

      "have the id" in {
        h1Tag.attr("id") shouldBe "userName"
      }

      "also contain a paragraph with the id" in {
        p1.attr("id") shouldBe "userNino"
      }

      "contain the paragraph text" in {
        p1.text shouldBe s"$plaPrintNino nino"
      }
    }

    "have a second heading which" should {

      "contains the text" in {
        doc.select("h2").text shouldBe plaPrintProtectionDetails
      }
    }

    "contain a table which" should {

      lazy val tableHeading = doc.select("tr th")

      "contain the following title message information" in {
        tableHeading.get(0).text shouldBe plaPrintApplicationDate
        tableHeading.get(1).text shouldBe plaPrintProtectionType
        tableHeading.get(2).text shouldBe plaPrintPla
        tableHeading.get(3).text shouldBe plaPrintProtectionNotificationNumber
        tableHeading.get(4).text shouldBe plaPrintSchemeAdministratorReference
      }

      lazy val tableData = doc.select("tr td")

      "contain the following id information" in {
        tableData.get(0).attr("id") shouldBe "applicationDate"
        tableData.get(1).attr("id") shouldBe "protectionType"
        tableData.get(2).attr("id") shouldBe "protectedAmount"
        tableData.get(3).attr("id") shouldBe "protectionRef"
        tableData.get(4).attr("id") shouldBe "psaRef"
      }

      "contain the following table information" in {
        tableData.get(0).text shouldBe "23/02/2015"
        tableData.get(1).text shouldBe "Individual protection 2016"
        tableData.get(2).text shouldBe "100.00"
        tableData.get(3).text shouldBe Messages("pla.protection.protectionReference")
        tableData.get(4).text shouldBe "PSA33456789"
      }
    }

    "have an sections paragraph which" should {

      lazy val p2 = doc.select("div p").get(1)
      lazy val p3 = doc.select("div p").get(2)

      "have a give to pension provider section with the text" in {
        p2.text shouldBe plaPrintGiveToPensionProvider
      }

      "have a contact hmrc section with the id" in {
        p3.attr("id") shouldBe "contactHMRC"
      }

      "have a contact hmrc section with the text" in {
        p3.text shouldBe plaPrintIP2016ContactHMRC
      }
    }

    "have an sections paragraph (Other Protection) which" should {

      lazy val p2 = doc2.select("div p").get(1)
      lazy val p3 = doc2.select("div p").get(2)

      "have a give to pension provider section with the text" in {
        p2.text shouldBe plaPrintGiveToPensionProvider
      }

    }
  }
}