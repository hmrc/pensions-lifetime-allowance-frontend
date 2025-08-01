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
import common.Exceptions.RequiredValueNotDefinedException
import config._
import connectors.PLAConnector
import constructors.DisplayConstructors
import enums.ApplicationType
import mocks.AuthMock
import models._
import models.amendModels._
import models.cache.CacheMap
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.Environment
import play.api.http.HeaderNames.CACHE_CONTROL
import play.api.i18n.{I18nSupport, Lang, Messages, MessagesApi}
import play.api.libs.json.JsNull
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionCacheService
import testHelpers._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import views.html.pages.amends._
import views.html.pages.fallback.technicalError

import java.time.LocalDate
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class AmendsPensionSharingOrderControllerSpec
    extends FakeApplication
    with MockitoSugar
    with SessionCacheTestHelper
    with BeforeAndAfterEach
    with AuthMock
    with I18nSupport {

  implicit lazy val mockMessage: Messages =
    fakeApplication().injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  val mockDisplayConstructors: DisplayConstructors = mock[DisplayConstructors]
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
  val mockPlaConnector: PLAConnector               = mock[PLAConnector]
  val mockMCC: MessagesControllerComponents        = fakeApplication().injector.instanceOf[MessagesControllerComponents]
  val mockAuthFunction: AuthFunction               = mock[AuthFunction]
  val mockAmendPsoDetails: amendPsoDetails         = app.injector.instanceOf[amendPsoDetails]
  val mockTechnicalError: technicalError           = app.injector.instanceOf[technicalError]
  val mockEnv: Environment                         = mock[Environment]
  val messagesApi: MessagesApi                     = mockMCC.messagesApi

  implicit val mockAppConfig: FrontendAppConfig = fakeApplication().injector.instanceOf[FrontendAppConfig]
  implicit val mockPlaContext: PlaContext       = mock[PlaContext]
  implicit val system: ActorSystem              = ActorSystem()
  implicit val materializer: Materializer       = mock[Materializer]
  implicit val mockLang: Lang                   = mock[Lang]
  implicit val formWithCSRF: FormWithCSRF       = app.injector.instanceOf[FormWithCSRF]
  implicit val ec: ExecutionContext             = app.injector.instanceOf[ExecutionContext]

  override def beforeEach(): Unit = {
    reset(mockSessionCacheService)
    reset(mockPlaConnector)
    reset(mockDisplayConstructors)
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
      status = Some("dormant"),
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

    val controller = new AmendsPensionSharingOrderController(
      mockSessionCacheService,
      mockMCC,
      authFunction,
      mockAmendPsoDetails,
      mockTechnicalError
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
    status = Some("dormant"),
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
    status = Some("dormant"),
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
    status = Some("dormant"),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456")
  )

  val testAmendIP2016ProtectionModelWithNoDebit = AmendProtectionModel(ip2016NoDebitProtection, ip2016NoDebitProtection)

  val noNotificationIdProtection = ProtectionModel(
    psaCheckReference = Some("testPSARef"),
    protectionID = Some(12345),
    uncrystallisedRights = Some(100000.00),
    nonUKRights = Some(0.0),
    preADayPensionInPayment = Some(0.0),
    postADayBenefitCrystallisationEvents = Some(0.0),
    protectionType = Some("IP2014"),
    status = Some("dormant"),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456")
  )

  val tstPensionContributionNoPsoDisplaySections = Seq(
    AmendDisplaySectionModel(
      "OverseasPensions",
      Seq(
        AmendDisplayRowModel(
          "YesNo",
          Some(controllers.routes.AmendsOverseasPensionController.amendOverseasPensions("ip2014", "active")),
          None,
          "Yes"
        ),
        AmendDisplayRowModel(
          "Amt",
          Some(controllers.routes.AmendsOverseasPensionController.amendOverseasPensions("ip2014", "active")),
          None,
          "£100,000"
        )
      )
    ),
    AmendDisplaySectionModel(
      "CurrentPensions",
      Seq(
        AmendDisplayRowModel(
          "Amt",
          Some(controllers.routes.AmendsCurrentPensionController.amendCurrentPensions("ip2014", "active")),
          None,
          "£1,000,000"
        )
      )
    ),
    AmendDisplaySectionModel(
      "CurrentPsos",
      Seq(
        AmendDisplayRowModel("YesNo", None, None, "No")
      )
    )
  )

  val tstAmendDisplayModel = AmendDisplayModel(
    protectionType = "IP2014",
    amended = true,
    pensionContributionSections = tstPensionContributionNoPsoDisplaySections,
    psoAdded = false,
    psoSections = Seq.empty,
    totalAmount = "£1,100,000"
  )

  val ip2014ActiveAmendmentProtection = ProtectionModel(
    psaCheckReference = Some("psaRef"),
    protectionID = Some(12345),
    notificationId = Some(33)
  )

  val tstActiveAmendResponseModel = AmendResponseModel(ip2014ActiveAmendmentProtection)

  val tstActiveAmendResponseDisplayModel = ActiveAmendResultDisplayModel(
    protectionType = ApplicationType.IP2014,
    notificationId = "33",
    protectedAmount = "£1,100,000",
    details = None
  )

  val ip2016InactiveAmendmentProtection = ProtectionModel(
    psaCheckReference = Some("psaRef"),
    protectionID = Some(12345),
    notificationId = Some(43)
  )

  val tstInactiveAmendResponseModel = AmendResponseModel(ip2016InactiveAmendmentProtection)

  val tstInactiveAmendResponseDisplayModel = InactiveAmendResultDisplayModel(
    notificationId = "43",
    additionalInfo = Seq.empty
  )

  def cacheFetchCondition[T](data: Option[T]): Unit =
    when(mockSessionCacheService.fetchAndGetFormData[T](anyString())(any(), any()))
      .thenReturn(Future.successful(data))

  "Calling the amendPsoDetails action" when {

    val testProtectionNoPsoList = ProtectionModel(
      psaCheckReference = Some("psaRef"),
      protectionID = Some(1234),
      pensionDebits = None
    )

    val testProtectionEmptyPsoList = ProtectionModel(
      psaCheckReference = Some("psaRef"),
      protectionID = Some(1234),
      pensionDebits = Some(List.empty)
    )

    val testProtectionSinglePsoList = ProtectionModel(
      psaCheckReference = Some("psaRef"),
      protectionID = Some(1234),
      pensionDebits = Some(List(PensionDebitModel("2016-12-23", 1000.0)))
    )

    val testProtectionMultiplePsoList = ProtectionModel(
      psaCheckReference = Some("psaRef"),
      protectionID = Some(1234),
      pensionDebits = Some(List(PensionDebitModel("2016-12-23", 1000.0), PensionDebitModel("2016-12-27", 11322.75)))
    )

    "there is no amendment model fetched from cache" in new Setup {

      lazy val result = controller.amendPsoDetails("ip2014", "open")(fakeRequest)
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](None)

      status(result) shouldBe 500
    }
    "show the technical error page for existing protections" in new Setup {
      lazy val result   = controller.amendPsoDetails("ip2014", "open")(fakeRequest)
      lazy val jsoupDoc = Jsoup.parse(contentAsString(result))

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](None)

      jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
      jsoupDoc.body
        .getElementById("tryAgainLink")
        .attr("href") shouldEqual s"${controllers.routes.ReadProtectionsController.currentProtections}"
      await(result).header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache"
    }

    "there is no PSO list stored in the AmendProtectionModel" in new Setup {

      lazy val result   = controller.amendPsoDetails("ip2014", "open")(fakeRequest)
      lazy val jsoupDoc = Jsoup.parse(contentAsString(result))

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](
        Some(AmendProtectionModel(testProtectionNoPsoList, testProtectionNoPsoList))
      )
      status(result) shouldBe 200

      jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.psoDetails.title")
      jsoupDoc.body.getElementById("pso.day").attr("value") shouldEqual ""
      jsoupDoc.body.getElementById("pso.month").attr("value") shouldEqual ""
      jsoupDoc.body.getElementById("pso.year").attr("value") shouldEqual ""
    }

    "there is an empty PSO list stored in the AmendProtectionModel" in new Setup {

      lazy val result   = controller.amendPsoDetails("ip2016", "open")(fakeRequest)
      lazy val jsoupDoc = Jsoup.parse(contentAsString(result))

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](
        Some(AmendProtectionModel(testProtectionEmptyPsoList, testProtectionEmptyPsoList))
      )

      status(result) shouldBe 200
      jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.psoDetails.title")
      jsoupDoc.body.getElementById("pso.day").attr("value") shouldEqual ""
      jsoupDoc.body.getElementById("pso.month").attr("value") shouldEqual ""
      jsoupDoc.body.getElementById("pso.year").attr("value") shouldEqual ""
    }

    "there is a PSO list of one PSO stored in the AmendProtectionModel" in new Setup {

      object DataItem extends AuthorisedFakeRequestTo(controller.amendPsoDetails("ip2016", "open"))

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](
        Some(AmendProtectionModel(testProtectionSinglePsoList, testProtectionSinglePsoList))
      )

      status(DataItem.result) shouldBe 200

      DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.psoDetails.title")
      DataItem.jsoupDoc.body.getElementById("pso.day").attr("value") shouldEqual "23"
      DataItem.jsoupDoc.body.getElementById("pso.month").attr("value") shouldEqual "12"
      DataItem.jsoupDoc.body.getElementById("pso.year").attr("value") shouldEqual "2016"
      DataItem.jsoupDoc.body.getElementById("psoAmt").attr("value") shouldEqual "1000"
    }

    "there is a PSO list of more then one PSO stored in the AmendProtectionModel" in new Setup {

      lazy val result   = controller.amendPsoDetails("ip2016", "open")(fakeRequest)
      lazy val jsoupDoc = Jsoup.parse(contentAsString(result))

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](
        Some(AmendProtectionModel(testProtectionMultiplePsoList, testProtectionMultiplePsoList))
      )

      status(result) shouldBe 500
      jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
      jsoupDoc.body
        .getElementById("tryAgainLink")
        .attr("href") shouldEqual s"${controllers.routes.ReadProtectionsController.currentProtections}"
      await(result).header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache"
    }
  }

  "Submitting Amend PSOs data" when {

    "submitting valid data for IP14" in new Setup {

      object DataItem
          extends AuthorisedFakeRequestToPost(
            controller.submitAmendPsoDetails(protectionType = "ip2014", status = "open", existingPSO = true),
            ("pso.day", "6"),
            ("pso.month", "4"),
            ("pso.year", "2014"),
            ("psoAmt", "100000")
          )

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))
      when(mockSessionCacheService.saveFormData(any(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("", Map("" -> JsNull))))

      status(DataItem.result) shouldBe 303
      redirectLocation(DataItem.result) shouldBe Some(s"${routes.AmendsController.amendsSummary("ip2014", "open")}")
    }

    "submitting valid data for IP16" in new Setup {

      object DataItem
          extends AuthorisedFakeRequestToPost(
            controller.submitAmendPsoDetails(protectionType = "ip2016", status = "open", existingPSO = true),
            ("pso.day", "6"),
            ("pso.month", "4"),
            ("pso.year", "2016"),
            ("psoAmt", "100000")
          )

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
      when(mockSessionCacheService.saveFormData(any(), any())(any(), any()))
        .thenReturn(Future.successful(CacheMap("", Map("" -> JsNull))))

      status(DataItem.result) shouldBe 303
      redirectLocation(DataItem.result) shouldBe Some(s"${routes.AmendsController.amendsSummary("ip2016", "open")}")
    }

    "submitting invalid data" in new Setup {

      object DataItem
          extends AuthorisedFakeRequestToPost(
            controller.submitAmendPsoDetails(protectionType = "ip2014", status = "open", existingPSO = true),
            ("pso.day", ""),
            ("pso.month", "1"),
            ("pso.year", "2015"),
            ("psoAmt", "100000")
          )

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      status(DataItem.result) shouldBe 400
    }

    "submitting data which fails additional validation" in new Setup {

      object DataItem
          extends AuthorisedFakeRequestToPost(
            controller.submitAmendPsoDetails(protectionType = "ip2014", status = "open", existingPSO = true),
            ("pso.day", "36"),
            ("pso.month", "1"),
            ("pso.year", "2015"),
            ("psoAmt", "100000")
          )

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      status(DataItem.result) shouldBe 400
    }
  }

  "Calling createPsoDetailsList" when {

    "not supplied with a PSO amount" should {

      "return the correct value not found exception" in new Setup() {
        (the[RequiredValueNotDefinedException] thrownBy {
          controller.createPsoDetailsList(AmendPSODetailsModel(LocalDate.of(2017, 3, 1), None))
        } should have).message("Value not found for psoAmt in createPsoDetailsList")
      }
    }

    "supplied with a PSO amount" should {

      "return the correct list" in new Setup {
        controller.createPsoDetailsList(AmendPSODetailsModel(LocalDate.of(2017, 3, 1), Some(1))) shouldBe Some(
          List(PensionDebitModel("2017-03-01", 1))
        )
      }
    }
  }

}
