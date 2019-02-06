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
import testHelpers.ViewSpecHelpers.withdraw.WithdrawConfirmationSpecMessages
import views.html.pages.withdraw.{withdrawConfirmation => views}


class WithdrawConfirmationViewSpec extends CommonViewSpecHelper with WithdrawConfirmationSpecMessages {


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


      s"has the first paragraph of ${"pla.withdraw.confirmation.contact.you.if.needed"}" in {
        grid.select("p").get(0).text shouldBe plaWithdrawConfirmationContactYouIfNeeded
      }

      s"have a question of ${"pla.withdraw.confirmation.other.protections.link"}" in {
        doc.select("div.grid > p").get(1).text shouldBe plaWithdrawConfirmationOtherProtections
      }

      "Other protections link " in {
        doc.select("div.grid > p a").text() shouldBe plaWithdrawConfirmationOtherProtectionsLink
      }

      "Other protections link href" in {
        doc.select("div.grid > p a").attr("href") shouldBe plaWithdrawConfirmationOtherProtectionsUrl

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
    "feedback link " in {
      doc.select("div.grid-row > p a").text() shouldBe plaWithdrawConfirmFeedbackLink
    }
    "feedback link href" in {
      doc.select("div.grid-row > p a").attr("href")  shouldBe plaWithdrawConfirmFeedbackUrl
    }

  }

}
