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
import config._
import config.wiring.PlaFormPartialRetriever
import connectors.{KeyStoreConnector, PLAConnector}
import constructors.{DisplayConstructors, ResponseConstructors}
import enums.{ApplicationOutcome, ApplicationType}
import mocks.AuthMock
import models._
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfter, BeforeAndAfterEach}
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Lang
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, Configuration, Environment}
import testHelpers.FakeApplication
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import utils.ActionWithSessionId
import views.html.pages.fallback.{noNotificationId, technicalError}
import views.html.pages.result.{manualCorrespondenceNeeded, resultRejected, resultSuccess, resultSuccessInactive}

import scala.concurrent.{ExecutionContext, Future}

class ResultControllerSpec extends FakeApplication with MockitoSugar
  with BeforeAndAfter with AuthMock with BeforeAndAfterEach {

  val mockDisplayConstructors: DisplayConstructors = mock[DisplayConstructors]
  val mockResponseConstructors: ResponseConstructors = mock[ResponseConstructors]
  val mockKeyStoreConnector: KeyStoreConnector = mock[KeyStoreConnector]
  val mockPlaConnector: PLAConnector = mock[PLAConnector]
  val mockMCC: MessagesControllerComponents = fakeApplication().injector.instanceOf[MessagesControllerComponents]
  val mockActionWithSessionId: ActionWithSessionId = mock[ActionWithSessionId]
  val mockHttp: DefaultHttpClient = mock[DefaultHttpClient]
  val mockEnv: Environment = mock[Environment]

  implicit val executionContext: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  implicit val mockPartialRetriever: PlaFormPartialRetriever = mock[PlaFormPartialRetriever]
  implicit val mockAppConfig: FrontendAppConfig = fakeApplication().injector.instanceOf[FrontendAppConfig]
  implicit val mockPlaContext: PlaContext = mock[PlaContext]
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = mock[Materializer]
  implicit val mockLang: Lang = mock[Lang]
  implicit val fakeRequest = FakeRequest()
  implicit val application = mock[Application]
  implicit val mockTechnicalError: technicalError = app.injector.instanceOf[technicalError]
  implicit val mockManualCorrespondenceNeeded: manualCorrespondenceNeeded = app.injector.instanceOf[manualCorrespondenceNeeded]
  implicit val mockNoNotificationID: noNotificationId = app.injector.instanceOf[noNotificationId]
  implicit val mockResultRejected: resultRejected = app.injector.instanceOf[resultRejected]
  implicit val mockResultSuccess: resultSuccess = app.injector.instanceOf[resultSuccess]
  implicit val mockResultSuccessInactive: resultSuccessInactive = app.injector.instanceOf[resultSuccessInactive]




  val authFunction = new AuthFunction {
    override implicit val partialRetriever: PlaFormPartialRetriever = mockPartialRetriever
    override implicit val plaContext: PlaContext = mockPlaContext
    override implicit val appConfig: FrontendAppConfig = mockAppConfig
    override implicit val technicalError: technicalError = mockTechnicalError
    override implicit val ec: ExecutionContext = executionContext

    override def authConnector: AuthConnector = mockAuthConnector
    override def config: Configuration = mockAppConfig.configuration
    override def env: Environment = mockEnv
  }


  override def beforeEach(): Unit = {
    reset(mockDisplayConstructors,
      mockResponseConstructors,
      mockPlaConnector,
      mockActionWithSessionId,
      mockHttp,
      mockEnv,
      mockPlaContext,
      mockKeyStoreConnector
    )
    super.beforeEach()
  }

  val successFP16Json = Json.parse("""{"certificateDate":"2016-05-10T17:20:55.138","nino":"AA123456A","notificationId":24,"protectionID":8243168284792526522,"protectionReference":"FP16138722390C","protectionType":"FP2016","status":"Open","version":1}""")
  val rejectionFP16Json = Json.parse("""{"nino":"AA123456A","notificationId":21,"protectionID":-4645895724767334826,"protectionType":"FP2016","status":"Rejected","version":1}""")

  val successIP16Json = Json.parse("""{"notificationId":12,"protectedAmount":1230000.0}""")
  val inactiveSuccessIP16Json = Json.parse("""{"notificationId":43,"protectedAmount":1600000.0}""")
  val rejectionIP16Json = Json.parse("""{"notificationId":9}""")

  val successIP14Json = Json.parse("""{"notificationId":3,"protectedAmount":1230000.0}""")
  val inactiveSuccessIP14Json = Json.parse("""{"notificationId":32,"protectedAmount":1400000.0}""")
  val rejectionIP14Json = Json.parse("""{"notificationId":1}""")

  val testFP16SuccessResponse = HttpResponse(status = 200, json = successFP16Json, headers = Map.empty)
  val testFP16RejectionResponse = HttpResponse(status = 409, json = rejectionFP16Json, headers = Map.empty)
  val testIP16SuccessResponse = HttpResponse(status = 200, json = successIP16Json, headers = Map.empty)
  val testIP16RejectionResponse = HttpResponse(status = 409, json = rejectionIP16Json, headers = Map.empty)
  val testIP14SuccessResponse = HttpResponse(status = 200, json = successIP14Json, headers = Map.empty)
  val testIP14RejectionResponse = HttpResponse(status = 409, json = rejectionIP14Json, headers = Map.empty)
  val testMCNeededResponse = HttpResponse(status = 423, body = "")

  val testIP16InactiveSuccessResponse = HttpResponse(status = 200, json = inactiveSuccessIP16Json, headers = Map.empty)
  val testIP14InactiveSuccessResponse = HttpResponse(status = 200, json = inactiveSuccessIP14Json, headers = Map.empty)

  val testFPSuccessProtectionModel = ProtectionModel(
    Some("testPSARef"),
    notificationId = Some(23),
    protectionID = Some(12345),
    protectionType = Some("FP2016"),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456"))

  val testFPRejectionProtectionModel = ProtectionModel(
    None,
    notificationId = Some(21),
    protectionID = Some(12345),
    protectionType = Some("FP2016"),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456"))

  val testIP16SuccessProtectionModel = ProtectionModel(
    Some("testPSARef"),
    notificationId = Some(12),
    protectionID = Some(12345),
    protectionType = Some("IP2016"),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456"))

  val testIP16InactiveSuccessProtectionModel = ProtectionModel(
    Some("testPSARef"),
    notificationId = Some(43),
    protectionID = Some(12345),
    protectionType = Some("IP2016"),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1600000),
    protectionReference = Some("PSA123456"))

  val testIP16RejectionProtectionModel = ProtectionModel(
    None,
    notificationId = Some(9),
    protectionID = Some(12345),
    protectionType = Some("IP2016"),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456"))

  val testIP14SuccessProtectionModel = ProtectionModel(
    Some("testPSARef"),
    notificationId = Some(3),
    protectionID = Some(12345),
    protectionType = Some("IP2014"),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456"))

  val testIP14InactiveSuccessProtectionModel = ProtectionModel(
    Some("testPSARef"),
    notificationId = Some(32),
    protectionID = Some(12345),
    protectionType = Some("IP2014"),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1400000),
    protectionReference = Some("PSA123456"))

  val testIP14RejectionProtectionModel = ProtectionModel(
    None,
    notificationId = Some(1),
    protectionID = Some(12345),
    protectionType = Some("IP2014"),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456"))

  val testFPSuccessApplyResponseModel = ApplyResponseModel(testFPSuccessProtectionModel)
  val testFPRejectionApplyResponseModel = ApplyResponseModel(testFPRejectionProtectionModel)
  val testIP16SuccessApplyResponseModel = ApplyResponseModel(testIP16SuccessProtectionModel)
  val testIP16InactiveSuccessApplyResponseModel = ApplyResponseModel(testIP16InactiveSuccessProtectionModel)
  val testIP16RejectionApplyResponseModel = ApplyResponseModel(testIP16RejectionProtectionModel)
  val testIP14SuccessApplyResponseModel = ApplyResponseModel(testIP14SuccessProtectionModel)
  val testIP14InactiveSuccessApplyResponseModel = ApplyResponseModel(testIP14InactiveSuccessProtectionModel)
  val testIP14RejectionApplyResponseModel = ApplyResponseModel(testIP14RejectionProtectionModel)

  val TestSuccessResultController = new ResultController(mockKeyStoreConnector, mockPlaConnector, mockDisplayConstructors, mockMCC, mockResponseConstructors, authFunction, mockTechnicalError, mockManualCorrespondenceNeeded, mockNoNotificationID, mockResultRejected, mockResultSuccess, mockResultSuccessInactive)
  val TestInactiveSuccessResultController = new ResultController(mockKeyStoreConnector, mockPlaConnector, mockDisplayConstructors, mockMCC, mockResponseConstructors, authFunction, mockTechnicalError, mockManualCorrespondenceNeeded, mockNoNotificationID, mockResultRejected, mockResultSuccess, mockResultSuccessInactive)
  val TestRejectResultController = new ResultController(mockKeyStoreConnector, mockPlaConnector, mockDisplayConstructors, mockMCC, mockResponseConstructors, authFunction, mockTechnicalError, mockManualCorrespondenceNeeded, mockNoNotificationID, mockResultRejected, mockResultSuccess, mockResultSuccessInactive)
  val TestMCNeededResultController = new ResultController(mockKeyStoreConnector, mockPlaConnector, mockDisplayConstructors, mockMCC, mockResponseConstructors, authFunction, mockTechnicalError, mockManualCorrespondenceNeeded, mockNoNotificationID, mockResultRejected, mockResultSuccess, mockResultSuccessInactive)
  val testResultController = new ResultController(mockKeyStoreConnector, mockPlaConnector, mockDisplayConstructors, mockMCC, mockResponseConstructors, authFunction, mockTechnicalError, mockManualCorrespondenceNeeded, mockNoNotificationID, mockResultRejected, mockResultSuccess, mockResultSuccessInactive)
  val TestIncorrectResponseModelResultController = new ResultController(mockKeyStoreConnector, mockPlaConnector, mockDisplayConstructors, mockMCC, mockResponseConstructors, authFunction, mockTechnicalError, mockManualCorrespondenceNeeded, mockNoNotificationID, mockResultRejected, mockResultSuccess, mockResultSuccessInactive)

  //////////////////////////////////////////////
  //  POST / REDIRECT
  /////////////////////////////////////////////


  "Successfully applying for FP" should {
    "return 303" in {

      when(mockPlaConnector.applyFP16(anyString())(any(), ArgumentMatchers.any())).thenReturn(Future.successful(testFP16RejectionResponse))
      when(mockPlaConnector.applyIP16(anyString(), any())(any(), ArgumentMatchers.any())).thenReturn(Future.successful(testIP16RejectionResponse))
      when(mockPlaConnector.applyIP14(anyString(), any())(any(), ArgumentMatchers.any())).thenReturn(Future.successful(testIP14RejectionResponse))

      when(mockKeyStoreConnector.fetchAllUserData(any())).thenReturn(Future.successful(Some(CacheMap("tstID", Map.empty[String, JsValue]))))
      when(mockKeyStoreConnector.saveData[ApplyResponseModel](anyString(), any())(any(), any())).thenReturn(Future.successful(CacheMap("tstId", Map.empty[String, JsValue])))
      when(mockKeyStoreConnector.fetchAndGetFormData[ApplyResponseModel](ArgumentMatchers.matches("applyResponseModel"))(any(), any())).thenReturn(Future.successful(Some(testIP14RejectionApplyResponseModel)))


      when(mockResponseConstructors.createApplyResponseModelFromJson(any())).thenReturn(Some(testFPRejectionApplyResponseModel))
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

      val DataItemResult = TestSuccessResultController.processFPApplication(fakeRequest)

      status(DataItemResult) shouldBe 303
      redirectLocation(DataItemResult) shouldBe Some(s"${routes.ResultController.displayFP16}")
    }
    "return 200" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[ApplyResponseModel](any())(any(), any()))
        .thenReturn(Future.successful(Some(testFPSuccessApplyResponseModel)))

      when(mockDisplayConstructors.createSuccessDisplayModel(any())(any(), any())).thenReturn(SuccessDisplayModel(
        ApplicationType.FP2016, "12313123", "50", true, None, Nil
      ))

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

      val GetItemResult = TestSuccessResultController.displayResult(ApplicationType.FP2016)(fakeRequest)
      status(GetItemResult) shouldBe 200
    }
  }

  "Unsuccessfully applying for FP" should {
    "return 303" in {
      when(mockPlaConnector.applyFP16(anyString())(any(), ArgumentMatchers.any())).thenReturn(Future.successful(testFP16RejectionResponse))
      when(mockPlaConnector.applyIP16(anyString(), any())(any(), ArgumentMatchers.any())).thenReturn(Future.successful(testIP16RejectionResponse))
      when(mockPlaConnector.applyIP14(anyString(), any())(any(), ArgumentMatchers.any())).thenReturn(Future.successful(testIP14RejectionResponse))

      when(mockKeyStoreConnector.fetchAllUserData(any())).thenReturn(Future.successful(Some(CacheMap("tstID", Map.empty[String, JsValue]))))
      when(mockKeyStoreConnector.saveData[ApplyResponseModel](anyString(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("tstId", Map.empty[String, JsValue])))
      when(mockKeyStoreConnector.fetchAndGetFormData[ApplyResponseModel](ArgumentMatchers.matches("applyResponseModel"))(any(), any()))
        .thenReturn(Future.successful(Some(testIP14RejectionApplyResponseModel)))


      when(mockResponseConstructors.createApplyResponseModelFromJson(any())).thenReturn(Some(testFPRejectionApplyResponseModel))
      when(mockKeyStoreConnector.fetchAndGetFormData[ApplyResponseModel](any())(any(), any()))
        .thenReturn(Future.successful(Some(testFPRejectionApplyResponseModel)))

      when(mockDisplayConstructors.createRejectionDisplayModel(any())(any())).thenReturn(RejectionDisplayModel(
        "12313123", Nil, ApplicationType.FP2016
      ))
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

      lazy val DataItemresult = TestRejectResultController.processFPApplication(fakeRequest)
      status(DataItemresult) shouldBe 303
      redirectLocation(DataItemresult) shouldBe Some(s"${routes.ResultController.displayFP16}")
    }
    "return 200" in {

      when(mockKeyStoreConnector.fetchAndGetFormData[ApplyResponseModel](any())(any(), any()))
        .thenReturn(Future.successful(Some(testFPRejectionApplyResponseModel)))

      when(mockDisplayConstructors.createRejectionDisplayModel(any())(any())).thenReturn(RejectionDisplayModel(
        "12313123", Nil, ApplicationType.FP2016
      ))
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

      lazy val GetItemResult = TestRejectResultController.displayResult(ApplicationType.FP2016)(fakeRequest)
      status(GetItemResult) shouldBe 200
    }
  }

  "Successfully applying for IP 2016" should {
    "return 303" in {

      when(mockPlaConnector.applyFP16(anyString())(any(), any())).thenReturn(Future.successful(testFP16RejectionResponse))
      when(mockPlaConnector.applyIP16(anyString(), any())(any(), any())).thenReturn(Future.successful(testIP16RejectionResponse))
      when(mockPlaConnector.applyIP14(anyString(), any())(any(), any())).thenReturn(Future.successful(testIP14RejectionResponse))

      when(mockKeyStoreConnector.fetchAllUserData(any())).thenReturn(Future.successful(Some(CacheMap("tstID", Map.empty[String, JsValue]))))
      when(mockKeyStoreConnector.saveData[ApplyResponseModel](anyString(), any())(any(), any())).thenReturn(Future.successful(CacheMap("tstId", Map.empty[String, JsValue])))
      when(mockKeyStoreConnector.fetchAndGetFormData[ApplyResponseModel](ArgumentMatchers.matches("applyResponseModel"))(any(), any())).thenReturn(Future.successful(Some(testIP14RejectionApplyResponseModel)))


      when(mockResponseConstructors.createApplyResponseModelFromJson(any())).thenReturn(Some(testIP16SuccessApplyResponseModel))
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      lazy val DataItemresult = TestSuccessResultController.processIPApplication(fakeRequest)
      status(DataItemresult) shouldBe 303
      redirectLocation(DataItemresult) shouldBe Some(s"${routes.ResultController.displayIP16}")

    }
    "return 200" in {

      when(mockKeyStoreConnector.fetchAndGetFormData[ApplyResponseModel](any())(any(), any()))
        .thenReturn(Future.successful(Some(testIP16SuccessApplyResponseModel)))

      when(mockDisplayConstructors.createSuccessDisplayModel(any())(any(), any())).thenReturn(SuccessDisplayModel(
        ApplicationType.FP2016, "12313123", "50", true, None, Nil
      ))

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      lazy val GetItemResult = TestSuccessResultController.displayResult(ApplicationType.IP2016)(fakeRequest)

      status(GetItemResult) shouldBe 200
    }

    "Unsuccessfully applying for IP 2016" should {
      "return 303" in {
        when(mockPlaConnector.applyFP16(anyString())(any(), any())).thenReturn(Future.successful(testFP16RejectionResponse))
        when(mockPlaConnector.applyIP16(anyString(), any())(any(), any())).thenReturn(Future.successful(testIP16RejectionResponse))
        when(mockPlaConnector.applyIP14(anyString(), any())(any(), any())).thenReturn(Future.successful(testIP14RejectionResponse))

        when(mockKeyStoreConnector.fetchAllUserData(any())).thenReturn(Future.successful(Some(CacheMap("tstID", Map.empty[String, JsValue]))))
        when(mockKeyStoreConnector.saveData[ApplyResponseModel](anyString(), any())(any(), any()))
          .thenReturn(Future.successful(CacheMap("tstId", Map.empty[String, JsValue])))
        when(mockKeyStoreConnector.fetchAndGetFormData[ApplyResponseModel](ArgumentMatchers.matches("applyResponseModel"))(any(), any()))
          .thenReturn(Future.successful(Some(testIP14RejectionApplyResponseModel)))


        when(mockResponseConstructors.createApplyResponseModelFromJson(any())).thenReturn(Some(testFPRejectionApplyResponseModel))
        when(mockKeyStoreConnector.fetchAndGetFormData[ApplyResponseModel](any())(any(), any()))
          .thenReturn(Future.successful(Some(testFPRejectionApplyResponseModel)))

        when(mockDisplayConstructors.createRejectionDisplayModel(any())(any())).thenReturn(RejectionDisplayModel(
          "12313123", Nil, ApplicationType.IP2016
        ))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        lazy val DataItemresult = TestRejectResultController.processIPApplication(fakeRequest)

        status(DataItemresult) shouldBe 303
        redirectLocation(DataItemresult) shouldBe Some(s"${routes.ResultController.displayIP16}")
      }
      "return 200" in {

        when(mockKeyStoreConnector.fetchAndGetFormData[ApplyResponseModel](any())(any(), any()))
          .thenReturn(Future.successful(Some(testFPRejectionApplyResponseModel)))

        when(mockDisplayConstructors.createRejectionDisplayModel(any())(any())).thenReturn(RejectionDisplayModel(
          "12313123", Nil, ApplicationType.IP2016
        ))
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        lazy val GetItemResult = TestRejectResultController.displayResult(ApplicationType.IP2016)(fakeRequest)
        status(GetItemResult) shouldBe 200
      }
    }

    "Failure to create an ApplyResponse model from an application response" should {
      "return 500" in {
        when(mockPlaConnector.applyFP16(anyString())(ArgumentMatchers.any(), any())).thenReturn(Future.successful(testFP16RejectionResponse))
        when(mockPlaConnector.applyIP16(anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), any())).thenReturn(Future.successful(testIP16RejectionResponse))
        when(mockPlaConnector.applyIP14(anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), any())).thenReturn(Future.successful(testIP14RejectionResponse))
        when(mockKeyStoreConnector.fetchAllUserData(ArgumentMatchers.any())).thenReturn(Future.successful(Some(CacheMap("tstID", Map.empty[String, JsValue]))))
        when(mockResponseConstructors.createApplyResponseModelFromJson(ArgumentMatchers.any())).thenReturn(None)

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        val result = testResultController.processFPApplication(fakeRequest)

        status(result) shouldBe 500
        await(result).header.headers("Cache-Control") shouldBe "no-cache"
      }
    }

    "Applying for inactive IP2016 protection" should {
      "return 303" in {
        when(mockPlaConnector.applyFP16(anyString())(ArgumentMatchers.any(), any())).thenReturn(Future.successful(testFP16SuccessResponse))
        when(mockPlaConnector.applyIP16(anyString(), ArgumentMatchers.any())(any(), any())).thenReturn(Future.successful(testIP16InactiveSuccessResponse))
        when(mockPlaConnector.applyIP14(anyString(), ArgumentMatchers.any())(any(), any())).thenReturn(Future.successful(testIP14InactiveSuccessResponse))


        when(mockKeyStoreConnector.fetchAllUserData(ArgumentMatchers.any())).thenReturn(Future.successful(Some(CacheMap("tstID", Map.empty[String, JsValue]))))
        when(mockKeyStoreConnector.saveData[ApplyResponseModel](anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(CacheMap("tstId", Map.empty[String, JsValue])))
        when(mockKeyStoreConnector.fetchAndGetFormData[ApplyResponseModel](ArgumentMatchers.matches("applyResponseModel"))(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(Some(testIP16InactiveSuccessApplyResponseModel)))


        when(mockResponseConstructors.createApplyResponseModelFromJson(ArgumentMatchers.any())).thenReturn(Some(testIP16InactiveSuccessApplyResponseModel))
        when(mockDisplayConstructors.createInactiveAmendResponseDisplayModel(any())).thenReturn(InactiveAmendResultDisplayModel(
          "12313123", Nil
        ))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

        val DataItemresult = TestInactiveSuccessResultController.processIPApplication(fakeRequest)
        status(DataItemresult) shouldBe 303
        redirectLocation(DataItemresult) shouldBe Some(s"${routes.ResultController.displayIP16}")
      }
      "return 200" in {
        when(mockKeyStoreConnector.fetchAndGetFormData[ApplyResponseModel](any())(any(), any()))
          .thenReturn(Future.successful(Some(testIP16InactiveSuccessApplyResponseModel)))

        when(mockDisplayConstructors.createSuccessDisplayModel(any())(any(), any())).thenReturn(SuccessDisplayModel(
          ApplicationType.FP2016, "12313123", "50", true, None, Nil
        ))

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

        val GetItemResult = TestInactiveSuccessResultController.displayResult(ApplicationType.IP2016)(fakeRequest)
        status(GetItemResult) shouldBe 200
      }
    }

    "Application outcome for IP14" should {
      implicit val protectionType = ApplicationType.IP2014
      "return Rejected for rejection codes" in {
        val rejectionCodes = List(1, 2, 25, 26, 27, 28, 29)
        for (code <- rejectionCodes) {
          TestSuccessResultController.applicationOutcome(code) shouldBe ApplicationOutcome.Rejected
        }
      }
      "return Successful for successful, active codes" in {
        val successfulActiveCodes = List(3, 4, 8, 33, 34)
        for (code <- successfulActiveCodes) {
          TestSuccessResultController.applicationOutcome(code) shouldBe ApplicationOutcome.Successful
        }
      }

      "return SuccessfulInactive for successful, inactive codes" in {
        val successfulInactiveCodes = List(5, 6, 7, 30, 31, 32)
        for (code <- successfulInactiveCodes) {
          TestSuccessResultController.applicationOutcome(code) shouldBe ApplicationOutcome.SuccessfulInactive
        }
      }
    }

    "Application outcome for IP16" should {
      implicit val protectionType = ApplicationType.IP2016
      "return Rejected for rejection codes" in {
        val rejectionCodes = List(9, 10, 11, 35, 36, 37, 38, 39)
        for (code <- rejectionCodes) {
          TestSuccessResultController.applicationOutcome(code) shouldBe ApplicationOutcome.Rejected
        }
      }

      "return Successful for successful, active codes" in {
        val successfulActiveCodes = List(12, 44)
        for (code <- successfulActiveCodes) {
          TestSuccessResultController.applicationOutcome(code) shouldBe ApplicationOutcome.Successful
        }
      }

      "return SuccessfulInactive for successful, inactive codes" in {
        val successfulInactiveCodes = List(13, 14, 15, 16, 40, 41, 42, 43)
        for (code <- successfulInactiveCodes) {
          TestSuccessResultController.applicationOutcome(code) shouldBe ApplicationOutcome.SuccessfulInactive
        }
      }
    }

    "Application outcome for FP16" should {
      implicit val protectionType = ApplicationType.FP2016
      "return Rejected for rejection codes" in {
        val rejectionCodes = List(17, 18, 19, 20, 21)
        for (code <- rejectionCodes) {
          TestSuccessResultController.applicationOutcome(code) shouldBe ApplicationOutcome.Rejected
        }
      }

      "return Successful for successful, active codes" in {
        val successfulActiveCodes = List(22, 23)
        for (code <- successfulActiveCodes) {
          TestSuccessResultController.applicationOutcome(code) shouldBe ApplicationOutcome.Successful
        }
      }

      "return SuccessfulInactive for successful, inactive codes" in {
        val successfulInactiveCodes = List(24)
        for (code <- successfulInactiveCodes) {
          TestSuccessResultController.applicationOutcome(code) shouldBe ApplicationOutcome.SuccessfulInactive
        }
      }
    }

    "Calling routeViaMCNeededCheck" when {

      "handling a 423 response" should {
        lazy val result = TestSuccessResultController.routeViaMCNeededCheck(HttpResponse(status = LOCKED, json = JsObject.empty, headers = Map.empty), "")(fakeRequest, ApplicationType.IP2016)

        "return a locked status" in {
          status(result) shouldBe LOCKED
        }
      }

      "handling any other response" should {

        "correctly redirect" in {
          when(mockKeyStoreConnector.saveData[ApplyResponseModel](anyString(), any())(any(), any())).thenReturn(Future.successful(CacheMap("tstId", Map.empty[String, JsValue])))

          when(mockResponseConstructors.createApplyResponseModelFromJson(any())).thenReturn(Some(testIP16SuccessApplyResponseModel))
          mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

          lazy val result = TestSuccessResultController.routeViaMCNeededCheck(HttpResponse(status = OK, json = JsObject.empty, headers = Map.empty), "AB123456A")(fakeRequest, ApplicationType.IP2016)

          status(result) shouldBe SEE_OTHER
        }
      }
    }

    "Calling saveAndRedirectToDisplay" when {
      "not provided with valid json" should {
        "return an internal server error" in {
          when(mockPlaConnector.applyFP16(anyString())(ArgumentMatchers.any(), any())).thenReturn(Future.successful(testFP16RejectionResponse))
          when(mockPlaConnector.applyIP16(anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), any())).thenReturn(Future.successful(testIP16RejectionResponse))
          when(mockPlaConnector.applyIP14(anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), any())).thenReturn(Future.successful(testIP14RejectionResponse))

          when(mockKeyStoreConnector.fetchAllUserData(ArgumentMatchers.any())).thenReturn(Future.successful(Some(CacheMap("tstID", Map.empty[String, JsValue]))))

          when(mockResponseConstructors.createApplyResponseModelFromJson(ArgumentMatchers.any())).thenReturn(None)

          val result = TestIncorrectResponseModelResultController.saveAndRedirectToDisplay(HttpResponse(status = OK, json = JsObject.empty, headers = Map.empty), "")(fakeRequest, ApplicationType.IP2016)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      }

      "provided with no notification ID" should {
        "return an internal server error" in {
          when(mockPlaConnector.applyFP16(anyString())(ArgumentMatchers.any(), any())).thenReturn(Future.successful(testFP16RejectionResponse))
          when(mockPlaConnector.applyIP16(anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), any())).thenReturn(Future.successful(testIP16RejectionResponse))
          when(mockPlaConnector.applyIP14(anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), any())).thenReturn(Future.successful(testIP14RejectionResponse))

          when(mockKeyStoreConnector.fetchAllUserData(ArgumentMatchers.any())).thenReturn(Future.successful(Some(CacheMap("tstID", Map.empty[String, JsValue]))))
          when(mockResponseConstructors.createApplyResponseModelFromJson(ArgumentMatchers.any())).thenReturn(Some(ApplyResponseModel(ProtectionModel(None, None))))

          val result = TestIncorrectResponseModelResultController.saveAndRedirectToDisplay(HttpResponse(status = OK, json = JsObject.empty, headers = Map.empty), "")(fakeRequest, ApplicationType.IP2016)
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      }

      "provided with an IP2016 protection" should {
        "redirect the user" in {
          when(mockPlaConnector.applyFP16(anyString())(ArgumentMatchers.any(), any())).thenReturn(Future.successful(testFP16SuccessResponse))
          when(mockPlaConnector.applyIP16(anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), any())).thenReturn(Future.successful(testIP16SuccessResponse))
          when(mockPlaConnector.applyIP14(anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), any())).thenReturn(Future.successful(testIP14SuccessResponse))

          when(mockKeyStoreConnector.fetchAllUserData(ArgumentMatchers.any())).thenReturn(Future.successful(Some(CacheMap("tstID", Map.empty[String, JsValue]))))
          when(mockKeyStoreConnector.saveData[ApplyResponseModel](anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(CacheMap("tstId", Map.empty[String, JsValue])))
          when(mockKeyStoreConnector.fetchAndGetFormData[ApplyResponseModel](ArgumentMatchers.matches("fp16ApplyResponseModel"))(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(Some(testFPSuccessApplyResponseModel)))
          when(mockKeyStoreConnector.fetchAndGetFormData[ApplyResponseModel](ArgumentMatchers.matches("ip14ApplyResponseModel"))(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(Some(testIP14SuccessApplyResponseModel)))
          when(mockKeyStoreConnector.fetchAndGetFormData[ApplyResponseModel](ArgumentMatchers.matches("applyResponseModel"))(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(Some(testIP16SuccessApplyResponseModel)))


          when(mockResponseConstructors.createApplyResponseModelFromJson(ArgumentMatchers.any())).thenReturn(Some(testFPSuccessApplyResponseModel))

          val result = TestSuccessResultController.saveAndRedirectToDisplay(HttpResponse(status = OK, json = JsObject.empty, headers = Map.empty), "")(fakeRequest, ApplicationType.IP2016)
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.ResultController.displayIP16.url)
        }
      }

      "provided with an FP2016 protection" should {
        "redirect the user" in {

          when(mockKeyStoreConnector.saveData[ApplyResponseModel](anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(CacheMap("tstId", Map.empty[String, JsValue])))
          when(mockResponseConstructors.createApplyResponseModelFromJson(ArgumentMatchers.any())).thenReturn(Some(testFPSuccessApplyResponseModel))

          val result = TestSuccessResultController.saveAndRedirectToDisplay(HttpResponse(status = OK, json = JsObject.empty, headers = Map.empty), "")(fakeRequest, ApplicationType.FP2016)
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.ResultController.displayFP16.url)
        }
      }
    }
  }
}

