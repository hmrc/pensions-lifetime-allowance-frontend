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

package views.pages.lookup

import models.PSALookupRequest
import org.jsoup.Jsoup
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.lookup.PsaLookupNotFoundSpecMessages
import views.html.pages.lookup.psa_lookup_not_found_results

class PsaLookupNotFoundResultsViewSpec extends CommonViewSpecHelper with PsaLookupNotFoundSpecMessages {

  "The Psa Lookup Not Found Print view" should {
    val psaLookupRequest = PSALookupRequest("check", Some("ref"))
    lazy val view        = application.injector.instanceOf[psa_lookup_not_found_results]
    lazy val doc         = Jsoup.parse(view.apply(psaLookupRequest, "timestamp").body)

    "have the correct header text" in {
      doc.select("h1.govuk-heading-xl").text() shouldBe checkDetailsText
    }

    "include a table".which {

      "has the correct first row".which {
        lazy val row = doc.select("#main-content > div > div > table > tbody > tr:nth-child(1)")

        "contains the correct first element text" in {
          row.select("th").get(0).text shouldBe tableRowOneElementOneText
        }

        "contains the correct second element text" in {
          row.select("td").get(0).text shouldBe tableRowOneElementTwoText
        }
      }

      "has the correct second row".which {
        lazy val row = doc.select("#main-content > div > div > table > tbody > tr:nth-child(2)")

        "contains the correct first element text" in {
          row.select("th").get(0).text shouldBe tableRowTwoElementOneText
        }

        "contains the correct second element text" in {
          row.select("td").get(0).text shouldBe tableRowTwoElementTwoText
        }
      }

      "has the correct third row".which {
        lazy val row = doc.select("#main-content > div > div > table > tbody > tr:nth-child(3)")

        "contains the correct first element text" in {
          row.select("th").get(0).text shouldBe tableRowThreeElementOneText
        }

        "contains the correct second element text" in {
          row.select("td").get(0).text shouldBe tableRowThreeElementTwoText
        }
      }
    }

    "have a paragraph describing details of the failed lookup" in {
      doc.select("#main-content > div > div > p:nth-child(3)").text() shouldBe detailsText
    }

    "have a paragraph describing possible causes" in {
      doc.select("#main-content > div > div > p:nth-child(4)").text() shouldBe causesText
    }

    "include a list".which {

      "has the first cause listed" in {
        doc.select("#main-content > div > div > ul > li:nth-child(1)").text() shouldBe causeOneText
      }

      "has the second cause listed" in {
        doc.select("#main-content > div > div > ul > li:nth-child(2)").text() shouldBe causeTwoText
      }
    }

    "have a paragraph describing a possible resolution" in {
      doc.select("#main-content > div > div > p:nth-child(6)").text() shouldBe suggestionsText
    }

    "have a save pdf link".which {

      "has the correct text" in {
        doc.getElementById("printLink").text() shouldBe pdfLinkText
      }

      "has the data module to print page" in {
        doc.getElementById("printLink").attr("data-module") shouldBe "hmrc-print-link"
      }
    }

    "have a start again link".which {

      "has the correct text" in {
        doc.getElementsByClass("govuk-button govuk-button--start").text() shouldBe startAgainText
      }

      "links to the start point" in {
        doc
          .getElementsByClass("govuk-button govuk-button--start")
          .attr("href") shouldBe controllers.routes.LookupController.redirectToStart.url
      }
    }
  }

}
