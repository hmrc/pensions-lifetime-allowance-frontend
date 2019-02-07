/*
 * Copyright 2019 HM Revenue & Customs
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

package views.pages.withdraw

import org.jsoup.Jsoup
import play.api.i18n.Messages.Implicits.applicationMessages
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.withdraw.WithdrawImplicationMessagesSpecMessages
import views.html.pages.withdraw.{withdrawImplicationMessage => views}

class WithdrawImplicationMessage extends CommonViewSpecHelper with WithdrawImplicationMessagesSpecMessages {

  "Withdraw Implication Message view" which {

    "is given the protection type IP2014" should {
      val protectionType = "IP2014"
      lazy val view = views(protectionType, "dormant")
      lazy val doc = Jsoup.parse(view.body)

      s"have a alert of ${"pla.withdraw.implication.info"}" in {
        doc.select("div").text() shouldBe plaWithdrawImplicationInfo(plaWithdrawProtectionIP2014label)
      }
    }

    "is given the protection type IP2016" should {
      val protectionType = "IP2016"
      lazy val view = views(protectionType, "dormant")
      lazy val doc = Jsoup.parse(view.body)

      s"have a alert of ${"pla.withdraw.implication.info"}" in {
        doc.select("div").text() shouldBe plaWithdrawImplicationInfo(plaWithdrawProtectionIP2016label)
      }
    }
  }

}
