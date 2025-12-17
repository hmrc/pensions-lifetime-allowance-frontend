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
import config._
import connectors.PsaLookupConnector
import mocks.AuthMock
import models._
import models.amend.AmendProtectionModel
import models.pla.AmendableProtectionType
import models.pla.request.AmendProtectionRequestStatus
import models.pla.response.ProtectionStatus.Dormant
import models.pla.response.ProtectionType.IndividualProtection2016
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
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
    with MockSessionCacheService
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

  override val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]

  val mockPlaConnector: PsaLookupConnector = mock[PsaLookupConnector]
  val mockAuthFunction: AuthFunction       = mock[AuthFunction]
  val technicalErrorView: technicalError   = inject[technicalError]
  val removePsoDebitsView: removePsoDebits = inject[removePsoDebits]
  val mockEnv: Environment                 = mock[Environment]

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

  val individualProtection2016 = ProtectionModel(
    psaCheckReference = "testPSARef",
    identifier = 12345,
    sequenceNumber = 1,
    protectionType = IndividualProtection2016,
    status = Dormant,
    certificateDate = Some(DateModel.of(2016, 4, 17)),
    certificateTime = Some(TimeModel.of(14, 24, 8)),
    uncrystallisedRightsAmount = Some(100000.00),
    nonUKRightsAmount = Some(2000.00),
    preADayPensionInPaymentAmount = Some(2000.00),
    postADayBenefitCrystallisationEventAmount = Some(2000.00),
    protectedAmount = Some(1250000),
    relevantAmount = Some(106000),
    protectionReference = Some("PSA123456")
  )

  val amendIndividualProtection2016: AmendProtectionModel =
    AmendProtectionModel.tryFromProtection(individualProtection2016).get

  val pensionDebit = PensionDebitModel(DateModel.of(2016, 12, 23), 1000.0)

  val amendIndividualProtection2016WithPso: AmendProtectionModel =
    amendIndividualProtection2016.withPensionDebit(Some(pensionDebit))

  "Removing a recently added PSO" when {

    "there is no amend protection model fetched from cache" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      mockFetchAmendProtectionModel(any(), any())(None)

      val result: Future[Result] =
        controller.removePso(AmendableProtectionType.IndividualProtection2016, AmendProtectionRequestStatus.Open)(
          fakeRequest
        )

      status(result) shouldBe 500
    }

    "show the technical error page for existing protections" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      mockFetchAmendProtectionModel(any(), any())(None)

      val result: Future[Result] =
        controller.removePso(AmendableProtectionType.IndividualProtection2016, AmendProtectionRequestStatus.Open)(
          fakeRequest
        )
      val jsoupDoc: Document = Jsoup.parse(contentAsString(result))

      jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
      jsoupDoc.body
        .getElementById("tryAgainLink")
        .attr("href") shouldEqual s"${controllers.routes.ReadProtectionsController.currentProtections}"
    }

    "have the correct cache control" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      mockFetchAmendProtectionModel(any(), any())(None)

      val result: Result =
        await(
          controller.removePso(AmendableProtectionType.IndividualProtection2016, AmendProtectionRequestStatus.Open)(
            fakeRequest
          )
        )

      result.header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache"
    }

    "a valid amend protection model is fetched from cache" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      mockFetchAmendProtectionModel(any(), any())(
        Some(amendIndividualProtection2016WithPso)
      )

      val result: Future[Result] =
        controller.removePso(AmendableProtectionType.IndividualProtection2016, AmendProtectionRequestStatus.Open)(
          fakeRequest
        )

      status(result) shouldBe 200
    }

    "show the remove pso page with correct details" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      mockFetchAmendProtectionModel(any(), any())(
        Some(amendIndividualProtection2016WithPso)
      )

      val result: Future[Result] =
        controller.removePso(AmendableProtectionType.IndividualProtection2016, AmendProtectionRequestStatus.Open)(
          fakeRequest
        )

      val jsoupDoc: Document = Jsoup.parse(contentAsString(result))

      jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.psoDetails.title")
    }

    "return 500 if the an amend protection model could not be retrieved from cache" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      mockFetchAmendProtectionModel(any(), any())(None)

      val result = FakeRequests.authorisedPost(
        controller.removePso(AmendableProtectionType.IndividualProtection2016, AmendProtectionRequestStatus.Open)
      )

      status(result) shouldEqual 500
    }
  }

  "Choosing remove with a valid amend protection model" should {
    "return 303 redirecting to amendment summary" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      mockFetchAmendProtectionModel(any(), any())(Some(amendIndividualProtection2016))
      mockSaveAmendProtectionModel()

      val result = FakeRequests.authorisedPost(
        controller.submitRemovePso(AmendableProtectionType.IndividualProtection2016, AmendProtectionRequestStatus.Open)
      )

      status(result) shouldBe SEE_OTHER

      redirectLocation(result) shouldBe Some(
        routes.AmendsController
          .amendsSummary(
            AmendableProtectionType.IndividualProtection2016,
            AmendProtectionRequestStatus.Open
          )
          .url
      )

    }

    "remove the pension sharing order from the cached amend model" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      mockFetchAmendProtectionModel(any(), any())(Some(amendIndividualProtection2016WithPso))
      mockSaveAmendProtectionModel()

      val result = FakeRequests.authorisedPost(
        controller.submitRemovePso(AmendableProtectionType.IndividualProtection2016, AmendProtectionRequestStatus.Open)
      )

      status(result) shouldBe SEE_OTHER

      verify(mockSessionCacheService).saveAmendProtectionModel(
        eqTo(amendIndividualProtection2016)
      )(any())
    }
  }

}
