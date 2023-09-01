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


import akka.actor.ActorSystem
import akka.stream.Materializer
import auth.AuthFunction
import config.wiring.PlaFormPartialRetriever
import config.{FrontendAppConfig, PlaContext}
import connectors.PLAConnector
import constructors.SummaryConstructor
import enums.ApplicationType
import mocks.AuthMock
import models.SummaryModel
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.libs.json.JsValue
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, Configuration, Environment}
import services.SessionCacheService
import testHelpers._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import models.cache.CacheMap
import java.util.UUID

import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import views.html.pages.fallback.technicalError
import views.html.pages.ip2016.summary

import scala.concurrent.{ExecutionContext, Future}

class SummaryControllerSpec extends FakeApplication with MockitoSugar with AuthMock {

  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
  val mockPlaConnector: PLAConnector = mock[PLAConnector]
  val mockMCC: MessagesControllerComponents = fakeApplication().injector.instanceOf[MessagesControllerComponents]

  implicit val executionContext: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  implicit val mockPartialRetriever: PlaFormPartialRetriever = mock[PlaFormPartialRetriever]
  implicit val mockAppConfig: FrontendAppConfig = fakeApplication().injector.instanceOf[FrontendAppConfig]
  implicit val mockPlaContext: PlaContext = mock[PlaContext]
  implicit val mockMessages: Messages = mock[Messages]
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = mock[Materializer]
  implicit val application = mock[Application]
  implicit val mockTechnicalError: technicalError = app.injector.instanceOf[technicalError]
  implicit val mockSummary: summary = app.injector.instanceOf[summary]
  implicit val formWithCSRF: FormWithCSRF = app.injector.instanceOf[FormWithCSRF]


  val mockSummaryConstructor: SummaryConstructor = mock[SummaryConstructor]
  val fakeRequest = FakeRequest()
  val mockAuthFunction: AuthFunction = fakeApplication().injector.instanceOf[AuthFunction]
  val mockEnv: Environment = mock[Environment]

  val tstSummaryModel = SummaryModel(ApplicationType.FP2016, false, List.empty, List.empty)

  val testSummaryController = new SummaryController(mockSessionCacheService, mockMCC, mockAuthFunction, mockTechnicalError, mockSummary) {
    override val summaryConstructor = mockSummaryConstructor
  }

  class Setup {

    val authFunction = new AuthFunction {
      override implicit val partialRetriever: PlaFormPartialRetriever = mockPartialRetriever
      override implicit val plaContext: PlaContext = mockPlaContext
      override implicit val appConfig: FrontendAppConfig = mockAppConfig
      override implicit val technicalError: technicalError = mockTechnicalError
      implicit val formWithCSRF: FormWithCSRF = app.injector.instanceOf[FormWithCSRF]
      override implicit val ec: ExecutionContext = executionContext

      override def authConnector: AuthConnector = mockAuthConnector
      override def config: Configuration = mockAppConfig.configuration
      override def env: Environment = mockEnv
    }

    val controller = new SummaryController(
      mockSessionCacheService,
      mockMCC,
      authFunction,
      mockTechnicalError,
      mockSummary
    ) {
      override val summaryConstructor = mockSummaryConstructor
    }
  }

  val sessionId = UUID.randomUUID.toString
  val mockUsername = "mockuser"
  val mockUserId = "/auth/oid/" + mockUsername

  "Navigating to summary when there is no user data" when {

    "user is applying for IP16" in new Setup {

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        when(mockSessionCacheService.fetchAllUserData(Matchers.any())).thenReturn(Future(None))
        val result = controller.summaryIP16(fakeRequest)

        status(result) shouldBe 500
    }
  }

  "Navigating to summary when there is invalid user data" when {

    "user is applying for IP16" in new Setup  {
      lazy val result = controller.summaryIP16(fakeRequest)

      when(controller.summaryConstructor.createSummaryData(Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(None)
      when(mockSessionCacheService.fetchAllUserData(Matchers.any())).thenReturn(Future(Some(CacheMap("tstID", Map.empty[String, JsValue]))))

      status(result) shouldBe 500
    }
  }

  "Navigating to summary when user has valid data" when {
    "user is applying for IP16" in new Setup  {
        when(controller.summaryConstructor.createSummaryData(Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Some(tstSummaryModel))
        when(mockSessionCacheService.fetchAllUserData(Matchers.any())).thenReturn(Future(Some(CacheMap("tstID", Map.empty[String, JsValue]))))

        val result = controller.summaryIP16(fakeRequest)
        status(result) shouldBe 200
    }
  }

  "Checking for data metrics flags" should {
    "return true for 'pensionsTakenBetween'" in new Setup  {
      SummaryController.recordDataMetrics("pensionsTakenBetween") shouldBe true
    }

    "return false for 'pensionsTakenBetweenAmt'" in new Setup  {
      SummaryController.recordDataMetrics("pensionsTakenBetweenAmt") shouldBe false
    }
  }
}
