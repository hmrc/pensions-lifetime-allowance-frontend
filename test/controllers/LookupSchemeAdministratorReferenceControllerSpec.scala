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
import models.PSALookupRequest
import models.cache.CacheMap
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.libs.json.{JsNumber, JsString, JsValue}
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.SessionCacheService
import testHelpers.FakeApplication
import uk.gov.hmrc.http.SessionKeys
import utils.ActionWithSessionId
import views.html.pages.lookup._

import scala.concurrent.{ExecutionContext, Future}

class LookupSchemeAdministratorReferenceControllerSpec
    extends FakeApplication
    with BeforeAndAfterEach
    with MockitoSugar {

  private val sessionCacheService: SessionCacheService = mock[SessionCacheService]
  private val messagesControllerComponents             = inject[MessagesControllerComponents]
  private val actionWithSessionId                      = inject[ActionWithSessionId]

  private val psa_lookup_scheme_admin_ref_form = mock[psa_lookup_scheme_admin_ref_form]
  private val withdrawnPSALookupJourney        = mock[withdrawnPSALookupJourney]

  private implicit val appConfig: FrontendAppConfig = mock[FrontendAppConfig]
  private implicit val ec: ExecutionContext         = inject[ExecutionContext]

  private val controller = new LookupSchemeAdministratorReferenceController(
    sessionCacheService,
    actionWithSessionId,
    messagesControllerComponents,
    psa_lookup_scheme_admin_ref_form,
    withdrawnPSALookupJourney
  )

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(appConfig)
    reset(psa_lookup_scheme_admin_ref_form)
    reset(withdrawnPSALookupJourney)

    when(psa_lookup_scheme_admin_ref_form.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(withdrawnPSALookupJourney.apply()(any(), any())).thenReturn(HtmlFormat.empty)
  }

  private val sessionId = SessionKeys.sessionId -> "lookup-test"
  private val request   = FakeRequest().withSession(sessionId)

  "LookupSchemeAdministratorReferenceController on displaySchemeAdministratorReferenceForm" when {

    "psalookupjourneyShutterEnabled toggle is disabled" should {
      "return 200 with correct message on psaRef form" in {
        when(appConfig.psalookupjourneyShutterEnabled).thenReturn(false)
        cacheFetchCondition[PSALookupRequest](None)

        val result = controller.displaySchemeAdministratorReferenceForm(request)

        status(result) shouldBe OK
        verify(psa_lookup_scheme_admin_ref_form).apply(any[Form[String]]())(any(), any())
      }
    }

    "psalookupjourneyShutterEnabled toggle is enabled" should {
      "return 200 with withdrawnPSALookupJourney view" in {
        when(appConfig.psalookupjourneyShutterEnabled).thenReturn(true)

        val result = controller.displaySchemeAdministratorReferenceForm(request)

        status(result) shouldBe OK
        verify(withdrawnPSALookupJourney).apply()(any(), any())
      }
    }
  }

  "LookupSchemeAdministratorReferenceController on submitSchemeAdministratorReferenceForm" when {

    val validPSARefForm   = Seq("pensionSchemeAdministratorCheckReference" -> "PSA12345678A")
    val invalidPSARefForm = Seq("pensionSchemeAdministratorCheckReference" -> "")

    val cacheData: Map[String, JsValue] = Map(
      "pensionSchemeAdministratorCheckReference" -> JsString(""),
      "ltaType"                                  -> JsNumber(5),
      "psaCheckResult"                           -> JsNumber(1),
      "protectedAmount"                          -> JsNumber(25000),
      "protectionNotificationNumber"             -> JsString("IP14000000000A")
    )

    val mockCacheMap = CacheMap("psa-lookup-result", cacheData)

    "psalookupjourneyShutterEnabled toggle is disabled" when {

      "submit psaRef form with valid data and redirect to pnn form" in {
        when(appConfig.psalookupjourneyShutterEnabled).thenReturn(false)
        cacheSaveCondition[PSALookupRequest](mockCacheMap)

        val request =
          FakeRequest().withSession(sessionId).withFormUrlEncodedBody(validPSARefForm: _*).withMethod("POST")

        val result = controller.submitSchemeAdministratorReferenceForm(request)

        status(result) shouldBe SEE_OTHER
        val expectedUrl = routes.LookupProtectionNotificationController.displayProtectionNotificationNoForm.url
        redirectLocation(result).get shouldBe expectedUrl
      }

      "display errors when invalid data entered for psaRef form" in {
        when(appConfig.psalookupjourneyShutterEnabled).thenReturn(false)

        val request =
          FakeRequest().withSession(sessionId).withFormUrlEncodedBody(invalidPSARefForm: _*).withMethod("POST")

        val result = controller.submitSchemeAdministratorReferenceForm(request)

        status(result) shouldBe BAD_REQUEST
        verify(psa_lookup_scheme_admin_ref_form).apply(any[Form[String]]())(any(), any())
      }
    }

    "psalookupjourneyShutterEnabled toggle is enabled" should {
      "return 200 with withdrawnPSALookupJourney view" in {
        when(appConfig.psalookupjourneyShutterEnabled).thenReturn(true)

        val result = controller.submitSchemeAdministratorReferenceForm(request)

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

}
