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

package testHelpers.ViewSpecHelpers

import config.wiring.{PlaFormPartialRetriever, SessionCookieCryptoFilterWrapper}
import config.{FrontendAppConfig, PlaContext}
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import testHelpers.{FakeApplication, MockTemplateRenderer}
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

trait CommonViewSpecHelper extends FakeApplication with CommonMessages with MockitoSugar {

  implicit val application = fakeApplication
  val http = mock[DefaultHttpClient]
  val sessionCookieCryptoFilterWrapper = mock[SessionCookieCryptoFilterWrapper]
  implicit lazy val fakeRequest = FakeRequest()
  implicit lazy val renderer = MockTemplateRenderer.renderer
  implicit val partialRetriever = mock[PlaFormPartialRetriever]

  implicit val mockAppConfig = fakeApplication.injector.instanceOf[FrontendAppConfig]
  implicit val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  implicit val plaContext = mock[PlaContext]

}

