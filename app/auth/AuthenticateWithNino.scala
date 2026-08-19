/*
 * Copyright 2026 HM Revenue & Customs
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
import play.api.Logging
import play.api.mvc.Results.{InternalServerError, Redirect}
import play.api.mvc.{ActionRefiner, MessagesRequest, RequestHeader, Result}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.{
  AuthConnector,
  AuthorisationException,
  AuthorisedFunctions,
  ConfidenceLevel,
  Enrolment,
  InsufficientConfidenceLevel,
  InsufficientEnrolments,
  NoActiveSession
}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendHeaderCarrierProvider

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthenticateWithNino @Inject() (
    override val authConnector: AuthConnector,
    appConfig: AppConfig,
    technicalError: views.html.pages.fallback.technicalError
)(using override val executionContext: ExecutionContext)
    extends ActionRefiner[MessagesRequest, AuthenticatedRequest]
    with AuthorisedFunctions
    with Logging
    with FrontendHeaderCarrierProvider {

  override def refine[A](request: MessagesRequest[A]): Future[Either[Result, AuthenticatedRequest[A]]] = {
    given MessagesRequest[A] = request

    authorised(Enrolment(enrolmentKey).and(ConfidenceLevel.L200))
      .retrieve(Retrievals.nino)
      .apply { ninoOpt =>
        val nino                 = ninoOpt.getOrElse(throw MissingNinoException)
        val authenticatedRequest = AuthenticatedRequest(nino, request)
        Future.successful(Right(authenticatedRequest))
      }
      .recover(authErrorHandling)
  }

  private val enrolmentKey: String = "HMRC-NI"

  private def authErrorHandling[A](
      using request: MessagesRequest[A]
  ): PartialFunction[Throwable, Left[Result, AuthenticatedRequest[A]]] = {
    case _: NoActiveSession =>
      Left(redirectToSignIn)
    case _: InsufficientEnrolments      => Left(redirectToIvUplift)
    case _: InsufficientConfidenceLevel => Left(redirectToIvUplift)
    case e: AuthorisationException =>
      logger.error("Unexpected auth exception ", e)
      Left(InternalServerError(technicalError()))
  }

  private def redirectToSignIn(using request: RequestHeader): Result =
    Redirect(
      appConfig.ggSignInUrl,
      Map(
        "continue" -> Seq(s"${appConfig.sessionMissingUpliftUrlPrefix}${request.uri}"),
        "origin"   -> Seq(appConfig.appName)
      )
    )

  private def redirectToIvUplift(using request: RequestHeader): Result =
    Redirect(
      appConfig.ivUpliftUrl,
      Map(
        "origin"          -> Seq(appConfig.appName),
        "confidenceLevel" -> Seq("200"),
        "completionURL"   -> Seq(request.uri),
        "failureURL"      -> Seq(appConfig.notAuthorisedRedirectUrl)
      )
    )

}
