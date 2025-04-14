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
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.{Lang, Messages, MessagesImpl}
import play.api.mvc.MessagesControllerComponents
import testHelpers.FakeApplication

import java.time.LocalDate
import java.util.Locale

class DisplaySpec extends FakeApplication with MockitoSugar {

  implicit val mockMessages: Messages = mock[Messages]
  val mockMCC = fakeApplication().injector.instanceOf[MessagesControllerComponents].messagesApi

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
      "lang is set to en" in {

        val (lang, messsage) = createLangMessages(Locale.ENGLISH)
        val tstDate = LocalDate.of(2018, 4, 17)
        dateDisplayString(tstDate)(lang, messsage) shouldBe "17 April 2018"
      }

      "lang is set to cy" in {
        val (lang, messsage) = createLangMessages(Locale.forLanguageTag("cy"))
        val tstDate = LocalDate.of(2018, 4, 17)
        dateDisplayString(tstDate)(lang, messsage) shouldBe "17 Ebrill 2018"
      }
    }
  }
}