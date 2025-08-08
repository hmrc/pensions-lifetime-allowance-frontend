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
import models.{PSALookupRequest, PSALookupResult}
import org.mockito.ArgumentMatchers.{any, anyString, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.libs.json.Json
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.SessionCacheService
import testHelpers.FakeApplication
import uk.gov.hmrc.http.SessionKeys
import utils.ActionWithSessionId
import views.html.pages.lookup._

import scala.concurrent.Future

class LookupControllerSpec extends FakeApplication with BeforeAndAfterEach with MockitoSugar {

  private val sessionId                                = SessionKeys.sessionId -> "lookup-test"
  private val sessionCacheService: SessionCacheService = mock[SessionCacheService]

  private val mockMCC: MessagesControllerComponents =
    fakeApplication().injector.instanceOf[MessagesControllerComponents]

  private val mockActionWithSessionId: ActionWithSessionId = fakeApplication().injector.instanceOf[ActionWithSessionId]

  private val psa_lookup_not_found_results: psa_lookup_not_found_results = mock[psa_lookup_not_found_results]
  private val pla_protection_guidance: pla_protection_guidance           = mock[pla_protection_guidance]
  private val psa_lookup_results: psa_lookup_results                     = mock[psa_lookup_results]
  private val withdrawnPSALookupJourney: withdrawnPSALookupJourney       = mock[withdrawnPSALookupJourney]

  private implicit val appConfig: FrontendAppConfig = mock[FrontendAppConfig]
  private implicit val application: Application     = mock[Application]

  private val controller = new LookupController(
    sessionCacheService,
    mockActionWithSessionId,
    mockMCC,
    psa_lookup_not_found_results,
    pla_protection_guidance,
    psa_lookup_results,
    withdrawnPSALookupJourney
  )

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(appConfig)
    reset(application)
    reset(psa_lookup_not_found_results)
    reset(pla_protection_guidance)
    reset(psa_lookup_results)
    reset(withdrawnPSALookupJourney)

    when(psa_lookup_not_found_results.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(pla_protection_guidance.apply()(any(), any())).thenReturn(HtmlFormat.empty)
    when(psa_lookup_results.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(withdrawnPSALookupJourney.apply()(any(), any())).thenReturn(HtmlFormat.empty)
  }

  private val request = FakeRequest().withSession(sessionId)

  private val plaReturnJson = Json.parse(
    """{
      |  "pensionSchemeAdministratorCheckReference": "PSA12345678A",
      |  "ltaType": 5,
      |  "psaCheckResult": 1,
      |  "protectedAmount": 25000,
      |  "protectionNotificationNumber": "IP14000000000A"
      |}""".stripMargin
  )

  private val psaRequestJson = Json.parse(
    """{
      | "pensionSchemeAdministratorCheckReference": "PSA12345678A",
      | "lifetimeAllowanceReference": "IP14000000000A"
      | }""".stripMargin
  )

  "LookupController on displayNotFoundResults" when {

    "psalookupjourneyShutterEnabled toggle is disabled" when {

      "there is data in session cache" should {
        "return 200 with psa_lookup_not_found_results view" in {
          when(appConfig.psalookupjourneyShutterEnabled).thenReturn(false)
          cacheFetchCondition[PSALookupRequest](Some(Json.fromJson[PSALookupRequest](psaRequestJson).get))

          val result = controller.displayNotFoundResults.apply(request)

          status(result) shouldBe OK
          val expectedLookupRequest = Json.fromJson[PSALookupRequest](psaRequestJson).get
          verify(psa_lookup_not_found_results).apply(eqTo(expectedLookupRequest), anyString())(any(), any())
        }
      }

      "there is NO data in session cache" should {
        "return 303 Redirect" in {
          when(appConfig.psalookupjourneyShutterEnabled).thenReturn(false)
          cacheFetchCondition[PSALookupRequest](None)

          val result = controller.displayNotFoundResults.apply(request)

          status(result) shouldBe SEE_OTHER
          redirectLocation(
            result
          ).get shouldBe routes.LookupSchemeAdministratorReferenceController.displaySchemeAdministratorReferenceForm.url
        }
      }
    }

    "psalookupjourneyShutterEnabled toggle is enabled" should {
      "return 200 with withdrawnPSALookupJourney view" in {
        when(appConfig.psalookupjourneyShutterEnabled).thenReturn(true)

        val result = controller.displayNotFoundResults.apply(request)

        status(result) shouldBe OK
        verify(withdrawnPSALookupJourney).apply()(any(), any())
      }
    }
  }

  "LookupController on displayLookupResults" when {

    "psalookupjourneyShutterEnabled toggle is disabled" when {

      "there is data in session cache" should {
        "return 200 with psa_lookup_results" in {
          when(appConfig.psalookupjourneyShutterEnabled).thenReturn(false)
          cacheFetchCondition[PSALookupResult](Some(Json.fromJson[PSALookupResult](plaReturnJson).get))

          val result = controller.displayLookupResults.apply(request)

          status(result) shouldBe OK
          val expectedLookupResult = Json.fromJson[PSALookupResult](plaReturnJson).get
          verify(psa_lookup_results).apply(eqTo(expectedLookupResult), anyString())(any(), any())
        }
      }

      "there is NO data in session cache" should {
        "return 303 Redirect" in {
          when(appConfig.psalookupjourneyShutterEnabled).thenReturn(false)
          cacheFetchCondition[PSALookupResult](None)

          val result = controller.displayLookupResults.apply(request)

          status(result) shouldBe SEE_OTHER
          redirectLocation(
            result
          ).get shouldBe routes.LookupSchemeAdministratorReferenceController.displaySchemeAdministratorReferenceForm.url
        }
      }
    }

    "psalookupjourneyShutterEnabled toggle is enabled" should {
      "return 200 with withdrawnPSALookupJourney view" in {
        when(appConfig.psalookupjourneyShutterEnabled).thenReturn(true)

        val result = controller.displayLookupResults.apply(request)

        status(result) shouldBe OK
        verify(withdrawnPSALookupJourney).apply()(any(), any())
      }
    }
  }

  "LookupController on displayProtectionTypeGuidance" when {

    "psalookupjourneyShutterEnabled toggle is disabled" should {
      "return 200 with pla_protection_guidance" in {
        when(appConfig.psalookupjourneyShutterEnabled).thenReturn(false)

        val result = controller.displayProtectionTypeGuidance.apply(request)

        status(result) shouldBe OK
        verify(pla_protection_guidance).apply()(any(), any())
      }
    }

    "psalookupjourneyShutterEnabled toggle is enabled" should {
      "return 200 with withdrawnPSALookupJourney view" in {
        when(appConfig.psalookupjourneyShutterEnabled).thenReturn(true)

        val result = controller.displayProtectionTypeGuidance.apply(request)

        status(result) shouldBe OK
        verify(withdrawnPSALookupJourney).apply()(any(), any())
      }
    }
  }

  "LookupController on redirectToStart" when {

    "psalookupjourneyShutterEnabled toggle is disabled" should {
      "return 303 Redirect" in {
        when(appConfig.psalookupjourneyShutterEnabled).thenReturn(false)
        when(sessionCacheService.remove(any())).thenReturn(Future.unit)

        val result = controller.redirectToStart.apply(request)

        status(result) shouldBe SEE_OTHER
        redirectLocation(
          result
        ).get shouldBe routes.LookupSchemeAdministratorReferenceController.displaySchemeAdministratorReferenceForm.url
      }
    }

    "psalookupjourneyShutterEnabled toggle is enabled" should {
      "return 200 with withdrawnPSALookupJourney view" in {
        when(appConfig.psalookupjourneyShutterEnabled).thenReturn(true)

        val result = controller.redirectToStart.apply(request)

        status(result) shouldBe OK
        verify(withdrawnPSALookupJourney).apply()(any(), any())
      }
    }
  }

  def cacheFetchCondition[T](data: Option[T]): Unit =
    when(sessionCacheService.fetchAndGetFormData[T](any())(any(), any()))
      .thenReturn(Future.successful(data))

}
