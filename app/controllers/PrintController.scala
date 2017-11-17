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

import auth.AuthFunction
import constructors.DisplayConstructors
import config.{AppConfig, AuthClientConnector, FrontendAppConfig}
import connectors.{CitizenDetailsConnector, KeyStoreConnector}
import models.{PersonalDetailsModel, ProtectionModel}
import play.api.{Configuration, Environment, Logger, Play}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._

import scala.concurrent.Future
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.Retrievals
import uk.gov.hmrc.play.frontend.config.AuthRedirects



object PrintController extends PrintController {
  val keyStoreConnector = KeyStoreConnector
  val citizenDetailsConnector = CitizenDetailsConnector
  val displayConstructors = DisplayConstructors
  lazy val appConfig = FrontendAppConfig
  override lazy val authConnector: AuthConnector = AuthClientConnector
  lazy val postSignInRedirectUrl = FrontendAppConfig.ip14StartUrl

  override def config: Configuration = Play.current.configuration
  override def env: Environment = Play.current.injector.instanceOf[Environment]
}

trait PrintController extends BaseController with AuthRedirects  with AuthorisedFunctions {

  val keyStoreConnector: KeyStoreConnector
  val citizenDetailsConnector: CitizenDetailsConnector
  val displayConstructors: DisplayConstructors
  val appConfig: AppConfig


  val printView = Action.async { implicit request =>
    authorised(Enrolment("HMRC-NI")).retrieve(Retrievals.nino) { externalId =>
      externalId.map { nino =>
        for {
          personalDetailsModel <- citizenDetailsConnector.getPersonDetails(nino)
          protectionModel <- keyStoreConnector.fetchAndGetFormData[ProtectionModel]("openProtection")
        } yield routePrintView(personalDetailsModel, protectionModel, nino)
      }.getOrElse {
        Logger.warn("No associated nino for user in printView action")
        Future.successful(InternalServerError(views.html.pages.fallback.technicalError("existingProtections")).withHeaders(CACHE_CONTROL -> "no-cache"))
      }
    }.recoverWith {
      case e: NoActiveSession => Future.successful(toGGLogin(appConfig.ipStartUrl))
      case e: InsufficientEnrolments => Future.successful(Redirect(s"$personalIVUrl?" +
        s"origin=${config.getString("appName")}" +
        s"&confidenceLevel=200" +
        s"&completionURL=${appConfig.ipStartUrl}" +
        s"&failureURL=${appConfig.notAuthorisedRedirectUrl}"))
      case e: InsufficientConfidenceLevel => Future.successful(Redirect(s"$personalIVUrl?" +
        s"origin=${config.getString("appname")}" +
        s"&confidenceLevel=200" +
        s"&completionURL=${appConfig.ipStartUrl}" +
        s"&failureURL=${appConfig.notAuthorisedRedirectUrl}"))
    }
  }


  private def routePrintView(personalDetailsModel: Option[PersonalDetailsModel], protectionModel: Option[ProtectionModel], nino: String)(implicit request: Request[AnyContent]): Result = {
    val displayModel = displayConstructors.createPrintDisplayModel(personalDetailsModel, protectionModel, nino)
      Ok(views.html.pages.result.resultPrint(displayModel))
  }




}
