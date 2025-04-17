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

import config.wiring.SessionCookieCryptoFilterWrapper
import config.{FrontendAppConfig, PlaContext}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import testHelpers.FakeApplication
import uk.gov.hmrc.http.client.HttpClientV2

trait CommonViewSpecHelper extends FakeApplication with CommonMessages with MockitoSugar {

  implicit val application: Application                  = fakeApplication()
  val http                                               = mock[HttpClientV2]
  val sessionCookieCryptoFilterWrapper                   = mock[SessionCookieCryptoFilterWrapper]
  implicit lazy val fakeRequest: FakeRequest[AnyContent] = FakeRequest()

  implicit val mockAppConfig: FrontendAppConfig = fakeApplication().injector.instanceOf[FrontendAppConfig]

  implicit val mockMessage: Messages =
    fakeApplication().injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  implicit val plaContext: PlaContext = mock[PlaContext]

}
