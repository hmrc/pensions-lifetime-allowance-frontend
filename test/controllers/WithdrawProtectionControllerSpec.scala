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

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import auth.MockConfig
import com.kenshoo.play.metrics.PlayModule
import connectors.{KeyStoreConnector, PLAConnector}
import constructors.DisplayConstructors
import models._
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import mocks.AuthMock
import org.scalatest.mock.MockitoSugar
import play.api.{Configuration, Environment}
import play.api.Play.current
import play.api.http.HeaderNames.CACHE_CONTROL
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.FakeRequest
import testHelpers.{AuthorisedFakeRequestTo, AuthorisedFakeRequestToPost, MockTemplateRenderer}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.PlayAuthConnector
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, Retrievals}
import uk.gov.hmrc.renderer.TemplateRenderer

import scala.concurrent.Future

class WithdrawProtectionControllerSpec extends UnitSpec with WithFakeApplication with MockitoSugar with AuthMock {

  override def bindModules = Seq(new PlayModule)

  val mockKeyStoreConnector = mock[KeyStoreConnector]
  val mockDisplayConstructors = mock[DisplayConstructors]
  val mockPLAConnector = mock[PLAConnector]
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  object TestWithdrawController extends WithdrawProtectionController {
    lazy val appConfig = MockConfig
    override lazy val authConnector = mockAuthConnector
    lazy val postSignInRedirectUrl = "http://localhost:9012/protect-your-lifetime-allowance/apply-ip"

    override val displayConstructors: DisplayConstructors = mockDisplayConstructors
    override val keyStoreConnector: KeyStoreConnector = mockKeyStoreConnector
    override val plaConnector: PLAConnector = mockPLAConnector

    override def config: Configuration = mock[Configuration]

    override def env: Environment = mock[Environment]
    override implicit val templateRenderer: TemplateRenderer = MockTemplateRenderer

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


  "In WithdrawProtectionController calling the withdrawSummary action" when {

    "there is no stored protection model" should {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      keystoreFetchCondition[ProtectionModel](None)
      lazy val result = await(TestWithdrawController.withdrawSummary(fakeRequest))
      lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

      "return 500" in {
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
      "have the correct cache control" in {
        result.header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache"
      }
    }

    "there is a stored protection model" should {

      lazy val result = await(TestWithdrawController.withdrawSummary(fakeRequest))
      lazy val jsoupDoc = Jsoup.parse(bodyOf(result))
      "return 200" in {
        keystoreFetchCondition[ProtectionModel](Some(ip2016Protection))
        when(mockDisplayConstructors.createWithdrawSummaryTable(Matchers.any())(Matchers.any())).thenReturn(tstAmendDisplayModel)
        status(result) shouldBe OK
      }
    }
  }

  "In WithdrawProtectionController calling the withdrawDateInput action" when {

    "there is no stored protection model" should {
      keystoreFetchCondition[ProtectionModel](None)
      lazy val result = await(TestWithdrawController.withdrawSummary(fakeRequest))
      lazy val jsoupDoc = Jsoup.parse(bodyOf(result))
      "return 500" in {
        keystoreFetchCondition[ProtectionModel](None)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
      "have the correct cache control" in {
        result.header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache"
      }
    }

    "there is a stored protection model" should {

      lazy val result = await(TestWithdrawController.withdrawDateInput(fakeRequest))
      lazy val jsoupDoc = Jsoup.parse(bodyOf(result))
      "return 200" in {
        keystoreFetchCondition[ProtectionModel](Some(ip2016Protection))
        status(result) shouldBe OK
        jsoupDoc.body().getElementsByTag("h1").text should include(Messages("pla.withdraw.date-input.title"))
      }
    }
  }

  "In withdrawProtectionController calling the submitWithdrawDateInput action" when {
    "there is a stored protection model" should {
      object UserRequest extends AuthorisedFakeRequestToPost(TestWithdrawController.submitWithdrawDateInput,
        ("withdrawDay", "20"), ("withdrawMonth", "7"), ("withdrawYear", "2017"))

      "return 200" in {
        keystoreFetchCondition[ProtectionModel](Some(ip2016Protection))
        status(UserRequest.result) shouldBe OK
      }

      object InvalidDayRequest extends AuthorisedFakeRequestToPost(TestWithdrawController.submitWithdrawDateInput,
        ("withdrawDay", "20000"), ("withdrawMonth", "10"), ("withdrawYear", "2017"))
      "return 400 Bad Request" in {
        keystoreFetchCondition[ProtectionModel](Some(ip2016Protection))
        status(InvalidDayRequest.result) shouldBe BAD_REQUEST
        InvalidDayRequest.jsoupDoc.body().getElementsByTag("li").text should include(Messages("pla.withdraw.date-input.form.day-too-high"))
      }

      object InvalidMultipleErrorRequest extends AuthorisedFakeRequestToPost(TestWithdrawController.submitWithdrawDateInput,
        ("withdrawDay", "20000"), ("withdrawMonth", "70000"), ("withdrawYear", "2010000007"))
      "return 400 Bad Request with single error" in {
        keystoreFetchCondition[ProtectionModel](Some(ip2016Protection))
        status(InvalidDayRequest.result) shouldBe BAD_REQUEST
        InvalidMultipleErrorRequest.jsoupDoc.body().getElementsByTag("li").text should include(Messages("pla.withdraw.date-input-form.date-invalid"))
      }

      object BadRequestDateInPast extends AuthorisedFakeRequestToPost(TestWithdrawController.submitWithdrawDateInput,
        ("withdrawDay", "20"), ("withdrawMonth", "1"), ("withdrawYear", "2012"))
      "return 400 Bad Request with date in past error" in {
        keystoreFetchCondition[ProtectionModel](Some(ip2016Protection))
        status(InvalidDayRequest.result) shouldBe BAD_REQUEST
        BadRequestDateInPast.jsoupDoc.body().getElementsByTag("li").text should include(Messages("pla.withdraw.date-input.form.date-before-start-date"))
      }
    }
  }

  def keystoreFetchCondition[T](data: Option[T]): Unit = {
    when(mockKeyStoreConnector.fetchAndGetFormData[T](Matchers.anyString())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(data))
  }
}
