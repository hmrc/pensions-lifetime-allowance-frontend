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

package config

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.inject.guice.GuiceApplicationBuilder

class AppConfigSpec extends AnyWordSpec with Matchers {

  private def makeConfig(values: (String, Any)*): AppConfig =
    new GuiceApplicationBuilder()
      .configure(values.toMap)
      .configure(
        "metrics.jvm"     -> false,
        "metrics.logback" -> false
      )
      .build()
      .injector
      .instanceOf[AppConfig]

  private val localAppConfig: AppConfig = makeConfig()

  private val stagingAppConfig: AppConfig = makeConfig(
    "platform.frontend.host" -> "https://www.staging.tax.service.gov.uk"
  )

  "serviceNavigationAccountHomeUrl" should {
    "use platform-specific url host" when {
      "platform.frontend.host is defined" in {
        stagingAppConfig.serviceNavigationAccountHomeUrl shouldBe "https://www.staging.tax.service.gov.uk/personal-account"
      }
    }

    "fallback to localhost with correct port" when {
      "platform.frontend.host is not defined" in {
        localAppConfig.serviceNavigationAccountHomeUrl shouldBe "http://localhost:9232/personal-account"
      }
    }
  }

  "serviceNavigationMessagesUrl" should {
    "use platform-specific url host" when {
      "platform.frontend.host is defined" in {
        stagingAppConfig.serviceNavigationMessagesUrl shouldBe "https://www.staging.tax.service.gov.uk/personal-account/messages"
      }
    }

    "fallback to localhost with correct port" when {
      "platform.frontend.host is not defined" in {
        localAppConfig.serviceNavigationMessagesUrl shouldBe "http://localhost:9232/personal-account/messages"
      }
    }
  }

  "serviceNavigationCheckProgressUrl" should {
    "use platform-specific url host" when {
      "platform.frontend.host is defined" in {
        stagingAppConfig.serviceNavigationCheckProgressUrl shouldBe "https://www.staging.tax.service.gov.uk/track"
      }
    }

    "fallback to localhost with correct port" when {
      "platform.frontend.host is not defined" in {
        localAppConfig.serviceNavigationCheckProgressUrl shouldBe "http://localhost:9100/track"
      }
    }
  }

  "serviceNavigationProfileAndSettingsUrl" should {
    "use platform-specific url host" when {
      "platform.frontend.host is defined" in {
        stagingAppConfig.serviceNavigationProfileAndSettingsUrl shouldBe "https://www.staging.tax.service.gov.uk/personal-account/profile-and-settings"
      }
    }

    "fallback to localhost with correct port" in {
      localAppConfig.serviceNavigationProfileAndSettingsUrl shouldBe "http://localhost:9232/personal-account/profile-and-settings"
    }
  }

}
