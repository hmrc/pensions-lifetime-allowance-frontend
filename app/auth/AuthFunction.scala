/*
 * Copyright 2018 HM Revenue & Customs
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
import controllers.BaseController
import play.api.Logger
import uk.gov.hmrc.auth.core.AuthorisedFunctions
import play.api.mvc._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.play.frontend.config.AuthRedirects
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.auth.core.retrieve.Retrievals

import scala.concurrent.Future


trait AuthFunction extends BaseController with AuthRedirects  with AuthorisedFunctions{

  val postSignInRedirectUrl: String

  val appConfig : AppConfig
  val enrolmentKey : String = "HMRC-NI"
  val originString : String = "origin="
  val confidenceLevel : String = "&confidenceLevel=200"
  val completionURL : String = "&completionURL="
  val failureURL : String = "&failureURL="
  private lazy val IVUpliftURL : String = s"$personalIVUrl?" +
    s"$originString${appConfig.appName}" +
    s"$confidenceLevel" +
    s"$completionURL$postSignInRedirectUrl" +
    s"$failureURL${appConfig.notAuthorisedRedirectUrl}"

  class MissingNinoException extends Exception("Nino not returned by authorised call")

  def genericAuthWithoutNino(pType: String)(body: => Future[Result])(implicit request: Request[AnyContent]): Future[Result] = {
    authorised(Enrolment(enrolmentKey) and ConfidenceLevel.L200) {
      body
    }.recover(authErrorHandling(pType))
  }

  def genericAuthWithNino(pType: String)(body: String => Future[Result])(implicit request: Request[AnyContent]): Future[Result] = {
    authorised(Enrolment(enrolmentKey) and ConfidenceLevel.L200).retrieve(Retrievals.nino) { nino =>
      body(nino.getOrElse(throw new MissingNinoException))
    }.recover(authErrorHandling(pType))
  }

  def authErrorHandling(pType: String)(implicit request: Request[AnyContent]):PartialFunction[Throwable, Result] = {
    case _: NoActiveSession =>             toGGLogin(postSignInRedirectUrl)
    case _: InsufficientEnrolments =>      Redirect(IVUpliftURL)
    case _: InsufficientConfidenceLevel => Redirect(IVUpliftURL)
    case e: AuthorisationException => {
      Logger.error("Unexpected auth exception ", e)
      InternalServerError(views.html.pages.fallback.technicalError(pType))
    }
  }

}
