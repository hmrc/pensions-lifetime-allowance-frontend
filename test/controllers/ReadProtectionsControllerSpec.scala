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
import config._
import connectors.PLAConnector
import connectors.PlaConnectorError.{IncorrectResponseBodyError, ResponseLockedError, UnexpectedResponseError}
import constructors.{DisplayConstructors, ResponseConstructors}
import mocks.AuthMock
import models.cache.CacheMap
import models.{ExistingProtectionsDisplayModel, ProtectionModel, ReadResponseModel, TransformedReadResponseModel}
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.HeaderNames.CACHE_CONTROL
import play.api.i18n.Lang
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, Environment}
import services.SessionCacheService
import testHelpers.FakeApplication
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.http.HttpResponse
import utils.ActionWithSessionId
import views.html.pages.existingProtections.existingProtections
import views.html.pages.fallback.technicalError
import views.html.pages.result.manualCorrespondenceNeeded

import scala.concurrent.{ExecutionContext, Future}

class ReadProtectionsControllerSpec extends FakeApplication with MockitoSugar with AuthMock {

  val testSuccessResponse =
    HttpResponse(status = 200, json = Json.parse("""{"thisJson":"doesNotMatter"}"""), headers = Map.empty)

  val testMCNeededResponse      = HttpResponse(423, "")
  val testUpstreamErrorResponse = HttpResponse(503, "")

  private val psaCheckReference = "PSA12345678A"
  val testReadResponseModel    = ReadResponseModel(psaCheckReference, Seq.empty)
  val testTransformedReadResponseModel    = TransformedReadResponseModel(None, Seq.empty)
  val testExistingProtectionsDisplayModel = ExistingProtectionsDisplayModel(None, Seq.empty)

  val mockDisplayConstructors: DisplayConstructors   = mock[DisplayConstructors]
  val mockResponseConstructors: ResponseConstructors = mock[ResponseConstructors]
  val mockSessionCacheService: SessionCacheService   = mock[SessionCacheService]
  val mockPlaConnector: PLAConnector                 = mock[PLAConnector]
  val mockMCC: MessagesControllerComponents        = fakeApplication().injector.instanceOf[MessagesControllerComponents]
  val mockActionWithSessionId: ActionWithSessionId = mock[ActionWithSessionId]
  val mockAuthFunction: AuthFunction               = fakeApplication().injector.instanceOf[AuthFunction]
  val mockEnv: Environment                         = mock[Environment]

  implicit val executionContext: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  implicit val mockAppConfig: FrontendAppConfig   = fakeApplication().injector.instanceOf[FrontendAppConfig]
  implicit val mockPlaContext: PlaContext         = mock[PlaContext]
  implicit val system: ActorSystem                = ActorSystem()
  implicit val materializer: Materializer         = mock[Materializer]
  implicit val mockLang: Lang                     = mock[Lang]
  implicit val application: Application           = mock[Application]

  implicit val mockTechnicalError: technicalError = app.injector.instanceOf[technicalError]

  implicit val mockManualCorrespondenceNeeded: manualCorrespondenceNeeded =
    app.injector.instanceOf[manualCorrespondenceNeeded]

  implicit val mockExistingProtections: existingProtections = app.injector.instanceOf[existingProtections]

  val fakeRequest: FakeRequest[AnyContent] = FakeRequest()

  class Setup {

    val authFunction = new AuthFunction {
      override implicit val plaContext: PlaContext         = mockPlaContext
      override implicit val appConfig: FrontendAppConfig   = mockAppConfig
      override implicit val technicalError: technicalError = mockTechnicalError
      override implicit val ec: ExecutionContext           = executionContext

      override def authConnector: AuthConnector = mockAuthConnector
    }

    val controller = new ReadProtectionsController(
      mockPlaConnector,
      mockSessionCacheService,
      mockDisplayConstructors,
      mockMCC,
      mockResponseConstructors,
      authFunction,
      mockTechnicalError,
      mockManualCorrespondenceNeeded,
      mockExistingProtections
    )

  }

