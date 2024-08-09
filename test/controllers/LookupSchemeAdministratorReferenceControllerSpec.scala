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
import models.{PSALookupRequest}
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
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, SessionKeys}
import uk.gov.hmrc.http.client.HttpClientV2
import utils.ActionWithSessionId
import views.html.pages.lookup._

import scala.concurrent.Future

class LookupSchemeAdministratorReferenceControllerSpec extends FakeApplication with BeforeAndAfterEach with MockitoSugar {

  private val sessionId = SessionKeys.sessionId -> "lookup-test"
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
  val mockPlaConnector: PLAConnector = mock[PLAConnector]
  val mockMCC: MessagesControllerComponents = fakeApplication().injector.instanceOf[MessagesControllerComponents]
  val mockActionWithSessionId: ActionWithSessionId = fakeApplication().injector.instanceOf[ActionWithSessionId]

  val mockHttp: HttpClientV2 = mock[HttpClientV2]

  implicit val mockAppConfig: FrontendAppConfig = fakeApplication().injector.instanceOf[FrontendAppConfig]
  implicit val mockPlaContext: PlaContext = mock[PlaContext]
  implicit val mockMessages: Messages = mock[Messages]
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = mock[Materializer]
  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val application = mock[Application]
  implicit val mockPsa_lookup_not_found_results: psa_lookup_not_found_results = app.injector.instanceOf[psa_lookup_not_found_results]
  implicit val mockPla_protection_guidance: pla_protection_guidance = app.injector.instanceOf[pla_protection_guidance]
  implicit val mockPsa_lookup_protection_notification_no_form: psa_lookup_protection_notification_no_form = app.injector.instanceOf[psa_lookup_protection_notification_no_form]
  implicit val mockPsa_lookup_results: psa_lookup_results = app.injector.instanceOf[psa_lookup_results]
  implicit val mockPsa_lookup_scheme_admin_ref_form: psa_lookup_scheme_admin_ref_form = app.injector.instanceOf[psa_lookup_scheme_admin_ref_form]
  implicit val formWithCSRF: FormWithCSRF = app.injector.instanceOf[FormWithCSRF]

  class Setup {
    val controller = new LookupSchemeAdministratorReferenceController(
      mockSessionCacheService,
      mockPlaConnector,
      mockActionWithSessionId,
      mockMCC,
      mockPsa_lookup_scheme_admin_ref_form,
    )
  }

  override def beforeEach(): Unit = {
    reset(mockPlaConnector)
  }

  private val validPSARefForm = Seq(
    "pensionSchemeAdministratorCheckReference" -> "PSA12345678A"
  )

  private val invalidPSARefForm = Seq(
    "pensionSchemeAdministratorCheckReference" -> ""
  )

  private val cacheData: Map[String, JsValue] = Map(
    "pensionSchemeAdministratorCheckReference" -> JsString(""),
    "ltaType" -> JsNumber(5),
    "psaCheckResult" -> JsNumber(1),
    "protectedAmount" -> JsNumber(25000),
    "protectionNotificationNumber" -> JsString("IP14000000000A")
  )

  private val mockCacheMap = CacheMap("psa-lookup-result", cacheData)


  "LookupSchemeAdministratorReferenceController" should {
    "return 200 with correct message on psaRef form" in new Setup  {
      cacheFetchCondition[PSALookupRequest](None)

      val request = FakeRequest().withSession(sessionId)
      val result = controller.displaySchemeAdministratorReferenceForm.apply(request)

      status(result) shouldBe OK
    }

    "submit psaRef form with valid data and redirect to pnn form" in new Setup  {
      val request = FakeRequest().withSession(sessionId).withFormUrlEncodedBody(validPSARefForm: _*).withMethod("POST")

      cacheSaveCondition[PSALookupRequest](mockCacheMap)

      val result = controller.submitSchemeAdministratorReferenceForm.apply(request)

      status(result) shouldBe  SEE_OTHER
      redirectLocation(result).get shouldBe  routes.LookupProtectionNotificationController.displayProtectionNotificationNoForm.url
    }

    "display errors when invalid data entered for psaRef form" in new Setup  {
      val request = FakeRequest().withSession(sessionId).withFormUrlEncodedBody(invalidPSARefForm: _*).withMethod("POST")

      cacheSaveCondition[PSALookupRequest](mockCacheMap)

      val result = controller.submitSchemeAdministratorReferenceForm.apply(request)

      status(result) shouldBe  BAD_REQUEST
    }

  }

  def cacheFetchCondition[T](data: Option[T]): Unit = when(mockSessionCacheService.fetchAndGetFormData[T](any())(any(), any()))
    .thenReturn(Future.successful(data))

  def cacheSaveCondition[T](data: CacheMap): Unit = when(mockSessionCacheService.saveFormData[T](any(), any())(any(), any()))
    .thenReturn(Future.successful(data))

  def plaConnectorReturn(data: HttpResponse): Unit = when(mockPlaConnector.psaLookup(any(), any())(any(), any()))
    .thenReturn(Future.successful(data))
}
