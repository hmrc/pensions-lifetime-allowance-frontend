/*
 * Copyright 2020 HM Revenue & Customs
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
import views.html.pages.lookup.psa_lookup_not_found_print

class PsaLookupNotFoundPrintViewSpec extends CommonViewSpecHelper with PsaLookupNotFoundSpecMessages {

  "The Psa Lookup Not Found Print view" should {
    val model = PSALookupRequest("check", Some("ref"))
    lazy val view = psa_lookup_not_found_print(model, "timestamp")
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

    "have a copyright message in the footer" in {
      doc.select("footer p").text() shouldBe copyrightText
    }
  }
}
