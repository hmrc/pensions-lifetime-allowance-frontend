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

package views.pages.confirmation

import org.jsoup.Jsoup

import play.api.i18n.Messages.Implicits._
import testHelpers.ViewSpecHelpers.confirmation.confirmFP
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.CommonMessages
import views.html.pages.confirmation.{confirmFP => views}

class confirmFPSpec extends CommonViewSpecHelper with confirmFP with CommonMessages {

  "The FP 2016 page" should {

    implicit lazy val view = views()
    implicit lazy val doc = Jsoup.parse(view.body)

      "have the correct title" in {
        doc.title() shouldBe {
          plaConfirmFP16Title
        }
      }

      "have a H1 tag that" should {

        lazy val h1Tag = doc.select("H1")

        s"have the page heading '$plaConfirmFP16PageHeading'" in {
          h1Tag.text shouldBe {
            plaConfirmFP16PageHeading
          }
        }

        "have the heading-large class" in {
          h1Tag.hasClass("heading-large") shouldBe true
        }
      }

      s"have the first declaration paragraph of $plaConfirmFP16Para1" in {
        doc.body.select("p").get(0).text shouldBe plaConfirmFP16Para1
      }

      s"have the second declaration paragraph of $plaConfirmFP16Para2" in {
        doc.body.select("p").get(1).text shouldBe plaConfirmFP16Para2
      }

      "has a list of bullet points that" should {

        s"have the first bullet of $plaConfirmFP16Bullet1" in {
          doc.body.select("ul li").get(0).text shouldBe plaConfirmFP16Bullet1
        }

        s"have the second bullet of $plaConfirmFP16Bullet2" in {
          doc.body.select("ul li").get(1).text shouldBe plaConfirmFP16Bullet2
        }

        s"have the third bullet of $plaConfirmFP16Bullet3" in {
          doc.body.select("ul li").get(2).text shouldBe plaConfirmFP16Bullet3
        }
      }

      s"have the first declaration paragraph of $plaConfirmFP16Para3" in {
        doc.body.select("p").get(2).text shouldBe plaConfirmFP16Para3
      }

      "have help text that" should {

        lazy val helpText = doc.body.select("summary").text
        lazy val helpLink = doc.body.select("details summary")

        s"have the label text $plaConfirmFP16Help" in {
          helpText shouldBe plaConfirmFP16Help
        }

        "have the class 'visuallyhidden'" in {
          helpLink.select("span").size shouldBe 1
        }
      }

      "has help paragraphs which" should {

        lazy val accordionText = doc.body.select("details div p")

        s"have the text $plaConfirmFP16HiddenPara1" in {
          accordionText.get(1).text shouldBe plaConfirmFP16HiddenPara1
        }

        s"have the text $plaConfirmFP16HiddenPara2" in {
          accordionText.get(2).text shouldBe s"$plaConfirmFP16HiddenPara2 $plaConfirmFP16HiddenParaLinkText ($plaBaseNewWindow)"
        }

        s"have the class 'visuallyhidden'" in {
          doc.body.select("details div").size shouldBe 1
        }
      }

      "have an external help link within the help paragraphs which" should {

        lazy val accordionLink = doc.select("a#fp16-help-link")

        s"have the link text $plaConfirmFP16HiddenParaLinkText ($plaBaseNewWindow)" in {
          doc.body.select("details div p a").get(0).text shouldBe s"$plaConfirmFP16HiddenParaLinkText ($plaBaseNewWindow)"
        }

        "have the correct link destination" in {
          accordionLink.attr("href") shouldBe "https://www.gov.uk/hmrc-internal-manuals/pensions-tax-manual/ptm093100"
        }

        "have the correct link ID" in {
          accordionLink.attr("id") shouldBe "fp16-help-link"
        }

        "should specify external link" in {
          accordionLink.attr("rel") shouldBe "external"
        }

        "should specify a blank target" in {
          accordionLink.attr("target") shouldBe "_blank"
        }
      }

      "have declaration text that" should {

        lazy val declarationText = doc.body.select("div p strong").text
        lazy val declarationStrong = doc.select("div p strong")
        lazy val declarationElement = doc.select("div")

        s"contain the text $plaConfirmFP16Declaration" in {
          declarationText shouldBe plaConfirmFP16Declaration
        }

        "have the panel-indent class" in {
          declarationElement.hasClass("panel-indent") shouldBe true
        }

        "have the bold-small class" in {
          declarationStrong.hasClass("bold-small") shouldBe true
        }
      }

      "have a continue button that" should {

        lazy val submitButton = doc.select("button")

        s"have the button text '$plaBaseSubmitApplication'" in {
          submitButton.text shouldBe plaBaseSubmitApplication
        }

        "be of type submit" in {
          submitButton.attr("type") shouldBe "submit"
        }

        "have the class 'button'" in {
          submitButton.hasClass("button") shouldBe true
        }
      }
  }
}




