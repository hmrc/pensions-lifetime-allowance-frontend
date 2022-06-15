/*
 * Copyright 2022 HM Revenue & Customs
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
import views.html.pages.withdraw.withdrawImplications
import org.jsoup.Jsoup
import play.api.data.Form
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.withdraw.WithdrawImplicationsSpecMessages

class WithdrawImplicationsSpec extends CommonViewSpecHelper with WithdrawImplicationsSpecMessages {

  val withdrawDateForModel = withdrawDateForm: Form[WithdrawDateFormModel]

  "Withdraw Implication view" when {
    lazy val view = application.injector.instanceOf[withdrawImplications]
    lazy val doc = Jsoup.parse(view.apply(withdrawDateForModel, "IP2014", "dormant").body)

    s"have a title ${"pla.withdraw.protection.title"}" in {
      doc.title() shouldBe plaWithdrawProtectionTitle(plaWithdrawProtectionIP2014label)
    }

    s"have a back link with text back " in {
      doc.select("a.back-link").text() shouldBe "Back"
    }

    s"have a back link with href" in {
      doc.select("a.back-link").attr("href") shouldBe routes.ReadProtectionsController.currentProtections.url
    }

    s"have the question of the page ${"pla.withdraw.protection.title"}" in {
      doc.select("h1.heading-large").text shouldEqual plaWithdrawProtectionTitle(plaWithdrawProtectionIP2014label)
    }

    "have a div tag that" should {
      "have a heading label" in {
        doc.select("div.grid > p").text() shouldBe plaWithdrawProtectionIfInfo(plaWithdrawProtectionIP2014label)
      }
      s"has first paragraph of ${"pla.withdraw.protection.if.info.1"}" in {
        doc.select("li").get(0).text() shouldBe plaWithdrawProtectionIfInfo1(plaWithdrawProtectionIP2014label)
      }

      s"has second paragraph of ${"pla.withdraw.protection.if.info.1"}" in {
        doc.select("li").get(1).text() shouldBe plaWithdrawProtectionIfInfo2(plaWithdrawProtectionIP2014label)
      }
    }


    "have a continue button that" should {
      lazy val button = doc.getElementById("continue-button")

      s"have text of ${"pla.withdraw.protection.continue.title"}" in {
        button.text() shouldBe plaWithdrawProtectionContinueTitle
      }

      s"have a href" in {
        button.attr("href") shouldBe routes.WithdrawProtectionController.getWithdrawDateInput.url
      }

    }

  }

}
