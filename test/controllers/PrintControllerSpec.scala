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
import config.{FrontendAppConfig, LocalTemplateRenderer, PlaContext}
import connectors.{CitizenDetailsConnector, KeyStoreConnector}
import constructors.DisplayConstructors
import mocks.AuthMock
import models._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import testHelpers.{FakeApplication, MockTemplateRenderer}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import views.html.pages.fallback.technicalError
import views.html.pages.result.resultPrint

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PrintControllerSpec extends FakeApplication with MockitoSugar with AuthMock with BeforeAndAfterEach {

  val mockDisplayConstructors: DisplayConstructors = mock[DisplayConstructors]
  val mockKeyStoreConnector: KeyStoreConnector = mock[KeyStoreConnector]
  val mockMCC: MessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val mockCitizenDetailsConnector: CitizenDetailsConnector = mock[CitizenDetailsConnector]
  val fakeRequest = FakeRequest()
  val mockEnv: Environment = mock[Environment]
  val resultPrintView: resultPrint = fakeApplication.injector.instanceOf[resultPrint]

  implicit val mockTemplateRenderer: LocalTemplateRenderer = MockTemplateRenderer.renderer
  implicit val mockPartialRetriever: PlaFormPartialRetriever = mock[PlaFormPartialRetriever]
  implicit val mockAppConfig: FrontendAppConfig = fakeApplication.injector.instanceOf[FrontendAppConfig]
  implicit val mockPlaContext: PlaContext = mock[PlaContext]
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = mock[Materializer]
  implicit val mockTechnicalError: technicalError = app.injector.instanceOf[technicalError]


  val testPersonalDetails = PersonalDetailsModel(Person("McTestFace", "Testy"))
  val testProtectionModel = ProtectionModel(psaCheckReference = Some("tstPSACeckRef"), protectionID = Some(1111111))
  val testPrintDisplayModel = PrintDisplayModel("Testy", "Mctestface", "AA11TESTA", "IP2016", "open", "PSATestNum", "ProtRefTestNum", Some("£1,246,500"), Some("3 April 2016"))

  val authFunction = new AuthFunction {
    override implicit val partialRetriever: PlaFormPartialRetriever = mockPartialRetriever
    override implicit val templateRenderer: LocalTemplateRenderer = mockTemplateRenderer
    override implicit val plaContext: PlaContext = mockPlaContext
    override implicit val appConfig: FrontendAppConfig = mockAppConfig
    override implicit val technicalError: technicalError = mockTechnicalError

    override def authConnector: AuthConnector = mockAuthConnector
    override def config: Configuration = mockAppConfig.configuration
    override def env: Environment = mockEnv
  }


  val TestPrintController = new PrintController(mockKeyStoreConnector, mockCitizenDetailsConnector, mockDisplayConstructors, resultPrintView, mockMCC, authFunction) {
  }

  override def beforeEach(): Unit = {
    reset(mockDisplayConstructors,
      mockKeyStoreConnector,
      mockCitizenDetailsConnector,
      mockPlaContext)
    super.beforeEach()
  }

  "Navigating to print protection" should {
    "return 200" when {
      "Valid data is provided" in {
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

        when(mockCitizenDetailsConnector.getPersonDetails(ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(testPersonalDetails)))
        when(mockKeyStoreConnector.fetchAndGetFormData[ProtectionModel](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(testProtectionModel)))
        when(mockDisplayConstructors.createPrintDisplayModel(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(testPrintDisplayModel)

        val result = TestPrintController.printView(fakeRequest)
        status(result) shouldBe 200
      }
    }

    "return a 303 redirect" when {
      "InValid data is provided" in {
        when(mockCitizenDetailsConnector.getPersonDetails(ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future(Some(testPersonalDetails)))
        when(mockKeyStoreConnector.fetchAndGetFormData[ProtectionModel](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future(None))
        when(mockDisplayConstructors.createPrintDisplayModel(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(testPrintDisplayModel)

        val result = TestPrintController.printView(fakeRequest)
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some(routes.ReadProtectionsController.currentProtections.url)
      }
    }
  }
}


