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

import auth.AuthFunctionImpl
import config._
import connectors.PlaConnectorError.{ConflictResponseError, LockedResponseError}
import connectors.{CitizenDetailsConnector, PlaConnector}
import constructors.display.DisplayConstructors
import mocks.AuthMock
import models.NotificationId._
import models.amend.AmendsGAModel
import models.cache.CacheMap
import models.display.{AmendDisplayModel, AmendDisplayRowModel, AmendDisplaySectionModel}
import models.pla.AmendableProtectionType
import models.pla.request.AmendProtectionRequestStatus
import models.pla.response.ProtectionStatus.{Dormant, Open}
import models.pla.response.ProtectionType.{FixedProtection2016, IndividualProtection2016}
import models.pla.response.{
  AmendProtectionResponse,
  AmendProtectionResponseStatus,
  ProtectionRecord,
  ProtectionRecordsList,
  ReadProtectionsResponse
}
import models.{DateModel, Person, PersonalDetailsModel, TimeModel}
import org.mockito.ArgumentMatchers.{any, anyString, eq => eqTo}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.SessionCacheService
import testHelpers._
import testdata.AmendProtectionDisplayModelTestData._
import testdata.AmendProtectionModelTestData
import testdata.PlaConnectorTestData.amendProtectionResponse
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import views.html.pages.amends._
import views.html.pages.fallback.technicalError
import views.html.pages.result.manualCorrespondenceNeeded

import scala.concurrent.{ExecutionContext, Future}

