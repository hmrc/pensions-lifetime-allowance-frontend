/*
 * Copyright 2026 HM Revenue & Customs
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

package views

import auth.MockConfig
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Mockito.when
import play.api.i18n.Messages
import play.api.mvc.{AnyContent, RequestHeader}
import play.api.test.FakeRequest
import play.twirl.api.Html
import testHelpers.CommonViewSpecHelper
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import views.html.Layout

class LayoutViewSpec extends CommonViewSpecHelper {

  val layoutView: Layout = inject[Layout]

  def renderView(
      pageTitle: String = "Page Title",
      isPsaLookupPage: Boolean = false,
      backLinkEnabled: Boolean = true,
      isUserResearchBannerVisible: Boolean = false,
      timeoutEnabled: Boolean = true,
      additionalPrintCss: Boolean = false,
      content: Html = Text("Content").asHtml
  )(implicit requestHeader: RequestHeader): Document = {
    val html = layoutView(
      pageTitle = pageTitle,
      isPsaLookupPage = isPsaLookupPage,
      backLinkEnabled = backLinkEnabled,
      isUserResearchBannerVisible = isUserResearchBannerVisible,
      timeoutEnabled = timeoutEnabled,
      additionalPrintCss = additionalPrintCss
    )(content)(requestHeader, implicitly[Messages])

    Jsoup.parse(html.toString)
  }

  val signedInRequest: FakeRequest[AnyContent] = fakeRequest.withSession("authToken" -> "some auth token")

  override def beforeEach(): Unit = {
    super.beforeEach()

    when(mockAppConfig.serviceNavigationAccountHomeUrl).thenReturn(MockConfig.serviceNavigationAccountHomeUrl)
    when(mockAppConfig.serviceNavigationMessagesUrl).thenReturn(MockConfig.serviceNavigationMessagesUrl)
    when(mockAppConfig.serviceNavigationCheckProgressUrl).thenReturn(MockConfig.serviceNavigationCheckProgressUrl)
    when(mockAppConfig.serviceNavigationProfileAndSettingsUrl).thenReturn(
      MockConfig.serviceNavigationProfileAndSettingsUrl
    )
    when(mockAppConfig.urBannerLink).thenReturn(MockConfig.urBannerLink)
  }

  "Layout view" should {
    "show correct page title" in {
      val title = "Some Page Title"
      renderView(pageTitle = title).title shouldBe s"$title - $plaBaseAppName - GOV.UK"
    }

    "show the sign out button" when {
      "the user is signed in" in {
        renderView()(signedInRequest).getElementsByClass("hmrc-sign-out-nav__link") should have size 1
      }
    }

    "not show the sign out button" when {
      "the user is not signed in" in {
        renderView().getElementsByClass("hmrc-sign-out-nav__link") shouldBe empty
      }
    }

    "use correct service name depending on isPsaLookupPage flag" when {
      "isPsaLookupPage is true" in {
        renderView(isPsaLookupPage = true)
          .getElementsByClass("govuk-service-navigation__text")
          .textSeq shouldEqual Seq(
          "Check your pension protections"
        )
      }

      "isPsaLookupPage is false" in {
        renderView(isPsaLookupPage = false)
          .getElementsByClass("govuk-service-navigation__text")
          .textSeq shouldEqual Seq(
          "Check your pension protections and enhancements"
        )
      }
    }

    "show the back button" when {
      "the backLinkEnabled flag is true" in {
        renderView(backLinkEnabled = true).getElementsByClass("govuk-back-link") should have size 1
      }
    }

    "not show the back button" when {
      "the backLinkEnabled flag is false" in {
        renderView(backLinkEnabled = false).getElementsByClass("govuk-back-link") shouldBe empty
      }
    }

    "show the user research banner" when {
      "the isUserResearchBannerVisible flag is true" in {
        renderView(isUserResearchBannerVisible = true).getElementsByClass(
          "hmrc-user-research-banner"
        ) should have size 1
      }
    }

    "not show the user research banner" when {
      "the isUserResearchBannerVisible flag is false" in {
        renderView(isUserResearchBannerVisible = false).getElementsByClass("hmrc-user-research-banner") shouldBe empty
      }
    }

    "include the timeout dialog" when {
      "the timeoutEnabled flag is true" in {
        renderView(timeoutEnabled = true).getElementsByAttribute("data-timeout") should have size 1
      }
    }

    "not include the timeout dialog" when {
      "the timeoutEnabled flag is false" in {
        renderView(timeoutEnabled = false).getElementsByAttribute("data-timeout") shouldBe empty
      }
    }

    "include the additional CSS for the print page" when {
      "the additionalPrintCss flag is true" in {
        renderView(additionalPrintCss = true).getElementsByAttributeValue(
          "href",
          "/check-your-pension-protections-and-enhancements/assets/stylesheets/pla-print.css"
        ) should have size 1
      }
    }

    "not include the additional CSS for the print page" when {
      "the additionalPrintCss flag is false" in {
        renderView(additionalPrintCss = false).getElementsByAttributeValue(
          "href",
          "/check-your-pension-protections-and-enhancements/assets/stylesheets/pla-print.css"
        ) shouldBe empty
      }
    }

    "include the service navigation menu bar" when {
      "the user is signed in" in {
        renderView()(signedInRequest).getElementsByClass("govuk-service-navigation__wrapper") should have size 1
      }
    }

    "not include the service navigation menu bar" when {
      "the user is not signed in" in {
        renderView().getElementsByClass("govuk-service-navigation__wrapper") shouldBe empty
      }
    }

    "include the language picker" when {
      "the user is signed in" in {
        renderView()(signedInRequest).getElementsByClass("hmrc-service-navigation-language-select") should have size 1
      }

      "the user is not signed in" in {
        renderView().getElementsByClass("hmrc-service-navigation-language-select") should have size 1
      }
    }

    "include the technical issues notice at the bottom of the page" in {
      renderView().getElementsByClass("hmrc-report-technical-issue") should have size 1
    }
  }

}
