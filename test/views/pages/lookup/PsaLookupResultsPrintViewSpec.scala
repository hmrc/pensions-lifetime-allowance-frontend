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

import models.PSALookupResult
import org.jsoup.Jsoup
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.lookup.PsaLookupResultsSpecMessages
import views.html.pages.lookup.psa_lookup_results_print

class PsaLookupResultsPrintViewSpec extends CommonViewSpecHelper with PsaLookupResultsSpecMessages {

  "The PsaLookupResults view" when {

    "provided with no optional values" should {
      val model = PSALookupResult("reference", 1, 0, None, None)
      lazy val psaLookupResultsPrint = fakeApplication().injector.instanceOf[psa_lookup_results_print]
      lazy val view =  psaLookupResultsPrint(model, "timestamp")
      lazy val doc = Jsoup.parse(view.body)

      "have a logo" which {

        "has the correct subtext" in {
          doc.select("span.govuk-header__logotype-text").text() shouldBe logoText
        }
      }

      "have the app name" in {
        doc.select(".govuk-header__content").text() shouldBe plaBaseAppName
      }

      "have the correct heading" in {
        doc.select("h1").text() shouldBe headingText
      }

      "have a table" which {

        "only has 3 rows" in {
          doc.select(".govuk-summary-list__row").size() shouldBe 3
        }

        "has a first row" which {
          lazy val row = doc.select(".govuk-summary-list__row").get(0)

          "has the correct first element" in {
            row.select(".govuk-summary-list__key").get(0).text() shouldBe schemeAdministratorRowTitleText
          }

          "has the correct second element" in {
            row.select(".govuk-summary-list__value").get(0).text() shouldBe "reference"
          }
        }

        "has a second row" which {
          lazy val row = doc.select(".govuk-summary-list__row").get(1)

          "has the correct first element" in {
            row.select(".govuk-summary-list__key").get(0).text() shouldBe statusRowTitleText
          }

          "has the correct second element" in {
            row.select(".govuk-summary-list__value").get(0).text() shouldBe statusRowInvalidText
          }
        }

        "has a third row" which {
          lazy val row = doc.select(".govuk-summary-list__row").get(2)

          "has the correct first element" in {
            row.select(".govuk-summary-list__key").get(0).text() shouldBe checkedOnTitleText
          }

          "has the correct second element" in {
            row.select(".govuk-summary-list__value").get(0).text() shouldBe "timestamp"
          }
        }
      }

      "have a copyright message in the footer" in {
        doc.select("footer .govuk-footer__copyright-logo").text() shouldBe copyrightText
      }
    }

    "provided with all optional values" should {
      val model = PSALookupResult("reference", 1, 0, Some(3), Some("data"))
      lazy val psaLookupResultsPrint = fakeApplication().injector.instanceOf[psa_lookup_results_print]
      lazy val view =  psaLookupResultsPrint(model, "timestamp")
      lazy val doc = Jsoup.parse(view.body)

      "have a table" which {

        "has a first row" which {
          lazy val row = doc.select(".govuk-summary-list__row").get(0)

          "has the correct first element" in {
            row.select(".govuk-summary-list__key").get(0).text() shouldBe schemeAdministratorRowTitleText
          }

          "has the correct second element" in {
            row.select(".govuk-summary-list__value").get(0).text() shouldBe "reference"
          }
        }

        "has a second row" which {
          lazy val row = doc.select(".govuk-summary-list__row").get(1)

          "has the correct first element" in {
            row.select(".govuk-summary-list__key").get(0).text() shouldBe protectionNumberTitleText
          }

          "has the correct second element" in {
            row.select(".govuk-summary-list__value").get(0).text() shouldBe "data"
          }
        }

        "has a third row" which {
          lazy val row = doc.select(".govuk-summary-list__row").get(2)

          "has the correct first element" in {
            row.select(".govuk-summary-list__key").get(0).text() shouldBe statusRowTitleText
          }

          "has the correct second element" in {
            row.select(".govuk-summary-list__value").get(0).text() shouldBe statusRowInvalidText
          }
        }

        "has a fourth row" which {
          lazy val row = doc.select(".govuk-summary-list__row").get(3)

          "has the correct first element" in {
            row.select(".govuk-summary-list__key").get(0).text() shouldBe checkedOnTitleText
          }

          "has the correct second element" in {
            row.select(".govuk-summary-list__value").get(0).text() shouldBe "timestamp"
          }
        }
      }

    }

    "provided with a valid result of FP2016" should {
      val model = PSALookupResult("reference", 1, 1, None, None)
      lazy val psaLookupResultsPrint = fakeApplication().injector.instanceOf[psa_lookup_results_print]
      lazy val view = psaLookupResultsPrint(model, "timestamp")
      lazy val doc = Jsoup.parse(view.body)

      "have a table" which {

        "has a first row" which {
          lazy val row = doc.select(".govuk-summary-list__row").get(0)

          "has the correct first element" in {
            row.select(".govuk-summary-list__key").get(0).text() shouldBe schemeAdministratorRowTitleText
          }

          "has the correct second element" in {
            row.select(".govuk-summary-list__value").get(0).text() shouldBe "reference"
          }
        }

        "has a second row" which {
          lazy val row = doc.select(".govuk-summary-list__row").get(1)

          "has the correct first element" in {
            row.select(".govuk-summary-list__key").get(0).text() shouldBe protectionTypeTitleText
          }

          "has the correct second element" in {
            row.select(".govuk-summary-list__value").get(0).text() shouldBe protectionTypeFP2016Text
          }
        }

        "has a third row" which {
          lazy val row = doc.select(".govuk-summary-list__row").get(2)

          "has the correct first element" in {
            row.select(".govuk-summary-list__key").get(0).text() shouldBe statusRowTitleText
          }

          "has the correct second element" in {
            row.select(".govuk-summary-list__value").get(0).text() shouldBe statusRowValidText
          }
        }

        "has a fourth row" which {
          lazy val row = doc.select(".govuk-summary-list__row").get(3)

          "has the correct first element" in {
            row.select(".govuk-summary-list__key").get(0).text() shouldBe checkedOnTitleText
          }

          "has the correct second element" in {
            row.select(".govuk-summary-list__value").get(0).text() shouldBe "timestamp"
          }
        }
      }

      "provided with a valid result of IP2016 with a value" should {
        val model = PSALookupResult("reference", 3, 1, Some(2), None)
        lazy val psaLookupResultsPrint = fakeApplication().injector.instanceOf[psa_lookup_results_print]
        lazy val view = psaLookupResultsPrint(model, "timestamp")
        lazy val doc = Jsoup.parse(view.body)

        "have a table" which {

          "has a first row" which {
            lazy val row = doc.select(".govuk-summary-list__row").get(0)

            "has the correct first element" in {
              row.select(".govuk-summary-list__key").get(0).text() shouldBe schemeAdministratorRowTitleText
            }

            "has the correct second element" in {
              row.select(".govuk-summary-list__value").get(0).text() shouldBe "reference"
            }
          }

          "has a second row" which {
            lazy val row = doc.select(".govuk-summary-list__row").get(1)

            "has the correct first element" in {
              row.select(".govuk-summary-list__key").get(0).text() shouldBe protectionTypeTitleText
            }

            "has the correct second element" in {
              row.select(".govuk-summary-list__value").get(0).text() shouldBe protectionTypeIP2016Text
            }
          }

          "has a third row" which {
            lazy val row = doc.select(".govuk-summary-list__row").get(2)

            "has the correct first element" in {
              row.select(".govuk-summary-list__key").get(0).text() shouldBe protectedAmountTitleText
            }

            "has the correct second element" in {
              row.select(".govuk-summary-list__value").get(0).text() shouldBe "£2"
            }
          }

          "has a fourth row" which {
            lazy val row = doc.select(".govuk-summary-list__row").get(3)

            "has the correct first element" in {
              row.select(".govuk-summary-list__key").get(0).text() shouldBe statusRowTitleText
            }

            "has the correct second element" in {
              row.select(".govuk-summary-list__value").get(0).text() shouldBe statusRowValidText
            }
          }
        }
      }

      "provided with a valid result of Primary" should {
        val model = PSALookupResult("reference", 4, 1, None, None)
        lazy val psaLookupResultsPrint = fakeApplication().injector.instanceOf[psa_lookup_results_print]
        lazy val view = psaLookupResultsPrint(model, "timestamp")
        lazy val doc = Jsoup.parse(view.body)

        "have a table" which {

          "has a second row" which {
            lazy val row = doc.select(".govuk-summary-list__row").get(1)

            "has the correct first element" in {
              row.select(".govuk-summary-list__key").get(0).text() shouldBe protectionTypeTitleText
            }

            "has the correct second element" in {
              row.select(".govuk-summary-list__value").get(0).text() shouldBe protectionTypePrimaryText
            }
          }
        }
      }
    }

    "provided with a valid result of Enhanced" should {
      val model = PSALookupResult("reference", 5, 1, None, None)
      lazy val psaLookupResultsPrint = fakeApplication().injector.instanceOf[psa_lookup_results_print]
      lazy val view =  psaLookupResultsPrint(model, "timestamp")
      lazy val doc = Jsoup.parse(view.body)

      "have a table" which {

        "has a second row" which {
          lazy val row = doc.select(".govuk-summary-list__row").get(1)

          "has the correct first element" in {
            row.select(".govuk-summary-list__key").get(0).text() shouldBe protectionTypeTitleText
          }

          "has the correct second element" in {
            row.select(".govuk-summary-list__value").get(0).text() shouldBe protectionTypeEnhancedText
          }
        }
      }
    }

    "provided with a valid result of FP2012" should {
      val model = PSALookupResult("reference", 6, 1, None, None)
      lazy val psaLookupResultsPrint = fakeApplication().injector.instanceOf[psa_lookup_results_print]
      lazy val view =  psaLookupResultsPrint(model, "timestamp")
      lazy val doc = Jsoup.parse(view.body)

      "have a table" which {

        "has a second row" which {
          lazy val row = doc.select(".govuk-summary-list__row").get(1)

          "has the correct first element" in {
            row.select(".govuk-summary-list__key").get(0).text() shouldBe protectionTypeTitleText
          }

          "has the correct second element" in {
            row.select(".govuk-summary-list__value").get(0).text() shouldBe protectionTypeFP2012Text
          }
        }
      }
    }

    "provided with a valid result of" should {
      val model = PSALookupResult("reference", 7, 1, None, None)
      lazy val psaLookupResultsPrint = fakeApplication().injector.instanceOf[psa_lookup_results_print]
      lazy val view =  psaLookupResultsPrint(model, "timestamp")
      lazy val doc = Jsoup.parse(view.body)

      "have a table" which {

        "has a second row" which {
          lazy val row = doc.select(".govuk-summary-list__row").get(1)

          "has the correct first element" in {
            row.select(".govuk-summary-list__key").get(0).text() shouldBe protectionTypeTitleText
          }

          "has the correct second element" in {
            row.select(".govuk-summary-list__value").get(0).text() shouldBe protectionTypeFP2014Text
          }
        }
      }
    }
  }
}
