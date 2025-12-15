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
import connectors.PlaConnectorError.{IncorrectResponseBodyError, LockedResponseError, UnexpectedResponseError}
import connectors.PlaConnector
import constructors.display.DisplayConstructors
import generators.ModelGenerators
import mocks.AuthMock
import models.amend.AmendProtectionModel
import models.{DateModel, ProtectionModel, TimeModel, TransformedReadResponseModel}
import models.cache.CacheMap
import models.pla.response.ProtectionStatus.{Dormant, Rejected}
import models.pla.response.ProtectionType.IndividualProtection2016
import models.display.{ExistingInactiveProtectionsDisplayModel, ExistingProtectionsDisplayModel}
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.HeaderNames.CACHE_CONTROL
import play.api.i18n.Lang
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, MessagesControllerComponents, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, Environment}
import services.SessionCacheService
import testHelpers.FakeApplication
import testdata.PlaConnectorTestData.readProtectionsResponse
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.http.HttpResponse
import utils.ActionWithSessionId
import views.html.pages.existingProtections.existingProtections
import views.html.pages.fallback.technicalError
import views.html.pages.result.manualCorrespondenceNeeded

import scala.concurrent.{ExecutionContext, Future}

class ReadProtectionsControllerSpec
    extends FakeApplication
    with MockitoSugar
    with AuthMock
    with ScalaFutures
    with ModelGenerators
    with BeforeAndAfterEach {

  val testSuccessResponse =
    HttpResponse(status = 200, json = Json.parse("""{"thisJson":"doesNotMatter"}"""), headers = Map.empty)

  val testMCNeededResponse      = HttpResponse(423, "")
  val testUpstreamErrorResponse = HttpResponse(503, "")

  private val testNino = "AB123456A"

  val testExistingProtectionsDisplayModel = ExistingProtectionsDisplayModel(
    inactiveProtections = ExistingInactiveProtectionsDisplayModel.empty,
    activeProtection = None
  )

  val mockDisplayConstructors: DisplayConstructors = mock[DisplayConstructors]
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
  val mockPlaConnector: PlaConnector               = mock[PlaConnector]
  val mockAppConfig: FrontendAppConfig             = mock[FrontendAppConfig]
  val mockMCC: MessagesControllerComponents        = inject[MessagesControllerComponents]
  val mockActionWithSessionId: ActionWithSessionId = mock[ActionWithSessionId]
  val mockAuthFunction: AuthFunction               = inject[AuthFunction]
  val mockEnv: Environment                         = mock[Environment]
  val mockCacheMap: CacheMap                       = mock[CacheMap]

  implicit val executionContext: ExecutionContext = inject[ExecutionContext]
  implicit val system: ActorSystem                = ActorSystem()
  implicit val materializer: Materializer         = mock[Materializer]
  implicit val mockLang: Lang                     = mock[Lang]
  implicit val application: Application           = mock[Application]

  implicit val mockTechnicalError: technicalError = inject[technicalError]

  implicit val mockManualCorrespondenceNeeded: manualCorrespondenceNeeded =
    inject[manualCorrespondenceNeeded]

  implicit val mockExistingProtections: existingProtections = inject[existingProtections]

  val fakeRequest: FakeRequest[AnyContent] = FakeRequest()

  override def beforeEach(): Unit = {
    reset(mockAppConfig)
    reset(mockPlaConnector)
    reset(mockDisplayConstructors)
    reset(mockSessionCacheService)
  }

  val authFunction: AuthFunction = new AuthFunction {
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
    authFunction,
    mockTechnicalError,
    mockManualCorrespondenceNeeded,
    mockExistingProtections
  )(
    application,
    mockAppConfig,
    executionContext
  )

  val individualProtection2016 = ProtectionModel(
    psaCheckReference = "testPSARef",
    identifier = 12345,
    sequenceNumber = 1,
    protectionType = IndividualProtection2016,
    status = Dormant,
    certificateDate = Some(DateModel.of(2016, 9, 4)),
    certificateTime = Some(TimeModel.of(9, 0, 19)),
    uncrystallisedRightsAmount = Some(100000.00),
    nonUKRightsAmount = Some(2000.00),
    preADayPensionInPaymentAmount = Some(2000.00),
    postADayBenefitCrystallisationEventAmount = Some(2000.00),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456")
  )

  val individualProtection2016AmendModel: AmendProtectionModel =
    AmendProtectionModel.tryFromProtection(individualProtection2016).get

  val nonAmendableProtection = ProtectionModel(
    psaCheckReference = "testPSARef",
    identifier = 12345,
    sequenceNumber = 1,
    protectionType = IndividualProtection2016,
    status = Rejected,
    certificateDate = Some(DateModel.of(2016, 9, 4)),
    certificateTime = Some(TimeModel.of(9, 0, 19)),
    uncrystallisedRightsAmount = Some(100000.00),
    nonUKRightsAmount = Some(2000.00),
    preADayPensionInPaymentAmount = Some(2000.00),
    postADayBenefitCrystallisationEventAmount = Some(2000.00),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456")
  )

  def mockCacheSave(): OngoingStubbing[Future[CacheMap]] = {
    when(mockSessionCacheService.saveOpenProtection(any())(any())).thenReturn(Future(mockCacheMap))
    when(mockSessionCacheService.saveAmendProtectionModel(any())(any())).thenReturn(Future(mockCacheMap))
  }

  "Calling saveActiveProtection" should {

    "return None" when {

      "provided with no protection model" in {
        when(mockPlaConnector.readProtections(any())(any(), any()))
          .thenReturn(Future.successful(Right(readProtectionsResponse)))
        when(mockDisplayConstructors.createExistingProtectionsDisplayModel(any())(any()))
          .thenReturn(testExistingProtectionsDisplayModel)

        await(controller.saveActiveProtection(None)(fakeRequest)) shouldBe None
        verify(mockSessionCacheService, times(0)).saveOpenProtection(any())(any())
      }
    }

    "return Some" when {

      "provided with a protection model" in {
        when(mockPlaConnector.readProtections(any())(any(), any()))
          .thenReturn(Future.successful(Right(readProtectionsResponse)))
        when(mockDisplayConstructors.createExistingProtectionsDisplayModel(any())(any()))
          .thenReturn(testExistingProtectionsDisplayModel)
        mockCacheSave()

        await(controller.saveActiveProtection(Some(individualProtection2016))(fakeRequest)) shouldBe defined
        verify(mockSessionCacheService).saveOpenProtection(eqTo(individualProtection2016))(any())
      }
    }
  }

  "Calling getAllProtections" should {
    "return an empty sequence if no protections exist" in {
      val model = TransformedReadResponseModel(None, Seq.empty)

      controller.getAllProtections(model) shouldBe empty
    }

    "return active protection if only protection" in {
      val model = TransformedReadResponseModel(Some(individualProtection2016), Seq.empty)

      val result = controller.getAllProtections(model)

      (result should have).length(1)
      result.head shouldBe individualProtection2016
    }

    "return inactive protections if only inactive protections" in {
      val model = TransformedReadResponseModel(None, Seq(nonAmendableProtection, nonAmendableProtection))

      val result = controller.getAllProtections(model)

      (result should have).length(2)
      result.head shouldBe nonAmendableProtection
      result(1) shouldBe nonAmendableProtection
    }

    "return both active and inactive protections if both present" in {
      val model = TransformedReadResponseModel(Some(individualProtection2016), Seq(nonAmendableProtection))

      val result = controller.getAllProtections(model)

      (result should have).length(2)
      result.head shouldBe individualProtection2016
      result(1) shouldBe nonAmendableProtection
    }
  }

  "Calling saveIfAmendable" should {
    "return None if protection not amendable" in {
      controller.saveIfAmendable(nonAmendableProtection)(fakeRequest) shouldBe None
    }

    "save protection if protection is amendable, returning Some" in {
      when(mockSessionCacheService.saveAmendProtectionModel(any())(any())).thenReturn(Future.successful(mockCacheMap))

      controller.saveIfAmendable(individualProtection2016)(fakeRequest) shouldBe defined

      verify(mockSessionCacheService).saveAmendProtectionModel(eqTo(individualProtection2016AmendModel))(any())
    }
  }

  "Calling saveAmendableProtections" should {

    "return an empty sequence if no protections exist" in {
      when(mockPlaConnector.readProtections(any())(any(), any()))
        .thenReturn(Future.successful(Right(readProtectionsResponse)))
      when(mockDisplayConstructors.createExistingProtectionsDisplayModel(any())(any()))
        .thenReturn(testExistingProtectionsDisplayModel)
      val model = TransformedReadResponseModel(None, Seq())
      mockCacheSave()

      await(controller.saveAmendableProtections(model)(fakeRequest)) shouldBe Seq()
    }

    "return an empty sequence if no protections are amendable" in {
      when(mockPlaConnector.readProtections(any())(any(), any()))
        .thenReturn(Future.successful(Right(readProtectionsResponse)))
      when(mockDisplayConstructors.createExistingProtectionsDisplayModel(any())(any()))
        .thenReturn(testExistingProtectionsDisplayModel)
      val model =
        TransformedReadResponseModel(Some(nonAmendableProtection), Seq(nonAmendableProtection, nonAmendableProtection))
      mockCacheSave()

      await(controller.saveAmendableProtections(model)(fakeRequest)) shouldBe Seq()
    }

    "return a single cache map if only the active protection is amendable" in {
      when(mockPlaConnector.readProtections(any())(any(), any()))
        .thenReturn(Future.successful(Right(readProtectionsResponse)))
      when(mockDisplayConstructors.createExistingProtectionsDisplayModel(any())(any()))
        .thenReturn(testExistingProtectionsDisplayModel)
      val model =
        TransformedReadResponseModel(
          Some(individualProtection2016),
          Seq(nonAmendableProtection, nonAmendableProtection)
        )
      mockCacheSave()

      await(controller.saveAmendableProtections(model)(fakeRequest)) shouldBe Seq(mockCacheMap)
    }

    "return a cache map per inactive elements if only they are amendable" in {
      when(mockPlaConnector.readProtections(any())(any(), any()))
        .thenReturn(Future.successful(Right(readProtectionsResponse)))
      when(mockDisplayConstructors.createExistingProtectionsDisplayModel(any())(any()))
        .thenReturn(testExistingProtectionsDisplayModel)
      val model = TransformedReadResponseModel(
        Some(nonAmendableProtection),
        Seq(individualProtection2016, individualProtection2016)
      )
      mockCacheSave()

      await(controller.saveAmendableProtections(model)(fakeRequest)) shouldBe Seq(mockCacheMap, mockCacheMap)
    }

    "return a cache map per element if they are all amendable" in {
      when(mockPlaConnector.readProtections(any())(any(), any()))
        .thenReturn(Future.successful(Right(readProtectionsResponse)))
      when(mockDisplayConstructors.createExistingProtectionsDisplayModel(any())(any()))
        .thenReturn(testExistingProtectionsDisplayModel)
      val model = TransformedReadResponseModel(
        Some(individualProtection2016),
        Seq(individualProtection2016, individualProtection2016)
      )
      mockCacheSave()

      await(controller.saveAmendableProtections(model)(fakeRequest)) shouldBe Seq(
        mockCacheMap,
        mockCacheMap,
        mockCacheMap
      )
    }
  }

  "Calling the currentProtections Action" when {

    "called should call PlaConnector" in {
      when(mockPlaConnector.readProtections(any())(any(), any()))
        .thenReturn(Future.successful(Right(readProtectionsResponseGen.sample.value)))
      when(mockDisplayConstructors.createExistingProtectionsDisplayModel(any())(any()))
        .thenReturn(testExistingProtectionsDisplayModel)
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some(testNino))
      mockCacheSave()

      controller.currentProtections(fakeRequest).futureValue

      verify(mockPlaConnector).readProtections(eqTo(testNino))(any(), any())
    }

    "receiving UnexpectedResponseError response" should {
      "return 500 and show the technical error page for existing protections" in {
        when(mockPlaConnector.readProtections(any())(any(), any()))
          .thenReturn(Future.successful(Left(UnexpectedResponseError(503))))
        when(mockDisplayConstructors.createExistingProtectionsDisplayModel(any())(any()))
          .thenReturn(testExistingProtectionsDisplayModel)
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some(testNino))

        val result: Future[Result] = controller.currentProtections(fakeRequest)

        status(result) shouldBe 500
        await(result).header.headers(CACHE_CONTROL) shouldBe "no-cache"
      }
    }

    "receiving LockedResponseError response" should {
      "return 423 and show the Manual Correspondence Needed page" in {
        when(mockPlaConnector.readProtections(any())(any(), any()))
          .thenReturn(Future.successful(Left(LockedResponseError)))
        when(mockDisplayConstructors.createExistingProtectionsDisplayModel(any())(any()))
          .thenReturn(testExistingProtectionsDisplayModel)

        val result: Future[Result] = controller.currentProtections(fakeRequest)

        status(result) shouldBe 423
      }
    }

    "receiving IncorrectResponseBodyError response" should {
      "return 500 and show the technical error page for existing protections" in {
        when(mockPlaConnector.readProtections(any())(any(), any()))
          .thenReturn(Future.successful(Left(IncorrectResponseBodyError)))
        when(mockDisplayConstructors.createExistingProtectionsDisplayModel(any())(any()))
          .thenReturn(testExistingProtectionsDisplayModel)
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some(testNino))

        val result: Future[Result] = controller.currentProtections(fakeRequest)

        status(result) shouldBe 500
        await(result).header.headers(CACHE_CONTROL) shouldBe "no-cache"
      }
    }

    "receiving a correct response from PLA" should {
      "return 200 and show the existing protections page" in {
        when(mockPlaConnector.readProtections(any())(any(), any()))
          .thenReturn(Future.successful(Right(readProtectionsResponse)))
        when(mockDisplayConstructors.createExistingProtectionsDisplayModel(any())(any()))
          .thenReturn(testExistingProtectionsDisplayModel)
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some(testNino))

        val result: Future[Result] = controller.currentProtections(fakeRequest)

        status(result) shouldBe 200
      }
    }
  }

}
