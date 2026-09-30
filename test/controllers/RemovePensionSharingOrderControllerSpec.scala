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

import auth.helpers.AuthMocks
import models.*
import models.amend.AmendProtectionModel
import models.pla.AmendableProtectionType
import models.pla.request.AmendProtectionRequestStatus
import models.pla.response.ProtectionStatus.Dormant
import models.pla.response.ProtectionType.IndividualProtection2016
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import play.api.i18n.Messages
import play.api.mvc.{AnyContentAsEmpty}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.SessionCacheService
import testHelpers.*
import testdata.AmendProtectionModelTestData
import views.html.pages.amends.removePensionSharingOrder
import views.html.pages.fallback.technicalError

import scala.concurrent.{ExecutionContext, Future}

class RemovePensionSharingOrderControllerSpec
  extends FakeApplication
    with MockSessionCacheService
    with AuthMocks
    with AmendProtectionModelTestData {

  private val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  private val messages: Messages = mcc.messagesApi.preferred(fakeRequest)

  private val executionContext: ExecutionContext = ExecutionContext.global

  override val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]

  private val technicalErrorView: technicalError = inject[technicalError]
  private val removePsoDebitsView: removePensionSharingOrder = inject[removePensionSharingOrder]

  private val controller = new removePensionSharingOrderController(
    mockSessionCacheService,
    mcc,
    authActions,
    technicalErrorView,
    removePsoDebitsView
  )(using executionContext)

  private val individualProtection2016 = ProtectionModel(
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

  private val amendIndividualProtection2016: AmendProtectionModel =
    AmendProtectionModel.tryFromProtection(individualProtection2016).get

  private val pensionDebit = PensionDebitModel(DateModel.of(2016, 12, 23), 1000.0)

  private val amendIndividualProtection2016WithPso: AmendProtectionModel =
    amendIndividualProtection2016.withPensionDebit(Some(pensionDebit))

  "Removing a recently added PSO" when {
    "show the remove pension sharing order page with heading and yes/no radio options" in {
      mockAuthSuccess("AB123456A")
      mockFetchAmendProtectionModel(any(), any())(
        Some(amendIndividualProtection2016WithPso)
      )

      val result =
        controller.removePso(
          AmendableProtectionType.IndividualProtection2016,
          AmendProtectionRequestStatus.Open
        )(fakeRequest)

      status(result) shouldBe OK

      val jsoupDoc = Jsoup.parse(contentAsString(result))

      jsoupDoc.select("h1").text shouldBe
        messages("pla.deletePensionSharing.title")

      jsoupDoc.body.text should include(messages("pla.base.yes"))
      jsoupDoc.body.text should include(messages("pla.base.no"))
    }

    "remove the pension sharing order and redirect when yes is selected" in {
      mockAuthSuccess("AB123456A")
      mockFetchAmendProtectionModel(any(), any())(
        Some(amendIndividualProtection2016WithPso)
      )
      mockSaveAmendProtectionModel()

      val request = FakeRequest(POST, "/")
        .withFormUrlEncodedBody(
          "removePensionSharing" -> "yes"
        )

      val result =
        controller.submitRemovePso(
          AmendableProtectionType.IndividualProtection2016,
          AmendProtectionRequestStatus.Open
        )(request)

      status(result) shouldBe SEE_OTHER

      redirectLocation(result) shouldBe Some(
        routes.AmendsController
          .amendsSummary(
            AmendableProtectionType.IndividualProtection2016,
            AmendProtectionRequestStatus.Open
          )
          .url
      )

      verify(mockSessionCacheService).saveAmendProtectionModel(
        eqTo(amendIndividualProtection2016)
      )(using any())
    }
    "not remove the pension sharing order and redirect when no is selected" in {
      mockAuthSuccess("AB123456A")
      mockFetchAmendProtectionModel(any(), any())(
        Some(amendIndividualProtection2016WithPso)
      )

      val request = FakeRequest(POST, "/")
        .withFormUrlEncodedBody(
          "removePensionSharing" -> "no"
        )

      val result =
        controller.submitRemovePso(
          AmendableProtectionType.IndividualProtection2016,
          AmendProtectionRequestStatus.Open
        )(request)

      status(result) shouldBe SEE_OTHER

      redirectLocation(result) shouldBe Some(
        routes.AmendsController
          .amendsSummary(
            AmendableProtectionType.IndividualProtection2016,
            AmendProtectionRequestStatus.Open
          )
          .url
      )

      verify(mockSessionCacheService, never())
        .saveAmendProtectionModel(any())(using any())
    }
  }
}