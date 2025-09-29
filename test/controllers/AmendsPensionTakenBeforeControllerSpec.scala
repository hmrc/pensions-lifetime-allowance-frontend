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

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class AmendsPensionTakenBeforeControllerSpec
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
  val mockAmendPensionsTakenBefore: amendPensionsTakenBefore = app.injector.instanceOf[amendPensionsTakenBefore]

  val mockAmendIP14PensionsWorthBefore: amendIP14PensionsTakenBefore =
    app.injector.instanceOf[amendIP14PensionsTakenBefore]

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

  class Setup {

    val authFunction = new AuthFunctionImpl(
      mockMCC,
      mockAuthConnector,
      mockTechnicalError
    )

    val controller = new AmendsPensionTakenBeforeController(
      mockSessionCacheService,
      mockMCC,
      authFunction,
      mockTechnicalError,
      mockAmendPensionsTakenBefore,
      mockAmendIP14PensionsWorthBefore
    )

  }

  val sessionId                                     = UUID.randomUUID.toString
  implicit val fakeRequest: FakeRequest[AnyContent] = FakeRequest()
  val mockUsername                                  = "mockuser"
  val mockUserId                                    = "/auth/oid/" + mockUsername

  val individualProtection2016Protection = ProtectionModel(
    psaCheckReference = Some("testPSARef"),
    uncrystallisedRights = Some(100000.00),
    nonUKRights = Some(2000.00),
    preADayPensionInPayment = Some(2000.00),
    postADayBenefitCrystallisationEvents = Some(2000.00),
    notificationId = Some(12),
    protectionID = Some(12345),
    protectionType = Some("IndividualProtection2016"),
    status = Some(Dormant.toString),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456")
  )

  val testAmendIndividualProtection2016ProtectionModel =
    AmendProtectionModel(individualProtection2016Protection, individualProtection2016Protection)

  val individualProtection2016LTAProtection = ProtectionModel(
    psaCheckReference = Some("testPSARef"),
    uncrystallisedRights = Some(100000.00),
    nonUKRights = Some(2000.00),
    preADayPensionInPayment = Some(2000.00),
    postADayBenefitCrystallisationEvents = Some(2000.00),
    notificationId = Some(12),
    protectionID = Some(12345),
    protectionType = Some("IndividualProtection2016LTA"),
    status = Some(Dormant.toString),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456")
  )

  val testAmendIndividualProtection2016LTAProtectionModel =
    AmendProtectionModel(individualProtection2016LTAProtection, individualProtection2016LTAProtection)

  val individualProtection2014Protection = ProtectionModel(
    psaCheckReference = Some("testPSARef"),
    uncrystallisedRights = Some(100000.00),
    nonUKRights = Some(2000.00),
    preADayPensionInPayment = Some(2000.00),
    postADayBenefitCrystallisationEvents = Some(2000.00),
    notificationId = Some(12),
    protectionID = Some(12345),
    protectionType = Some("IndividualProtection2014"),
    status = Some(Dormant.toString),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456")
  )

  val testAmendIndividualProtection2014ProtectionModel =
    AmendProtectionModel(individualProtection2014Protection, individualProtection2014Protection)

  val individualProtection2014LTAProtection = ProtectionModel(
    psaCheckReference = Some("testPSARef"),
    uncrystallisedRights = Some(100000.00),
    nonUKRights = Some(2000.00),
    preADayPensionInPayment = Some(2000.00),
    postADayBenefitCrystallisationEvents = Some(2000.00),
    notificationId = Some(12),
    protectionID = Some(12345),
    protectionType = Some("IndividualProtection2014LTA"),
    status = Some(Dormant.toString),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456")
  )

  val testAmendIndividualProtection2014LTAProtectionModel =
    AmendProtectionModel(individualProtection2014LTAProtection, individualProtection2014LTAProtection)

  val individualProtection2016NoDebitProtection = ProtectionModel(
    psaCheckReference = Some("testPSARef"),
    uncrystallisedRights = Some(100000.00),
    nonUKRights = Some(0.0),
    preADayPensionInPayment = Some(0.0),
    postADayBenefitCrystallisationEvents = Some(0.0),
    notificationId = Some(12),
    protectionID = Some(12345),
    protectionType = Some("IndividualProtection2016"),
    status = Some(Dormant.toString),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456")
  )

  val testAmendIndividualProtection2016ProtectionModelWithNoDebit =
    AmendProtectionModel(individualProtection2016NoDebitProtection, individualProtection2016NoDebitProtection)

  val individualProtection2016LTANoDebitProtection = ProtectionModel(
    psaCheckReference = Some("testPSARef"),
    uncrystallisedRights = Some(100000.00),
    nonUKRights = Some(0.0),
    preADayPensionInPayment = Some(0.0),
    postADayBenefitCrystallisationEvents = Some(0.0),
    notificationId = Some(12),
    protectionID = Some(12345),
    protectionType = Some("IndividualProtection2016LTA"),
    status = Some(Dormant.toString),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456")
  )

  val testAmendIndividualProtection2016LTAProtectionModelWithNoDebit =
    AmendProtectionModel(individualProtection2016LTANoDebitProtection, individualProtection2016LTANoDebitProtection)

  val tstPensionContributionNoPsoDisplaySections = Seq(
    AmendDisplaySectionModel(
      "PensionsTakenBefore",
      Seq(
        AmendDisplayRowModel(
          "YesNo",
          Some(
            controllers.routes.AmendsPensionTakenBeforeController
              .amendPensionsTakenBefore(Strings.ProtectionTypeURL.IndividualProtection2014, "active")
          ),
          None,
          "No"
        )
      )
    )
  )

  def cacheFetchCondition[T](data: Option[T]): Unit =
    when(mockSessionCacheService.fetchAndGetFormData[T](anyString())(any(), any()))
      .thenReturn(Future.successful(data))

  "In AmendsPensionTakenBeforeController calling the .amendPensionsTakenBefore action" when {

    "not supplied with a stored model" in new Setup {
      lazy val result =
        controller.amendPensionsTakenBefore(Strings.ProtectionTypeURL.IndividualProtection2016, "open")(fakeRequest)
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](None)

      status(result) shouldBe 500
    }

    "supplied with the stored test model for (dormant, IndividualProtection2016, preADay = £0.0)" in new Setup {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](Some(testAmendIndividualProtection2016ProtectionModelWithNoDebit))
    }

    "supplied with the stored test model for (dormant, IndividualProtection2016, preADay = £2000)" in new Setup {

      lazy val result =
        controller.amendPensionsTakenBefore(Strings.ProtectionTypeURL.IndividualProtection2016, "dormant")(fakeRequest)
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](Some(testAmendIndividualProtection2016ProtectionModel))

      status(result) shouldBe 200
    }

    "should take the user to the pensions taken before page" in new Setup {
      lazy val result =
        controller.amendPensionsTakenBefore(Strings.ProtectionTypeURL.IndividualProtection2016, "dormant")(fakeRequest)
      lazy val jsoupDoc = Jsoup.parse(contentAsString(result))

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](Some(testAmendIndividualProtection2016ProtectionModel))

      jsoupDoc.body.getElementsByClass("govuk-heading-xl").text shouldEqual Messages("pla.pensionsTakenBefore.title")
    }

    "return some HTML that" should {

      "contain some text and use the character set utf-8" in new Setup {
        lazy val result =
          controller.amendPensionsTakenBefore(Strings.ProtectionTypeURL.IndividualProtection2016, "dormant")(
            fakeRequest
          )
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        cacheFetchCondition[AmendProtectionModel](Some(testAmendIndividualProtection2016ProtectionModel))

        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }

      "have the value of the check box set as 'Yes' by default" in new Setup {
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        cacheFetchCondition[AmendProtectionModel](Some(testAmendIndividualProtection2016ProtectionModel))
      }

      "have the value of the input field set to 2000 by default" in new Setup {
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        cacheFetchCondition[AmendProtectionModel](Some(testAmendIndividualProtection2016ProtectionModel))
      }
    }
    "supplied with the stored test model for (dormant, IndividualProtection2014, preADay = £2000)" in new Setup {
      lazy val result =
        controller.amendPensionsTakenBefore(Strings.ProtectionTypeURL.IndividualProtection2016, "dormant")(fakeRequest)
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](Some(testAmendIndividualProtection2014ProtectionModel))

      status(result) shouldBe 200
    }
  }

  "Submitting Amend IndividualProtection2016 Pensions Taken Before data" when {

    "the data is invalid" in new Setup {
      lazy val result =
        controller.submitAmendPensionsTakenBefore(Strings.ProtectionTypeURL.IndividualProtection2016, "dormant")(
          fakeRequest
        )
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

      status(result) shouldBe 400
    }
  }

  "Submitting Amend IndividualProtection2016LTA Pensions Taken Before data" when {

    "the data is invalid" in new Setup {
      lazy val result =
        controller.submitAmendPensionsTakenBefore(Strings.ProtectionTypeURL.IndividualProtection2016LTA, "dormant")(
          fakeRequest
        )
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

      status(result) shouldBe 400
    }
  }

  "the data is invalidated by additional validation" in new Setup {
    object DataItem
        extends AuthorisedFakeRequestToPost(
          controller.submitAmendPensionsTakenBefore(Strings.ProtectionTypeURL.IndividualProtection2016, "dormant"),
          ("amendedPensionsTakenBefore", "1")
        )

    mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

    status(DataItem.result) shouldBe 400
  }

  "the model can't be fetched from cache" in new Setup {
    object DataItem
        extends AuthorisedFakeRequestToPost(
          controller.submitAmendPensionsTakenBefore(Strings.ProtectionTypeURL.IndividualProtection2016, "dormant"),
          ("amendedPensionsTakenBefore", "no"),
          ("amendedPensionsTakenBeforeAmt", "0")
        )

    mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
    cacheFetchCondition[AmendProtectionModel](None)

    status(DataItem.result) shouldBe 500
  }

  "the data is valid with a no for IndividualProtection2016" in new Setup {
    object DataItem
        extends AuthorisedFakeRequestToPost(
          controller.submitAmendPensionsTakenBefore(Strings.ProtectionTypeURL.IndividualProtection2016, "dormant"),
          ("amendedPensionsTakenBefore", "no"),
          ("amendedPensionsTakenBeforeAmt", "0")
        )

    mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
    cacheSaveCondition[PensionsTakenBeforeModel](mockSessionCacheService)
    cacheFetchCondition[AmendProtectionModel](Some(testAmendIndividualProtection2016ProtectionModel))

    status(DataItem.result) shouldBe 303
    redirectLocation(DataItem.result) shouldBe Some(
      s"${routes.AmendsController.amendsSummary(Strings.ProtectionTypeURL.IndividualProtection2016, "dormant")}"
    )
  }

  "the data is valid with a yes for IndividualProtection2016" in new Setup {
    object DataItem
        extends AuthorisedFakeRequestToPost(
          controller.submitAmendPensionsTakenBefore(Strings.ProtectionTypeURL.IndividualProtection2016, "dormant"),
          ("amendedPensionsTakenBefore", "yes")
        )

    mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
    cacheFetchCondition[AmendProtectionModel](Some(testAmendIndividualProtection2016ProtectionModel))
    cacheSaveCondition[PensionsTakenBeforeModel](mockSessionCacheService)
    cacheSaveCondition[AmendProtectionModel](mockSessionCacheService)

    status(DataItem.result) shouldBe 303
    redirectLocation(DataItem.result) shouldBe Some(
      s"${routes.AmendsPensionWorthBeforeController.amendPensionsWorthBefore(Strings.ProtectionTypeURL.IndividualProtection2016, "dormant")}"
    )
  }

  "the data is valid with a no for IndividualProtection2016LTA" in new Setup {
    object DataItem
        extends AuthorisedFakeRequestToPost(
          controller.submitAmendPensionsTakenBefore(Strings.ProtectionTypeURL.IndividualProtection2016LTA, "dormant"),
          ("amendedPensionsTakenBefore", "no"),
          ("amendedPensionsTakenBeforeAmt", "0")
        )

    mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
    cacheSaveCondition[PensionsTakenBeforeModel](mockSessionCacheService)
    cacheFetchCondition[AmendProtectionModel](Some(testAmendIndividualProtection2016LTAProtectionModel))

    status(DataItem.result) shouldBe 303
    redirectLocation(DataItem.result) shouldBe Some(
      s"${routes.AmendsController.amendsSummary(Strings.ProtectionTypeURL.IndividualProtection2016LTA, "dormant")}"
    )
  }

  "the data is valid with a yes for IndividualProtection2016LTA" in new Setup {
    object DataItem
        extends AuthorisedFakeRequestToPost(
          controller.submitAmendPensionsTakenBefore(Strings.ProtectionTypeURL.IndividualProtection2016LTA, "dormant"),
          ("amendedPensionsTakenBefore", "yes")
        )

    mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
    cacheFetchCondition[AmendProtectionModel](Some(testAmendIndividualProtection2016LTAProtectionModel))
    cacheSaveCondition[PensionsTakenBeforeModel](mockSessionCacheService)
    cacheSaveCondition[AmendProtectionModel](mockSessionCacheService)

    status(DataItem.result) shouldBe 303
    redirectLocation(DataItem.result) shouldBe Some(
      s"${routes.AmendsPensionWorthBeforeController.amendPensionsWorthBefore(Strings.ProtectionTypeURL.IndividualProtection2016LTA, "dormant")}"
    )
  }

  "the data is valid with a no for IndividualProtection2014" in new Setup {
    object DataItem
        extends AuthorisedFakeRequestToPost(
          controller.submitAmendPensionsTakenBefore(Strings.ProtectionTypeURL.IndividualProtection2014, "dormant"),
          ("amendedPensionsTakenBefore", "no"),
          ("amendedPensionsTakenBeforeAmt", "0")
        )

    mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
    cacheSaveCondition[PensionsTakenBeforeModel](mockSessionCacheService)
    cacheFetchCondition[AmendProtectionModel](Some(testAmendIndividualProtection2014ProtectionModel))

    status(DataItem.result) shouldBe 303
    redirectLocation(DataItem.result) shouldBe Some(
      s"${routes.AmendsController.amendsSummary(Strings.ProtectionTypeURL.IndividualProtection2014, "dormant")}"
    )
  }

  "the data is valid with a yes for IndividualProtection2014" in new Setup {
    object DataItem
        extends AuthorisedFakeRequestToPost(
          controller.submitAmendPensionsTakenBefore(Strings.ProtectionTypeURL.IndividualProtection2014, "dormant"),
          ("amendedPensionsTakenBefore", "yes")
        )

    mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
    cacheFetchCondition[AmendProtectionModel](Some(testAmendIndividualProtection2014ProtectionModel))
    cacheSaveCondition[PensionsTakenBeforeModel](mockSessionCacheService)
    cacheSaveCondition[AmendProtectionModel](mockSessionCacheService)

    status(DataItem.result) shouldBe 303
    redirectLocation(DataItem.result) shouldBe Some(
      s"${routes.AmendsPensionWorthBeforeController.amendPensionsWorthBefore(Strings.ProtectionTypeURL.IndividualProtection2014, "dormant")}"
    )
  }

  "the data is valid with a no for IndividualProtection2014LTA" in new Setup {
    object DataItem
        extends AuthorisedFakeRequestToPost(
          controller.submitAmendPensionsTakenBefore(Strings.ProtectionTypeURL.IndividualProtection2014LTA, "dormant"),
          ("amendedPensionsTakenBefore", "no"),
          ("amendedPensionsTakenBeforeAmt", "0")
        )

    mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
    cacheSaveCondition[PensionsTakenBeforeModel](mockSessionCacheService)
    cacheFetchCondition[AmendProtectionModel](Some(testAmendIndividualProtection2014LTAProtectionModel))

    status(DataItem.result) shouldBe 303
    redirectLocation(DataItem.result) shouldBe Some(
      s"${routes.AmendsController.amendsSummary(Strings.ProtectionTypeURL.IndividualProtection2014LTA, "dormant")}"
    )
  }

  "the data is valid with a yes for IndividualProtection2014LTA" in new Setup {
    object DataItem
        extends AuthorisedFakeRequestToPost(
          controller.submitAmendPensionsTakenBefore(Strings.ProtectionTypeURL.IndividualProtection2014LTA, "dormant"),
          ("amendedPensionsTakenBefore", "yes")
        )

    mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
    cacheFetchCondition[AmendProtectionModel](Some(testAmendIndividualProtection2014LTAProtectionModel))
    cacheSaveCondition[PensionsTakenBeforeModel](mockSessionCacheService)
    cacheSaveCondition[AmendProtectionModel](mockSessionCacheService)

    status(DataItem.result) shouldBe 303
    redirectLocation(DataItem.result) shouldBe Some(
      s"${routes.AmendsPensionWorthBeforeController.amendPensionsWorthBefore(Strings.ProtectionTypeURL.IndividualProtection2014LTA, "dormant")}"
    )
  }

}
