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

import auth.{AuthFunction, AuthFunctionImpl}
import common.Strings
import config._
import mocks.AuthMock
import models._
import models.amendModels._
import models.pla.response.ProtectionStatus.Dormant
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.Environment
import play.api.i18n.{I18nSupport, Lang, Messages, MessagesApi}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionCacheService
import testHelpers._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import views.html.pages.amends._
import views.html.pages.fallback.technicalError

import scala.concurrent.{ExecutionContext, Future}

class AmendsCurrentPensionControllerSpec
    extends FakeApplication
    with MockitoSugar
    with SessionCacheTestHelper
    with BeforeAndAfterEach
    with AuthMock
    with I18nSupport {

  implicit lazy val mockMessage: Messages =
    fakeApplication().injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
  val mockMCC: MessagesControllerComponents        = fakeApplication().injector.instanceOf[MessagesControllerComponents]
  val mockAuthFunction: AuthFunction               = mock[AuthFunction]
  val mockTechnicalError: technicalError           = app.injector.instanceOf[technicalError]
  val mockAmendCurrentPensions: amendCurrentPensions         = app.injector.instanceOf[amendCurrentPensions]
  val mockAmendIP14CurrentPensions: amendIP14CurrentPensions = app.injector.instanceOf[amendIP14CurrentPensions]
  val mockEnv: Environment                                   = mock[Environment]
  val messagesApi: MessagesApi                               = mockMCC.messagesApi

  implicit val mockAppConfig: FrontendAppConfig = fakeApplication().injector.instanceOf[FrontendAppConfig]
  implicit val mockPlaContext: PlaContext       = mock[PlaContext]
  implicit val system: ActorSystem              = ActorSystem()
  implicit val materializer: Materializer       = mock[Materializer]
  implicit val mockLang: Lang                   = mock[Lang]
  implicit val formWithCSRF: FormWithCSRF       = app.injector.instanceOf[FormWithCSRF]
  implicit val ec: ExecutionContext             = app.injector.instanceOf[ExecutionContext]

  override def beforeEach(): Unit = {
    reset(mockSessionCacheService)
    reset(mockAuthConnector)
    reset(mockEnv)
    super.beforeEach()
  }

  val testIP16DormantModel = AmendProtectionModel(
    ProtectionModel(None, None),
    ProtectionModel(
      None,
      None,
      protectionType = Some("IP2016"),
      status = Some(Dormant.toString),
      relevantAmount = Some(100000),
      uncrystallisedRights = Some(100000)
    )
  )

  class Setup {

    val authFunction = new AuthFunctionImpl(
      mockMCC,
      mockAuthConnector,
      mockTechnicalError
    )

    val controller = new AmendsCurrentPensionController(
      mockSessionCacheService,
      mockMCC,
      authFunction,
      mockTechnicalError,
      mockAmendCurrentPensions,
      mockAmendIP14CurrentPensions
    )

  }

  implicit val fakeRequest: FakeRequest[AnyContent] = FakeRequest()

  val ip2014Protection = ProtectionModel(
    psaCheckReference = Some("testPSARef"),
    uncrystallisedRights = Some(100000.00),
    nonUKRights = Some(2000.00),
    preADayPensionInPayment = Some(2000.00),
    postADayBenefitCrystallisationEvents = Some(2000.00),
    notificationId = Some(12),
    protectionID = Some(12345),
    protectionType = Some("IP2014"),
    status = Some(Dormant.toString),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456")
  )

  val testAmendIP2014ProtectionModel = AmendProtectionModel(ip2014Protection, ip2014Protection)

  def cacheFetchCondition[T](data: Option[T]): Unit =
    when(mockSessionCacheService.fetchAndGetFormData[T](anyString())(any(), any()))
      .thenReturn(Future.successful(data))

  "Calling the .amendCurrentPensions action" when {

    "not supplied with a stored model" in new Setup {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](None)

      val result =
        controller.amendCurrentPensions(Strings.ProtectionTypeURL.IndividualProtection2016, "open")(fakeRequest)
      status(result) shouldBe 500
    }

    "supplied with a stored test model (£100000, IP2016, dormant)" in new Setup {
      val testModel = new AmendProtectionModel(
        ProtectionModel(None, None),
        ProtectionModel(None, None, uncrystallisedRights = Some(100000))
      )
      lazy val result =
        controller.amendCurrentPensions(Strings.ProtectionTypeURL.IndividualProtection2016, "dormant")(fakeRequest)
      lazy val jsoupDoc = Jsoup.parse(contentAsString(result))

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](Some(testModel))
      status(result) shouldBe 200

      cacheFetchCondition[AmendProtectionModel](Some(testModel))
      jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.currentPensions.title")
    }

    "return some HTML that" should {

      "contain some text and use the character set utf-8" in new Setup {
        val testModel = new AmendProtectionModel(
          ProtectionModel(None, None),
          ProtectionModel(None, None, uncrystallisedRights = Some(100000))
        )
        lazy val result =
          controller.amendCurrentPensions(Strings.ProtectionTypeURL.IndividualProtection2016, "dormant")(fakeRequest)
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        cacheFetchCondition[AmendProtectionModel](Some(testModel))

        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }

      "have the value 100000 completed in the amount input by default" in new Setup {
        lazy val result =
          controller.amendCurrentPensions(Strings.ProtectionTypeURL.IndividualProtection2016, "dormant")(fakeRequest)
        lazy val jsoupDoc = Jsoup.parse(contentAsString(result))
        val testModel = new AmendProtectionModel(
          ProtectionModel(None, None),
          ProtectionModel(None, None, uncrystallisedRights = Some(100000))
        )

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        cacheFetchCondition[AmendProtectionModel](Some(testModel))

        jsoupDoc.body.getElementById("amendedUKPensionAmt").attr("value") shouldBe "100000"
      }
    }
  }

  "supplied with a stored test model (£100000, IP2014, dormant)" in new Setup {
    lazy val result =
      controller.amendCurrentPensions(Strings.ProtectionTypeURL.IndividualProtection2014, "dormant")(fakeRequest)
    mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
    cacheFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))

    status(result) shouldBe 200
  }

  "Submitting Amend IP16 Current Pensions data" when {

    "the data is valid" in new Setup {
      object DataItem
          extends AuthorisedFakeRequestToPost(
            controller.submitAmendCurrentPension(Strings.ProtectionTypeURL.IndividualProtection2016, "dormant"),
            ("amendedUKPensionAmt", "100000")
          )

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheSaveCondition[AmendProtectionModel](mockSessionCacheService)
      cacheFetchCondition[AmendProtectionModel](Some(testIP16DormantModel))

      status(DataItem.result) shouldBe 303
      redirectLocation(DataItem.result) shouldBe Some(s"${routes.AmendsController.amendsSummary(
          Strings.ProtectionTypeURL.IndividualProtection2016,
          Strings.StatusURL.Dormant
        )}")
    }

    "the data is invalid" in new Setup {
      object DataItem
          extends AuthorisedFakeRequestToPost(
            controller.submitAmendCurrentPension(Strings.ProtectionTypeURL.IndividualProtection2016, "dormant"),
            ("amendedUKPensionAmt", "")
          )
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      status(DataItem.result) shouldBe 400
    }

    "the model can't be fetched from cache" in new Setup {
      object DataItem
          extends AuthorisedFakeRequestToPost(
            controller.submitAmendCurrentPension("IP2016", "dormant"),
            ("amendedUKPensionAmt", "1000000")
          )
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](None)

      status(DataItem.result) shouldBe 500
    }
  }

  "Submitting Amend IP14 Current Pensions data" when {

    "the data is valid" in new Setup {
      object DataItem
          extends AuthorisedFakeRequestToPost(
            controller.submitAmendCurrentPension(Strings.ProtectionTypeURL.IndividualProtection2014, "dormant"),
            ("amendedUKPensionAmt", "100000")
          )

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheSaveCondition[AmendProtectionModel](mockSessionCacheService)
      cacheFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))

      status(DataItem.result) shouldBe 303
      redirectLocation(DataItem.result) shouldBe Some(
        s"${routes.AmendsController.amendsSummary(Strings.ProtectionTypeURL.IndividualProtection2014, "dormant")}"
      )
    }

    "the data is invalid" in new Setup {
      object DataItem
          extends AuthorisedFakeRequestToPost(
            controller.submitAmendCurrentPension(Strings.ProtectionTypeURL.IndividualProtection2014, "dormant"),
            ("amendedUKPensionAmt", "")
          )
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      status(DataItem.result) shouldBe 400
    }

    "the model can't be fetched from cache" in new Setup {
      object DataItem
          extends AuthorisedFakeRequestToPost(
            controller.submitAmendCurrentPension(Strings.ProtectionTypeURL.IndividualProtection2014, "dormant"),
            ("amendedUKPensionAmt", "1000000")
          )
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](None)

      status(DataItem.result) shouldBe 500
    }
  }

}
