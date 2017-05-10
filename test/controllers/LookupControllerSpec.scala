/*
 * Copyright 2017 HM Revenue & Customs
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

import connectors.{KeyStoreConnector, PLAConnector}
import models.PSALookupRequest
import org.mockito.Matchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.http.Status
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}

import scala.concurrent.Future

class LookupControllerSpec extends PlayMessagesSpec with BeforeAndAfterEach with MockitoSugar {

  implicit val hc = HeaderCarrier()

  private val mockKeyStoreConnector = mock[KeyStoreConnector]
  private val mockPLAConnector = mock[PLAConnector]

  private val sessionId = SessionKeys.sessionId -> "lookup-test"

  val controller = new LookupController(messagesApi, app)

  override def beforeEach() = reset(mockKeyStoreConnector, mockPLAConnector)

  "LookupController" should {
    "return 200" in {
      val request = FakeRequest().withSession(sessionId)
      keystoreFetchCondition[PSALookupRequest](None)
      val result = controller.displayLookupForm.apply(request)
      implicit val messages = getMessages(request)
      status(result) mustBe Status.OK
      contentAsString(result) must include(Messages("psa.lookup.form.title"))
      verify(mockKeyStoreConnector, times(1)).fetchAndGetFormData(any())(any(), any())
    }

    "return 200 with populated form" in {
      val request = FakeRequest().withSession(sessionId)
      keystoreFetchCondition[PSALookupRequest](Some(PSALookupRequest("PSA87654321S", "A123456A")))
      val result = controller.displayLookupForm.apply(request)
      implicit val messages = getMessages(request)
      status(result) mustBe Status.OK
      contentAsString(result) must include(Messages("psa.lookup.form.title"))
      verify(mockKeyStoreConnector, times(1)).fetchAndGetFormData(any())(any(), any())
      contentAsString(result) must include("A123456A")
    }
  }

  def keystoreFetchCondition[T](data: Option[T]): Unit = when(mockKeyStoreConnector.fetchAndGetFormData[T](any())(any(), any())).thenReturn(Future.successful(data))
}
