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

package views.pages.existingProtections

import models.display.{
  ExistingInactiveProtectionsByType,
  ExistingInactiveProtectionsDisplayModel,
  ExistingProtectionDisplayModel,
  ExistingProtectionsDisplayModel
}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.Call
import testHelpers.CommonViewSpecHelper
import testHelpers.messages.existingProtections.ExistingProtectionsViewMessages
import views.html.pages.existingProtections.existingProtections

class ExistingProtectionsViewSpec extends CommonViewSpecHelper with ExistingProtectionsViewMessages {

  val view: existingProtections = inject[existingProtections]

  val tstPSACheckRef = "PSA33456789"

  val protectionModel = ExistingProtectionDisplayModel(
    "IP2016",
    "active",
    Some(Call("", "", "")),
    Some(tstPSACheckRef),
    "protectionReference",
    Some("250.00"),
    Some("")
  )

  val protectionModel2 = ExistingProtectionDisplayModel(
    "IP2014",
    "dormant",
    Some(Call("", "", "")),
    Some(""),
    "protectionReference",
    Some(""),
    Some("")
  )

  val tstProtectionDisplayModelDormant1 = ExistingProtectionDisplayModel(
    "IP2014",
    "dormant",
    Some(controllers.routes.AmendsController.amendsSummary("fp2016", "dormant")),
    Some(tstPSACheckRef),
    Messages("pla.protection.protectionReference"),
    Some("100.00"),
    Some(""),
    None
  )

  val modelOnlyActive = ExistingProtectionsDisplayModel(
    inactiveProtections = ExistingInactiveProtectionsDisplayModel.empty,
    activeProtection = Some(protectionModel)
  )

  val docOnlyActive: Document = Jsoup.parse(view.apply(modelOnlyActive).body)

  val modelOnlyInactive = ExistingProtectionsDisplayModel(
    activeProtection = None,
    inactiveProtections = ExistingInactiveProtectionsDisplayModel(
      dormantProtections = ExistingInactiveProtectionsByType(
        Seq(
          "IP2014" -> List(
            tstProtectionDisplayModelDormant1
          )
        )
      ),
      withdrawnProtections = ExistingInactiveProtectionsByType.empty,
      unsuccessfulProtections = ExistingInactiveProtectionsByType.empty,
      rejectedProtections = ExistingInactiveProtectionsByType.empty,
      expiredProtections = ExistingInactiveProtectionsByType.empty
    )
  )

  val docOnlyInactive: Document = Jsoup.parse(view.apply(modelOnlyInactive).body)

  val modelActiveAndInactive = ExistingProtectionsDisplayModel(
    activeProtection = Some(protectionModel2),
    inactiveProtections = ExistingInactiveProtectionsDisplayModel(
      dormantProtections = ExistingInactiveProtectionsByType(
        Seq(
          "IP2014" -> List(
            tstProtectionDisplayModelDormant1
          )
        )
      ),
      withdrawnProtections = ExistingInactiveProtectionsByType.empty,
      unsuccessfulProtections = ExistingInactiveProtectionsByType.empty,
      rejectedProtections = ExistingInactiveProtectionsByType.empty,
      expiredProtections = ExistingInactiveProtectionsByType.empty
    )
  )

  val docActiveAndInactive: Document = Jsoup.parse(view.apply(modelActiveAndInactive).body)

  val modelNoProtections = ExistingProtectionsDisplayModel(
    activeProtection = None,
    inactiveProtections = ExistingInactiveProtectionsDisplayModel.empty
  )

  val docNoProtections: Document = Jsoup.parse(view.apply(modelNoProtections).body)

  "The Existing Protections page" should {

    "have the correct title" in {
      docOnlyActive.title() shouldBe plaExistingProtectionsTitleNew
    }

    "have the correct heading which" should {

      val h1Tag = docOnlyActive.select("H1")

      s"contain the text '$plaExistingProtectionsPageHeading'" in {
        h1Tag.text shouldBe plaExistingProtectionsPageHeading
      }
    }

    "have a protections section display which if no active protections are present" should {

      "have the id" in {
        docOnlyInactive.select("#listProtections").size() shouldBe 1
      }

      "have the message" in {
        docOnlyInactive.select("div p").get(0).text shouldBe plaExistingProtectionsNoActiveProtections
      }
    }

    "have a noProtections section display if no protections are present" should {

      "display the noProtections section" in {
        docNoProtections.select("#noProtections").text shouldBe plaExistingProtectionsNoProtections
      }

    }

    "have a protections section display which if active protections are present" should {

      "display the protection details section" in {
        docOnlyActive.select("div div").hasClass("protection-detail") shouldBe true
      }

      "contain the correct data" in {
        docOnlyActive
          .select("#listProtections > div > h3")
          .text shouldBe "Individual protection 2016 for active protection"
        docOnlyActive.select("#activeProtectedAmountContent").text shouldBe "250.00"
      }
    }

    "have another protections list which if no other protections are present" should {

      "have the message" in {
        docOnlyActive.getElementById("noOtherProtections").text shouldBe plaExistingProtectionsNoOtherProtections
      }
    }

    "have another protections list which if other protections are present" should {

      "display the correct html including the print link" in {
        docActiveAndInactive
          .select("a#printLink")
          .attr("href") shouldBe "/check-your-pension-protections-and-enhancements/print-protection"
      }

      "contain the data" in {
        docActiveAndInactive
          .select("#listProtections > h3")
          .text shouldBe "Individual protection 2014 for dormant protections"
        docActiveAndInactive.select("#dormantInactiveProtectedAmount1Content").text shouldBe "100.00"
      }
    }

    s"have a content for Existing Protections page" in {
      docOnlyActive.select("#activeProtectedAmountHeading").text shouldBe plaExistingProtectionsProtectedAmount
      docOnlyActive.select("#activeProtectionReferenceHeading").text shouldBe plaExistingProtectionsProtectionRef
      docOnlyActive.select("#activePSACheckRefHeading").text shouldBe plaExistingProtectionsPSARef
    }

    s"have a content for Existing Protections page for inactive protection" in {
      docActiveAndInactive
        .select("#dormantInactiveProtectedAmount1Heading")
        .text shouldBe plaExistingProtectionsProtectedAmount
      docActiveAndInactive
        .select("#dormantInactiveProtectionReference1Heading")
        .text shouldBe plaExistingProtectionsProtectionRef
      docActiveAndInactive.select("#dormantInactivePSACheckRef1Heading").text shouldBe plaExistingProtectionsPSARef
    }

    "have a view details about taking higher tax-free lump sums with protected allowances and the link which" should {
      val link = docOnlyActive.select("#main-content > div > div > p > a")

      s"have a link destination about taking higher tax-free lump sums with protected allowances" in {
        link.attr("href") shouldBe plaExistingProtectionsHref2016ShutterEnabled
      }

      s"have the link text $plaExistingProtectionsLinkText2016ShutterEnabled" in {
        link.text shouldBe plaExistingProtectionsLinkText2016ShutterEnabled
      }

      s"have a question of ${"pla.existingProtections.other.protections.link_2016ShutterEnabled"}" in {
        docOnlyActive
          .select("#main-content > div > div > p")
          .text shouldBe plaExistingProtectionOtherText2016ShutterEnabled
      }
    }
  }

}
