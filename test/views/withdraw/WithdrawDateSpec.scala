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
import forms.WithdrawDateForm._
import org.jsoup.Jsoup
import play.api.i18n.Messages.Implicits._
import play.api.libs.json.Json
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.withdraw.WithdrawDateSpecMessages
import views.html.pages.withdraw.{withdrawDate => views}

class WithdrawDateSpec extends CommonViewSpecHelper with WithdrawDateSpecMessages {

  "Withdraw Date view with form without errors" when {

    val postData = Json.obj(
      "withdrawDay" -> "19",
      "withdrawMonth" -> "1",
      "withdrawYear" -> "2018"
    )
    val WithdrawDateForm = withdrawDateForm.bind(postData)
    lazy val form = WithdrawDateForm
    lazy val view = views(WithdrawDateForm, "IP2014", "dormant")
    lazy val doc = Jsoup.parse(view.body)

    s"have a title $plaWithdrawDateInputTitle" in {
      doc.title() shouldBe plaWithdrawDateInputTitle
    }

    s"have the question of the page $plaWithdrawDateInputTitle" in {
      doc.select("h1.heading-large").text shouldEqual plaWithdrawDateInputTitle
    }

    s"have a back link with text back " in {
      doc.select("a.back-link").text() shouldBe "Back"
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

    "display an error summary message for the day2" in {
      doc.body().select("fieldset.form-field--error").size() shouldBe 0
    }
    "display an error notification message for the day2" in {
      doc.body().select("div.error-summary--show").size() shouldBe 0
    }

    "have a submit button that" should {
      lazy val button = doc.getElementById("submit")

      s"have text of $plaWithdrawDateInputFormContinue" in {
        button.text() shouldBe plaWithdrawDateInputFormContinue
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
    val postData = Json.obj(
      "withdrawDay" -> "",
      "withdrawMonth" -> "",
      "withdrawYear" -> "2018"
    )
    val WithdrawDateForm = withdrawDateForm.bind(postData)
    lazy val form = WithdrawDateForm
    lazy val view = views(WithdrawDateForm, "IP2014", "dormant")
    lazy val doc = Jsoup.parse(view.body)

    "display an error message for the input" in {
      doc.body().select("fieldset.form-field--error").size() shouldBe 1
    }
    "display an error summary message for the amount" in {
      doc.body().select("div.error-summary--show").size() shouldBe 1
    }
  }


}
