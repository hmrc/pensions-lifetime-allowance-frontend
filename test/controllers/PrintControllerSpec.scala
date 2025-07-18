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

import auth.AuthFunction
import config.{FrontendAppConfig, PlaContext}
import connectors.CitizenDetailsConnector
import constructors.DisplayConstructors
import mocks.AuthMock
import models._
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.Environment
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.SessionCacheService
import testHelpers.FakeApplication
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import utils.Constants
import views.html.pages.fallback.technicalError
import views.html.pages.result.{resultPrint, resultPrintViewAmendment}

import scala.concurrent.{ExecutionContext, Future}

class PrintControllerSpec extends FakeApplication with MockitoSugar with AuthMock with BeforeAndAfterEach {

  val mockDisplayConstructors: DisplayConstructors = mock[DisplayConstructors]
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
  val mockMCC: MessagesControllerComponents        = fakeApplication().injector.instanceOf[MessagesControllerComponents]
  val mockCitizenDetailsConnector: CitizenDetailsConnector = mock[CitizenDetailsConnector]
  val fakeRequest: FakeRequest[AnyContent]                 = FakeRequest()
  val mockEnv: Environment                                 = mock[Environment]
  val resultPrintView: resultPrint                         = mock[resultPrint]

  val resultPrintViewAmendment: resultPrintViewAmendment =
    mock[resultPrintViewAmendment]

  implicit val executionContext: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  implicit val mockAppConfig: FrontendAppConfig   = mock[FrontendAppConfig]
  implicit val mockPlaContext: PlaContext         = mock[PlaContext]
  implicit val system: ActorSystem                = ActorSystem()
  implicit val materializer: Materializer         = mock[Materializer]
  implicit val mockTechnicalError: technicalError = app.injector.instanceOf[technicalError]

  val testPersonalDetails = PersonalDetailsModel(Person("McTestFace", "Testy"))

  val testPrintDisplayModel = PrintDisplayModel(
    "Testy",
    "Mctestface",
    "AA11TESTA",
    "IP2016",
    "open",
    "PSATestNum",
    "ProtRefTestNum",
    Some("£1,246,500"),
    Some("3 April 2016"),
    8
  )

  val authFunction = new AuthFunction {
    override implicit val plaContext: PlaContext         = mockPlaContext
    override implicit val appConfig: FrontendAppConfig   = mockAppConfig
    override implicit val technicalError: technicalError = mockTechnicalError
    override implicit val ec: ExecutionContext           = executionContext

    override def authConnector: AuthConnector = mockAuthConnector
  }

  val TestPrintController = new PrintController(
    mockSessionCacheService,
    mockCitizenDetailsConnector,
    mockDisplayConstructors,
    resultPrintView,
    resultPrintViewAmendment,
    mockMCC,
    authFunction
  ) {}

  override def beforeEach(): Unit = {
    reset(mockDisplayConstructors)
    reset(mockSessionCacheService)
    reset(mockCitizenDetailsConnector)
    reset(resultPrintView)
    reset(resultPrintViewAmendment)
    reset(mockPlaContext)
    reset(mockAppConfig)
    super.beforeEach()
  }

