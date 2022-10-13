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

package views.pages.result

import org.jsoup.Jsoup
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.result.ManualCorrespondenceNeeded
import views.html.pages.result.manualCorrespondenceNeeded

class ManualCorrespondenceNeededSpec extends CommonViewSpecHelper with ManualCorrespondenceNeeded {

  "The Manual Correspondence needed page" should {

    lazy val view = application.injector.instanceOf[manualCorrespondenceNeeded]
    lazy val doc = Jsoup.parse(view().body)

    "have the correct title" in {
      doc.title() shouldBe plaMcNeededTitle
    }

    "have the first heading which" should {

      lazy val h1Tag = doc.select("h1.govuk-heading-xl")

      s"have the heading text $plaMcNeededPageHeading" in {
      h1Tag.text shouldBe plaMcNeededPageHeading
      }

      "have data metrics" in {
        h1Tag.attr("data-metrics") shouldBe "result:manual-correspondence:mc-needed"
      }
    }

    "have the the first paragraph which" should {

      lazy val p1 = doc.select("div div p").get(0)

      s"have the text $plaMcNeededNeedToSpeakToYou" in {
        p1.text shouldBe plaMcNeededNeedToSpeakToYou
      }

    }

    "have the second heading which" should {

      lazy val h2Tag0 = doc.select("div h2").get(0)

      s"have the heading text $plaMcNeededHowToFix" in {
        h2Tag0.text shouldBe plaMcNeededHowToFix
      }
    }

    "should contain a list of instructions which" should {

      lazy val listOption = doc.select("#main-content > div > div > ol")

      s"include the list option $plaMcNeededTelephone" in {
        listOption.select("li:nth-child(1)").text shouldBe plaMcNeededTelephone
      }

      s"include the list option $plaMcNeededSayCantLogIn" in {
        listOption.select("li:nth-child(2)").text shouldBe plaMcNeededSayCantLogIn
      }

      s"include the list option $plaMcNeededSayYes" in {
        listOption.select("li:nth-child(3)").text shouldBe plaMcNeededSayYes
      }

      s"include the list option $plaMcNeededAdvisorHelp" in {
        listOption.select("li:nth-child(4)").text shouldBe plaMcNeededAdvisorHelp
      }

      s"include the list option $plaMcNeededTellAdvisor" in {
        listOption.select("li:nth-child(5)").text shouldBe plaMcNeededTellAdvisor
      }
    }

    "have the third heading which" should {

      lazy val h2Tag1 = doc.select("h2").get(1)

      s"have the heading text $plaMcNeededOtherContact" in {
        h2Tag1.text shouldBe plaMcNeededOtherContact
      }
    }

    "should contain a list of other contact options which" should {

      lazy val listOption = doc.select("#main-content > div > div > ul")

      s"include the list option $plaMcNeededTextphone" in {
        listOption.select("li:nth-child(1)").text shouldBe plaMcNeededTextphone
      }

      s"include the list option $plaMcNeededNonUKPhone" in {
        listOption.select("li:nth-child(2)").text shouldBe plaMcNeededNonUKPhone
      }
    }

    "have a set of paragraphs which" should {

      lazy val pList = doc.select("div p")

      s"have the text $plaMcNeededLinesOpen" in {
        pList.get(1).text shouldBe plaMcNeededLinesOpen
      }

      s"have the text $plaMcNeededLinesClosed" in {
        pList.get(2).text shouldBe plaMcNeededLinesClosed
      }

      s"have the text $plaMcNeededLinesBusy" in {
        pList.get(3).text shouldBe plaMcNeededLinesBusy
      }
    }

    "have a link to call charges which" should {

      lazy val link = doc.select("div p a")

      s"have a link destination of callCharges" in {
        link.attr("href") shouldBe common.Links.callCharges
      }

      s"have the link text $plaMcNeededcallCharges ($plaBaseNewTab)" in {
        link.text shouldBe s"$plaMcNeededcallCharges ($plaBaseNewTab)"
      }

      "should specify a blank target" in {
        link.attr("target") shouldBe "_blank"
      }
    }
  }
}