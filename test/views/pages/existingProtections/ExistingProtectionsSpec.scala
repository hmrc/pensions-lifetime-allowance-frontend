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

package views.pages.existingProtections

import models.{ExistingProtectionDisplayModel, ExistingProtectionsDisplayModel}
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.mvc.Call
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.existingProtections.ExistingProtections
import views.html.pages.existingProtections.existingProtections
import scala.concurrent.ExecutionContext.Implicits.global

class ExistingProtectionsSpec extends CommonViewSpecHelper with ExistingProtections {

  "The Existing Protections page" should {

    lazy val protectionModel = ExistingProtectionDisplayModel("IP2016", "active", Some(Call("", "", "")), Some(tstPSACheckRef), "protectionReference", Some("250.00"), Some(""))
    lazy val protectionModel2 = ExistingProtectionDisplayModel("IP2014", "dormant", Some(Call("", "", "")), Some(""), "protectionReference", Some(""), Some(""))
    lazy val tstPSACheckRef = "PSA33456789"

    lazy val tstProtectionDisplayModelActive1 = ExistingProtectionDisplayModel("FP2016","active", Some(controllers.routes.AmendsController.amendsSummary("ip2014", "active")), Some(tstPSACheckRef), Messages("pla.protection.protectionReference"), Some("100.00"), None, None)
    lazy val tstProtectionDisplayModelDormant1 = ExistingProtectionDisplayModel("IP2014", "dormant", Some(controllers.routes.AmendsController.amendsSummary("fp2016", "dormant")), Some(tstPSACheckRef), Messages("pla.protection.protectionReference"), Some("100.00"), Some(""), None)
    lazy val tstProtectionDisplayModelEmpty1 = ExistingProtectionDisplayModel("", "", None, None, "", None, None, None)

    lazy val model = ExistingProtectionsDisplayModel(Some(protectionModel), List(tstProtectionDisplayModelActive1))
    lazy val view = application.injector.instanceOf[existingProtections]
    lazy val doc = Jsoup.parse(view.apply(model).body)

    lazy val model2 = ExistingProtectionsDisplayModel(None, List(tstProtectionDisplayModelEmpty1))
    lazy val view2 = application.injector.instanceOf[existingProtections]
    lazy val doc2 = Jsoup.parse(view2.apply(model2).body)

    lazy val model2b = ExistingProtectionsDisplayModel(Some(protectionModel2), List(tstProtectionDisplayModelDormant1))
    lazy val view2b = application.injector.instanceOf[existingProtections]
    lazy val doc2b = Jsoup.parse(view2b.apply(model2b).body)

    lazy val model3 = ExistingProtectionsDisplayModel(Some(protectionModel), List.empty)
    lazy val view3 = application.injector.instanceOf[existingProtections]
    lazy val doc3 = Jsoup.parse(view3.apply(model3).body)



    "have the correct title" in {
      doc.title() shouldBe plaExistingProtectionsTitleNew
    }

    "have the correct heading which" should {

      lazy val h1Tag = doc.select("H1")

      s"contain the text '$plaExistingProtectionsPageHeading'" in {
        h1Tag.text shouldBe plaExistingProtectionsPageHeading
      }
    }

    "have a protections section display which if no active protections are present" should {

      "have the id" in {
        doc2.select("#listProtections").size() shouldBe 1
      }

      "have the message" in {
        doc2.select("div p").get(0).text shouldBe plaExistingProtectionsNoActiveProtections
      }
    }


    "have a protections section display which if active protections are present" should {

      "display the protection details section" in {
        doc.select("div div").hasClass("protection-detail") shouldBe true
      }

      "contain the correct data" in {
        doc.select("#activeProtection").text shouldBe "Individual protection 2016"
        doc.select("#activeProtectedAmountContent").text shouldBe "250.00"
      }
    }


    "have another protections list which if no other protections are present" should {

      "have the message" in {
        doc3.select("#listProtections > p:nth-child(2)").text shouldBe plaExistingProtectionsNoOtherProtections
      }
    }


    "have another protections list which if other protections are present" should {

      "display the correct html including the print link" in {
        doc2b.select("a#printLink").attr("href") shouldBe "/protect-your-lifetime-allowance/print-protection"
      }

      "contain the data" in {
        doc2b.select("#inactiveProtection1").text shouldBe "Individual protection 2014"
        doc2b.select("#inactiveProtectedAmount1Content").text shouldBe "100.00"
      }
    }


    "have a view details about other protections and how to apply link which" should {

      lazy val link = doc.select("#main-content > div > div > p > a")

      s"have a link destination of view details about other protections and how to apply" in {
        link.attr("href") shouldBe plaExistingProtectionsHref
      }

      s"have the link text $plaExistingProtectionsLinkText" in {
       link.text shouldBe plaExistingProtectionsLinkText
      }

      s"have a question of ${"pla.existingProtections.other.protections.link"}" in {
      doc.select("#main-content > div > div > p").text shouldBe plaExistingProtectionOtherText
        }
      }
    }
}


