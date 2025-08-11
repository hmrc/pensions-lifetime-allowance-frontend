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

package views.playhelpers.templates

import models.PrintDisplayModel
import org.jsoup.Jsoup
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testdata.AmendProtectionOutcomeViewsTestData._
import views.html.playHelpers.templates.amendProtectionOutcomeTable

class AmendProtectionOutcomeTableSpec extends CommonViewSpecHelper {

  private val amendProtectionOutcomeTable = fakeApplication().injector.instanceOf[amendProtectionOutcomeTable]

  private val NameHeader               = "Full Name"
  private val NinoHeader               = "National Insurance number"
  private val ProtectedAmountHeader    = "Protected amount"
  private val ProtectionRefHeader      = "Protection reference number"
  private val FixedProtectionRefHeader = "Fixed protection 2016 reference number"
  private val ProtectionTypeHeader     = "Protection type"
  private val PsaRefHeader             = "Pension scheme administrator check reference"
  private val ApplicationDateHeader    = "Application date"
  private val StatusHeader             = "Status"
  private val DormantText              = "Dormant"
  private val WithdrawnText            = "Withdrawn"

  private def getPrintDisplayModelFor(notificationId: Int): PrintDisplayModel = {
    val iP14PrintDisplayModels =
      (1 to 7).map(notificationId => notificationId -> printDisplayModelIP14.copy(notificationId = notificationId))
    val iP16PrintDisplayModels =
      (8 to 14).map(notificationId => notificationId -> printDisplayModelIP16.copy(notificationId = notificationId))

    (iP14PrintDisplayModels ++ iP16PrintDisplayModels).toMap.apply(notificationId)
  }