class AmendsControllerSpec
    extends FakeApplication
    with MockitoSugar
    with MockSessionCacheService
    with BeforeAndAfterEach
    with AuthMock
    with ScalaFutures
    with AmendProtectionModelTestData
    with I18nSupport {

  private val displayConstructors: DisplayConstructors         = mock[DisplayConstructors]
  private val citizenDetailsConnector: CitizenDetailsConnector = mock[CitizenDetailsConnector]
  private val plaConnector: PlaConnector                       = mock[PlaConnector]
  private val appConfig: FrontendAppConfig                     = mock[FrontendAppConfig]

  private val messagesControllerComponents: MessagesControllerComponents =
    inject[MessagesControllerComponents]

  private val manualCorrespondenceNeededView: manualCorrespondenceNeeded     = mock[manualCorrespondenceNeeded]
  private val technicalErrorView: technicalError                             = mock[technicalError]
  private val amendOutcomeView: amendOutcome                                 = mock[amendOutcome]
  private val amendOutcomeNoNotificationIdView: amendOutcomeNoNotificationId = mock[amendOutcomeNoNotificationId]
  private val amendSummaryView: amendSummary                                 = mock[amendSummary]

  override val messagesApi: MessagesApi                     = messagesControllerComponents.messagesApi
  override val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]

  private val ec: ExecutionContext = inject[ExecutionContext]

  private val authFunction = new AuthFunctionImpl(
    messagesControllerComponents,
    mockAuthConnector,
    technicalErrorView
  )(appConfig, ec)

  private val controller = new AmendsController(
    sessionCacheService = mockSessionCacheService,
    citizenDetailsConnector = citizenDetailsConnector,
    plaConnector = plaConnector,
    displayConstructors = displayConstructors,
    mcc = messagesControllerComponents,
    authFunction = authFunction,
    manualCorrespondenceNeeded = manualCorrespondenceNeededView,
    technicalError = technicalErrorView,
    amendOutcome = amendOutcomeView,
    amendOutcomeNoNotificationId = amendOutcomeNoNotificationIdView,
    amendSummary = amendSummaryView
  )(ec)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionCacheService)
    reset(citizenDetailsConnector)
    reset(plaConnector)
    reset(plaConnector)
    reset(displayConstructors)
    reset(mockAuthConnector)
    reset(manualCorrespondenceNeededView)
    reset(technicalErrorView)
    reset(amendOutcomeView)
    reset(amendOutcomeNoNotificationIdView)
    reset(amendSummaryView)
    reset(appConfig)

    mockAuthRetrieval[Option[String]](Retrievals.nino, Some(testNino))
    when(manualCorrespondenceNeededView.apply()(any(), any())).thenReturn(HtmlFormat.empty)
    when(technicalErrorView.apply()(any(), any())).thenReturn(HtmlFormat.empty)
    when(amendOutcomeView.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(amendOutcomeNoNotificationIdView.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(amendSummaryView.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  private implicit val fakeRequest: FakeRequest[AnyContent] = FakeRequest()

  private val testNino: String = "AB123456A"

  private val psaCheckReference = "PSA12345678A"

  private val testPensionContributionNoPsoDisplaySections = Seq(
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

  private val testAmendDisplayModel = AmendDisplayModel(
    protectionType = AmendableProtectionType.IndividualProtection2014,
    amended = true,
    pensionContributionSections = testPensionContributionNoPsoDisplaySections,
    psoAdded = false,
    psoSections = Seq.empty,
    totalAmount = "£1,100,000"
  )

  private val testPersonalDetails = PersonalDetailsModel(Person("McTestFace", "Testy"))

  "In AmendsController calling the amendsSummary action" when {

    "there is no stored amends model" in {
      mockFetchAmendProtectionModel(any(), any())(None)

      val result = controller.amendsSummary(
        AmendableProtectionType.IndividualProtection2016,
        AmendProtectionRequestStatus.Open
      )(fakeRequest)

      status(result) shouldBe 500
      verify(technicalErrorView).apply()(any(), any())
    }

    "there is a stored, updated amends model" in {
      mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2014))
      when(displayConstructors.createAmendDisplayModel(any())(any())).thenReturn(testAmendDisplayModel)
      val protectionType   = AmendableProtectionType.IndividualProtection2014
      val protectionStatus = AmendProtectionRequestStatus.Dormant

      val result = controller.amendsSummary(protectionType, protectionStatus)(fakeRequest)

      status(result) shouldBe 200
      verify(amendSummaryView)
        .apply(eqTo(testAmendDisplayModel), eqTo(protectionType), eqTo(protectionStatus))(any(), any())
    }
  }

  "Calling the amendProtection action" when {

    "PlaConnector returns a valid response should redirect to amendmentOutcome" in {
      mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2014))
      when(plaConnector.amendProtection(any(), any())(any(), any()))
        .thenReturn(Future.successful(Right(amendProtectionResponse)))
      mockSaveAmendResponseModel()
      mockSaveAmendsGAModel()

      val result =
        controller.amendProtection(
          AmendableProtectionType.IndividualProtection2014,
          AmendProtectionRequestStatus.Dormant
        )(fakeRequest)

      status(result) shouldBe 303
      redirectLocation(result) shouldBe Some(s"${routes.AmendsController.amendmentOutcome}")
      verify(mockSessionCacheService).saveAmendResponseModel(any())(any())
    }

    "PlaConnector returns LockedResponseError should return Locked response and manual correspondence page" in {
      mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2014))
      when(plaConnector.amendProtection(any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(LockedResponseError)))
      mockSaveAmendsGAModel()

      val result =
        controller.amendProtection(
          AmendableProtectionType.IndividualProtection2014,
          AmendProtectionRequestStatus.Dormant
        )(fakeRequest)

      status(result) shouldBe 423
      verify(manualCorrespondenceNeededView).apply()(any(), any())
    }

    "PlaConnector returns ConflictResponseError should return InternalServerError and technical error page" in {
      mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2014))
      when(plaConnector.amendProtection(any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(ConflictResponseError)))
      mockSaveAmendsGAModel()

      val result =
        controller.amendProtection(
          AmendableProtectionType.IndividualProtection2014,
          AmendProtectionRequestStatus.Dormant
        )(fakeRequest)

      status(result) shouldBe 500
      verify(technicalErrorView).apply()(any(), any())
    }

    "PlaConnector returns a response with no notificationId" should {
      "redirect to amendment outcome page" in {
        val response = AmendProtectionResponse(
          lifetimeAllowanceIdentifier = 1,
          lifetimeAllowanceSequenceNumber = 1,
          lifetimeAllowanceType = AmendableProtectionType.IndividualProtection2014,
          certificateDate = Some(DateModel.of(2025, 10, 28)),
          certificateTime = Some(TimeModel.of(9, 38, 20)),
          status = AmendProtectionResponseStatus.Open,
          protectionReference = Some("psaRef"),
          relevantAmount = 1_350_000,
          preADayPensionInPaymentAmount = 375_000,
          postADayBenefitCrystallisationEventAmount = 375_000,
          uncrystallisedRightsAmount = 375_000,
          nonUKRightsAmount = 375_000,
          pensionDebitAmount = None,
          pensionDebitEnteredAmount = None,
          notificationIdentifier = None,
          protectedAmount = Some(1_350_000),
          pensionDebitStartDate = None,
          pensionDebitTotalAmount = Some(150_000)
        )

        mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2014))
        when(plaConnector.amendProtection(any(), any())(any(), any()))
          .thenReturn(Future.successful(Right(response)))
        mockSaveAmendsGAModel()
        mockSaveAmendResponseModel()

        val result =
          controller.amendProtection(
            AmendableProtectionType.IndividualProtection2014,
            AmendProtectionRequestStatus.Dormant
          )(fakeRequest)

        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some(routes.AmendsController.amendmentOutcome)

        verify(mockSessionCacheService).saveAmendResponseModel(any())(any())
      }
    }
  }

  "AmendsController on amendmentOutcome" when {

    val emptyAmendsGAModel: AmendsGAModel = AmendsGAModel(None, None, None, None, None)

    "there is no AmendResponseModel stored in cache" should {
      "return Internal Server Error" in {
        mockFetchAmendResponseModel(None)
        mockFetchAmendsGAModel(Some(emptyAmendsGAModel))
        when(citizenDetailsConnector.getPersonDetails(anyString())(any()))
          .thenReturn(Future.successful(Some(testPersonalDetails)))

        val result = controller.amendmentOutcome()(fakeRequest)

        status(result) shouldBe INTERNAL_SERVER_ERROR
        verify(technicalErrorView).apply()(any(), any())
      }
    }

    Seq(
      NotificationId1  -> amendResponseModelNotification1  -> amendOutcomeDisplayModelNotification1,
      NotificationId2  -> amendResponseModelNotification2  -> amendOutcomeDisplayModelNotification2,
      NotificationId3  -> amendResponseModelNotification3  -> amendOutcomeDisplayModelNotification3,
      NotificationId4  -> amendResponseModelNotification4  -> amendOutcomeDisplayModelNotification4,
      NotificationId5  -> amendResponseModelNotification5  -> amendOutcomeDisplayModelNotification5,
      NotificationId6  -> amendResponseModelNotification6  -> amendOutcomeDisplayModelNotification6,
      NotificationId8  -> amendResponseModelNotification8  -> amendOutcomeDisplayModelNotification8,
      NotificationId9  -> amendResponseModelNotification9  -> amendOutcomeDisplayModelNotification9,
      NotificationId10 -> amendResponseModelNotification10 -> amendOutcomeDisplayModelNotification10,
      NotificationId11 -> amendResponseModelNotification11 -> amendOutcomeDisplayModelNotification11,
      NotificationId12 -> amendResponseModelNotification12 -> amendOutcomeDisplayModelNotification12,
      NotificationId13 -> amendResponseModelNotification13 -> amendOutcomeDisplayModelNotification13
    ).foreach { case ((notificationId, amendResponseModel), amendOutcomeDisplayModel) =>
      s"AmendResponseModel stored in cache contains notification ID: $notificationId" should {
        "return Ok status with amendOutcome view" in {
          mockFetchAmendResponseModel(Some(amendResponseModel))
          mockFetchAmendsGAModel(Some(emptyAmendsGAModel))
          when(citizenDetailsConnector.getPersonDetails(anyString())(any()))
            .thenReturn(Future.successful(Some(testPersonalDetails)))
          when(mockSessionCacheService.saveOpenProtection(any())(any()))
            .thenReturn(Future.successful(CacheMap("", Map.empty)))
          when(displayConstructors.createAmendOutcomeDisplayModel(any(), any(), anyString(), any())(any()))
            .thenReturn(amendOutcomeDisplayModel)

          val result = controller.amendmentOutcome()(fakeRequest)

          status(result) shouldBe OK
          verify(mockSessionCacheService).saveOpenProtection(eqTo(amendResponseModel.toProtectionModel))(any())
          verify(amendOutcomeView).apply(eqTo(amendOutcomeDisplayModel))(any(), any())
        }
      }
    }

    Seq(
      NotificationId7  -> amendResponseModelNotification7  -> amendOutcomeDisplayModelNotification7,
      NotificationId14 -> amendResponseModelNotification14 -> amendOutcomeDisplayModelNotification14
    )
      .foreach { case ((notificationId, amendResponseModel), amendOutcomeDisplayModel) =>
        s"AmendResponseModel stored in cache contains notification ID: $notificationId" should {
          val individualProtectionRecord = ProtectionRecord(
            identifier = 2,
            sequenceNumber = 2,
            `type` = IndividualProtection2016,
            certificateDate = DateModel.of(2016, 9, 4),
            certificateTime = TimeModel.of(9, 0, 19),
            status = Dormant,
            protectionReference = Some("IP16123001"),
            relevantAmount = Some(1000001),
            preADayPensionInPaymentAmount = Some(3000),
            postADayBenefitCrystallisationEventAmount = Some(100),
            uncrystallisedRightsAmount = Some(100),
            nonUKRightsAmount = Some(100),
            pensionDebitAmount = Some(100),
            pensionDebitEnteredAmount = Some(100),
            protectedAmount = Some(100000),
            pensionDebitStartDate = Some(DateModel.of(2016, 9, 4)),
            pensionDebitTotalAmount = Some(10000),
            lumpSumAmount = None,
            lumpSumPercentage = None,
            enhancementFactor = Some(5.6)
          )

          val fixedProtectionRecord = individualProtectionRecord.copy(
            `type` = FixedProtection2016,
            status = Open,
            protectionReference = Some("FP16123456")
          )

          val readProtectionResponseModel = {
            val fixedProtectionRecordsList      = ProtectionRecordsList(fixedProtectionRecord, None)
            val individualProtectionRecordsList = ProtectionRecordsList(individualProtectionRecord, None)

            ReadProtectionsResponse(
              psaCheckReference,
              Some(Seq(fixedProtectionRecordsList, individualProtectionRecordsList))
            )
          }

          "call PLAConnector" in {
            mockFetchAmendResponseModel(Some(amendResponseModel))
            mockFetchAmendsGAModel(Some(emptyAmendsGAModel))
            when(citizenDetailsConnector.getPersonDetails(anyString())(any()))
              .thenReturn(Future.successful(Some(testPersonalDetails)))
            when(plaConnector.readProtections(any())(any(), any()))
              .thenReturn(Future.successful(Right(readProtectionResponseModel)))
            when(displayConstructors.createAmendOutcomeDisplayModel(any(), any(), anyString(), any())(any()))
              .thenReturn(amendOutcomeDisplayModel)

            controller.amendmentOutcome()(fakeRequest).futureValue

            verify(plaConnector).readProtections(eqTo(testNino))(any(), any())
          }

          "call SessionCacheService.saveFormData providing correct data" in {
            mockFetchAmendResponseModel(Some(amendResponseModel))
            mockFetchAmendsGAModel(Some(emptyAmendsGAModel))
            when(citizenDetailsConnector.getPersonDetails(anyString())(any()))
              .thenReturn(Future.successful(Some(testPersonalDetails)))
            when(plaConnector.readProtections(any())(any(), any()))
              .thenReturn(Future.successful(Right(readProtectionResponseModel)))
            when(mockSessionCacheService.saveOpenProtection(any())(any()))
              .thenReturn(Future.successful(CacheMap("", Map.empty)))
            when(displayConstructors.createAmendOutcomeDisplayModel(any(), any(), anyString(), any())(any()))
              .thenReturn(amendOutcomeDisplayModel)

            controller.amendmentOutcome()(fakeRequest).futureValue

            verify(mockSessionCacheService)
              .saveOpenProtection(eqTo(amendResponseModel.toProtectionModel))(any())
          }

          "return Ok status with amendOutcome view" in {
            mockFetchAmendResponseModel(Some(amendResponseModel))
            mockFetchAmendsGAModel(Some(emptyAmendsGAModel))
            when(citizenDetailsConnector.getPersonDetails(anyString())(any()))
              .thenReturn(Future.successful(Some(testPersonalDetails)))
            when(plaConnector.readProtections(any())(any(), any()))
              .thenReturn(Future.successful(Right(readProtectionResponseModel)))
            when(displayConstructors.createAmendOutcomeDisplayModel(any(), any(), anyString(), any())(any()))
              .thenReturn(amendOutcomeDisplayModel)

            val result = controller.amendmentOutcome()(fakeRequest)

            status(result) shouldBe OK
            verify(amendOutcomeView).apply(eqTo(amendOutcomeDisplayModel))(any(), any())
          }
        }
      }

    "AmendResponseModel stored in cache contains no notification ID" should {
      import testdata.AmendProtectionDisplayModelTestData._

      "return Ok status with amendOutcomeNoNotificationId view" in {
        val amendResponseModel = amendResponseModelNoNotificationIdIndividualProtection2014

        val amendResultDisplayModel = amendOutcomeDisplayModelNoNotificationIdIndividualProtection2014

        mockFetchAmendResponseModel(Some(amendResponseModel))
        mockFetchAmendsGAModel(Some(emptyAmendsGAModel))
        when(citizenDetailsConnector.getPersonDetails(anyString())(any()))
          .thenReturn(Future.successful(Some(testPersonalDetails)))
        when(displayConstructors.createAmendOutcomeDisplayModelNoNotificationId(any(), any(), any())(any()))
          .thenReturn(amendResultDisplayModel)

        val result = controller.amendmentOutcome()(fakeRequest)

        status(result) shouldBe OK
        verify(amendOutcomeNoNotificationIdView).apply(eqTo(amendResultDisplayModel))(any(), any())
      }

    }

  }

}
