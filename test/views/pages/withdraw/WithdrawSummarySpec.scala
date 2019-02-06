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

import config.FrontendAppConfig
import controllers.routes
import models.{AmendDisplayModel, AmendDisplayRowModel, AmendDisplaySectionModel}
import org.jsoup.Jsoup
import play.api.i18n.Messages.Implicits._
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.withdraw.WithdrawSummarySpecMessages
import views.html.pages.withdraw.{withdrawSummary => views}

class WithdrawSummarySpec extends CommonViewSpecHelper with WithdrawSummarySpecMessages {

  "Withdraw Summary view" which {

    "has been provided data" should {

      lazy val tstPensionContributionNoPsoDisplaySections = Seq(
        AmendDisplaySectionModel("PensionsTakenBefore", Seq(
          AmendDisplayRowModel("YesNo", Some(controllers.routes.AmendsController.amendPensionsTakenBefore("ip2014", "active")), None, "No"))
        ))
      lazy val model = AmendDisplayModel("IP2014", amended = true, tstPensionContributionNoPsoDisplaySections, psoAdded = false, Seq(), "£1,100,000")
      lazy val view = views(model)
      lazy val doc = Jsoup.parse(view.body)

      s"have a title ${"pla.withdraw.protection.title"}" in {
        doc.title() shouldBe plaWithdrawTitle
      }

      "have 'li' tag with class of heading-large that has text" in {
        doc.select("h1").text() shouldBe plaWithdrawHeadingIP2014
      }

      "have a table that" should {

        "heading that has a text" in {
          doc.select("h2").text() shouldBe plaWithdrawSummaryHeading
        }

        s"have text of $plaWithdrawTotalValueOfPensions with id of 'total-message'" in {
          doc.getElementById("total-message").text() shouldBe plaWithdrawTotalValueOfPensions
        }

        s"have text of $model.totalAmount with id of 'total-amount'" in {
          doc.getElementById("total-amount").text() shouldBe model.totalAmount
        }

      }

      "have a continue button that" should {
        lazy val button = doc.getElementById("continue-button")

        s"have text of ${"pla.withdraw.protection.continue.title"}" in {
          button.text() shouldBe plaWithdrawSummaryContinue
        }

        "have a class of button" in {
          button.className() shouldBe "button"
        }

        s"have a href" in {
          button.attr("href") shouldBe routes.WithdrawProtectionController.withdrawImplications().url
        }
      }

      "have summary text" in {
        doc.select("td").get(0).text() shouldBe plaWithdrawSummaryText
      }

      "have summary data text" in {
        doc.select("td").get(1).text() shouldBe plaBaseNo
      }

      "have a summary-link href" in {
        doc.select("td.summary-link > a").attr("href") should include(plaWithdrawSummaryLink)

      }

      "have a summary-link text" in {
        doc.select("td.summary-link").text() shouldBe plaWithdrawSummaryLinkText
      }

    }
  }

  "has no provided data" should {

    val tstPensionContributionNoPsoDisplaySections = Seq(
    )
    val model = AmendDisplayModel("IP2014", amended = true, tstPensionContributionNoPsoDisplaySections, psoAdded = false, Seq(), "£1,100,000")
    lazy val view = views(model)
    lazy val doc = Jsoup.parse(view.body)

    "have 2 tr tags" in {
      doc.select("tr").size() shouldBe 2
    }

  }

}
