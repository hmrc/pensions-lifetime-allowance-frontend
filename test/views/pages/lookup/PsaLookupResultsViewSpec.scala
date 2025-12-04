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

import models.PsaLookupResult
import org.jsoup.Jsoup
import testHelpers.CommonViewSpecHelper
import testHelpers.messages.lookup.PsaLookupResultsSpecMessages
import views.html.pages.lookup.psa_lookup_results

class PsaLookupResultsViewSpec extends CommonViewSpecHelper with PsaLookupResultsSpecMessages {

  val view: psa_lookup_results = inject[psa_lookup_results]

  "The PsaLookupResults view" when {

    "provided with no optional values" should {
      val psaLookupResults = PsaLookupResult("reference", 1, 0, None, None)
      val doc              = Jsoup.parse(view.apply(psaLookupResults, "timestamp").body)

      "have the correct title" in {
        doc.title() shouldBe titleText
      }

      "have the correct heading" in {
        doc.select("h1").text() shouldBe headingText
      }

      "have a table".which {

        "only has 3 rows" in {
          doc.getElementsByClass("govuk-summary-list__key").size() shouldBe 3
        }

        "has a first row".which {
          val row = doc.getElementsByClass("govuk-summary-list__row").get(0)

          "has the correct first element" in {
            row.select("dt").text() shouldBe schemeAdministratorRowTitleText
          }

          "has the correct second element" in {
            row.select("dd").text() shouldBe "reference"
          }
        }

        "has a second row".which {
          val row = doc.getElementsByClass("govuk-summary-list__row").get(1)

          "has the correct first element" in {
            row.select("dt").text() shouldBe statusRowTitleText
          }

          "has the correct second element" in {
            row.select("dd").text() shouldBe statusRowInvalidText
          }
        }

        "has a third row".which {
          val row = doc.getElementsByClass("govuk-summary-list__row").get(2)

          "has the correct first element" in {
            row.select("dt").text() shouldBe checkedOnTitleText
          }

          "has the correct second element" in {
            row.select("dd").text() shouldBe timestampText
          }
        }
      }

      "has a pdf link".which {

        "has the correct text" in {
          doc.getElementById("printLink").text() shouldBe pdfLinkText
        }

        "has the data module to print page" in {
          doc.getElementById("printLink").attr("data-module") shouldBe "hmrc-print-link"
        }
      }

      "has a start again link".which {

        "has the correct text" in {
          doc.getElementsByClass("govuk-button govuk-button--start").text() shouldBe startAgainLinkText
        }

        "has the correct destination" in {
          doc
            .getElementsByClass("govuk-button govuk-button--start")
            .attr("href") shouldBe controllers.routes.LookupController.redirectToStart.url
        }
      }
    }

    "provided with all optional values" should {
      val psaLookupResults = PsaLookupResult("reference", 1, 0, Some(3), Some("data"))
      val doc              = Jsoup.parse(view.apply(psaLookupResults, "timestamp").body)

      "have a table".which {

        "only has 4 rows" in {
          doc.select("dt").size() shouldBe 4
        }

        "has a first row".which {
          val row = doc.getElementsByClass("govuk-summary-list__row").get(0)

          "has the correct first element" in {
            row.select("dt").text() shouldBe schemeAdministratorRowTitleText
          }

          "has the correct second element" in {
            row.select("dd").text() shouldBe "reference"
          }
        }

        "has a second row".which {
          val row = doc.getElementsByClass("govuk-summary-list__row").get(1)

          "has the correct first element" in {
            row.select("dt").text() shouldBe protectionNumberTitleText
          }

          "has the correct second element" in {
            row.select("dd").text() shouldBe "data"
          }
        }

        "has a third row".which {
          val row = doc.getElementsByClass("govuk-summary-list__row").get(2)

          "has the correct first element" in {
            row.select("dt").text() shouldBe statusRowTitleText
          }

          "has the correct second element" in {
            row.select("dd").text() shouldBe statusRowInvalidText
          }
        }

        "has a fourth row".which {
          val row = doc.getElementsByClass("govuk-summary-list__row").get(3)

          "has the correct first element" in {
            row.select("dt").text() shouldBe checkedOnTitleText
          }

          "has the correct second element" in {
            row.select("dd").text() shouldBe timestampText
          }
        }
      }

    }

    "provided with a valid result of FP2016" should {
      val psaLookupResults = PsaLookupResult("reference", 1, 1, None, None)
      val doc              = Jsoup.parse(view.apply(psaLookupResults, "timestamp").body)

      "have a table".which {

        "only has 4 rows" in {
          doc.select("dt").size() shouldBe 4
        }

        "has a first row".which {
          val row = doc.getElementsByClass("govuk-summary-list__row").get(0)

          "has the correct first element" in {
            row.select("dt").text() shouldBe schemeAdministratorRowTitleText
          }

          "has the correct second element" in {
            row.select("dd").text() shouldBe "reference"
          }
        }

        "has a second row".which {
          val row = doc.getElementsByClass("govuk-summary-list__row").get(1)

          "has the correct first element" in {
            row.select("dt").text() shouldBe protectionTypeTitleText
          }

          "has the correct second element" in {
            row.select("dd").text() shouldBe protectionTypeFixedProtection2016Text
          }
        }

        "has a third row".which {
          val row = doc.getElementsByClass("govuk-summary-list__row").get(2)

          "has the correct first element" in {
            row.select("dt").text() shouldBe statusRowTitleText
          }

          "has the correct second element" in {
            row.select("dd").text() shouldBe statusRowValidText
          }
        }

        "has a fourth row".which {
          val row = doc.getElementsByClass("govuk-summary-list__row").get(3)

          "has the correct first element" in {
            row.select("dt").text() shouldBe checkedOnTitleText
          }

          "has the correct second element" in {
            row.select("dd").text() shouldBe timestampText
          }
        }
      }
    }

    "provided with a valid result of IndividualProtection2014" should {
      val psaLookupResults = PsaLookupResult("reference", 2, 1, None, None)
      val doc              = Jsoup.parse(view.apply(psaLookupResults, "timestamp").body)

      "have a table".which {

        "only has 4 rows" in {
          doc.select("dt").size() shouldBe 4
        }

        "has a first row".which {
          val row = doc.getElementsByClass("govuk-summary-list__row").get(0)

          "has the correct first element" in {
            row.select("dt").text() shouldBe schemeAdministratorRowTitleText
          }

          "has the correct second element" in {
            row.select("dd").text() shouldBe "reference"
          }
        }

        "has a second row".which {
          val row = doc.getElementsByClass("govuk-summary-list__row").get(1)

          "has the correct first element" in {
            row.select("dt").text() shouldBe protectionTypeTitleText
          }

          "has the correct second element" in {
            row.select("dd").text() shouldBe protectionTypeIndividualProtection2014Text
          }
        }

        "has a third row".which {
          val row = doc.getElementsByClass("govuk-summary-list__row").get(2)

          "has the correct first element" in {
            row.select("dt").text() shouldBe statusRowTitleText
          }

          "has the correct second element" in {
            row.select("dd").text() shouldBe statusRowValidText
          }
        }

        "has a fourth row".which {
          val row = doc.getElementsByClass("govuk-summary-list__row").get(3)

          "has the correct first element" in {
            row.select("dt").text() shouldBe checkedOnTitleText
          }

          "has the correct second element" in {
            row.select("dd").text() shouldBe timestampText
          }
        }
      }
    }

    "provided with a valid result of IndividualProtection2016 with a value" should {
      val psaLookupResults = PsaLookupResult("reference", 3, 1, Some(2), None)
      val doc              = Jsoup.parse(view.apply(psaLookupResults, "timestamp").body)

      "have a table".which {

        "only has 5 rows" in {
          doc.select("dt").size() shouldBe 5
        }

        "has a first row".which {
          val row = doc.getElementsByClass("govuk-summary-list__row").get(0)

          "has the correct first element" in {
            row.select("dt").text() shouldBe schemeAdministratorRowTitleText
          }

          "has the correct second element" in {
            row.select("dd").text() shouldBe "reference"
          }
        }

        "has a second row".which {
          val row = doc.getElementsByClass("govuk-summary-list__row").get(1)

          "has the correct first element" in {
            row.select("dt").text() shouldBe protectionTypeTitleText
          }

          "has the correct second element" in {
            row.select("dd").text() shouldBe protectionTypeIndividualProtection2016Text
          }
        }

        "has a third row".which {
          val row = doc.getElementsByClass("govuk-summary-list__row").get(2)

          "has the correct first element" in {
            row.select("dt").text() shouldBe protectedAmountTitleText
          }

          "has the correct second element" in {
            row.select("dd").text() shouldBe "£2"
          }
        }

        "has a fourth row".which {
          val row = doc.getElementsByClass("govuk-summary-list__row").get(3)

          "has the correct first element" in {
            row.select("dt").text() shouldBe statusRowTitleText
          }

          "has the correct second element" in {
            row.select("dd").text() shouldBe statusRowValidText
          }
        }

        "has a fifth row".which {
          val row = doc.getElementsByClass("govuk-summary-list__row").get(4)

          "has the correct first element" in {
            row.select("dt").text() shouldBe checkedOnTitleText
          }

          "has the correct second element" in {
            row.select("dd").text() shouldBe timestampText
          }
        }
      }
    }

    "provided with a valid result of Primary" should {
      val psaLookupResults = PsaLookupResult("reference", 4, 1, None, None)
      val doc              = Jsoup.parse(view.apply(psaLookupResults, "").body)

      "have a table".which {

        "only has 4 rows" in {
          doc.select("dt").size() shouldBe 4
        }

        "has a second row".which {
          val row = doc.getElementsByClass("govuk-summary-list__row").get(1)

          "has the correct first element" in {
            row.select("dt").text() shouldBe protectionTypeTitleText
          }

          "has the correct second element" in {
            row.select("dd").text() shouldBe protectionTypePrimaryText
          }
        }
      }
    }

    "provided with a valid result of Enhanced" should {
      val psaLookupResults = PsaLookupResult("reference", 5, 1, None, None)
      val doc              = Jsoup.parse(view.apply(psaLookupResults, "").body)

      "have a table".which {

        "only has 4 rows" in {
          doc.select("dt").size() shouldBe 4
        }

        "has a second row".which {
          val row = doc.getElementsByClass("govuk-summary-list__row").get(1)

          "has the correct first element" in {
            row.select("dt").text() shouldBe protectionTypeTitleText
          }

          "has the correct second element" in {
            row.select("dd").text() shouldBe protectionTypeEnhancedText
          }
        }
      }
    }

    "provided with a valid result of FP2012" should {
      val psaLookupResults = PsaLookupResult("reference", 6, 1, None, None)
      val doc              = Jsoup.parse(view.apply(psaLookupResults, "").body)

      "have a table".which {

        "only has 4 rows" in {
          doc.select("dt").size() shouldBe 4
        }

        "has a second row".which {
          val row = doc.getElementsByClass("govuk-summary-list__row").get(1)

          "has the correct first element" in {
            row.select("dt").text() shouldBe protectionTypeTitleText
          }

          "has the correct second element" in {
            row.select("dd").text() shouldBe protectionTypeFixedProtection2012Text
          }
        }
      }
    }

    "provided with a valid result of" should {
      val psaLookupResults = PsaLookupResult("reference", 7, 1, None, None)
      val doc              = Jsoup.parse(view.apply(psaLookupResults, "").body)

      "have a table".which {

        "only has 4 rows" in {
          doc.select("dt").size() shouldBe 4
        }

        "has a second row".which {
          val row = doc.getElementsByClass("govuk-summary-list__row").get(1)

          "has the correct first element" in {
            row.select("dt").text() shouldBe protectionTypeTitleText
          }

          "has the correct second element" in {
            row.select("dd").text() shouldBe protectionTypeFixedProtection2014Text
          }
        }
      }
    }
  }

}
