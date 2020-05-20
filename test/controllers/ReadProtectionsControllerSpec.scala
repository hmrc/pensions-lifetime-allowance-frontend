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

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import auth.{AuthFunction, AuthFunctionImpl}
import config._
import config.wiring.PlaFormPartialRetriever
import connectors.{KeyStoreConnector, PLAConnector}
import constructors.{DisplayConstructors, ResponseConstructors}
import mocks.AuthMock
import models.{ExistingProtectionsDisplayModel, ProtectionModel, TransformedReadResponseModel}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.{Configuration, Environment}
import play.api.http.HeaderNames.CACHE_CONTROL
import play.api.i18n.Lang
import play.api.libs.json.Json
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import testHelpers.MockTemplateRenderer
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.Retrievals
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import utils.ActionWithSessionId

import scala.concurrent.Future

class ReadProtectionsControllerSpec extends UnitSpec with MockitoSugar with AuthMock with WithFakeApplication with BeforeAndAfterEach {

  val testSuccessResponse = HttpResponse(200, Some(Json.parse("""{"thisJson":"doesNotMatter"}""")))
  val testMCNeededResponse = HttpResponse(423)
  val testUpstreamErrorResponse = HttpResponse(503)

  val testTransformedReadResponseModel = TransformedReadResponseModel(None, Seq.empty)
  val testExistingProtectionsDisplayModel = ExistingProtectionsDisplayModel(None, Seq.empty)

  val mockDisplayConstructors: DisplayConstructors = mock[DisplayConstructors]
  val mockResponseConstructors: ResponseConstructors = mock[ResponseConstructors]
  val mockKeyStoreConnector: KeyStoreConnector = mock[KeyStoreConnector]
  val mockPlaConnector: PLAConnector = mock[PLAConnector]
  val mockMCC: MessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val mockActionWithSessionId: ActionWithSessionId = mock[ActionWithSessionId]
  val mockAuthFunction: AuthFunction = fakeApplication.injector.instanceOf[AuthFunction]
  val mockEnv: Environment = mock[Environment]

  implicit val mockTemplateRenderer: LocalTemplateRenderer = MockTemplateRenderer.renderer
  implicit val mockPartialRetriever: PlaFormPartialRetriever = mock[PlaFormPartialRetriever]
  implicit val mockAppConfig: FrontendAppConfig = fakeApplication.injector.instanceOf[FrontendAppConfig]
  implicit val mockPlaContext: PlaContext = mock[PlaContext]
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val mockLang: Lang = mock[Lang]


  val fakeRequest = FakeRequest()

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

