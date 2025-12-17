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
import org.mockito.ArgumentMatchers.any
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
import testdata.AmendProtectionModelTestData
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import views.html.pages.amends._
import views.html.pages.fallback.technicalError

import scala.concurrent.ExecutionContext

class AmendsCurrentPensionControllerSpec
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

  implicit val mockMessage: Messages = messagesApi.preferred(fakeRequest)

  implicit val appConfig: FrontendAppConfig   = inject[FrontendAppConfig]
  implicit val system: ActorSystem            = ActorSystem()
  implicit val mockMaterializer: Materializer = mock[Materializer]
  implicit val mockLang: Lang                 = mock[Lang]
  implicit val formWithCSRF: FormWithCSRF     = inject[FormWithCSRF]
  implicit val ec: ExecutionContext           = inject[ExecutionContext]

  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
  val mockAuthFunction: AuthFunction               = mock[AuthFunction]
  val mockEnv: Environment                         = mock[Environment]

  val technicalErrorView: technicalError                     = inject[technicalError]
  val amendIP16CurrentPensionsView: amendIP16CurrentPensions = inject[amendIP16CurrentPensions]
  val amendIP14CurrentPensionsView: amendIP14CurrentPensions = inject[amendIP14CurrentPensions]

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

  val controller = new AmendsCurrentPensionController(
    mockSessionCacheService,
    mcc,
    authFunction,
    technicalErrorView,
    amendIP16CurrentPensionsView,
    amendIP14CurrentPensionsView
  )

  "Calling the .amendCurrentPensions action" when {

    "not supplied with a stored model" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      mockFetchAmendProtectionModel(any(), any())(None)

      val result =
        controller.amendCurrentPensions(
          AmendableProtectionType.IndividualProtection2016,
          AmendProtectionRequestStatus.Open
        )(
          fakeRequest
        )

      status(result) shouldBe 500
    }

    "supplied with a stored test model (£100000, IndividualProtection2016, dormant)" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2016))

      val result =
        controller.amendCurrentPensions(
          AmendableProtectionType.IndividualProtection2016,
          AmendProtectionRequestStatus.Dormant
        )(
          fakeRequest
        )
      val jsoupDoc = Jsoup.parse(contentAsString(result))

      status(result) shouldBe 200

      jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.currentPensions.title")
    }

    "return some HTML that" should {

      "contain some text and use the character set utf-8" in {
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2016))

        val result =
          controller.amendCurrentPensions(
            AmendableProtectionType.IndividualProtection2016,
            AmendProtectionRequestStatus.Dormant
          )(fakeRequest)

        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }

      "have the value 100000 completed in the amount input by default" in {
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2016))

        val result =
          controller.amendCurrentPensions(
            AmendableProtectionType.IndividualProtection2016,
            AmendProtectionRequestStatus.Dormant
          )(fakeRequest)
        val jsoupDoc = Jsoup.parse(contentAsString(result))

        jsoupDoc.body.getElementById("amendedUKPensionAmt").attr("value") shouldBe "100000"
      }
    }
  }

  "supplied with a stored test model (£100000, IndividualProtection2014, dormant)" in {
    mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
    mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2014))

    val result =
      controller.amendCurrentPensions(
        AmendableProtectionType.IndividualProtection2014,
        AmendProtectionRequestStatus.Dormant
      )(fakeRequest)

    status(result) shouldBe 200
  }

  "supplied with a stored test model (£100000, IndividualProtection2014LTA, dormant)" in {
    mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
    mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2014LTA))

    val result =
      controller.amendCurrentPensions(
        AmendableProtectionType.IndividualProtection2014LTA,
        AmendProtectionRequestStatus.Dormant
      )(fakeRequest)

    status(result) shouldBe 200
  }

  "Submitting Amend IndividualProtection2016 Current Pensions data" when {

    "the data is valid" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      mockSaveAmendProtectionModel()
      mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2016))

      val result = FakeRequests.authorisedPost(
        controller.submitAmendCurrentPension(
          AmendableProtectionType.IndividualProtection2016,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedUKPensionAmt", "100000")
      )

      status(result) shouldBe 303
      redirectLocation(result) shouldBe Some(s"${routes.AmendsController.amendsSummary(
          AmendableProtectionType.IndividualProtection2016,
          AmendProtectionRequestStatus.Dormant
        )}")
    }

    "the data is invalid" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

      val result = FakeRequests.authorisedPost(
        controller.submitAmendCurrentPension(
          AmendableProtectionType.IndividualProtection2016,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedUKPensionAmt", "")
      )

      status(result) shouldBe 400
    }

    "the model can't be fetched from cache" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      mockFetchAmendProtectionModel(any(), any())(None)

      val result = FakeRequests.authorisedPost(
        controller
          .submitAmendCurrentPension(
            AmendableProtectionType.IndividualProtection2016,
            AmendProtectionRequestStatus.Dormant
          ),
        ("amendedUKPensionAmt", "1000000")
      )

      status(result) shouldBe 500
    }
  }

  "Submitting Amend IndividualProtection2014 Current Pensions data" when {

    "the data is valid" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      mockSaveAmendProtectionModel()
      mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2014))

      val result = FakeRequests.authorisedPost(
        controller.submitAmendCurrentPension(
          AmendableProtectionType.IndividualProtection2014,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedUKPensionAmt", "100000")
      )

      status(result) shouldBe 303
      redirectLocation(result) shouldBe Some(
        s"${routes.AmendsController.amendsSummary(AmendableProtectionType.IndividualProtection2014, AmendProtectionRequestStatus.Dormant)}"
      )
    }

    "the data is invalid" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

      val result = FakeRequests.authorisedPost(
        controller.submitAmendCurrentPension(
          AmendableProtectionType.IndividualProtection2014,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedUKPensionAmt", "")
      )

      status(result) shouldBe 400
    }

    "the model can't be fetched from cache" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      mockFetchAmendProtectionModel(any(), any())(None)

      val result = FakeRequests.authorisedPost(
        controller.submitAmendCurrentPension(
          AmendableProtectionType.IndividualProtection2014,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedUKPensionAmt", "1000000")
      )

      status(result) shouldBe 500
    }
  }

  "Submitting Amend IndividualProtection2016LTA Current Pensions data" when {

    "the data is valid" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      mockSaveAmendProtectionModel()
      mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2016LTA))

      val result = FakeRequests.authorisedPost(
        controller.submitAmendCurrentPension(
          AmendableProtectionType.IndividualProtection2016LTA,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedUKPensionAmt", "100000")
      )

      status(result) shouldBe 303
      redirectLocation(result) shouldBe Some(s"${routes.AmendsController.amendsSummary(
          AmendableProtectionType.IndividualProtection2016LTA,
          AmendProtectionRequestStatus.Dormant
        )}")
    }

    "the data is invalid" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

      val result = FakeRequests.authorisedPost(
        controller.submitAmendCurrentPension(
          AmendableProtectionType.IndividualProtection2016LTA,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedUKPensionAmt", "")
      )

      status(result) shouldBe 400
    }

    "the model can't be fetched from cache" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      mockFetchAmendProtectionModel(any(), any())(None)

      val result = FakeRequests.authorisedPost(
        controller.submitAmendCurrentPension(
          AmendableProtectionType.IndividualProtection2016LTA,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedUKPensionAmt", "1000000")
      )

      status(result) shouldBe 500
    }
  }

  "Submitting Amend IndividualProtection2014LTA Current Pensions data" when {

    "the data is valid" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      mockSaveAmendProtectionModel()
      mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2014LTA))

      val result = FakeRequests.authorisedPost(
        controller.submitAmendCurrentPension(
          AmendableProtectionType.IndividualProtection2014LTA,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedUKPensionAmt", "100000")
      )

      status(result) shouldBe 303
      redirectLocation(result) shouldBe Some(
        s"${routes.AmendsController.amendsSummary(AmendableProtectionType.IndividualProtection2014LTA, AmendProtectionRequestStatus.Dormant)}"
      )
    }

    "the data is invalid" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

      val result = FakeRequests.authorisedPost(
        controller.submitAmendCurrentPension(
          AmendableProtectionType.IndividualProtection2014LTA,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedUKPensionAmt", "")
      )

      status(result) shouldBe 400
    }

    "the model can't be fetched from cache" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      mockFetchAmendProtectionModel(any(), any())(None)

      val result = FakeRequests.authorisedPost(
        controller.submitAmendCurrentPension(
          AmendableProtectionType.IndividualProtection2014LTA,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedUKPensionAmt", "1000000")
      )

      status(result) shouldBe 500
    }
  }

}
