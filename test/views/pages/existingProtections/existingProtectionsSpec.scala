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

package views.pages.existingProtections

import config.FrontendAppConfig
import models.{ExistingProtectionDisplayModel, ExistingProtectionsDisplayModel}
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.mvc.Call
import testHelpers.ViewSpecHelpers.{CommonMessages, CommonViewSpecHelper}
import testHelpers.ViewSpecHelpers.existingProtections.existingProtections
import views.html.pages.existingProtections.{existingProtections => views}


class existingProtectionsSpec extends CommonViewSpecHelper with existingProtections with CommonMessages {

  "The Existing Protections page" should {

    lazy val protectionModel = ExistingProtectionDisplayModel("IP2016", "active", Some(Call("", "", "")), Some(""), "protectionReference", Some("100.00"), Some(""))
    lazy val tstPSACheckRef = "PSA33456789"
    lazy val tstProtectionDisplayModelDormant1 = ExistingProtectionDisplayModel(
      protectionType = "IP2014",
      status = "dormant",
      amendCall = Some(controllers.routes.AmendsController.amendsSummary("ip2014", "dormant")),
      psaCheckReference = Some(tstPSACheckRef),
      protectionReference = Messages("pla.protection.protectionReference"),
      protectedAmount = None,
      certificateDate = None,
      withdrawnDate =None)

    implicit lazy val model = ExistingProtectionsDisplayModel(Some(protectionModel), List(tstProtectionDisplayModelDormant1))
    implicit lazy val view = views(model)
    implicit lazy val doc = Jsoup.parse(view.body)

    "have the correct title" in {
      doc.title() shouldBe plaExistingProtectionsTitle
    }

    "have breadcrumb links which" should {

      "have the class" in {
        doc.select("nav").hasClass("breadcrumb-nav") shouldBe true
      }

      "have the link destination" in {
        doc.select("nav a").attr("href") shouldBe FrontendAppConfig.ptaFrontendUrl
      }

      "have the link message" in {
        doc.select("nav a").text shouldBe plaExistingProtectionsBreadcrumbPTAHome
      }

      "have the link id" in {
        doc.select("nav a").attr("id") shouldBe "account-home-breadcrumb-link"
      }

      "have the message" in {
        doc.select("nav ul li").get(2).text shouldBe plaExistingProtectionsPageBreadcrumb
      }
    }

    "have the correct heading which" should {

      lazy val h1Tag = doc.select("H1")

      s"contain the text '$plaExistingProtectionsPageHeading'" in {
        h1Tag.text shouldBe plaExistingProtectionsPageHeading
      }

      "have the class" in {
        h1Tag.hasClass("heading-xlarge") shouldBe true
      }
    }
      /*If No previous protections exist ...*/
//    "have a protections list which" should {
//
//      "have the id" in {
//        doc.select("div").get(2).attr("id") shouldBe "listProtections"
//      }
//
//      "have the message" in {
//        doc.select("div").get(2).text shouldBe plaExistingProtectionsNoActiveProtections
//      }
//    }
//
//    "have another protections list which" should {
//
//      "have the message" in {
//        doc.select("div").get(3).text shouldBe plaExistingProtectionsNoOtherProtections
//      }
//    }

    "have a back to home link which" should {

      lazy val link = doc.select("section a").get(1)

      s"have a link destination of account home" in {
        link.attr("href") shouldBe FrontendAppConfig.ptaFrontendUrl
      }

      s"have the link text $plaExistingProtectionsBackToHome" in {
        link.text shouldBe plaExistingProtectionsBackToHome
      }

      "have the correct id" in {
        link.attr("id") shouldBe "account-home-text-link"
      }
    }
  }
}

    //    <nav class="breadcrumb-nav breadcrumb-nav--slim">
    //      <ul class="breadcrumb-nav__list" role="breadcrumbs">
    //        <li class="breadcrumb-nav__item">
    //          <a href="@{FrontendAppConfig.ptaFrontendUrl}" id="account-home-breadcrumb-link">@Messages("pla.existingProtections.BreadcrumbPTAHome")</a>
    //        </li>
    //        <li class="breadcrumb-nav__item breadcrumb-nav__item--trail"></li>
    //        <li class="breadcrumb-nav__item">@Messages("pla.existingProtections.pageBreadcrumb")</li>
    //      </ul>
    //    </nav>
    //
    //      <div class="grid grid-2-3">
    //        <h1 class="heading-xlarge">@Messages("pla.existingProtections.pageHeading")</h1>
    //      </div>
    //
    //      <div class="grid-wrapper"></div>
    //
    //      <div id="listProtections">
    //        @if(protections.activeProtection.isEmpty) {
    //        <p>@Messages("pla.existingProtections.noActiveProtections")</p>
    //        } else {
    //    @protections.activeProtection.map { protection =>
    //      <div class="protection-detail">
    //        @activeExistingProtection(protection, "active", 1)
    //      </div>
    //    }
    //    <span class="inline-space"></span>
    //
    //    }
    //
    //    @if(protections.otherProtections.size == 0) {
    //    <p>@Messages("pla.existingProtections.noOtherProtections")</p>
    //    } else {
    //    @for((protection, index) <- protections.otherProtections.zipWithIndex) {
    //      <section>
    //        @inactiveExistingProtection(protection, "other", index + 1)
    //      </section>
    //    }
    //    <span class="inline-space"></span>
    //
    //    }
    //    </div>
    //
    //    <section>
    //      <a href="@{FrontendAppConfig.ptaFrontendUrl}" id="account-home-text-link">@Messages("pla.existingProtections.backToHome")</a>
    //    </section>


