/*
 * Copyright 2017 HM Revenue & Customs
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

import java.time.{LocalDate, LocalDateTime}
import java.util.UUID

import org.scalatestplus.play.OneAppPerSuite
import play.api.http._
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers._
import connectors.{IdentityVerificationConnector, KeyStoreConnector}
import org.mockito.Matchers
import org.scalatest.mockito.MockitoSugar
import testHelpers._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.time.DateTimeUtils._
import uk.gov.hmrc.renderer.TemplateRenderer
import org.mockito.Mockito._
import play.api.libs.json.Format
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class UnauthorisedControllerSpec extends UnitSpec with OneAppPerSuite with MockitoSugar {

  val fakeRequest = FakeRequest("GET", "/")
  val mockKeystoreConnector = mock[KeyStoreConnector]
  implicit val hc = mock[HeaderCarrier]

  def testUnauthorisedController(identityVerificationEnabled: Boolean = true): UnauthorisedController = new UnauthorisedController {
    override val identityVerificationConnector: IdentityVerificationConnector = MockIdentityVerificationConnector
    override implicit val templateRenderer: TemplateRenderer = MockTemplateRenderer
    override val keystoreConnector: KeyStoreConnector = mockKeystoreConnector
  }

  def setupKeystoreMocks(data: Option[LocalDateTime]): Unit = {
    when(mockKeystoreConnector
      .fetchAndGetFormData[LocalDateTime](Matchers.eq("technical-issues-timestamp"))(Matchers.any[HeaderCarrier], Matchers.any[Format[LocalDateTime]]))
      .thenReturn(Future.successful(data))
    when(mockKeystoreConnector
      .saveFormData(Matchers.eq("technical-issues-timestamp"), Matchers.any[LocalDateTime])(Matchers.any[HeaderCarrier], Matchers.any[Format[LocalDateTime]]))
      .thenReturn(Future.successful(mock[CacheMap]))
  }

  "GET /not-authorised" should {
    "show not authorised page" in {
      val result = testUnauthorisedController().showNotAuthorised(None)(fakeRequest)
      contentAsString(result) should include ("We were unable to confirm your identity")
      status(result) shouldBe UNAUTHORIZED
    }

    "show generic not_authorised template for FailedMatching journey" in {
      val result = testUnauthorisedController().showNotAuthorised(Some("failed-matching-journey-id"))(fakeRequest)
      contentAsString(result) should include ("We were unable to confirm your identity")
      status(result) shouldBe UNAUTHORIZED
    }

    "show generic not_authorised template for InsufficientEvidence journey" in {
      val result = testUnauthorisedController().showNotAuthorised(Some("insufficient-evidence-journey-id"))(fakeRequest)
      contentAsString(result) should include ("We were unable to confirm your identity")
      status(result) shouldBe UNAUTHORIZED
    }

    "show generic not_authorised template for Incomplete journey" in {
      val result = testUnauthorisedController().showNotAuthorised(Some("incomplete-journey-id"))(fakeRequest)
      contentAsString(result) should include ("We were unable to confirm your identity")
      status(result) shouldBe UNAUTHORIZED
    }

    "show generic not_authorised template for PreconditionFailed journey" in {
      val result = testUnauthorisedController().showNotAuthorised(Some("precondition-failed-journey-id"))(fakeRequest)
      contentAsString(result) should include ("We were unable to confirm your identity")
      status(result) shouldBe UNAUTHORIZED
    }

    "show generic not_authorised template for UserAborted journey" in {
      val result = testUnauthorisedController().showNotAuthorised(Some("user-aborted-journey-id"))(fakeRequest)
      contentAsString(result) should include ("We were unable to confirm your identity")
      status(result) shouldBe UNAUTHORIZED
    }

    "show technical_issue template for TechnicalIssue journey" which {

      "returns an INTERNAL_SERVER_ERROR on the first attempt" in {
        setupKeystoreMocks(None)
        val result = testUnauthorisedController().showNotAuthorised(Some("technical-issue-journey-id"))(fakeRequest)
        contentAsString(result) should include("There is a technical problem")
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "returns an OK on a second attempt within an hour" in {
        setupKeystoreMocks(Some(LocalDateTime.now().minusMinutes(59)))
        val result = testUnauthorisedController().showNotAuthorised(Some("technical-issue-journey-id"))(fakeRequest)
        contentAsString(result) should include("There is a technical problem")
        status(result) shouldBe OK
      }

      "returns an INTERNAL_SERVER_ERROR after an hour" in {
        setupKeystoreMocks(Some(LocalDateTime.now().minusMinutes(60)))
        val result = testUnauthorisedController().showNotAuthorised(Some("technical-issue-journey-id"))(fakeRequest)
        contentAsString(result) should include("There is a technical problem")
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "show locked_out template for LockedOut journey" in {
      val result = testUnauthorisedController().showNotAuthorised(Some("locked-out-journey-id"))(fakeRequest)
      contentAsString(result) should include ("You have tried to confirm your identity too many times")
      status(result) shouldBe UNAUTHORIZED
    }

    "show timeout template for Timeout journey" in {
      val result = testUnauthorisedController().showNotAuthorised(Some("timeout-journey-id"))(fakeRequest)
      contentAsString(result) should include ("signed out due to inactivity")
      status(result) shouldBe UNAUTHORIZED
    }

    "show 2FA failure page when no journey ID specified" in {
      val result = testUnauthorisedController().showNotAuthorised(None)(fakeRequest)
      contentAsString(result) should include ("We were unable to confirm your identity")
      contentAsString(result) should not include "If you cannot confirm your identity and you have a query you can"
      status(result) shouldBe UNAUTHORIZED
    }
  }
}
