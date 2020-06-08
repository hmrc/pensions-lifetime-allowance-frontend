/*
 * Copyright 2020 HM Revenue & Customs
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


import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import auth.AuthFunction
import config.wiring.PlaFormPartialRetriever
import config.{FrontendAppConfig, LocalTemplateRenderer, PlaContext}
import connectors.{KeyStoreConnector, PLAConnector}
import constructors.SummaryConstructor
import enums.ApplicationType
import mocks.AuthMock
import models.SummaryModel
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.libs.json.JsValue
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.{Configuration, Environment}
import testHelpers._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SummaryControllerSpec extends UnitSpec with MockitoSugar with AuthMock with WithFakeApplication {

  val mockKeyStoreConnector: KeyStoreConnector = mock[KeyStoreConnector]
  val mockPlaConnector: PLAConnector = mock[PLAConnector]
  val mockMCC: MessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]

  implicit val mockTemplateRenderer: LocalTemplateRenderer = MockTemplateRenderer.renderer
  implicit val mockPartialRetriever: PlaFormPartialRetriever = mock[PlaFormPartialRetriever]
  implicit val mockAppConfig: FrontendAppConfig = fakeApplication.injector.instanceOf[FrontendAppConfig]
  implicit val mockPlaContext: PlaContext = mock[PlaContext]
  implicit val mockMessages: Messages = mock[Messages]
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val mockSummaryConstructor: SummaryConstructor = mock[SummaryConstructor]
  val fakeRequest = FakeRequest()
  val mockAuthFunction: AuthFunction = fakeApplication.injector.instanceOf[AuthFunction]
  val mockEnv: Environment = mock[Environment]

  val tstSummaryModel = SummaryModel(ApplicationType.FP2016, false, List.empty, List.empty)

  val testSummaryController = new SummaryController(mockKeyStoreConnector, mockMCC, mockAuthFunction) {
    override val summaryConstructor = mockSummaryConstructor
  }

  class Setup {

    val authFunction = new AuthFunction {
      override implicit val partialRetriever: PlaFormPartialRetriever = mockPartialRetriever
      override implicit val templateRenderer: LocalTemplateRenderer = mockTemplateRenderer
      override implicit val plaContext: PlaContext = mockPlaContext
      override implicit val appConfig: FrontendAppConfig = mockAppConfig
      override def authConnector: AuthConnector = mockAuthConnector
      override def config: Configuration = mockAppConfig.configuration
      override def env: Environment = mockEnv
    }

    val controller = new SummaryController(
      mockKeyStoreConnector,
      mockMCC,
      authFunction
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
        when(mockKeyStoreConnector.fetchAllUserData(ArgumentMatchers.any())).thenReturn(Future(None))
        val result = await(controller.summaryIP16(fakeRequest))

        status(result) shouldBe 500
    }
  }

  "Navigating to summary when there is invalid user data" when {

    "user is applying for IP16" in new Setup  {
      lazy val result = await(controller.summaryIP16(fakeRequest))

      when(controller.summaryConstructor.createSummaryData(ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(None)
      when(mockKeyStoreConnector.fetchAllUserData(ArgumentMatchers.any())).thenReturn(Future(Some(CacheMap("tstID", Map.empty[String, JsValue]))))

      status(result) shouldBe 500
    }
  }

  "Navigating to summary when user has valid data" when {
    "user is applying for IP16" in new Setup  {
        when(controller.summaryConstructor.createSummaryData(ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Some(tstSummaryModel))
        when(mockKeyStoreConnector.fetchAllUserData(ArgumentMatchers.any())).thenReturn(Future(Some(CacheMap("tstID", Map.empty[String, JsValue]))))

        val result = await(controller.summaryIP16(fakeRequest))
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
