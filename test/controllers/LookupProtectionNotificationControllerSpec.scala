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

import config.FrontendAppConfig
import connectors.PsaLookupConnector
import models.cache.CacheMap
import models.{PsaLookupRequest, PsaLookupResult}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.libs.json.{JsNumber, JsString, JsValue, Json}
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.SessionCacheService
import testHelpers.FakeApplication
import uk.gov.hmrc.http.{HttpResponse, SessionKeys, UpstreamErrorResponse}
import utils.ActionWithSessionId
import views.html.pages.lookup._

import scala.concurrent.{ExecutionContext, Future}

class LookupProtectionNotificationControllerSpec extends FakeApplication with BeforeAndAfterEach with MockitoSugar {

  private val sessionCacheService: SessionCacheService = mock[SessionCacheService]
  private val plaConnector: PsaLookupConnector         = mock[PsaLookupConnector]
  private val messagesControllerComponents             = inject[MessagesControllerComponents]
  private val actionWithSessionId                      = inject[ActionWithSessionId]

  private val psa_lookup_protection_notification_no_form = mock[psa_lookup_protection_notification_no_form]
  private val withdrawnPSALookupJourney                  = mock[withdrawnPSALookupJourney]

  private implicit val appConfig: FrontendAppConfig = mock[FrontendAppConfig]
  private implicit val ec: ExecutionContext         = inject[ExecutionContext]

  private val controller = new LookupProtectionNotificationController(
    sessionCacheService,
    plaConnector,
    actionWithSessionId,
    messagesControllerComponents,
    psa_lookup_protection_notification_no_form,
    withdrawnPSALookupJourney
  )

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(plaConnector)
    reset(psa_lookup_protection_notification_no_form)
    reset(withdrawnPSALookupJourney)
    reset(appConfig)

    when(psa_lookup_protection_notification_no_form.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(withdrawnPSALookupJourney.apply()(any(), any())).thenReturn(HtmlFormat.empty)
  }

  private val sessionId = SessionKeys.sessionId -> "lookup-test"
  private val request   = FakeRequest().withSession(sessionId)

  "LookupProtectionNotificationController on displayProtectionNotificationNoForm" when {

    "psalookupjourneyShutterEnabled toggle is disabled" when {

      "there is data in cache" should {
        "return 200 with correct message on pnn form" in {
          when(appConfig.psalookupjourneyShutterEnabled).thenReturn(false)
          cacheFetchCondition[PsaLookupRequest](Some(PsaLookupRequest("PSAREF")))

          val result = controller.displayProtectionNotificationNoForm(request)

          status(result) shouldBe OK
          verify(psa_lookup_protection_notification_no_form).apply(any[Form[String]]())(any(), any())
        }
      }

      "redirect when no data entered on first page for pnn form" in {
        when(appConfig.psalookupjourneyShutterEnabled).thenReturn(false)
        cacheFetchCondition[PsaLookupRequest](None)

        val result = controller.displayProtectionNotificationNoForm(request)

        status(result) shouldBe SEE_OTHER
        val expectedUrl =
          routes.LookupSchemeAdministratorReferenceController.displaySchemeAdministratorReferenceForm.url
        redirectLocation(result).get shouldBe expectedUrl
      }
    }

    "psalookupjourneyShutterEnabled toggle is enabled" should {
      "return 200 with withdrawnPSALookupJourney view" in {
        when(appConfig.psalookupjourneyShutterEnabled).thenReturn(true)

        val result = controller.displayProtectionNotificationNoForm(request)

        status(result) shouldBe OK
        verify(withdrawnPSALookupJourney).apply()(any(), any())
      }
    }
  }

