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

        "has the correct left column heading" in {
          doc.select("#main-content > div > div > dl > div:nth-child(1) > dt").text() shouldBe tableHeadingLeft
        }

        "has the correct right column heading" in {
          doc.select("#main-content > div > div > dl > div:nth-child(1) > dd").text() shouldBe tableHeadingRight
        }
      }

      "has a first row" which {
        lazy val row = doc.select("tr").get(1)

        "has the correct left element" in {
          doc.select("#main-content > div > div > dl > div:nth-child(2) > dt").text() shouldBe rowOneLeft
        }

        "has the correct right element" in {
          doc.select("#main-content > div > div > dl > div:nth-child(2) > dd").text() shouldBe rowOneRight
        }
      }

      "has a second row" which {
        lazy val row = doc.select("div").get(3)

        "has the correct left element" in {
          doc.select("#main-content > div > div > dl > div:nth-child(3) > dt").text() shouldBe rowTwoLeft
        }

        "has the correct right element" in {
          doc.select("#main-content > div > div > dl > div:nth-child(3) > dd").text() shouldBe rowTwoRight
        }
      }

      "has a third row" which {
        lazy val row = doc.select("tr").get(3)

        "has the correct left element" in {
          doc.select("#main-content > div > div > dl > div:nth-child(4) > dt").text() shouldBe rowThreeLeft
        }

        "has the correct right element" in {
          doc.select("#main-content > div > div > dl > div:nth-child(4) > dd").text() shouldBe rowThreeRight
        }
      }

      "has a fourth row" which {
        lazy val row = doc.select("tr").get(4)

        "has the correct left element" in {
          doc.select("#main-content > div > div > dl > div:nth-child(5) > dt").text() shouldBe rowFourLeft
        }

        "has the correct right element" in {
          doc.select("#main-content > div > div > dl > div:nth-child(5) > dd").text() shouldBe rowFourRight
        }
      }

      "has a fifth row" which {
        lazy val row = doc.select("tr").get(5)

        "has the correct left element" in {
          doc.select("#main-content > div > div > dl > div:nth-child(6) > dt").text() shouldBe rowFiveLeft
        }

        "has the correct right element" which {
          lazy val element = doc.select("#main-content > div > div > dl > div:nth-child(6) > dd")

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

        "has the correct left element" in {
          doc.select("#main-content > div > div > dl > div:nth-child(7) > dt").text() shouldBe rowSixLeft
        }

        "has the correct right element" which {
          lazy val element = doc.select("#main-content > div > div > dl > div:nth-child(7) > dd")

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

        "has the correct left element" in {
          doc.select("#main-content > div > div > dl > div:nth-child(8) > dt").text() shouldBe rowSevenLeft
        }

        "has the correct right element" in {
          doc.select("#main-content > div > div > dl > div:nth-child(8) > dd").text() shouldBe rowSevenRight
        }
      }
    }

    "has a back link" which {
      lazy val link = doc.getElementsByClass("govuk-link govuk-body")

      "has the correct text" in {
        link.text() shouldBe plaBaseBack
      }

      "has a link to the correct location" in {
        link.attr("href") shouldBe controllers.routes.LookupController.displayLookupResults.url
      }
    }
  }
}
