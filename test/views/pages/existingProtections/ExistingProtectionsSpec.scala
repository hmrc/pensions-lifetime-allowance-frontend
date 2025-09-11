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

import config.FrontendAppConfig
import models.{ExistingInactiveProtectionsDisplayModel, ExistingProtectionDisplayModel, ExistingProtectionsDisplayModel}
import org.jsoup.Jsoup
import org.mockito.Mockito.when
import play.api.i18n.Messages
import play.api.inject
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Call
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.existingProtections.ExistingProtections
import views.html.pages.existingProtections.existingProtections

import scala.collection.SeqMap

class ExistingProtectionsSpec extends CommonViewSpecHelper with ExistingProtections {

  "The Existing Protections page" should {

    lazy val protectionModel = ExistingProtectionDisplayModel(
      "IP2016",
      "active",
      Some(Call("", "", "")),
      Some(tstPSACheckRef),
      "protectionReference",
      Some("250.00"),
      Some("")
    )
    lazy val protectionModel2 = ExistingProtectionDisplayModel(
      "IP2014",
      "dormant",
      Some(Call("", "", "")),
      Some(""),
      "protectionReference",
      Some(""),
      Some("")
    )
    lazy val tstPSACheckRef = "PSA33456789"

    lazy val tstProtectionDisplayModelDormant1 = ExistingProtectionDisplayModel(
      "IP2014",
      "dormant",
      Some(controllers.routes.AmendsController.amendsSummary("fp2016", "dormant")),
      Some(tstPSACheckRef),
      Messages("pla.protection.protectionReference"),
      Some("100.00"),
      Some(""),
      None
    )

    val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

    val application = new GuiceApplicationBuilder()
      .configure("metrics.enabled" -> false)
      .overrides(inject.bind[FrontendAppConfig].toInstance(mockAppConfig))
      .build()

    lazy val modelOnlyActive = ExistingProtectionsDisplayModel(
      inactiveProtections = None,
      activeProtection = Some(protectionModel),
    )
    lazy val viewOnlyActive = application.injector.instanceOf[existingProtections]
    lazy val docOnlyActive = Jsoup.parse(viewOnlyActive.apply(modelOnlyActive).body)

    lazy val modelOnlyInactive = ExistingProtectionsDisplayModel(
      activeProtection = None,
      inactiveProtections = Some(ExistingInactiveProtectionsDisplayModel(
        dormantProtections = SeqMap(
          "IP2014" -> List(
            tstProtectionDisplayModelDormant1,
          ),
        ),
        withdrawnProtections = SeqMap.empty,
        unsuccessfulProtections = SeqMap.empty,
        rejectedProtections = SeqMap.empty,
        expiredProtections = SeqMap.empty,
      )),
    )
    lazy val viewOnlyInactive = application.injector.instanceOf[existingProtections]
    lazy val docOnlyInactive = Jsoup.parse(viewOnlyInactive.apply(modelOnlyInactive).body)

    lazy val modelActiveAndInactive = ExistingProtectionsDisplayModel(
      activeProtection = Some(protectionModel2),
      inactiveProtections = Some(ExistingInactiveProtectionsDisplayModel(
        dormantProtections = SeqMap(
          "IP2014" -> List(
            tstProtectionDisplayModelDormant1
          )
        ),
        withdrawnProtections = SeqMap.empty,
        unsuccessfulProtections = SeqMap.empty,
        rejectedProtections = SeqMap.empty,
        expiredProtections = SeqMap.empty
      ))
    )
    lazy val viewActiveAndInactive = application.injector.instanceOf[existingProtections]
    lazy val docActiveAndInactive = Jsoup.parse(viewActiveAndInactive.apply(modelActiveAndInactive).body)

    lazy val modelNoProtections = ExistingProtectionsDisplayModel(
      activeProtection = None,
      inactiveProtections = None,
    )
    lazy val viewNoProtections = application.injector.instanceOf[existingProtections]
    lazy val docNoProtections = Jsoup.parse(viewNoProtections.apply(modelNoProtections).body)

    "have the correct title" in {
      docOnlyActive.title() shouldBe plaExistingProtectionsTitleNew
    }

    "have the correct heading which" should {

      lazy val h1Tag = docOnlyActive.select("H1")

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

    "have a protections section display which if active protections are present" should {

      "display the protection details section" in {
        docOnlyActive.select("div div").hasClass("protection-detail") shouldBe true
      }

      "contain the correct data" in {
        docOnlyActive.select("#listProtections > div > h3").text shouldBe "Individual protection 2016 for active protection"
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
        docActiveAndInactive.select("a#printLink").attr("href") shouldBe "/check-your-pension-protections/print-protection"
      }

      "contain the data" in {
        docActiveAndInactive.select("#listProtections > h3").text shouldBe "Individual protection 2014 for dormant protections"
        docActiveAndInactive.select("#dormantInactiveProtectedAmount1Content").text shouldBe "100.00"
      }
    }

    s"have a content for Existing Protections page when hip migration is enabled " in {
      when(mockAppConfig.hipMigrationEnabled).thenReturn(true)
      lazy val doc = Jsoup.parse(viewOnlyActive.apply(modelOnlyActive).body)
      doc.select("#activeProtectedAmountHeading").text shouldBe plaExistingProtectionsProtectedAmountHip
      doc.select("#activeProtectionReferenceHeading").text shouldBe plaExistingProtectionsProtectionRefHip
      doc.select("#activePSACheckRefHeading").text shouldBe plaExistingProtectionsPSARefHip
    }

    s"have a content for Existing Protections page for inactive protection when hip migration is enabled  " in {
      when(mockAppConfig.hipMigrationEnabled).thenReturn(true)
      lazy val doc2b = Jsoup.parse(viewActiveAndInactive.apply(modelActiveAndInactive).body)
      doc2b.select("#dormantInactiveProtectedAmount1Heading").text shouldBe plaExistingProtectionsProtectedAmountHip
      doc2b.select("#dormantInactiveProtectionReference1Heading").text shouldBe plaExistingProtectionsProtectionRefHip
      doc2b.select("#dormantInactivePSACheckRef1Heading").text shouldBe plaExistingProtectionsPSARefHip
    }

    s"have a content for Existing Protections page when hip migration is disabled " in {
      when(mockAppConfig.hipMigrationEnabled).thenReturn(false)
      lazy val doc = Jsoup.parse(viewOnlyActive.apply(modelOnlyActive).body)
      doc.select("#activeProtectedAmountHeading").text shouldBe plaExistingProtectionsProtectedAmount
      doc.select("#activeProtectionReferenceHeading").text shouldBe plaExistingProtectionsProtectionRef
      doc.select("#activePSACheckRefHeading").text shouldBe plaExistingProtectionsPSARef
    }

    "have a view details about taking higher tax-free lump sums with protected allowances and the link which" should {
      val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

      val application = new GuiceApplicationBuilder()
        .configure("metrics.enabled" -> false)
        .overrides(inject.bind[FrontendAppConfig].toInstance(mockAppConfig))
        .build()
      lazy val view = application.injector.instanceOf[existingProtections]
      lazy val doc = Jsoup.parse(view.apply(modelOnlyActive).body)

      lazy val link = doc.select("#main-content > div > div > p > a")

      s"have a link destination about taking higher tax-free lump sums with protected allowances" in {
        link.attr("href") shouldBe plaExistingProtectionsHref2016ShutterEnabled
      }

      s"have the link text $plaExistingProtectionsLinkText2016ShutterEnabled" in {
        link.text shouldBe plaExistingProtectionsLinkText2016ShutterEnabled
      }

      s"have a question of ${"pla.existingProtections.other.protections.link_2016ShutterEnabled"}" in {
        doc.select("#main-content > div > div > p").text shouldBe plaExistingProtectionOtherText2016ShutterEnabled
      }
    }
  }

}
