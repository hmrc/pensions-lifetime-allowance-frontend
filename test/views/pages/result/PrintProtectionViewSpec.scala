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

import models.display.PrintDisplayModel
import models.pla.response.ProtectionType
import models.pla.response.ProtectionType.{FixedProtection2016, FixedProtection2016LTA}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import testHelpers.CommonViewSpecHelper
import testHelpers.messages.result.ResultPrintPageContentMessages
import views.html.pages.result.printProtection

class PrintProtectionViewSpec extends CommonViewSpecHelper with ResultPrintPageContentMessages {

  val view: printProtection = app.injector.instanceOf[printProtection]

  def doc(printDisplayModel: PrintDisplayModel = model): Document = Jsoup.parse(view(printDisplayModel).body)

  val nino = "AB123456A"

  val model = PrintDisplayModel(
    firstName = "Jim",
    surname = "Davis",
    nino = nino,
    protectionType = "IP2016",
    status = "active",
    psaCheckReference = "PSA33456789",
    protectionReference = "IP123456789001",
    protectedAmount = Some("1,200,000"),
    certificateDate = Some("23/02/2015"),
    certificateTime = Some("12:34:56"),
    lumpSumPercentage = Some("42%"),
    lumpSumAmount = Some("1,000"),
    enhancementFactor = Some("0.17"),
    factor = Some("0.42")
  )

  "The Print Result Page" should {
    "have correct title" in {
      doc().title() shouldBe plaPrintTitle
    }

    "have correct service name" in {
      val serviceName = doc().getElementsByClass("govuk-header__service-name")
      serviceName.text shouldBe plaPrintServiceName
    }

    "have a first heading".which {

      val h1Tag = doc().select("h1")
      val p1    = doc().select("p").get(0)

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
        p1.text shouldBe s"$plaPrintNino $nino"
      }
    }

    "have a second heading".which {

      "contain the text" in {
        doc().select("h2").first().text shouldBe plaPrintProtectionDetails
      }
    }

    "contain a table".which {

      "contains the following title message information" in {
        val tableHeading = doc().select("tr th")

        tableHeading.get(0).text shouldBe plaPrintProtectionType
        tableHeading.get(1).text shouldBe plaPrintProtectedAmount
        tableHeading.get(2).text shouldBe plaPrintProtectionNotificationNumber
        tableHeading.get(3).text shouldBe plaPrintSchemeAdministratorReference
        tableHeading.get(4).text shouldBe plaPrintLumpSumPercentage
        tableHeading.get(5).text shouldBe plaPrintLumpSumAmount
        tableHeading.get(6).text shouldBe plaPrintEnhancementFactor
        tableHeading.get(7).text shouldBe plaPrintFactor
        tableHeading.get(8).text shouldBe plaPrintApplicationDate
        tableHeading.get(9).text shouldBe plaPrintApplicationTime
      }

      "contains correct IDs" in {
        val tableData = doc().select("tr td")

        tableData.get(0).attr("id") shouldBe "protectionType"
        tableData.get(1).attr("id") shouldBe "protectedAmount"
        tableData.get(2).attr("id") shouldBe "protectionRef"
        tableData.get(3).attr("id") shouldBe "psaRef"

        tableData.get(8).attr("id") shouldBe "applicationDate"
        tableData.get(9).attr("id") shouldBe "applicationTime"
      }

      "contains correct table data" when {

        "provided with all optional fields" in {
          val tableData = doc().select("tr td")

          tableData.get(0).text shouldBe "Individual protection 2016"
          tableData.get(1).text shouldBe model.protectedAmount.get
          tableData.get(2).text shouldBe model.protectionReference
          tableData.get(3).text shouldBe model.psaCheckReference
          tableData.get(4).text shouldBe model.lumpSumPercentage.get
          tableData.get(5).text shouldBe model.lumpSumAmount.get
          tableData.get(6).text shouldBe model.enhancementFactor.get
          tableData.get(7).text shouldBe model.factor.get
          tableData.get(8).text shouldBe model.certificateDate.get
          tableData.get(9).text shouldBe model.certificateTime.get
        }

        "provided with only mandatory fields" in {
          val newModel = model.copy(
            certificateTime = None,
            lumpSumPercentage = None,
            lumpSumAmount = None,
            enhancementFactor = None,
            factor = None
          )
          val tableData = doc(newModel).select("tr td")

          tableData.get(0).text shouldBe "Individual protection 2016"
          tableData.get(1).text shouldBe model.protectedAmount.get
          tableData.get(2).text shouldBe model.protectionReference
          tableData.get(3).text shouldBe model.psaCheckReference
          tableData.get(4).text shouldBe model.certificateDate.get
        }
      }
    }

    "have a 'give to pension provider' section with correct ID" in {
      val p2 = doc().select("div p").get(1)

      p2.text shouldBe plaPrintGiveToPensionProvider
    }

    "have a contact hmrc section" when {

      ProtectionType.values.diff(Seq(FixedProtection2016, FixedProtection2016LTA)).map(_.toString).foreach {
        protectionType =>
          s"provided with protection type: $protectionType" in {
            val p3 = {
              val newModel = model.copy(protectionType = protectionType)
              val newView  = view(newModel)
              val newDoc   = Jsoup.parse(newView.body)
              newDoc.select("div p").get(2)
            }

            p3.text shouldBe plaPrintContactHMRC
          }
      }

      Seq("FP2016", FixedProtection2016.toString, FixedProtection2016LTA.toString).foreach { protectionType =>
        s"provided with protection type: $protectionType" in {
          val p3 = {
            val newModel = model.copy(protectionType = protectionType)
            val newView  = view(newModel)
            val newDoc   = Jsoup.parse(newView.body)
            newDoc.select("div p").get(2)
          }

          p3.text shouldBe plaPrintFP2016ContactHMRC
        }
      }
    }

    "not have back link with text back " in {
      val backButton = doc().select(".govuk-back-link").text()
      backButton.isEmpty shouldBe true
    }

  }

}
