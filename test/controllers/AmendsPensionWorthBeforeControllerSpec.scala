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
import config.*
import models.pla.AmendableProtectionType
import models.pla.request.AmendProtectionRequestStatus
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.Environment
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.SessionCacheService
import testHelpers.*
import testdata.AmendProtectionModelTestData
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import views.html.pages.amends.*
import views.html.pages.fallback.technicalError

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class AmendsPensionWorthBeforeControllerSpec
    extends FakeApplication
    with MockitoSugar
    with MockSessionCacheService
    with BeforeAndAfterEach
    with AuthMocks
    with AmendProtectionModelTestData {

  implicit val fakeRequest: FakeRequest[AnyContent] = FakeRequest()

  implicit val messages: Messages = mcc.messagesApi.preferred(fakeRequest)

  implicit val appConfig: AppConfig       = inject[AppConfig]
  implicit val system: ActorSystem        = ActorSystem()
  implicit val materializer: Materializer = mock[Materializer]
  implicit val mockLang: Lang             = mock[Lang]
  implicit val formWithCSRF: FormWithCSRF = inject[FormWithCSRF]
  implicit val ec: ExecutionContext       = inject[ExecutionContext]

  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
  val technicalErrorView: technicalError           = inject[technicalError]

  val amendIP16PensionsWorthBeforeView: amendIP16PensionsWorthBefore =
    inject[amendIP16PensionsWorthBefore]

  val amendIP14PensionsWorthBeforeView: amendIP14PensionsWorthBefore =
    inject[amendIP14PensionsWorthBefore]

  val mockEnv: Environment = mock[Environment]

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(mockSessionCacheService)
    reset(mockEnv)
  }

  val controller = new AmendsPensionWorthBeforeController(
    mockSessionCacheService,
    mcc,
    authActions,
    technicalErrorView,
    amendIP16PensionsWorthBeforeView,
    amendIP14PensionsWorthBeforeView
  )

  val sessionId: String  = UUID.randomUUID.toString
  val mockUsername       = "mockuser"
  val mockUserId: String = "/auth/oid/" + mockUsername

  "AmendsPensionWorthBeforeController" must {

    "return a 200 status" when {

      "IndividualProtection2014 model is returned from the cache" in {

        mockAuthSuccess("AB123456A")
        mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2014))

        val result: Future[Result] =
          controller.amendPensionsWorthBefore(
            AmendableProtectionType.IndividualProtection2014,
            AmendProtectionRequestStatus.Dormant
          )(
            fakeRequest
          )

        status(result) shouldBe OK
        contentAsString(result) should include(messages("pla.ip14PensionsTakenBefore.question"))
      }

      "IndividualProtection2014LTA model is returned from the cache" in {

        mockAuthSuccess("AB123456A")
        mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2014LTA))

        val result: Future[Result] =
          controller.amendPensionsWorthBefore(
            AmendableProtectionType.IndividualProtection2014LTA,
            AmendProtectionRequestStatus.Dormant
          )(
            fakeRequest
          )

        status(result) shouldBe OK
        contentAsString(result) should include(messages("pla.ip14PensionsTakenBefore.question"))
      }

      "IndividualProtection2016 model is returned from cache" in {

        mockAuthSuccess("AB123456A")
        mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2016))

        val result: Future[Result] =
          controller.amendPensionsWorthBefore(
            AmendableProtectionType.IndividualProtection2016,
            AmendProtectionRequestStatus.Dormant
          )(
            fakeRequest
          )

        status(result) shouldBe OK
        contentAsString(result) should include(messages("pla.pensionsWorthBefore.title"))
      }

      "IndividualProtection2016LTA model is returned from cache" in {

        mockAuthSuccess("AB123456A")
        mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2016LTA))

        val result: Future[Result] =
          controller.amendPensionsWorthBefore(
            AmendableProtectionType.IndividualProtection2016LTA,
            AmendProtectionRequestStatus.Dormant
          )(
            fakeRequest
          )

        status(result) shouldBe OK
        contentAsString(result) should include(messages("pla.pensionsWorthBefore.title"))
      }
    }

    "return 500 when nothing is returned from cache" in {

      mockAuthSuccess("AB123456A")
      mockFetchAmendProtectionModel(any(), any())(None)

      val result: Future[Result] =
        controller.amendPensionsWorthBefore(
          AmendableProtectionType.IndividualProtection2016,
          AmendProtectionRequestStatus.Dormant
        )(fakeRequest)

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }

  "Submitting Amend IndividualProtection2016 Pensions Worth Before" when {
    "the data is valid" in {
      mockAuthSuccess("AB123456A")
      mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2016))
      mockSaveAmendProtectionModel()

      val result = FakeRequests.authorisedPost(
        controller.submitAmendPensionsWorthBefore(
          AmendableProtectionType.IndividualProtection2016,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedPensionsTakenBeforeAmt", "10000")
      )

      status(result) shouldBe 303
      redirectLocation(result) shouldBe Some(s"${routes.AmendsController.amendsSummary(
          AmendableProtectionType.IndividualProtection2016,
          AmendProtectionRequestStatus.Dormant
        )}")
    }

    "the data is invalid" in {
      mockAuthSuccess("AB123456A")
      mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2016))
      mockSaveAmendProtectionModel()

      val result = FakeRequests.authorisedPost(
        controller.submitAmendPensionsWorthBefore(
          AmendableProtectionType.IndividualProtection2016,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedPensionsTakenBeforeAmt", "yes")
      )

      status(result) shouldBe 400
    }

    "the model can't be fetched from cache" in {
      mockAuthSuccess("AB123456A")
      mockFetchAmendProtectionModel(any(), any())(None)
      mockSaveAmendProtectionModel()

      val result = FakeRequests.authorisedPost(
        controller.submitAmendPensionsWorthBefore(
          AmendableProtectionType.IndividualProtection2016,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedPensionsTakenBeforeAmt", "10000")
      )

      status(result) shouldBe 500
    }
  }

  "Submitting Amend IndividualProtection2016LTA Pensions Worth Before" when {
    "the data is valid" in {
      mockAuthSuccess("AB123456A")
      mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2016LTA))
      mockSaveAmendProtectionModel()

      val result = FakeRequests.authorisedPost(
        controller.submitAmendPensionsWorthBefore(
          AmendableProtectionType.IndividualProtection2016LTA,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedPensionsTakenBeforeAmt", "10000")
      )

      status(result) shouldBe 303
      redirectLocation(result) shouldBe Some(s"${routes.AmendsController.amendsSummary(
          AmendableProtectionType.IndividualProtection2016LTA,
          AmendProtectionRequestStatus.Dormant
        )}")
    }

    "the data is invalid" in {
      mockAuthSuccess("AB123456A")
      mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2016LTA))

      val result = FakeRequests.authorisedPost(
        controller.submitAmendPensionsWorthBefore(
          AmendableProtectionType.IndividualProtection2016LTA,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedPensionsTakenBeforeAmt", "yes")
      )

      status(result) shouldBe 400
    }

    "the model can't be fetched from cache" in {
      mockAuthSuccess("AB123456A")
      mockFetchAmendProtectionModel(any(), any())(None)

      val result = FakeRequests.authorisedPost(
        controller.submitAmendPensionsWorthBefore(
          AmendableProtectionType.IndividualProtection2016LTA,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedPensionsTakenBeforeAmt", "10000")
      )

      status(result) shouldBe 500
    }
  }

  "Submitting Amend IndividualProtection2014 Pensions Worth Before" when {
    "the data is valid" in {
      mockAuthSuccess("AB123456A")
      mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2014))
      mockSaveAmendProtectionModel()

      val result = FakeRequests.authorisedPost(
        controller.submitAmendPensionsWorthBefore(
          AmendableProtectionType.IndividualProtection2014,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedPensionsTakenBeforeAmt", "10000")
      )

      status(result) shouldBe 303
      redirectLocation(result) shouldBe Some(s"${routes.AmendsController.amendsSummary(
          AmendableProtectionType.IndividualProtection2014,
          AmendProtectionRequestStatus.Dormant
        )}")
    }

    "the data is invalid" in {
      mockAuthSuccess("AB123456A")
      mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2014))

      val result = FakeRequests.authorisedPost(
        controller.submitAmendPensionsWorthBefore(
          AmendableProtectionType.IndividualProtection2014,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedPensionsTakenBeforeAmt", "yes")
      )

      status(result) shouldBe 400
    }

    "the model can't be fetched from cache" in {
      mockAuthSuccess("AB123456A")
      mockFetchAmendProtectionModel(any(), any())(None)

      val result = FakeRequests.authorisedPost(
        controller.submitAmendPensionsWorthBefore(
          AmendableProtectionType.IndividualProtection2014,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedPensionsTakenBeforeAmt", "10000")
      )

      status(result) shouldBe 500
    }
  }

  "Submitting Amend IndividualProtection2014LTA Pensions Worth Before" when {
    "the data is valid" in {
      mockAuthSuccess("AB123456A")
      mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2014LTA))
      mockSaveAmendProtectionModel()

      val result = FakeRequests.authorisedPost(
        controller.submitAmendPensionsWorthBefore(
          AmendableProtectionType.IndividualProtection2014LTA,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedPensionsTakenBeforeAmt", "10000")
      )

      status(result) shouldBe 303
      redirectLocation(result) shouldBe Some(s"${routes.AmendsController.amendsSummary(
          AmendableProtectionType.IndividualProtection2014LTA,
          AmendProtectionRequestStatus.Dormant
        )}")
    }

    "the data is invalid" in {
      mockAuthSuccess("AB123456A")
      mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2014LTA))

      val result = FakeRequests.authorisedPost(
        controller.submitAmendPensionsWorthBefore(
          AmendableProtectionType.IndividualProtection2014LTA,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedPensionsTakenBeforeAmt", "yes")
      )

      status(result) shouldBe 400
    }

    "the model can't be fetched from cache" in {
      mockAuthSuccess("AB123456A")
      mockFetchAmendProtectionModel(any(), any())(None)

      val result = FakeRequests.authorisedPost(
        controller.submitAmendPensionsWorthBefore(
          AmendableProtectionType.IndividualProtection2014LTA,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedPensionsTakenBeforeAmt", "10000")
      )

      status(result) shouldBe 500
    }
  }

}
