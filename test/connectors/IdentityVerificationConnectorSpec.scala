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

package connectors

import config.FrontendAppConfig
import enums.IdentityVerificationResult
import org.mockito.ArgumentMatchers.{any, eq => eqs}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status
import play.api.libs.json.Json
import services.SessionCacheService
import testHelpers.FakeApplication
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source

class IdentityVerificationConnectorSpec extends FakeApplication with ScalaFutures with MockitoSugar {
  implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  implicit val executionContext: ExecutionContext = fakeApplication().injector.instanceOf[ExecutionContext]
  val mockAppConfig                               = fakeApplication().injector.instanceOf[FrontendAppConfig]
  val mockHttp                                    = mock[HttpClientV2]
  val mockSessionCacheService                     = mock[SessionCacheService]

  val identityVerficationConstructor = new IdentityVerificationConnector(mockAppConfig, mockHttp)

  val possibleJournies = Map(
    "success-journey-id"               -> "test/resources/identity-verification/success.json",
    "incomplete-journey-id"            -> "test/resources/identity-verification/incomplete.json",
    "failed-matching-journey-id"       -> "test/resources/identity-verification/failed-matching.json",
    "insufficient-evidence-journey-id" -> "test/resources/identity-verification/insufficient-evidence.json",
    "locked-out-journey-id"            -> "test/resources/identity-verification/locked-out.json",
    "user-aborted-journey-id"          -> "test/resources/identity-verification/user-aborted.json",
    "timeout-journey-id"               -> "test/resources/identity-verification/timeout.json",
    "technical-issue-journey-id"       -> "test/resources/identity-verification/technical-issue.json",
    "precondition-failed-journey-id"   -> "test/resources/identity-verification/precondition-failed.json",
    "failed-iv-journey-id"             -> "test/resources/identity-verification/failed-iv.json",
    "invalid-journey-id"               -> "test/resources/identity-verification/invalid-result.json",
    "invalid-fields-journey-id"        -> "test/resources/identity-verification/invalid-fields.json"
  )

  def mockJourneyId(journeyId: String): Unit = {
    val fileContents                   = Source.fromFile(possibleJournies(journeyId)).mkString
    val requestBuilder: RequestBuilder = mock[RequestBuilder]
    val serviceUrl                     = identityVerficationConstructor.serviceUrl
    val journeyIdUrl                   = s"$serviceUrl/mdtp/journey/journeyId/$journeyId"
    when(mockHttp.get(eqs(new URL(journeyIdUrl)))(any)).thenReturn(requestBuilder)
    when(requestBuilder.execute[HttpResponse](any, any))
      .thenReturn(
        Future.successful(HttpResponse(status = Status.OK, json = Json.parse(fileContents), headers = Map.empty))
      )
  }

  possibleJournies.keys.foreach(mockJourneyId)

  "return success when identityVerification returns success" in {
    identityVerficationConstructor
      .identityVerificationResponse("success-journey-id")
      .futureValue shouldBe IdentityVerificationResult.Success
  }

  "return incomplete when identityVerification returns incomplete" in {
    identityVerficationConstructor
      .identityVerificationResponse("incomplete-journey-id")
      .futureValue shouldBe IdentityVerificationResult.Incomplete
  }

  "return failed matching when identityVerification returns failed matching" in {
    identityVerficationConstructor
      .identityVerificationResponse("failed-matching-journey-id")
      .futureValue shouldBe IdentityVerificationResult.FailedMatching
  }

  "return insufficient evidence when identityVerification returns insufficient evidence" in {
    identityVerficationConstructor
      .identityVerificationResponse("insufficient-evidence-journey-id")
      .futureValue shouldBe IdentityVerificationResult.InsufficientEvidence
  }

  "return locked out when identityVerification returns locked out" in {
    identityVerficationConstructor
      .identityVerificationResponse("locked-out-journey-id")
      .futureValue shouldBe IdentityVerificationResult.LockedOut
  }

  "return user aborted when identityVerification returns user aborted" in {
    identityVerficationConstructor
      .identityVerificationResponse("user-aborted-journey-id")
      .futureValue shouldBe IdentityVerificationResult.UserAborted
  }

  "return timeout when identityVerification returns timeout" in {
    identityVerficationConstructor
      .identityVerificationResponse("timeout-journey-id")
      .futureValue shouldBe IdentityVerificationResult.Timeout
  }

  "return technical issue when identityVerification returns technical issue" in {
    identityVerficationConstructor
      .identityVerificationResponse("technical-issue-journey-id")
      .futureValue shouldBe IdentityVerificationResult.TechnicalIssue
  }

  "return precondition failed when identityVerification returns precondition failed" in {
    identityVerficationConstructor
      .identityVerificationResponse("precondition-failed-journey-id")
      .futureValue shouldBe IdentityVerificationResult.PreconditionFailed
  }

  "return failed IV when identityVerification returns failed IV result type" in {
    identityVerficationConstructor
      .identityVerificationResponse("failed-iv-journey-id")
      .futureValue shouldBe IdentityVerificationResult.FailedIV
  }

  "return unknown outcome when identityVerification returns non-existent result type" in {
    identityVerficationConstructor
      .identityVerificationResponse("invalid-journey-id")
      .futureValue shouldBe IdentityVerificationResult.UnknownOutcome
  }

  "return failed future for invalid json fields" in {
    val result = identityVerficationConstructor.identityVerificationResponse("invalid-fields-journey-id")
    ScalaFutures.whenReady(result.failed)(e => e shouldBe a[identityVerficationConstructor.JsonValidationException])
  }

}
