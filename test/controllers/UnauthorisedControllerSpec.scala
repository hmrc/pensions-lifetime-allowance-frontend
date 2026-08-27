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

import connectors.IdentityVerificationConnector
import enums.IdentityVerificationResult
import models.cache.CacheMap
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.{MessagesControllerComponents, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.SessionCacheService
import testHelpers.*
import uk.gov.hmrc.http.HeaderCarrier
import views.html.pages.ivFailure.{lockedOut, technicalIssue, unauthorised}
import views.html.pages.timeout

import scala.concurrent.{ExecutionContext, Future}

class UnauthorisedControllerSpec
    extends FakeApplication
    with MockitoSugar
    with BeforeAndAfterEach
    with MockSessionCacheService {

  private val mcc: MessagesControllerComponents                                = inject[MessagesControllerComponents]
  private val fakeRequest                                                      = FakeRequest("GET", "/")
  private val mockIdentityVerificationConnector: IdentityVerificationConnector = mock[IdentityVerificationConnector]

  override val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]

  private val executionContext: ExecutionContext = ExecutionContext.global
  private val mockLockedOut: lockedOut           = inject[lockedOut]
  private val mockTechnicalIssue: technicalIssue = inject[technicalIssue]
  private val mockUnauthorised: unauthorised     = inject[unauthorised]
  private val mockTimeout: timeout               = inject[timeout]

  private val controller = new UnauthorisedController(
    mockIdentityVerificationConnector,
    mockSessionCacheService,
    mcc,
    mockLockedOut,
    mockTechnicalIssue,
    mockUnauthorised,
    mockTimeout
  )(using executionContext)

  override def beforeEach(): Unit = {
    reset(mockIdentityVerificationConnector)
    super.beforeEach()
  }

  private def setupCacheMocks(data: Option[Boolean]): Unit = {
    when(mockSessionCacheService.fetchPreviousTechnicalIssues(using any())).thenReturn(Future.successful(data))
    when(mockSessionCacheService.savePreviousTechnicalIssues(any())(using any()))
      .thenReturn(Future.successful(mock[CacheMap]))
  }

  "GET /not-authorised" should {
    "show not authorised page" in {
      val result: Future[Result] = controller.showNotAuthorised(None)(fakeRequest)
      contentAsString(result) should include("We cannot confirm your identity")
      status(result) shouldBe UNAUTHORIZED
    }

    "show generic not_authorised template for FailedMatching journey" in {
      when(mockIdentityVerificationConnector.identityVerificationResponse(any())(using any[HeaderCarrier]))
        .thenReturn(Future.successful(IdentityVerificationResult.FailedMatching))
      val result: Future[Result] = controller.showNotAuthorised(Some("failed-matching-journey-id"))(fakeRequest)
      contentAsString(result) should include("We cannot confirm your identity")
      status(result) shouldBe UNAUTHORIZED
    }

    "show generic not_authorised template for InsufficientEvidence journey" in {
      when(mockIdentityVerificationConnector.identityVerificationResponse(any())(using any[HeaderCarrier]))
        .thenReturn(Future.successful(IdentityVerificationResult.InsufficientEvidence))
      val result: Future[Result] =
        controller.showNotAuthorised(Some("insufficient-evidence-journey-id"))(fakeRequest)
      contentAsString(result) should include("We cannot confirm your identity")
      status(result) shouldBe UNAUTHORIZED
    }

    "show generic not_authorised template for Incomplete journey" in {
      when(mockIdentityVerificationConnector.identityVerificationResponse(any())(using any[HeaderCarrier]))
        .thenReturn(Future.successful(IdentityVerificationResult.Incomplete))
      val result: Future[Result] = controller.showNotAuthorised(Some("incomplete-journey-id"))(fakeRequest)
      contentAsString(result) should include("We cannot confirm your identity")
      status(result) shouldBe UNAUTHORIZED
    }

    "show generic not_authorised template for PreconditionFailed journey" in {
      when(mockIdentityVerificationConnector.identityVerificationResponse(any())(using any[HeaderCarrier]))
        .thenReturn(Future.successful(IdentityVerificationResult.PreconditionFailed))
      val result: Future[Result] =
        controller.showNotAuthorised(Some("precondition-failed-journey-id"))(fakeRequest)
      contentAsString(result) should include("We cannot confirm your identity")
      status(result) shouldBe UNAUTHORIZED
    }

    "show generic not_authorised template for UserAborted journey" in {
      when(mockIdentityVerificationConnector.identityVerificationResponse(any())(using any[HeaderCarrier]))
        .thenReturn(Future.successful(IdentityVerificationResult.UserAborted))
      val result: Future[Result] = controller.showNotAuthorised(Some("user-aborted-journey-id"))(fakeRequest)
      contentAsString(result) should include("We cannot confirm your identity")
      status(result) shouldBe UNAUTHORIZED
    }
  }

  "show technical_issue template for TechnicalIssue journey".which {

    "returns an INTERNAL_SERVER_ERROR on the first attempt" in {
      when(mockIdentityVerificationConnector.identityVerificationResponse(any())(using any()))
        .thenReturn(Future.successful(IdentityVerificationResult.TechnicalIssue))

      setupCacheMocks(None)

      val result: Future[Result] = controller.showNotAuthorised(Some("technical-issue-journey-id"))(fakeRequest)
      contentAsString(result) should include("There is a technical problem")
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "returns an OK on any attempt after the first" in {
      when(mockIdentityVerificationConnector.identityVerificationResponse(any())(using any()))
        .thenReturn(Future.successful(IdentityVerificationResult.TechnicalIssue))

      setupCacheMocks(Some(true))
      val result: Future[Result] = controller.showNotAuthorised(Some("technical-issue-journey-id"))(fakeRequest)

      contentAsString(result) should include("There is a technical problem")
      status(result) shouldBe OK
    }

    "returns an INTERNAL_SERVER_ERROR if a false is returned" in {
      when(mockIdentityVerificationConnector.identityVerificationResponse(any())(using any()))
        .thenReturn(Future.successful(IdentityVerificationResult.TechnicalIssue))
      setupCacheMocks(Some(false))
      val result: Future[Result] = controller.showNotAuthorised(Some("technical-issue-journey-id"))(fakeRequest)
      contentAsString(result) should include("There is a technical problem")
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }

  "show locked_out template for LockedOut journey" in {
    when(mockIdentityVerificationConnector.identityVerificationResponse(any())(using any()))
      .thenReturn(Future.successful(IdentityVerificationResult.LockedOut))

    val result: Future[Result] = controller.showNotAuthorised(Some("locked-out-journey-id"))(fakeRequest)
    contentAsString(result) should include("You have tried to confirm your identity too many times")
    status(result) shouldBe UNAUTHORIZED
  }

  "show timeout template for Timeout journey" in {
    when(mockIdentityVerificationConnector.identityVerificationResponse(any())(using any()))
      .thenReturn(Future.successful(IdentityVerificationResult.Timeout))

    val result: Future[Result] = controller.showNotAuthorised(Some("timeout-journey-id"))(fakeRequest)
    contentAsString(result) should include("signed out due to inactivity")
    status(result) shouldBe UNAUTHORIZED
  }

  "show 2FA failure page when no journey ID specified" in {
    val result: Future[Result] = controller.showNotAuthorised(None)(fakeRequest)
    contentAsString(result) should include("We cannot confirm your identity")
    (contentAsString(result) should not).include("If you cannot confirm your identity and you have a query you can")
    status(result) shouldBe UNAUTHORIZED
  }

}
