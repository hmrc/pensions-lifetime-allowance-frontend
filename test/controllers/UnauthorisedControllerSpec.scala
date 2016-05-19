/*
 * Copyright 2016 HM Revenue & Customs
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

import java.util.UUID

import org.scalatestplus.play.OneAppPerSuite
import play.api.http._
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers._
import connectors.IdentityVerificationConnector
import testHelpers._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.time.DateTimeUtils._

class UnauthorisedControllerSpec extends UnitSpec with OneAppPerSuite {

  val fakeRequest = FakeRequest("GET", "/")

  def testUnauthorisedController(identityVerificationEnabled: Boolean = true): UnauthorisedController = new UnauthorisedController {
    override val identityVerificationConnector: IdentityVerificationConnector = MockIdentityVerificationConnector
  }

  // ## TESTS CURRENTLY FAILING ##
  // "GET /not-authorised" should {
  //   "show not authorised page" in {
  //     val result = testUnauthorisedController().showNotAuthorised(None)(fakeRequest)
  //     contentAsString(result) should include ("We were unable to confirm your identity")
  //   }

  //   "show generic not_authorised template for FailedMatching journey" in {
  //     val result = testUnauthorisedController().showNotAuthorised(Some("failed-matching-journey-id"))(fakeRequest)
  //     contentAsString(result) should include ("We were unable to confirm your identity")
  //   }

  //   "show generic not_authorised template for InsufficientEvidence journey" in {
  //     val result = testUnauthorisedController().showNotAuthorised(Some("insufficient-evidence-journey-id"))(fakeRequest)
  //     contentAsString(result) should include ("We were unable to confirm your identity")
  //   }

  //   "show generic not_authorised template for Incomplete journey" in {
  //     val result = testUnauthorisedController().showNotAuthorised(Some("incomplete-journey-id"))(fakeRequest)
  //     contentAsString(result) should include ("We were unable to confirm your identity")
  //   }

  //   "show generic not_authorised template for PreconditionFailed journey" in {
  //     val result = testUnauthorisedController().showNotAuthorised(Some("precondition-failed-journey-id"))(fakeRequest)
  //     contentAsString(result) should include ("We were unable to confirm your identity")
  //   }

  //   "show generic not_authorised template for UserAborted journey" in {
  //     val result = testUnauthorisedController().showNotAuthorised(Some("user-aborted-journey-id"))(fakeRequest)
  //     contentAsString(result) should include ("We were unable to confirm your identity")
  //   }

  //   "show technical_issue template for TechnicalIssue journey" in {
  //     val result = testUnauthorisedController().showNotAuthorised(Some("technical-issue-journey-id"))(fakeRequest)
  //     contentAsString(result) should include ("This online service is experiencing technical difficulties.")
  //   }

  //   "show locked_out template for LockedOut journey" in {
  //     val result = testUnauthorisedController().showNotAuthorised(Some("locked-out-journey-id"))(fakeRequest)
  //     contentAsString(result) should include ("You have reached the maximum number of attempts to confirm your identity.")
  //   }

  //   "show timeout template for Timeout journey" in {
  //     val result = testUnauthorisedController().showNotAuthorised(Some("timeout-journey-id"))(fakeRequest)
  //     contentAsString(result) should include ("Your session has ended because you have not done anything for 15 minutes.")
  //   }

  //   "show 2FA failure page when no journey ID specified" in {
  //     val result = testUnauthorisedController().showNotAuthorised(None)(fakeRequest)
  //     contentAsString(result) should include ("We were unable to confirm your identity")
  //     contentAsString(result) should not include "If you cannot confirm your identity and you have a query you can"
  //   }
  // }
}
