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
import views.html.pages.lookup.psa_lookup_not_found_print

class PsaLookupNotFoundPrintViewSpec extends CommonViewSpecHelper with PsaLookupNotFoundSpecMessages {

  "The Psa Lookup Not Found Print view" should {
    val model = PSALookupRequest("check", Some("ref"))
    lazy val psaLookupNotFoundPrintView = fakeApplication().injector.instanceOf[psa_lookup_not_found_print]
    lazy val view = psaLookupNotFoundPrintView(model, "timestamp")
    lazy val doc = Jsoup.parse(view.body)

    "have a logo" which {

      "has the correct subtext" in {
        doc.select("span.govuk-header__logotype-text").text() shouldBe logoText
      }

    }

    "have the app name" in {
      doc.select(".govuk-header__content").text() shouldBe plaBaseAppName
    }

    "have the correct header text" in {
      doc.select("h1").text() shouldBe checkDetailsText
    }

    "include a table" which {

      "has the correct first row" which {
        lazy val row = doc.select(".govuk-summary-list__row").get(0)

        "contains the correct first element text" in {
          row.select(".govuk-summary-list__key").text shouldBe tableRowOneElementOneText
        }

        "contains the correct second element text" in {
          row.select(".govuk-summary-list__value").text shouldBe tableRowOneElementTwoText
        }
      }

      "has the correct second row" which {
        lazy val row = doc.select(".govuk-summary-list__row").get(1)

        "contains the correct first element text" in {
          row.select(".govuk-summary-list__key").text shouldBe tableRowTwoElementOneText
        }

        "contains the correct second element text" in {
          row.select(".govuk-summary-list__value").text shouldBe tableRowTwoElementTwoText
        }
      }

      "has the correct third row" which {
        lazy val row = doc.select(".govuk-summary-list__row").get(2)

        "contains the correct first element text" in {
          row.select(".govuk-summary-list__key").text shouldBe tableRowThreeElementOneText
        }

        "contains the correct second element text" in {
          row.select(".govuk-summary-list__value").text shouldBe tableRowThreeElementTwoText
        }
      }
    }

    "have a paragraph describing details of the failed lookup" in {
      doc.select("p.govuk-body").get(0).text() shouldBe detailsText
    }

    "have a paragraph describing possible causes" in {
      doc.select("p.govuk-body").get(1).text() shouldBe causesText
    }

    "include a list" which {

      "has the first cause listed" in {
        doc.select("ul.govuk-list.govuk-list--bullet li").get(0).text() shouldBe causeOneText
      }

      "has the second cause listed" in {
        doc.select("ul.govuk-list.govuk-list--bullet li").get(1).text() shouldBe causeTwoText
      }
    }

    "have a paragraph describing a possible resolution" in {
      doc.select("p.govuk-body").get(2).text() shouldBe suggestionsText
    }

    "have a copyright message in the footer" in {
      doc.select("footer .govuk-footer__copyright-logo").text() shouldBe copyrightText
    }
  }
}
