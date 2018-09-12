/*
 * Copyright 2018 HM Revenue & Customs
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

import config.LocalTemplateRenderer
import config.wiring.PlaFormPartialRetriever
import connectors.{KeyStoreConnector, PLAConnector}
import models.{PSALookupRequest, PSALookupResult}
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.libs.json.{JsNumber, JsString, JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, SessionKeys, Upstream4xxResponse}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class LookupControllerSpec extends UnitSpec with BeforeAndAfterEach with MockitoSugar with WithFakeApplication {

  implicit val hc = HeaderCarrier()

  private val mockKeyStoreConnector = mock[KeyStoreConnector]
  private val mockPLAConnector = mock[PLAConnector]
  private val mockPartialRetriever = mock[PlaFormPartialRetriever]
  private val mockTemplateRenderer = mock[LocalTemplateRenderer]

  private val sessionId = SessionKeys.sessionId -> "lookup-test"

  val TestController = new LookupController(mockKeyStoreConnector, mockPLAConnector, mockPartialRetriever, mockTemplateRenderer)

  private val validPSARefForm = Seq(
    "pensionSchemeAdministratorCheckReference" -> "PSA12345678A"
  )

  private val invalidPSARefForm = Seq(
    "pensionSchemeAdministratorCheckReference" -> ""
  )

  private val validPNNForm = Seq(
    "lifetimeAllowanceReference" -> "IP141000000000A"
  )

  private val invalidPNNForm = Seq(
    "lifetimeAllowanceReference" -> ""
  )

  private val plaReturnJson = Json.parse(
    """{
      |  "pensionSchemeAdministratorCheckReference": "PSA12345678A",
      |  "ltaType": 5,
      |  "psaCheckResult": 1,
      |  "protectedAmount": 25000,
      |  "protectionNotificationNumber": "IP14000000000A"
      |}""".stripMargin)

  private val plaInvalidReturnJson = Json.parse(
    """{
      |  "pensionSchemeAdministratorCheckReference": "PSA12345678A",
      |  "ltaType": 5,
      |  "psaCheckResult": 0,
      |  "protectedAmount": 25000,
      |  "protectionNotificationNumber": "IP14000000000A"
      |}""".stripMargin)

  private val psaRequestJson = Json.parse(
    """{
      | "pensionSchemeAdministratorCheckReference": "PSA12345678A",
      | "lifetimeAllowanceReference": "IP14000000000A"
      | }""".stripMargin
  )

  private val cacheData: Map[String, JsValue] = Map(
    "pensionSchemeAdministratorCheckReference" -> JsString(""),
    "ltaType" -> JsNumber(5),
    "psaCheckResult" -> JsNumber(1),
    "protectedAmount" -> JsNumber(25000),
    "protectionNotificationNumber" -> JsString("IP14000000000A")
  )

  private val mockCacheMap = CacheMap("psa-lookup-result", cacheData)

  override def beforeEach() = reset(mockKeyStoreConnector, mockPLAConnector)

  "LookupController" should {
    "return 200 with correct message on psaRef form" in {
      keystoreFetchCondition[PSALookupRequest](None)

      val request = FakeRequest().withSession(sessionId)
      val result = TestController.displaySchemeAdministratorReferenceForm.apply(request)

      status(result) shouldBe  OK
    }

    "submit psaRef form with valid data and redirect to pnn form" in {
      val request = FakeRequest().withSession(sessionId).withFormUrlEncodedBody(validPSARefForm: _*)

      keystoreSaveCondition[PSALookupRequest](mockCacheMap)

      val result = TestController.submitSchemeAdministratorReferenceForm.apply(request)

      status(result) shouldBe  SEE_OTHER
      redirectLocation(result).get shouldBe  routes.LookupController.displayProtectionNotificationNoForm().url
    }

    "display errors when invalid data entered for psaRef form" in {
      val request = FakeRequest().withSession(sessionId).withFormUrlEncodedBody(invalidPSARefForm: _*)

      keystoreSaveCondition[PSALookupRequest](mockCacheMap)

      val result = TestController.submitSchemeAdministratorReferenceForm.apply(request)

      status(result) shouldBe  BAD_REQUEST
    }

    "return 200 with correct message on pnn form" in {
      keystoreFetchCondition[PSALookupRequest](Some(PSALookupRequest("PSAREF")))

      val request = FakeRequest().withSession(sessionId)
      val result = TestController.displayProtectionNotificationNoForm.apply(request)

      status(result) shouldBe  OK
    }

    "redirect when no data entered on first page for pnn form" in {
      keystoreFetchCondition[PSALookupRequest](None)

      val request = FakeRequest().withSession(sessionId)
      val result = TestController.displayProtectionNotificationNoForm.apply(request)

      status(result) shouldBe  SEE_OTHER
      redirectLocation(result).get shouldBe  routes.LookupController.displaySchemeAdministratorReferenceForm().url
    }

    "submit pnn form with valid data and redirect to results page" in {
      keystoreFetchCondition[PSALookupRequest](Some(PSALookupRequest("PSA REF")))
      plaConnectorReturn(HttpResponse(OK, Some(plaReturnJson)))

      val request = FakeRequest().withSession(sessionId).withFormUrlEncodedBody(validPNNForm: _*)

      keystoreSaveCondition[PSALookupResult](mockCacheMap)

      val result = TestController.submitProtectionNotificationNoForm.apply(request)

      status(result) shouldBe  SEE_OTHER
      redirectLocation(result).get shouldBe  routes.LookupController.displayLookupResults().url
    }

    "submit pnn form with valid data and redirect when a NOT FOUND is returned" in {
      keystoreFetchCondition[PSALookupRequest](Some(PSALookupRequest("PSA REF")))
      plaConnectorReturn(HttpResponse(OK, Some(plaReturnJson)))

      val request = FakeRequest().withSession(sessionId).withFormUrlEncodedBody(validPNNForm: _*)

      when(mockPLAConnector.psaLookup(any(), any())(any()))
        .thenReturn(Future.failed(Upstream4xxResponse("message", NOT_FOUND, NOT_FOUND)))
      keystoreSaveCondition[PSALookupResult](mockCacheMap)

      val result = TestController.submitProtectionNotificationNoForm.apply(request)

      status(result) shouldBe  SEE_OTHER
      redirectLocation(result).get shouldBe  routes.LookupController.displayNotFoundResults().url
    }

    "display errors when invalid data entered for pnn form" in {
      val request = FakeRequest().withSession(sessionId).withFormUrlEncodedBody(invalidPNNForm: _*)

      keystoreSaveCondition[PSALookupRequest](mockCacheMap)

      val result = TestController.submitProtectionNotificationNoForm.apply(request)

      status(result) shouldBe  BAD_REQUEST
    }

    "redirect to the administrator reference form when PSA request data not found on submission" in {
      val request = FakeRequest().withSession(sessionId).withFormUrlEncodedBody(validPNNForm: _*)

      keystoreFetchCondition[PSALookupRequest](None)

      val result = TestController.submitProtectionNotificationNoForm.apply(request)

      status(result) shouldBe  SEE_OTHER
      redirectLocation(result) shouldBe  Some(routes.LookupController.displaySchemeAdministratorReferenceForm().url)
    }

    "return 200 with correct message on results page" in {
      val request = FakeRequest().withSession(sessionId)
      keystoreFetchCondition[PSALookupResult](Some(Json.fromJson[PSALookupResult](plaReturnJson).get))

      val result = TestController.displayLookupResults.apply(request)

      status(result) shouldBe  OK
    }

    "redirect when no result data is stored on results page" in {
      val request = FakeRequest().withSession(sessionId)
      keystoreFetchCondition[PSALookupResult](None)

      val result = TestController.displayLookupResults.apply(request)

      status(result) shouldBe  SEE_OTHER
      redirectLocation(result).get shouldBe  routes.LookupController.displaySchemeAdministratorReferenceForm().url
    }

    "return 200 with correct message on not found results page" in {
      val request = FakeRequest().withSession(sessionId)
      keystoreFetchCondition[PSALookupRequest](Some(Json.fromJson[PSALookupRequest](psaRequestJson).get))

      val result = TestController.displayNotFoundResults.apply(request)

      status(result) shouldBe  OK
    }

    "redirect when no result data is stored on not found results page" in {
      val request = FakeRequest().withSession(sessionId)
      keystoreFetchCondition[PSALookupRequest](None)

      val result = TestController.displayLookupResults.apply(request)

      status(result) shouldBe  SEE_OTHER
      redirectLocation(result).get shouldBe  routes.LookupController.displaySchemeAdministratorReferenceForm().url
    }

    "return 200 with correct message on protection type guidance page" in {

      val request = FakeRequest().withSession(sessionId)
      val result = TestController.displayProtectionTypeGuidance.apply(request)

      status(result) shouldBe  OK
    }

  }

  def keystoreFetchCondition[T](data: Option[T]): Unit = when(mockKeyStoreConnector.fetchAndGetFormData[T](any())(any(), any()))
    .thenReturn(Future.successful(data))

  def keystoreSaveCondition[T](data: CacheMap): Unit = when(mockKeyStoreConnector.saveFormData[T](any(), any())(any(), any()))
    .thenReturn(Future.successful(data))

  def plaConnectorReturn(data: HttpResponse): Unit = when(mockPLAConnector.psaLookup(any(), any())(any()))
    .thenReturn(Future.successful(data))
}
