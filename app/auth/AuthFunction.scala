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

  val appConfig : AppConfig
  val enrolmentKey : String = "HMRC-NI"
  val originString : String = "origin="
  val confidenceLevel : String = "&confidenceLevel=200"
  val completionURL : String = "&completionURL="
  val failureURL : String = "&failureURL="
  val appName : String = "appName"

  class MissingNinoException extends Exception("Nino not returned by authorised call")

  def genericAuthWithoutNino(pType: String)(body: Future[Result])(implicit request: Request[AnyContent]): Future[Result] = {
    authorised(Enrolment(enrolmentKey) and ConfidenceLevel.L200) {
      body
    }.recoverWith(authErrorHandling(pType))
  }

  def genericAuthWithNino(pType: String)(body: String => Future[Result])(implicit request: Request[AnyContent]): Future[Result] = {
      authorised(Enrolment(enrolmentKey) and ConfidenceLevel.L200).retrieve(Retrievals.nino) { nino =>
      body(nino.getOrElse(throw new MissingNinoException))
    }.recoverWith(authErrorHandling(pType))
  }

  def authErrorHandling(pType: String)(implicit request: Request[AnyContent]):PartialFunction[Throwable, Future[Result]] = {
    case e: NoActiveSession => Future.successful(toGGLogin(appConfig.ipStartUrl))
    case e: InsufficientEnrolments => Future.successful(Redirect(s"$personalIVUrl?" +
      s"$originString${config.getString(appName)}" +
      s"$confidenceLevel" +
      s"$completionURL${appConfig.ipStartUrl}" +
      s"$failureURL${appConfig.notAuthorisedRedirectUrl}"))
    case e: InsufficientConfidenceLevel => Future.successful(Redirect(s"$personalIVUrl?" +
      s"$originString${config.getString(appName)}" +
      s"$confidenceLevel" +
      s"$completionURL${appConfig.ipStartUrl}" +
      s"$failureURL${appConfig.notAuthorisedRedirectUrl}"))
    case e: AuthorisationException => Future.successful(InternalServerError(views.html.pages.fallback.technicalError(pType)))
  }

}
