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

import config.{FrontendAppConfig, PlaContext}
import play.api.Logging
import play.api.i18n.Messages
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthFunctionImpl @Inject() (
    mcc: MessagesControllerComponents,
    authClientConnector: AuthConnector,
    val technicalError: views.html.pages.fallback.technicalError
)(implicit val appConfig: FrontendAppConfig, implicit val plaContext: PlaContext, implicit val ec: ExecutionContext)
    extends FrontendController(mcc)
    with AuthFunction
    with Logging {
  override def authConnector: AuthConnector = authClientConnector
}

trait AuthFunction extends AuthorisedFunctions with Logging {
  implicit val plaContext: PlaContext
  implicit val appConfig: FrontendAppConfig
  implicit val ec: ExecutionContext
  val technicalError: views.html.pages.fallback.technicalError
  val enrolmentKey: String    = "HMRC-NI"
  val originString: String    = "origin="
  val confidenceLevel: String = "&confidenceLevel=200"
  val completionURL: String   = "&completionURL="
  val failureURL: String      = "&failureURL="

  private def IVUpliftURL()(implicit request: Request[AnyContent]): String = s"${appConfig.ivUpliftUrl}?" +
    s"$originString${appConfig.appName}" +
    s"$confidenceLevel" +
    s"$completionURL${request.uri}" +
    s"$failureURL${appConfig.notAuthorisedRedirectUrl}"

  class MissingNinoException extends Exception("Nino not returned by authorised call")

  def genericAuthWithoutNino(pType: String)(
      body: => Future[Result]
  )(implicit request: Request[AnyContent], messages: Messages, hc: HeaderCarrier): Future[Result] =
    authorised(Enrolment(enrolmentKey).and(ConfidenceLevel.L200)) {
      body
    }.recover(authErrorHandling(pType))

  def genericAuthWithNino(pType: String)(
      body: String => Future[Result]
  )(implicit request: Request[AnyContent], messages: Messages, hc: HeaderCarrier): Future[Result] =
    authorised(Enrolment(enrolmentKey).and(ConfidenceLevel.L200))
      .retrieve(Retrievals.nino)(nino => body(nino.getOrElse(throw new MissingNinoException)))
      .recover(authErrorHandling(pType))

  def authErrorHandling(
      pType: String
  )(implicit request: Request[AnyContent], messages: Messages): PartialFunction[Throwable, Result] = {
    case _: NoActiveSession =>
      val upliftUrl = upliftEnvironmentUrl(request.uri)
      Redirect(appConfig.ggSignInUrl, Map("continue" -> Seq(upliftUrl), "origin" -> Seq(appConfig.appName)))
    case _: InsufficientEnrolments      => Redirect(IVUpliftURL())
    case _: InsufficientConfidenceLevel => Redirect(IVUpliftURL())
    case e: AuthorisationException =>
      logger.error("Unexpected auth exception ", e)
      InternalServerError(technicalError(pType))
  }

  def upliftEnvironmentUrl(requestUri: String): String =
    appConfig.sessionMissingUpliftUrlPrefix match {
      case Some(prefix) => prefix + requestUri
      case _            => requestUri
    }

}
