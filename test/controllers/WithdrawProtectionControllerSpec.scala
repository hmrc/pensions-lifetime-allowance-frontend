/*
 * Copyright 2019 HM Revenue & Customs
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
import akka.stream.{ActorMaterializer, Materializer}
import config.LocalTemplateRenderer
import config.wiring.PlaFormPartialRetriever
import connectors.{KeyStoreConnector, PLAConnector}
import constructors.DisplayConstructors
import mocks.AuthMock
import models._
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.http.HeaderNames.CACHE_CONTROL
import play.api.i18n.Messages
import play.api.libs.json.{JsString, JsValue, Json, OFormat}
import play.api.mvc.{Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import testHelpers.{AuthorisedFakeRequestToPost, MockTemplateRenderer}
import uk.gov.hmrc.auth.core.retrieve.Retrievals
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class WithdrawProtectionControllerSpec extends UnitSpec with MockitoSugar with AuthMock with WithFakeApplication {

  val keyStoreConnector = mock[KeyStoreConnector]
  val plaConnector = mock[PLAConnector]

  implicit val templateRenderer: LocalTemplateRenderer = MockTemplateRenderer.renderer
  implicit val partialRetriever: PlaFormPartialRetriever = mock[PlaFormPartialRetriever]
  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: Materializer = ActorMaterializer()

  val mockDisplayConstructors = mock[DisplayConstructors]

  val controller = new WithdrawProtectionController(keyStoreConnector, plaConnector, partialRetriever, templateRenderer ) {
    override lazy val authConnector = mockAuthConnector
    override lazy val postSignInRedirectUrl = "http://localhost:9012/protect-your-lifetime-allowance/apply-ip"
    override val displayConstructors = mockDisplayConstructors
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

  val withdrawDateForm = WithdrawDateFormModel(
    withdrawDay = Some(5),
    withdrawMonth = Some(9),
    withdrawYear = Some(2017)
  )

  val invalidWithdrawDateForm = WithdrawDateFormModel(
    withdrawDay = Some(3),
    withdrawMonth = Some(9),
    withdrawYear = Some(2016)
  )

  val tstPensionContributionNoPsoDisplaySections = Seq(

    AmendDisplaySectionModel("PensionsTakenBefore", Seq(
      AmendDisplayRowModel("YesNo", Some(controllers.routes.AmendsController.amendPensionsTakenBefore("ip2014", "active")), None, "No"))
    ),
    AmendDisplaySectionModel("PensionsTakenBetween", Seq(
      AmendDisplayRowModel("YesNo", Some(controllers.routes.AmendsController.amendPensionsTakenBetween("ip2014", "active")), None, "No"))
    ),
    AmendDisplaySectionModel("OverseasPensions", Seq(
      AmendDisplayRowModel("YesNo", Some(controllers.routes.AmendsController.amendOverseasPensions("ip2014", "active")), None, "Yes"),
      AmendDisplayRowModel("Amt", Some(controllers.routes.AmendsController.amendOverseasPensions("ip2014", "active")), None, "£100,000"))
    ),
    AmendDisplaySectionModel("CurrentPensions", Seq(
      AmendDisplayRowModel("Amt", Some(controllers.routes.AmendsController.amendCurrentPensions("ip2014", "active")), None, "£1,000,000"))
    ),
    AmendDisplaySectionModel("CurrentPsos", Seq(
      AmendDisplayRowModel("YesNo", None, None, "No")))
  )

  val tstAmendDisplayModel = AmendDisplayModel("IP2014", amended = true,
    tstPensionContributionNoPsoDisplaySections,
    psoAdded = false, Seq(), "£1,100,000"
  )

  val fakeRequest = FakeRequest()

  "In WithdrawProtectionController calling the showWithdrawConfirmation action" should {

    "return 200" in {
      mockAuthConnector(Future.successful())
      when(keyStoreConnector.remove(ArgumentMatchers.any()))
        .thenReturn(Future.successful(mock[HttpResponse]))
      status(controller.showWithdrawConfirmation("")(fakeRequest)) shouldBe 200
    }
  }

  "In WithdrawProtectionController calling the displayWithdrawConfirmation action" when {

    "handling an OK response" should  {
      lazy val result = {
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        keystoreFetchCondition[ProtectionModel](Some(ip2016Protection))
        when(plaConnector.amendProtection(ArgumentMatchers.anyString(), ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future.successful(HttpResponse(OK)))
        await(controller.displayWithdrawConfirmation("")(fakeRequest))
      }

      "return 303" in {
        status(result) shouldBe SEE_OTHER
      }
    }

    "handling a non-OK response" should {
      lazy val result = {
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        keystoreFetchCondition[ProtectionModel](Some(ip2016Protection))
        when(plaConnector.amendProtection(ArgumentMatchers.anyString(), ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR)))
        await(controller.displayWithdrawConfirmation("")(fakeRequest))
      }

      "return 500" in {
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "have the correct cache control" in {
        result.header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache"
      }
    }
  }

  "In WithdrawProtectionController calling the withdrawImplications action" when {

    "there is no stored protection model" should {
      lazy val result = {
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        keystoreFetchCondition[ProtectionModel](None)
        await(controller.withdrawImplications(fakeRequest))
      }

      "return 500" in {
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "have the correct cache control" in {
        result.header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache"
      }
    }

    "there is a stored protection model" should {

      "return 200" in {
        keystoreFetchCondition[ProtectionModel](Some(ip2016Protection))
        when(mockDisplayConstructors.createWithdrawSummaryTable(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(tstAmendDisplayModel)

        val result = await(controller.withdrawImplications(fakeRequest))
        status(result) shouldBe OK
      }
    }
  }


  "In WithdrawProtectionController calling the withdrawSummary action" when {

    "there is no stored protection model" should {
      lazy val result = {
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        keystoreFetchCondition[ProtectionModel](None)
        await(controller.withdrawSummary(fakeRequest))
      }

      "return 500" in {
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
      "have the correct cache control" in {
        result.header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache"
      }
    }

    "there is a stored protection model" should {
      "return 200" in {
        keystoreFetchCondition[ProtectionModel](Some(ip2016Protection))
        when(mockDisplayConstructors.createWithdrawSummaryTable(ArgumentMatchers.any())(ArgumentMatchers.any())).thenReturn(tstAmendDisplayModel)

        val result = await(controller.withdrawSummary(fakeRequest))
        status(result) shouldBe OK
      }
    }
  }

  "In WithdrawProtectionController calling the getWithdrawDateInput action" when {

    "there is no stored protection model" should {
      keystoreFetchCondition[ProtectionModel](None)
      lazy val result = await(controller.withdrawSummary(fakeRequest))

      "return 500" in {
        keystoreFetchCondition[ProtectionModel](None)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
      "have the correct cache control" in {
        result.header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache"
      }
    }

    "there is a stored protection model" should {

      lazy val result = await(controller.getWithdrawDateInput(fakeRequest))

      "return 200" in {
        keystoreFetchCondition[ProtectionModel](Some(ip2016Protection))
        status(result) shouldBe OK
      }
    }
  }

  "In WithdrawProtectionController calling the postWithdrawDateInput action" when {
    val testController = new WithdrawProtectionController(keyStoreConnector, plaConnector, partialRetriever, templateRenderer ) {
      override lazy val authConnector = mockAuthConnector
      override lazy val postSignInRedirectUrl = "http://localhost:9012/protect-your-lifetime-allowance/apply-ip"
      override val displayConstructors = mockDisplayConstructors

      override def validateAndSaveWithdrawDateForm(protection: ProtectionModel)(implicit request: Request[_]): Future[Result] = {
        Future.successful(Redirect(routes.WithdrawProtectionController.getSubmitWithdrawDateInput()))
      }
    }

    "there is no stored protection model" should {
      keystoreFetchCondition[ProtectionModel](None)
      lazy val result = await(testController.postWithdrawDateInput(fakeRequest))

      "return 500" in {
        keystoreFetchCondition[ProtectionModel](None)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "there is a stored protection model" should {
      "return 303" in {
        keystoreFetchCondition[ProtectionModel](Some(ip2016Protection))
        lazy val result = await(testController.postWithdrawDateInput(fakeRequest))

        status(result) shouldBe SEE_OTHER
      }
    }
  }

  "In WithdrawProtectionController calling the getSubmitWithdrawDateInput action" when {
    val testController = new WithdrawProtectionController(keyStoreConnector, plaConnector, partialRetriever, templateRenderer ) {
      override lazy val authConnector = mockAuthConnector
      override lazy val postSignInRedirectUrl = "http://localhost:9012/protect-your-lifetime-allowance/apply-ip"
      override val displayConstructors = mockDisplayConstructors

      override def fetchWithdrawDateForm(protection: ProtectionModel)(implicit request: Request[_]): Future[Result] = {
        Ok("test")
      }
    }

    "there is a stored protection Model" should {
      "return a 200" in {
        keystoreFetchCondition[ProtectionModel](Some(ip2016Protection))
        lazy val result = await(testController.getSubmitWithdrawDateInput(fakeRequest))
        status(result) shouldBe OK
      }
    }

    "there is no stored protection model" should {
      "return a 500" in {
        keystoreFetchCondition[ProtectionModel](None)
        lazy val result = await(testController.getSubmitWithdrawDateInput(fakeRequest))
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "In withdrawProtectionController calling the submitWithdrawDateInput action" when {

    "there is a stored protection model" should {
      object UserRequest extends AuthorisedFakeRequestToPost(controller.submitWithdrawDateInput,
        ("withdrawDay", "20"), ("withdrawMonth", "7"), ("withdrawYear", "2017"))

      "return 200" in {
        keystoreFetchCondition[ProtectionModel](Some(ip2016Protection))
        status(UserRequest.result) shouldBe OK
      }

      object InvalidDayRequest extends AuthorisedFakeRequestToPost(controller.submitWithdrawDateInput,
        ("withdrawDay", "20000"), ("withdrawMonth", "10"), ("withdrawYear", "2017"))
      "return 400 Bad Request" in {
        keystoreFetchCondition[ProtectionModel](Some(ip2016Protection))
        status(InvalidDayRequest.result) shouldBe BAD_REQUEST
      }

      object InvalidMultipleErrorRequest extends AuthorisedFakeRequestToPost(controller.submitWithdrawDateInput,
        ("withdrawDay", "20000"), ("withdrawMonth", "70000"), ("withdrawYear", "2010000007"))
      "return 400 Bad Request with single error" in {
        keystoreFetchCondition[ProtectionModel](Some(ip2016Protection))
        status(InvalidDayRequest.result) shouldBe BAD_REQUEST
      }

      object BadRequestDateInPast extends AuthorisedFakeRequestToPost(controller.submitWithdrawDateInput,
        ("withdrawDay", "20"), ("withdrawMonth", "1"), ("withdrawYear", "2012"))
      "return 400 Bad Request with date in past error" in {
        keystoreFetchCondition[ProtectionModel](Some(ip2016Protection))
        status(InvalidDayRequest.result) shouldBe BAD_REQUEST
      }
    }
  }

  "In WithdrawProtectionController calling the validateAndSaveWithdrawDateForm action" when {
    "the form has errors" should {
      "return a 400" in {
        val requestWithForm = FakeRequest().withBody(Json.toJson(invalidWithdrawDateForm))
        lazy val result = await(controller.validateAndSaveWithdrawDateForm(ip2016Protection)(requestWithForm))
        status(result) shouldBe BAD_REQUEST
      }
    }

    "the form does not have errors" should {
      "return a 303" in {
        when(keyStoreConnector.saveFormData[WithdrawDateFormModel](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(CacheMap("test", Map.empty)))

        val requestWithFormInvalid = FakeRequest().withBody(Json.toJson(withdrawDateForm))
        lazy val result = await(controller.validateAndSaveWithdrawDateForm(ip2016Protection)(requestWithFormInvalid))

        status(result) shouldBe SEE_OTHER
        result.header.headers("Location") shouldBe  "/protect-your-lifetime-allowance/withdraw-protection/date-input-confirmation"

      }
    }
  }

  "In WithdrawProtectionController calling the fetchWithdrawDateForm action" when {
    "there is a stored withdrawDateForm" should {
      "return a 400" in {
        keystoreFetchCondition[WithdrawDateFormModel](None)
        lazy val result = await(controller.fetchWithdrawDateForm(ip2016Protection)(fakeRequest))
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "there is no stored withdrawDateForm" should {
      "return a 200" in {
        keystoreFetchCondition[WithdrawDateFormModel](Some(withdrawDateForm))
        lazy val result = await(controller.fetchWithdrawDateForm(ip2016Protection)(fakeRequest))
        status(result) shouldBe OK
      }
    }
  }

  def keystoreFetchCondition[T](data: Option[T]): Unit = {
    when(keyStoreConnector.fetchAndGetFormData[T](ArgumentMatchers.anyString())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(data))
  }
}
