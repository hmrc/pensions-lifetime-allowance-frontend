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

package auth

import config.AppConfig
import auth.helpers.AuthMocks
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Results.{InternalServerError, Redirect}
import play.api.mvc.MessagesRequest
import play.api.test.FakeRequest
import play.api.test.Helpers.GET
import play.twirl.api.Html
import testHelpers.FakeApplication
import uk.gov.hmrc.auth.core.{
  AuthConnector,
  BearerTokenExpired,
  ConfidenceLevel,
  Enrolment,
  IncorrectCredentialStrength,
  InsufficientConfidenceLevel,
  InsufficientEnrolments
}
import views.html.pages.fallback.technicalError

import scala.concurrent.{ExecutionContext, Future}

class AuthenticateWithNinoSpec
    extends FakeApplication
    with MockitoSugar
    with BeforeAndAfterEach
    with AuthMocks
    with ScalaFutures {

  private val appConfig: AppConfig           = mock[AppConfig]
  private val authConnector: AuthConnector   = mock[AuthConnector]
  private val technicalError: technicalError = mock[technicalError]

  private val requestUrl        = "https://www.pla-frontend.gov.uk/ip16-start-page"
  private val requestUrlEncoded = "https%3A%2F%2Fwww.pla-frontend.gov.uk%2Fip16-start-page"
  private val fakeRequest       = new MessagesRequest(FakeRequest(GET, requestUrl), mcc.messagesApi)

  private val executionContext: ExecutionContext = ExecutionContext.global

  override def beforeEach(): Unit =
    reset(authConnector)

  private val authenticateWithNino = new AuthenticateWithNino(
    authConnector = authConnector,
    appConfig = appConfig,
    technicalError = technicalError
  )(using executionContext)

  "AuthenticateWithNino" should {

    "call AuthConnector" in {
      when(authConnector.authorise[Option[String]](any(), any())(any(), any()))
        .thenReturn(Future.successful(Some("nino")))

      authenticateWithNino.refine(fakeRequest).futureValue

      val predicate = Enrolment("HMRC-NI").and(ConfidenceLevel.L200)

      verify(authConnector).authorise(eqTo(predicate), any())(any(), any())

    }

    "return Right containing authenticated request" when {

      "auth is successful" in {
        when(authConnector.authorise[Option[String]](any(), any())(any(), any()))
          .thenReturn(Future.successful(Some("nino")))

        authenticateWithNino.refine(fakeRequest).futureValue shouldBe Right(AuthenticatedRequest("nino", fakeRequest))
      }

    }

    "return Left".that {
      "redirects to sign in" when {
        "the user has no active session" in {
          when(authConnector.authorise[Option[String]](any(), any())(any(), any()))
            .thenReturn(Future.failed(BearerTokenExpired()))
          when(appConfig.ggSignInUrl).thenReturn("https://gg-sign-in-url")
          when(appConfig.sessionMissingUpliftUrlPrefix).thenReturn("session-missing-uplift-url-prefix_")
          when(appConfig.appName).thenReturn("appName")

          authenticateWithNino.refine(fakeRequest).futureValue shouldBe Left(
            Redirect(
              s"https://gg-sign-in-url?continue=session-missing-uplift-url-prefix_$requestUrlEncoded&origin=appName"
            )
          )
        }
      }

      "redirects to identity verification uplift" when {
        "the user has insufficient enrolments" in {
          when(authConnector.authorise[Option[String]](any(), any())(any(), any()))
            .thenReturn(Future.failed(InsufficientEnrolments()))
          when(appConfig.ivUpliftUrl).thenReturn("https://iv-uplift-url")
          when(appConfig.appName).thenReturn("appName")
          when(appConfig.notAuthorisedRedirectUrl).thenReturn("not-authorised-redirect-url")

          authenticateWithNino.refine(fakeRequest).futureValue shouldBe Left(
            Redirect(
              s"https://iv-uplift-url?origin=appName&confidenceLevel=200&completionURL=$requestUrlEncoded&failureURL=not-authorised-redirect-url"
            )
          )
        }

        "the user has insufficient confidence level" in {
          when(authConnector.authorise[Option[String]](any(), any())(any(), any()))
            .thenReturn(Future.failed(InsufficientConfidenceLevel()))
          when(appConfig.ivUpliftUrl).thenReturn("https://iv-uplift-url")
          when(appConfig.appName).thenReturn("appName")
          when(appConfig.notAuthorisedRedirectUrl).thenReturn("not-authorised-redirect-url")

          authenticateWithNino.refine(fakeRequest).futureValue shouldBe Left(
            Redirect(
              s"https://iv-uplift-url?origin=appName&confidenceLevel=200&completionURL=$requestUrlEncoded&failureURL=not-authorised-redirect-url"
            )
          )
        }
      }

      "shows technical error page with internal server error" when {
        "any other auth failure occurs" in {
          when(authConnector.authorise[Option[String]](any(), any())(any(), any()))
            .thenReturn(Future.failed(IncorrectCredentialStrength()))
          when(technicalError.apply()(using any(), any())).thenReturn(Html("technical error page HTML"))

          authenticateWithNino.refine(fakeRequest).futureValue shouldBe Left(
            InternalServerError(Html("technical error page HTML"))
          )

          verify(technicalError).apply()(using eqTo(fakeRequest), any())
        }
      }
    }
  }

}
