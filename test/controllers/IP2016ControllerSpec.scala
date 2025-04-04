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
import connectors.PLAConnector
import mocks.AuthMock
import models._
import models.cache.CacheMap
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.libs.json.JsValue
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, Environment}
import services.SessionCacheService
import testHelpers._
import uk.gov.hmrc.auth.core.{AuthConnector, PlayAuthConnector}
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import uk.gov.hmrc.http.HeaderCarrier
import views.html.pages.fallback.technicalError
import views.html.pages.ip2016
import views.html.pages.ip2016._

import java.time.LocalDate
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class IP2016ControllerSpec extends FakeApplication with MockitoSugar with BeforeAndAfterEach with SessionCacheTestHelper with AuthMock {


  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
  val mockPlaConnector: PLAConnector = mock[PLAConnector]
  val mockMCC: MessagesControllerComponents = fakeApplication().injector.instanceOf[MessagesControllerComponents]
  val mockAuthFunction: AuthFunction = fakeApplication().injector.instanceOf[AuthFunction]
  val mockEnv: Environment = mock[Environment]
  val mockWithdrawnAp2016View: withdrawnAP2016 = app.injector.instanceOf[withdrawnAP2016]


  implicit val mockAppConfig = mock[FrontendAppConfig]
  implicit val executionContext: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  //implicit val mockAppConfig: FrontendAppConfig = fakeApplication().injector.instanceOf[FrontendAppConfig]
  implicit val mockPlaContext: PlaContext = mock[PlaContext]
  implicit val mockMessages: Messages = mock[Messages]
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = mock[Materializer]
  implicit val application: Application = mock[Application]
  implicit val mockTechnicalError: technicalError = app.injector.instanceOf[technicalError]
  implicit val mockPensionsTaken: pensionsTaken = app.injector.instanceOf[pensionsTaken]
  implicit val mockPensionsTakenBefore: pensionsTakenBefore = app.injector.instanceOf[pensionsTakenBefore]
  implicit val mockPensionsWorthBefore: pensionsWorthBefore = app.injector.instanceOf[pensionsWorthBefore]
  implicit val mockPensionsTakenBetween: pensionsTakenBetween = app.injector.instanceOf[pensionsTakenBetween]
  implicit val mockPensionsUsedBetween: pensionsUsedBetween = app.injector.instanceOf[pensionsUsedBetween]
  implicit val mockOverseasPensions: overseasPensions = app.injector.instanceOf[overseasPensions]
  implicit val mockCurrentPensions: currentPensions = app.injector.instanceOf[currentPensions]
  implicit val mockPsoDetails: psoDetails = app.injector.instanceOf[psoDetails]
  implicit val mockRemovePsoDetails: removePsoDetails = app.injector.instanceOf[removePsoDetails]
  implicit val mockPensionDebits: pensionDebits = app.injector.instanceOf[pensionDebits]
  implicit val formWithCSRF: FormWithCSRF = app.injector.instanceOf[FormWithCSRF]


  class Setup {

    val authFunction: AuthFunction = new AuthFunction {
      override implicit val plaContext: PlaContext = mockPlaContext
      override implicit val appConfig: FrontendAppConfig = mockAppConfig
      override implicit val technicalError: technicalError = mockTechnicalError
      override implicit val ec: ExecutionContext = executionContext

      override def authConnector: AuthConnector = mockAuthConnector
    }

    val controller = new IP2016Controller(
      mockSessionCacheService,
      mockMCC,
      authFunction,
      mockPensionsTaken,
      mockPensionsTakenBefore,
      mockPensionsWorthBefore,
      mockPensionsTakenBetween,
      mockPensionsUsedBetween,
      mockOverseasPensions,
      mockCurrentPensions,
      mockPsoDetails,
      mockRemovePsoDetails,
      mockPensionDebits,
      mockWithdrawnAp2016View
    )
  }

  override def beforeEach(): Unit = {
    reset(mockSessionCacheService, mockAuthConnector, mockPlaConnector)
  }

  implicit val hc: HeaderCarrier = HeaderCarrier()

  //    lazy val TestIP2016Controller = fakeApplication().injector.instanceOf[IP2016Controller]
  object TestIP2016Controller extends IP2016Controller(mockSessionCacheService, mockMCC, mockAuthFunction, mockPensionsTaken, mockPensionsTakenBefore, mockPensionsWorthBefore, mockPensionsTakenBetween, mockPensionsUsedBetween, mockOverseasPensions, mockCurrentPensions, mockPsoDetails, mockRemovePsoDetails, mockPensionDebits, mockWithdrawnAp2016View) {
    lazy val authConnector: PlayAuthConnector = mockAuthConnector
  }

  val sessionId: String = UUID.randomUUID.toString
  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  val mockUsername = "mockuser"
  val mockUserId: String = "/auth/oid/" + mockUsername

  def cacheFetchCondition[T](data: Option[T]): Unit = {
    when(mockSessionCacheService.fetchAndGetFormData[T](anyString())(any(), any()))
      .thenReturn(Future.successful(data))
  }


  def psoDetailsCacheSetup(data: Option[PSODetailsModel]): OngoingStubbing[Future[Option[PSODetailsModel]]] = {
    when(mockSessionCacheService.fetchAndGetFormData[PSODetailsModel](ArgumentMatchers.eq(s"psoDetails"))(any(), any()))
      .thenReturn(Future.successful(data))
  }

  def pensionsDebitsSaveData(data: Option[PensionDebitsModel]): OngoingStubbing[Future[CacheMap]] = {
    when(mockSessionCacheService.saveFormData(anyString(), any())(any(), any()))
      .thenReturn(Future(CacheMap("tstId", Map.empty[String, JsValue])))
  }

  ///////////////////////////////////////////////
  // Pensions Taken
  ///////////////////////////////////////////////
  "In IP2016Controller calling the .pensionsTaken action" when {

    "applyFor2016IPAndFpShutterEnabled is disabled and not supplied with a stored model " should {
      "return 200" in new Setup {
        when(mockAppConfig.applyFor2016IpAndFpShutterEnabled).thenReturn(false)
        lazy val result: Future[Result] = controller.pensionsTaken(fakeRequest)
        mockAuthConnector(Future.successful({}))

        cacheFetchCondition[PensionsTakenModel](None)
        status(result) shouldBe 200
        contentAsString(result) should include ("Before 6 April 2016, did you turn 75, take money from your pensions, or transfer to an overseas pension?")

      }
    }

    "applyFor2016IPAndFpShutterEnabled is enabled and not supplied with a stored model " should {
      "return 200 and withdrawnAP2016 view" in new Setup {
        when(mockAppConfig.applyFor2016IpAndFpShutterEnabled).thenReturn(true)
        lazy val result: Future[Result] = controller.pensionsTaken(fakeRequest)
        mockAuthConnector(Future.successful({}))

        cacheFetchCondition[PensionsTakenModel](None)
        status(result) shouldBe 200
        contentAsString(result) should include("Sorry, applications for 2016 protection have ended")

      }
    }

    "supplied with a stored test model" should {
      "return 200" in new Setup {
        mockAuthConnector(Future.successful({}))
        val testModel = new PensionsTakenModel(Some("yes"))
        lazy val result: Future[Result] = controller.pensionsTaken(fakeRequest)

        mockAuthConnector(Future.successful({}))
        cacheFetchCondition[PensionsTakenModel](Some(testModel))
        status(result) shouldBe 200
      }

      "return some HTML that" should {
        "contain some text and use the character set utf-8" in new Setup {
          mockAuthConnector(Future.successful({}))
          val testModel = new PensionsTakenModel(Some("yes"))
          lazy val result: Future[Result] = controller.pensionsTaken(fakeRequest)

          cacheFetchCondition[PensionsTakenModel](Some(testModel))
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }
      }
    }
  }

  "Submitting Pensions Taken data" when {

    "Submitting 'yes' in pensionsTakenForm" should {
      "redirect to pensions taken before" in new Setup {

        object DataItem extends AuthorisedFakeRequestToPost(controller.submitPensionsTaken, ("pensionsTaken", "yes"))

        mockAuthConnector(Future.successful({}))
        cacheSaveCondition[PensionsTakenModel](mockSessionCacheService)
        status(DataItem.result) shouldBe 303
        redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2016Controller.pensionsTakenBefore}")
      }
    }

    "Submitting 'no' in pensionsTakenForm" should {
      "redirect to overseas pensions" in new Setup {

        object DataItem extends AuthorisedFakeRequestToPost(controller.submitPensionsTaken, ("pensionsTaken", "no"))

        mockAuthConnector(Future.successful({}))
        cacheSaveCondition[PensionsTakenModel](mockSessionCacheService)
        status(DataItem.result) shouldBe 303
        redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2016Controller.overseasPensions}")
      }
    }

    "Submitting pensionsTakenForm with no data" should {
      "return 400" in new Setup {

        object DataItem extends AuthorisedFakeRequestToPost(controller.submitPensionsTaken, ("pensionsTaken", ""))

        mockAuthConnector(Future.successful({}))
        status(DataItem.result) shouldBe 400
      }
    }
  }


  ///////////////////////////////////////////////
  // Pensions Taken Before
  ///////////////////////////////////////////////
  "In IP2016Controller calling the .pensionsTakenBefore action" when {

    "applyFor2016IPAndFpShutterEnabled is disabled and controller not supplied with a stored model" should {
      "return 200" in new Setup {
        when(mockAppConfig.applyFor2016IpAndFpShutterEnabled).thenReturn(false)
        lazy val result: Future[Result] = controller.pensionsTakenBefore(fakeRequest)
        mockAuthConnector(Future.successful({}))

        cacheFetchCondition[PensionsTakenBeforeModel](None)
        status(result) shouldBe 200
        contentAsString(result) should include ("Did you get an income from any of your pensions before 6 April 2006?")
      }

      "take the user to the pensions taken before page" in {
        mockAuthConnector(Future.successful({}))
        cacheFetchCondition[PensionsTakenBeforeModel](None)
      }
    }

    "applyFor2016IPAndFpShutterEnabled is enabled and controller not supplied with a stored model" should {
      "return 200 and withdrawnAP2016 view" in new Setup {
        when(mockAppConfig.applyFor2016IpAndFpShutterEnabled).thenReturn(true)
        lazy val result: Future[Result] = controller.pensionsTakenBefore(fakeRequest)
        mockAuthConnector(Future.successful({}))

        cacheFetchCondition[PensionsTakenBeforeModel](None)
        status(result) shouldBe 200
        contentAsString(result) should include("Sorry, applications for 2016 protection have ended")
      }

      "take the user to the pensions taken before page" in {
        mockAuthConnector(Future.successful({}))
        cacheFetchCondition[PensionsTakenBeforeModel](None)
      }
    }

    "supplied with a stored test model" should {
      "return 200" in new Setup {
        val testModel = new PensionsTakenBeforeModel("yes")
        lazy val result: Future[Result] = controller.pensionsTakenBefore(fakeRequest)

        mockAuthConnector(Future.successful({}))
        cacheFetchCondition[PensionsTakenBeforeModel](Some(testModel))
        status(result) shouldBe 200
      }

      "take the user to the pensions taken before page" in {
        mockAuthConnector(Future.successful({}))
        cacheFetchCondition[PensionsTakenBeforeModel](None)
      }

      "return some HTML that" should {

        "use the character set utf-8" in new Setup {
          val testModel = new PensionsTakenBeforeModel("yes")
          lazy val result: Future[Result] = controller.pensionsTakenBefore(fakeRequest)

          mockAuthConnector(Future.successful({}))
          cacheFetchCondition[PensionsTakenBeforeModel](Some(testModel))
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }

        "have the radio option `yes` selected by default" in new Setup {
          val testModel = new PensionsTakenBeforeModel("yes")
          mockAuthConnector(Future.successful({}))
          cacheFetchCondition[PensionsTakenBeforeModel](Some(testModel))
        }
      }
    }

  }

  "Submitting Pensions Taken Before data" when {

    "Submitting 'yes' in pensionsTakenBeforeForm" when {

      "valid data is submitted" should {
        "redirect to pensions worth before" in new Setup {

          object DataItem extends AuthorisedFakeRequestToPost(controller.submitPensionsTakenBefore, ("pensionsTakenBefore", "yes"))

          mockAuthConnector(Future.successful({}))

          cacheSaveCondition[PensionsTakenModel](mockSessionCacheService)
          status(DataItem.result) shouldBe 303
          redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2016Controller.pensionsWorthBefore}")
        }
      }

      "invalid data is submitted" should {
        "return 400" in new Setup {

          object DataItem extends AuthorisedFakeRequestToPost(controller.submitPensionsTakenBefore, ("pensionsTakenBefore", ""))

          mockAuthConnector(Future.successful({}))

          status(DataItem.result) shouldBe 400
        }
      }
    }

    "Submitting 'no' in pensionsTakenBeforeForm" when {

      "valid data is submitted" should {
        "redirect to pensions taken between" in new Setup {

          object DataItem extends AuthorisedFakeRequestToPost(controller.submitPensionsTakenBefore, ("pensionsTakenBefore", "no"))

          mockAuthConnector(Future.successful({}))

          cacheSaveCondition[PensionsTakenModel](mockSessionCacheService)
          status(DataItem.result) shouldBe 303
          redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2016Controller.pensionsTakenBetween}")
        }
      }
    }
  }

  ///////////////////////////////////////////////
  // Pensions Worth Before
  ///////////////////////////////////////////////
  "In IP2016Controller calling the .pensionsWorthBefore action" when {

    "applyFor2016IPAndFpShutterEnabled is disabled and not supplied with a stored model" should {
      "return 200" in new Setup {
        when(mockAppConfig.applyFor2016IpAndFpShutterEnabled).thenReturn(false)
        lazy val result: Future[Result] = controller.pensionsWorthBefore(fakeRequest)
        mockAuthConnector(Future.successful({}))

        cacheFetchCondition[PensionsWorthBeforeModel](None)
        status(result) shouldBe 200
        contentAsString(result) should include ("What were these pensions worth on 5 April 2016?")
      }

      "take the user to the pensions worth before page" in {
        mockAuthConnector(Future.successful({}))
        cacheFetchCondition[PensionsWorthBeforeModel](None)
      }
    }

    "applyFor2016IPAndFpShutterEnabled is enabled and not supplied with a stored model" should {
      "return 200 and withdrawnAP2016 view" in new Setup {
        when(mockAppConfig.applyFor2016IpAndFpShutterEnabled).thenReturn(true)
        lazy val result: Future[Result] = controller.pensionsWorthBefore(fakeRequest)
        mockAuthConnector(Future.successful({}))

        cacheFetchCondition[PensionsWorthBeforeModel](None)
        status(result) shouldBe 200
        contentAsString(result) should include("Sorry, applications for 2016 protection have ended")
      }

      "take the user to the pensions worth before page" in {
        mockAuthConnector(Future.successful({}))
        cacheFetchCondition[PensionsWorthBeforeModel](None)
      }
    }

    "supplied with a stored test model" should {
      "return 200" in new Setup {
        val testModel = new PensionsWorthBeforeModel(Some(1))
        lazy val result: Future[Result] = controller.pensionsWorthBefore(fakeRequest)

        mockAuthConnector(Future.successful({}))
        cacheFetchCondition[PensionsWorthBeforeModel](Some(testModel))
        status(result) shouldBe 200
      }

      "take the user to the pensions worth before page" in {
        mockAuthConnector(Future.successful({}))
        cacheFetchCondition[PensionsWorthBeforeModel](None)
      }

      "return some HTML that" should {

        "contain some text and use the character set utf-8" in new Setup {
          val testModel = new PensionsWorthBeforeModel(Some(1))
          lazy val result: Future[Result] = controller.pensionsWorthBefore(fakeRequest)

          mockAuthConnector(Future.successful({}))
          cacheFetchCondition[PensionsWorthBeforeModel](Some(testModel))
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }

        "have the amount £1 completed by default" in new Setup {
          val testModel = new PensionsWorthBeforeModel(Some(1))
          mockAuthConnector(Future.successful({}))
          cacheFetchCondition[PensionsWorthBeforeModel](Some(testModel))
        }
      }
    }
  }

  "Submitting Pensions Worth Before data" when {

    "valid data is submitted" should {
      "redirect to pensions taken between" in new Setup {

        object DataItem extends AuthorisedFakeRequestToPost(controller.submitPensionsWorthBefore, ("pensionsWorthBeforeAmt", "1"))

        mockAuthConnector(Future.successful({}))

        cacheSaveCondition[PensionsTakenModel](mockSessionCacheService)
        status(DataItem.result) shouldBe 303
        redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2016Controller.pensionsTakenBetween}")
      }
    }

    "no data is submitted" should {
      "return 400" in new Setup {

        object DataItem extends AuthorisedFakeRequestToPost(controller.submitPensionsWorthBefore, ("pensionsWorthBeforeAmt", ""))

        mockAuthConnector(Future.successful({}))

        status(DataItem.result) shouldBe 400
      }
    }

    "invalid data is submitted" should {
      "return 400" in new Setup {

        object DataItem extends AuthorisedFakeRequestToPost(controller.submitPensionsWorthBefore, ("pensionsWorthBeforeAmt", "-100"))

        mockAuthConnector(Future.successful({}))
        status(DataItem.result) shouldBe 400
      }
    }
  }

  ///////////////////////////////////////////////
  // Pensions Taken Between
  ///////////////////////////////////////////////
  "In IP2016Controller calling the .pensionsTakenBetween action" when {

    "applyFor2016IPAndFpShutterEnabled is disabled and not supplied with a stored model" should {
      "return 200" in new Setup {
        when(mockAppConfig.applyFor2016IpAndFpShutterEnabled).thenReturn(false)
        lazy val result: Future[Result] = controller.pensionsTakenBetween(fakeRequest)
        mockAuthConnector(Future.successful({}))
        cacheFetchCondition[PensionsTakenBetweenModel](None)
        status(result) shouldBe 200
        contentAsString(result) should include ("Between 6 April 2006 and 5 April 2016, did you get money from your pensions, transfer a pension overseas, or turn 75 with money still in a pension?")

      }
    }

    "applyFor2016IPAndFpShutterEnabled is enabled and not supplied with a stored model" should {
      "return 200 and withdrawnAP2016 view" in new Setup {
        when(mockAppConfig.applyFor2016IpAndFpShutterEnabled).thenReturn(true)
        lazy val result: Future[Result] = controller.pensionsTakenBetween(fakeRequest)
        mockAuthConnector(Future.successful({}))
        cacheFetchCondition[PensionsTakenBetweenModel](None)
        status(result) shouldBe 200
        contentAsString(result) should include("Sorry, applications for 2016 protection have ended")

      }
    }

    "supplied with a stored test model" should {
      "return 200" in new Setup {
        val testModel = new PensionsTakenBetweenModel("yes")
        lazy val result: Future[Result] = controller.pensionsTakenBetween(fakeRequest)

        mockAuthConnector(Future.successful({}))
        cacheFetchCondition[PensionsTakenBetweenModel](Some(testModel))
        status(result) shouldBe 200
      }

      "return some HTML that" should {

        "contain some text and use the character set utf-8" in new Setup {
          val testModel = new PensionsTakenBetweenModel("yes")
          lazy val result: Future[Result] = controller.pensionsTakenBetween(fakeRequest)

          mockAuthConnector(Future.successful({}))
          cacheFetchCondition[PensionsTakenBetweenModel](Some(testModel))
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }
      }
    }
  }

  "Submitting Pensions Taken Between data" when {

    "Submitting 'yes' in pensionsTakenBetweenForm" when {

      "submitting valid data 'yes'" should {
        "redirect to pension used between" in new Setup {

          object DataItem extends AuthorisedFakeRequestToPost(controller.submitPensionsTakenBetween, ("pensionsTakenBetween", "yes"))

          mockAuthConnector(Future.successful({}))
          cacheSaveCondition[PensionsTakenModel](mockSessionCacheService)
          status(DataItem.result) shouldBe 303
          redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2016Controller.pensionsUsedBetween}")
        }
      }

      "submitting valid data 'no'" should {
        "redirect to overseas pensions" in new Setup {

          object DataItem extends AuthorisedFakeRequestToPost(controller.submitPensionsTakenBetween, ("pensionsTakenBetween", "no"))

          mockAuthConnector(Future.successful({}))
          cacheSaveCondition[PensionsTakenModel](mockSessionCacheService)
          status(DataItem.result) shouldBe 303
          redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2016Controller.overseasPensions}")
        }
      }

      "submitting invalid data" should {
        "return 400" in new Setup {

          object DataItem extends AuthorisedFakeRequestToPost(controller.submitPensionsTakenBetween, ("pensionsTakenBetween", ""))

          mockAuthConnector(Future.successful({}))
          status(DataItem.result) shouldBe 400
        }
      }
    }
  }

  ///////////////////////////////////////////////
  // Pensions Used Between
  ///////////////////////////////////////////////
  "In IP2016Controller calling the .pensionsUsedBetween action" when {

    "applyFor2016IPAndFpShutterEnabled is disabled and not supplied with a stored model" should {
      "return 200" in new Setup {
        when(mockAppConfig.applyFor2016IpAndFpShutterEnabled).thenReturn(false)
        lazy val result: Future[Result] = controller.pensionsUsedBetween(fakeRequest)
        mockAuthConnector(Future.successful({}))
        cacheFetchCondition[PensionsUsedBetweenModel](None)
        status(result) shouldBe 200
        contentAsString(result) should include ("How much lifetime allowance have you used?")
      }
    }

    "applyFor2016IPAndFpShutterEnabled is enabled and not supplied with a stored model" should {
      "return 200 and withdrawnAP2016 view" in new Setup {
        when(mockAppConfig.applyFor2016IpAndFpShutterEnabled).thenReturn(true)
        lazy val result: Future[Result] = controller.pensionsUsedBetween(fakeRequest)
        mockAuthConnector(Future.successful({}))
        cacheFetchCondition[PensionsUsedBetweenModel](None)
        status(result) shouldBe 200
        contentAsString(result) should include("Sorry, applications for 2016 protection have ended")
      }
    }

    "supplied with a stored test model" should {
      "return 200" in new Setup {
        val testModel = new PensionsUsedBetweenModel(Some(1))
        lazy val result: Future[Result] = controller.pensionsUsedBetween(fakeRequest)

        mockAuthConnector(Future.successful({}))
        cacheFetchCondition[PensionsUsedBetweenModel](Some(testModel))
        status(result) shouldBe 200
      }
    }
  }

  "Submitting Pensions Used Between data" when {

    "Submitting valid in pensionsUsedBetweenForm" when {

      "submitting valid data" should {
        "redirect to overseas pensions" in new Setup {

          object DataItem extends AuthorisedFakeRequestToPost(controller.submitPensionsUsedBetween, ("pensionsUsedBetweenAmt", "1"))

          mockAuthConnector(Future.successful({}))
          cacheSaveCondition[PensionsTakenModel](mockSessionCacheService)
          status(DataItem.result) shouldBe 303
          redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2016Controller.overseasPensions}")
        }
      }

      "submitting invalid data" should {
        "return 400" in new Setup {

          object DataItem extends AuthorisedFakeRequestToPost(controller.submitPensionsUsedBetween, ("pensionsTakenBetweenAmt", ""))

          mockAuthConnector(Future.successful({}))
          status(DataItem.result) shouldBe 400
        }
      }

    }
  }

  ///////////////////////////////////////////////
  // Overseas Pensions
  ///////////////////////////////////////////////

  "In IP2016Controller calling the .overseasPensions action" when {

    "applyFor2016IPAndFpShutterEnabled is disabled and not supplied with a stored model" should {
      "return 200" in new Setup {
        when(mockAppConfig.applyFor2016IpAndFpShutterEnabled).thenReturn(false)
        lazy val result: Future[Result] = controller.overseasPensions(fakeRequest)
        mockAuthConnector(Future.successful({}))
        cacheFetchCondition[OverseasPensionsModel](None)
        status(result) shouldBe 200
        contentAsString(result) should include ("Have you put money into a pension scheme held overseas?")
      }
    }

    "applyFor2016IPAndFpShutterEnabled is enabled and not supplied with a stored model" should {
      "return 200 and withdrawnAP2016 view" in new Setup {
        when(mockAppConfig.applyFor2016IpAndFpShutterEnabled).thenReturn(true)
        lazy val result: Future[Result] = controller.overseasPensions(fakeRequest)
        mockAuthConnector(Future.successful({}))
        cacheFetchCondition[OverseasPensionsModel](None)
        status(result) shouldBe 200
        contentAsString(result) should include("Sorry, applications for 2016 protection have ended")
      }
    }

    "supplied with a stored test model (yes, £100000)" should {
      "return 200" in new Setup {
        val overseasPensionAmt: BigDecimal = 100000
        val testModel = new OverseasPensionsModel("yes", Some(overseasPensionAmt))
        lazy val result: Future[Result] = controller.overseasPensions(fakeRequest)

        mockAuthConnector(Future.successful({}))
        cacheFetchCondition[OverseasPensionsModel](Some(testModel))
        status(result) shouldBe 200
      }

      "return some HTML that" should {

        "contain some text and use the character set utf-8" in new Setup {
          val overseasPensionAmt: BigDecimal = 100000
          val testModel = new OverseasPensionsModel("yes", Some(overseasPensionAmt))
          lazy val result: Future[Result] = controller.overseasPensions(fakeRequest)

          mockAuthConnector(Future.successful({}))
          cacheFetchCondition[OverseasPensionsModel](Some(testModel))
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }
      }
    }
  }

  "Submitting Overseas Pensions data" when {


    "Submitting valid data" should {
      "redirect to Current Pensions" in new Setup {

        object DataItem extends AuthorisedFakeRequestToPost(controller.submitOverseasPensions, ("overseasPensions", "no"), ("overseasPensionsAmt", ""))

        mockAuthConnector(Future.successful({}))
        cacheSaveCondition[PensionsTakenModel](mockSessionCacheService)
        status(DataItem.result) shouldBe 303
        redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2016Controller.currentPensions}")
      }
    }
    "Submitting invalid data" should {

      "return 400" in new Setup {

        object DataItem extends AuthorisedFakeRequestToPost(controller.submitOverseasPensions, ("overseasPensions", ""), ("overseasPensionsAmt", ""))

        mockAuthConnector(Future.successful({}))
        status(DataItem.result) shouldBe 400
      }
    }

    "Submitting invalid data that fails additional validation" should {
      "return 400" in new Setup {

        object DataItem extends AuthorisedFakeRequestToPost(controller.submitOverseasPensions, ("overseasPensions", "yes"), ("overseasPensionsAmt", ""))

        mockAuthConnector(Future.successful({}))
        status(DataItem.result) shouldBe 400
      }
    }
  }


  ///////////////////////////////////////////////
  // CURRENT PENSIONS
  ///////////////////////////////////////////////
  "In IP2016Controller calling the .currentPensions action" when {

    "applyFor2016IPAndFpShutterEnabled is disabled and not supplied with a stored model" should {
      "return 200" in new Setup {
        when(mockAppConfig.applyFor2016IpAndFpShutterEnabled).thenReturn(false)
        lazy val result: Future[Result] = controller.currentPensions(fakeRequest)
        mockAuthConnector(Future.successful({}))
        cacheFetchCondition[CurrentPensionsModel](None)
        status(result) shouldBe 200
        contentAsString(result) should include ("What were your UK pensions worth on 5 April 2016?")
      }
    }

    "applyFor2016IPAndFpShutterEnabled is enabled and not supplied with a stored model" should {
      "return 200 and withdrawnAP2016 view" in new Setup {
        when(mockAppConfig.applyFor2016IpAndFpShutterEnabled).thenReturn(true)
        lazy val result: Future[Result] = controller.currentPensions(fakeRequest)
        mockAuthConnector(Future.successful({}))
        cacheFetchCondition[CurrentPensionsModel](None)
        status(result) shouldBe 200
        contentAsString(result) should include("Sorry, applications for 2016 protection have ended")
      }
    }

    "supplied with a stored test model (£100000)" should {
      "return 200" in new Setup {
        val overseasPensionAmt: BigDecimal = 100000
        val testModel = new CurrentPensionsModel(Some(overseasPensionAmt))
        lazy val result: Future[Result] = controller.currentPensions(fakeRequest)

        mockAuthConnector(Future.successful({}))
        cacheFetchCondition[CurrentPensionsModel](Some(testModel))
        status(result) shouldBe 200
      }


      "return some HTML that" should {

        "contain some text and use the character set utf-8" in new Setup {
          val overseasPensionAmt: BigDecimal = 100000
          val testModel = new CurrentPensionsModel(Some(overseasPensionAmt))
          lazy val result: Future[Result] = controller.currentPensions(fakeRequest)

          mockAuthConnector(Future.successful({}))
          cacheFetchCondition[CurrentPensionsModel](Some(testModel))
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }

      }
    }
  }

  "Submitting Current Pensions data" when {

    "valid data is submitted" should {
      "redirect to Pension Debits page" in new Setup {

        object DataItem extends AuthorisedFakeRequestToPost(controller.submitCurrentPensions, ("currentPensionsAmt", "100000"))

        mockAuthConnector(Future.successful({}))
        cacheSaveCondition[PensionsTakenModel](mockSessionCacheService)
        status(DataItem.result) shouldBe 303
        redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2016Controller.pensionDebits}")
      }
    }

    "invalid data is submitted" should {
      "return 400" in new Setup {

        object DataItem extends AuthorisedFakeRequestToPost(controller.submitCurrentPensions, ("currentPensionsAmt", ""))

        mockAuthConnector(Future.successful({}))
        status(DataItem.result) shouldBe 400
      }
    }
  }


  ///////////////////////////////////////////////
  // PENSION DEBITS
  ///////////////////////////////////////////////
  "In IP2016Controller calling the .pensionDebits action" when {

    "applyFor2016IPAndFpShutterEnabled is disabled and not supplied with a stored model" should {
      "return 200" in new Setup {
        when(mockAppConfig.applyFor2016IpAndFpShutterEnabled).thenReturn(false)
        lazy val result: Future[Result] = controller.pensionDebits(fakeRequest)
        mockAuthConnector(Future.successful({}))
        cacheFetchCondition[PensionDebitsModel](None)
        status(result) shouldBe 200
        contentAsString(result) should include ("Have any of your pensions been shared in a divorce since 5 April 2016?")
      }

    }

    "applyFor2016IPAndFpShutterEnabled is enabled and not supplied with a stored model" should {
      "return 200 and withdrawnAP2016 view" in new Setup {
        when(mockAppConfig.applyFor2016IpAndFpShutterEnabled).thenReturn(true)
        lazy val result: Future[Result] = controller.pensionDebits(fakeRequest)
        mockAuthConnector(Future.successful({}))
        cacheFetchCondition[PensionDebitsModel](None)
        status(result) shouldBe 200
        contentAsString(result) should include("Sorry, applications for 2016 protection have ended")
      }

    }

    "supplied with a stored test model" should {
      "return 200" in new Setup {
        val testModel = new PensionDebitsModel(Some("yes"))
        lazy val result: Future[Result] = controller.pensionDebits(fakeRequest)

        mockAuthConnector(Future.successful({}))
        cacheFetchCondition[PensionDebitsModel](Some(testModel))
        status(result) shouldBe 200
      }

      "return some HTML that" should {

        "contain some text and use the character set utf-8" in new Setup {
          val testModel = new PensionDebitsModel(Some("yes"))
          lazy val result: Future[Result] = controller.pensionDebits(fakeRequest)

          mockAuthConnector(Future.successful({}))
          cacheFetchCondition[PensionDebitsModel](Some(testModel))
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }
      }
    }
  }

  "Submitting Pensions Debits data" when {

    "Submitting 'yes' in pensionDebitsForm" should {
      "redirect to number of pension sharing orders" in new Setup {

        object DataItem extends AuthorisedFakeRequestToPost(controller.submitPensionDebits, ("pensionDebits", "yes"))

        mockAuthConnector(Future.successful({}))
        cacheSaveCondition[PensionDebitsModel](mockSessionCacheService)
        status(DataItem.result) shouldBe 303
        redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2016Controller.psoDetails}")
      }
    }

    "Submitting 'no' in pensionDebitsForm" should {
      "redirect to summary" in new Setup {

        object DataItem extends AuthorisedFakeRequestToPost(controller.submitPensionDebits, ("pensionDebits", "no"))

        mockAuthConnector(Future.successful({}))
        cacheSaveCondition[PensionDebitsModel](mockSessionCacheService)
        status(DataItem.result) shouldBe 303
        redirectLocation(DataItem.result) shouldBe Some(s"${routes.SummaryController.summaryIP16}")
      }
    }

    "Submitting pensionDebitsForm with no data" should {
      "return 400" in new Setup {

        object DataItem extends AuthorisedFakeRequestToPost(controller.submitPensionDebits, ("pensionDebits", ""))

        mockAuthConnector(Future.successful({}))
        status(DataItem.result) shouldBe 400
      }
    }
  }

  ///////////////////////////////////////////////
  // PENSION DETAILS
  ///////////////////////////////////////////////

  "In IP2016Controller calling the .psoDetails action" when {
    "applyFor2016IPAndFpShutterEnabled is disabled and not supplied with a stored model" should {
      "return 200" in new Setup {
        when(mockAppConfig.applyFor2016IpAndFpShutterEnabled).thenReturn(false)
        lazy val result: Future[Result] = controller.psoDetails(fakeRequest)

        mockAuthConnector(Future.successful({}))
        cacheFetchCondition[PSODetailsModel](None)
        status(result) shouldBe 200
        contentAsString(result) should include ("Pension sharing order")
      }
    }

    "applyFor2016IPAndFpShutterEnabled is enabled and not supplied with a stored model" should {
      "return 200 and withdrawnAP2016 view" in new Setup {
        when(mockAppConfig.applyFor2016IpAndFpShutterEnabled).thenReturn(true)
        lazy val result: Future[Result] = controller.psoDetails(fakeRequest)

        mockAuthConnector(Future.successful({}))
        cacheFetchCondition[PSODetailsModel](None)
        status(result) shouldBe 200
        contentAsString(result) should include("Sorry, applications for 2016 protection have ended")
      }
    }

    "supplied with a stored test model" should {
      "return 200" in new Setup {
        val year = 2016
        val month = 8
        val day = 1
        val psoAmt: BigDecimal = 1234
        val testModel: PSODetailsModel = PSODetailsModel(LocalDate.of(year, month, day), Some(psoAmt))
        lazy val result: Future[Result] = controller.psoDetails(fakeRequest)

        mockAuthConnector(Future.successful({}))
        cacheFetchCondition[PSODetailsModel](Some(testModel))
        status(result) shouldBe 200
      }


      "return some HTML that" should {
        "contain some text and use the character set utf-8" in new Setup {
          val year: Int = 2016
          val month: Int = 8
          val day: Int = 1
          val psoAmt: BigDecimal = 1234
          val testModel: PSODetailsModel = PSODetailsModel(LocalDate.of(year, month, day), Some(psoAmt))
          lazy val result: Future[Result] = controller.psoDetails(fakeRequest)

          mockAuthConnector(Future.successful({}))
          cacheFetchCondition[PSODetailsModel](Some(testModel))
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }
      }
    }

  }

  "Submitting valid PSO details data" when {

    "submitting valid PSO details" should {
      "redirect to the summary page with a valid PSO" in new Setup {

        object DataItem extends AuthorisedFakeRequestToPost(controller.submitPSODetails,

          ("pso.day", "6"),
          ("pso.month", "4"),
          ("pso.year", "2016"),
          ("psoAmt", "100000")
        )

        mockAuthConnector(Future.successful({}))
        cacheSaveCondition[PensionsTakenModel](mockSessionCacheService)
        status(DataItem.result) shouldBe 303
        redirectLocation(DataItem.result) shouldBe Some(s"${routes.SummaryController.summaryIP16}")
      }
    }

    "submitting an invalid set of PSO details" should {
      "return 400" in new Setup {

        object DataItem extends AuthorisedFakeRequestToPost(controller.submitPSODetails,

          ("pso.day", ""),
          ("pso.month", "1"),
          ("pso.year", "2015"),
          ("psoAmt", "100000")
        )

        mockAuthConnector(Future.successful({}))
        status(DataItem.result) shouldBe 400
      }

    }

    "submitting an invalid set of PSO details that fails additional validation" should {
      "return 400" in new Setup {

        object DataItem extends AuthorisedFakeRequestToPost(controller.submitPSODetails,
          ("pso.day", "35"),
          ("pso.month", "1"),
          ("pso.year", "2015"),
          ("psoAmt", "100000")
        )

        mockAuthConnector(Future.successful({}))
        status(DataItem.result) shouldBe 400
      }
    }
  }

  "In IP2016Controller calling the .removePsoDetails action" when {

    "applyFor2016IPAndFpShutterEnabled is disabled and supplied with a stored model" should {
      "return 200" in new Setup {
        when(mockAppConfig.applyFor2016IpAndFpShutterEnabled).thenReturn(false)
        lazy val result: Future[Result] = controller.removePsoDetails(fakeRequest)

        mockAuthConnector(Future.successful({}))
        status(result) shouldBe 200
        contentAsString(result) should not include ("Sorry, applications for 2016 protection have ended")
      }
    }

    "applyFor2016IPAndFpShutterEnabled is enabled and supplied with a stored model" should {
      "return 200 and withdrawnAP2016 view" in new Setup {
        when(mockAppConfig.applyFor2016IpAndFpShutterEnabled).thenReturn(true)
        lazy val result: Future[Result] = controller.removePsoDetails(fakeRequest)

        mockAuthConnector(Future.successful({}))
        status(result) shouldBe 200
        contentAsString(result) should include("Sorry, applications for 2016 protection have ended")
      }
    }
  }

  "Submitting a pso for removal from application" when {

    "not supplied with a stored model" should {
      "return 303" in new Setup {
        lazy val result: Future[Result] = controller.submitRemovePsoDetails(fakeRequest)
        val testModel = new PensionDebitsModel(Some("yes"))

        mockAuthConnector(Future.successful({}))
        pensionsDebitsSaveData(Some(testModel))
        status(result) shouldBe 303
      }
    }
    "not supplied with a stored model" should {
      "redirect location should be the summary page" in new Setup {
        lazy val result: Future[Result] = controller.submitRemovePsoDetails(fakeRequest)
        val testModel = new PensionDebitsModel(Some("yes"))

        mockAuthConnector(Future.successful({}))
        pensionsDebitsSaveData(Some(testModel))
        redirectLocation(result) shouldBe Some(s"${routes.SummaryController.summaryIP16}")
      }
    }
  }
}
