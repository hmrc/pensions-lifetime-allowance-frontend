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

import forms.WithdrawDateForm._
import models.WithdrawDateFormModel
import org.jsoup.Jsoup
import play.api.data.Form
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.withdraw.WithdrawDateSpecMessages
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import views.html.pages.withdraw.withdrawDate

import java.time.LocalDate

class WithdrawDateSpec extends CommonViewSpecHelper with WithdrawDateSpecMessages {
  val withdrawDateForModelAllGood: Form[WithdrawDateFormModel] =
    withdrawDateForm(LocalDate.of(2017, 5, 4)).bind(Map[String,String]("withdrawDate.day" -> "19", "withdrawDate.month" -> "1", "withdrawDate.year" -> "2018"))
  val withdrawDateForModelInError: Form[WithdrawDateFormModel] =
    withdrawDateForm(LocalDate.now()).bind(Map[String,String]("withdrawDate.date" -> "", "withdrawDate.month" -> "", "withdrawDate.year" -> "2018"))
  lazy val view: withdrawDate = application.injector.instanceOf[withdrawDate]
  implicit val formWithCSRF: FormWithCSRF = app.injector.instanceOf[FormWithCSRF]

  "Withdraw Date view with form without errors" when {
    lazy val doc = Jsoup.parse(view.apply(withdrawDateForModelAllGood, "IP2014", "dormant").body)
    s"have a title $plaWithdrawDateInputTitle" in {
      doc.title() shouldBe plaWithdrawDateInputTitle
    }
    s"have the question of the page $plaWithdrawDateInputTitle" in {
      doc.select("h1.govuk-fieldset__heading").text shouldEqual plaWithdrawDateInputHeading
    }
    s"have a back link with text back " in {
      doc.select(".govuk-back-link").text() shouldBe plaBaseBack
    }
    s"have a back link with href" in {
      doc.select(".govuk-back-link").attr("href") shouldBe "#"
    }
    s"have a hint of $plaWithdrawDateInputFormHint" in {
      doc.select(".govuk-hint").text() shouldBe plaWithdrawDateInputFormHint
    }
    "display the withdraw day hint" in {
      doc.select("#withdrawDate > div:nth-child(1) > div > label").text() shouldBe plaWithdrawDateInputFormDay
    }
    "display the withdraw month hint" in {
      doc.select("#withdrawDate > div:nth-child(2) > div > label").text() shouldBe plaWithdrawDateInputFormMonth
    }
    "display the withdraw year hint" in {
      doc.select("#withdrawDate > div:nth-child(3) > div > label").text() shouldBe plaWithdrawDateInputFormYear
    }
    "not display an error summary message for the day" in {
      doc.body().select(".govuk-error-message").size() shouldBe 0
    }
    "not display an error notification message for the day" in {
      doc.body().select(".govuk-error-summary").size() shouldBe 0
    }
    "have a submit button that" should {
      lazy val button = doc.getElementById("submit")
      s"have text of $plaBaseContinue" in {
        button.text() shouldBe plaBaseContinue
      }
      "be of type submit" in {
        button.attr("id") shouldBe "submit"
      }
      "have the class 'button'" in {
        button.hasClass("govuk-button") shouldBe true
      }
    }
  }
  "Withdraw Date view with form with errors" should {
    lazy val doc = Jsoup.parse(view.apply(withdrawDateForModelInError, "IP2014", "dormant").body)
    "display an error message for the input" in {
      doc.body().select(".govuk-error-message").size() shouldBe 1
    }
    "display an error summary message for the amount" in {
      doc.body().select(".govuk-error-summary").size() shouldBe 1
    }
  }
}