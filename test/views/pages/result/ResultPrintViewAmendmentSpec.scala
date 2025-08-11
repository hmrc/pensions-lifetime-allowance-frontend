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
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import views.html.pages.result.resultPrintViewAmendment
import views.pages.models.ViewConstants

class ResultPrintViewAmendmentSpec extends CommonViewSpecHelper {

  private val resultPrintViewAmendment = fakeApplication().injector.instanceOf[resultPrintViewAmendment]

  private val printDisplayModel = PrintDisplayModel(
    "Jim",
    "Davis",
    "nino",
    "IP2016",
    "dormant",
    "PSA33456789",
    "IP14XXXXXX",
    Some("100.00"),
    Some("23/02/2015"),
    15
  )

  private val titleText                     = "Print your active protection - Check your pension protections - GOV.UK"
  private val serviceNameText               = "Check your pension protections"
  private val ninoText                      = "National Insurance number"
  private val protectionDetailsText         = "Protection details"
  private val applicationDateText           = "Application date"
  private val protectionTypeText            = "Protection type"
  private val protectedAmountText           = "Protected amount"
  private val protectionReferenceNumberText = "Protection reference number"
  private val fixedProtectionReferenceNumberText      = "Fixed protection 2016 reference number"
  private val pensionSchemeAdministratorReferenceText = "Pension Scheme administrator check reference"
  private val statusText                              = "Status"
  private val dormantStatusText                       = "Dormant"
  private val withdrawnStatusText                     = "Withdrawn"

  private val giveToPensionProviderText =
    "Give these details to your pension provider when you decide to take money from your pension"

  private val iP2014ContactHmrcText =
    "If your pension gets shared in a divorce or civil partnership split, contact HMRC Pension Schemes Services within 60 days."

  "resultPrintViewAmendment" when {

    (1 to 15).foreach { notificationId =>
      s"provided with PrintDisplayModel containing notificationId: $notificationId" should {

        val model = printDisplayModel.copy(notificationId = notificationId)
        val doc   = Jsoup.parse(resultPrintViewAmendment(model).body)

        "contain title" in {
          doc.title() shouldBe titleText
        }

        "contain service name" in {
          doc.getElementsByClass("govuk-header__service-name").text shouldBe serviceNameText
        }

        "contain first heading with paragraph" in {
          val header    = doc.select("h1")
          val paragraph = doc.select("p").get(0)

          header.attr("id") shouldBe "userName"
          header.text shouldBe "Jim Davis"

          paragraph.attr("id") shouldBe "userNino"
          paragraph.text shouldBe s"$ninoText nino"
        }

        "contain second heading" in {
          doc.select("h2").first().text shouldBe protectionDetailsText
        }

        "contain table row with certificate date" in {
          doc.getElementsByClass("govuk-table__header").get(0).text shouldBe applicationDateText
          doc.getElementsByClass("govuk-table__cell").get(0).text shouldBe "23/02/2015"
        }

        "contain table row with protection type" in {
          doc.getElementsByClass("govuk-table__header").get(1).text shouldBe protectionTypeText
          doc.getElementsByClass("govuk-table__cell").get(1).text shouldBe "Individual protection 2016"
        }

        "contain table row with protected amount" in {
          doc.getElementsByClass("govuk-table__header").get(2).text shouldBe protectedAmountText
          doc.getElementsByClass("govuk-table__cell").get(2).text shouldBe "100.00"
        }

        "NOT contain table row with certificate date if certificateDate is empty" in {
          val model = printDisplayModel.copy(notificationId = notificationId, certificateDate = None)
          val doc   = Jsoup.parse(resultPrintViewAmendment(model).body)

          doc.getElementsByClass("govuk-table__header").text shouldNot include(applicationDateText)
          doc.getElementsByClass("govuk-table__cell").text shouldNot include("23/02/2015")
        }

        "NOT contain table row with protected amount if protectedAmount is empty" in {
          val model = printDisplayModel.copy(notificationId = notificationId, protectedAmount = None)
          val doc   = Jsoup.parse(resultPrintViewAmendment(model).body)

          doc.getElementsByClass("govuk-table__header").text shouldNot include(protectedAmountText)
          doc.getElementsByClass("govuk-table__cell").text shouldNot include("100.00")
        }

        "contain paragraph with 'Give these details to pension provider' text" in {
          val paragraph = doc.select("div p").get(1)

          paragraph.text shouldBe giveToPensionProviderText
        }

        "contain paragraph with 'Contact HMRC' text" in {
          val paragraph = doc.select("div p").get(2)

          paragraph.attr("id") shouldBe "contactHMRC"
          paragraph.text shouldBe iP2014ContactHmrcText
        }
      }
    }

    Seq(1, 5, 8).foreach { notificationId =>
      s"provided with PrintDisplayModel containing notificationId: $notificationId" should {

        val model = printDisplayModel.copy(notificationId = notificationId)
        val doc   = Jsoup.parse(resultPrintViewAmendment(model).body)

        "contain table row with protection reference number" in {
          doc.getElementsByClass("govuk-table__header").get(3).text shouldBe protectionReferenceNumberText
          doc.getElementsByClass("govuk-table__cell").get(3).text shouldBe "IP14XXXXXX"
        }

        "contain table row with pension scheme administrator reference" in {
          doc.getElementsByClass("govuk-table__header").get(4).text shouldBe pensionSchemeAdministratorReferenceText
          doc.getElementsByClass("govuk-table__cell").get(4).text shouldBe "PSA33456789"
        }
      }
    }

    Seq(7, 14).foreach { notificationId =>
      s"provided with PrintDisplayModel containing notificationId: $notificationId" should {
        "contain table row with fixed protection reference number" in {
          val model = printDisplayModel.copy(notificationId = notificationId)
          val doc   = Jsoup.parse(resultPrintViewAmendment(model).body)

          doc.getElementsByClass("govuk-table__header").get(3).text shouldBe fixedProtectionReferenceNumberText
          doc.getElementsByClass("govuk-table__cell").get(3).text shouldBe "IP14XXXXXX"
        }
      }
    }

    ViewConstants.dormantNotificationIds.foreach { notificationId =>
      s"provided with PrintDisplayModel containing notificationId: $notificationId" should {
        "contain table row with status: Dormant" in {
          val model = printDisplayModel.copy(notificationId = notificationId)
          val doc   = Jsoup.parse(resultPrintViewAmendment(model).body)

          doc.getElementsByClass("govuk-table__header").get(3).text shouldBe statusText
          doc.getElementsByClass("govuk-table__cell").get(3).text shouldBe dormantStatusText
        }
      }
    }

    ViewConstants.withdrawnNotificationIds.foreach { notificationId =>
      s"provided with PrintDisplayModel containing notificationId: $notificationId" should {
        "contain table row with status: Withdrawn" in {
          val model = printDisplayModel.copy(notificationId = notificationId)
          val doc   = Jsoup.parse(resultPrintViewAmendment(model).body)

          doc.getElementsByClass("govuk-table__header").get(3).text shouldBe statusText
          doc.getElementsByClass("govuk-table__cell").get(3).text shouldBe withdrawnStatusText
        }
      }
    }
  }

}
