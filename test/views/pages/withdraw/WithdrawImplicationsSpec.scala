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

import controllers.routes
import forms.WithdrawDateForm.withdrawDateForm
import models.WithdrawDateFormModel
import org.jsoup.Jsoup
import play.api.data.Form
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.withdraw.WithdrawImplicationsSpecMessages
import views.html.pages.withdraw.withdrawImplications

import java.time.LocalDate

class WithdrawImplicationsSpec extends CommonViewSpecHelper with WithdrawImplicationsSpecMessages {

  val withdrawDateForModel: Form[WithdrawDateFormModel] = withdrawDateForm(LocalDate.now()): Form[WithdrawDateFormModel]

  "Withdraw Implication view for IP2014" when {
    lazy val view = app.injector.instanceOf[withdrawImplications]
    lazy val doc  = Jsoup.parse(view.apply(withdrawDateForModel, "IP2014", "dormant").body)

    s"have a title ${"pla.withdraw.protection.title"}" in {
      doc.title() shouldBe plaWithdrawProtectionTitle(plaWithdrawProtectionIP2014label)
    }

    s"have a back link with text back " in {
      doc.select(".govuk-back-link").text() shouldBe "Back"
    }

    s"have a back link with href" in {
      doc.select(".govuk-back-link").attr("href") shouldBe "#"
    }

    s"have the question of the page ${"pla.withdraw.protection.title"}" in {
      doc.select("h1.govuk-heading-xl").text shouldEqual plaWithdrawProtectionHeading(plaWithdrawProtectionIP2014label)
    }

    s"have a alert of ${"pla.withdraw.implication.info"}" in {
      doc
        .select("#main-content > div > div > div > strong")
        .text() shouldBe s"Warning ${plaWithdrawImplicationInfo(plaWithdrawProtectionIP2014label)}"
    }

    "have a div tag that" should {
      "have a heading label" in {
        doc.select("#main-content > div > div > p").text() shouldBe plaWithdrawProtectionIfInfo(
          plaWithdrawProtectionIP2014label
        )
      }
      s"has first paragraph of ${"pla.withdraw.protection.if.info.1"}" in {
        doc.select("#main-content > div > div > ul > li:nth-child(1)").text() shouldBe plaWithdrawProtectionIfInfo1(
          plaWithdrawProtectionIP2014label
        )
      }

      s"has second paragraph of ${"pla.withdraw.protection.if.info.1"}" in {
        doc.select("#main-content > div > div > ul > li:nth-child(2)").text() shouldBe plaWithdrawProtectionIfInfo2(
          plaWithdrawProtectionIP2014label
        )
      }
    }

    "have a continue button that" should {
      lazy val button = doc.getElementById("continue-button")

      s"have text of ${"pla.withdraw.protection.continue.title"}" in {
        button.text() shouldBe plaWithdrawProtectionContinueTitle
      }

      s"have a href" in {
        button.attr("href") shouldBe routes.WithdrawProtectionDateInputController.getWithdrawDateInput.url
      }

    }

  }

  "Withdraw Implication view for IP2016" when {
    lazy val view = app.injector.instanceOf[withdrawImplications]
    lazy val doc  = Jsoup.parse(view.apply(withdrawDateForModel, "IP2016", "dormant").body)

    s"have a title ${"pla.withdraw.protection.title"}" in {
      doc.title() shouldBe plaWithdrawProtectionTitle(plaWithdrawProtectionIP2016label)
    }

    s"have a back link with text back " in {
      doc.select(".govuk-back-link").text() shouldBe "Back"
    }

    s"have a back link with href" in {
      doc.select(".govuk-back-link").attr("href") shouldBe "#"
    }

    s"have the question of the page ${"pla.withdraw.protection.title"}" in {
      doc.select("h1.govuk-heading-xl").text shouldEqual plaWithdrawProtectionHeading(plaWithdrawProtectionIP2016label)
    }

    s"have a alert of ${"pla.withdraw.implication.info"}" in {
      doc
        .select("#main-content > div > div > div > strong")
        .text() shouldBe s"Warning ${plaWithdrawImplicationInfo(plaWithdrawProtectionIP2016label)}"
    }

    "have a div tag that" should {
      "have a heading label" in {
        doc.select("#main-content > div > div > p").text() shouldBe plaWithdrawProtectionIfInfo(
          plaWithdrawProtectionIP2016label
        )
      }
      s"has first paragraph of ${"pla.withdraw.protection.if.info.1"}" in {
        doc.select("#main-content > div > div > ul > li:nth-child(1)").text() shouldBe plaWithdrawProtectionIfInfo1(
          plaWithdrawProtectionIP2016label
        )
      }

      s"has second paragraph of ${"pla.withdraw.protection.if.info.1"}" in {
        doc.select("#main-content > div > div > ul > li:nth-child(2)").text() shouldBe plaWithdrawProtectionIfInfo2(
          plaWithdrawProtectionIP2016label
        )
      }
    }

    "have a continue button that" should {
      lazy val button = doc.getElementById("continue-button")

      s"have text of ${"pla.withdraw.protection.continue.title"}" in {
        button.text() shouldBe plaWithdrawProtectionContinueTitle
      }

      s"have a href" in {
        button.attr("href") shouldBe routes.WithdrawProtectionDateInputController.getWithdrawDateInput.url
      }

    }

  }

}
