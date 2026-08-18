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
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.Environment
import play.api.i18n.{Lang, Messages}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.SessionCacheService
import testHelpers.*
import testdata.AmendProtectionModelTestData
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import views.html.pages.amends.*
import views.html.pages.fallback.technicalError

import scala.concurrent.ExecutionContext

class AmendsCurrentPensionControllerSpec
    extends FakeApplication
    with MockitoSugar
    with MockSessionCacheService
    with BeforeAndAfterEach
    with AuthMocks
    with AmendProtectionModelTestData {

  implicit val fakeRequest: FakeRequest[AnyContent] = FakeRequest()

  implicit val messages: Messages = mcc.messagesApi.preferred(fakeRequest)

  implicit val appConfig: AppConfig           = inject[AppConfig]
  implicit val system: ActorSystem            = ActorSystem()
  implicit val mockMaterializer: Materializer = mock[Materializer]
  implicit val mockLang: Lang                 = mock[Lang]
  implicit val formWithCSRF: FormWithCSRF     = inject[FormWithCSRF]
  implicit val ec: ExecutionContext           = inject[ExecutionContext]

  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
  val mockEnv: Environment                         = mock[Environment]

  val technicalErrorView: technicalError                     = inject[technicalError]
  val amendIP16CurrentPensionsView: amendIP16CurrentPensions = inject[amendIP16CurrentPensions]
  val amendIP14CurrentPensionsView: amendIP14CurrentPensions = inject[amendIP14CurrentPensions]

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(mockSessionCacheService)
    reset(mockEnv)
  }

  val controller = new AmendsCurrentPensionController(
    mockSessionCacheService,
    mcc,
    authActions,
    technicalErrorView,
    amendIP16CurrentPensionsView,
    amendIP14CurrentPensionsView
  )

  "Calling the .amendCurrentPensions action" when {

    "not supplied with a stored model" in {
      mockAuthSuccess("AB123456A")
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
      mockAuthSuccess("AB123456A")
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

      jsoupDoc.body.getElementsByTag("h1").text shouldEqual messages("pla.currentPensions.title")
    }

    "return some HTML that" should {

      "contain some text and use the character set utf-8" in {
        mockAuthSuccess("AB123456A")
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
        mockAuthSuccess("AB123456A")
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
    mockAuthSuccess("AB123456A")
    mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2014))

    val result =
      controller.amendCurrentPensions(
        AmendableProtectionType.IndividualProtection2014,
        AmendProtectionRequestStatus.Dormant
      )(fakeRequest)

    status(result) shouldBe 200
  }

  "supplied with a stored test model (£100000, IndividualProtection2014LTA, dormant)" in {
    mockAuthSuccess("AB123456A")
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
      mockAuthSuccess("AB123456A")
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
      mockAuthSuccess("AB123456A")

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
      mockAuthSuccess("AB123456A")
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
      mockAuthSuccess("AB123456A")
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
      mockAuthSuccess("AB123456A")

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
      mockAuthSuccess("AB123456A")
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
      mockAuthSuccess("AB123456A")
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
      mockAuthSuccess("AB123456A")

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
      mockAuthSuccess("AB123456A")
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
      mockAuthSuccess("AB123456A")
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
      mockAuthSuccess("AB123456A")

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
      mockAuthSuccess("AB123456A")
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
