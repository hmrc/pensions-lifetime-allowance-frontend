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

import connectors.{KeyStoreConnector, PLAConnector, PdfGeneratorConnector}
import controllers.LookupController.pnnForm
import forms.{PSALookupProtectionNotificationNoForm, PSALookupSchemeAdministratorReferenceForm}
import models.{PSALookupRequest, PSALookupResult}
import org.mockito.Matchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.data.{Form, FormError}
import play.api.http.Status
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsNumber, JsString, JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import testHelpers.MockTemplateRenderer
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.renderer.TemplateRenderer

import scala.concurrent.Future
import uk.gov.hmrc.http.{ HeaderCarrier, HttpResponse, SessionKeys }

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

    val psaRefForm: Form[String] = PSALookupSchemeAdministratorReferenceForm.psaRefForm
    val pnnForm: Form[String] = PSALookupProtectionNotificationNoForm.pnnForm
    override implicit val templateRenderer: TemplateRenderer = MockTemplateRenderer

    val lookupRequestID = "psa-lookup-request"
    val lookupResultID = "psa-lookup-result"
  }

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

      status(result) mustBe OK
      contentAsString(result) must include(Messages("psa.lookup.form.psaref.hint"))
    }

    "submit psaRef form with valid data and redirect to pnn form" in {
      val request = FakeRequest().withSession(sessionId).withFormUrlEncodedBody(validPSARefForm: _*)

      keystoreSaveCondition[PSALookupRequest](mockCacheMap)

      val result = TestController.submitSchemeAdministratorReferenceForm.apply(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).get mustBe routes.LookupController.displayProtectionNotificationNoForm().url
    }

    "display errors when invalid data entered for psaRef form" in {
      val request = FakeRequest().withSession(sessionId).withFormUrlEncodedBody(invalidPSARefForm: _*)

      keystoreSaveCondition[PSALookupRequest](mockCacheMap)

      val result = TestController.submitSchemeAdministratorReferenceForm.apply(request)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) must include(Messages("psa.lookup.form.psaref.required"))
    }

    "return 200 with correct message on pnn form" in {
      keystoreFetchCondition[PSALookupRequest](Some(PSALookupRequest("PSAREF")))

      val request = FakeRequest().withSession(sessionId)
      val result = TestController.displayProtectionNotificationNoForm.apply(request)

      status(result) mustBe OK
      contentAsString(result) must include(Messages("psa.lookup.form.pnn.hint"))
    }

    "redirect when no data entered on first page for pnn form" in {
      keystoreFetchCondition[PSALookupRequest](None)

      val request = FakeRequest().withSession(sessionId)
      val result = TestController.displayProtectionNotificationNoForm.apply(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).get mustBe routes.LookupController.displaySchemeAdministratorReferenceForm().url
    }

    "submit pnn form with valid data and redirect to results page" in {
      keystoreFetchCondition[PSALookupRequest](Some(PSALookupRequest("PSA REF")))
      plaConnectorReturn(HttpResponse(OK, Some(plaReturnJson)))

      val request = FakeRequest().withSession(sessionId).withFormUrlEncodedBody(validPNNForm: _*)

      keystoreSaveCondition[PSALookupResult](mockCacheMap)

      val result = TestController.submitProtectionNotificationNoForm.apply(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).get mustBe routes.LookupController.displayLookupResults().url
    }

    "display errors when invalid data entered for pnn form" in {
      val request = FakeRequest().withSession(sessionId).withFormUrlEncodedBody(invalidPNNForm: _*)

      keystoreSaveCondition[PSALookupRequest](mockCacheMap)

      val result = TestController.submitProtectionNotificationNoForm.apply(request)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) must include(Messages("psa.lookup.form.pnn.required"))
    }

    "return 200 with correct message on results page" in {
      val request = FakeRequest().withSession(sessionId)
      keystoreFetchCondition[PSALookupResult](Some(Json.fromJson[PSALookupResult](plaReturnJson).get))

      val result = TestController.displayLookupResults.apply(request)

      status(result) mustBe OK
      contentAsString(result) must include(Messages("psa.lookup.results.title"))
    }

    "redirect when no result data is stored on results page" in {
      val request = FakeRequest().withSession(sessionId)
      keystoreFetchCondition[PSALookupResult](None)

      val result = TestController.displayLookupResults.apply(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).get mustBe routes.LookupController.displaySchemeAdministratorReferenceForm().url
    }

    "return 200 with correct message on not found results page" in {
      val request = FakeRequest().withSession(sessionId)
      keystoreFetchCondition[PSALookupRequest](Some(Json.fromJson[PSALookupRequest](psaRequestJson).get))

      val result = TestController.displayNotFoundResults.apply(request)

      status(result) mustBe OK
      contentAsString(result) must include(Messages("psa.lookup.not-found.results.table.try-again"))
    }

    "redirect when no result data is stored on not found results page" in {
      val request = FakeRequest().withSession(sessionId)
      keystoreFetchCondition[PSALookupRequest](None)

      val result = TestController.displayLookupResults.apply(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).get mustBe routes.LookupController.displaySchemeAdministratorReferenceForm().url
    }

    "return 200 with correct message on protection type guidance page" in {

      val request = FakeRequest().withSession(sessionId)
      val result = TestController.displayProtectionTypeGuidance.apply(request)

      status(result) mustBe OK
      contentAsString(result) must include(Messages("psa.lookup.protection-guidance.title"))
      contentAsString(result) must include(Messages("psa.lookup.protection-guidance.protection-type-primary-label"))
    }

  }

  def keystoreFetchCondition[T](data: Option[T]): Unit = when(mockKeyStoreConnector.fetchAndGetFormData[T](any())(any(), any()))
    .thenReturn(Future.successful(data))

  def keystoreSaveCondition[T](data: CacheMap): Unit = when(mockKeyStoreConnector.saveFormData[T](any(), any())(any(), any()))
    .thenReturn(Future.successful(data))

  def plaConnectorReturn(data: HttpResponse): Unit = when(mockPLAConnector.psaLookup(any(), any())(any()))
    .thenReturn(Future.successful(data))
}
