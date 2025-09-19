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
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.Environment
import play.api.i18n.{I18nSupport, Lang, Messages, MessagesApi}
import play.api.mvc.{AnyContent, MessagesControllerComponents, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionCacheService
import testHelpers._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import views.html.pages.amends._
import views.html.pages.fallback.technicalError

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class AmendsPensionWorthBeforeControllerSpec
    extends FakeApplication
    with MockitoSugar
    with SessionCacheTestHelper
    with BeforeAndAfterEach
    with AuthMock
    with I18nSupport {

  implicit lazy val messages: Messages =
    fakeApplication().injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
  val mockMCC: MessagesControllerComponents        = fakeApplication().injector.instanceOf[MessagesControllerComponents]
  val mockAuthFunction: AuthFunction               = mock[AuthFunction]
  val mockTechnicalError: technicalError           = app.injector.instanceOf[technicalError]
  val mockAmendPensionsWorthBefore: amendPensionsWorthBefore = app.injector.instanceOf[amendPensionsWorthBefore]

  val mockAmendIP14PensionsWorthBefore: amendIP14PensionsWorthBefore =
    app.injector.instanceOf[amendIP14PensionsWorthBefore]

  val mockEnv: Environment     = mock[Environment]
  val messagesApi: MessagesApi = mockMCC.messagesApi

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

    val controller = new AmendsPensionWorthBeforeController(
      mockSessionCacheService,
      mockMCC,
      authFunction,
      mockTechnicalError,
      mockAmendPensionsWorthBefore,
      mockAmendIP14PensionsWorthBefore
    )

  }

  val sessionId                                     = UUID.randomUUID.toString
  implicit val fakeRequest: FakeRequest[AnyContent] = FakeRequest()
  val mockUsername                                  = "mockuser"
  val mockUserId                                    = "/auth/oid/" + mockUsername

  val ip2016Protection = ProtectionModel(
    psaCheckReference = Some("testPSARef"),
    uncrystallisedRights = Some(100000.00),
    nonUKRights = Some(2000.00),
    preADayPensionInPayment = Some(2000.00),
    postADayBenefitCrystallisationEvents = Some(2000.00),
    notificationId = Some(12),
    protectionID = Some(12345),
    protectionType = Some("IP2016"),
    status = Some(Dormant.toString),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456")
  )

  val testAmendIP2016ProtectionModel = AmendProtectionModel(ip2016Protection, ip2016Protection)

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

  val ip2016NoDebitProtection = ProtectionModel(
    psaCheckReference = Some("testPSARef"),
    uncrystallisedRights = Some(100000.00),
    nonUKRights = Some(0.0),
    preADayPensionInPayment = Some(0.0),
    postADayBenefitCrystallisationEvents = Some(0.0),
    notificationId = Some(12),
    protectionID = Some(12345),
    protectionType = Some("IP2016"),
    status = Some(Dormant.toString),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456")
  )

  val testAmendIP2016ProtectionModelWithNoDebit = AmendProtectionModel(ip2016NoDebitProtection, ip2016NoDebitProtection)

  def cacheFetchCondition[T](data: Option[T]): Unit =
    when(mockSessionCacheService.fetchAndGetFormData[T](anyString())(any(), any()))
      .thenReturn(Future.successful(data))

  "AmendsPensionWorthBeforeController" must {

    "return a 200 status" when {

      "model is returned from cache and protection type is IP2014" in new Setup {

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        cacheFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))

        val result: Future[Result] =
          controller.amendPensionsWorthBefore(Strings.ProtectionTypeURL.IndividualProtection2014, "dormant")(
            fakeRequest
          )

        status(result) shouldBe OK
        contentAsString(result) should include(messages("pla.ip14PensionsTakenBefore.question"))
      }

      "IP2016 model is returned from cache" in new Setup {

        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        cacheFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))

        val result: Future[Result] =
          controller.amendPensionsWorthBefore(Strings.ProtectionTypeURL.IndividualProtection2016, "dormant")(
            fakeRequest
          )

        status(result) shouldBe OK
        contentAsString(result) should include(messages("pla.pensionsWorthBefore.title"))
      }
    }

    "return 500 when nothing is returned from cache" in new Setup {

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](None)

      val result =
        controller.amendPensionsWorthBefore(Strings.ProtectionTypeURL.IndividualProtection2016, "dormant")(fakeRequest)

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }

  "Submitting Amend IP16 Pensions Worth Before" when {
    "the data is valid" in new Setup {
      object DataItem
          extends AuthorisedFakeRequestToPost(
            controller.submitAmendPensionsWorthBefore(Strings.ProtectionTypeURL.IndividualProtection2016, "dormant"),
            ("amendedPensionsTakenBeforeAmt", "10000")
          )

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
      cacheSaveCondition[PensionsWorthBeforeModel](mockSessionCacheService)
      cacheSaveCondition[AmendProtectionModel](mockSessionCacheService)

      status(DataItem.result) shouldBe 303
      redirectLocation(DataItem.result) shouldBe Some(s"${routes.AmendsController.amendsSummary(
          Strings.ProtectionTypeURL.IndividualProtection2016,
          Strings.StatusURL.Dormant
        )}")
    }

    "the data is invalid" in new Setup {
      object DataItem
          extends AuthorisedFakeRequestToPost(
            controller.submitAmendPensionsWorthBefore(Strings.ProtectionTypeURL.IndividualProtection2016, "dormant"),
            ("amendedPensionsTakenBeforeAmt", "yes")
          )

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
      cacheSaveCondition[PensionsWorthBeforeModel](mockSessionCacheService)
      cacheSaveCondition[AmendProtectionModel](mockSessionCacheService)

      status(DataItem.result) shouldBe 400
    }

    "the model can't be fetched from cache" in new Setup {
      object DataItem
          extends AuthorisedFakeRequestToPost(
            controller.submitAmendPensionsWorthBefore(Strings.ProtectionTypeURL.IndividualProtection2016, "dormant"),
            ("amendedPensionsTakenBeforeAmt", "10000")
          )

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](None)
      cacheSaveCondition[PensionsWorthBeforeModel](mockSessionCacheService)
      cacheSaveCondition[AmendProtectionModel](mockSessionCacheService)

      status(DataItem.result) shouldBe 500
    }
  }

  "Submitting Amend IP14 Pensions Worth Before" when {
    "the data is valid" in new Setup {
      object DataItem
          extends AuthorisedFakeRequestToPost(
            controller.submitAmendPensionsWorthBefore(Strings.ProtectionTypeURL.IndividualProtection2014, "dormant"),
            ("amendedPensionsTakenBeforeAmt", "10000")
          )

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))
      cacheSaveCondition[PensionsWorthBeforeModel](mockSessionCacheService)
      cacheSaveCondition[AmendProtectionModel](mockSessionCacheService)

      status(DataItem.result) shouldBe 303
      redirectLocation(DataItem.result) shouldBe Some(s"${routes.AmendsController.amendsSummary(
          Strings.ProtectionTypeURL.IndividualProtection2014,
          Strings.StatusURL.Dormant
        )}")
    }

    "the data is invalid" in new Setup {
      object DataItem
          extends AuthorisedFakeRequestToPost(
            controller.submitAmendPensionsWorthBefore(Strings.ProtectionTypeURL.IndividualProtection2014, "dormant"),
            ("amendedPensionsTakenBeforeAmt", "yes")
          )

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))
      cacheSaveCondition[PensionsWorthBeforeModel](mockSessionCacheService)
      cacheSaveCondition[AmendProtectionModel](mockSessionCacheService)

      status(DataItem.result) shouldBe 400
    }

    "the model can't be fetched from cache" in new Setup {
      object DataItem
          extends AuthorisedFakeRequestToPost(
            controller.submitAmendPensionsWorthBefore(Strings.ProtectionTypeURL.IndividualProtection2014, "dormant"),
            ("amendedPensionsTakenBeforeAmt", "10000")
          )

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](None)
      cacheSaveCondition[PensionsWorthBeforeModel](mockSessionCacheService)
      cacheSaveCondition[AmendProtectionModel](mockSessionCacheService)

      status(DataItem.result) shouldBe 500
    }
  }

}
