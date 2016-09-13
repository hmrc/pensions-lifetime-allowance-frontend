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
import auth.{PLAUser, AuthorisedForPLA}
import enums.ApplicationType
import config.{FrontendAuthConnector, FrontendAppConfig}
import connectors.{CitizenDetailsConnector, KeyStoreConnector}
import models.{PersonalDetailsModel, ProtectionDisplayModel}
import play.api.Logger
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.{SessionKeys, HeaderCarrier}
import uk.gov.hmrc.play.http.logging.SessionId
import play.api.mvc._
import scala.concurrent.Future


object PrintController extends PrintController {
  val keyStoreConnector = KeyStoreConnector
  val citizenDetailsConnector = CitizenDetailsConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val postSignInRedirectUrl = FrontendAppConfig.ip14StartUrl
}

trait PrintController extends FrontendController with AuthorisedForPLA {

  val keyStoreConnector: KeyStoreConnector
  val citizenDetailsConnector: CitizenDetailsConnector

  val printView = AuthorisedByAny.async { implicit user => implicit request =>

      user.nino.map { nino =>
        citizenDetailsConnector.getPersonDetails(nino).flatMap( model => {
          model.map {
            personalDetailsModel => routePrintView(personalDetailsModel, nino)
          }.getOrElse {
            Logger.error(s"Couldn't retrieve personal details for user nino: ${nino} in printView")
            Future.successful(InternalServerError(views.html.pages.fallback.technicalError("existingProtections")).withHeaders(CACHE_CONTROL -> "no-cache"))
          }
        }
        )
      }.getOrElse {
        Logger.error("No associated nino for user in printView action")
        Future.successful(InternalServerError(views.html.pages.fallback.technicalError("existingProtections")).withHeaders(CACHE_CONTROL -> "no-cache"))
      }
    }


  private def routePrintView(personalDetailsModel: PersonalDetailsModel, nino: String)(implicit request: Request[AnyContent]): Future[Result] = {
    keyStoreConnector.fetchAndGetFormData[ProtectionDisplayModel]("openProtection").map {
      case Some(model) => Ok(views.html.pages.result.resultPrint(personalDetailsModel, model, nino))
      case _ => {
        Logger.error(s"Unable to retrieve protection details from KeyStore for user nino: ${nino}")
        InternalServerError(views.html.pages.fallback.technicalError("existingProtections")).withHeaders(CACHE_CONTROL -> "no-cache")
      }
    }
  }


}
