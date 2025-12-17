/*
 * Copyright 2025 HM Revenue & Customs
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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers.CACHE_CONTROL
import play.twirl.api.HtmlFormat
import testHelpers._
import views.html.pages.fallback.technicalError

class AmendControllerErrorHelperSpec
    extends FakeApplication
    with MockitoSugar
    with BeforeAndAfterEach
    with AmendControllerErrorHelper
    with I18nSupport {

  private val messagesControllerComponents: MessagesControllerComponents =
    inject[MessagesControllerComponents]

  override val messagesApi: MessagesApi = messagesControllerComponents.messagesApi

  implicit val fakeRequest: FakeRequest[_] = FakeRequest()
  implicit val mockMessage: Messages       = messagesApi.preferred(fakeRequest)

  val technicalError: technicalError = mock[technicalError]

  override def beforeEach(): Unit =
    reset(technicalError)

  "couldNotRetrieveModelForNino" should {
    "return the correctly formatted log message" in {
      couldNotRetrieveModelForNino(
        "NINO",
        "WHEN"
      ) shouldBe "Could not retrieve amend protection model for user with nino NINO WHEN"
    }
  }

  "buildTechnicalError" should {
    "return the technical error page" in {
      when(technicalError.apply()(any(), any())).thenReturn(HtmlFormat.raw("html"))

      val error = buildTechnicalError(technicalError)

      error.header.headers.get(CACHE_CONTROL) shouldBe Some("no-cache")
    }
  }

}
