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

package views.pages.result

import models.PrintDisplayModel
import org.jsoup.Jsoup
import play.api.i18n.Messages
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.result.ResultPrintViewAmendment
import views.html.pages.result.resultPrintViewAmendment

class ResultPrintViewAmendmentSpec extends CommonViewSpecHelper with ResultPrintViewAmendment {

  "The Print Result Page" should {
    lazy val model = PrintDisplayModel(
      "Jim",
      "Davis",
      "nino",
      "IP2016",
      "dormant",
      "PSA33456789",
      Messages("pla.protection.protectionReference"),
      Some("100.00"),
      Some("23/02/2015"),
      12
    )
    lazy val resultPrintView = fakeApplication().injector.instanceOf[resultPrintViewAmendment]
    lazy val view            = resultPrintView(model)
    lazy val doc             = Jsoup.parse(view.body)

    lazy val model2 = PrintDisplayModel(
      "Jim",
      "Davis",
      "nino",
      "IP2014",
      "withdrawn",
      "PSA33456789",
      Messages("pla.protection.protectionReference"),
      Some("100.00"),
      Some("23/02/2015"),
      6
    )

    lazy val view2 = resultPrintView(model2)
    lazy val doc2  = Jsoup.parse(view2.body)

    lazy val model3 = PrintDisplayModel(
      "Jim",
      "Davis",
      "nino",
      "IP2014",
      "active",
      "PSA33456789",
      Messages("pla.protection.protectionReference"),
      Some("100.00"),
      Some("23/02/2015"),
      5
    )

    lazy val view3 = resultPrintView(model3)
    lazy val doc3  = Jsoup.parse(view3.body)

    "have the new correct title" in {
      doc.title() shouldBe plaPrintTitle
    }

    "have a new service name which" should {

      lazy val serviceName = doc.getElementsByClass("govuk-header__service-name")

      "contain the text" in {
        serviceName.text shouldBe plaPrintServiceName
      }
    }

    "have a first heading which" should {

      lazy val h1Tag = doc.select("h1")
      lazy val p1    = doc.select("p").get(0)

      "contain the text" in {
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

      "contain the text" in {
        doc.select("h2").first().text shouldBe plaPrintProtectionDetails
      }
    }

    "contain a table which" should {

      lazy val tableHeading = doc.select("tr th")

      "contain the following title message information" in {
        tableHeading.get(0).text shouldBe plaPrintApplicationDate
        tableHeading.get(1).text shouldBe plaPrintProtectionType
        tableHeading.get(2).text shouldBe plaPrintPla
        tableHeading.get(3).text shouldBe status
      }

      lazy val tableData = doc.select("tr td")

      "contain the following id information" in {
        tableData.get(0).attr("id") shouldBe "applicationDate"
        tableData.get(1).attr("id") shouldBe "protectionType"
        tableData.get(2).attr("id") shouldBe "protectedAmount"
        tableData.get(3).attr("id") shouldBe "dormantStatus"
      }

      "contain the following table information" in {
        tableData.get(0).text shouldBe "23/02/2015"
        tableData.get(1).text shouldBe "Individual protection 2016"
        tableData.get(2).text shouldBe "100.00"
        tableData.get(3).text shouldBe dormantStatus
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
        p3.text shouldBe plaPrintIP2014ContactHMRC
      }
    }

    "have an sections paragraph (Other Protection) which" should {

      lazy val p2 = doc2.select("div p").get(1)

      "have a give to pension provider section with the text" in {
        p2.text shouldBe plaPrintGiveToPensionProvider
      }

    }

    "have the new correct title for model2" in {
      doc2.title() shouldBe plaPrintTitle
    }

    "have a new service name which for model2" should {

      lazy val serviceName2 = doc2.getElementsByClass("govuk-header__service-name")

      "contain the text for model2" in {
        serviceName2.text shouldBe plaPrintServiceName
      }
    }

    "have a first heading which for model2" should {

      lazy val h1TagDoc2 = doc2.select("h1")
      lazy val p1Doc2    = doc2.select("p").get(0)

      "contain the text for model2" in {
        h1TagDoc2.text shouldBe "Jim Davis"
      }

      "have the id for model2" in {
        h1TagDoc2.attr("id") shouldBe "userName"
      }

      "also contain a paragraph with the id for model2" in {
        p1Doc2.attr("id") shouldBe "userNino"
      }

      "contain the paragraph text for model2" in {
        p1Doc2.text shouldBe s"$plaPrintNino nino"
      }
    }

    "have a second heading which for model2" should {

      "contain the text for model2" in {
        doc2.select("h2").first().text shouldBe plaPrintProtectionDetails
      }
    }

    "contain a table for model2 which" should {

      lazy val tableHeading = doc2.select("tr th")

      "contain the following title message information for model2" in {
        tableHeading.get(0).text shouldBe plaPrintApplicationDate
        tableHeading.get(1).text shouldBe plaPrintProtectionType
        tableHeading.get(2).text shouldBe plaPrintPla
        tableHeading.get(3).text shouldBe status
      }

      lazy val tableData = doc2.select("tr td")

      "contain the following id information for model2" in {
        tableData.get(0).attr("id") shouldBe "applicationDate"
        tableData.get(1).attr("id") shouldBe "protectionType"
        tableData.get(2).attr("id") shouldBe "protectedAmount"
        tableData.get(3).attr("id") shouldBe "withdrawnStatus"
      }

      "contain the following table information for model2" in {
        tableData.get(0).text shouldBe "23/02/2015"
        tableData.get(1).text shouldBe "Individual protection 2014"
        tableData.get(2).text shouldBe "100.00"
        tableData.get(3).text shouldBe withdrawnStatus
      }
    }

    "have the new correct title for model3" in {
      doc3.title() shouldBe plaPrintTitle
    }

    "have a new service name which for model3" should {

      lazy val serviceName3 = doc3.getElementsByClass("govuk-header__service-name")

      "contain the text for model2" in {
        serviceName3.text shouldBe plaPrintServiceName
      }
    }

    "have a first heading which for model3" should {

      lazy val h1TagDoc3 = doc3.select("h1")
      lazy val p1Doc3    = doc3.select("p").get(0)

      "contain the text for model3" in {
        h1TagDoc3.text shouldBe "Jim Davis"
      }

      "have the id for model3" in {
        h1TagDoc3.attr("id") shouldBe "userName"
      }

      "also contain a paragraph with the id for model3" in {
        p1Doc3.attr("id") shouldBe "userNino"
      }

      "contain the paragraph text for model3" in {
        p1Doc3.text shouldBe s"$plaPrintNino nino"
      }
    }

    "have a second heading which for model3" should {

      "contain the text for model3" in {
        doc3.select("h2").first().text shouldBe plaPrintProtectionDetails
      }
    }

    "contain a table for model3 which" should {

      lazy val tableHeading = doc3.select("tr th")

      "contain the following title message information for model3" in {
        tableHeading.get(0).text shouldBe plaPrintApplicationDate
        tableHeading.get(1).text shouldBe plaPrintProtectionType
        tableHeading.get(2).text shouldBe plaPrintPla
        tableHeading.get(3).text shouldBe plaPrintProtectionNotificationNumber
        tableHeading.get(4).text shouldBe plaPrintSchemeAdministratorReference
      }

      lazy val tableData = doc3.select("tr td")

      "contain the following id information for model3" in {
        tableData.get(0).attr("id") shouldBe "applicationDate"
        tableData.get(1).attr("id") shouldBe "protectionType"
        tableData.get(2).attr("id") shouldBe "protectedAmount"
        tableData.get(3).attr("id") shouldBe "protectionRefNum"
        tableData.get(4).attr("id") shouldBe "psaCheckRef"
      }

      "contain the following table information for model3" in {
        tableData.get(0).text shouldBe "23/02/2015"
        tableData.get(1).text shouldBe "Individual protection 2014"
        tableData.get(2).text shouldBe "100.00"
        tableData.get(3).text shouldBe Messages("pla.protection.protectionReference")
        tableData.get(4).text shouldBe "PSA33456789"
      }
    }
  }

}