  val ip2016Protection = ProtectionModel(
    psaCheckReference = Some("testPSARef"),
    uncrystallisedRights = Some(100000.00),
    nonUKRights = Some(2000.00),
    preADayPensionInPayment = Some(2000.00),
    postADayBenefitCrystallisationEvents = Some(2000.00),
    notificationId = Some(12),
    protectionID = Some(12345),
    protectionType = Some("IP2016"),
    status = Some("dormant"),
    certificateDate = Some("2016-09-04T09:00:19.157"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456")
  )

  val nonAmendableProtection = ProtectionModel(
    psaCheckReference = Some("testPSARef"),
    uncrystallisedRights = Some(100000.00),
    nonUKRights = Some(2000.00),
    preADayPensionInPayment = Some(2000.00),
    postADayBenefitCrystallisationEvents = Some(2000.00),
    notificationId = Some(12),
    protectionID = Some(12345),
    protectionType = Some("IP2016"),
    status = Some("rejected"),
    certificateDate = Some("2016-09-04T09:00:19.157"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456")
  )

  val mockCacheMap = mock[CacheMap]

  def mockCacheSave: OngoingStubbing[Future[CacheMap]] =
    when(mockSessionCacheService.saveFormData(any(), any())(any(), any()))
      .thenReturn(Future(mockCacheMap))

  "Calling saveActiveProtection" should {

    "return a true" when {

      "provided with no protection model" in new Setup {
        when(mockPlaConnector.readProtections(any())(any(), any()))
          .thenReturn(Future.successful(Right(testReadResponseModel)))
        when(mockDisplayConstructors.createExistingProtectionsDisplayModel(any())(any()))
          .thenReturn(testExistingProtectionsDisplayModel)

        await(controller.saveActiveProtection(None)(fakeRequest)) shouldBe true
      }

      "provided with a protection model" in new Setup {
        when(mockPlaConnector.readProtections(any())(any(), any()))
          .thenReturn(Future.successful(Right(testReadResponseModel)))
        when(mockDisplayConstructors.createExistingProtectionsDisplayModel(any())(any()))
          .thenReturn(testExistingProtectionsDisplayModel)
        when(mockSessionCacheService.saveFormData(any(), any())(any(), any()))
          .thenReturn(Future.successful(mock[CacheMap]))

        await(controller.saveActiveProtection(Some(ip2016Protection))(fakeRequest)) shouldBe true
      }
    }
  }

  "Calling getAmendableProtection" should {

    "return an empty sequence if no protections exist" in new Setup {
      when(mockPlaConnector.readProtections(any())(any(), any()))
        .thenReturn(Future.successful(Right(testReadResponseModel)))
      when(mockDisplayConstructors.createExistingProtectionsDisplayModel(any())(any()))
        .thenReturn(testExistingProtectionsDisplayModel)
      val model = TransformedReadResponseModel(None, Seq())

      controller.getAmendableProtections(model) shouldBe Seq()
    }

    "return an empty sequence if no protections are amendable" in new Setup {
      when(mockPlaConnector.readProtections(any())(any(), any()))
        .thenReturn(Future.successful(Right(testReadResponseModel)))
      when(mockDisplayConstructors.createExistingProtectionsDisplayModel(any())(any()))
        .thenReturn(testExistingProtectionsDisplayModel)
      val model =
        TransformedReadResponseModel(Some(nonAmendableProtection), Seq(nonAmendableProtection, nonAmendableProtection))

      controller.getAmendableProtections(model) shouldBe Seq()
    }

    "return a single element if only the active protection is amendable" in new Setup {
      when(mockPlaConnector.readProtections(any())(any(), any()))
        .thenReturn(Future.successful(Right(testReadResponseModel)))
      when(mockDisplayConstructors.createExistingProtectionsDisplayModel(any())(any()))
        .thenReturn(testExistingProtectionsDisplayModel)
      val model =
        TransformedReadResponseModel(Some(ip2016Protection), Seq(nonAmendableProtection, nonAmendableProtection))

      controller.getAmendableProtections(model) shouldBe Seq(ip2016Protection)
    }

    "return all inactive elements if only they are amendable" in new Setup {
      when(mockPlaConnector.readProtections(any())(any(), any()))
        .thenReturn(Future.successful(Right(testReadResponseModel)))
      when(mockDisplayConstructors.createExistingProtectionsDisplayModel(any())(any()))
        .thenReturn(testExistingProtectionsDisplayModel)
      val model = TransformedReadResponseModel(Some(nonAmendableProtection), Seq(ip2016Protection, ip2016Protection))

      controller.getAmendableProtections(model) shouldBe Seq(ip2016Protection, ip2016Protection)
    }

    "return all elements if they are all amendable" in new Setup {
      when(mockPlaConnector.readProtections(any())(any(), any()))
        .thenReturn(Future.successful(Right(testReadResponseModel)))
      when(mockDisplayConstructors.createExistingProtectionsDisplayModel(any())(any()))
        .thenReturn(testExistingProtectionsDisplayModel)
      val model = TransformedReadResponseModel(Some(ip2016Protection), Seq(ip2016Protection, ip2016Protection))

      controller.getAmendableProtections(model) shouldBe Seq(ip2016Protection, ip2016Protection, ip2016Protection)
    }
  }

  "Calling saveAmendableProtection" should {

    "return an empty sequence if no protections exist" in new Setup {
      when(mockPlaConnector.readProtections(any())(any(), any()))
        .thenReturn(Future.successful(Right(testReadResponseModel)))
      when(mockDisplayConstructors.createExistingProtectionsDisplayModel(any())(any()))
        .thenReturn(testExistingProtectionsDisplayModel)
      val model = TransformedReadResponseModel(None, Seq())
      mockCacheSave

      await(controller.saveAmendableProtections(model)(fakeRequest)) shouldBe Seq()
    }

    "return an empty sequence if no protections are amendable" in new Setup {
      when(mockPlaConnector.readProtections(any())(any(), any()))
        .thenReturn(Future.successful(Right(testReadResponseModel)))
      when(mockDisplayConstructors.createExistingProtectionsDisplayModel(any())(any()))
        .thenReturn(testExistingProtectionsDisplayModel)
      val model =
        TransformedReadResponseModel(Some(nonAmendableProtection), Seq(nonAmendableProtection, nonAmendableProtection))
      mockCacheSave

      await(controller.saveAmendableProtections(model)(fakeRequest)) shouldBe Seq()
    }

    "return a single cache map if only the active protection is amendable" in new Setup {
      when(mockPlaConnector.readProtections(any())(any(), any()))
        .thenReturn(Future.successful(Right(testReadResponseModel)))
      when(mockDisplayConstructors.createExistingProtectionsDisplayModel(any())(any()))
        .thenReturn(testExistingProtectionsDisplayModel)
      val model =
        TransformedReadResponseModel(Some(ip2016Protection), Seq(nonAmendableProtection, nonAmendableProtection))
      mockCacheSave

      await(controller.saveAmendableProtections(model)(fakeRequest)) shouldBe Seq(mockCacheMap)
    }

    "return a cache map per inactive elements if only they are amendable" in new Setup {
      when(mockPlaConnector.readProtections(any())(any(), any()))
        .thenReturn(Future.successful(Right(testReadResponseModel)))
      when(mockDisplayConstructors.createExistingProtectionsDisplayModel(any())(any()))
        .thenReturn(testExistingProtectionsDisplayModel)
      val model = TransformedReadResponseModel(Some(nonAmendableProtection), Seq(ip2016Protection, ip2016Protection))
      mockCacheSave

      await(controller.saveAmendableProtections(model)(fakeRequest)) shouldBe Seq(mockCacheMap, mockCacheMap)
    }

    "return a cache map per element if they are all amendable" in new Setup {
      when(mockPlaConnector.readProtections(any())(any(), any()))
        .thenReturn(Future.successful(Right(testReadResponseModel)))
      when(mockDisplayConstructors.createExistingProtectionsDisplayModel(any())(any()))
        .thenReturn(testExistingProtectionsDisplayModel)
      val model = TransformedReadResponseModel(Some(ip2016Protection), Seq(ip2016Protection, ip2016Protection))
      mockCacheSave

      await(controller.saveAmendableProtections(model)(fakeRequest)) shouldBe Seq(
        mockCacheMap,
        mockCacheMap,
        mockCacheMap
      )
    }
  }

  "Calling the currentProtections Action" when {

    "receiving an upstream error" should {
      "return 500 and show the technical error page for existing protections" in new Setup {
        when(mockPlaConnector.readProtections(any())(any(), any()))
          .thenReturn(Future.successful(Left(UnexpectedResponseError(503))))
        when(mockDisplayConstructors.createExistingProtectionsDisplayModel(any())(any()))
          .thenReturn(testExistingProtectionsDisplayModel)
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

        val result = controller.currentProtections(fakeRequest)

        status(result) shouldBe 500
        await(result).header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache"
      }
    }

    "receiving a ResponseLockedError response" should {
      "return 423 and show the Manual Correspondence Needed page" in new Setup {
        when(mockPlaConnector.readProtections(any())(any(), any()))
          .thenReturn(Future.successful(Left(ResponseLockedError)))
        when(mockDisplayConstructors.createExistingProtectionsDisplayModel(any())(any()))
          .thenReturn(testExistingProtectionsDisplayModel)

        val result = controller.currentProtections(fakeRequest)

        status(result) shouldBe 423
      }
    }

    "receiving incorrect json in the PLA response" should {
      "return 500 and show the technical error page for existing protections" in new Setup {
        when(mockPlaConnector.readProtections(any())(any(), any()))
          .thenReturn(Future.successful(Left(IncorrectResponseBodyError)))
        when(mockDisplayConstructors.createExistingProtectionsDisplayModel(any())(any()))
          .thenReturn(testExistingProtectionsDisplayModel)
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

        val result = controller.currentProtections(fakeRequest)

        status(result) shouldBe 500
        await(result).header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache"
      }
    }

    "receiving a correct response from PLA" should {
      "return 200 and show the existing protections page" in new Setup {
        when(mockPlaConnector.readProtections(any())(any(), any()))
          .thenReturn(Future.successful(Right(testReadResponseModel)))
        when(mockResponseConstructors.transformReadResponseModel(any()))
          .thenReturn(testTransformedReadResponseModel)
        when(mockDisplayConstructors.createExistingProtectionsDisplayModel(any())(any()))
          .thenReturn(testExistingProtectionsDisplayModel)
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

        val result = controller.currentProtections(fakeRequest)

        status(result) shouldBe 200
      }
    }
  }

}
