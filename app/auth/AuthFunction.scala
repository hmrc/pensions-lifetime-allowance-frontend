/*
 * Copyright 2021 HM Revenue & Customs
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
import config.wiring.PlaFormPartialRetriever
import config.{FrontendAppConfig, LocalTemplateRenderer, PlaContext}
import javax.inject.Inject
import play.api.i18n.Messages
import play.api.mvc.Results._
import play.api.mvc._
import play.api.{Configuration, Environment, Logger}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.{AuthorisedFunctions, _}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.config.AuthRedirects
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthFunctionImpl @Inject()(mcc: MessagesControllerComponents,
                                 authClientConnector: AuthConnector,
                                 val env: Environment)(
                                  implicit val appConfig: FrontendAppConfig,
                                  implicit val partialRetriever: PlaFormPartialRetriever,
                                  implicit val templateRenderer:LocalTemplateRenderer,
                                  implicit val plaContext: PlaContext)
  extends FrontendController(mcc) with AuthFunction {
  override def config: Configuration = appConfig.configuration
  override def authConnector: AuthConnector = authClientConnector
}
trait AuthFunction extends AuthRedirects with AuthorisedFunctions {

  implicit val partialRetriever: PlaFormPartialRetriever
  implicit val templateRenderer: LocalTemplateRenderer
  implicit val plaContext: PlaContext
  implicit val appConfig: FrontendAppConfig

  val enrolmentKey: String = "HMRC-NI"
  val originString: String = "origin="
  val confidenceLevel: String = "&confidenceLevel=200"
  val completionURL: String = "&completionURL="
  val failureURL: String = "&failureURL="

  private def IVUpliftURL()(implicit request: Request[AnyContent]): String = s"$personalIVUrl?" +
    s"$originString${appConfig.appName}" +
    s"$confidenceLevel" +
    s"$completionURL${request.uri}" +
    s"$failureURL${appConfig.notAuthorisedRedirectUrl}"

  class MissingNinoException extends Exception("Nino not returned by authorised call")

  def genericAuthWithoutNino(pType: String)(body: => Future[Result])(implicit request: Request[AnyContent], messages: Messages, hc: HeaderCarrier): Future[Result] = {
    authorised(Enrolment(enrolmentKey) and ConfidenceLevel.L200) {
      body
    }.recover(authErrorHandling(pType))
  }

  def genericAuthWithNino(pType: String)(body: String => Future[Result])(implicit request: Request[AnyContent], messages: Messages, hc: HeaderCarrier): Future[Result] = {
    authorised(Enrolment(enrolmentKey) and ConfidenceLevel.L200).retrieve(Retrievals.nino) { nino =>
      body(nino.getOrElse(throw new MissingNinoException))
    }.recover(authErrorHandling(pType))
  }


  def authErrorHandling(pType: String)(implicit request: Request[AnyContent], messages: Messages): PartialFunction[Throwable, Result] = {
    case _: NoActiveSession => {
      val upliftUrl = upliftEnvironmentUrl(request.uri)
      Redirect(appConfig.ggSignInUrl,
        Map("continue" -> Seq(upliftUrl), "origin" -> Seq(appConfig.appName)))
    }
    case _: InsufficientEnrolments => Redirect(IVUpliftURL)
    case _: InsufficientConfidenceLevel => Redirect(IVUpliftURL)
    case e: AuthorisationException =>
      Logger.error("Unexpected auth exception ", e)
      InternalServerError(views.html.pages.fallback.technicalError(pType))

  }

  def upliftEnvironmentUrl(requestUri: String): String = {
    appConfig.sessionMissingUpliftUrlPrefix match {
      case Some(prefix) => prefix + requestUri
      case _ => requestUri
    }
  }

}
