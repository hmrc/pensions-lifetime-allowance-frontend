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

package views.lookup

import models.PSALookupRequest
import org.jsoup.Jsoup
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.lookup.PsaLookupNotFoundSpecMessages
import views.html.pages.lookup.psa_lookup_not_found_results
import play.api.i18n.Messages.Implicits._

class PsaLookupNotFoundResultsViewSpec extends CommonViewSpecHelper with PsaLookupNotFoundSpecMessages {

  "The Psa Lookup Not Found Print view" should {
    val model = PSALookupRequest("check", Some("ref"))
    lazy val view = psa_lookup_not_found_results(model, "timestamp")
    lazy val doc = Jsoup.parse(view.body)

    "have the correct header text" in {
      doc.select("h1").text() shouldBe checkDetailsText
    }

    "include a table" which {

      "has the correct first row" which {
        lazy val row = doc.select("tr").get(0)

        "contains the correct first element text" in {
          row.select("td").get(0).text shouldBe tableRowOneElementOneText
        }

        "contains the correct second element text" in {
          row.select("td").get(1).text shouldBe tableRowOneElementTwoText
        }
      }

      "has the correct second row" which {
        lazy val row = doc.select("tr").get(1)

        "contains the correct first element text" in {
          row.select("td").get(0).text shouldBe tableRowTwoElementOneText
        }

        "contains the correct second element text" in {
          row.select("td").get(1).text shouldBe tableRowTwoElementTwoText
        }
      }

      "has the correct third row" which {
        lazy val row = doc.select("tr").get(2)

        "contains the correct first element text" in {
          row.select("td").get(0).text shouldBe tableRowThreeElementOneText
        }

        "contains the correct second element text" in {
          row.select("td").get(1).text shouldBe tableRowThreeElementTwoText
        }
      }
    }

    "have a paragraph describing details of the failed lookup" in {
      doc.select("article p").get(0).text() shouldBe detailsText
    }

    "have a paragraph describing possible causes" in {
      doc.select("article p").get(1).text() shouldBe causesText
    }

    "include a list" which {

      "has the first cause listed" in {
        doc.select("article li").get(0).text() shouldBe causeOneText
      }

      "has the second cause listed" in {
        doc.select("article li").get(1).text() shouldBe causeTwoText
      }
    }

    "have a paragraph describing a possible resolution" in {
      doc.select("article p").get(2).text() shouldBe suggestionsText
    }

    "have a save pdf link" which {

      "has the correct text" in {
        doc.select("a.button--secondary").text() shouldBe pdfLinkText
      }

      "links to the pdf page" in {
        doc.select("a.button--secondary").attr("href") shouldBe controllers.routes.PrintPdfController.printNotFoundPDF().url
      }
    }

    "have a start again link" which {

      "has the correct text" in {
        doc.select("a.button--get-started").text() shouldBe startAgainText
      }

      "links to the start point" in {
        doc.select("a.button--get-started").attr("href") shouldBe controllers.routes.LookupController.redirectToStart().url
      }
    }
  }
}
