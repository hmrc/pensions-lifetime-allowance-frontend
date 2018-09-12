/*
 * Copyright 2018 HM Revenue & Customs
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

import config.wiring.PlaFormPartialRetriever
import config.{AuthClientConnector, LocalTemplateRenderer, PlaContextImpl}
import connectors.{KeyStoreConnector, PLAConnector}
import constructors.ResponseConstructors
import enums.{ApplicationOutcome, ApplicationType}
import mocks.AuthMock
import models.{ApplyResponseModel, ProtectionModel}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import testHelpers.TestConfigHelper
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.Retrievals
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ResultControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfter with AuthMock with WithFakeApplication {

  implicit val fakeRequest = FakeRequest()
  implicit val plaContext = PlaContextImpl

  implicit val partialRetriever  = mock[PlaFormPartialRetriever]
  implicit val templateRenderer  = mock[LocalTemplateRenderer]
  val keyStoreConnector = mock[KeyStoreConnector]
  val plaConnector      = mock[PLAConnector]

  val successFP16Json = Json.parse("""{"certificateDate":"2016-05-10T17:20:55.138","nino":"AA123456A","notificationId":24,"protectionID":8243168284792526522,"protectionReference":"FP16138722390C","protectionType":"FP2016","status":"Open","version":1}""")
  val rejectionFP16Json = Json.parse("""{"nino":"AA123456A","notificationId":21,"protectionID":-4645895724767334826,"protectionType":"FP2016","status":"Rejected","version":1}""")

  val successIP16Json = Json.parse("""{"notificationId":12,"protectedAmount":1230000.0}""")
  val inactiveSuccessIP16Json = Json.parse("""{"notificationId":43,"protectedAmount":1600000.0}""")
  val rejectionIP16Json = Json.parse("""{"notificationId":9}""")

  val successIP14Json = Json.parse("""{"notificationId":3,"protectedAmount":1230000.0}""")
  val inactiveSuccessIP14Json = Json.parse("""{"notificationId":32,"protectedAmount":1400000.0}""")
  val rejectionIP14Json = Json.parse("""{"notificationId":1}""")

  val testFP16SuccessResponse = HttpResponse(200, Some(successFP16Json))
  val testFP16RejectionResponse = HttpResponse(409, Some(rejectionFP16Json))
  val testIP16SuccessResponse = HttpResponse(200, Some(successIP16Json))
  val testIP16RejectionResponse = HttpResponse(409, Some(rejectionIP16Json))
  val testIP14SuccessResponse = HttpResponse(200, Some(successIP14Json))
  val testIP14RejectionResponse = HttpResponse(409, Some(rejectionIP14Json))
  val testMCNeededResponse = HttpResponse(423)

  val testIP16InactiveSuccessResponse = HttpResponse(200, Some(inactiveSuccessIP16Json))
  val testIP14InactiveSuccessResponse = HttpResponse(200, Some(inactiveSuccessIP14Json))

  val testFPSuccessProtectionModel = ProtectionModel(
    Some("testPSARef"),
    notificationId = Some(24),
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

  val mockResponseConstructors: ResponseConstructors = mock[ResponseConstructors]

  val TestSuccessResultController = new ResultController(keyStoreConnector, plaConnector, partialRetriever, templateRenderer) {
    override lazy val authConnector: AuthConnector = mockAuthConnector
    override val responseConstructors: ResponseConstructors = mockResponseConstructors

    when(plaConnector.applyFP16(anyString())(ArgumentMatchers.any())).thenReturn(Future.successful(testFP16SuccessResponse))
    when(plaConnector.applyIP16(anyString(), ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.successful(testIP16SuccessResponse))
    when(plaConnector.applyIP14(anyString(), ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.successful(testIP14SuccessResponse))


    when(keyStoreConnector.fetchAllUserData(ArgumentMatchers.any())).thenReturn(Future.successful(Some(CacheMap("tstID", Map.empty[String, JsValue]))))
    when(keyStoreConnector.saveData[ApplyResponseModel](anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(CacheMap("tstId", Map.empty[String, JsValue])))
    when(keyStoreConnector.fetchAndGetFormData[ApplyResponseModel](ArgumentMatchers.matches("fp16ApplyResponseModel"))(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Some(testFPSuccessApplyResponseModel))
    when(keyStoreConnector.fetchAndGetFormData[ApplyResponseModel](ArgumentMatchers.matches("ip14ApplyResponseModel"))(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Some(testIP14SuccessApplyResponseModel))
    when(keyStoreConnector.fetchAndGetFormData[ApplyResponseModel](ArgumentMatchers.matches("applyResponseModel"))(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Some(testIP16SuccessApplyResponseModel))


    when(mockResponseConstructors.createApplyResponseModelFromJson(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Some(testFPSuccessApplyResponseModel))
  }

  val TestInactiveSuccessResultController = new ResultController(keyStoreConnector, plaConnector, partialRetriever, templateRenderer) {
    override lazy val authConnector: AuthConnector = mockAuthConnector
    override val responseConstructors: ResponseConstructors = mockResponseConstructors

    when(plaConnector.applyFP16(anyString())(ArgumentMatchers.any())).thenReturn(Future.successful(testFP16SuccessResponse))
    when(plaConnector.applyIP16(anyString(), ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.successful(testIP16InactiveSuccessResponse))
    when(plaConnector.applyIP14(anyString(), ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.successful(testIP14InactiveSuccessResponse))


    when(keyStoreConnector.fetchAllUserData(ArgumentMatchers.any())).thenReturn(Future.successful(Some(CacheMap("tstID", Map.empty[String, JsValue]))))
    when(keyStoreConnector.saveData[ApplyResponseModel](anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(CacheMap("tstId", Map.empty[String, JsValue])))
    when(keyStoreConnector.fetchAndGetFormData[ApplyResponseModel](ArgumentMatchers.matches("fp16ApplyResponseModel"))(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Some(testFPSuccessApplyResponseModel))
    when(keyStoreConnector.fetchAndGetFormData[ApplyResponseModel](ArgumentMatchers.matches("ip14ApplyResponseModel"))(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Some(testIP14InactiveSuccessApplyResponseModel))
    when(keyStoreConnector.fetchAndGetFormData[ApplyResponseModel](ArgumentMatchers.matches("applyResponseModel"))(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Some(testIP16InactiveSuccessApplyResponseModel))


    when(mockResponseConstructors.createApplyResponseModelFromJson(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Some(testFPSuccessApplyResponseModel))
  }

  val TestRejectResultController = new ResultController(keyStoreConnector, plaConnector, partialRetriever, templateRenderer) {
    override lazy val authConnector: AuthConnector = mockAuthConnector
    override val responseConstructors: ResponseConstructors = mockResponseConstructors

    when(plaConnector.applyFP16(anyString())(ArgumentMatchers.any())).thenReturn(Future.successful(testFP16RejectionResponse))
    when(plaConnector.applyIP16(anyString(), ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.successful(testIP16RejectionResponse))
    when(plaConnector.applyIP14(anyString(), ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.successful(testIP14RejectionResponse))

    when(keyStoreConnector.fetchAllUserData(ArgumentMatchers.any())).thenReturn(Future.successful(Some(CacheMap("tstID", Map.empty[String, JsValue]))))
    when(keyStoreConnector.saveData[ApplyResponseModel](anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(CacheMap("tstId", Map.empty[String, JsValue])))
    when(keyStoreConnector.fetchAndGetFormData[ApplyResponseModel](ArgumentMatchers.matches("fp16ApplyResponseModel"))(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Some(testFPRejectionApplyResponseModel))
    when(keyStoreConnector.fetchAndGetFormData[ApplyResponseModel](ArgumentMatchers.matches("ip14ApplyResponseModel"))(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Some(testIP14RejectionApplyResponseModel))
    when(keyStoreConnector.fetchAndGetFormData[ApplyResponseModel](ArgumentMatchers.matches("applyResponseModel"))(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Some(testIP16RejectionApplyResponseModel))


    when(mockResponseConstructors.createApplyResponseModelFromJson(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Some(testFPRejectionApplyResponseModel))

  }

  val TestMCNeededResultController = new ResultController(keyStoreConnector, plaConnector, partialRetriever, templateRenderer) {
    override lazy val authConnector: AuthConnector = mockAuthConnector
    override val responseConstructors: ResponseConstructors = mockResponseConstructors

    when(plaConnector.applyFP16(anyString())(ArgumentMatchers.any())).thenReturn(Future.successful(testMCNeededResponse))
    when(plaConnector.applyIP16(anyString(), ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.successful(testMCNeededResponse))
    when(plaConnector.applyIP14(anyString(), ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.successful(testMCNeededResponse))

    when(keyStoreConnector.fetchAllUserData(ArgumentMatchers.any())).thenReturn(Future.successful(Some(CacheMap("tstID", Map.empty[String, JsValue]))))
  }

  val TestNoNotificationIdResponse = {
    when(plaConnector.applyFP16(anyString())(ArgumentMatchers.any())).thenReturn(Future.successful(testFP16RejectionResponse))
    when(plaConnector.applyIP16(anyString(), ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.successful(testIP16RejectionResponse))
    when(plaConnector.applyIP14(anyString(), ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.successful(testIP14RejectionResponse))

    val keyStoreConnector = mock[KeyStoreConnector]
    when(keyStoreConnector.fetchAllUserData(ArgumentMatchers.any())).thenReturn(Future.successful(Some(CacheMap("tstID", Map.empty[String, JsValue]))))

    val responseConstructors = mock[ResponseConstructors]
    when(responseConstructors.createApplyResponseModelFromJson(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Some(ApplyResponseModel(ProtectionModel(None, None))))
  }

  val testResultController = new ResultController(keyStoreConnector, plaConnector, partialRetriever, templateRenderer) {
    override lazy val authConnector: AuthConnector = mockAuthConnector
    override val responseConstructors: ResponseConstructors = mockResponseConstructors

    when(plaConnector.applyFP16(anyString())(ArgumentMatchers.any())).thenReturn(Future.successful(testFP16RejectionResponse))
    when(plaConnector.applyIP16(anyString(), ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.successful(testIP16RejectionResponse))
    when(plaConnector.applyIP14(anyString(), ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.successful(testIP14RejectionResponse))
    when(keyStoreConnector.fetchAllUserData(ArgumentMatchers.any())).thenReturn(Future.successful(Some(CacheMap("tstID", Map.empty[String, JsValue]))))
    when(mockResponseConstructors.createApplyResponseModelFromJson(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(None)
  }


  val TestIncorrectResponseModelResultController = new ResultController(keyStoreConnector, plaConnector, partialRetriever, templateRenderer) {
    override lazy val authConnector: AuthConnector = mockAuthConnector
    override val responseConstructors: ResponseConstructors = mockResponseConstructors

    when(plaConnector.applyFP16(anyString())(ArgumentMatchers.any())).thenReturn(Future.successful(testFP16RejectionResponse))
    when(plaConnector.applyIP16(anyString(), ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.successful(testIP16RejectionResponse))
    when(plaConnector.applyIP14(anyString(), ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.successful(testIP14RejectionResponse))

    when(keyStoreConnector.fetchAllUserData(ArgumentMatchers.any())).thenReturn(Future.successful(Some(CacheMap("tstID", Map.empty[String, JsValue]))))

    when(mockResponseConstructors.createApplyResponseModelFromJson(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(None)
  }

  //////////////////////////////////////////////
  //  POST / REDIRECT
  /////////////////////////////////////////////


  "Successfully applying for FP" should {
    "return 303" in {

      when(plaConnector.applyFP16(anyString())(ArgumentMatchers.any())).thenReturn(Future.successful(testFP16RejectionResponse))
      when(plaConnector.applyIP16(anyString(), ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.successful(testIP16RejectionResponse))
      when(plaConnector.applyIP14(anyString(), ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.successful(testIP14RejectionResponse))

      when(keyStoreConnector.fetchAllUserData(ArgumentMatchers.any())).thenReturn(Future.successful(Some(CacheMap("tstID", Map.empty[String, JsValue]))))
      when(keyStoreConnector.saveData[ApplyResponseModel](anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(CacheMap("tstId", Map.empty[String, JsValue])))
      when(keyStoreConnector.fetchAndGetFormData[ApplyResponseModel](ArgumentMatchers.matches("fp16ApplyResponseModel"))(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Some(testFPRejectionApplyResponseModel))
      when(keyStoreConnector.fetchAndGetFormData[ApplyResponseModel](ArgumentMatchers.matches("ip14ApplyResponseModel"))(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Some(testIP14RejectionApplyResponseModel))
      when(keyStoreConnector.fetchAndGetFormData[ApplyResponseModel](ArgumentMatchers.matches("applyResponseModel"))(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Some(testIP16RejectionApplyResponseModel))


      when(mockResponseConstructors.createApplyResponseModelFromJson(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Some(testFPRejectionApplyResponseModel))
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

      val DataItemResult = await(TestSuccessResultController.processFPApplication(fakeRequest))

      status(DataItemResult) shouldBe 303
      redirectLocation(DataItemResult) shouldBe Some(s"${routes.ResultController.displayFP16()}")
    }
    "return 200" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

      val GetItemResult = await(TestSuccessResultController.displayResult(ApplicationType.FP2016)(fakeRequest))
      status(GetItemResult) shouldBe 200
    }
  }

  "Unsuccessfully applying for FP" should {

    mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
    lazy val DataItemresult = await(TestRejectResultController.processFPApplication(fakeRequest))
    lazy val GetItemResult = await(TestRejectResultController.displayResult(ApplicationType.FP2016)(fakeRequest))

    "return 303" in {
      status(DataItemresult) shouldBe 303
    }
    "redirect the user to the result rejection page" in {
      redirectLocation(DataItemresult) shouldBe Some(s"${routes.ResultController.displayFP16()}")
    }
    "return 200" in {
      status(GetItemResult) shouldBe 200
    }
  }

  "Successfully applying for IP 2016" should {

    mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
    lazy val DataItemresult = await(TestSuccessResultController.processIPApplication(fakeRequest))
    lazy val GetItemResult = await(TestSuccessResultController.displayResult(ApplicationType.IP2016)(fakeRequest))

    "return 303" in {
      status(DataItemresult) shouldBe 303
    }
    "redirect the user to the result success page" in {
      redirectLocation(DataItemresult) shouldBe Some(s"${routes.ResultController.displayIP16()}")
    }
    "return 200" in {
      status(GetItemResult) shouldBe 200
    }
  }

  "Unsuccessfully applying for IP 2016" should {

    mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
    lazy val DataItemresult = await(TestRejectResultController.processIPApplication(fakeRequest))
    lazy val GetItemResult = await(TestRejectResultController.displayResult(ApplicationType.IP2016)(fakeRequest))

    "return 303" in {
      status(DataItemresult) shouldBe 303
    }
    "redirect the user to the result rejection page" in {
      redirectLocation(DataItemresult) shouldBe Some(s"${routes.ResultController.displayIP16()}")
    }
    "return 200" in {
      status(GetItemResult) shouldBe 200
    }
  }

  "Failure to create an ApplyResponse model from an application response" should {
    "return 500" in {
      when(plaConnector.applyFP16(anyString())(ArgumentMatchers.any())).thenReturn(Future.successful(testFP16RejectionResponse))
      when(plaConnector.applyIP16(anyString(), ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.successful(testIP16RejectionResponse))
      when(plaConnector.applyIP14(anyString(), ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.successful(testIP14RejectionResponse))
      when(keyStoreConnector.fetchAllUserData(ArgumentMatchers.any())).thenReturn(Future.successful(Some(CacheMap("tstID", Map.empty[String, JsValue]))))
      when(mockResponseConstructors.createApplyResponseModelFromJson(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(None)

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      val result = await(testResultController.processFPApplication(fakeRequest))

      status(result) shouldBe 500
      result.header.headers("Cache-Control") shouldBe "no-cache"
    }
  }

  "Applying for inactive IP2016 protection" should {
    "return 303" in {
      when(plaConnector.applyFP16(anyString())(ArgumentMatchers.any())).thenReturn(Future.successful(testFP16SuccessResponse))
      when(plaConnector.applyIP16(anyString(), ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.successful(testIP16InactiveSuccessResponse))
      when(plaConnector.applyIP14(anyString(), ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.successful(testIP14InactiveSuccessResponse))


      when(keyStoreConnector.fetchAllUserData(ArgumentMatchers.any())).thenReturn(Future.successful(Some(CacheMap("tstID", Map.empty[String, JsValue]))))
      when(keyStoreConnector.saveData[ApplyResponseModel](anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(CacheMap("tstId", Map.empty[String, JsValue])))
      when(keyStoreConnector.fetchAndGetFormData[ApplyResponseModel](ArgumentMatchers.matches("fp16ApplyResponseModel"))(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Some(testFPSuccessApplyResponseModel))
      when(keyStoreConnector.fetchAndGetFormData[ApplyResponseModel](ArgumentMatchers.matches("ip14ApplyResponseModel"))(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Some(testIP14InactiveSuccessApplyResponseModel))
      when(keyStoreConnector.fetchAndGetFormData[ApplyResponseModel](ArgumentMatchers.matches("applyResponseModel"))(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Some(testIP16InactiveSuccessApplyResponseModel))


      when(mockResponseConstructors.createApplyResponseModelFromJson(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Some(testFPSuccessApplyResponseModel))
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

      val DataItemresult = await(TestInactiveSuccessResultController.processIPApplication(fakeRequest))
      status(DataItemresult) shouldBe 303
      redirectLocation(DataItemresult) shouldBe Some(s"${routes.ResultController.displayIP16()}")
    }
    "return 200" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

      val GetItemResult = await(TestInactiveSuccessResultController.displayResult(ApplicationType.IP2016)(fakeRequest))
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
      lazy val result = TestSuccessResultController.routeViaMCNeededCheck(HttpResponse(LOCKED), "")(fakeRequest, ApplicationType.IP2016)

      "return a locked status" in {
        status(result) shouldBe LOCKED
      }
    }

    "handling any other response" should {
      lazy val result = TestSuccessResultController.routeViaMCNeededCheck(HttpResponse(OK), "")(fakeRequest, ApplicationType.IP2016)

      "correctly redirect" in {
        status(result) shouldBe SEE_OTHER
      }
    }
  }

  "Calling saveAndRedirectToDisplay" when {
    "not provided with valid json" should {
      "return an internal server error" in {
        when(plaConnector.applyFP16(anyString())(ArgumentMatchers.any())).thenReturn(Future.successful(testFP16RejectionResponse))
        when(plaConnector.applyIP16(anyString(), ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.successful(testIP16RejectionResponse))
        when(plaConnector.applyIP14(anyString(), ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.successful(testIP14RejectionResponse))

        when(keyStoreConnector.fetchAllUserData(ArgumentMatchers.any())).thenReturn(Future.successful(Some(CacheMap("tstID", Map.empty[String, JsValue]))))

        when(mockResponseConstructors.createApplyResponseModelFromJson(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(None)

        val result = TestIncorrectResponseModelResultController.saveAndRedirectToDisplay(HttpResponse(OK), "")(fakeRequest, ApplicationType.IP2016)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "provided with no notification ID" should {
      val test = new ResultController(keyStoreConnector, plaConnector, partialRetriever, templateRenderer){
        override lazy val authConnector = mockAuthConnector
        override val responseConstructors = mockResponseConstructors
      }
      when(plaConnector.applyFP16(anyString())(ArgumentMatchers.any())).thenReturn(Future.successful(testFP16RejectionResponse))
      when(plaConnector.applyIP16(anyString(), ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.successful(testIP16RejectionResponse))
      when(plaConnector.applyIP14(anyString(), ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.successful(testIP14RejectionResponse))

      when(keyStoreConnector.fetchAllUserData(ArgumentMatchers.any())).thenReturn(Future.successful(Some(CacheMap("tstID", Map.empty[String, JsValue]))))
      when(mockResponseConstructors.createApplyResponseModelFromJson(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Some(ApplyResponseModel(ProtectionModel(None, None))))

      "return an internal server error" in {
        val result = test.saveAndRedirectToDisplay(HttpResponse(OK), "")(fakeRequest, ApplicationType.IP2016)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "provided with an IP2016 protection" should {
      "redirect the user" in {
        when(plaConnector.applyFP16(anyString())(ArgumentMatchers.any())).thenReturn(Future.successful(testFP16SuccessResponse))
        when(plaConnector.applyIP16(anyString(), ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.successful(testIP16SuccessResponse))
        when(plaConnector.applyIP14(anyString(), ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Future.successful(testIP14SuccessResponse))


        when(keyStoreConnector.fetchAllUserData(ArgumentMatchers.any())).thenReturn(Future.successful(Some(CacheMap("tstID", Map.empty[String, JsValue]))))
        when(keyStoreConnector.saveData[ApplyResponseModel](anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(CacheMap("tstId", Map.empty[String, JsValue])))
        when(keyStoreConnector.fetchAndGetFormData[ApplyResponseModel](ArgumentMatchers.matches("fp16ApplyResponseModel"))(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Some(testFPSuccessApplyResponseModel))
        when(keyStoreConnector.fetchAndGetFormData[ApplyResponseModel](ArgumentMatchers.matches("ip14ApplyResponseModel"))(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Some(testIP14SuccessApplyResponseModel))
        when(keyStoreConnector.fetchAndGetFormData[ApplyResponseModel](ArgumentMatchers.matches("applyResponseModel"))(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Some(testIP16SuccessApplyResponseModel))


        when(mockResponseConstructors.createApplyResponseModelFromJson(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(Some(testFPSuccessApplyResponseModel))

        val result = TestSuccessResultController.saveAndRedirectToDisplay(HttpResponse(OK), "")(fakeRequest, ApplicationType.IP2016)
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.ResultController.displayIP16().url)
      }
    }

    "provided with an FP2016 protection" should {

      "redirect the user" in {
        val result = TestSuccessResultController.saveAndRedirectToDisplay(HttpResponse(OK), "")(fakeRequest, ApplicationType.FP2016)
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.ResultController.displayFP16().url)
      }
    }
  }
}
