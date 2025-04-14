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
import play.api.libs.json.Json
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionCacheService
import testHelpers.FakeApplication
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, SessionKeys}
import utils.ActionWithSessionId
import views.html.pages.lookup._

import scala.concurrent.Future

class LookupControllerSpec extends FakeApplication with BeforeAndAfterEach with MockitoSugar {

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
  implicit val application: Application = mock[Application]
  implicit val mockPsa_lookup_not_found_results: psa_lookup_not_found_results = app.injector.instanceOf[psa_lookup_not_found_results]
  implicit val mockPla_protection_guidance: pla_protection_guidance = app.injector.instanceOf[pla_protection_guidance]
  implicit val mockPsa_lookup_protection_notification_no_form: psa_lookup_protection_notification_no_form = app.injector.instanceOf[psa_lookup_protection_notification_no_form]
  implicit val mockPsa_lookup_results: psa_lookup_results = app.injector.instanceOf[psa_lookup_results]
  implicit val mockPsa_lookup_scheme_admin_ref_form: psa_lookup_scheme_admin_ref_form = app.injector.instanceOf[psa_lookup_scheme_admin_ref_form]
  implicit val formWithCSRF: FormWithCSRF = app.injector.instanceOf[FormWithCSRF]

  class Setup {
    val controller = new LookupController(
      mockSessionCacheService,
      mockPlaConnector,
      mockActionWithSessionId,
      mockMCC,
      mockPsa_lookup_not_found_results,
      mockPla_protection_guidance,
      mockPsa_lookup_results,
    )
  }

  override def beforeEach(): Unit = {
    reset(mockPlaConnector)
  }

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

  "LookupController" should {
    "return 200 with correct message on results page" in new Setup  {
      val request = FakeRequest().withSession(sessionId)
      cacheFetchCondition[PSALookupResult](Some(Json.fromJson[PSALookupResult](plaReturnJson).get))

      val result = controller.displayLookupResults.apply(request)

      status(result) shouldBe  OK
    }

    "redirect when no result data is stored on results page" in new Setup  {
      val request = FakeRequest().withSession(sessionId)
      cacheFetchCondition[PSALookupResult](None)

      val result = controller.displayLookupResults.apply(request)

      status(result) shouldBe  SEE_OTHER
      redirectLocation(result).get shouldBe  routes.LookupSchemeAdministratorReferenceController.displaySchemeAdministratorReferenceForm.url
    }

    "return 200 with correct message on not found results page" in new Setup  {
      val request = FakeRequest().withSession(sessionId)
      cacheFetchCondition[PSALookupRequest](Some(Json.fromJson[PSALookupRequest](psaRequestJson).get))

      val result = controller.displayNotFoundResults.apply(request)

      status(result) shouldBe  OK
    }

    "redirect when no result data is stored on not found results page" in new Setup  {
      val request = FakeRequest().withSession(sessionId)
      cacheFetchCondition[PSALookupRequest](None)

      val result = controller.displayLookupResults.apply(request)

      status(result) shouldBe  SEE_OTHER
      redirectLocation(result).get shouldBe  routes.LookupSchemeAdministratorReferenceController.displaySchemeAdministratorReferenceForm.url
    }

    "return 200 with correct message on protection type guidance page" in new Setup  {

      val request = FakeRequest().withSession(sessionId)
      val result = controller.displayProtectionTypeGuidance.apply(request)

      status(result) shouldBe  OK
    }

  }

  def cacheFetchCondition[T](data: Option[T]): Unit = when(mockSessionCacheService.fetchAndGetFormData[T](any())(any(), any()))
    .thenReturn(Future.successful(data))

  def cacheSaveCondition[T](data: CacheMap): Unit = when(mockSessionCacheService.saveFormData[T](any(), any())(any(), any()))
    .thenReturn(Future.successful(data))

  def plaConnectorReturn(data: HttpResponse): Unit = when(mockPlaConnector.psaLookup(any(), any())(any(), any()))
    .thenReturn(Future.successful(data))
}
