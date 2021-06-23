/*
 * Copyright 2021 HM Revenue & Customs
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
import forms.WithdrawDateForm._
import models.WithdrawDateFormModel
import org.jsoup.Jsoup
import play.api.data.Form
import play.api.libs.json.Json
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.withdraw.WithdrawDateSpecMessages
import uk.gov.hmrc.play.views.html.helpers.{ErrorSummary, FormWithCSRF}
import views.html.pages.withdraw.withdrawDate

class WithdrawDateSpec extends CommonViewSpecHelper with WithdrawDateSpecMessages {
  val withdrawDateForModelAllGood = withdrawDateForm.bind(Map[String,String]("withdrawDay" -> "19", "withdrawMonth" -> "1", "withdrawYear" -> "2018"))
  val withdrawDateForModelInError = withdrawDateForm.bind(Map[String,String]("withdrawDay" -> "", "withdrawMonth" -> "", "withdrawYear" -> "2018"))
  lazy val view = application.injector.instanceOf[withdrawDate]
  implicit val errorSummary: ErrorSummary = app.injector.instanceOf[ErrorSummary]
  implicit val formWithCSRF: FormWithCSRF = app.injector.instanceOf[FormWithCSRF]

  "Withdraw Date view with form without errors" when {
    lazy val doc = Jsoup.parse(view.apply(withdrawDateForModelAllGood, "IP2014", "dormant").body)
    s"have a title $plaWithdrawDateInputTitle" in {
      doc.title() shouldBe plaWithdrawDateInputTitle
    }
    s"have the question of the page $plaWithdrawDateInputTitle" in {
      doc.select("h1.heading-large").text shouldEqual plaWithdrawDateInputTitle
    }
    s"have a back link with text back " in {
      doc.select("a.back-link").text() shouldBe plaBaseBack
    }
    s"have a back link with href" in {
      doc.select("a.back-link").attr("href") shouldBe routes.WithdrawProtectionController.withdrawImplications().url
    }
    s"have a hint of $plaWithdrawDateInputFormHint" in {
      doc.select("span").get(0).text() shouldBe plaWithdrawDateInputFormHint
    }
    "display the withdraw day of the form" in {
      doc.body().select("#withdrawDay").attr("value") shouldEqual "19"
    }
    "display the withdraw month of the form" in {
      doc.body().select("#withdrawMonth").attr("value") shouldEqual "1"
    }
    "display the withdraw year of the form" in {
      doc.body().select("#withdrawYear").attr("value") shouldEqual "2018"
    }
    "display the withdraw day hint" in {
      doc.select("label > span").get(0).text() shouldBe plaWithdrawDateInputFormDay
    }
    "display the withdraw month hint" in {
      doc.select("label > span").get(1).text() shouldBe plaWithdrawDateInputFormMonth
    }
    "display the withdraw year hint" in {
      doc.select("label > span").get(2).text() shouldBe plaWithdrawDateInputFormYear
    }
    "not display an error summary message for the day" in {
      doc.body().select("fieldset.form-field--error").size() shouldBe 0
    }
    "not display an error notification message for the day" in {
      doc.body().select("div.error-summary--show").size() shouldBe 0
    }
    "have a submit button that" should {
      lazy val button = doc.getElementById("submit")
      s"have text of $plaBaseContinue" in {
        button.text() shouldBe plaBaseContinue
      }
      "be of type submit" in {
        button.attr("type") shouldBe "submit"
      }
      "have the class 'button'" in {
        button.hasClass("button") shouldBe true
      }
    }
  }
  "Withdraw Date view with form with errors" should {
    lazy val doc = Jsoup.parse(view.apply(withdrawDateForModelInError, "IP2014", "dormant").body)
    "display an error message for the input" in {
      doc.body().select("fieldset.form-field--error").size() shouldBe 1
    }
    "display an error summary message for the amount" in {
      doc.body().select("div.error-summary--show").size() shouldBe 1
    }
  }
}