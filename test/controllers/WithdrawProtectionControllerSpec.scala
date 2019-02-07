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
import play.api.test.FakeRequest
import play.api.test.Helpers._
import testHelpers.{AuthorisedFakeRequestToPost, MockTemplateRenderer}
import uk.gov.hmrc.auth.core.retrieve.Retrievals
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class WithdrawProtectionControllerSpec extends UnitSpec with MockitoSugar with AuthMock with WithFakeApplication {

  val keyStoreConnector = mock[KeyStoreConnector]
  val plaConnector = mock[PLAConnector]

  implicit val templateRenderer: LocalTemplateRenderer = MockTemplateRenderer.renderer
  implicit val partialRetriever: PlaFormPartialRetriever = mock[PlaFormPartialRetriever]

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

  "In WithdrawProtectionController calling the withdrawDateInput action" when {

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

      lazy val result = await(controller.withdrawDateInput(fakeRequest))

      "return 200" in {
        keystoreFetchCondition[ProtectionModel](Some(ip2016Protection))
        status(result) shouldBe OK
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

  def keystoreFetchCondition[T](data: Option[T]): Unit = {
    when(keyStoreConnector.fetchAndGetFormData[T](ArgumentMatchers.anyString())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(data))
  }
}
