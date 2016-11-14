/*
 * Copyright 2016 HM Revenue & Customs
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

import auth.{MockAuthConnector, MockConfig}
import com.kenshoo.play.metrics.PlayModule
import org.mockito.Matchers
import play.api.i18n.Messages
import testHelpers.{AuthorisedFakeRequestTo, AuthorisedFakeRequestToPost, FakeRequestTo}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers.anyString
import config.{FrontendAppConfig, FrontendAuthConnector}
import play.api.libs.json.{JsValue, Json}
import connectors.{KeyStoreConnector, PLAConnector}
import constructors.ResponseConstructors
import enums.{ApplicationOutcome, ApplicationType}
import models.{ApplyResponseModel, ProtectionModel}
import org.scalatest.BeforeAndAfter
import play.api.mvc.{Action, AnyContent, Result}
import play.api.test.Helpers._

import scala.concurrent.Future


class ResultControllerSpec extends UnitSpec with MockitoSugar with WithFakeApplication with BeforeAndAfter {
  override def bindModules = Seq(new PlayModule)

  val successFP16Json = Json.parse("""{"certificateDate":"2016-05-10T17:20:55.138","nino":"AA123456A","notificationId":24,"protectionID":8243168284792526522,"protectionReference":"FP16138722390C","protectionType":"FP2016","status":"Open","version":1}""")
  val rejectionFP16Json = Json.parse("""{"nino":"AA123456A","notificationId":21,"protectionID":-4645895724767334826,"protectionType":"FP2016","status":"Rejected","version":1}""")

  val successIP16Json = Json.parse("""{"notificationId":12,"protectedAmount":1230000.0}""")
  val inactiveSuccessIP16Json = Json.parse("""{"notificationId":43,"protectedAmount":1600000.0}""")
  val rejectionIP16Json = Json.parse("""{"notificationId":9}""")

  val successIP14Json = Json.parse("""{"notificationId":3,"protectedAmount":1230000.0}""")
  val inactiveSuccessIP14Json = Json.parse("""{"notificationId":32,"protectedAmount":1400000.0}""")
  val rejectionIP14Json = Json.parse("""{"notificationId":1}""")

  val testFP16SuccessResponse = HttpResponse(200,Some(successFP16Json))
  val testFP16RejectionResponse = HttpResponse(409,Some(rejectionFP16Json))
  val testIP16SuccessResponse = HttpResponse(200,Some(successIP16Json))
  val testIP16RejectionResponse = HttpResponse(409,Some(rejectionIP16Json))
  val testIP14SuccessResponse = HttpResponse(200,Some(successIP14Json))
  val testIP14RejectionResponse = HttpResponse(409,Some(rejectionIP14Json))
  val testMCNeededResponse = HttpResponse(423)

  val testIP16InactiveSuccessResponse = HttpResponse(200,Some(inactiveSuccessIP16Json))
  val testIP14InactiveSuccessResponse = HttpResponse(200,Some(inactiveSuccessIP14Json))

  val testFPSuccessProtectionModel = ProtectionModel (
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

  val testIP16SuccessProtectionModel = ProtectionModel (
    Some("testPSARef"),
    notificationId = Some(12),
    protectionID = Some(12345),
    protectionType = Some("IP2016"),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456"))

  val testIP16InactiveSuccessProtectionModel = ProtectionModel (
    Some("testPSARef"),
    notificationId = Some(43),
    protectionID = Some(12345),
    protectionType = Some("IP2016"),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1600000),
    protectionReference = Some("PSA123456"))

  val testIP16RejectionProtectionModel = ProtectionModel (
    None,
    notificationId = Some(9),
    protectionID = Some(12345),
    protectionType = Some("IP2016"),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456"))

  val testIP14SuccessProtectionModel = ProtectionModel (
    Some("testPSARef"),
    notificationId = Some(3),
    protectionID = Some(12345),
    protectionType = Some("IP2014"),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456"))

  val testIP14InactiveSuccessProtectionModel = ProtectionModel (
    Some("testPSARef"),
    notificationId = Some(32),
    protectionID = Some(12345),
    protectionType = Some("IP2014"),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1400000),
    protectionReference = Some("PSA123456"))

  val testIP14RejectionProtectionModel = ProtectionModel (
    None,
    notificationId = Some(1),
    protectionID = Some(12345),
    protectionType = Some("IP2014"),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456"))

  val testFPSuccessApplyResponseModel             = ApplyResponseModel(testFPSuccessProtectionModel)
  val testFPRejectionApplyResponseModel           = ApplyResponseModel(testFPRejectionProtectionModel)
  val testIP16SuccessApplyResponseModel           = ApplyResponseModel(testIP16SuccessProtectionModel)
  val testIP16InactiveSuccessApplyResponseModel   = ApplyResponseModel(testIP16InactiveSuccessProtectionModel)
  val testIP16RejectionApplyResponseModel         = ApplyResponseModel(testIP16RejectionProtectionModel)
  val testIP14SuccessApplyResponseModel           = ApplyResponseModel(testIP14SuccessProtectionModel)
  val testIP14InactiveSuccessApplyResponseModel   = ApplyResponseModel(testIP14InactiveSuccessProtectionModel)
  val testIP14RejectionApplyResponseModel         = ApplyResponseModel(testIP14RejectionProtectionModel)

  val mockKeyStoreConnector = mock[KeyStoreConnector]
  val mockResponseConstructors = mock[ResponseConstructors]


  object TestSuccessResultController extends ResultController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val postSignInRedirectUrl = "http://localhost:9012/protect-your-lifetime-allowance/apply-for-fp16"

    implicit val hc: HeaderCarrier = HeaderCarrier()

    override val plaConnector = mock[PLAConnector]
    when(plaConnector.applyFP16(anyString())(Matchers.any())).thenReturn(Future(testFP16SuccessResponse))
    when(plaConnector.applyIP16(anyString(), Matchers.any())(Matchers.any())).thenReturn(Future(testIP16SuccessResponse))
    when(plaConnector.applyIP14(anyString(), Matchers.any())(Matchers.any())).thenReturn(Future(testIP14SuccessResponse))


    override val keyStoreConnector = mock[KeyStoreConnector]
    when(keyStoreConnector.fetchAllUserData(Matchers.any())).thenReturn(Future(Some(CacheMap("tstID", Map.empty[String, JsValue]))))
    when(keyStoreConnector.saveData[ApplyResponseModel](anyString(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future(CacheMap("tstId", Map.empty[String, JsValue])))
    when(keyStoreConnector.fetchAndGetFormData[ApplyResponseModel](Matchers.matches("fp16ApplyResponseModel"))(Matchers.any(), Matchers.any())).thenReturn(Some(testFPSuccessApplyResponseModel))
    when(keyStoreConnector.fetchAndGetFormData[ApplyResponseModel](Matchers.matches("ip14ApplyResponseModel"))(Matchers.any(), Matchers.any())).thenReturn(Some(testIP14SuccessApplyResponseModel))
    when(keyStoreConnector.fetchAndGetFormData[ApplyResponseModel](Matchers.matches("applyResponseModel"))(Matchers.any(), Matchers.any())).thenReturn(Some(testIP16SuccessApplyResponseModel))


    override val responseConstructors = mockResponseConstructors
    when(responseConstructors.createApplyResponseModelFromJson(Matchers.any())(Matchers.any())).thenReturn(Some(testFPSuccessApplyResponseModel))
  }

  object TestInactiveSuccessResultController extends ResultController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val postSignInRedirectUrl = "http://localhost:9012/protect-your-lifetime-allowance/apply-for-fp16"

    implicit val hc: HeaderCarrier = HeaderCarrier()

    override val plaConnector = mock[PLAConnector]
    when(plaConnector.applyFP16(anyString())(Matchers.any())).thenReturn(Future(testFP16SuccessResponse))
    when(plaConnector.applyIP16(anyString(), Matchers.any())(Matchers.any())).thenReturn(Future(testIP16InactiveSuccessResponse))
    when(plaConnector.applyIP14(anyString(), Matchers.any())(Matchers.any())).thenReturn(Future(testIP14InactiveSuccessResponse))


    override val keyStoreConnector = mock[KeyStoreConnector]
    when(keyStoreConnector.fetchAllUserData(Matchers.any())).thenReturn(Future(Some(CacheMap("tstID", Map.empty[String, JsValue]))))
    when(keyStoreConnector.saveData[ApplyResponseModel](anyString(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future(CacheMap("tstId", Map.empty[String, JsValue])))
    when(keyStoreConnector.fetchAndGetFormData[ApplyResponseModel](Matchers.matches("fp16ApplyResponseModel"))(Matchers.any(), Matchers.any())).thenReturn(Some(testFPSuccessApplyResponseModel))
    when(keyStoreConnector.fetchAndGetFormData[ApplyResponseModel](Matchers.matches("ip14ApplyResponseModel"))(Matchers.any(), Matchers.any())).thenReturn(Some(testIP14InactiveSuccessApplyResponseModel))
    when(keyStoreConnector.fetchAndGetFormData[ApplyResponseModel](Matchers.matches("applyResponseModel"))(Matchers.any(), Matchers.any())).thenReturn(Some(testIP16InactiveSuccessApplyResponseModel))


    override val responseConstructors = mockResponseConstructors
    when(responseConstructors.createApplyResponseModelFromJson(Matchers.any())(Matchers.any())).thenReturn(Some(testFPSuccessApplyResponseModel))
  }

  object TestRejectResultController extends ResultController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val postSignInRedirectUrl = "http://localhost:9012/protect-your-lifetime-allowance/apply-for-fp16"

    implicit val hc: HeaderCarrier = HeaderCarrier()
    override val plaConnector = mock[PLAConnector]
    when(plaConnector.applyFP16(anyString())(Matchers.any())).thenReturn(Future(testFP16RejectionResponse))
    when(plaConnector.applyIP16(anyString(), Matchers.any())(Matchers.any())).thenReturn(Future(testIP16RejectionResponse))
    when(plaConnector.applyIP14(anyString(), Matchers.any())(Matchers.any())).thenReturn(Future(testIP14RejectionResponse))

    override val keyStoreConnector = mock[KeyStoreConnector]
    when(keyStoreConnector.fetchAllUserData(Matchers.any())).thenReturn(Future(Some(CacheMap("tstID", Map.empty[String, JsValue]))))
    when(keyStoreConnector.saveData[ApplyResponseModel](anyString(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future(CacheMap("tstId", Map.empty[String, JsValue])))
    when(keyStoreConnector.fetchAndGetFormData[ApplyResponseModel](Matchers.matches("fp16ApplyResponseModel"))(Matchers.any(), Matchers.any())).thenReturn(Some(testFPRejectionApplyResponseModel))
    when(keyStoreConnector.fetchAndGetFormData[ApplyResponseModel](Matchers.matches("ip14ApplyResponseModel"))(Matchers.any(), Matchers.any())).thenReturn(Some(testIP14RejectionApplyResponseModel))
    when(keyStoreConnector.fetchAndGetFormData[ApplyResponseModel](Matchers.matches("applyResponseModel"))(Matchers.any(), Matchers.any())).thenReturn(Some(testIP16RejectionApplyResponseModel))


    override val responseConstructors =  mock[ResponseConstructors]
    when(responseConstructors.createApplyResponseModelFromJson(Matchers.any())(Matchers.any())).thenReturn(Some(testFPRejectionApplyResponseModel))

  }

  object TestMCNeededResultController extends ResultController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val postSignInRedirectUrl = "http://localhost:9012/protect-your-lifetime-allowance/apply-for-fp16"

    implicit val hc: HeaderCarrier = HeaderCarrier()
    override val plaConnector = mock[PLAConnector]
    when(plaConnector.applyFP16(anyString())(Matchers.any())).thenReturn(Future(testMCNeededResponse))
    when(plaConnector.applyIP16(anyString(), Matchers.any())(Matchers.any())).thenReturn(Future(testMCNeededResponse))
    when(plaConnector.applyIP14(anyString(), Matchers.any())(Matchers.any())).thenReturn(Future(testMCNeededResponse))

    override val keyStoreConnector = mock[KeyStoreConnector]
    when(keyStoreConnector.fetchAllUserData(Matchers.any())).thenReturn(Future(Some(CacheMap("tstID", Map.empty[String, JsValue]))))

    override val responseConstructors = mock[ResponseConstructors]

  }

  object TestIncorrectResponseModelResultController extends ResultController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val postSignInRedirectUrl = "http://localhost:9012/protect-your-lifetime-allowance/apply-for-fp16"

    implicit val hc: HeaderCarrier = HeaderCarrier()
    override val plaConnector = mock[PLAConnector]
    when(plaConnector.applyFP16(anyString())(Matchers.any())).thenReturn(Future(testFP16RejectionResponse))
    when(plaConnector.applyIP16(anyString(), Matchers.any())(Matchers.any())).thenReturn(Future(testIP16RejectionResponse))
    when(plaConnector.applyIP14(anyString(), Matchers.any())(Matchers.any())).thenReturn(Future(testIP14RejectionResponse))

    override val keyStoreConnector = mock[KeyStoreConnector]
    when(keyStoreConnector.fetchAllUserData(Matchers.any())).thenReturn(Future(Some(CacheMap("tstID", Map.empty[String, JsValue]))))

    override val responseConstructors = mock[ResponseConstructors]
    when(responseConstructors.createApplyResponseModelFromJson(Matchers.any())(Matchers.any())).thenReturn(None)

  }

  ///////////////////////////////////////////////
  // Initial Setup
  ///////////////////////////////////////////////
  "ResultController should be correctly initialised" in {
    ResultController.authConnector shouldBe FrontendAuthConnector
  }

  //////////////////////////////////////////////
  //  POST / REDIRECT
  /////////////////////////////////////////////


  "Successfully applying for FP" should {

    object DataItem extends AuthorisedFakeRequestToPost(TestSuccessResultController.processFPApplication)
    object GetItem extends AuthorisedFakeRequestTo(TestSuccessResultController.displayResult(ApplicationType.FP2016))

    "return 303" in {status(DataItem.result) shouldBe 303}
    "redirect to the result success page" in {redirectLocation(DataItem.result) shouldBe Some(s"${routes.ResultController.displayFP16()}")}
    "return 200" in {status(GetItem.result) shouldBe 200}
    "take the user to the result success page" in {GetItem.jsoupDoc.title shouldEqual Messages("pla.resultSuccess.title")}
  }

  "Unsuccessfully applying for FP" should {

    object DataItem extends AuthorisedFakeRequestToPost(TestRejectResultController.processFPApplication)
    object GetItem extends AuthorisedFakeRequestTo(TestRejectResultController.displayResult(ApplicationType.FP2016))

    "return 303" in { status(DataItem.result) shouldBe 303 }
    "redirect the user to the result rejection page" in {redirectLocation(DataItem.result) shouldBe Some(s"${routes.ResultController.displayFP16()}")}
    "return 200" in {status(GetItem.result) shouldBe 200}
    "take the user to the result rejection page" in {GetItem.jsoupDoc.title shouldEqual Messages("pla.resultRejection.title")}
  }

  "Successfully applying for IP 2016" should {

    object DataItem extends AuthorisedFakeRequestToPost(TestSuccessResultController.processIPApplication)
    object GetItem extends AuthorisedFakeRequestTo(TestSuccessResultController.displayResult(ApplicationType.IP2016))

    "return 303" in { status(DataItem.result) shouldBe 303 }
    "redirect the user to the result success page" in {redirectLocation(DataItem.result) shouldBe Some(s"${routes.ResultController.displayIP16()}")}
    "return 200" in {status(GetItem.result) shouldBe 200}
    "take the user to the result success page" in {GetItem.jsoupDoc.title shouldEqual Messages("pla.resultSuccess.title")}
  }

  "Unsuccessfully applying for IP 2016" should {

    object DataItem extends AuthorisedFakeRequestToPost(TestRejectResultController.processIPApplication)
    object GetItem extends AuthorisedFakeRequestTo(TestRejectResultController.displayResult(ApplicationType.IP2016))

    "return 303" in { status(DataItem.result) shouldBe 303 }
    "redirect the user to the result rejection page" in {redirectLocation(DataItem.result) shouldBe Some(s"${routes.ResultController.displayIP16()}")}
    "return 200" in {status(GetItem.result) shouldBe 200}
    "take the user to the result rejection page" in {GetItem.jsoupDoc.title shouldEqual Messages("pla.resultRejection.title")}
  }

  "Successfully applying for IP 2014" should {

    object DataItem extends AuthorisedFakeRequestToPost(TestSuccessResultController.processIP14Application)
    object GetItem extends AuthorisedFakeRequestTo(TestSuccessResultController.displayResult(ApplicationType.IP2014))

    "return 303" in { status(DataItem.result) shouldBe 303 }
    "redirect the user to the result success page" in {redirectLocation(DataItem.result) shouldBe Some(s"${routes.ResultController.displayIP14()}")}
    "return 200" in {status(GetItem.result) shouldBe 200}
    "take the user to the result success page" in {GetItem.jsoupDoc.title shouldEqual Messages("pla.resultSuccess.title")}
  }

  "Unsuccessfully applying for IP 2014" should {

    object DataItem extends AuthorisedFakeRequestToPost(TestRejectResultController.processIP14Application)
    object GetItem extends AuthorisedFakeRequestTo(TestRejectResultController.displayResult(ApplicationType.IP2014))

    "return 303" in {
      status(DataItem.result) shouldBe 303
    }
    "redirect the user to the result rejection page" in {
      redirectLocation(DataItem.result) shouldBe Some(s"${routes.ResultController.displayIP14()}")
    }
    "return 200" in {
      status(GetItem.result) shouldBe 200
    }
    "take the user to the result rejection page" in {GetItem.jsoupDoc.title shouldEqual Messages("pla.resultRejection.title")}
  }

  "Applying for IP14 when Manual Correspondence is needed" should {
    object DataItem extends AuthorisedFakeRequestToPost(TestMCNeededResultController.processIP14Application)
    "return 423 (Locked)" in { status(DataItem.result) shouldBe 423 }
    "take the user to the MC needed page" in {DataItem.jsoupDoc.title shouldEqual Messages("pla.mcNeeded.title")}
  }

  "Applying for IP16 when Manual Correspondence is needed" should {
    object DataItem extends AuthorisedFakeRequestToPost(TestMCNeededResultController.processIPApplication)
    "return 423 (Locked)" in { status(DataItem.result) shouldBe 423 }
    "take the user to the MC needed page" in {DataItem.jsoupDoc.title shouldEqual Messages("pla.mcNeeded.title")}
  }

  "Applying for FP16 when Manual Correspondence is needed" should {
    object DataItem extends AuthorisedFakeRequestToPost(TestMCNeededResultController.processFPApplication)
    "return 423 (Locked)" in { status(DataItem.result) shouldBe 423 }
    "take the user to the MC needed page" in {DataItem.jsoupDoc.title shouldEqual Messages("pla.mcNeeded.title")}
  }

 "Failure to create an ApplyResponse model from an application response" should {
   object DataItem extends AuthorisedFakeRequestToPost(TestIncorrectResponseModelResultController.processFPApplication)
   "return 500" in {
     status(DataItem.result) shouldBe 500
   }
   "return \"no-cache\" in the response header" in {
     DataItem.result.header.headers.head._2 shouldBe "no-cache"
   }
 }

  "Applying for inactive IP2016 protection" should {
    object DataItem extends AuthorisedFakeRequestToPost(TestInactiveSuccessResultController.processIPApplication)
    object GetItem extends AuthorisedFakeRequestTo(TestInactiveSuccessResultController.displayResult(ApplicationType.IP2016))

    "return 303" in {
      status(DataItem.result) shouldBe 303
    }
    "redirect the user to the result inactive success page" in {
      redirectLocation(DataItem.result) shouldBe Some(s"${routes.ResultController.displayIP16()}")
    }
    "return 200" in {
      status(GetItem.result) shouldBe 200
    }
    "take the user to the result inactive success page" in {
      GetItem.jsoupDoc.title shouldEqual Messages("pla.resultSuccess.title")
    }
  }

  "Application outcome for IP14" should {
    implicit val protectionType = ApplicationType.IP2014
    "return Rejected for rejection codes" in {
      val rejectionCodes = List(1,2,25,26,27,28,29)
      for(code <- rejectionCodes) {
        TestSuccessResultController.applicationOutcome(code) shouldBe ApplicationOutcome.Rejected
      }
    }

    "return Successful for successful, active codes" in {
      val successfulActiveCodes = List(3,4,8,33,34)
      for(code <- successfulActiveCodes) {
        TestSuccessResultController.applicationOutcome(code) shouldBe ApplicationOutcome.Successful
      }
    }

    "return SuccessfulInactive for successful, inactive codes" in {
      val successfulInactiveCodes = List(5,6,7,30,31,32)
      for(code <- successfulInactiveCodes) {
        TestSuccessResultController.applicationOutcome(code) shouldBe ApplicationOutcome.SuccessfulInactive
      }
    }
  }

  "Application outcome for IP16" should {
    implicit val protectionType = ApplicationType.IP2016
    "return Rejected for rejection codes" in {
      val rejectionCodes = List(9,10,11,35,36,37,38,39)
      for(code <- rejectionCodes) {
        TestSuccessResultController.applicationOutcome(code) shouldBe ApplicationOutcome.Rejected
      }
    }

    "return Successful for successful, active codes" in {
      val successfulActiveCodes = List(12,44)
      for(code <- successfulActiveCodes) {
        TestSuccessResultController.applicationOutcome(code) shouldBe ApplicationOutcome.Successful
      }
    }

    "return SuccessfulInactive for successful, inactive codes" in {
      val successfulInactiveCodes = List(13,14,15,16,40,41,42,43)
      for(code <- successfulInactiveCodes) {
        TestSuccessResultController.applicationOutcome(code) shouldBe ApplicationOutcome.SuccessfulInactive
      }
    }
  }

  "Application outcome for FP16" should {
    implicit val protectionType = ApplicationType.FP2016
    "return Rejected for rejection codes" in {
      val rejectionCodes = List(17,18,19,20,21)
      for(code <- rejectionCodes) {
        TestSuccessResultController.applicationOutcome(code) shouldBe ApplicationOutcome.Rejected
      }
    }

    "return Successful for successful, active codes" in {
      val successfulActiveCodes = List(22,23)
      for(code <- successfulActiveCodes) {
        TestSuccessResultController.applicationOutcome(code) shouldBe ApplicationOutcome.Successful
      }
    }

    "return SuccessfulInactive for successful, inactive codes" in {
      val successfulInactiveCodes = List(24)
      for(code <- successfulInactiveCodes) {
        TestSuccessResultController.applicationOutcome(code) shouldBe ApplicationOutcome.SuccessfulInactive
      }
    }
  }

}
