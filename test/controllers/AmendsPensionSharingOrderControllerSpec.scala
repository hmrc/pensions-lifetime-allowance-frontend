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

import auth.{AuthFunction, AuthFunctionImpl, authenticatedFakeRequest}
import common.Exceptions
import config._
import connectors.PsaLookupConnector
import constructors.display.DisplayConstructors
import mocks.AuthMock
import models.amend.AmendProtectionModel
import models.display.{AmendDisplayModel, AmendDisplayRowModel, AmendDisplaySectionModel}
import models.pla.AmendableProtectionType
import models.pla.request.AmendProtectionRequestStatus
import models.{DateModel, PensionDebitModel}
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.Environment
import play.api.http.HeaderNames.CACHE_CONTROL
import play.api.i18n.{I18nSupport, Lang, Messages, MessagesApi}
import play.api.mvc.{AnyContent, MessagesControllerComponents, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionCacheService
import testHelpers._
import testdata.AmendProtectionModelTestData
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import views.html.pages.amends._
import views.html.pages.fallback.technicalError

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class AmendsPensionSharingOrderControllerSpec
    extends FakeApplication
    with MockitoSugar
    with MockSessionCacheService
    with BeforeAndAfterEach
    with AuthMock
    with I18nSupport
    with AmendProtectionModelTestData
    with ScalaFutures {

  implicit val fakeRequest: FakeRequest[AnyContent] = FakeRequest()

  val mcc: MessagesControllerComponents = inject[MessagesControllerComponents]
  val messagesApi: MessagesApi          = mcc.messagesApi

  implicit val messages: Messages = messagesApi.preferred(fakeRequest)

  implicit val appConfig: FrontendAppConfig   = inject[FrontendAppConfig]
  implicit val system: ActorSystem            = ActorSystem()
  implicit val mockMaterializer: Materializer = mock[Materializer]
  implicit val mockLang: Lang                 = mock[Lang]
  implicit val formWithCSRF: FormWithCSRF     = inject[FormWithCSRF]
  implicit val ec: ExecutionContext           = inject[ExecutionContext]

  val mockDisplayConstructors: DisplayConstructors = mock[DisplayConstructors]
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
  val mockPlaConnector: PsaLookupConnector         = mock[PsaLookupConnector]
  val mockAuthFunction: AuthFunction               = mock[AuthFunction]
  val mockEnv: Environment                         = mock[Environment]

  val amendPsoDetailsView: amendPsoDetails = inject[amendPsoDetails]
  val technicalErrorView: technicalError   = inject[technicalError]

  override def beforeEach(): Unit = {
    reset(mockSessionCacheService)
    reset(mockPlaConnector)
    reset(mockDisplayConstructors)
    reset(mockAuthConnector)
    reset(mockEnv)
    super.beforeEach()
  }

  val authFunction = new AuthFunctionImpl(
    mcc,
    mockAuthConnector,
    technicalErrorView
  )

  val controller = new AmendsPensionSharingOrderController(
    mockSessionCacheService,
    mcc,
    authFunction,
    amendPsoDetailsView,
    technicalErrorView
  )

  val sessionId: String  = UUID.randomUUID.toString
  val mockUsername       = "mockuser"
  val mockUserId: String = "/auth/oid/" + mockUsername

  val tstPensionContributionNoPsoDisplaySections = Seq(
    AmendDisplaySectionModel(
      "OverseasPensions",
      Seq(
        AmendDisplayRowModel(
          "YesNo",
          Some(
            controllers.routes.AmendsOverseasPensionController
              .amendOverseasPensions(
                AmendableProtectionType.IndividualProtection2014,
                AmendProtectionRequestStatus.Open
              )
          ),
          None,
          "Yes"
        ),
        AmendDisplayRowModel(
          "Amt",
          Some(
            controllers.routes.AmendsOverseasPensionController
              .amendOverseasPensions(
                AmendableProtectionType.IndividualProtection2014,
                AmendProtectionRequestStatus.Open
              )
          ),
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
          Some(
            controllers.routes.AmendsCurrentPensionController
              .amendCurrentPensions(AmendableProtectionType.IndividualProtection2014, AmendProtectionRequestStatus.Open)
          ),
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
    protectionType = AmendableProtectionType.IndividualProtection2014,
    amended = true,
    pensionContributionSections = tstPensionContributionNoPsoDisplaySections,
    psoAdded = false,
    psoSections = Seq.empty,
    totalAmount = "£1,100,000"
  )

  "Calling the amendPsoDetails action" when {
    "there is no amendment model fetched from cache" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      mockFetchAmendProtectionModel(any(), any())(None)

      val result: Future[Result] =
        controller.amendPsoDetails(AmendableProtectionType.IndividualProtection2014, AmendProtectionRequestStatus.Open)(
          fakeRequest
        )

      status(result) shouldBe 500
    }
    "show the technical error page for existing protections" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      mockFetchAmendProtectionModel(any(), any())(None)

      val result: Future[Result] =
        controller.amendPsoDetails(AmendableProtectionType.IndividualProtection2014, AmendProtectionRequestStatus.Open)(
          fakeRequest
        )
      val jsoupDoc: Document = Jsoup.parse(contentAsString(result))

      jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
      jsoupDoc.body
        .getElementById("tryAgainLink")
        .attr("href") shouldEqual s"${controllers.routes.ReadProtectionsController.currentProtections}"
      await(result).header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache"
    }

    "there is no PSO stored in the AmendProtectionModel" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      mockFetchAmendProtectionModel(any(), any())(
        Some(amendDormantIndividualProtection2016)
      )

      val result: Future[Result] =
        controller.amendPsoDetails(AmendableProtectionType.IndividualProtection2014, AmendProtectionRequestStatus.Open)(
          fakeRequest
        )
      val jsoupDoc: Document = Jsoup.parse(contentAsString(result))

      status(result) shouldBe 200

      jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.psoDetails.title")
      jsoupDoc.body.getElementById("pso.day").attr("value") shouldEqual ""
      jsoupDoc.body.getElementById("pso.month").attr("value") shouldEqual ""
      jsoupDoc.body.getElementById("pso.year").attr("value") shouldEqual ""
    }

    "there is a PSO stored in the AmendProtectionModel" in {
      val amendDormantIndividualProtection2014WithPensionDebit =
        amendDormantIndividualProtection2014.withPensionDebit(Some(PensionDebitModel(DateModel.of(2016, 12, 23), 1000)))

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      mockFetchAmendProtectionModel(any(), any())(
        Some(amendDormantIndividualProtection2014WithPensionDebit)
      )

      val result = FakeRequests.authorisedGet(
        controller.amendPsoDetails(AmendableProtectionType.IndividualProtection2016, AmendProtectionRequestStatus.Open)
      )

      status(result) shouldBe 200

      val jsoupDoc = Jsoup.parse(contentAsString(result))

      jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.psoDetails.title")
      jsoupDoc.body.getElementById("pso.day").attr("value") shouldEqual "23"
      jsoupDoc.body.getElementById("pso.month").attr("value") shouldEqual "12"
      jsoupDoc.body.getElementById("pso.year").attr("value") shouldEqual "2016"
      jsoupDoc.body.getElementById("psoAmt").attr("value") shouldEqual "1000"
    }

  }

  "Calling the submitAmendPsoDetails action" when {

    case class TestData(
        amendProtectionModel: AmendProtectionModel,
        psoYear: Int,
        protectionType: AmendableProtectionType
    )

    Seq(
      TestData(
        amendDormantIndividualProtection2014,
        2014,
        AmendableProtectionType.IndividualProtection2014
      ),
      TestData(
        amendDormantIndividualProtection2014LTA,
        2014,
        AmendableProtectionType.IndividualProtection2014LTA
      ),
      TestData(
        amendDormantIndividualProtection2016,
        2016,
        AmendableProtectionType.IndividualProtection2016
      ),
      TestData(
        amendDormantIndividualProtection2016LTA,
        2016,
        AmendableProtectionType.IndividualProtection2016LTA
      )
    ).foreach { testData =>
      s"provided with valid data for ${testData.protectionType}" should {

        val requestData = Seq(
          ("pso.day", "6"),
          ("pso.month", "4"),
          ("pso.year", testData.psoYear.toString),
          ("psoAmt", "100000")
        )

        "return 303 (Redirect) and amendsSummary view" in {
          mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
          mockFetchAmendProtectionModel(any(), any())(Some(testData.amendProtectionModel))
          mockSaveAmendProtectionModel()

          val result: Future[Result] = controller.submitAmendPsoDetails(
            protectionType = testData.protectionType,
            status = AmendProtectionRequestStatus.Open,
            existingPSO = true
          )(authenticatedFakeRequest().withFormUrlEncodedBody(requestData: _*).withMethod("POST"))

          status(result) shouldBe 303
          redirectLocation(result) shouldBe Some(
            s"${routes.AmendsController.amendsSummary(testData.protectionType, AmendProtectionRequestStatus.Open).url}"
          )
        }

        "save correct data into cache" in {
          mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
          mockFetchAmendProtectionModel(any(), any())(Some(testData.amendProtectionModel))
          mockSaveAmendProtectionModel()

          controller
            .submitAmendPsoDetails(
              protectionType = testData.protectionType,
              status = AmendProtectionRequestStatus.Open,
              existingPSO = true
            )(authenticatedFakeRequest().withFormUrlEncodedBody(requestData: _*).withMethod("POST"))
            .futureValue

          val expectedAmendProtectionModel = testData.amendProtectionModel.withPensionDebit(
            Some(PensionDebitModel(DateModel.of(testData.psoYear, 4, 6), 10_000))
          )

          verify(mockSessionCacheService).saveAmendProtectionModel(eqTo(expectedAmendProtectionModel))(any())
        }
      }
    }

    "provided with invalid data" should {

      "return 400 (Bad Request)" in {
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

        val data = Seq(
          ("pso.day", ""),
          ("pso.month", "13"),
          ("pso.year", "2015"),
          ("psoAmt", "100000")
        )

        val result: Future[Result] = controller.submitAmendPsoDetails(
          protectionType = AmendableProtectionType.IndividualProtection2014,
          status = AmendProtectionRequestStatus.Open,
          existingPSO = true
        )(authenticatedFakeRequest().withFormUrlEncodedBody(data: _*).withMethod("POST"))

        status(result) shouldBe 400
      }
    }

    "AmendProtectionModel is NOT found in cache" should {

      "return " in {
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        mockFetchAmendProtectionModel(any(), any())(None)
        mockSaveAmendProtectionModel()

        val requestData = Seq(
          ("pso.day", "6"),
          ("pso.month", "4"),
          ("pso.year", "2014"),
          ("psoAmt", "100000")
        )

        val result: Throwable = controller
          .submitAmendPsoDetails(
            protectionType = AmendableProtectionType.IndividualProtection2014,
            status = AmendProtectionRequestStatus.Open,
            existingPSO = true
          )(authenticatedFakeRequest().withFormUrlEncodedBody(requestData: _*).withMethod("POST"))
          .failed
          .futureValue

        result shouldBe a[Exceptions.RequiredValueNotDefinedException]
        result.getMessage shouldBe "Value not found for amendModel in updateAmendModelWithPso"
      }
    }
  }

}
