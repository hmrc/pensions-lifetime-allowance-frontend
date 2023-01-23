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

package controllers
import play.api.i18n.Lang
import testHelpers.FakeApplication

class PlaLanguageControllerSpec extends FakeApplication {

  lazy val controller = fakeApplication.injector.instanceOf[PlaLanguageController]

  "PlaLanguageController" should {

    "have a call to the correct url for changing languages" in {
      controller.langToCall("en").url shouldBe controllers.routes.PlaLanguageController.switchToLanguage("en").url
    }

    "have the correct fallback url" in {
      controller.fallbackURL shouldBe "/"
    }

    "have the correct map of languages" in {
      controller.languageMap shouldBe Map(
        "english" -> Lang("en"),
        "cymraeg" -> Lang("cy")
      )
    }
  }
}