  "Navigating to print protection" should {

    "return 200 when HIP migration is disabled for amendmentCodesList" when
      Constants.amendmentCodesList.foreach { notificationId =>
        s"Valid data is provided with notificationId: $notificationId " in {
          val testProtectionModel = ProtectionModel(
            psaCheckReference = Some("tstPSACeckRef"),
            protectionID = Some(1111111),
            notificationId = Some(notificationId)
          )
          mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

          when(mockCitizenDetailsConnector.getPersonDetails(any())(any()))
            .thenReturn(Future.successful(Some(testPersonalDetails)))
          when(mockSessionCacheService.fetchAndGetFormData[ProtectionModel](any())(any(), any()))
            .thenReturn(Future.successful(Some(testProtectionModel)))
          when(mockDisplayConstructors.createPrintDisplayModel(any(), any(), any())(any()))
            .thenReturn(testPrintDisplayModel)
          when(resultPrintViewAmendment.apply(any())(any(), any()))
            .thenReturn(HtmlFormat.empty)
          when(resultPrintView.apply(any())(any(), any()))
            .thenReturn(HtmlFormat.empty)
          when(mockAppConfig.hipMigrationEnabled).thenReturn(false)

          val result = TestPrintController.printView(fakeRequest)
          status(result) shouldBe 200
          verify(resultPrintView).apply(ArgumentMatchers.eq(testPrintDisplayModel))(any(), any())
        }
      }

    "return 200 when HIP migration is enabled" when
      Constants.amendmentCodesList.foreach { notificationId =>
        s"Valid data is provided with notificationId: $notificationId " in {
          val testProtectionModel = ProtectionModel(
            psaCheckReference = Some("tstPSACeckRef"),
            protectionID = Some(1111111),
            notificationId = Some(notificationId)
          )
          mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

          when(mockCitizenDetailsConnector.getPersonDetails(any())(any()))
            .thenReturn(Future.successful(Some(testPersonalDetails)))
          when(mockSessionCacheService.fetchAndGetFormData[ProtectionModel](any())(any(), any()))
            .thenReturn(Future.successful(Some(testProtectionModel)))
          when(mockDisplayConstructors.createPrintDisplayModel(any(), any(), any())(any()))
            .thenReturn(testPrintDisplayModel)
          when(resultPrintViewAmendment.apply(any())(any(), any()))
            .thenReturn(HtmlFormat.empty)
          when(resultPrintView.apply(any())(any(), any()))
            .thenReturn(HtmlFormat.empty)
          when(mockAppConfig.hipMigrationEnabled).thenReturn(true)

          val result = TestPrintController.printView(fakeRequest)
          status(result) shouldBe 200
          verify(resultPrintViewAmendment).apply(ArgumentMatchers.eq(testPrintDisplayModel))(any(), any())
        }
      }

    "return 200 when HIP migration is enabled" when
      List(16, 17).foreach { notificationId =>
        s"Valid data is provided with notificationId: $notificationId " in {
          val testProtectionModel = ProtectionModel(
            psaCheckReference = Some("tstPSACeckRef"),
            protectionID = Some(1111111),
            notificationId = Some(notificationId)
          )
          mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

          when(mockCitizenDetailsConnector.getPersonDetails(any())(any()))
            .thenReturn(Future.successful(Some(testPersonalDetails)))
          when(mockSessionCacheService.fetchAndGetFormData[ProtectionModel](any())(any(), any()))
            .thenReturn(Future.successful(Some(testProtectionModel)))
          when(mockDisplayConstructors.createPrintDisplayModel(any(), any(), any())(any()))
            .thenReturn(testPrintDisplayModel)
          when(resultPrintViewAmendment.apply(any())(any(), any()))
            .thenReturn(HtmlFormat.empty)
          when(resultPrintView.apply(any())(any(), any()))
            .thenReturn(HtmlFormat.empty)
          when(mockAppConfig.hipMigrationEnabled).thenReturn(true)

          val result = TestPrintController.printView(fakeRequest)
          status(result) shouldBe 200
          verify(resultPrintView).apply(ArgumentMatchers.eq(testPrintDisplayModel))(any(), any())
        }
      }

    "return a 303 redirect" when {
      "InValid data is provided" in {
        when(mockCitizenDetailsConnector.getPersonDetails(any())(any()))
          .thenReturn(Future(Some(testPersonalDetails)))
        when(mockSessionCacheService.fetchAndGetFormData[ProtectionModel](any())(any(), any()))
          .thenReturn(Future(None))
        when(mockDisplayConstructors.createPrintDisplayModel(any(), any(), any())(any()))
          .thenReturn(testPrintDisplayModel)

        val result = TestPrintController.printView(fakeRequest)
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some(routes.ReadProtectionsController.currentProtections.url)
      }
    }
  }

}
