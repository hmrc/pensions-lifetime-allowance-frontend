/*
 * Copyright 2019 HM Revenue & Customs
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

import com.kenshoo.play.metrics.Metrics
import config.LocalTemplateRenderer
import config.wiring.PlaFormPartialRetriever
import connectors.{IdentityVerificationConnector, KeyStoreConnector}
import javax.inject.Inject
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.Mode.Mode
import play.api.http.Status
import play.api.{Configuration, Environment}
import play.api.libs.json.{Format, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.MetricsService
import testHelpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpGet, HttpResponse}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future
import scala.io.Source

class UnauthorisedControllerSpec  extends UnitSpec with MockitoSugar with WithFakeApplication {

  val env = mock[Environment]
  val fakeRequest = FakeRequest("GET", "/")
  val mockKeystoreConnector = mock[KeyStoreConnector]
  implicit val hc = mock[HeaderCarrier]
  implicit val templateRenderer: LocalTemplateRenderer = MockTemplateRenderer.renderer
  implicit val partialRetriever: PlaFormPartialRetriever = mock[PlaFormPartialRetriever]


  object MockIdentityVerificationHttp extends MockitoSugar {
    val mockHttp = mock[HttpGet]

    val possibleJournies = Map (
      "success-journey-id" -> "test/resources/identity-verification/success.json",
      "incomplete-journey-id" -> "test/resources/identity-verification/incomplete.json",
      "failed-matching-journey-id" -> "test/resources/identity-verification/failed-matching.json",
      "insufficient-evidence-journey-id" -> "test/resources/identity-verification/insufficient-evidence.json",
      "locked-out-journey-id" -> "test/resources/identity-verification/locked-out.json",
      "user-aborted-journey-id" -> "test/resources/identity-verification/user-aborted.json",
      "timeout-journey-id" -> "test/resources/identity-verification/timeout.json",
      "technical-issue-journey-id" -> "test/resources/identity-verification/technical-issue.json",
      "precondition-failed-journey-id" -> "test/resources/identity-verification/precondition-failed.json",
      "failed-iv-journey-id" -> "test/resources/identity-verification/failed-iv.json",
      "invalid-journey-id" -> "test/resources/identity-verification/invalid-result.json",
      "invalid-fields-journey-id" -> "test/resources/identity-verification/invalid-fields.json"
    )

    def mockJourneyId(journeyId: String): Unit = {
      val fileContents = Source.fromFile(possibleJournies(journeyId)).mkString
      when(mockHttp.GET[HttpResponse](ArgumentMatchers.contains(journeyId))(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).
        thenReturn(Future.successful(HttpResponse(Status.OK, responseJson = Some(Json.parse(fileContents)))))
    }

    possibleJournies.keys.foreach(mockJourneyId)
  }

  object MockIdentityVerificationConnector extends IdentityVerificationConnector {
    override val serviceUrl: String = ""
    override def http: HttpGet = MockIdentityVerificationHttp.mockHttp

    override protected def mode: Mode = mock[Mode]

    override protected def runModeConfiguration: Configuration = mock[Configuration]
  }

  val testUnauthorisedController = new UnauthorisedController(MockIdentityVerificationConnector,mockKeystoreConnector,partialRetriever,templateRenderer)

//  def testUnauthorisedController(identityVerificationEnabled: Boolean = true): UnauthorisedController = new UnauthorisedController {
//    override val identityVerificationConnector: IdentityVerificationConnector = MockIdentityVerificationConnector
//    override implicit val templateRenderer: TemplateRenderer = MockTemplateRenderer
//    override val keystoreConnector: KeyStoreConnector = mockKeystoreConnector
//  }

  def setupKeystoreMocks(data: Option[Boolean]): Unit = {
    when(mockKeystoreConnector
      .fetchAndGetFormData[Boolean](ArgumentMatchers.eq("previous-technical-issues"))(ArgumentMatchers.any[HeaderCarrier], ArgumentMatchers.any[Format[Boolean]]))
      .thenReturn(Future.successful(data))
    when(mockKeystoreConnector
      .saveFormData(ArgumentMatchers.eq("previous-technical-issues"), ArgumentMatchers.any[Boolean])(ArgumentMatchers.any[HeaderCarrier], ArgumentMatchers.any[Format[Boolean]]))
      .thenReturn(Future.successful(mock[CacheMap]))
  }

  "GET /not-authorised" should {
    "show not authorised page" in {
      val result = testUnauthorisedController.showNotAuthorised(None)(fakeRequest)
      contentAsString(result) should include("We cannot confirm your identity")
      status(result) shouldBe UNAUTHORIZED
    }

    "show generic not_authorised template for FailedMatching journey" in {
      val result = testUnauthorisedController.showNotAuthorised(Some("failed-matching-journey-id"))(fakeRequest)
      contentAsString(result) should include("We cannot confirm your identity")
      status(result) shouldBe UNAUTHORIZED
    }

    "show generic not_authorised template for InsufficientEvidence journey" in {
      val result = testUnauthorisedController.showNotAuthorised(Some("insufficient-evidence-journey-id"))(fakeRequest)
      contentAsString(result) should include("We cannot confirm your identity")
      status(result) shouldBe UNAUTHORIZED
    }

    "show generic not_authorised template for Incomplete journey" in {
      val result = testUnauthorisedController.showNotAuthorised(Some("incomplete-journey-id"))(fakeRequest)
      contentAsString(result) should include("We cannot confirm your identity")
      status(result) shouldBe UNAUTHORIZED
    }

    "show generic not_authorised template for PreconditionFailed journey" in {
      val result = testUnauthorisedController.showNotAuthorised(Some("precondition-failed-journey-id"))(fakeRequest)
      contentAsString(result) should include("We cannot confirm your identity")
      status(result) shouldBe UNAUTHORIZED
    }

    "show generic not_authorised template for UserAborted journey" in {
      val result = testUnauthorisedController.showNotAuthorised(Some("user-aborted-journey-id"))(fakeRequest)
      contentAsString(result) should include("We cannot confirm your identity")
      status(result) shouldBe UNAUTHORIZED
    }

    "show technical_issue template for TechnicalIssue journey" which {

      "returns an INTERNAL_SERVER_ERROR on the first attempt" in {
        setupKeystoreMocks(None)
        val result = testUnauthorisedController.showNotAuthorised(Some("technical-issue-journey-id"))(fakeRequest)
        contentAsString(result) should include("There is a technical problem")
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "returns an OK on any attempt after the first" in {
        setupKeystoreMocks(Some(true))
        val result = testUnauthorisedController.showNotAuthorised(Some("technical-issue-journey-id"))(fakeRequest)
        contentAsString(result) should include("There is a technical problem")
        status(result) shouldBe OK
      }

      "returns an INTERNAL_SERVER_ERROR if a false is returned" in {
        setupKeystoreMocks(Some(false))
        val result = testUnauthorisedController.showNotAuthorised(Some("technical-issue-journey-id"))(fakeRequest)
        contentAsString(result) should include("There is a technical problem")
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "show locked_out template for LockedOut journey" in {
      val result = testUnauthorisedController.showNotAuthorised(Some("locked-out-journey-id"))(fakeRequest)
      contentAsString(result) should include("You have tried to confirm your identity too many times")
      status(result) shouldBe UNAUTHORIZED
    }

    "show timeout template for Timeout journey" in {
      val result = testUnauthorisedController.showNotAuthorised(Some("timeout-journey-id"))(fakeRequest)
      contentAsString(result) should include("signed out due to inactivity")
      status(result) shouldBe UNAUTHORIZED
    }

    "show 2FA failure page when no journey ID specified" in {
      val result = testUnauthorisedController.showNotAuthorised(None)(fakeRequest)
      contentAsString(result) should include("We cannot confirm your identity")
      contentAsString(result) should not include "If you cannot confirm your identity and you have a query you can"
      status(result) shouldBe UNAUTHORIZED
    }
  }
}