  "LookupProtectionNotificationController on submitProtectionNotificationNoForm" when {

    val validPNNForm   = Seq("lifetimeAllowanceReference" -> "IP141000000000A")
    val invalidPNNForm = Seq("lifetimeAllowanceReference" -> "")

    val plaReturnJson = Json.parse(
      """{
        |  "pensionSchemeAdministratorCheckReference": "PSA12345678A",
        |  "ltaType": 5,
        |  "psaCheckResult": 1,
        |  "protectedAmount": 25000,
        |  "protectionNotificationNumber": "IP14000000000A"
        |}""".stripMargin
    )

    val cacheData: Map[String, JsValue] = Map(
      "pensionSchemeAdministratorCheckReference" -> JsString(""),
      "ltaType"                                  -> JsNumber(5),
      "psaCheckResult"                           -> JsNumber(1),
      "protectedAmount"                          -> JsNumber(25000),
      "protectionNotificationNumber"             -> JsString("IP14000000000A")
    )

    val mockCacheMap = CacheMap("psa-lookup-result", cacheData)

    val postRequest = FakeRequest().withSession(sessionId).withFormUrlEncodedBody(validPNNForm: _*).withMethod("POST")

    "psalookupjourneyShutterEnabled toggle is disabled" should {

      "submit pnn form with valid data and redirect to results page" in {
        when(appConfig.psalookupjourneyShutterEnabled).thenReturn(false)
        cacheFetchCondition[PsaLookupRequest](Some(PsaLookupRequest("PSA REF")))
        plaConnectorReturn(HttpResponse(status = OK, json = plaReturnJson, headers = Map.empty))
        cacheSaveCondition[PsaLookupResult](mockCacheMap)

        val result = controller.submitProtectionNotificationNoForm(postRequest)

        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe routes.LookupController.displayLookupResults.url
      }

      "submit pnn form with valid data and redirect when a NOT FOUND is returned" in {
        when(appConfig.psalookupjourneyShutterEnabled).thenReturn(false)
        cacheFetchCondition[PsaLookupRequest](Some(PsaLookupRequest("PSA REF")))
        when(plaConnector.psaLookup(any(), any())(any(), any()))
          .thenReturn(Future.failed(UpstreamErrorResponse("message", NOT_FOUND, NOT_FOUND)))
        cacheSaveCondition[PsaLookupResult](mockCacheMap)

        val result = controller.submitProtectionNotificationNoForm(postRequest)

        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe routes.LookupController.displayNotFoundResults.url
      }

      "display errors when invalid data entered for pnn form" in {
        when(appConfig.psalookupjourneyShutterEnabled).thenReturn(false)
        cacheSaveCondition[PsaLookupRequest](mockCacheMap)
        val postRequestInvalid = postRequest.withFormUrlEncodedBody(invalidPNNForm: _*)

        val result = controller.submitProtectionNotificationNoForm(postRequestInvalid)

        status(result) shouldBe BAD_REQUEST
      }

      "redirect to the administrator reference form when PSA request data not found on submission" in {
        when(appConfig.psalookupjourneyShutterEnabled).thenReturn(false)
        cacheFetchCondition[PsaLookupRequest](None)

        val result = controller.submitProtectionNotificationNoForm(postRequest)

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(
          routes.LookupSchemeAdministratorReferenceController.displaySchemeAdministratorReferenceForm.url
        )
      }
    }

    "psalookupjourneyShutterEnabled toggle is enabled" should {
      "return 200 with withdrawnPSALookupJourney view" in {
        when(appConfig.psalookupjourneyShutterEnabled).thenReturn(true)

        val result = controller.submitProtectionNotificationNoForm(postRequest)

        status(result) shouldBe OK
        verify(withdrawnPSALookupJourney).apply()(any(), any())
      }
    }

  }

  def cacheFetchCondition[T](data: Option[T]): Unit =
    when(sessionCacheService.fetchAndGetFormData[T](any())(any(), any()))
      .thenReturn(Future.successful(data))

  def cacheSaveCondition[T](data: CacheMap): Unit =
    when(sessionCacheService.saveFormData[T](any(), any())(any(), any()))
      .thenReturn(Future.successful(data))

  def plaConnectorReturn(data: HttpResponse): Unit =
    when(plaConnector.psaLookup(any(), any())(any(), any()))
      .thenReturn(Future.successful(data))

}
