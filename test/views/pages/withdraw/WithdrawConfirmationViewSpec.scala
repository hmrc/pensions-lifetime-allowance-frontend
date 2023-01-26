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

package views.pages.withdraw

import org.jsoup.Jsoup
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.withdraw.WithdrawConfirmationSpecMessages
import views.html.pages.withdraw.withdrawConfirmation

class WithdrawConfirmationViewSpec extends CommonViewSpecHelper with WithdrawConfirmationSpecMessages {

  "Withdraw Confirmation view" when {
    lazy val view = application.injector.instanceOf[withdrawConfirmation]
    lazy val doc = Jsoup.parse(view("IP2014").body)

    s"have a title ${"pla.withdraw.confirmation.message"}" in {
      doc.title() shouldBe plaWithdrawConfirmationTitle(plaWithdrawProtectionIP2014label)
    }

    s"have a question of ${"pla.withdraw.confirmation.message"}" in {
      doc.select("h1").text() shouldBe plaWithdrawConfirmationMessage(plaWithdrawProtectionIP2014label)
    }

    "have a div tag that" should {

      s"have the first paragraph of ${"pla.withdraw.confirmation.contact.you.if.needed"}" in {
        doc.select("#main-content > div > div > p:nth-child(2)").text shouldBe plaWithdrawConfirmationContactYouIfNeeded
      }

      s"have a question of ${"pla.withdraw.confirmation.other.protections.link"}" in {
        doc.select("#main-content > div > div > p:nth-child(3)").text shouldBe plaWithdrawConfirmationOtherProtections
      }

      "Other protections link " in {
        doc.select("p:nth-child(3) > a").text() shouldBe plaWithdrawConfirmationOtherProtectionsLink
      }

      "Other protections link href" in {
        doc.select("#main-content > div > div > p:nth-child(3) > a").attr("href") shouldBe plaWithdrawConfirmationOtherProtectionsUrl

      }
    }

    s"have a message of ${"pla.withdraw.confirm.feedback-heading"}" in {
      doc.select("h2.govuk-heading-m").text shouldBe plaWithdrawConfirmFeedbackHeading
    }

    s"feedback message of ${"pla.withdraw.confirm.feedback-text"}" in {
      doc.select("#main-content > div > div > p:nth-child(5)").text shouldBe plaWithdrawConfirmFeedbackText
    }
    "feedback link " in {
      doc.select("#submit-survey-button").text() shouldBe plaWithdrawConfirmFeedbackLink
    }
    "feedback link href" in {
      doc.select("#submit-survey-button").attr("href")  shouldBe plaWithdrawConfirmFeedbackUrl
    }

  }

}
