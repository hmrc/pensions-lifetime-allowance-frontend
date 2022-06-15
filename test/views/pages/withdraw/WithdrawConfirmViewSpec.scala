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
import org.jsoup.Jsoup
import play.api.i18n.Lang
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.withdraw.WithdrawConfirmSpecMessages
import uk.gov.hmrc.play.views.html.helpers.{ErrorSummary, FormWithCSRF}
import views.html.pages.withdraw.withdrawConfirm

class WithdrawConfirmViewSpec extends CommonViewSpecHelper with WithdrawConfirmSpecMessages {

  implicit val errorSummary: ErrorSummary = app.injector.instanceOf[ErrorSummary]
  implicit val formWithCSRF: FormWithCSRF = app.injector.instanceOf[FormWithCSRF]

  "Withdraw Confirm view" when {
    implicit val lang = mock[Lang]
    val withdrawDate = "2018-01-16"
    val withdrawDatePretty = "16 January 2018"

    lazy val view = application.injector.instanceOf[withdrawConfirm]

    lazy val doc = Jsoup.parse(view.apply(withdrawDate,"ip14", "dormant").body)

    s"have a title ${"pla.withdraw.what-happens.info-heading"}" in {
      doc.title() shouldBe plaWithdrawWhatHappensInfoHeading
    }

    s"have a question of ${"pla.withdraw.what-happens.info-heading"}" in {
      doc.select("h1.heading-large").text() shouldBe plaWithdrawWhatHappensInfoHeading
    }

    s"have a back link with text 'back' " in {
      doc.select("a").text() shouldBe plaBaseBack
    }

    "have a back with href" in {
      doc.select("a").attr("href") shouldBe routes.WithdrawProtectionController.getWithdrawDateInput.url
    }

    "have a form action of 'getAction'" in {
      doc.select("form").attr("action") shouldBe routes.WithdrawProtectionController.displayWithdrawConfirmation(withdrawDate).url
    }

    "have a form method of 'GET'" in {
      doc.select("form").attr("method") shouldBe "GET"
    }

    "have a submit button that" should {
      lazy val submitButton = doc.select("button")

      s"have the button text of '$plaWithdrawImplicationsSubmit'" in {
        submitButton.text shouldBe plaWithdrawImplicationsSubmit
      }

      "be of type submit" in {
        submitButton.attr("type") shouldBe "submit"
      }
    }

    "have a div tag that" should {
      lazy val grid = doc.select("div.grid")

      s"has the first paragraph of ${"pla.withdraw.protection.what-happens.info.1"}" in {
        grid.select("p").get(0).text shouldBe plaWithdrawProtectionWhatHappensInfo1(withdrawDatePretty)
      }

      s"has the second paragraph of $plaWithdrawProtectionWhatHappensInfo2" in {
        grid.select("p").get(1).text shouldBe plaWithdrawProtectionWhatHappensInfo2
      }

      s"has the third paragraph of ${"pla.withdraw.protection.what-happens.info.3"}" in {
        grid.select("p").get(2).text shouldBe plaWithdrawProtectionWhatHappensInfo3(withdrawDatePretty)
      }

      s"has the fourth paragraph of ${"pla.withdraw.protection.what-happens.info.4"}" in {
        grid.select("p").get(3).text shouldBe plaWithdrawProtectionWhatHappensInfo4(withdrawDatePretty)
      }

      s"has the fifth paragraph of $plaWithdrawProtectionWhatHappensInfo5" in {
        grid.select("p").get(4).text shouldBe plaWithdrawProtectionWhatHappensInfo5
      }

    }


  }


}