  "amendProtectionOutcomeTable" when {

    Seq(1, 5).foreach { notificationId =>
      s"provided with PrintDisplayModel containing notificationId: $notificationId" when {

        val printDisplayModel = getPrintDisplayModelFor(notificationId)

        "includeProtectedAmount is set to false" should {

          val doc = Jsoup.parse(amendProtectionOutcomeTable(printDisplayModel).body)

          val tableHeadings = doc.select("tr th")
          val tableData     = doc.select("tr td")

          "contain row for name" in {
            val rowIndex = 0
            tableHeadings.get(rowIndex).text shouldBe NameHeader
            tableData.get(rowIndex).attr("id") shouldBe "yourFullName"
            tableData.get(rowIndex).text shouldBe "Jim Davis"
          }

          "contain row for National Insurance Number" in {
            val rowIndex = 1
            tableHeadings.get(rowIndex).text shouldBe NinoHeader
            tableData.get(rowIndex).attr("id") shouldBe "yourNino"
            tableData.get(rowIndex).text shouldBe printDisplayModel.nino
          }

          "contain row for Protection Type" in {
            val rowIndex = 2
            tableHeadings.get(rowIndex).text shouldBe ProtectionTypeHeader
            tableData.get(rowIndex).attr("id") shouldBe "protectionType"
            tableData.get(rowIndex).text shouldBe "Individual protection 2014"
          }

          "contain row for Protection Reference Number" in {
            val rowIndex = 3
            tableHeadings.get(rowIndex).text shouldBe ProtectionRefHeader
            tableData.get(rowIndex).attr("id") shouldBe "protectionRefNum"
            tableData.get(rowIndex).text shouldBe printDisplayModel.protectionReference
          }

          "contain row for PSA Check Reference" in {
            val rowIndex = 4
            tableHeadings.get(rowIndex).text shouldBe PsaRefHeader
            tableData.get(rowIndex).attr("id") shouldBe "psaCheckRef"
            tableData.get(rowIndex).text shouldBe printDisplayModel.psaCheckReference
          }

          "contain row for Application Date" in {
            val rowIndex = 5
            tableHeadings.get(rowIndex).text shouldBe ApplicationDateHeader
            tableData.get(rowIndex).attr("id") shouldBe "applicationDate"
            tableData.get(rowIndex).text shouldBe printDisplayModel.certificateDate.get
          }
        }

        "includeProtectedAmount is set to true" should {

          val doc = Jsoup.parse(amendProtectionOutcomeTable(printDisplayModel, includeProtectedAmount = true).body)

          val tableHeadings = doc.select("tr th")
          val tableData     = doc.select("tr td")

          "contain row for name" in {
            val rowIndex = 0
            tableHeadings.get(rowIndex).text shouldBe NameHeader
            tableData.get(rowIndex).attr("id") shouldBe "yourFullName"
            tableData.get(rowIndex).text shouldBe "Jim Davis"
          }

          "contain row for National Insurance Number" in {
            val rowIndex = 1
            tableHeadings.get(rowIndex).text shouldBe NinoHeader
            tableData.get(rowIndex).attr("id") shouldBe "yourNino"
            tableData.get(rowIndex).text shouldBe printDisplayModel.nino
          }

          "contain row for Protected Amount" in {
            val rowIndex = 2
            tableHeadings.get(rowIndex).text shouldBe ProtectedAmountHeader
            tableData.get(rowIndex).attr("id") shouldBe "protectedAmount"
            tableData.get(rowIndex).text shouldBe printDisplayModel.protectedAmount.get
          }

          "contain row for Protection Type" in {
            val rowIndex = 3
            tableHeadings.get(rowIndex).text shouldBe ProtectionTypeHeader
            tableData.get(rowIndex).attr("id") shouldBe "protectionType"
            tableData.get(rowIndex).text shouldBe "Individual protection 2014"
          }

          "contain row for Protection Reference Number" in {
            val rowIndex = 4
            tableHeadings.get(rowIndex).text shouldBe ProtectionRefHeader
            tableData.get(rowIndex).attr("id") shouldBe "protectionRefNum"
            tableData.get(rowIndex).text shouldBe printDisplayModel.protectionReference
          }

          "contain row for PSA Check Reference" in {
            val rowIndex = 5
            tableHeadings.get(rowIndex).text shouldBe PsaRefHeader
            tableData.get(rowIndex).attr("id") shouldBe "psaCheckRef"
            tableData.get(rowIndex).text shouldBe printDisplayModel.psaCheckReference
          }

          "contain row for Application Date" in {
            val rowIndex = 6
            tableHeadings.get(rowIndex).text shouldBe ApplicationDateHeader
            tableData.get(rowIndex).attr("id") shouldBe "applicationDate"
            tableData.get(rowIndex).text shouldBe printDisplayModel.certificateDate.get
          }
        }
      }
    }

    Seq(2, 3, 4).foreach { notificationId =>
      s"provided with PrintDisplayModel containing notificationId: $notificationId" should {

        val printDisplayModel = getPrintDisplayModelFor(notificationId)

        "includeProtectedAmount is set to false" should {

          val doc = Jsoup.parse(amendProtectionOutcomeTable(printDisplayModel).body)

          val tableHeadings = doc.select("tr th")
          val tableData     = doc.select("tr td")

          "contain row for name" in {
            val rowIndex = 0
            tableHeadings.get(rowIndex).text shouldBe NameHeader
            tableData.get(rowIndex).attr("id") shouldBe "yourFullName"
            tableData.get(rowIndex).text shouldBe "Jim Davis"
          }

          "contain row for National Insurance Number" in {
            val rowIndex = 1
            tableHeadings.get(rowIndex).text shouldBe NinoHeader
            tableData.get(rowIndex).attr("id") shouldBe "yourNino"
            tableData.get(rowIndex).text shouldBe printDisplayModel.nino
          }

          "contain row for Protection Type" in {
            val rowIndex = 2
            tableHeadings.get(rowIndex).text shouldBe ProtectionTypeHeader
            tableData.get(rowIndex).attr("id") shouldBe "protectionType"
            tableData.get(rowIndex).text shouldBe "Individual protection 2014"
          }

          "contain row for Application Date" in {
            val rowIndex = 3
            tableHeadings.get(rowIndex).text shouldBe ApplicationDateHeader
            tableData.get(rowIndex).attr("id") shouldBe "applicationDate"
            tableData.get(rowIndex).text shouldBe printDisplayModel.certificateDate.get
          }

          "contain row for Status" in {
            val rowIndex = 4
            tableHeadings.get(rowIndex).text shouldBe StatusHeader
            tableData.get(rowIndex).attr("id") shouldBe "status"
            tableData.get(rowIndex).text shouldBe DormantText
          }
        }

        "includeProtectedAmount is set to true" should {

          val doc = Jsoup.parse(amendProtectionOutcomeTable(printDisplayModel, includeProtectedAmount = true).body)

          val tableHeadings = doc.select("tr th")
          val tableData     = doc.select("tr td")

          "contain row for name" in {
            val rowIndex = 0
            tableHeadings.get(rowIndex).text shouldBe NameHeader
            tableData.get(rowIndex).attr("id") shouldBe "yourFullName"
            tableData.get(rowIndex).text shouldBe "Jim Davis"
          }

          "contain row for National Insurance Number" in {
            val rowIndex = 1
            tableHeadings.get(rowIndex).text shouldBe NinoHeader
            tableData.get(rowIndex).attr("id") shouldBe "yourNino"
            tableData.get(rowIndex).text shouldBe printDisplayModel.nino
          }

          "contain row for Protected Amount" in {
            val rowIndex = 2
            tableHeadings.get(rowIndex).text shouldBe ProtectedAmountHeader
            tableData.get(rowIndex).attr("id") shouldBe "protectedAmount"
            tableData.get(rowIndex).text shouldBe printDisplayModel.protectedAmount.get
          }

          "contain row for Protection Type" in {
            val rowIndex = 3
            tableHeadings.get(rowIndex).text shouldBe ProtectionTypeHeader
            tableData.get(rowIndex).attr("id") shouldBe "protectionType"
            tableData.get(rowIndex).text shouldBe "Individual protection 2014"
          }

          "contain row for Application Date" in {
            val rowIndex = 4
            tableHeadings.get(rowIndex).text shouldBe ApplicationDateHeader
            tableData.get(rowIndex).attr("id") shouldBe "applicationDate"
            tableData.get(rowIndex).text shouldBe printDisplayModel.certificateDate.get
          }

          "contain row for Status" in {
            val rowIndex = 5
            tableHeadings.get(rowIndex).text shouldBe StatusHeader
            tableData.get(rowIndex).attr("id") shouldBe "status"
            tableData.get(rowIndex).text shouldBe DormantText
          }
        }
      }
    }

    "provided with PrintDisplayModel containing notificationId: 6" should {

      val printDisplayModel = getPrintDisplayModelFor(6)

      "includeProtectedAmount is set to false" should {

        val doc = Jsoup.parse(amendProtectionOutcomeTable(printDisplayModel).body)

        val tableHeadings = doc.select("tr th")
        val tableData     = doc.select("tr td")

        "contain row for name" in {
          val rowIndex = 0
          tableHeadings.get(rowIndex).text shouldBe NameHeader
          tableData.get(rowIndex).attr("id") shouldBe "yourFullName"
          tableData.get(rowIndex).text shouldBe "Jim Davis"
        }

        "contain row for National Insurance Number" in {
          val rowIndex = 1
          tableHeadings.get(rowIndex).text shouldBe NinoHeader
          tableData.get(rowIndex).attr("id") shouldBe "yourNino"
          tableData.get(rowIndex).text shouldBe printDisplayModel.nino
        }

        "contain row for Protection Type" in {
          val rowIndex = 2
          tableHeadings.get(rowIndex).text shouldBe ProtectionTypeHeader
          tableData.get(rowIndex).attr("id") shouldBe "protectionType"
          tableData.get(rowIndex).text shouldBe "Individual protection 2014"
        }

        "contain row for Application Date" in {
          val rowIndex = 3
          tableHeadings.get(rowIndex).text shouldBe ApplicationDateHeader
          tableData.get(rowIndex).attr("id") shouldBe "applicationDate"
          tableData.get(rowIndex).text shouldBe printDisplayModel.certificateDate.get
        }

        "contain row for Status" in {
          val rowIndex = 4
          tableHeadings.get(rowIndex).text shouldBe StatusHeader
          tableData.get(rowIndex).attr("id") shouldBe "status"
          tableData.get(rowIndex).text shouldBe WithdrawnText
        }
      }

      "includeProtectedAmount is set to true" should {

        val doc = Jsoup.parse(amendProtectionOutcomeTable(printDisplayModel, includeProtectedAmount = true).body)

        val tableHeadings = doc.select("tr th")
        val tableData     = doc.select("tr td")

        "contain row for name" in {
          val rowIndex = 0
          tableHeadings.get(rowIndex).text shouldBe NameHeader
          tableData.get(rowIndex).attr("id") shouldBe "yourFullName"
          tableData.get(rowIndex).text shouldBe "Jim Davis"
        }

        "contain row for National Insurance Number" in {
          val rowIndex = 1
          tableHeadings.get(rowIndex).text shouldBe NinoHeader
          tableData.get(rowIndex).attr("id") shouldBe "yourNino"
          tableData.get(rowIndex).text shouldBe printDisplayModel.nino
        }

        "contain row for Protected Amount" in {
          val rowIndex = 2
          tableHeadings.get(rowIndex).text shouldBe ProtectedAmountHeader
          tableData.get(rowIndex).attr("id") shouldBe "protectedAmount"
          tableData.get(rowIndex).text shouldBe printDisplayModel.protectedAmount.get
        }

        "contain row for Protection Type" in {
          val rowIndex = 3
          tableHeadings.get(rowIndex).text shouldBe ProtectionTypeHeader
          tableData.get(rowIndex).attr("id") shouldBe "protectionType"
          tableData.get(rowIndex).text shouldBe "Individual protection 2014"
        }

        "contain row for Application Date" in {
          val rowIndex = 4
          tableHeadings.get(rowIndex).text shouldBe ApplicationDateHeader
          tableData.get(rowIndex).attr("id") shouldBe "applicationDate"
          tableData.get(rowIndex).text shouldBe printDisplayModel.certificateDate.get
        }

        "contain row for Status" in {
          val rowIndex = 5
          tableHeadings.get(rowIndex).text shouldBe StatusHeader
          tableData.get(rowIndex).attr("id") shouldBe "status"
          tableData.get(rowIndex).text shouldBe WithdrawnText
        }
      }
    }

    "provided with PrintDisplayModel containing notificationId: 7" should {

      val printDisplayModel = getPrintDisplayModelFor(7)

      "includeProtectedAmount is set to false" should {

        val doc = Jsoup.parse(amendProtectionOutcomeTable(printDisplayModel).body)

        val tableHeadings = doc.select("tr th")
        val tableData     = doc.select("tr td")

        "contain row for name" in {
          val rowIndex = 0
          tableHeadings.get(rowIndex).text shouldBe NameHeader
          tableData.get(rowIndex).attr("id") shouldBe "yourFullName"
          tableData.get(rowIndex).text shouldBe "Jim Davis"
        }

        "contain row for National Insurance Number" in {
          val rowIndex = 1
          tableHeadings.get(rowIndex).text shouldBe NinoHeader
          tableData.get(rowIndex).attr("id") shouldBe "yourNino"
          tableData.get(rowIndex).text shouldBe "nino"
        }

        "contain row for Protection Type" in {
          val rowIndex = 2
          tableHeadings.get(rowIndex).text shouldBe ProtectionTypeHeader
          tableData.get(rowIndex).attr("id") shouldBe "protectionType"
          tableData.get(rowIndex).text shouldBe "Individual protection 2014"
        }

        "contain row for Fixed Protection 2016 Reference Number" in {
          val rowIndex = 3
          tableHeadings.get(rowIndex).text shouldBe FixedProtectionRefHeader
          tableData.get(rowIndex).attr("id") shouldBe "fixedProtectionRefNum"
          tableData.get(rowIndex).text shouldBe "IP14XXXXXX"
        }

        "contain row for Application Date" in {
          val rowIndex = 4
          tableHeadings.get(rowIndex).text shouldBe ApplicationDateHeader
          tableData.get(rowIndex).attr("id") shouldBe "applicationDate"
          tableData.get(rowIndex).text shouldBe "14/07/2015"
        }
      }

      "includeProtectedAmount is set to true" should {

        val doc = Jsoup.parse(amendProtectionOutcomeTable(printDisplayModel, includeProtectedAmount = true).body)

        val tableHeadings = doc.select("tr th")
        val tableData     = doc.select("tr td")

        "contain row for name" in {
          val rowIndex = 0
          tableHeadings.get(rowIndex).text shouldBe NameHeader
          tableData.get(rowIndex).attr("id") shouldBe "yourFullName"
          tableData.get(rowIndex).text shouldBe "Jim Davis"
        }

        "contain row for National Insurance Number" in {
          val rowIndex = 1
          tableHeadings.get(rowIndex).text shouldBe NinoHeader
          tableData.get(rowIndex).attr("id") shouldBe "yourNino"
          tableData.get(rowIndex).text shouldBe "nino"
        }

        "contain row for Protected Amount" in {
          val rowIndex = 2
          tableHeadings.get(rowIndex).text shouldBe ProtectedAmountHeader
          tableData.get(rowIndex).attr("id") shouldBe "protectedAmount"
          tableData.get(rowIndex).text shouldBe printDisplayModel.protectedAmount.get
        }

        "contain row for Protection Type" in {
          val rowIndex = 3
          tableHeadings.get(rowIndex).text shouldBe ProtectionTypeHeader
          tableData.get(rowIndex).attr("id") shouldBe "protectionType"
          tableData.get(rowIndex).text shouldBe "Individual protection 2014"
        }

        "contain row for Fixed Protection 2016 Reference Number" in {
          val rowIndex = 4
          tableHeadings.get(rowIndex).text shouldBe FixedProtectionRefHeader
          tableData.get(rowIndex).attr("id") shouldBe "fixedProtectionRefNum"
          tableData.get(rowIndex).text shouldBe "IP14XXXXXX"
        }

        "contain row for Application Date" in {
          val rowIndex = 5
          tableHeadings.get(rowIndex).text shouldBe ApplicationDateHeader
          tableData.get(rowIndex).attr("id") shouldBe "applicationDate"
          tableData.get(rowIndex).text shouldBe "14/07/2015"
        }
      }
    }

    "provided with PrintDisplayModel containing notificationId: 8" should {

      val printDisplayModel = getPrintDisplayModelFor(8)

      "includeProtectedAmount is set to false" should {

        val doc = Jsoup.parse(amendProtectionOutcomeTable(printDisplayModel).body)

        val tableHeadings = doc.select("tr th")
        val tableData     = doc.select("tr td")

        "contain row for name" in {
          val rowIndex = 0
          tableHeadings.get(rowIndex).text shouldBe NameHeader
          tableData.get(rowIndex).attr("id") shouldBe "yourFullName"
          tableData.get(rowIndex).text shouldBe "Jim Davis"
        }

        "contain row for National Insurance Number" in {
          val rowIndex = 1
          tableHeadings.get(rowIndex).text shouldBe NinoHeader
          tableData.get(rowIndex).attr("id") shouldBe "yourNino"
          tableData.get(rowIndex).text shouldBe "nino"
        }

        "contain row for Protection Type" in {
          val rowIndex = 2
          tableHeadings.get(rowIndex).text shouldBe ProtectionTypeHeader
          tableData.get(rowIndex).attr("id") shouldBe "protectionType"
          tableData.get(rowIndex).text shouldBe "Individual protection 2016"
        }

        "contain row for Protection Reference Number" in {
          val rowIndex = 3
          tableHeadings.get(rowIndex).text shouldBe ProtectionRefHeader
          tableData.get(rowIndex).attr("id") shouldBe "protectionRefNum"
          tableData.get(rowIndex).text shouldBe "IP16XXXXXX"
        }

        "contain row for PSA Check Reference" in {
          val rowIndex = 4
          tableHeadings.get(rowIndex).text shouldBe PsaRefHeader
          tableData.get(rowIndex).attr("id") shouldBe "psaCheckRef"
          tableData.get(rowIndex).text shouldBe "psaRef"
        }

        "contain row for Application Date" in {
          val rowIndex = 5
          tableHeadings.get(rowIndex).text shouldBe ApplicationDateHeader
          tableData.get(rowIndex).attr("id") shouldBe "applicationDate"
          tableData.get(rowIndex).text shouldBe "14/07/2017"
        }
      }

      "includeProtectedAmount is set to true" should {

        val doc = Jsoup.parse(amendProtectionOutcomeTable(printDisplayModel, includeProtectedAmount = true).body)

        val tableHeadings = doc.select("tr th")
        val tableData     = doc.select("tr td")

        "contain row for name" in {
          val rowIndex = 0
          tableHeadings.get(rowIndex).text shouldBe NameHeader
          tableData.get(rowIndex).attr("id") shouldBe "yourFullName"
          tableData.get(rowIndex).text shouldBe "Jim Davis"
        }

        "contain row for National Insurance Number" in {
          val rowIndex = 1
          tableHeadings.get(rowIndex).text shouldBe NinoHeader
          tableData.get(rowIndex).attr("id") shouldBe "yourNino"
          tableData.get(rowIndex).text shouldBe "nino"
        }

        "contain row for Protected Amount" in {
          val rowIndex = 2
          tableHeadings.get(rowIndex).text shouldBe ProtectedAmountHeader
          tableData.get(rowIndex).attr("id") shouldBe "protectedAmount"
          tableData.get(rowIndex).text shouldBe printDisplayModel.protectedAmount.get
        }

        "contain row for Protection Type" in {
          val rowIndex = 3
          tableHeadings.get(rowIndex).text shouldBe ProtectionTypeHeader
          tableData.get(rowIndex).attr("id") shouldBe "protectionType"
          tableData.get(rowIndex).text shouldBe "Individual protection 2016"
        }

        "contain row for Protection Reference Number" in {
          val rowIndex = 4
          tableHeadings.get(rowIndex).text shouldBe ProtectionRefHeader
          tableData.get(rowIndex).attr("id") shouldBe "protectionRefNum"
          tableData.get(rowIndex).text shouldBe "IP16XXXXXX"
        }

        "contain row for PSA Check Reference" in {
          val rowIndex = 5
          tableHeadings.get(rowIndex).text shouldBe PsaRefHeader
          tableData.get(rowIndex).attr("id") shouldBe "psaCheckRef"
          tableData.get(rowIndex).text shouldBe "psaRef"
        }

        "contain row for Application Date" in {
          val rowIndex = 6
          tableHeadings.get(rowIndex).text shouldBe ApplicationDateHeader
          tableData.get(rowIndex).attr("id") shouldBe "applicationDate"
          tableData.get(rowIndex).text shouldBe "14/07/2017"
        }
      }
    }

    Seq(9, 10, 11, 12).foreach { notificationId =>
      s"provided with PrintDisplayModel containing notificationId: $notificationId" should {

        val printDisplayModel = getPrintDisplayModelFor(notificationId)

        "includeProtectedAmount is set to false" should {

          val doc = Jsoup.parse(amendProtectionOutcomeTable(printDisplayModel).body)

          val tableHeadings = doc.select("tr th")
          val tableData     = doc.select("tr td")

          "contain row for name" in {
            val rowIndex = 0
            tableHeadings.get(rowIndex).text shouldBe NameHeader
            tableData.get(rowIndex).attr("id") shouldBe "yourFullName"
            tableData.get(rowIndex).text shouldBe "Jim Davis"
          }

          "contain row for National Insurance Number" in {
            val rowIndex = 1
            tableHeadings.get(rowIndex).text shouldBe NinoHeader
            tableData.get(rowIndex).attr("id") shouldBe "yourNino"
            tableData.get(rowIndex).text shouldBe "nino"
          }

          "contain row for Protection Type" in {
            val rowIndex = 2
            tableHeadings.get(rowIndex).text shouldBe ProtectionTypeHeader
            tableData.get(rowIndex).attr("id") shouldBe "protectionType"
            tableData.get(rowIndex).text shouldBe "Individual protection 2016"
          }

          "contain row for Application Date" in {
            val rowIndex = 3
            tableHeadings.get(rowIndex).text shouldBe ApplicationDateHeader
            tableData.get(rowIndex).attr("id") shouldBe "applicationDate"
            tableData.get(rowIndex).text shouldBe "14/07/2017"
          }

          "contain row for Status" in {
            val rowIndex = 4
            tableHeadings.get(rowIndex).text shouldBe StatusHeader
            tableData.get(rowIndex).attr("id") shouldBe "status"
            tableData.get(rowIndex).text shouldBe DormantText
          }
        }

        "includeProtectedAmount is set to true" should {

          val doc = Jsoup.parse(amendProtectionOutcomeTable(printDisplayModel, includeProtectedAmount = true).body)

          val tableHeadings = doc.select("tr th")
          val tableData     = doc.select("tr td")

          "contain row for name" in {
            val rowIndex = 0
            tableHeadings.get(rowIndex).text shouldBe NameHeader
            tableData.get(rowIndex).attr("id") shouldBe "yourFullName"
            tableData.get(rowIndex).text shouldBe "Jim Davis"
          }

          "contain row for National Insurance Number" in {
            val rowIndex = 1
            tableHeadings.get(rowIndex).text shouldBe NinoHeader
            tableData.get(rowIndex).attr("id") shouldBe "yourNino"
            tableData.get(rowIndex).text shouldBe "nino"
          }

          "contain row for Protected Amount" in {
            val rowIndex = 2
            tableHeadings.get(rowIndex).text shouldBe ProtectedAmountHeader
            tableData.get(rowIndex).attr("id") shouldBe "protectedAmount"
            tableData.get(rowIndex).text shouldBe printDisplayModel.protectedAmount.get
          }

          "contain row for Protection Type" in {
            val rowIndex = 3
            tableHeadings.get(rowIndex).text shouldBe ProtectionTypeHeader
            tableData.get(rowIndex).attr("id") shouldBe "protectionType"
            tableData.get(rowIndex).text shouldBe "Individual protection 2016"
          }

          "contain row for Application Date" in {
            val rowIndex = 4
            tableHeadings.get(rowIndex).text shouldBe ApplicationDateHeader
            tableData.get(rowIndex).attr("id") shouldBe "applicationDate"
            tableData.get(rowIndex).text shouldBe "14/07/2017"
          }

          "contain row for Status" in {
            val rowIndex = 5
            tableHeadings.get(rowIndex).text shouldBe StatusHeader
            tableData.get(rowIndex).attr("id") shouldBe "status"
            tableData.get(rowIndex).text shouldBe DormantText
          }
        }
      }
    }

    "provided with PrintDisplayModel containing notificationId: 13" should {

      val printDisplayModel = getPrintDisplayModelFor(13)

      "includeProtectedAmount is set to false" should {

        val doc = Jsoup.parse(amendProtectionOutcomeTable(printDisplayModel).body)

        val tableHeadings = doc.select("tr th")
        val tableData     = doc.select("tr td")

        "contain row for name" in {
          val rowIndex = 0
          tableHeadings.get(rowIndex).text shouldBe NameHeader
          tableData.get(rowIndex).attr("id") shouldBe "yourFullName"
          tableData.get(rowIndex).text shouldBe "Jim Davis"
        }

        "contain row for National Insurance Number" in {
          val rowIndex = 1
          tableHeadings.get(rowIndex).text shouldBe NinoHeader
          tableData.get(rowIndex).attr("id") shouldBe "yourNino"
          tableData.get(rowIndex).text shouldBe "nino"
        }

        "contain row for Protection Type" in {
          val rowIndex = 2
          tableHeadings.get(rowIndex).text shouldBe ProtectionTypeHeader
          tableData.get(rowIndex).attr("id") shouldBe "protectionType"
          tableData.get(rowIndex).text shouldBe "Individual protection 2016"
        }

        "contain row for Application Date" in {
          val rowIndex = 3
          tableHeadings.get(rowIndex).text shouldBe ApplicationDateHeader
          tableData.get(rowIndex).attr("id") shouldBe "applicationDate"
          tableData.get(rowIndex).text shouldBe "14/07/2017"
        }

        "contain row for Status" in {
          val rowIndex = 4
          tableHeadings.get(rowIndex).text shouldBe StatusHeader
          tableData.get(rowIndex).attr("id") shouldBe "status"
          tableData.get(rowIndex).text shouldBe WithdrawnText
        }
      }

      "includeProtectedAmount is set to true" should {

        val doc = Jsoup.parse(amendProtectionOutcomeTable(printDisplayModel, includeProtectedAmount = true).body)

        val tableHeadings = doc.select("tr th")
        val tableData     = doc.select("tr td")

        "contain row for name" in {
          val rowIndex = 0
          tableHeadings.get(rowIndex).text shouldBe NameHeader
          tableData.get(rowIndex).attr("id") shouldBe "yourFullName"
          tableData.get(rowIndex).text shouldBe "Jim Davis"
        }

        "contain row for National Insurance Number" in {
          val rowIndex = 1
          tableHeadings.get(rowIndex).text shouldBe NinoHeader
          tableData.get(rowIndex).attr("id") shouldBe "yourNino"
          tableData.get(rowIndex).text shouldBe "nino"
        }

        "contain row for Protected Amount" in {
          val rowIndex = 2
          tableHeadings.get(rowIndex).text shouldBe ProtectedAmountHeader
          tableData.get(rowIndex).attr("id") shouldBe "protectedAmount"
          tableData.get(rowIndex).text shouldBe printDisplayModel.protectedAmount.get
        }

        "contain row for Protection Type" in {
          val rowIndex = 3
          tableHeadings.get(rowIndex).text shouldBe ProtectionTypeHeader
          tableData.get(rowIndex).attr("id") shouldBe "protectionType"
          tableData.get(rowIndex).text shouldBe "Individual protection 2016"
        }

        "contain row for Application Date" in {
          val rowIndex = 4
          tableHeadings.get(rowIndex).text shouldBe ApplicationDateHeader
          tableData.get(rowIndex).attr("id") shouldBe "applicationDate"
          tableData.get(rowIndex).text shouldBe "14/07/2017"
        }

        "contain row for Status" in {
          val rowIndex = 5
          tableHeadings.get(rowIndex).text shouldBe StatusHeader
          tableData.get(rowIndex).attr("id") shouldBe "status"
          tableData.get(rowIndex).text shouldBe WithdrawnText
        }
      }
    }

    "provided with PrintDisplayModel containing notificationId: 14" should {

      val printDisplayModel = getPrintDisplayModelFor(14)

      "includeProtectedAmount is set to false" should {

        val doc = Jsoup.parse(amendProtectionOutcomeTable(printDisplayModel).body)

        val tableHeadings = doc.select("tr th")
        val tableData     = doc.select("tr td")

        "contain row for name" in {
          val rowIndex = 0
          tableHeadings.get(rowIndex).text shouldBe NameHeader
          tableData.get(rowIndex).attr("id") shouldBe "yourFullName"
          tableData.get(rowIndex).text shouldBe "Jim Davis"
        }

        "contain row for National Insurance Number" in {
          val rowIndex = 1
          tableHeadings.get(rowIndex).text shouldBe NinoHeader
          tableData.get(rowIndex).attr("id") shouldBe "yourNino"
          tableData.get(rowIndex).text shouldBe "nino"
        }

        "contain row for Protection Type" in {
          val rowIndex = 2
          tableHeadings.get(rowIndex).text shouldBe ProtectionTypeHeader
          tableData.get(rowIndex).attr("id") shouldBe "protectionType"
          tableData.get(rowIndex).text shouldBe "Individual protection 2016"
        }

        "contain row for Fixed Protection 2016 Reference Number" in {
          val rowIndex = 3
          tableHeadings.get(rowIndex).text shouldBe FixedProtectionRefHeader
          tableData.get(rowIndex).attr("id") shouldBe "fixedProtectionRefNum"
          tableData.get(rowIndex).text shouldBe "IP16XXXXXX"
        }

        "contain row for Application Date" in {
          val rowIndex = 4
          tableHeadings.get(rowIndex).text shouldBe ApplicationDateHeader
          tableData.get(rowIndex).attr("id") shouldBe "applicationDate"
          tableData.get(rowIndex).text shouldBe "14/07/2017"
        }
      }

      "includeProtectedAmount is set to true" should {

        val doc = Jsoup.parse(amendProtectionOutcomeTable(printDisplayModel, includeProtectedAmount = true).body)

        val tableHeadings = doc.select("tr th")
        val tableData     = doc.select("tr td")

        "contain row for name" in {
          val rowIndex = 0
          tableHeadings.get(rowIndex).text shouldBe NameHeader
          tableData.get(rowIndex).attr("id") shouldBe "yourFullName"
          tableData.get(rowIndex).text shouldBe "Jim Davis"
        }

        "contain row for National Insurance Number" in {
          val rowIndex = 1
          tableHeadings.get(rowIndex).text shouldBe NinoHeader
          tableData.get(rowIndex).attr("id") shouldBe "yourNino"
          tableData.get(rowIndex).text shouldBe "nino"
        }

        "contain row for Protected Amount" in {
          val rowIndex = 2
          tableHeadings.get(rowIndex).text shouldBe ProtectedAmountHeader
          tableData.get(rowIndex).attr("id") shouldBe "protectedAmount"
          tableData.get(rowIndex).text shouldBe printDisplayModel.protectedAmount.get
        }

        "contain row for Protection Type" in {
          val rowIndex = 3
          tableHeadings.get(rowIndex).text shouldBe ProtectionTypeHeader
          tableData.get(rowIndex).attr("id") shouldBe "protectionType"
          tableData.get(rowIndex).text shouldBe "Individual protection 2016"
        }

        "contain row for Fixed Protection 2016 Reference Number" in {
          val rowIndex = 4
          tableHeadings.get(rowIndex).text shouldBe FixedProtectionRefHeader
          tableData.get(rowIndex).attr("id") shouldBe "fixedProtectionRefNum"
          tableData.get(rowIndex).text shouldBe "IP16XXXXXX"
        }

        "contain row for Application Date" in {
          val rowIndex = 5
          tableHeadings.get(rowIndex).text shouldBe ApplicationDateHeader
          tableData.get(rowIndex).attr("id") shouldBe "applicationDate"
          tableData.get(rowIndex).text shouldBe "14/07/2017"
        }
      }
    }
  }

}
