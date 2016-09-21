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

import models._
import enums.ApplicationType
import auth.{PLAUser, AuthorisedForPLA}
import config.{FrontendAppConfig,FrontendAuthConnector}
import play.api.Logger
import play.api.mvc._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._
import play.api.libs.json.Json
import constructors.{ResponseConstructors, ExistingProtectionsConstructor}
import connectors.{KeyStoreConnector, PLAConnector}
import views.html._


object ReadProtectionsController extends ReadProtectionsController with ServicesConfig {
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val postSignInRedirectUrl = FrontendAppConfig.existingProtectionsUrl

  override val keyStoreConnector = KeyStoreConnector
  override val plaConnector = PLAConnector
}

trait ReadProtectionsController extends FrontendController with AuthorisedForPLA {

  val keyStoreConnector: KeyStoreConnector
  val plaConnector : PLAConnector

  val currentProtections = AuthorisedByAny.async {
    implicit user =>  implicit request =>
    plaConnector.readProtections(user.nino.get).map { response =>
      response.status match {
        case 200 => redirectFromSuccess(response)
        case 423 => Locked(pages.result.manualCorrespondenceNeeded())
        case num => {
          Logger.error(s"unexpected status $num passed to currentProtections for nino: ${user.nino}")
          InternalServerError(views.html.pages.fallback.technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
        }
      }
    }
  }

  def redirectFromSuccess(response: HttpResponse)(implicit request: Request[AnyContent], user: PLAUser): Result = {
    ResponseConstructors.createTransformedReadResponseModelFromJson(Json.parse(response.body)) match {
      case Some(model) => saveAndDisplayExistingProtections(model)
      case _ => {
        Logger.error(s"unable to create existing protections model from microservice response for nino: ${user.nino}")
        InternalServerError(views.html.pages.fallback.technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
      }
    }

  }

  def saveAndDisplayExistingProtections(model: TransformedReadResponseModel)(implicit request: Request[AnyContent]): Result = {
    model.activeProtection.map { activeModel =>
      keyStoreConnector.saveData[ProtectionModel]("openProtection", activeModel)
    }
    val displayModel: ExistingProtectionsDisplayModel = ExistingProtectionsConstructor.createExistingProtectionsDisplayModel(model)
    Ok(pages.existingProtections.existingProtections(displayModel))
  }

}
