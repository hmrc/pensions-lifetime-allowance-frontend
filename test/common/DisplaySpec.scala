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

package common

import common.Display._
import models.{DateModel, TimeModel}
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.mvc.MessagesControllerComponents
import testHelpers.FakeApplication

import java.time.{LocalDate, LocalTime}
import java.util.Locale

class DisplaySpec extends FakeApplication with MockitoSugar {

  implicit val mockMessages: Messages = mock[Messages]
  val mockMCC: MessagesApi            = inject[MessagesControllerComponents].messagesApi

  "currencyDisplayString" should {

    "correctly create a currency string for 1000.45" in {
      val tstAmt = BigDecimal(1000.45)
      currencyDisplayString(tstAmt) shouldBe "£1,000.45"
    }

    "correctly create a currency string for -1000.45" in {
      val tstAmt = BigDecimal(-1000.45)
      currencyDisplayString(tstAmt) shouldBe "£-1,000.45"
    }

    "create a currency string for 7" in {
      val tstAmt = BigDecimal(7)
      currencyDisplayString(tstAmt) shouldBe "£7"
    }

    "create a currency string for 3.9" in {
      val tstAmt = BigDecimal(3.9)
      currencyDisplayString(tstAmt) shouldBe "£3.90"
    }
  }

  "dateDisplayString" should {
    def createLangMessages(languageCode: Locale): (Lang, Messages) = {
      val lang = new Lang(languageCode)
      (lang, MessagesImpl(lang, mockMCC))
    }

    "correctly create a date string for 17/04/2018" when {
      val tstDate = DateModel(LocalDate.of(2018, 4, 17))

      "lang is set to en" in {

        val (lang, message) = createLangMessages(Locale.ENGLISH)
        dateDisplayString(tstDate)(lang, message) shouldBe "17 April 2018"
      }

      "lang is set to cy" in {
        val (lang, message) = createLangMessages(Locale.forLanguageTag("cy"))
        dateDisplayString(tstDate)(lang, message) shouldBe "17 Ebrill 2018"
      }
    }

  }

  "timeDisplayString" should {
    "correctly create a time string for 17:23:09" in {
      val testTime = TimeModel(LocalTime.of(17, 23, 9))

      timeDisplayString(testTime) shouldBe "5:23pm"
    }

    "correct create a time string for 09:35:25" in {
      val testTime = TimeModel(LocalTime.of(9, 35, 25))

      timeDisplayString(testTime) shouldBe "9:35am"
    }
  }

  "percentageDisplayString" should {
    "correctly create a percentage string for 34" in {
      percentageDisplayString(34) shouldBe "34%"
    }
  }

  "factorDisplayString" should {
    "correctly format factor string" when {
      "the factor is 0.34" in {
        factorDisplayString(0.34) shouldBe "0.34"
      }

      "the factor is 0.343" in {
        factorDisplayString(0.343) shouldBe "0.34"
      }

      "the factor is 0.5" in {
        factorDisplayString(0.5) shouldBe "0.50"
      }

      "the factor is 0" in {
        factorDisplayString(0) shouldBe "0.00"
      }
    }
  }

}
