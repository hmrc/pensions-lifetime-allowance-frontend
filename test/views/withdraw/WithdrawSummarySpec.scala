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

import config.FrontendAppConfig
import controllers.routes
import models.{AmendDisplayModel, AmendDisplayRowModel, AmendDisplaySectionModel}
import models.amendModels.AmendProtectionModel
import org.jsoup.Jsoup
import play.api.i18n.Messages.Implicits._
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.withdraw.WithdrawSummarySpecMessages
import uk.gov.hmrc.play.test.UnitSpec
import views.html.pages.withdraw.{withdrawSummary => views}

class WithdrawSummarySpec extends CommonViewSpecHelper with WithdrawSummarySpecMessages  {

  "Withdraw Summary view" when {

    val tstPensionContributionNoPsoDisplaySections = Seq(
      AmendDisplaySectionModel("PensionsTakenBefore", Seq(
        AmendDisplayRowModel("YesNo", Some(controllers.routes.AmendsController.amendPensionsTakenBefore("ip2014", "active")), None, "No"))
      ))
    val model = AmendDisplayModel("IP2014", amended = true, tstPensionContributionNoPsoDisplaySections, psoAdded = false, Seq(), "£1,100,000")
    lazy val view = views(model)
    lazy val doc = Jsoup.parse(view.body)

    s"have a title ${"pla.withdraw.protection.title"}" in {
      doc.title() shouldBe plaWithdrawTitle
    }

    "have a navigation tag that" should {

      "have a size of 1" in {
        doc.select("nav").size() shouldBe 1
      }

      "have a 'ul' tag with class name of 'breadcrumb-nav__list'" in {
        doc.select("ul").hasClass("breadcrumb-nav__list") shouldBe true
      }

      "have a 'ul' tag that has a size of 1" in {
        doc.select("ul").size() shouldBe 1
      }

      "have 'li' tag with class name of 'breadcrumb-nav__item'" in {
        doc.select("li").get(0).hasClass("breadcrumb-nav__item") shouldBe true
      }

      "have 'a' tag with id of 'account-home-breadcrumb-link'" in {
        doc.select("a").get(0).id() shouldBe "account-home-breadcrumb-link"
      }

      s"have text of $plaExistingProtectionsBreadcrumbPTAHome with id of 'account-home-breadcrumb-link'" in {
        doc.getElementById("account-home-breadcrumb-link").text() shouldBe plaExistingProtectionsBreadcrumbPTAHome
      }

      "have 'a' tag with id of 'account-home-breadcrumb-link' has a href" in {
        doc.getElementById("account-home-breadcrumb-link").attr("href") shouldBe FrontendAppConfig.ptaFrontendUrl
      }

      "have 'a' tag with id of 'existing-protections-breadcrumb-link'" in {
        doc.select("a").get(1).id() shouldBe "existing-protections-breadcrumb-link"
      }

      s"have text of $plaExistingProtectionsPageBreadcrumb with id of 'existing-protections-breadcrumb-link'" in {
        doc.getElementById("existing-protections-breadcrumb-link").text() shouldBe plaExistingProtectionsPageBreadcrumb
      }

      "have 'a' tag with id of 'existing-protections-breadcrumb-link' has a href" in {
        doc.getElementById("existing-protections-breadcrumb-link").attr("href") shouldBe controllers.routes.ReadProtectionsController.currentProtections.url
      }

      "have 'li' tag with class of 'breadcrumb-nav__item' that has text" in {
        doc.select("li.breadcrumb-nav__item").get(2).text() shouldBe plaExistingProtectionsPageBreadcrumb
      }
    }

    "have 'li' tag with class of heading-large that has text" in {
      doc.select("h1").text() shouldBe plaWithdrawHeadingIP2014
    }

    "have a table that" should {

      "heading that has a text" in {
        doc.select("h2").text() shouldBe plaWithdrawSummaryHeading
      }

      "table cell with classname 'total-font summary-text'" in {
        doc.getElementById("total-message").attr("class") shouldBe "total-font summary-text"
      }

      "table cell with classname 'total-font summary-data'" in {
        doc.getElementById("total-amount").attr("class") shouldBe "total-font summary-data"
      }

      s"have text of $plaWithdrawTotalValueOfPensions with id of 'total-message'" in {
        doc.getElementById("total-message").text() shouldBe plaWithdrawTotalValueOfPensions

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

  }

}
