/*
 * Copyright 2019 HM Revenue & Customs
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
import common.{Helpers, Strings}
import config.wiring.PlaFormPartialRetriever
import models._
import enums.ApplicationType
import config.{AppConfig, AuthClientConnector, FrontendAppConfig, LocalTemplateRenderer}
import models.amendModels.AmendProtectionModel
import play.api.{Configuration, Environment, Logger, Play}
import play.api.mvc._
import play.api.libs.json.Json
import constructors.{DisplayConstructors, ResponseConstructors}
import connectors.{KeyStoreConnector, PLAConnector}
import javax.inject.Inject
import uk.gov.hmrc.http.cache.client.CacheMap
import views.html._

import scala.concurrent.Future
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.{HttpResponse, NotFoundException, Upstream4xxResponse}


class ReadProtectionsControllerImpl @Inject()(val plaConnector: PLAConnector,
                                              val keyStoreConnector: KeyStoreConnector,
                                              implicit val partialRetriever: PlaFormPartialRetriever,
                                              implicit val templateRenderer:LocalTemplateRenderer) extends ReadProtectionsController {
  lazy val appConfig = FrontendAppConfig
  override lazy val authConnector: AuthConnector = AuthClientConnector
  lazy val postSignInRedirectUrl = FrontendAppConfig.existingProtectionsUrl

  val displayConstructors = DisplayConstructors
  val responseConstructors = ResponseConstructors

  override def config: Configuration = Play.current.configuration
  override def env: Environment = Play.current.injector.instanceOf[Environment]
}

trait ReadProtectionsController extends AuthFunction {
  val appConfig: AppConfig
  val authConnector: AuthConnector
  val postSignInRedirectUrl: String

  val displayConstructors: DisplayConstructors
  val responseConstructors: ResponseConstructors

  override def config: Configuration
  override def env: Environment

  val plaConnector: PLAConnector
  val keyStoreConnector: KeyStoreConnector
  implicit val partialRetriever: PlaFormPartialRetriever
  implicit val templateRenderer:LocalTemplateRenderer


  val currentProtections = Action.async {
    implicit request =>
      genericAuthWithNino("existingProtections") { nino =>
        plaConnector.readProtections(nino).flatMap { response =>
          response.status match {
            case OK => redirectFromSuccess(response, nino)
            case LOCKED => Future.successful(Locked(pages.result.manualCorrespondenceNeeded()))
            case num => {
              Logger.error(s"unexpected status $num passed to currentProtections for nino: $nino")
              Future.successful(InternalServerError(views.html.pages.fallback.technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache"))
            }
          }
        }.recover {
          case e: NotFoundException => Logger.warn(s"Error 404 passed to currentProtections for nino: $nino")
            throw new Upstream4xxResponse(e.message, 404, 500)
          case otherException: Exception => throw otherException
        }
      }
  }

  def redirectFromSuccess(response: HttpResponse, nino: String)(implicit request: Request[AnyContent]): Future[Result] = {
    responseConstructors.createTransformedReadResponseModelFromJson(Json.parse(response.body)).map {
      readResponseModel =>
        saveAndDisplayExistingProtections(readResponseModel)
    }.getOrElse {
      Logger.warn(s"unable to create transformed read response model from microservice response for nino: $nino")
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

  def saveNonAmendableProtections(model: TransformedReadResponseModel)(implicit request: Request[AnyContent]): Future[Seq[CacheMap]] = {
    Future.sequence(getNonAmendableProtections(model).map(x => saveNonAmendableProtection(x)))
  }

  def getNonAmendableProtections(model: TransformedReadResponseModel): Seq[ProtectionModel] = {
    model.inactiveProtections.filter(!Helpers.protectionIsAmendable(_)) ++ model.activeProtection.filter(!Helpers.protectionIsAmendable(_))
  }

  def saveNonAmendableProtection(protection: ProtectionModel)(implicit request: Request[AnyContent]): Future[CacheMap] = {
    keyStoreConnector.saveData[ProtectionModel](Strings.keyStoreNonAmendableProtectionName(protection), protection)
  }

}

