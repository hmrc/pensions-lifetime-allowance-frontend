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

import config.{FrontendAppConfig, PlaContext}
import connectors.PLAConnector
import models.cache.CacheMap
import models.{PSALookupRequest, PSALookupResult}
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.i18n.Messages
import play.api.libs.json.{JsNumber, JsString, JsValue, Json}
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionCacheService
import testHelpers.FakeApplication
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, SessionKeys, UpstreamErrorResponse}
import uk.gov.hmrc.http.client.HttpClientV2
import utils.ActionWithSessionId
import views.html.pages.lookup._

import scala.concurrent.Future

class LookupProtectionNotificationControllerSpec extends FakeApplication with BeforeAndAfterEach with MockitoSugar {

  private val sessionId                            = SessionKeys.sessionId -> "lookup-test"
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
  val mockPlaConnector: PLAConnector               = mock[PLAConnector]
  val mockMCC: MessagesControllerComponents        = fakeApplication().injector.instanceOf[MessagesControllerComponents]
  val mockActionWithSessionId: ActionWithSessionId = fakeApplication().injector.instanceOf[ActionWithSessionId]
  val mockHttp: HttpClientV2                       = mock[HttpClientV2]

  implicit val mockAppConfig: FrontendAppConfig = fakeApplication().injector.instanceOf[FrontendAppConfig]
  implicit val mockPlaContext: PlaContext       = mock[PlaContext]
  implicit val mockMessages: Messages           = mock[Messages]
  implicit val system: ActorSystem              = ActorSystem()
  implicit val materializer: Materializer       = mock[Materializer]
  implicit val hc: HeaderCarrier                = HeaderCarrier()
  implicit val application                      = mock[Application]

  implicit val mockPsa_lookup_not_found_results: psa_lookup_not_found_results =
    app.injector.instanceOf[psa_lookup_not_found_results]

  implicit val mockPla_protection_guidance: pla_protection_guidance = app.injector.instanceOf[pla_protection_guidance]

  implicit val mockPsa_lookup_protection_notification_no_form: psa_lookup_protection_notification_no_form =
    app.injector.instanceOf[psa_lookup_protection_notification_no_form]

  implicit val mockPsa_lookup_results: psa_lookup_results = app.injector.instanceOf[psa_lookup_results]

  implicit val mockPsa_lookup_scheme_admin_ref_form: psa_lookup_scheme_admin_ref_form =
    app.injector.instanceOf[psa_lookup_scheme_admin_ref_form]

  implicit val formWithCSRF: FormWithCSRF = app.injector.instanceOf[FormWithCSRF]

  class Setup {

    val controller = new LookupProtectionNotificationController(
      mockSessionCacheService,
      mockPlaConnector,
      mockActionWithSessionId,
      mockMCC,
      mockPsa_lookup_protection_notification_no_form
    )

  }

  override def beforeEach(): Unit =
    reset(mockPlaConnector)

  private val validPNNForm = Seq(
    "lifetimeAllowanceReference" -> "IP141000000000A"
  )

  private val invalidPNNForm = Seq(
    "lifetimeAllowanceReference" -> ""
  )

  private val plaReturnJson = Json.parse("""{
                                           |  "pensionSchemeAdministratorCheckReference": "PSA12345678A",
                                           |  "ltaType": 5,
                                           |  "psaCheckResult": 1,
                                           |  "protectedAmount": 25000,
                                           |  "protectionNotificationNumber": "IP14000000000A"
                                           |}""".stripMargin)

  private val cacheData: Map[String, JsValue] = Map(
    "pensionSchemeAdministratorCheckReference" -> JsString(""),
    "ltaType"                                  -> JsNumber(5),
    "psaCheckResult"                           -> JsNumber(1),
    "protectedAmount"                          -> JsNumber(25000),
    "protectionNotificationNumber"             -> JsString("IP14000000000A")
  )

  private val mockCacheMap = CacheMap("psa-lookup-result", cacheData)

  "LookupProtectionNotificationController" should {

    "return 200 with correct message on pnn form" in new Setup {
      cacheFetchCondition[PSALookupRequest](Some(PSALookupRequest("PSAREF")))

      val request = FakeRequest().withSession(sessionId)
      val result  = controller.displayProtectionNotificationNoForm.apply(request)

      status(result) shouldBe OK
    }

    "redirect when no data entered on first page for pnn form" in new Setup {
      cacheFetchCondition[PSALookupRequest](None)

      val request = FakeRequest().withSession(sessionId)
      val result  = controller.displayProtectionNotificationNoForm.apply(request)

      status(result) shouldBe SEE_OTHER
      redirectLocation(
        result
      ).get shouldBe routes.LookupSchemeAdministratorReferenceController.displaySchemeAdministratorReferenceForm.url
    }

    "submit pnn form with valid data and redirect to results page" in new Setup {
      cacheFetchCondition[PSALookupRequest](Some(PSALookupRequest("PSA REF")))
      plaConnectorReturn(HttpResponse(status = OK, json = plaReturnJson, headers = Map.empty))

      val request = FakeRequest().withSession(sessionId).withFormUrlEncodedBody(validPNNForm: _*).withMethod("POST")

      cacheSaveCondition[PSALookupResult](mockCacheMap)

      val result = controller.submitProtectionNotificationNoForm.apply(request)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe routes.LookupController.displayLookupResults.url
    }

    "submit pnn form with valid data and redirect when a NOT FOUND is returned" in new Setup {
      cacheFetchCondition[PSALookupRequest](Some(PSALookupRequest("PSA REF")))
      plaConnectorReturn(HttpResponse(status = OK, json = plaReturnJson, headers = Map.empty))

      val request = FakeRequest().withSession(sessionId).withFormUrlEncodedBody(validPNNForm: _*).withMethod("POST")

      when(mockPlaConnector.psaLookup(any(), any())(any(), any()))
        .thenReturn(Future.failed(UpstreamErrorResponse("message", NOT_FOUND, NOT_FOUND)))
      cacheSaveCondition[PSALookupResult](mockCacheMap)

      val result = controller.submitProtectionNotificationNoForm.apply(request)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe routes.LookupController.displayNotFoundResults.url
    }

    "display errors when invalid data entered for pnn form" in new Setup {
      val request = FakeRequest().withSession(sessionId).withFormUrlEncodedBody(invalidPNNForm: _*).withMethod("POST")

      cacheSaveCondition[PSALookupRequest](mockCacheMap)

      val result = controller.submitProtectionNotificationNoForm.apply(request)

      status(result) shouldBe BAD_REQUEST
    }

    "redirect to the administrator reference form when PSA request data not found on submission" in new Setup {
      val request = FakeRequest().withSession(sessionId).withFormUrlEncodedBody(validPNNForm: _*).withMethod("POST")

      cacheFetchCondition[PSALookupRequest](None)

      val result = controller.submitProtectionNotificationNoForm.apply(request)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(
        routes.LookupSchemeAdministratorReferenceController.displaySchemeAdministratorReferenceForm.url
      )
    }

  }

  def cacheFetchCondition[T](data: Option[T]): Unit =
    when(mockSessionCacheService.fetchAndGetFormData[T](any())(any(), any()))
      .thenReturn(Future.successful(data))

  def cacheSaveCondition[T](data: CacheMap): Unit =
    when(mockSessionCacheService.saveFormData[T](any(), any())(any(), any()))
      .thenReturn(Future.successful(data))

  def plaConnectorReturn(data: HttpResponse): Unit = when(mockPlaConnector.psaLookup(any(), any())(any(), any()))
    .thenReturn(Future.successful(data))

}
