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

import config.AppConfig
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import testHelpers.FakeApplication
import uk.gov.hmrc.http.SessionKeys
import views.html.pages.psaLookup.psaLookupClosed

class PsaLookupClosedControllerSpec extends FakeApplication with BeforeAndAfterEach with MockitoSugar {

  private val sessionId = SessionKeys.sessionId -> "lookup-test"

  private val mockMCC: MessagesControllerComponents =
    inject[MessagesControllerComponents]

  private val mockPsaLookupClosed: psaLookupClosed = mock[psaLookupClosed]

  private implicit val appConfig: AppConfig = mock[AppConfig]

  private val controller = new PsaLookupClosedController(
    mockMCC,
    mockPsaLookupClosed
  )

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(appConfig)
    reset(mockPsaLookupClosed)

    when(mockPsaLookupClosed.apply()(any(), any())).thenReturn(HtmlFormat.empty)
  }

  private val request = FakeRequest().withSession(sessionId)

  "PsaLookupClosedController.psaLookupClosed" should {

    "return 200 with withdrawnPSALookupJourney view" in {
      val result = controller.psaLookupClosed.apply(request)

      status(result) shouldBe OK
      verify(mockPsaLookupClosed).apply()(any(), any())
    }

  }

}
