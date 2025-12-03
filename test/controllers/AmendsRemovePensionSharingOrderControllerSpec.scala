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
import connectors.PsaLookupConnector
import mocks.AuthMock
import models._
import models.amend.AmendProtectionModel
import models.pla.response.ProtectionStatus.{Dormant, Open}
import models.pla.response.ProtectionType.IndividualProtection2016
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.ArgumentMatchers.{any, anyString, eq => eqTo}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.Environment
import play.api.http.HeaderNames.CACHE_CONTROL
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

import scala.concurrent.{ExecutionContext, Future}

class AmendsRemovePensionSharingOrderControllerSpec
    extends FakeApplication
    with MockitoSugar
    with SessionCacheTestHelper
    with BeforeAndAfterEach
    with AuthMock
    with I18nSupport {

  implicit val fakeRequest: FakeRequest[AnyContent] = FakeRequest()

  val mcc: MessagesControllerComponents = inject[MessagesControllerComponents]
  val messagesApi: MessagesApi          = mcc.messagesApi

  implicit val messages: Messages = messagesApi.preferred(fakeRequest)

  implicit val mockAppConfig: FrontendAppConfig = inject[FrontendAppConfig]
  implicit val system: ActorSystem              = ActorSystem()
  implicit val materializer: Materializer       = mock[Materializer]
  implicit val mockLang: Lang                   = mock[Lang]
  implicit val formWithCSRF: FormWithCSRF       = inject[FormWithCSRF]
  implicit val ec: ExecutionContext             = inject[ExecutionContext]

  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
  val mockPlaConnector: PsaLookupConnector         = mock[PsaLookupConnector]
  val mockAuthFunction: AuthFunction               = mock[AuthFunction]
  val technicalErrorView: technicalError           = inject[technicalError]
  val removePsoDebitsView: removePsoDebits         = inject[removePsoDebits]
  val mockEnv: Environment                         = mock[Environment]

  override def beforeEach(): Unit = {
    reset(mockSessionCacheService)
    reset(mockPlaConnector)
    reset(mockAuthConnector)
    reset(mockEnv)
    super.beforeEach()
  }

  val authFunction = new AuthFunctionImpl(
    mcc,
    mockAuthConnector,
    technicalErrorView
  )

  val controller = new AmendsRemovePensionSharingOrderController(
    mockSessionCacheService,
    mcc,
    authFunction,
    technicalErrorView,
    removePsoDebitsView
  )

  val individualProtection2016Protection = ProtectionModel(
    psaCheckReference = Some("testPSARef"),
    uncrystallisedRights = Some(100000.00),
    nonUKRights = Some(2000.00),
    preADayPensionInPayment = Some(2000.00),
    postADayBenefitCrystallisationEvents = Some(2000.00),
    notificationId = Some(12),
    identifier = Some(12345),
    protectionType = Some("IndividualProtection2016"),
    status = Some(Dormant.toString),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1250000),
    relevantAmount = Some(106000),
    protectionReference = Some("PSA123456")
  )

  val testAmendIndividualProtection2016ProtectionModel =
    AmendProtectionModel(individualProtection2016Protection, individualProtection2016Protection)

  def cacheFetchCondition[T](data: Option[T]): Unit =
    when(mockSessionCacheService.fetchAndGetFormData[T](anyString())(any(), any()))
      .thenReturn(Future.successful(data))

  "Removing a recently added PSO" when {

    val testProtectionSinglePsoList = ProtectionModel(
      psaCheckReference = Some("psaRef"),
      identifier = Some(1234),
      pensionDebits = Some(List(PensionDebitModel("2016-12-23", 1000.0)))
    )

    "there is no amend protection model fetched from cache" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](None)

      val result: Future[Result] =
        controller.removePso(Strings.ProtectionTypeUrl.IndividualProtection2016, "open")(fakeRequest)

      status(result) shouldBe 500
    }

    "show the technical error page for existing protections" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](None)

      val result: Future[Result] =
        controller.removePso(Strings.ProtectionTypeUrl.IndividualProtection2016, "open")(fakeRequest)
      val jsoupDoc: Document = Jsoup.parse(contentAsString(result))

      jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
      jsoupDoc.body
        .getElementById("tryAgainLink")
        .attr("href") shouldEqual s"${controllers.routes.ReadProtectionsController.currentProtections}"
    }

    "have the correct cache control" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](None)

      val result: Result =
        await(controller.removePso(Strings.ProtectionTypeUrl.IndividualProtection2016, "open")(fakeRequest))

      result.header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache"
    }

    "a valid amend protection model is fetched from cache" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](
        Some(AmendProtectionModel(testProtectionSinglePsoList, testProtectionSinglePsoList))
      )

      val result: Future[Result] =
        controller.removePso(Strings.ProtectionTypeUrl.IndividualProtection2016, "open")(fakeRequest)

      status(result) shouldBe 200
    }

    "show the remove pso page with correct details" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](
        Some(AmendProtectionModel(testProtectionSinglePsoList, testProtectionSinglePsoList))
      )

      val result: Future[Result] =
        controller.removePso(Strings.ProtectionTypeUrl.IndividualProtection2016, "open")(fakeRequest)

      val jsoupDoc: Document = Jsoup.parse(contentAsString(result))

      jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.psoDetails.title")
    }

    "return 500 if the an amend protection model could not be retrieved from cache" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](None)

      val result = FakeRequests.authorisedPost(
        controller.removePso(Strings.ProtectionTypeUrl.IndividualProtection2016, "open")
      )

      status(result) shouldEqual 500
    }
  }

  "Choosing remove with a valid amend protection model" should {
    "return 303 redirecting to amendment summary" in {
      val individualProtection2016Protection = ProtectionModel(
        psaCheckReference = Some("testPSARef"),
        uncrystallisedRights = Some(100000.00),
        nonUKRights = Some(2000.00),
        preADayPensionInPayment = Some(2000.00),
        postADayBenefitCrystallisationEvents = Some(2000.00),
        notificationId = Some(12),
        identifier = Some(12345),
        protectionType = Some("IndividualProtection2016"),
        status = Some(Open.toString),
        certificateDate = Some("2016-04-17"),
        pensionDebits = Some(List(PensionDebitModel("2016-12-23", 1000.0))),
        protectedAmount = Some(1250000),
        protectionReference = Some("PSA123456")
      )

      val testAmendIndividualProtection2016ProtectionModel =
        AmendProtectionModel(individualProtection2016Protection, individualProtection2016Protection)

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](Some(testAmendIndividualProtection2016ProtectionModel))
      cacheSaveCondition[AmendProtectionModel](mockSessionCacheService)

      val result = FakeRequests.authorisedPost(
        controller.submitRemovePso(Strings.ProtectionTypeUrl.IndividualProtection2016, "open")
      )

      status(result) shouldBe 303
      redirectLocation(result) shouldBe Some(
        routes.AmendsController
          .amendsSummary(
            Strings.ProtectionTypeUrl.IndividualProtection2016,
            Strings.StatusUrl.Open
          )
          .toString
      )

    }

    "remove the pension sharing order from the cached amend model" in {
      val pensionDebitStartDate     = "2016-12-23"
      val pensionDebitEnteredAmount = 1_000

      val testProtection: ProtectionModel = individualProtection2016Protection.copy(
        pensionDebits =
          Some(List(PensionDebitModel(startDate = pensionDebitStartDate, amount = pensionDebitEnteredAmount))),
        pensionDebitStartDate = Some(pensionDebitStartDate),
        pensionDebitEnteredAmount = Some(pensionDebitEnteredAmount)
      )

      val testAmendProtectionModel = AmendProtectionModel(testProtection, testProtection)

      val updatedTestProtection: ProtectionModel = testProtection.copy(
        pensionDebits = None,
        pensionDebitStartDate = None,
        pensionDebitEnteredAmount = None
      )

      val updatedAmendProtectionModel = AmendProtectionModel(testProtection, updatedTestProtection)

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](Some(testAmendProtectionModel))
      cacheSaveCondition[AmendProtectionModel](mockSessionCacheService)

      val result = FakeRequests.authorisedPost(
        controller.submitRemovePso(Strings.ProtectionTypeUrl.IndividualProtection2016, "open")
      )

      status(result) shouldBe SEE_OTHER

      verify(mockSessionCacheService).saveFormData[AmendProtectionModel](
        eqTo(Strings.protectionCacheKey(IndividualProtection2016.toString, Open.toString)),
        eqTo(updatedAmendProtectionModel)
      )(any(), eqTo(AmendProtectionModel.format))
    }
  }

}
