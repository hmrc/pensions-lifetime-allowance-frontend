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

import auth.MockAuthConnector
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
import models.SuccessResponseModel
import play.api.mvc.{Action, AnyContent, Result}
import play.api.test.Helpers._

import scala.concurrent.Future


class ResultControllerSpec extends UnitSpec with MockitoSugar with WithFakeApplication {

  val successFP16Json = Json.parse("""{"certificateDate":"2016-05-10T17:20:55.138","nino":"AA123456A","notificationId":24,"protectionID":8243168284792526522,"protectionReference":"FP16138722390C","protectionType":"FP2016","status":"Open","version":1}""")
  val rejectionFP16Json = Json.parse("""{"nino":"AA123456A","notificationId":21,"protectionID":-4645895724767334826,"protectionType":"FP2016","status":"Rejected","version":1}""")

  val successIP16Json = Json.parse("""{"notificationId":12,"protectedAmount":1230000.0}""")
  val rejectionIP16Json = Json.parse("""{"notificationId":9}""")

  val successIP14Json = Json.parse("""{"notificationId":3,"protectedAmount":1230000.0}""")
  val rejectionIP14Json = Json.parse("""{"notificationId":1}""")

  val testFP16SuccessResponse = HttpResponse(200,Some(successFP16Json))
  val testFP16RejectionResponse = HttpResponse(409,Some(rejectionFP16Json))
  val testIP16SuccessResponse = HttpResponse(200,Some(successIP16Json))
  val testIP16RejectionResponse = HttpResponse(409,Some(rejectionIP16Json))
  val testIP14SuccessResponse = HttpResponse(200,Some(successIP14Json))
  val testIP14RejectionResponse = HttpResponse(409,Some(rejectionIP14Json))
  val testMCNeededResponse = HttpResponse(423)

  object TestSuccessResultController extends ResultController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val postSignInRedirectUrl = "http://localhost:9012/protect-your-lifetime-allowance/apply-for-fp16"

    implicit val hc: HeaderCarrier = HeaderCarrier()

    override val plaConnector = mock[PLAConnector]
    when(plaConnector.applyFP16(anyString())(Matchers.any())).thenReturn(Future(testFP16SuccessResponse))
    when(plaConnector.applyIP16(anyString(), Matchers.any())(Matchers.any())).thenReturn(Future(testIP16SuccessResponse))
    when(plaConnector.applyIP14(anyString(), Matchers.any())(Matchers.any())).thenReturn(Future(testIP14SuccessResponse))


    override val keyStoreConnector = mock[KeyStoreConnector]
    when(keyStoreConnector.fetchAllUserData(Matchers.any())).thenReturn(Future(Some(CacheMap("tstID", Map.empty[String, JsValue]))))
  }

  object TestRejectResultController extends ResultController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val postSignInRedirectUrl = "http://localhost:9012/protect-your-lifetime-allowance/apply-for-fp16"

    implicit val hc: HeaderCarrier = HeaderCarrier()
    override val plaConnector = mock[PLAConnector]
    when(plaConnector.applyFP16(anyString())(Matchers.any())).thenReturn(Future(testFP16RejectionResponse))
    when(plaConnector.applyIP16(anyString(), Matchers.any())(Matchers.any())).thenReturn(Future(testIP16RejectionResponse))
    when(plaConnector.applyIP14(anyString(), Matchers.any())(Matchers.any())).thenReturn(Future(testIP14RejectionResponse))

    override val keyStoreConnector = mock[KeyStoreConnector]
    when(keyStoreConnector.fetchAllUserData(Matchers.any())).thenReturn(Future(Some(CacheMap("tstID", Map.empty[String, JsValue]))))
  }

  object TestMCNeededResultController extends ResultController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val postSignInRedirectUrl = "http://localhost:9012/protect-your-lifetime-allowance/apply-for-fp16"

    implicit val hc: HeaderCarrier = HeaderCarrier()
    override val plaConnector = mock[PLAConnector]
    when(plaConnector.applyFP16(anyString())(Matchers.any())).thenReturn(Future(testMCNeededResponse))
    when(plaConnector.applyIP16(anyString(), Matchers.any())(Matchers.any())).thenReturn(Future(testMCNeededResponse))
    when(plaConnector.applyIP14(anyString(), Matchers.any())(Matchers.any())).thenReturn(Future(testMCNeededResponse))

    override val keyStoreConnector = mock[KeyStoreConnector]
    when(keyStoreConnector.fetchAllUserData(Matchers.any())).thenReturn(Future(Some(CacheMap("tstID", Map.empty[String, JsValue]))))
  }

  ///////////////////////////////////////////////
  // Initial Setup
  ///////////////////////////////////////////////
  "ResultController should be correctly initialised" in {
    ResultController.authConnector shouldBe FrontendAuthConnector
  }

  "Successfully applying for FP" should {

    object DataItem extends AuthorisedFakeRequestToPost(TestSuccessResultController.processFPApplication)

    "return 303" in {status(DataItem.result) shouldBe 303}
    "redirect to the result success page" in {redirectLocation(DataItem.result) shouldBe Some(s"${routes.ResultController.displayFP16()}")}
  }

  "Unsuccessfully applying for FP" should {

    object DataItem extends AuthorisedFakeRequestToPost(TestRejectResultController.processFPApplication)
    "return 303" in { status(DataItem.result) shouldBe 303 }
    "redirect the user to the result rejection page" in {redirectLocation(DataItem.result) shouldBe Some(s"${routes.ResultController.displayFP16()}")}
      //{DataItem.jsoupDoc.title shouldEqual Messages("pla.resultRejection.title")}
  }

  "Successfully applying for IP 2016" should {

    object DataItem extends AuthorisedFakeRequestToPost(TestSuccessResultController.processIPApplication)
    "return 303" in { status(DataItem.result) shouldBe 303 }
    "redirect the user to the result success page" in {redirectLocation(DataItem.result) shouldBe Some(s"${routes.ResultController.displayIP16()}")}
      //{DataItem.jsoupDoc.title shouldEqual Messages("pla.resultSuccess.title")}
  }

  "Unsuccessfully applying for IP 2016" should {

    object DataItem extends AuthorisedFakeRequestToPost(TestRejectResultController.processIPApplication)
    "return 303" in { status(DataItem.result) shouldBe 303 }
    "redirect the user to the result rejection page" in {redirectLocation(DataItem.result) shouldBe Some(s"${routes.ResultController.displayIP16()}")}
      //{DataItem.jsoupDoc.title shouldEqual Messages("pla.resultRejection.title")}
  }

  "Successfully applying for IP 2014" should {

    object DataItem extends AuthorisedFakeRequestToPost(TestSuccessResultController.processIP14Application)
    "return 303" in { status(DataItem.result) shouldBe 303 }
    "take the user to the result success page" in {redirectLocation(DataItem.result) shouldBe Some(s"${routes.ResultController.displayIP14()}")}
      //{DataItem.jsoupDoc.title shouldEqual Messages("pla.resultSuccess.title")}
  }

  "Unsuccessfully applying for IP 2014" should {

    object DataItem extends AuthorisedFakeRequestToPost(TestRejectResultController.processIP14Application)
    "return 303" in { status(DataItem.result) shouldBe 303 }
    "take the user to the result rejection page" in {redirectLocation(DataItem.result) shouldBe Some(s"${routes.ResultController.displayIP14()}")}
      //{DataItem.jsoupDoc.title shouldEqual Messages("pla.resultRejection.title")}
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


}
