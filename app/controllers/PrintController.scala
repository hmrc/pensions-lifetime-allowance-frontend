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

import auth.AuthorisedForPLA
import constructors.DisplayConstructors
import config.{FrontendAuthConnector, FrontendAppConfig}
import connectors.{CitizenDetailsConnector, KeyStoreConnector}
import models.{ProtectionModel, PersonalDetailsModel}
import play.api.Logger
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._
import scala.concurrent.Future


object PrintController extends PrintController {
  val keyStoreConnector = KeyStoreConnector
  val citizenDetailsConnector = CitizenDetailsConnector
  val displayConstructors = DisplayConstructors
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val postSignInRedirectUrl = FrontendAppConfig.ip14StartUrl
}

trait PrintController extends FrontendController with AuthorisedForPLA {

  val keyStoreConnector: KeyStoreConnector
  val citizenDetailsConnector: CitizenDetailsConnector
  val displayConstructors: DisplayConstructors

  val printView = AuthorisedByAny.async { implicit user => implicit request =>

      user.nino.map { nino =>
        for {
          personalDetailsModel <- citizenDetailsConnector.getPersonDetails(nino)
          protectionModel <- keyStoreConnector.fetchAndGetFormData[ProtectionModel]("openProtection")
        } yield routePrintView(personalDetailsModel, protectionModel, nino)
      }.getOrElse {
        Logger.error("No associated nino for user in printView action")
        Future.successful(InternalServerError(views.html.pages.fallback.technicalError("existingProtections")).withHeaders(CACHE_CONTROL -> "no-cache"))
      }
    }


  private def routePrintView(personalDetailsModel: Option[PersonalDetailsModel], protectionModel: Option[ProtectionModel], nino: String)(implicit request: Request[AnyContent]): Result = {
    println("\n\nPrintView: " + protectionModel + "\n\n")
    val displayModel = displayConstructors.createPrintDisplayModel(personalDetailsModel, protectionModel, nino)
      Ok(views.html.pages.result.resultPrint(displayModel))
  }




}
