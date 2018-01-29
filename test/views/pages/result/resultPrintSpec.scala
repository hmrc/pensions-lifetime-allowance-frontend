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

package views.pages.result

import play.api.i18n.Messages.Implicits._
import org.jsoup.Jsoup
import testHelpers.ViewSpecHelpers.{CommonMessages, CommonViewSpecHelper}
import testHelpers.ViewSpecHelpers.result.resultPrint
import views.html.pages.result.{resultPrint => views}
import models.PrintDisplayModel
import play.api.i18n.Messages

class resultPrintSpec extends CommonViewSpecHelper with resultPrint with CommonMessages {

  "The Print Result Page" should {

    implicit lazy val model = PrintDisplayModel("Jim", "Davis", "nino", "IP2016", "active", "PSA33456789", Messages("pla.protection.protectionReference"), Some("100.00"), Some("23/02/2015"))
    implicit lazy val view = views(model)
    implicit lazy val doc = Jsoup.parse(view.body)

    "have the correct title" in {
      doc.title() shouldBe plaPrintTitle
    }

    "have a primary heading which" should {

      lazy val p0 = doc.select("p").get(0)

      "be the class" in {
        p0.hasClass("header") shouldBe true
      }

      "contain the text" in {
        p0.text shouldBe plaPrintHmrc
      }
    }

    "have a second heading which" should {

      lazy val h1Tag = doc.select("h1")
      lazy val p1 = doc.select("p").get(1)

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

      lazy val tableHeading = doc.select("tr td")

      "be of the class" in {
        doc.select("table").hasClass("table") shouldBe true
      }

      "contain the following title message information" in {
        tableHeading.get(0).text shouldBe plaPrintApplicationDate
        tableHeading.get(2).text shouldBe plaPrintProtectionType
        tableHeading.get(4).text shouldBe plaPrintPla
        tableHeading.get(6).text shouldBe plaPrintProtectionNotificationNumber
        tableHeading.get(8).text shouldBe plaPrintSchemeAdministratorReference
      }

      "contain the following id information" in {
        tableHeading.get(1).attr("id") shouldBe "applicationDate"
        tableHeading.get(3).attr("id") shouldBe "protectionType"
        tableHeading.get(5).attr("id") shouldBe "protectedAmount"
        tableHeading.get(7).attr("id") shouldBe "protectionRef"
        tableHeading.get(9).attr("id") shouldBe "psaRef"
      }

      "contain the following table information" in {
        tableHeading.get(1).text shouldBe "23/02/2015"
        tableHeading.get(3).text shouldBe "Individual protection 2016"
        tableHeading.get(5).text shouldBe "100.00"
        tableHeading.get(7).text shouldBe Messages("pla.protection.protectionReference")
        tableHeading.get(9).text shouldBe "PSA33456789"
      }
    }

    "have an sections paragraph which" should {

      lazy val p2 = doc.select("div p").get(1)
      lazy val p3 = doc.select("div p").get(2)

      "have a give to pension provider section with the class" in {
        p2.hasClass("faint") shouldBe true
      }

      "have a give to pension provider section with the text" in {
        p2.text shouldBe plaPrintGiveToPensionProvider
      }

      "have a contact hmrc section with the class" in {
        p3.hasClass("faint") shouldBe true
      }

      "have a contact hmrc section with the id" in {
        p3.attr("id") shouldBe "contactHMRC"
      }

      "have a contact hmrc section with the text" in {
        p3.text shouldBe plaPrintIP2016ContactHMRC
      }
    }
  }
}