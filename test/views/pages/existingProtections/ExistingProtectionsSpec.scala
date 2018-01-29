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
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.existingProtections.ExistingProtections
import views.html.pages.existingProtections.{existingProtections => views}

class ExistingProtectionsSpec extends CommonViewSpecHelper with ExistingProtections {

  "The Existing Protections page" should {

    lazy val protectionModel = ExistingProtectionDisplayModel("IP2016", "active", Some(Call("", "", "")), Some(""), "protectionReference", Some("100.00"), Some(""))
    lazy val protectionModel2 = ExistingProtectionDisplayModel("IP2014", "dormant", Some(Call("", "", "")), Some(""), "protectionReference", Some(""), Some(""))
    lazy val protectionModel3 = ExistingProtectionDisplayModel("", "", Some(Call("", "", "")), Some(""), "", Some(""), Some(""))
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

    lazy val tstProtectionDisplayModelActive1 = ExistingProtectionDisplayModel(
      protectionType = "FP2016",
      status = "active",
      amendCall = Some(controllers.routes.AmendsController.amendsSummary("fp2016", "active")),
      psaCheckReference = Some(tstPSACheckRef),
      protectionReference = Messages("pla.protection.protectionReference"),
      protectedAmount = Some("100"),
      certificateDate = Some(""),
      withdrawnDate =None)

    lazy val tstProtectionDisplayModelEmpty1 = ExistingProtectionDisplayModel(
      protectionType = "",
      status = "",
      amendCall = Some(controllers.routes.AmendsController.amendsSummary("", "")),
      psaCheckReference = Some(""),
      protectionReference = Messages(""),
      protectedAmount = None,
      certificateDate = None,
      withdrawnDate =None)

    lazy val model = ExistingProtectionsDisplayModel(Some(protectionModel), List(tstProtectionDisplayModelDormant1))
    lazy val view = views(model)
    lazy val doc = Jsoup.parse(view.body)

    lazy val model2 = ExistingProtectionsDisplayModel(Some(protectionModel2), List(tstProtectionDisplayModelActive1))
    lazy val view2 = views(model2)
    lazy val doc2 = Jsoup.parse(view2.body)

    lazy val model3 = ExistingProtectionsDisplayModel(Some(protectionModel3), List(tstProtectionDisplayModelEmpty1))
    lazy val view3 = views(model3)
    lazy val doc3 = Jsoup.parse(view3.body)



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
    }


    "have a protections section display which" should {

      "have the id" in {
        doc2.select("div").get(2).attr("id") shouldBe "listProtections"
      }

      "have the message" in {
        doc2.select("div").get(2).text shouldBe plaExistingProtectionsNoActiveProtections
      }
    }

    "have another protections list which" should {

      "have the message" in {
        doc3.select("div").get(2).text shouldBe plaExistingProtectionsNoOtherProtections
      }
    }

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


