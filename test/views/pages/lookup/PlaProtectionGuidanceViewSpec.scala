/*
 * Copyright 2021 HM Revenue & Customs
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

import org.jsoup.Jsoup
import testHelpers.ViewSpecHelpers.CommonViewSpecHelper
import testHelpers.ViewSpecHelpers.lookup.ProtectionGuidanceSpecMessages
import views.html.pages.lookup.pla_protection_guidance

class PlaProtectionGuidanceViewSpec extends CommonViewSpecHelper with ProtectionGuidanceSpecMessages {

  "The PLA Protection Guidance View" should {
    lazy val view = application.injector.instanceOf[pla_protection_guidance]
    lazy val doc = Jsoup.parse(view().body)

    "have a header with the correct content" in {
      doc.select("h1").text() shouldBe title
    }

    "have a table" which {

      "has a heading row" which {
        lazy val row = doc.select("tr").first()

        "has the correct left column heading" in {
          row.select("th").get(0).text() shouldBe tableHeadingLeft
        }

        "has the correct right column heading" in {
          row.select("th").get(1).text() shouldBe tableHeadingRight
        }
      }

      "has a first row" which {
        lazy val row = doc.select("tr").get(1)

        "has the correct left element" in {
          row.select("td").get(0).text() shouldBe rowOneLeft
        }

        "has the correct right element" in {
          row.select("td").get(1).text() shouldBe rowOneRight
        }
      }

      "has a second row" which {
        lazy val row = doc.select("tr").get(2)

        "has the correct left element" in {
          row.select("td").get(0).text() shouldBe rowTwoLeft
        }

        "has the correct right element" in {
          row.select("td").get(1).text() shouldBe rowTwoRight
        }
      }

      "has a third row" which {
        lazy val row = doc.select("tr").get(3)

        "has the correct left element" in {
          row.select("td").get(0).text() shouldBe rowThreeLeft
        }

        "has the correct right element" in {
          row.select("td").get(1).text() shouldBe rowThreeRight
        }
      }

      "has a fourth row" which {
        lazy val row = doc.select("tr").get(4)

        "has the correct left element" in {
          row.select("td").get(0).text() shouldBe rowFourLeft
        }

        "has the correct right element" in {
          row.select("td").get(1).text() shouldBe rowFourRight
        }
      }

      "has a fifth row" which {
        lazy val row = doc.select("tr").get(5)

        "has the correct left element" in {
          row.select("td").get(0).text() shouldBe rowFiveLeft
        }

        "has the correct right element" which {
          lazy val element = row.select("td").get(1)

          "has the correct text" in {
            element.text() shouldBe rowFiveRight
          }

          "has a list with the correct first element" in {
            element.select("li").get(0).text() shouldBe rowFiveListOne
          }

          "has a list with the correct second element" in {
            element.select("li").get(1).text() shouldBe rowFiveListTwo
          }
        }
      }

      "has a sixth row" which {
        lazy val row = doc.select("tr").get(6)

        "has the correct left element" in {
          row.select("td").get(0).text() shouldBe rowSixLeft
        }

        "has the correct right element" which {
          lazy val element = row.select("td").get(1)

          "has the correct text" in {
            element.text() shouldBe rowSixRight
          }

          "has a list with the correct first element" in {
            element.select("li").get(0).text() shouldBe rowSixListOne
          }

          "has a list with the correct second element" in {
            element.select("li").get(1).text() shouldBe rowSixListTwo
          }
        }
      }

      "has a seventh row" which {
        lazy val row = doc.select("tr").get(7)

        "has the correct left element" in {
          row.select("td").get(0).text() shouldBe rowSevenLeft
        }

        "has the correct right element" in {
          row.select("td").get(1).text() shouldBe rowSevenRight
        }
      }
    }

    "has a back link" which {
      lazy val link = doc.select("div a.back-link")

      "has the correct text" in {
        link.text() shouldBe plaBaseBack
      }

      "has a link to the correct location" in {
       link.attr("href") shouldBe controllers.routes.LookupController.displayLookupResults().url
      }
    }
  }
}
