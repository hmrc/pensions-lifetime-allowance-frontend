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
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
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

  private val displayConstructors: DisplayConstructors           = mock[DisplayConstructors]
  private val sessionCacheService: SessionCacheService           = mock[SessionCacheService]
  private val citizenDetailsConnector: CitizenDetailsConnector   = mock[CitizenDetailsConnector]
  private val resultPrintView: resultPrint                       = mock[resultPrint]
  private val resultPrintViewAmendment: resultPrintViewAmendment = mock[resultPrintViewAmendment]

  private val messagesControllerComponents: MessagesControllerComponents =
    fakeApplication().injector.instanceOf[MessagesControllerComponents]

  private implicit val executionContext: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  private implicit val appConfig: FrontendAppConfig       = mock[FrontendAppConfig]

  private val authFunction = new AuthFunction {
    override implicit val plaContext: PlaContext         = mock[PlaContext]
    override implicit val appConfig: FrontendAppConfig   = appConfig
    override implicit val technicalError: technicalError = app.injector.instanceOf[technicalError]
    override implicit val ec: ExecutionContext           = executionContext

    override def authConnector: AuthConnector = mockAuthConnector
  }

  private val printController = new PrintController(
    sessionCacheService,
    citizenDetailsConnector,
    displayConstructors,
    resultPrintView,
    resultPrintViewAmendment,
    messagesControllerComponents,
    authFunction
  )

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(displayConstructors)
    reset(sessionCacheService)
    reset(citizenDetailsConnector)
    reset(resultPrintView)
    reset(resultPrintViewAmendment)
    reset(appConfig)

    when(resultPrintViewAmendment.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(resultPrintView.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  private val fakeRequest: FakeRequest[AnyContent] = FakeRequest()

  private val testNino: String = "AB123456A"

  private val testPersonalDetails = PersonalDetailsModel(Person("McTestFace", "Testy"))

  private val testPrintDisplayModel = PrintDisplayModel(
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

  "PrintController on printView" should {

    "return Ok with resultPrintViewAmendment view" when
      Constants.amendmentCodesList.foreach { notificationId =>
        s"ProtectionModel stored in cache contains notificationId: $notificationId and HIP migration is enabled" in {
          val testProtectionModel = ProtectionModel(
            psaCheckReference = Some("tstPSACeckRef"),
            protectionID = Some(1111111),
            notificationId = Some(notificationId)
          )
          mockAuthRetrieval[Option[String]](Retrievals.nino, Some(testNino))

          when(citizenDetailsConnector.getPersonDetails(any())(any()))
            .thenReturn(Future.successful(Some(testPersonalDetails)))
          when(sessionCacheService.fetchAndGetFormData[ProtectionModel](any())(any(), any()))
            .thenReturn(Future.successful(Some(testProtectionModel)))
          when(displayConstructors.createPrintDisplayModel(any(), any(), any())(any()))
            .thenReturn(testPrintDisplayModel)

          when(appConfig.hipMigrationEnabled).thenReturn(true)

          val result = printController.printView(fakeRequest)

          status(result) shouldBe 200
          verify(resultPrintViewAmendment).apply(ArgumentMatchers.eq(testPrintDisplayModel))(any(), any())
        }
      }

    "return Ok with resultPrintView view" when {

      "HIP migration is enabled" when
        Seq(15, 16).foreach { notificationId =>
          s"Valid data is provided with notificationId: $notificationId " in {
            val testProtectionModel = ProtectionModel(
              psaCheckReference = Some("tstPSACeckRef"),
              protectionID = Some(1111111),
              notificationId = Some(notificationId)
            )
            mockAuthRetrieval[Option[String]](Retrievals.nino, Some(testNino))

            when(citizenDetailsConnector.getPersonDetails(any())(any()))
              .thenReturn(Future.successful(Some(testPersonalDetails)))
            when(sessionCacheService.fetchAndGetFormData[ProtectionModel](any())(any(), any()))
              .thenReturn(Future.successful(Some(testProtectionModel)))
            when(displayConstructors.createPrintDisplayModel(any(), any(), any())(any()))
              .thenReturn(testPrintDisplayModel)

            when(appConfig.hipMigrationEnabled).thenReturn(true)

            val result = printController.printView(fakeRequest)

            status(result) shouldBe 200
            verify(resultPrintView).apply(ArgumentMatchers.eq(testPrintDisplayModel))(any(), any())
          }
        }

      "HIP migration is disabled" when
        Constants.amendmentCodesList.foreach { notificationId =>
          s"Valid data is provided with notificationId: $notificationId " in {
            val testProtectionModel = ProtectionModel(
              psaCheckReference = Some("tstPSACeckRef"),
              protectionID = Some(1111111),
              notificationId = Some(notificationId)
            )
            mockAuthRetrieval[Option[String]](Retrievals.nino, Some(testNino))

            when(citizenDetailsConnector.getPersonDetails(any())(any()))
              .thenReturn(Future.successful(Some(testPersonalDetails)))
            when(sessionCacheService.fetchAndGetFormData[ProtectionModel](any())(any(), any()))
              .thenReturn(Future.successful(Some(testProtectionModel)))
            when(displayConstructors.createPrintDisplayModel(any(), any(), any())(any()))
              .thenReturn(testPrintDisplayModel)

            when(appConfig.hipMigrationEnabled).thenReturn(false)

            val result = printController.printView(fakeRequest)
            status(result) shouldBe 200
            verify(resultPrintView).apply(ArgumentMatchers.eq(testPrintDisplayModel))(any(), any())
          }
        }

    }

    "return Redirect to ReadProtectionsController.currentProtections" when {
      "there is no data in cache" in {
        when(citizenDetailsConnector.getPersonDetails(any())(any()))
          .thenReturn(Future(Some(testPersonalDetails)))
        when(sessionCacheService.fetchAndGetFormData[ProtectionModel](any())(any(), any()))
          .thenReturn(Future(None))
        when(displayConstructors.createPrintDisplayModel(any(), any(), any())(any()))
          .thenReturn(testPrintDisplayModel)

        val result = printController.printView(fakeRequest)

        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some(routes.ReadProtectionsController.currentProtections.url)
      }
    }
  }

}
