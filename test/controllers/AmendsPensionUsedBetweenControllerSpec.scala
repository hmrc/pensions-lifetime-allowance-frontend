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
import mocks.AuthMock
import models.pla.AmendableProtectionType
import models.pla.request.AmendProtectionRequestStatus
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.ArgumentMatchers.any
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
import testdata.AmendProtectionModelTestData
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import views.html.pages.amends._
import views.html.pages.fallback.technicalError

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class AmendsPensionUsedBetweenControllerSpec
    extends FakeApplication
    with MockitoSugar
    with MockSessionCacheService
    with BeforeAndAfterEach
    with AuthMock
    with AmendProtectionModelTestData
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
  val mockAuthFunction: AuthFunction               = mock[AuthFunction]
  val technicalErrorView: technicalError           = inject[technicalError]

  val amendIP16PensionsUsedBetweenView: amendIP16PensionsUsedBetween =
    inject[amendIP16PensionsUsedBetween]

  val amendIP14PensionsUsedBetweenView: amendIP14PensionsUsedBetween =
    inject[amendIP14PensionsUsedBetween]

  val mockEnv: Environment = mock[Environment]

  override def beforeEach(): Unit = {
    reset(mockSessionCacheService)
    reset(mockAuthConnector)
    reset(mockEnv)
    super.beforeEach()
  }

  val authFunction = new AuthFunctionImpl(
    mcc,
    mockAuthConnector,
    technicalErrorView
  )

  val controller = new AmendsPensionUsedBetweenController(
    mockSessionCacheService,
    mcc,
    authFunction,
    technicalErrorView,
    amendIP16PensionsUsedBetweenView,
    amendIP14PensionsUsedBetweenView
  )

  val sessionId: String  = UUID.randomUUID.toString
  val mockUsername       = "mockuser"
  val mockUserId: String = "/auth/oid/" + mockUsername

  "In AmendsPensionUsedBetweenController calling the .amendPensionsUsedBetween action" when {
    "not supplied with a stored model" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      mockFetchAmendProtectionModel(any(), any())(None)

      val result: Future[Result] =
        controller.amendPensionsUsedBetween(
          AmendableProtectionType.IndividualProtection2016,
          AmendProtectionRequestStatus.Open
        )(fakeRequest)

      status(result) shouldBe 500

    }

    "supplied with the stored test model for (dormant, IndividualProtection2014, preADay = £0.0)" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2014))

      val result: Future[Result] =
        controller.amendPensionsUsedBetween(
          AmendableProtectionType.IndividualProtection2014,
          AmendProtectionRequestStatus.Dormant
        )(fakeRequest)
      val jsoupDoc: Document = Jsoup.parse(contentAsString(result))

      jsoupDoc.body
        .getElementById("amendedPensionsUsedBetweenAmt")
        .attr("class") shouldBe "govuk-input govuk-input--width-10"
    }

  }

  "Submitting Amend IndividualProtection2016 Pensions Used Between data" when {

    "the model can't be fetched from cache" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      mockFetchAmendProtectionModel(any(), any())(None)

      val result = FakeRequests.authorisedPost(
        controller.submitAmendPensionsUsedBetween(
          AmendableProtectionType.IndividualProtection2016,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedPensionsUsedBetweenAmt", "0")
      )

      status(result) shouldBe 500
    }

    "the data is invalid on validation" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

      val result = FakeRequests.authorisedPost(
        controller.submitAmendPensionsUsedBetween(
          AmendableProtectionType.IndividualProtection2016,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedPensionsUsedBetweenAmt", "yes")
      )

      status(result) shouldBe 400
    }

    "the data is valid with a no response" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2016))
      mockSaveAmendProtectionModel()

      val result = FakeRequests.authorisedPost(
        controller.submitAmendPensionsUsedBetween(
          AmendableProtectionType.IndividualProtection2016,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedPensionsUsedBetweenAmt", "0")
      )

      status(result) shouldBe 303

      redirectLocation(result) shouldBe Some(s"${routes.AmendsController.amendsSummary(
          AmendableProtectionType.IndividualProtection2016,
          AmendProtectionRequestStatus.Dormant
        )}")
    }
  }

  "Submitting Amend IndividualProtection2016LTA Pensions Used Between data" when {

    "the model can't be fetched from cache" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      mockFetchAmendProtectionModel(any(), any())(None)

      val result = FakeRequests.authorisedPost(
        controller.submitAmendPensionsUsedBetween(
          AmendableProtectionType.IndividualProtection2016LTA,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedPensionsUsedBetweenAmt", "0")
      )

      status(result) shouldBe 500
    }

    "the data is invalid on validation" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

      val result = FakeRequests.authorisedPost(
        controller.submitAmendPensionsUsedBetween(
          AmendableProtectionType.IndividualProtection2016LTA,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedPensionsUsedBetweenAmt", "yes")
      )

      status(result) shouldBe 400
    }

    "the data is valid with a no response" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2016LTA))
      mockSaveAmendProtectionModel()

      val result = FakeRequests.authorisedPost(
        controller.submitAmendPensionsUsedBetween(
          AmendableProtectionType.IndividualProtection2016LTA,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedPensionsUsedBetweenAmt", "0")
      )

      status(result) shouldBe 303

      redirectLocation(result) shouldBe Some(s"${routes.AmendsController.amendsSummary(
          AmendableProtectionType.IndividualProtection2016LTA,
          AmendProtectionRequestStatus.Dormant
        )}")
    }
  }

  "Submitting Amend IndividualProtection2014 Pensions Used Between data" when {

    "the model can't be fetched from cache" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      mockFetchAmendProtectionModel(any(), any())(None)

      val result = FakeRequests.authorisedPost(
        controller.submitAmendPensionsUsedBetween(
          AmendableProtectionType.IndividualProtection2014,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedPensionsUsedBetweenAmt", "0")
      )

      status(result) shouldBe 500
    }

    "the data is invalid on validation" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

      val result = FakeRequests.authorisedPost(
        controller.submitAmendPensionsUsedBetween(
          AmendableProtectionType.IndividualProtection2014,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedPensionsUsedBetweenAmt", "yes")
      )

      status(result) shouldBe 400
    }

    "the data is valid with a no response" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2014))
      mockSaveAmendProtectionModel()

      val result = FakeRequests.authorisedPost(
        controller.submitAmendPensionsUsedBetween(
          AmendableProtectionType.IndividualProtection2014,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedPensionsUsedBetweenAmt", "0")
      )

      status(result) shouldBe 303

      redirectLocation(result) shouldBe Some(s"${routes.AmendsController.amendsSummary(
          AmendableProtectionType.IndividualProtection2014,
          AmendProtectionRequestStatus.Dormant
        )}")
    }
  }

  "Submitting Amend IndividualProtection2014LTA Pensions Used Between data" when {

    "the model can't be fetched from cache" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      mockFetchAmendProtectionModel(any(), any())(None)

      val result = FakeRequests.authorisedPost(
        controller.submitAmendPensionsUsedBetween(
          AmendableProtectionType.IndividualProtection2014LTA,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedPensionsUsedBetweenAmt", "0")
      )

      status(result) shouldBe 500
    }

    "the data is invalid on validation" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

      val result = FakeRequests.authorisedPost(
        controller.submitAmendPensionsUsedBetween(
          AmendableProtectionType.IndividualProtection2014LTA,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedPensionsUsedBetweenAmt", "yes")
      )

      status(result) shouldBe 400
    }

    "the data is valid with a no response" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2014LTA))
      mockSaveAmendProtectionModel()

      val result = FakeRequests.authorisedPost(
        controller.submitAmendPensionsUsedBetween(
          AmendableProtectionType.IndividualProtection2014LTA,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedPensionsUsedBetweenAmt", "0")
      )

      status(result) shouldBe 303

      redirectLocation(result) shouldBe Some(s"${routes.AmendsController.amendsSummary(
          AmendableProtectionType.IndividualProtection2014LTA,
          AmendProtectionRequestStatus.Dormant
        )}")
    }
  }

}
