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
import models.{PSALookupRequest, PSALookupResult}
import org.mockito.Matchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsNumber, JsString, JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse, SessionKeys}

import scala.concurrent.Future

class LookupControllerSpec extends PlaySpec with BeforeAndAfterEach with MockitoSugar with GuiceOneAppPerSuite {

  implicit override lazy val app: Application = new GuiceApplicationBuilder().
    disable[com.kenshoo.play.metrics.PlayModule].build()

  implicit val hc = HeaderCarrier()

  private val mockKeyStoreConnector = mock[KeyStoreConnector]
  private val mockPLAConnector = mock[PLAConnector]

  private val sessionId = SessionKeys.sessionId -> "lookup-test"

  object TestController extends LookupController {
    override val keyStoreConnector: KeyStoreConnector = mockKeyStoreConnector
    override val plaConnector: PLAConnector = mockPLAConnector
  }

  private val validPostData = Seq(
    "pensionSchemeAdministratorCheckReference" -> "PSA12345678A",
    "lifetimeAllowanceReference" -> "IP161000000000A"
  )

  private val invalidPostData = Seq(
    "pensionSchemeAdministratorCheckReference" -> "",
    "lifetimeAllowanceReference" -> "IP161000000000A"
  )

  private val plaReturnJson = Json.parse(
    """{
      |  "pensionSchemeAdministratorCheckReference": "PSA12345678A",
      |  "ltaType": 5,
      |  "psaCheckResult": 1,
      |  "protectedAmount": 25000
      |}""".stripMargin)

  private val plaInvalidReturnJson = Json.parse(
    """{
      |  "pensionSchemeAdministratorCheckReference": "PSA12345678A",
      |  "ltaType": 5,
      |  "psaCheckResult": 0,
      |  "protectedAmount": 25000
      |}""".stripMargin)

  private val cacheData: Map[String, JsValue] = Map(
    "pensionSchemeAdministratorCheckReference" -> JsString(""),
    "ltaType" -> JsNumber(5),
    "psaCheckResult" -> JsNumber(1),
    "protectedAmount" -> JsNumber(25000)
  )

  private val mockCacheMap = CacheMap("psa-lookup-result", cacheData)

  override def beforeEach() = reset(mockKeyStoreConnector, mockPLAConnector)

  "LookupController" should {
    "return 200 with correct message on form page" in {
      val request = FakeRequest().withSession(sessionId)
      val result = TestController.displayLookupForm.apply(request)

      status(result) mustBe Status.OK
      contentAsString(result) must include(Messages("psa.lookup.form.title"))
    }

    "submit form with valid data and redirect to results page" in {
      val request = FakeRequest().withSession(sessionId).withFormUrlEncodedBody(validPostData: _*)

      plaConnectorReturn(HttpResponse(OK, Some(plaReturnJson)))
      keystoreSaveCondition[PSALookupRequest](mockCacheMap)

      val result = TestController.submitLookupRequest.apply(request)

      status(result) mustBe Status.SEE_OTHER
      redirectLocation(result).get mustBe routes.LookupController.displayLookupResults().url
      verify(mockKeyStoreConnector, times(1)).saveFormData(any(), any())(any(), any())
      verify(mockPLAConnector, times(1)).psaLookup(any(), any())(any())
    }

    "submit form with valid data but invalid protection and reload form page" in {
      val request = FakeRequest().withSession(sessionId).withFormUrlEncodedBody(validPostData: _*)

      plaConnectorReturn(HttpResponse(OK, Some(plaInvalidReturnJson)))

      val result = TestController.submitLookupRequest.apply(request)

      status(result) mustBe Status.BAD_REQUEST
      contentAsString(result) must include(Messages("psa.lookup.form.not-found"))
      verify(mockPLAConnector, times(1)).psaLookup(any(), any())(any())
    }

    "submit form with invalid data and return bad request" in {
      val request = FakeRequest().withSession(sessionId).withFormUrlEncodedBody(invalidPostData: _*)

      val result = TestController.submitLookupRequest.apply(request)

      status(result) mustBe Status.BAD_REQUEST
      contentAsString(result) must include(Messages("psa.lookup.form.psaref.required"))
    }

    "return 200 with correct message on results page" in {
      val request = FakeRequest().withSession(sessionId)
      keystoreFetchCondition[PSALookupResult](Some(Json.fromJson[PSALookupResult](plaReturnJson).get))

      val result = TestController.displayLookupResults.apply(request)

      status(result) mustBe Status.OK
      contentAsString(result) must include(Messages("psa.lookup.results.title"))
    }
  }

  def keystoreFetchCondition[T](data: Option[T]): Unit = when(mockKeyStoreConnector.fetchAndGetFormData[T](any())(any(), any()))
    .thenReturn(Future.successful(data))

  def keystoreSaveCondition[T](data: CacheMap): Unit = when(mockKeyStoreConnector.saveFormData[T](any(), any())(any(), any()))
    .thenReturn(Future.successful(data))

  def plaConnectorReturn(data: HttpResponse): Unit = when(mockPLAConnector.psaLookup(any(), any())(any()))
    .thenReturn(Future.successful(data))
}