    val controller = new ReadProtectionsController(
      mockPlaConnector,
      mockKeyStoreConnector,
      mockDisplayConstructors,
      mockMCC,
      mockResponseConstructors,
      authFunction)
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
    protectionReference = Some("PSA123456"))

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
    protectionReference = Some("PSA123456"))

  val mockCacheMap = mock[CacheMap]

  def mockKeystoreSave: OngoingStubbing[Future[CacheMap]] = {
    import play.api.Configuration
    import uk.gov.hmrc.auth.core.AuthConnector
    when(mockKeyStoreConnector.saveData(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(mockCacheMap)
  }


    "Calling saveActiveProtection" should {

      "return a true" when {

        "provided with no protection model" in new Setup{
          when(mockPlaConnector.readProtections(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testSuccessResponse)
          when(mockResponseConstructors.createTransformedReadResponseModelFromJson(ArgumentMatchers.any())).thenReturn(Some(testTransformedReadResponseModel))
          when(mockDisplayConstructors.createExistingProtectionsDisplayModel(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testExistingProtectionsDisplayModel)
          await(controller.saveActiveProtection(None)(fakeRequest)) shouldBe true
        }

        "provided with a protection model" in new Setup {
          when(mockPlaConnector.readProtections(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testSuccessResponse)
          when(mockResponseConstructors.createTransformedReadResponseModelFromJson(ArgumentMatchers.any())).thenReturn(Some(testTransformedReadResponseModel))
          when(mockDisplayConstructors.createExistingProtectionsDisplayModel(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testExistingProtectionsDisplayModel)
          when(mockKeyStoreConnector.saveData(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Future.successful(mock[CacheMap]))
          await(controller.saveActiveProtection(Some(ip2016Protection))(fakeRequest)) shouldBe true
        }
      }
    }

    "Calling getAmendableProtection" should {

      "return an empty sequence if no protections exist" in new Setup {
        when(mockPlaConnector.readProtections(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testSuccessResponse)
        when(mockResponseConstructors.createTransformedReadResponseModelFromJson(ArgumentMatchers.any())).thenReturn(Some(testTransformedReadResponseModel))
        when(mockDisplayConstructors.createExistingProtectionsDisplayModel(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testExistingProtectionsDisplayModel)
        val model = TransformedReadResponseModel(None, Seq())

        controller.getAmendableProtections(model) shouldBe Seq()
      }

      "return an empty sequence if no protections are amendable" in new Setup {
        when(mockPlaConnector.readProtections(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testSuccessResponse)
        when(mockResponseConstructors.createTransformedReadResponseModelFromJson(ArgumentMatchers.any())).thenReturn(Some(testTransformedReadResponseModel))
        when(mockDisplayConstructors.createExistingProtectionsDisplayModel(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testExistingProtectionsDisplayModel)
        val model = TransformedReadResponseModel(Some(nonAmendableProtection), Seq(nonAmendableProtection, nonAmendableProtection))

        controller.getAmendableProtections(model) shouldBe Seq()
      }

      "return a single element if only the active protection is amendable" in new Setup {
        when(mockPlaConnector.readProtections(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testSuccessResponse)
        when(mockResponseConstructors.createTransformedReadResponseModelFromJson(ArgumentMatchers.any())).thenReturn(Some(testTransformedReadResponseModel))
        when(mockDisplayConstructors.createExistingProtectionsDisplayModel(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testExistingProtectionsDisplayModel)
        val model = TransformedReadResponseModel(Some(ip2016Protection), Seq(nonAmendableProtection, nonAmendableProtection))

        controller.getAmendableProtections(model) shouldBe Seq(ip2016Protection)
      }

      "return all inactive elements if only they are amendable" in new Setup {
        when(mockPlaConnector.readProtections(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testSuccessResponse)
        when(mockResponseConstructors.createTransformedReadResponseModelFromJson(ArgumentMatchers.any())).thenReturn(Some(testTransformedReadResponseModel))
        when(mockDisplayConstructors.createExistingProtectionsDisplayModel(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testExistingProtectionsDisplayModel)
        val model = TransformedReadResponseModel(Some(nonAmendableProtection), Seq(ip2016Protection, ip2016Protection))

        controller.getAmendableProtections(model) shouldBe Seq(ip2016Protection, ip2016Protection)
      }

      "return all elements if they are all amendable" in new Setup {
        when(mockPlaConnector.readProtections(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testSuccessResponse)
        when(mockResponseConstructors.createTransformedReadResponseModelFromJson(ArgumentMatchers.any())).thenReturn(Some(testTransformedReadResponseModel))
        when(mockDisplayConstructors.createExistingProtectionsDisplayModel(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testExistingProtectionsDisplayModel)
        val model = TransformedReadResponseModel(Some(ip2016Protection), Seq(ip2016Protection, ip2016Protection))

        controller.getAmendableProtections(model) shouldBe Seq(ip2016Protection, ip2016Protection, ip2016Protection)
      }
    }

    "Calling saveAmendableProtection" should {

      "return an empty sequence if no protections exist" in new Setup {
        when(mockPlaConnector.readProtections(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testSuccessResponse)
        when(mockResponseConstructors.createTransformedReadResponseModelFromJson(ArgumentMatchers.any())).thenReturn(Some(testTransformedReadResponseModel))
        when(mockDisplayConstructors.createExistingProtectionsDisplayModel(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testExistingProtectionsDisplayModel)
        val model = TransformedReadResponseModel(None, Seq())
        mockKeystoreSave
        await(controller.saveAmendableProtections(model)(fakeRequest)) shouldBe Seq()
      }

      "return an empty sequence if no protections are amendable" in new Setup {
        when(mockPlaConnector.readProtections(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testSuccessResponse)
        when(mockResponseConstructors.createTransformedReadResponseModelFromJson(ArgumentMatchers.any())).thenReturn(Some(testTransformedReadResponseModel))
        when(mockDisplayConstructors.createExistingProtectionsDisplayModel(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testExistingProtectionsDisplayModel)
        val model = TransformedReadResponseModel(Some(nonAmendableProtection), Seq(nonAmendableProtection, nonAmendableProtection))
        mockKeystoreSave
        await(controller.saveAmendableProtections(model)(fakeRequest)) shouldBe Seq()
      }

      "return a single cache map if only the active protection is amendable" in new Setup {
        when(mockPlaConnector.readProtections(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testSuccessResponse)
        when(mockResponseConstructors.createTransformedReadResponseModelFromJson(ArgumentMatchers.any())).thenReturn(Some(testTransformedReadResponseModel))
        when(mockDisplayConstructors.createExistingProtectionsDisplayModel(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testExistingProtectionsDisplayModel)
        val model = TransformedReadResponseModel(Some(ip2016Protection), Seq(nonAmendableProtection, nonAmendableProtection))
        mockKeystoreSave
        await(controller.saveAmendableProtections(model)(fakeRequest)) shouldBe Seq(mockCacheMap)
      }

      "return a cache map per inactive elements if only they are amendable" in new Setup {
        when(mockPlaConnector.readProtections(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testSuccessResponse)
        when(mockResponseConstructors.createTransformedReadResponseModelFromJson(ArgumentMatchers.any())).thenReturn(Some(testTransformedReadResponseModel))
        when(mockDisplayConstructors.createExistingProtectionsDisplayModel(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testExistingProtectionsDisplayModel)
        val model = TransformedReadResponseModel(Some(nonAmendableProtection), Seq(ip2016Protection, ip2016Protection))
        mockKeystoreSave
        await(controller.saveAmendableProtections(model)(fakeRequest)) shouldBe Seq(mockCacheMap, mockCacheMap)
      }

      "return a cache map per element if they are all amendable" in new Setup {
        when(mockPlaConnector.readProtections(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testSuccessResponse)
        when(mockResponseConstructors.createTransformedReadResponseModelFromJson(ArgumentMatchers.any())).thenReturn(Some(testTransformedReadResponseModel))
        when(mockDisplayConstructors.createExistingProtectionsDisplayModel(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testExistingProtectionsDisplayModel)
        val model = TransformedReadResponseModel(Some(ip2016Protection), Seq(ip2016Protection, ip2016Protection))
        mockKeystoreSave
        await(controller.saveAmendableProtections(model)(fakeRequest)) shouldBe Seq(mockCacheMap, mockCacheMap, mockCacheMap)
      }
    }

    "Calling getNonAmendableProtection" should {

      "return an empty sequence if no protections exist" in new Setup {
        when(mockPlaConnector.readProtections(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testSuccessResponse)
        when(mockResponseConstructors.createTransformedReadResponseModelFromJson(ArgumentMatchers.any())).thenReturn(Some(testTransformedReadResponseModel))
        when(mockDisplayConstructors.createExistingProtectionsDisplayModel(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testExistingProtectionsDisplayModel)
        val model = TransformedReadResponseModel(None, Seq())

        controller.getNonAmendableProtections(model) shouldBe Seq()
      }

      "return an empty sequence if all protections are amendable" in new Setup {
        when(mockPlaConnector.readProtections(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testSuccessResponse)
        when(mockResponseConstructors.createTransformedReadResponseModelFromJson(ArgumentMatchers.any())).thenReturn(Some(testTransformedReadResponseModel))
        when(mockDisplayConstructors.createExistingProtectionsDisplayModel(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testExistingProtectionsDisplayModel)
        val model = TransformedReadResponseModel(Some(ip2016Protection), Seq(ip2016Protection, ip2016Protection))

        controller.getNonAmendableProtections(model) shouldBe Seq()
      }

      "return a single element if only the active protection is non-amendable" in new Setup {
        when(mockPlaConnector.readProtections(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testSuccessResponse)
        when(mockResponseConstructors.createTransformedReadResponseModelFromJson(ArgumentMatchers.any())).thenReturn(Some(testTransformedReadResponseModel))
        when(mockDisplayConstructors.createExistingProtectionsDisplayModel(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testExistingProtectionsDisplayModel)
        val model = TransformedReadResponseModel(Some(nonAmendableProtection), Seq(ip2016Protection, ip2016Protection))

        controller.getNonAmendableProtections(model) shouldBe Seq(nonAmendableProtection)
      }

      "return all inactive elements if only they are non-amendable" in new Setup {
        when(mockPlaConnector.readProtections(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testSuccessResponse)
        when(mockResponseConstructors.createTransformedReadResponseModelFromJson(ArgumentMatchers.any())).thenReturn(Some(testTransformedReadResponseModel))
        when(mockDisplayConstructors.createExistingProtectionsDisplayModel(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testExistingProtectionsDisplayModel)
        val model = TransformedReadResponseModel(Some(ip2016Protection), Seq(nonAmendableProtection, nonAmendableProtection))

        controller.getNonAmendableProtections(model) shouldBe Seq(nonAmendableProtection, nonAmendableProtection)
      }

      "return all elements if they are all non-amendable" in new Setup {
        when(mockPlaConnector.readProtections(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testSuccessResponse)
        when(mockResponseConstructors.createTransformedReadResponseModelFromJson(ArgumentMatchers.any())).thenReturn(Some(testTransformedReadResponseModel))
        when(mockDisplayConstructors.createExistingProtectionsDisplayModel(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testExistingProtectionsDisplayModel)
        val model = TransformedReadResponseModel(Some(nonAmendableProtection), Seq(nonAmendableProtection, nonAmendableProtection))

        controller.getNonAmendableProtections(model) shouldBe Seq(nonAmendableProtection, nonAmendableProtection, nonAmendableProtection)
      }
    }

    "Calling saveNonAmendableProtection" should {

      "return an empty sequence if no protections exist" in new Setup {
        when(mockPlaConnector.readProtections(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testSuccessResponse)
        when(mockResponseConstructors.createTransformedReadResponseModelFromJson(ArgumentMatchers.any())).thenReturn(Some(testTransformedReadResponseModel))
        when(mockDisplayConstructors.createExistingProtectionsDisplayModel(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testExistingProtectionsDisplayModel)
        val model = TransformedReadResponseModel(None, Seq())
        mockKeystoreSave
        await(controller.saveNonAmendableProtections(model)(fakeRequest)) shouldBe Seq()
      }

      "return an empty sequence if no protections are amendable" in new Setup {
        when(mockPlaConnector.readProtections(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testSuccessResponse)
        when(mockResponseConstructors.createTransformedReadResponseModelFromJson(ArgumentMatchers.any())).thenReturn(Some(testTransformedReadResponseModel))
        when(mockDisplayConstructors.createExistingProtectionsDisplayModel(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testExistingProtectionsDisplayModel)
        val model = TransformedReadResponseModel(Some(ip2016Protection), Seq(ip2016Protection, ip2016Protection))
        mockKeystoreSave
        await(controller.saveNonAmendableProtections(model)(fakeRequest)) shouldBe Seq()
      }

      "return a single cache map if only the active protection is amendable" in new Setup {
        when(mockPlaConnector.readProtections(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testSuccessResponse)
        when(mockResponseConstructors.createTransformedReadResponseModelFromJson(ArgumentMatchers.any())).thenReturn(Some(testTransformedReadResponseModel))
        when(mockDisplayConstructors.createExistingProtectionsDisplayModel(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testExistingProtectionsDisplayModel)
        val model = TransformedReadResponseModel(Some(nonAmendableProtection), Seq(ip2016Protection, ip2016Protection))
        mockKeystoreSave
        await(controller.saveNonAmendableProtections(model)(fakeRequest)) shouldBe Seq(mockCacheMap)
      }

      "return a cache map per inactive elements if only they are amendable" in new Setup {
        when(mockPlaConnector.readProtections(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testSuccessResponse)
        when(mockResponseConstructors.createTransformedReadResponseModelFromJson(ArgumentMatchers.any())).thenReturn(Some(testTransformedReadResponseModel))
        when(mockDisplayConstructors.createExistingProtectionsDisplayModel(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testExistingProtectionsDisplayModel)
        val model = TransformedReadResponseModel(Some(ip2016Protection), Seq(nonAmendableProtection, nonAmendableProtection))
        mockKeystoreSave
        await(controller.saveNonAmendableProtections(model)(fakeRequest)) shouldBe Seq(mockCacheMap, mockCacheMap)
      }

      "return a cache map per element if they are all amendable" in new Setup {
        when(mockPlaConnector.readProtections(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testSuccessResponse)
        when(mockResponseConstructors.createTransformedReadResponseModelFromJson(ArgumentMatchers.any())).thenReturn(Some(testTransformedReadResponseModel))
        when(mockDisplayConstructors.createExistingProtectionsDisplayModel(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testExistingProtectionsDisplayModel)
        val model = TransformedReadResponseModel(Some(nonAmendableProtection), Seq(nonAmendableProtection, nonAmendableProtection))
        mockKeystoreSave
        await(controller.saveNonAmendableProtections(model)(fakeRequest)) shouldBe Seq(mockCacheMap, mockCacheMap, mockCacheMap)
      }
    }

  "Calling the currentProtections Action" when {
    "receiving an upstream error" should {
      "return 500 and show the technical error page for existing protections" in new Setup {
        when(mockPlaConnector.readProtections(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testUpstreamErrorResponse)
        when(mockResponseConstructors.createTransformedReadResponseModelFromJson(ArgumentMatchers.any())).thenReturn(Some(testTransformedReadResponseModel))
        when(mockDisplayConstructors.createExistingProtectionsDisplayModel(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testExistingProtectionsDisplayModel)
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))


        val result = await(controller.currentProtections(fakeRequest))
        status(result) shouldBe 500

        result.header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache"
      }
    }

    "receiving an MC needed response" should {
      "return 423 and show the MC Needed page" in new Setup {
        when(mockPlaConnector.readProtections(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testSuccessResponse)
        when(mockResponseConstructors.createTransformedReadResponseModelFromJson(ArgumentMatchers.any())).thenReturn(Some(testTransformedReadResponseModel))
        when(mockDisplayConstructors.createExistingProtectionsDisplayModel(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testExistingProtectionsDisplayModel)
        when(mockPlaConnector.readProtections(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testMCNeededResponse)
        val result = await(controller.currentProtections(fakeRequest))
        status(result) shouldBe 423
      }
    }

    "receiving incorrect json in the PLA response" should {
      "return 500 and show the technical error page for existing protections" in new Setup {
        when(mockPlaConnector.readProtections(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testSuccessResponse)
        when(mockResponseConstructors.createTransformedReadResponseModelFromJson(ArgumentMatchers.any())).thenReturn(Some(testTransformedReadResponseModel))
        when(mockDisplayConstructors.createExistingProtectionsDisplayModel(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testExistingProtectionsDisplayModel)
        when(mockPlaConnector.readProtections(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testSuccessResponse)
        when(mockResponseConstructors.createTransformedReadResponseModelFromJson(ArgumentMatchers.any())).thenReturn(None)

        val result = await(controller.currentProtections(fakeRequest))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        status(result) shouldBe 500
        result.header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache"
      }
    }

    "receiving a correct response from PLA" should {
      "return 200 and show the existing protections page" in new Setup {
        when(mockPlaConnector.readProtections(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testSuccessResponse)
        when(mockResponseConstructors.createTransformedReadResponseModelFromJson(ArgumentMatchers.any())).thenReturn(Some(testTransformedReadResponseModel))
        when(mockDisplayConstructors.createExistingProtectionsDisplayModel(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(testExistingProtectionsDisplayModel)

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        val result = await(controller.currentProtections(fakeRequest))

        status(result) shouldBe 200
      }
    }
  }
}
