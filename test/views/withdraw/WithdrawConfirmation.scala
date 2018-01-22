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

package views.withdraw

import controllers.routes
import org.jsoup.Jsoup
import play.api.i18n.Messages.Implicits.applicationMessages
import play.api.i18n.Messages
import play.twirl.api.Html
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.withdraw.WithdrawConfirmationSpecMessage
import views.html.pages.withdraw.{withdrawConfirmation => views}


class WithdrawConfirmation extends CommonViewSpecHelper with WithdrawConfirmationSpecMessage {


  "Withdraw Confirmation view" when {
    lazy val protectionType = "IP2014"
    lazy val view = views(protectionType)
    lazy val doc = Jsoup.parse(view.body)

    s"have a title ${"pla.withdraw.confirmation.message"}" in {
      doc.title() shouldBe plaWithdrawConfirmationMessage(plaWithdrawProtectionIP2014label)
    }

    s"have a question of ${"pla.withdraw.confirmation.message"}" in {
      doc.select("span.heading-large").text() shouldBe plaWithdrawConfirmationMessage(plaWithdrawProtectionIP2014label)
    }

    "have a div tag that" should {
      lazy val grid = doc.select("div.grid-row > div.grid")

      "have the class 'grid'" in {
        grid.hasClass("grid") shouldBe true
      }

      s"has the first paragraph of ${"pla.withdraw.confirmation.check.details"}" in {
        grid.select("p").get(0).text shouldBe plaWithdrawConfirmationCheckDetails
      }

      s"has the second paragraph of ${"pla.withdraw.confirmation.contact.you.if.needed"}" in {
        grid.select("p").get(1).text shouldBe plaWithdrawConfirmationContactYouIfNeeded
      }

      "have a div tag size" in {
        grid.size() shouldBe 1
      }
    }

    s"have a message of ${"pla.withdraw.confirm.feedback-heading"}" in {
      doc.select("div.grid-row > h2").text shouldBe plaWithdrawConfirmFeedbackHeading
    }

    s"feedback message of ${"pla.withdraw.confirm.feedback-text"}" in {
      doc.select("div.grid-row > p").text shouldBe plaWithdrawConfirmFeedbackText
    }

  }

}
