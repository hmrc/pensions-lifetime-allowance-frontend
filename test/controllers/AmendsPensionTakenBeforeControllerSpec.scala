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

class AmendsPensionTakenBeforeControllerSpec
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

  implicit val appConfig: FrontendAppConfig   = inject[FrontendAppConfig]
  implicit val system: ActorSystem            = ActorSystem()
  implicit val mockMaterializer: Materializer = mock[Materializer]
  implicit val mockLang: Lang                 = mock[Lang]
  implicit val formWithCSRF: FormWithCSRF     = inject[FormWithCSRF]
  implicit val ec: ExecutionContext           = inject[ExecutionContext]

  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
  val mockAuthFunction: AuthFunction               = mock[AuthFunction]
  val technicalErrorView: technicalError           = inject[technicalError]

  val amendIP16PensionsTakenBeforeView: amendIP16PensionsTakenBefore =
    inject[amendIP16PensionsTakenBefore]

  val amendIP14PensionsTakenBeforeView: amendIP14PensionsTakenBefore =
    inject[amendIP14PensionsTakenBefore]

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

  val controller = new AmendsPensionTakenBeforeController(
    mockSessionCacheService,
    mcc,
    authFunction,
    technicalErrorView,
    amendIP16PensionsTakenBeforeView,
    amendIP14PensionsTakenBeforeView
  )

  val sessionId: String  = UUID.randomUUID.toString
  val mockUsername       = "mockuser"
  val mockUserId: String = "/auth/oid/" + mockUsername

  "In AmendsPensionTakenBeforeController calling the .amendPensionsTakenBefore action" when {

    "not supplied with a stored model" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      mockFetchAmendProtectionModel(any(), any())(None)

      val result: Future[Result] =
        controller.amendPensionsTakenBefore(
          AmendableProtectionType.IndividualProtection2016,
          AmendProtectionRequestStatus.Open
        )(fakeRequest)

      status(result) shouldBe 500
    }

    "supplied with the stored test model for (dormant, IndividualProtection2016, preADay = £0.0)" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2016))
    }

    "supplied with the stored test model for (dormant, IndividualProtection2016, preADay = £2000)" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2016))

      val result: Future[Result] =
        controller.amendPensionsTakenBefore(
          AmendableProtectionType.IndividualProtection2016,
          AmendProtectionRequestStatus.Dormant
        )(fakeRequest)

      status(result) shouldBe 200
    }

    "should take the user to the pensions taken before page" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2016))

      val result: Future[Result] =
        controller.amendPensionsTakenBefore(
          AmendableProtectionType.IndividualProtection2016,
          AmendProtectionRequestStatus.Dormant
        )(fakeRequest)
      val jsoupDoc: Document = Jsoup.parse(contentAsString(result))

      jsoupDoc.body.getElementsByClass("govuk-heading-xl").text shouldEqual Messages("pla.pensionsTakenBefore.title")
    }

    "return some HTML that" should {

      "contain some text and use the character set utf-8" in {
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2016))

        val result: Future[Result] =
          controller.amendPensionsTakenBefore(
            AmendableProtectionType.IndividualProtection2016,
            AmendProtectionRequestStatus.Dormant
          )(
            fakeRequest
          )

        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }

      "have the value of the check box set as 'Yes' by default" in {
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2016))
      }

      "have the value of the input field set to 2000 by default" in {
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2016))
      }
    }
    "supplied with the stored test model for (dormant, IndividualProtection2014, preADay = £2000)" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2014))

      val result: Future[Result] =
        controller.amendPensionsTakenBefore(
          AmendableProtectionType.IndividualProtection2016,
          AmendProtectionRequestStatus.Dormant
        )(fakeRequest)

      status(result) shouldBe 200
    }
  }

  "Submitting Amend IndividualProtection2016 Pensions Taken Before data" when {

    "the data is invalid" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

      val result: Future[Result] =
        controller.submitAmendPensionsTakenBefore(
          AmendableProtectionType.IndividualProtection2016,
          AmendProtectionRequestStatus.Dormant
        )(
          fakeRequest
        )

      status(result) shouldBe 400
    }
  }

  "Submitting Amend IndividualProtection2016LTA Pensions Taken Before data" when {

    "the data is invalid" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

      val result: Future[Result] =
        controller.submitAmendPensionsTakenBefore(
          AmendableProtectionType.IndividualProtection2016LTA,
          AmendProtectionRequestStatus.Dormant
        )(
          fakeRequest
        )

      status(result) shouldBe 400
    }
  }

  "the data is invalidated by additional validation" in {
    mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

    val result = FakeRequests.authorisedPost(
      controller.submitAmendPensionsTakenBefore(
        AmendableProtectionType.IndividualProtection2016,
        AmendProtectionRequestStatus.Dormant
      ),
      ("amendedPensionsTakenBefore", "1")
    )

    status(result) shouldBe 400
  }

  "the model can't be fetched from cache" in {
    mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
    mockFetchAmendProtectionModel(any(), any())(None)

    val result = FakeRequests.authorisedPost(
      controller.submitAmendPensionsTakenBefore(
        AmendableProtectionType.IndividualProtection2016,
        AmendProtectionRequestStatus.Dormant
      ),
      ("amendedPensionsTakenBefore", "no"),
      ("amendedPensionsTakenBeforeAmt", "0")
    )

    status(result) shouldBe 500
  }

  "the data is valid with a no for IndividualProtection2016" in {
    mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
    mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2016))
    mockSaveAmendProtectionModel()

    val result = FakeRequests.authorisedPost(
      controller.submitAmendPensionsTakenBefore(
        AmendableProtectionType.IndividualProtection2016,
        AmendProtectionRequestStatus.Dormant
      ),
      ("amendedPensionsTakenBefore", "no"),
      ("amendedPensionsTakenBeforeAmt", "0")
    )

    status(result) shouldBe 303
    redirectLocation(result) shouldBe Some(
      s"${routes.AmendsController.amendsSummary(AmendableProtectionType.IndividualProtection2016, AmendProtectionRequestStatus.Dormant)}"
    )
  }

  "the data is valid with a yes for IndividualProtection2016" in {
    mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
    mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2016))
    mockSaveAmendProtectionModel()

    val result = FakeRequests.authorisedPost(
      controller.submitAmendPensionsTakenBefore(
        AmendableProtectionType.IndividualProtection2016,
        AmendProtectionRequestStatus.Dormant
      ),
      ("amendedPensionsTakenBefore", "yes")
    )

    status(result) shouldBe 303
    redirectLocation(result) shouldBe Some(
      s"${routes.AmendsPensionWorthBeforeController.amendPensionsWorthBefore(AmendableProtectionType.IndividualProtection2016, AmendProtectionRequestStatus.Dormant)}"
    )
  }

  "the data is valid with a no for IndividualProtection2016LTA" in {
    mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
    mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2016LTA))
    mockSaveAmendProtectionModel()

    val result = FakeRequests.authorisedPost(
      controller.submitAmendPensionsTakenBefore(
        AmendableProtectionType.IndividualProtection2016LTA,
        AmendProtectionRequestStatus.Dormant
      ),
      ("amendedPensionsTakenBefore", "no"),
      ("amendedPensionsTakenBeforeAmt", "0")
    )

    status(result) shouldBe 303
    redirectLocation(result) shouldBe Some(
      s"${routes.AmendsController.amendsSummary(AmendableProtectionType.IndividualProtection2016LTA, AmendProtectionRequestStatus.Dormant)}"
    )
  }

  "the data is valid with a yes for IndividualProtection2016LTA" in {
    mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
    mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2016LTA))
    mockSaveAmendProtectionModel()

    val result = FakeRequests.authorisedPost(
      controller.submitAmendPensionsTakenBefore(
        AmendableProtectionType.IndividualProtection2016LTA,
        AmendProtectionRequestStatus.Dormant
      ),
      ("amendedPensionsTakenBefore", "yes")
    )

    status(result) shouldBe 303
    redirectLocation(result) shouldBe Some(
      s"${routes.AmendsPensionWorthBeforeController.amendPensionsWorthBefore(AmendableProtectionType.IndividualProtection2016LTA, AmendProtectionRequestStatus.Dormant)}"
    )
  }

  "the data is valid with a no for IndividualProtection2014" in {
    mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
    mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2014))
    mockSaveAmendProtectionModel()

    val result = FakeRequests.authorisedPost(
      controller.submitAmendPensionsTakenBefore(
        AmendableProtectionType.IndividualProtection2014,
        AmendProtectionRequestStatus.Dormant
      ),
      ("amendedPensionsTakenBefore", "no"),
      ("amendedPensionsTakenBeforeAmt", "0")
    )

    status(result) shouldBe 303
    redirectLocation(result) shouldBe Some(
      s"${routes.AmendsController.amendsSummary(AmendableProtectionType.IndividualProtection2014, AmendProtectionRequestStatus.Dormant)}"
    )
  }

  "the data is valid with a yes for IndividualProtection2014" in {
    mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
    mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2014))
    mockSaveAmendProtectionModel()

    val result = FakeRequests.authorisedPost(
      controller.submitAmendPensionsTakenBefore(
        AmendableProtectionType.IndividualProtection2014,
        AmendProtectionRequestStatus.Dormant
      ),
      ("amendedPensionsTakenBefore", "yes")
    )

    status(result) shouldBe 303
    redirectLocation(result) shouldBe Some(
      s"${routes.AmendsPensionWorthBeforeController.amendPensionsWorthBefore(AmendableProtectionType.IndividualProtection2014, AmendProtectionRequestStatus.Dormant)}"
    )
  }

  "the data is valid with a no for IndividualProtection2014LTA" in {
    mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
    mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2014LTA))
    mockSaveAmendProtectionModel()

    val result = FakeRequests.authorisedPost(
      controller.submitAmendPensionsTakenBefore(
        AmendableProtectionType.IndividualProtection2014LTA,
        AmendProtectionRequestStatus.Dormant
      ),
      ("amendedPensionsTakenBefore", "no"),
      ("amendedPensionsTakenBeforeAmt", "0")
    )

    status(result) shouldBe 303
    redirectLocation(result) shouldBe Some(
      s"${routes.AmendsController.amendsSummary(AmendableProtectionType.IndividualProtection2014LTA, AmendProtectionRequestStatus.Dormant)}"
    )
  }

  "the data is valid with a yes for IndividualProtection2014LTA" in {
    mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
    mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2014LTA))
    mockSaveAmendProtectionModel()

    val result = FakeRequests.authorisedPost(
      controller.submitAmendPensionsTakenBefore(
        AmendableProtectionType.IndividualProtection2014LTA,
        AmendProtectionRequestStatus.Dormant
      ),
      ("amendedPensionsTakenBefore", "yes")
    )

    status(result) shouldBe 303
    redirectLocation(result) shouldBe Some(
      s"${routes.AmendsPensionWorthBeforeController.amendPensionsWorthBefore(AmendableProtectionType.IndividualProtection2014LTA, AmendProtectionRequestStatus.Dormant)}"
    )
  }

}
