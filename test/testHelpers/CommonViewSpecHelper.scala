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

package testHelpers

import config.FrontendAppConfig
import org.mockito.Mockito.reset
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.api.Application
import testHelpers.messages.CommonMessages
import uk.gov.hmrc.http.client.HttpClientV2

trait CommonViewSpecHelper extends FakeApplication with CommonMessages with MockitoSugar with BeforeAndAfterEach {

  implicit val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

  override implicit lazy val app: Application = GuiceApplicationBuilder()
    .overrides(play.api.inject.bind[FrontendAppConfig].toInstance(mockAppConfig))
    .build()

  val http: HttpClientV2 = mock[HttpClientV2]

  implicit val fakeRequest: FakeRequest[AnyContent] = FakeRequest()

  implicit val messages: Messages =
    inject[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  override def beforeEach(): Unit = {
    reset(mockAppConfig)
    super.beforeEach()
  }

}
