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

import models.ExistingProtectionsModel
import auth.AuthorisedForPLA
import config.{FrontendAppConfig,FrontendAuthConnector}
import play.api.mvc._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._
import play.api.libs.json.Json
import constructors.{ResponseConstructors, ExistingProtectionsConstructor}
import connectors.PLAConnector
import views.html._


object ReadProtectionsController extends ReadProtectionsController with ServicesConfig {
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val postSignInRedirectUrl = FrontendAppConfig.existingProtectionsUrl

  override val plaConnector = PLAConnector
}

trait ReadProtectionsController extends FrontendController with AuthorisedForPLA {

  val plaConnector : PLAConnector

  val currentProtections = AuthorisedByAny.async {
    implicit user =>  implicit request =>
    plaConnector.readProtections(user.nino.get).map { response =>
      response.status match {
        case 200 => redirectFromSuccess(response)
                  // TODO: Redirect to technical error
        case _ => Redirect(routes.IntroductionController.introduction())
      }
    }
  }

  def redirectFromSuccess(response: HttpResponse)(implicit request: Request[AnyContent]): Result = {
    ResponseConstructors.createExistingProtectionsModelFromJson(Json.parse(response.body)) match {
      case Some(model) => displayExistingProtections(model)
                  // TODO: Redirect to technical error
      case _ => Redirect(routes.IntroductionController.introduction())
    }

  }

  def displayExistingProtections(model: ExistingProtectionsModel)(implicit request: Request[AnyContent]): Result = {
    val displayModel = ExistingProtectionsConstructor.createExistingProtectionsDisplayModel(model)
    Ok(pages.existingProtections.existingProtections(displayModel))
  }

}
