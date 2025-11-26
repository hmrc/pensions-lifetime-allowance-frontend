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
import config.FrontendAppConfig
import connectors.CitizenDetailsConnector
import constructors.display.DisplayConstructors
import mocks.AuthMock
import models._
import models.display.PrintDisplayModel
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
import views.html.pages.result.resultPrint

import scala.concurrent.{ExecutionContext, Future}

class PrintControllerSpec extends FakeApplication with MockitoSugar with AuthMock with BeforeAndAfterEach {

  private val displayConstructors: DisplayConstructors         = mock[DisplayConstructors]
  private val sessionCacheService: SessionCacheService         = mock[SessionCacheService]
  private val citizenDetailsConnector: CitizenDetailsConnector = mock[CitizenDetailsConnector]
  private val resultPrintView: resultPrint                     = mock[resultPrint]

  private val messagesControllerComponents: MessagesControllerComponents =
    inject[MessagesControllerComponents]

  private implicit val executionContext: ExecutionContext   = inject[ExecutionContext]
  private implicit val frontendAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

  private val authFunction = new AuthFunction {
    override implicit val appConfig: FrontendAppConfig   = frontendAppConfig
    override implicit val technicalError: technicalError = inject[technicalError]
    override implicit val ec: ExecutionContext           = executionContext

    override def authConnector: AuthConnector = mockAuthConnector
  }

  private val printController = new PrintController(
    sessionCacheService,
    citizenDetailsConnector,
    displayConstructors,
    resultPrintView,
    messagesControllerComponents,
    authFunction
  )

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(displayConstructors)
    reset(sessionCacheService)
    reset(citizenDetailsConnector)
    reset(resultPrintView)
    reset(frontendAppConfig)

    when(resultPrintView.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  private val fakeRequest: FakeRequest[AnyContent] = FakeRequest()

  private val testNino: String = "AB123456A"

  private val testPersonalDetails = PersonalDetailsModel(Person("McTestFace", "Testy"))

  private val testProtectionModel = ProtectionModel(
    psaCheckReference = Some("tstPSACeckRef"),
    protectionID = Some(1111111),
    notificationId = Some(15)
  )

  private val testPrintDisplayModel = PrintDisplayModel(
    "Testy",
    "Mctestface",
    "AA11TESTA",
    "IP2016",
    "open",
    "PSATestNum",
    "ProtRefTestNum",
    Some("£1,246,500"),
    Some("3 April 2016")
  )

  "PrintController on printView" when {

    "there is no data in cache" should {
      "return Redirect to ReadProtectionsController.currentProtections" in {
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some(testNino))
        when(citizenDetailsConnector.getPersonDetails(any())(any()))
          .thenReturn(Future(Some(testPersonalDetails)))
        when(sessionCacheService.fetchAndGetFormData[ProtectionModel](any())(any(), any()))
          .thenReturn(Future(None))

        val result = printController.printView(fakeRequest)

        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some(routes.ReadProtectionsController.currentProtections.url)
      }
    }

    (Constants.amendmentCodesList ++ Seq(15, 16)).foreach { notificationId =>
      s"ProtectionModel stored in cache contains notificationId: $notificationId" should {
        "return Ok with resultPrintView view" in {
          val protectionModel   = testProtectionModel.copy(notificationId = Some(notificationId))
          val printDisplayModel = testPrintDisplayModel
          mockAuthRetrieval[Option[String]](Retrievals.nino, Some(testNino))

          when(citizenDetailsConnector.getPersonDetails(any())(any()))
            .thenReturn(Future.successful(Some(testPersonalDetails)))
          when(sessionCacheService.fetchAndGetFormData[ProtectionModel](any())(any(), any()))
            .thenReturn(Future.successful(Some(protectionModel)))
          when(displayConstructors.createPrintDisplayModel(any(), any(), any())(any()))
            .thenReturn(printDisplayModel)

          val result = printController.printView(fakeRequest)

          status(result) shouldBe 200
          verify(resultPrintView).apply(ArgumentMatchers.eq(printDisplayModel))(any(), any())
        }
      }
    }
  }

}
