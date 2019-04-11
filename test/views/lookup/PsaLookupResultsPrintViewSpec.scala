/*
 * Copyright 2019 HM Revenue & Customs
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

import models.PSALookupResult
import org.jsoup.Jsoup
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.lookup.PsaLookupResultsSpecMessages
import views.html.pages.lookup.psa_lookup_results_print

class PsaLookupResultsPrintViewSpec extends CommonViewSpecHelper with PsaLookupResultsSpecMessages {

  "The PsaLookupResults view" when {

    "provided with no optional values" should {
      val model = PSALookupResult("reference", 1, 0, None, None)
      lazy val view =  psa_lookup_results_print(model, "timestamp")
      lazy val doc = Jsoup.parse(view.body)

      "have a logo" which {

        "has the correct subtext" in {
          doc.select("header a").text() shouldBe logoText
        }

        "has the correct image source" in {
          doc.select("header a img").attr("src") shouldBe "data:image/png;base64, iVBORw0KGgoAAAANSUhEUgAAAEcAAAA+CAMAAABOQ/YkAAAAGFBMVEX////q6ut5eXqjo6PFxcVFRUYfHyAKCgtUCyV+AAACr0lEQVR42u2X607sMAyE49vk/d/4kNjNpLSr1VJ0fmEJ6Abn8/iSFFr/HftvnDbNHnNscvQxRycHjzkYGH9Wn6nCKy38nBNigZEYoN4ecEZOUl+POLTPOBF4w6Hfaw5GTQ0njsy1Myfm2muO1+ySIzrrHOE7R+hHzjWNFB5f34a2ih3eOyJAP3nF0eJEJigxc+UWHTqUflcOVLHrMQbURFNG7LpDg5z0tS3vJYw1O+DNO8pvl90YSHvI7FcuZA5gLfzgLL9cESSHgaq81LPVbLnZmh9sstseaJmtBW9rN7grjbKTw0AnkCOVFTPTEe20b3ogx0caAsuVYjPri+zkVKCzC0f3IvYqm3OIe8pe53sbsqkHD+756jDFleG7n6py7ezCkrRSzqY7mS8sVtPYonZUMliQVyBKkPLgyDRwmFLO27en05sj3Pq5IXj72tO9DEbOfFxKg+IYL6WftuYzj3jj4b/jhKx5JpocXjl1GeHoJcghN+WSQyzyCuR9KNpBN+WE2apewekAdLFJKT3ZAcMSEJVU1ITYzICZpDzVGcVKT21F06OOom4x1z3GPgFUmoytbrrimE+on+7n0Bk1zKq0yFrRfJ1az2ykxYK243YwaZLSOJgXDsdAMOXayJmc2h7LUeTg2HDXmnJvi6QcPHJqv42fkr80Hauj0pXRAFtkSBmfpHgbJzWKdHP00oo6+tGywmMHMqR+LUY0UZ7YxukCbL52sIJYVBOQXS5Pn8OkWiLJSW/na0Cux7uMUXK0sDhc0+XJU8l55p9Izkfr5NBfudNrpO1oD+r88FQ4+RsncjfWTnF3aTR3Z9MjidLJ4Zvqc7Mbjv2AEzec+AEHNxx8jvF+5fAQ0sR3uxZQbzmaXTHViMDLt3CoqmXr4paDiP6BIfS3/j/94/xxfonzD3xNVDwDqvB0AAAAAElFTkSuQmCC"
        }
      }

      "have the app name" in {
        doc.select("header span").text() shouldBe plaBaseAppName
      }

      "have some centered content with the correct text" in {
        doc.select("main span").text() shouldBe hmrcText
      }

      "have the correct heading" in {
        doc.select("h1").text() shouldBe headingText
      }

      "have a table" which {

        "only has 3 rows" in {
          doc.select("tr").size() shouldBe 3
        }

        "has a first row" which {
          lazy val row = doc.select("tr").get(0)

          "has the correct first element" in {
            row.select("td").get(0).text() shouldBe schemeAdministratorRowTitleText
          }

          "has the correct second element" in {
            row.select("td").get(1).text() shouldBe "reference"
          }
        }

        "has a second row" which {
          lazy val row = doc.select("tr").get(1)

          "has the correct first element" in {
            row.select("td").get(0).text() shouldBe statusRowTitleText
          }

          "has the correct second element" in {
            row.select("td").get(1).text() shouldBe statusRowInvalidText
          }
        }

        "has a third row" which {
          lazy val row = doc.select("tr").get(2)

          "has the correct first element" in {
            row.select("td").get(0).text() shouldBe checkedOnTitleText
          }

          "has the correct second element" in {
            row.select("td").get(1).text() shouldBe "timestamp"
          }
        }
      }

      "have a copyright message in the footer" in {
        doc.select("footer p").text() shouldBe copyrightText
      }
    }

    "provided with all optional values" should {
      val model = PSALookupResult("reference", 1, 0, Some(3), Some("data"))
      lazy val view =  psa_lookup_results_print(model, "timestamp")
      lazy val doc = Jsoup.parse(view.body)

      "have a table" which {

        "only has 4 rows" in {
          doc.select("tr").size() shouldBe 4
        }

        "has a first row" which {
          lazy val row = doc.select("tr").get(0)

          "has the correct first element" in {
            row.select("td").get(0).text() shouldBe schemeAdministratorRowTitleText
          }

          "has the correct second element" in {
            row.select("td").get(1).text() shouldBe "reference"
          }
        }

        "has a second row" which {
          lazy val row = doc.select("tr").get(1)

          "has the correct first element" in {
            row.select("td").get(0).text() shouldBe protectionNumberTitleText
          }

          "has the correct second element" in {
            row.select("td").get(1).text() shouldBe "data"
          }
        }

        "has a third row" which {
          lazy val row = doc.select("tr").get(2)

          "has the correct first element" in {
            row.select("td").get(0).text() shouldBe statusRowTitleText
          }

          "has the correct second element" in {
            row.select("td").get(1).text() shouldBe statusRowInvalidText
          }
        }

        "has a fourth row" which {
          lazy val row = doc.select("tr").get(3)

          "has the correct first element" in {
            row.select("td").get(0).text() shouldBe checkedOnTitleText
          }

          "has the correct second element" in {
            row.select("td").get(1).text() shouldBe "timestamp"
          }
        }
      }

    }

    "provided with a valid result of FP2016" should {
      val model = PSALookupResult("reference", 1, 1, None, None)
      lazy val view = psa_lookup_results_print(model, "timestamp")
      lazy val doc = Jsoup.parse(view.body)

      "have a table" which {

        "only has 4 rows" in {
          doc.select("tr").size() shouldBe 4
        }

        "has a first row" which {
          lazy val row = doc.select("tr").get(0)

          "has the correct first element" in {
            row.select("td").get(0).text() shouldBe schemeAdministratorRowTitleText
          }

          "has the correct second element" in {
            row.select("td").get(1).text() shouldBe "reference"
          }
        }

        "has a second row" which {
          lazy val row = doc.select("tr").get(1)

          "has the correct first element" in {
            row.select("td").get(0).text() shouldBe protectionTypeTitleText
          }

          "has the correct second element" in {
            row.select("td").get(1).text() shouldBe protectionTypeFP2016Text
          }
        }

        "has a third row" which {
          lazy val row = doc.select("tr").get(2)

          "has the correct first element" in {
            row.select("td").get(0).text() shouldBe statusRowTitleText
          }

          "has the correct second element" in {
            row.select("td").get(1).text() shouldBe statusRowValidText
          }
        }

        "has a fourth row" which {
          lazy val row = doc.select("tr").get(3)

          "has the correct first element" in {
            row.select("td").get(0).text() shouldBe checkedOnTitleText
          }

          "has the correct second element" in {
            row.select("td").get(1).text() shouldBe "timestamp"
          }
        }
      }
    }

    "provided with a valid result of IP2014" should {
      val model = PSALookupResult("reference", 2, 1, None, None)
      lazy val view = psa_lookup_results_print(model, "timestamp")
      lazy val doc = Jsoup.parse(view.body)

      "have a table" which {

        "only has 4 rows" in {
          doc.select("tr").size() shouldBe 4
        }

        "has a first row" which {
          lazy val row = doc.select("tr").get(0)

          "has the correct first element" in {
            row.select("td").get(0).text() shouldBe schemeAdministratorRowTitleText
          }

          "has the correct second element" in {
            row.select("td").get(1).text() shouldBe "reference"
          }
        }

        "has a second row" which {
          lazy val row = doc.select("tr").get(1)

          "has the correct first element" in {
            row.select("td").get(0).text() shouldBe protectionTypeTitleText
          }

          "has the correct second element" in {
            row.select("td").get(1).text() shouldBe protectionTypeIP2014Text
          }
        }

        "has a third row" which {
          lazy val row = doc.select("tr").get(2)

          "has the correct first element" in {
            row.select("td").get(0).text() shouldBe statusRowTitleText
          }

          "has the correct second element" in {
            row.select("td").get(1).text() shouldBe statusRowValidText
          }
        }

        "has a fourth row" which {
          lazy val row = doc.select("tr").get(3)

          "has the correct first element" in {
            row.select("td").get(0).text() shouldBe checkedOnTitleText
          }

          "has the correct second element" in {
            row.select("td").get(1).text() shouldBe "timestamp"
          }
        }
      }
    }

    "provided with a valid result of IP2016 with a value" should {
      val model = PSALookupResult("reference", 3, 1, Some(2), None)
      lazy val view = psa_lookup_results_print(model, "timestamp")
      lazy val doc = Jsoup.parse(view.body)

      "have a table" which {

        "only has 5 rows" in {
          doc.select("tr").size() shouldBe 5
        }

        "has a first row" which {
          lazy val row = doc.select("tr").get(0)

          "has the correct first element" in {
            row.select("td").get(0).text() shouldBe schemeAdministratorRowTitleText
          }

          "has the correct second element" in {
            row.select("td").get(1).text() shouldBe "reference"
          }
        }

        "has a second row" which {
          lazy val row = doc.select("tr").get(1)

          "has the correct first element" in {
            row.select("td").get(0).text() shouldBe protectionTypeTitleText
          }

          "has the correct second element" in {
            row.select("td").get(1).text() shouldBe protectionTypeIP2016Text
          }
        }

        "has a third row" which {
          lazy val row = doc.select("tr").get(2)

          "has the correct first element" in {
            row.select("td").get(0).text() shouldBe protectedAmountTitleText
          }

          "has the correct second element" in {
            row.select("td").get(1).text() shouldBe "£2"
          }
        }

        "has a fourth row" which {
          lazy val row = doc.select("tr").get(3)

          "has the correct first element" in {
            row.select("td").get(0).text() shouldBe statusRowTitleText
          }

          "has the correct second element" in {
            row.select("td").get(1).text() shouldBe statusRowValidText
          }
        }

        "has a fifth row" which {
          lazy val row = doc.select("tr").get(4)

          "has the correct first element" in {
            row.select("td").get(0).text() shouldBe checkedOnTitleText
          }

          "has the correct second element" in {
            row.select("td").get(1).text() shouldBe "timestamp"
          }
        }
      }
    }

    "provided with a valid result of Primary" should {
      val model = PSALookupResult("reference", 4, 1, None, None)
      lazy val view = psa_lookup_results_print(model, "timestamp")
      lazy val doc = Jsoup.parse(view.body)

      "have a table" which {

        "only has 4 rows" in {
          doc.select("tr").size() shouldBe 4
        }

        "has a second row" which {
          lazy val row = doc.select("tr").get(1)

          "has the correct first element" in {
            row.select("td").get(0).text() shouldBe protectionTypeTitleText
          }

          "has the correct second element" in {
            row.select("td").get(1).text() shouldBe protectionTypePrimaryText
          }
        }
      }
    }

    "provided with a valid result of Enhanced" should {
      val model = PSALookupResult("reference", 5, 1, None, None)
      lazy val view = psa_lookup_results_print(model, "timestamp")
      lazy val doc = Jsoup.parse(view.body)

      "have a table" which {

        "only has 4 rows" in {
          doc.select("tr").size() shouldBe 4
        }

        "has a second row" which {
          lazy val row = doc.select("tr").get(1)

          "has the correct first element" in {
            row.select("td").get(0).text() shouldBe protectionTypeTitleText
          }

          "has the correct second element" in {
            row.select("td").get(1).text() shouldBe protectionTypeEnhancedText
          }
        }
      }
    }

    "provided with a valid result of FP2012" should {
      val model = PSALookupResult("reference", 6, 1, None, None)
      lazy val view = psa_lookup_results_print(model, "timestamp")
      lazy val doc = Jsoup.parse(view.body)

      "have a table" which {

        "only has 4 rows" in {
          doc.select("tr").size() shouldBe 4
        }

        "has a second row" which {
          lazy val row = doc.select("tr").get(1)

          "has the correct first element" in {
            row.select("td").get(0).text() shouldBe protectionTypeTitleText
          }

          "has the correct second element" in {
            row.select("td").get(1).text() shouldBe protectionTypeFP2012Text
          }
        }
      }
    }

    "provided with a valid result of" should {
      val model = PSALookupResult("reference", 7, 1, None, None)
      lazy val view = psa_lookup_results_print(model, "timestamp")
      lazy val doc = Jsoup.parse(view.body)

      "have a table" which {

        "only has 4 rows" in {
          doc.select("tr").size() shouldBe 4
        }

        "has a second row" which {
          lazy val row = doc.select("tr").get(1)

          "has the correct first element" in {
            row.select("td").get(0).text() shouldBe protectionTypeTitleText
          }

          "has the correct second element" in {
            row.select("td").get(1).text() shouldBe protectionTypeFP2014Text
          }
        }
      }
    }
  }
}
