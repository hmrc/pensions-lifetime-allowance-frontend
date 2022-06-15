/*
 * Copyright 2022 HM Revenue & Customs
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

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import config.wiring.PlaFormPartialRetriever
import config.{FrontendAppConfig, LocalTemplateRenderer, PlaContext}
import connectors.{KeyStoreConnector, PLAConnector}
import views.html.pages.lookup.{pla_protection_guidance, psa_lookup_not_found_results, psa_lookup_protection_notification_no_form, psa_lookup_results, psa_lookup_scheme_admin_ref_form}
import models.{PSALookupRequest, PSALookupResult}
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.i18n.Messages
import play.api.libs.json.{JsNumber, JsString, JsValue, Json}
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import testHelpers.{FakeApplication, MockTemplateRenderer}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, SessionKeys, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import uk.gov.hmrc.play.views.html.helpers.{ErrorSummary, FormWithCSRF}
import utils.ActionWithSessionId

import scala.concurrent.Future

class LookupControllerSpec extends FakeApplication with BeforeAndAfterEach with MockitoSugar {

  private val sessionId = SessionKeys.sessionId -> "lookup-test"
  val mockKeyStoreConnector: KeyStoreConnector = mock[KeyStoreConnector]
  val mockPlaConnector: PLAConnector = mock[PLAConnector]
  val mockMCC: MessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val mockActionWithSessionId: ActionWithSessionId = fakeApplication.injector.instanceOf[ActionWithSessionId]

  val mockHttp: DefaultHttpClient = mock[DefaultHttpClient]
  implicit val templateRenderer: LocalTemplateRenderer = MockTemplateRenderer.renderer
  implicit val partialRetriever: PlaFormPartialRetriever = mock[PlaFormPartialRetriever]
  implicit val mockAppConfig: FrontendAppConfig = fakeApplication.injector.instanceOf[FrontendAppConfig]
  implicit val mockPlaContext: PlaContext = mock[PlaContext]
  implicit val mockMessages: Messages = mock[Messages]
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val application = mock[Application]
  implicit val mockPsa_lookup_not_found_results: psa_lookup_not_found_results = app.injector.instanceOf[psa_lookup_not_found_results]
  implicit val mockPla_protection_guidance: pla_protection_guidance = app.injector.instanceOf[pla_protection_guidance]
  implicit val mockPsa_lookup_protection_notification_no_form: psa_lookup_protection_notification_no_form = app.injector.instanceOf[psa_lookup_protection_notification_no_form]
  implicit val mockPsa_lookup_results: psa_lookup_results = app.injector.instanceOf[psa_lookup_results]
  implicit val mockPsa_lookup_scheme_admin_ref_form: psa_lookup_scheme_admin_ref_form = app.injector.instanceOf[psa_lookup_scheme_admin_ref_form]
  implicit val errorSummary: ErrorSummary = app.injector.instanceOf[ErrorSummary]
  implicit val formWithCSRF: FormWithCSRF = app.injector.instanceOf[FormWithCSRF]

  class Setup {
    val controller = new LookupController(
      mockKeyStoreConnector,
      mockPlaConnector,
      mockActionWithSessionId,
      mockMCC,
      mockPsa_lookup_not_found_results,
      mockPla_protection_guidance,
      mockPsa_lookup_protection_notification_no_form,
      mockPsa_lookup_results,
      mockPsa_lookup_scheme_admin_ref_form,
    )
  }

  override def beforeEach() {
    reset(mockPlaConnector)
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


  "LookupController" should {
    "return 200 with correct message on psaRef form" in new Setup  {
      keystoreFetchCondition[PSALookupRequest](None)

      val request = FakeRequest().withSession(sessionId)
      val result = controller.displaySchemeAdministratorReferenceForm.apply(request)

      status(result) shouldBe OK
    }

    "submit psaRef form with valid data and redirect to pnn form" in new Setup  {
      val request = FakeRequest().withSession(sessionId).withFormUrlEncodedBody(validPSARefForm: _*).withMethod("POST")

      keystoreSaveCondition[PSALookupRequest](mockCacheMap)

      val result = controller.submitSchemeAdministratorReferenceForm.apply(request)

      status(result) shouldBe  SEE_OTHER
      redirectLocation(result).get shouldBe  routes.LookupController.displayProtectionNotificationNoForm.url
    }

    "display errors when invalid data entered for psaRef form" in new Setup  {
      val request = FakeRequest().withSession(sessionId).withFormUrlEncodedBody(invalidPSARefForm: _*).withMethod("POST")

      keystoreSaveCondition[PSALookupRequest](mockCacheMap)

      val result = controller.submitSchemeAdministratorReferenceForm.apply(request)

      status(result) shouldBe  BAD_REQUEST
    }

    "return 200 with correct message on pnn form" in new Setup  {
      keystoreFetchCondition[PSALookupRequest](Some(PSALookupRequest("PSAREF")))

      val request = FakeRequest().withSession(sessionId)
      val result = controller.displayProtectionNotificationNoForm.apply(request)

      status(result) shouldBe  OK
    }

    "redirect when no data entered on first page for pnn form" in new Setup  {
      keystoreFetchCondition[PSALookupRequest](None)

      val request = FakeRequest().withSession(sessionId)
      val result = controller.displayProtectionNotificationNoForm.apply(request)

      status(result) shouldBe  SEE_OTHER
      redirectLocation(result).get shouldBe  routes.LookupController.displaySchemeAdministratorReferenceForm.url
    }

    "submit pnn form with valid data and redirect to results page" in new Setup  {
      keystoreFetchCondition[PSALookupRequest](Some(PSALookupRequest("PSA REF")))
      plaConnectorReturn(HttpResponse(status = OK, json = plaReturnJson, headers = Map.empty))

      val request = FakeRequest().withSession(sessionId).withFormUrlEncodedBody(validPNNForm: _*).withMethod("POST")

      keystoreSaveCondition[PSALookupResult](mockCacheMap)

      val result = controller.submitProtectionNotificationNoForm.apply(request)

      status(result) shouldBe  SEE_OTHER
      redirectLocation(result).get shouldBe  routes.LookupController.displayLookupResults.url
    }

    "submit pnn form with valid data and redirect when a NOT FOUND is returned" in new Setup  {
      keystoreFetchCondition[PSALookupRequest](Some(PSALookupRequest("PSA REF")))
      plaConnectorReturn(HttpResponse(status = OK, json = plaReturnJson, headers = Map.empty))

      val request = FakeRequest().withSession(sessionId).withFormUrlEncodedBody(validPNNForm: _*).withMethod("POST")

      when(mockPlaConnector.psaLookup(any(), any())(any(), any()))
        .thenReturn(Future.failed(UpstreamErrorResponse("message", NOT_FOUND, NOT_FOUND)))
      keystoreSaveCondition[PSALookupResult](mockCacheMap)

      val result = controller.submitProtectionNotificationNoForm.apply(request)

      status(result) shouldBe  SEE_OTHER
      redirectLocation(result).get shouldBe  routes.LookupController.displayNotFoundResults.url
    }

    "display errors when invalid data entered for pnn form" in new Setup  {
      val request = FakeRequest().withSession(sessionId).withFormUrlEncodedBody(invalidPNNForm: _*).withMethod("POST")

      keystoreSaveCondition[PSALookupRequest](mockCacheMap)

      val result = controller.submitProtectionNotificationNoForm.apply(request)

      status(result) shouldBe  BAD_REQUEST
    }

    "redirect to the administrator reference form when PSA request data not found on submission" in new Setup  {
      val request = FakeRequest().withSession(sessionId).withFormUrlEncodedBody(validPNNForm: _*).withMethod("POST")

      keystoreFetchCondition[PSALookupRequest](None)

      val result = controller.submitProtectionNotificationNoForm.apply(request)

      status(result) shouldBe  SEE_OTHER
      redirectLocation(result) shouldBe  Some(routes.LookupController.displaySchemeAdministratorReferenceForm.url)
    }

    "return 200 with correct message on results page" in new Setup  {
      val request = FakeRequest().withSession(sessionId)
      keystoreFetchCondition[PSALookupResult](Some(Json.fromJson[PSALookupResult](plaReturnJson).get))

      val result = controller.displayLookupResults.apply(request)

      status(result) shouldBe  OK
    }

    "redirect when no result data is stored on results page" in new Setup  {
      val request = FakeRequest().withSession(sessionId)
      keystoreFetchCondition[PSALookupResult](None)

      val result = controller.displayLookupResults.apply(request)

      status(result) shouldBe  SEE_OTHER
      redirectLocation(result).get shouldBe  routes.LookupController.displaySchemeAdministratorReferenceForm.url
    }

    "return 200 with correct message on not found results page" in new Setup  {
      val request = FakeRequest().withSession(sessionId)
      keystoreFetchCondition[PSALookupRequest](Some(Json.fromJson[PSALookupRequest](psaRequestJson).get))

      val result = controller.displayNotFoundResults.apply(request)

      status(result) shouldBe  OK
    }

    "redirect when no result data is stored on not found results page" in new Setup  {
      val request = FakeRequest().withSession(sessionId)
      keystoreFetchCondition[PSALookupRequest](None)

      val result = controller.displayLookupResults.apply(request)

      status(result) shouldBe  SEE_OTHER
      redirectLocation(result).get shouldBe  routes.LookupController.displaySchemeAdministratorReferenceForm.url
    }

    "return 200 with correct message on protection type guidance page" in new Setup  {

      val request = FakeRequest().withSession(sessionId)
      val result = controller.displayProtectionTypeGuidance.apply(request)

      status(result) shouldBe  OK
    }

  }

  def keystoreFetchCondition[T](data: Option[T]): Unit = when(mockKeyStoreConnector.fetchAndGetFormData[T](any())(any(), any()))
    .thenReturn(Future.successful(data))

  def keystoreSaveCondition[T](data: CacheMap): Unit = when(mockKeyStoreConnector.saveFormData[T](any(), any())(any(), any()))
    .thenReturn(Future.successful(data))

  def plaConnectorReturn(data: HttpResponse): Unit = when(mockPlaConnector.psaLookup(any(), any())(any(), any()))
    .thenReturn(Future.successful(data))
}
