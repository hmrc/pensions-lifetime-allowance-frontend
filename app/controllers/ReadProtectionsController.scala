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

import common.{Helpers, Strings}
import models._
import enums.ApplicationType
import auth.{AuthorisedForPLA, PLAUser}
import config.{FrontendAppConfig, FrontendAuthConnector}
import models.amendModels.AmendProtectionModel
import play.api.Logger
import play.api.mvc._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._
import play.api.libs.json.Json
import constructors.{DisplayConstructors, ResponseConstructors}
import connectors.{KeyStoreConnector, PLAConnector}
import uk.gov.hmrc.http.cache.client.CacheMap
import views.html._

import scala.concurrent.Future
import play.api.i18n.Messages.Implicits._
import play.api.Play.current


object ReadProtectionsController extends ReadProtectionsController with ServicesConfig {
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val postSignInRedirectUrl = FrontendAppConfig.existingProtectionsUrl

  override val keyStoreConnector = KeyStoreConnector
  override val plaConnector = PLAConnector
  override val displayConstructors = DisplayConstructors
  override val responseConstructors = ResponseConstructors
}

trait ReadProtectionsController extends FrontendController with AuthorisedForPLA {

  val keyStoreConnector: KeyStoreConnector
  val plaConnector : PLAConnector
  val displayConstructors : DisplayConstructors
  val responseConstructors : ResponseConstructors

  val currentProtections = AuthorisedByAny.async {
    implicit user =>  implicit request =>
    plaConnector.readProtections(user.nino.get).flatMap { response =>
      response.status match {
        case 200 => redirectFromSuccess(response)
        case 423 => Future.successful(Locked(pages.result.manualCorrespondenceNeeded()))
        case num => {
          Logger.error(s"unexpected status $num passed to currentProtections for nino: ${user.nino}")
          Future.successful(InternalServerError(views.html.pages.fallback.technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache"))
        }
      }
    }.recover{
      case e: NotFoundException => Logger.warn(s"Error 404 passed to currentProtections for nino: ${user.nino}")
        throw new Upstream4xxResponse(e.message, 404, 500)
      case otherException: Exception => throw otherException
    }
  }

  def redirectFromSuccess(response: HttpResponse)(implicit request: Request[AnyContent], user: PLAUser): Future[Result] = {
    responseConstructors.createTransformedReadResponseModelFromJson(Json.parse(response.body)).map {
      readResponseModel =>
        saveAndDisplayExistingProtections(readResponseModel)
    }.getOrElse{
      Logger.warn(s"unable to create transformed read response model from microservice response for nino: ${user.nino}")
      Future.successful(InternalServerError(views.html.pages.fallback.technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache"))
    }
  }

  def saveAndDisplayExistingProtections(model: TransformedReadResponseModel)(implicit request: Request[AnyContent]): Future[Result] = {

    val displayModel: ExistingProtectionsDisplayModel = displayConstructors.createExistingProtectionsDisplayModel(model)
    for {
      stepOne <- saveActiveProtection(model.activeProtection)
      stepTwo <- saveAmendableProtections(model)
    } yield Ok(pages.existingProtections.existingProtections(displayModel))

  }

  def saveActiveProtection(activeModel: Option[ProtectionModel])(implicit request: Request[AnyContent]): Future[Boolean] = {
    activeModel.map { model =>
      keyStoreConnector.saveData[ProtectionModel]("openProtection", model).map { cacheMap =>
        true
      }
    }.getOrElse(Future.successful(true))
  }

  def saveAmendableProtections(model: TransformedReadResponseModel)(implicit request: Request[AnyContent]): Future[Seq[CacheMap]] = {
    Future.sequence(getAmendableProtections(model).map(x => saveProtection(x)))
  }

  def getAmendableProtections(model: TransformedReadResponseModel): Seq[ProtectionModel] = {
    model.inactiveProtections.filter(Helpers.protectionIsAmendable) ++ model.activeProtection.filter(Helpers.protectionIsAmendable)
  }

  def saveProtection(protection: ProtectionModel)(implicit request: Request[AnyContent]): Future[CacheMap] = {
    keyStoreConnector.saveData[AmendProtectionModel](Strings.keyStoreProtectionName(protection), AmendProtectionModel(protection, protection))
  }

}
