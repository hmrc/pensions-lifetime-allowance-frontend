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
import constructors.display.DisplayConstructors
import models.pla.AmendableProtectionType
import models.pla.request.AmendProtectionRequestStatus
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import testHelpers.*
import testdata.AmendProtectionModelTestData
import views.html.pages.amends.{amendIP14OverseasPensions, amendIP16OverseasPensions}
import views.html.pages.fallback.technicalError

import scala.concurrent.{ExecutionContext, Future}

class AmendsOverseasPensionControllerSpec
    extends FakeApplication
    with MockitoSugar
    with MockSessionCacheService
    with BeforeAndAfterEach
    with AuthMocks
    with AmendProtectionModelTestData {

  private val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  private val messages: Messages = mcc.messagesApi.preferred(fakeRequest)

  private val executionContext: ExecutionContext = ExecutionContext.global

  private val mockDisplayConstructors: DisplayConstructors = mock[DisplayConstructors]

  private val technicalErrorView: technicalError = inject[technicalError]
  private val amendIP16OverseasPensionsView      = inject[amendIP16OverseasPensions]
  private val amendIP14OverseasPensionsView      = inject[amendIP14OverseasPensions]

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(mockDisplayConstructors)
  }

  private val controller = new AmendsOverseasPensionController(
    mockSessionCacheService,
    mcc,
    authActions,
    technicalErrorView,
    amendIP16OverseasPensionsView,
    amendIP14OverseasPensionsView
  )(using executionContext)

  "In AmendsOverseasPensionController calling the .amendOverseasPensions action" when {

    "not supplied with a stored model" in {
      mockAuthSuccess("AB123456A")
      mockFetchAmendProtectionModel(any(), any())(None)

      val result: Future[Result] =
        controller.amendOverseasPensions(
          AmendableProtectionType.IndividualProtection2016,
          AmendProtectionRequestStatus.Open
        )(fakeRequest)

      status(result) shouldBe 500
    }

    "supplied with the stored test model for (dormant, IndividualProtection2016, nonUKRights = £0.0)" in {
      mockAuthSuccess("AB123456A")
      mockFetchAmendProtectionModel(any(), any())(
        Some(amendDormantIndividualProtection2016.withNonUKRightsAmount(None))
      )

      val result: Future[Result] =
        controller.amendOverseasPensions(
          AmendableProtectionType.IndividualProtection2016,
          AmendProtectionRequestStatus.Dormant
        )(fakeRequest)
      val jsoupDoc: Document = Jsoup.parse(contentAsString(result))

      jsoupDoc.body
        .getElementById("conditional-amendedOverseasPensions")
        .attr("class") shouldBe "govuk-radios__conditional govuk-radios__conditional--hidden"
    }

    "supplied with the stored test model for (dormant, IndividualProtection2016, nonUKRights = £2000)" in {
      mockAuthSuccess("AB123456A")
      mockFetchAmendProtectionModel(any(), any())(
        Some(amendDormantIndividualProtection2016.withNonUKRightsAmount(Some(2_000)))
      )

      val result: Future[Result] =
        controller.amendOverseasPensions(
          AmendableProtectionType.IndividualProtection2016,
          AmendProtectionRequestStatus.Dormant
        )(fakeRequest)

      status(result) shouldBe 200
    }

    "should take the user to the overseas pensions page" in {
      mockAuthSuccess("AB123456A")
      mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2016))

      val result: Future[Result] =
        controller.amendOverseasPensions(
          AmendableProtectionType.IndividualProtection2016,
          AmendProtectionRequestStatus.Dormant
        )(fakeRequest)
      val jsoupDoc: Document = Jsoup.parse(contentAsString(result))

      jsoupDoc.body.getElementsByTag("h1").text shouldEqual messages("pla.overseasPensions.title")
    }

    "return some HTML that" should {

      "contain some text and use the character set utf-8" in {
        mockAuthSuccess("AB123456A")
        mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2016))

        val result: Future[Result] =
          controller.amendOverseasPensions(
            AmendableProtectionType.IndividualProtection2016,
            AmendProtectionRequestStatus.Dormant
          )(fakeRequest)

        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }

      "have the value of the check box set as 'Yes' by default" in {
        mockAuthSuccess("AB123456A")
        mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2016))

        val result: Future[Result] =
          controller.amendOverseasPensions(
            AmendableProtectionType.IndividualProtection2016,
            AmendProtectionRequestStatus.Dormant
          )(fakeRequest)
        val jsoupDoc: Document = Jsoup.parse(contentAsString(result))

        jsoupDoc.body
          .getElementById("conditional-amendedOverseasPensions")
          .attr("class") shouldBe "govuk-radios__conditional"
      }

      "have the value of the input field set to 2000 by default" in {
        mockAuthSuccess("AB123456A")
        mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2016))

        val result: Future[Result] =
          controller.amendOverseasPensions(
            AmendableProtectionType.IndividualProtection2016,
            AmendProtectionRequestStatus.Dormant
          )(fakeRequest)
        val jsoupDoc: Document = Jsoup.parse(contentAsString(result))

        jsoupDoc.body.getElementById("amendedOverseasPensionsAmt").attr("value") shouldBe "2000"
      }
    }

    "supplied with the stored test model for (dormant, IndividualProtection2014, nonUKRights = £2000)" in {
      mockAuthSuccess("AB123456A")
      mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2014))

      val result: Future[Result] =
        controller.amendOverseasPensions(
          AmendableProtectionType.IndividualProtection2014,
          AmendProtectionRequestStatus.Dormant
        )(fakeRequest)

      status(result) shouldBe 200
    }
  }

  "Submitting Amend IndividualProtection2016 Overseas Pensions data" when {

    "there is an error reading the form" in {
      mockAuthSuccess("AB123456A")

      val result: Future[Result] =
        controller.submitAmendOverseasPensions(
          AmendableProtectionType.IndividualProtection2016,
          AmendProtectionRequestStatus.Dormant
        )(
          fakeRequest
        )

      status(result) shouldBe 400
    }

    "the model can't be fetched from cache" in {
      mockAuthSuccess("AB123456A")
      mockFetchAmendProtectionModel(any(), any())(None)

      val result = FakeRequests.authorisedPost(
        controller.submitAmendOverseasPensions(
          AmendableProtectionType.IndividualProtection2016,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedOverseasPensions", "no"),
        ("amendedOverseasPensionsAmt", "0")
      )

      status(result) shouldBe 500
    }

    "the data is valid with a no response" in {
      mockAuthSuccess("AB123456A")
      mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2016))
      mockSaveAmendProtectionModel()

      val result = FakeRequests.authorisedPost(
        controller.submitAmendOverseasPensions(
          AmendableProtectionType.IndividualProtection2016,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedOverseasPensions", "no"),
        ("amendedOverseasPensionsAmt", "0")
      )

      status(result) shouldBe 303
      redirectLocation(result) shouldBe Some(s"${routes.AmendsController.amendsSummary(
          AmendableProtectionType.IndividualProtection2016,
          AmendProtectionRequestStatus.Dormant
        )}")
    }

    "the data is valid with a yes response" in {
      mockAuthSuccess("AB123456A")
      mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2016))
      mockSaveAmendProtectionModel()

      val result = FakeRequests.authorisedPost(
        controller.submitAmendOverseasPensions(
          AmendableProtectionType.IndividualProtection2016,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedOverseasPensions", "yes"),
        ("amendedOverseasPensionsAmt", "10")
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
        controller.submitAmendOverseasPensions(
          AmendableProtectionType.IndividualProtection2016,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedOverseasPensions", "yes"),
        ("amendedOverseasPensionsAmt", "")
      )

      status(result) shouldBe 400

      val jsoupDoc = Jsoup.parse(contentAsString(result))

      jsoupDoc.getElementsByClass("govuk-error-message").text should include(
        messages("pla.overseasPensions.amount.errors.mandatoryError.IndividualProtection2016")
      )
    }
  }

  "Submitting Amend IndividualProtection2014 Overseas Pensions data" when {

    "there is an error reading the form" in {
      mockAuthSuccess("AB123456A")

      val result: Future[Result] =
        controller.submitAmendOverseasPensions(
          AmendableProtectionType.IndividualProtection2014,
          AmendProtectionRequestStatus.Dormant
        )(
          fakeRequest
        )

      status(result) shouldBe 400
    }

    "the model can't be fetched from cache" in {
      mockAuthSuccess("AB123456A")
      mockFetchAmendProtectionModel(any(), any())(None)

      val result = FakeRequests.authorisedPost(
        controller.submitAmendOverseasPensions(
          AmendableProtectionType.IndividualProtection2014,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedOverseasPensions", "no"),
        ("amendedOverseasPensionsAmt", "0")
      )

      status(result) shouldBe 500
    }

    "the data is valid with a no response" in {
      mockAuthSuccess("AB123456A")
      mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2014))
      mockSaveAmendProtectionModel()

      val result = FakeRequests.authorisedPost(
        controller.submitAmendOverseasPensions(
          AmendableProtectionType.IndividualProtection2014,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedOverseasPensions", "no"),
        ("amendedOverseasPensionsAmt", "0")
      )

      status(result) shouldBe 303
      redirectLocation(result) shouldBe Some(s"${routes.AmendsController.amendsSummary(
          AmendableProtectionType.IndividualProtection2014,
          AmendProtectionRequestStatus.Dormant
        )}")
    }

    "the data is valid with a yes response" in {
      mockAuthSuccess("AB123456A")
      mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2014))
      mockSaveAmendProtectionModel()

      val result = FakeRequests.authorisedPost(
        controller.submitAmendOverseasPensions(
          AmendableProtectionType.IndividualProtection2014,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedOverseasPensions", "yes"),
        ("amendedOverseasPensionsAmt", "10")
      )

      status(result) shouldBe 303
      redirectLocation(result) shouldBe Some(s"${routes.AmendsController.amendsSummary(
          AmendableProtectionType.IndividualProtection2014,
          AmendProtectionRequestStatus.Dormant
        )}")
    }

    "the data is invalid" in {
      mockAuthSuccess("AB123456A")

      val result = FakeRequests.authorisedPost(
        controller.submitAmendOverseasPensions(
          AmendableProtectionType.IndividualProtection2014,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedOverseasPensions", "yes"),
        ("amendedOverseasPensionsAmt", "")
      )

      status(result) shouldBe 400

      val jsoupDoc = Jsoup.parse(contentAsString(result))

      jsoupDoc.getElementsByClass("govuk-error-message").text should include(
        messages("pla.overseasPensions.amount.errors.mandatoryError.IndividualProtection2014")
      )
    }
  }

  "Submitting Amend IndividualProtection2016LTA Overseas Pensions data" when {

    "there is an error reading the form" in {
      mockAuthSuccess("AB123456A")

      val result: Future[Result] =
        controller.submitAmendOverseasPensions(
          AmendableProtectionType.IndividualProtection2016LTA,
          AmendProtectionRequestStatus.Dormant
        )(
          fakeRequest
        )

      status(result) shouldBe 400
    }

    "the model can't be fetched from cache" in {
      mockAuthSuccess("AB123456A")
      mockFetchAmendProtectionModel(any(), any())(None)

      val result = FakeRequests.authorisedPost(
        controller.submitAmendOverseasPensions(
          AmendableProtectionType.IndividualProtection2016LTA,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedOverseasPensions", "no"),
        ("amendedOverseasPensionsAmt", "0")
      )

      status(result) shouldBe 500
    }

    "the data is valid with a no response" in {
      mockAuthSuccess("AB123456A")
      mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2016LTA))
      mockSaveAmendProtectionModel()

      val result = FakeRequests.authorisedPost(
        controller.submitAmendOverseasPensions(
          AmendableProtectionType.IndividualProtection2016LTA,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedOverseasPensions", "no"),
        ("amendedOverseasPensionsAmt", "0")
      )

      status(result) shouldBe 303
      redirectLocation(result) shouldBe Some(s"${routes.AmendsController.amendsSummary(
          AmendableProtectionType.IndividualProtection2016LTA,
          AmendProtectionRequestStatus.Dormant
        )}")
    }

    "the data is valid with a yes response" in {
      mockAuthSuccess("AB123456A")
      mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2016LTA))
      mockSaveAmendProtectionModel()

      val result = FakeRequests.authorisedPost(
        controller.submitAmendOverseasPensions(
          AmendableProtectionType.IndividualProtection2016LTA,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedOverseasPensions", "yes"),
        ("amendedOverseasPensionsAmt", "10")
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
        controller.submitAmendOverseasPensions(
          AmendableProtectionType.IndividualProtection2016LTA,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedOverseasPensions", "yes"),
        ("amendedOverseasPensionsAmt", "")
      )

      status(result) shouldBe 400

      val jsoupDoc = Jsoup.parse(contentAsString(result))

      jsoupDoc.getElementsByClass("govuk-error-message").text should include(
        messages("pla.overseasPensions.amount.errors.mandatoryError.IndividualProtection2016LTA")
      )
    }
  }

  "Submitting Amend IndividualProtection2014LTA Overseas Pensions data" when {

    "there is an error reading the form" in {
      mockAuthSuccess("AB123456A")

      val result: Future[Result] =
        controller.submitAmendOverseasPensions(
          AmendableProtectionType.IndividualProtection2014LTA,
          AmendProtectionRequestStatus.Dormant
        )(
          fakeRequest
        )

      status(result) shouldBe 400
    }

    "the model can't be fetched from cache" in {
      mockAuthSuccess("AB123456A")
      mockFetchAmendProtectionModel(any(), any())(None)

      val result = FakeRequests.authorisedPost(
        controller.submitAmendOverseasPensions(
          AmendableProtectionType.IndividualProtection2014LTA,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedOverseasPensions", "no"),
        ("amendedOverseasPensionsAmt", "0")
      )

      status(result) shouldBe 500
    }

    "the data is valid with a no response" in {
      mockAuthSuccess("AB123456A")
      mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2014LTA))
      mockSaveAmendProtectionModel()

      val result = FakeRequests.authorisedPost(
        controller.submitAmendOverseasPensions(
          AmendableProtectionType.IndividualProtection2014LTA,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedOverseasPensions", "no"),
        ("amendedOverseasPensionsAmt", "0")
      )

      status(result) shouldBe 303
      redirectLocation(result) shouldBe Some(s"${routes.AmendsController.amendsSummary(
          AmendableProtectionType.IndividualProtection2014LTA,
          AmendProtectionRequestStatus.Dormant
        )}")
    }

    "the data is valid with a yes response" in {
      mockAuthSuccess("AB123456A")
      mockFetchAmendProtectionModel(any(), any())(Some(amendDormantIndividualProtection2014LTA))
      mockSaveAmendProtectionModel()

      val result = FakeRequests.authorisedPost(
        controller.submitAmendOverseasPensions(
          AmendableProtectionType.IndividualProtection2014LTA,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedOverseasPensions", "yes"),
        ("amendedOverseasPensionsAmt", "10")
      )

      status(result) shouldBe 303
      redirectLocation(result) shouldBe Some(s"${routes.AmendsController.amendsSummary(
          AmendableProtectionType.IndividualProtection2014LTA,
          AmendProtectionRequestStatus.Dormant
        )}")
    }

    "the data is invalid" in {
      mockAuthSuccess("AB123456A")

      val result = FakeRequests.authorisedPost(
        controller.submitAmendOverseasPensions(
          AmendableProtectionType.IndividualProtection2014LTA,
          AmendProtectionRequestStatus.Dormant
        ),
        ("amendedOverseasPensions", "yes"),
        ("amendedOverseasPensionsAmt", "")
      )

      status(result) shouldBe 400

      val jsoupDoc = Jsoup.parse(contentAsString(result))

      jsoupDoc.getElementsByClass("govuk-error-message").text should include(
        messages("pla.overseasPensions.amount.errors.mandatoryError.IndividualProtection2014LTA")
      )
    }
  }

}
