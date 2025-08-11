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

import org.jsoup.Jsoup
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testdata.AmendProtectionOutcomeViewsTestData.printDisplayModelIP14
import views.html.pages.result.resultPrintViewAmendment

class ResultPrintViewAmendmentSpec extends CommonViewSpecHelper {

  private val resultPrintViewAmendment = fakeApplication().injector.instanceOf[resultPrintViewAmendment]

  private val printDisplayModel = printDisplayModelIP14

  private val titleText             = "Print your active protection - Check your pension protections - GOV.UK"
  private val serviceNameText       = "Check your pension protections"
  private val protectionDetailsText = "Protection details"
  private val applicationDateText   = "Application date"
  private val protectedAmountText   = "Protected amount"

  private val giveToPensionProviderText =
    "Give these details to your pension provider when you decide to take money from your pension"

  private val iP2014ContactHmrcText =
    "If your pension gets shared in a divorce or civil partnership split, contact HMRC Pension Schemes Services within 60 days."

  "resultPrintViewAmendment" when
    (1 to 14).foreach { notificationId =>
      s"provided with PrintDisplayModel containing notificationId: $notificationId" should {

        val model = printDisplayModel.copy(notificationId = notificationId)
        val doc   = Jsoup.parse(resultPrintViewAmendment(model).body)

        "contain title" in {
          doc.title() shouldBe titleText
        }

        "contain service name" in {
          doc.getElementsByClass("govuk-header__service-name").text shouldBe serviceNameText
        }

        "contain second heading" in {
          doc.select("h2").first().text shouldBe protectionDetailsText
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
          val paragraph = doc.select("p").get(0)

          paragraph.text shouldBe giveToPensionProviderText
        }

        "contain paragraph with 'Contact HMRC' text" in {
          val paragraph = doc.select("p").get(1)

          paragraph.attr("id") shouldBe "contactHMRC"
          paragraph.text shouldBe iP2014ContactHmrcText
        }
      }
    }

}
