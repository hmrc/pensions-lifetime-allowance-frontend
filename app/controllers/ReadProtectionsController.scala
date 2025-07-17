/*
 * Copyright 2023 HM Revenue & Customs
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
import config.{FrontendAppConfig, PlaContext}
import connectors.PLAConnector
import constructors.{DisplayConstructors, ResponseConstructors}
import enums.ApplicationType
import models._
import models.amendModels.AmendProtectionModel
import models.cache.CacheMap
import play.api.i18n.{I18nSupport, Lang}
import play.api.libs.json.Json
import play.api.mvc._
import play.api.{Application, Logging}
import services.SessionCacheService
import uk.gov.hmrc.http.{HttpResponse, NotFoundException, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReadProtectionsController @Inject() (
    val plaConnector: PLAConnector,
    val sessionCacheService: SessionCacheService,
    displayConstructors: DisplayConstructors,
    mcc: MessagesControllerComponents,
    responseConstructors: ResponseConstructors,
    authFunction: AuthFunction,
    technicalError: views.html.pages.fallback.technicalError,
    manualCorrespondenceNeeded: views.html.pages.result.manualCorrespondenceNeeded,
    existingProtections: pages.existingProtections.existingProtections
)(
    implicit val appConfig: FrontendAppConfig,
    implicit val plaContext: PlaContext,
    implicit val application: Application,
    implicit val ec: ExecutionContext
) extends FrontendController(mcc)
    with I18nSupport
    with Logging {

  val currentProtections: Action[AnyContent] = Action.async { implicit request =>
    implicit val lang: Lang = mcc.messagesApi.preferred(request).lang

    authFunction.genericAuthWithNino("existingProtections") { nino =>
      plaConnector
        .readProtections(nino)
        .flatMap { response =>
          response.status match {
            case OK     => handleSuccess(response, nino)
            case LOCKED => Future.successful(Locked(manualCorrespondenceNeeded()))
            case num =>
              logger.error(s"unexpected status $num passed to currentProtections for nino: $nino")
              Future.successful(
                InternalServerError(technicalError(ApplicationType.existingProtections.toString))
                  .withHeaders(CACHE_CONTROL -> "no-cache")
              )
          }
        }
        .recover {
          case e: NotFoundException =>
            logger.warn(s"Error 404 passed to currentProtections for nino: $nino")
            throw UpstreamErrorResponse(e.message, 404, 500)
          case otherException: Exception => throw otherException
        }
    }
  }

  private def handleSuccess(
      response: HttpResponse,
      nino: String
  )(implicit request: Request[AnyContent], lang: Lang): Future[Result] =
    responseConstructors
      .createTransformedReadResponseModelFromJson(Json.parse(response.body))
      .map(readResponseModel => saveAndDisplayExistingProtections(readResponseModel))
      .getOrElse {
        logger.warn(s"unable to create transformed read response model from microservice response for nino: $nino")
        Future.successful(
          InternalServerError(technicalError(ApplicationType.existingProtections.toString))
            .withHeaders(CACHE_CONTROL -> "no-cache")
        )
      }

  private def saveAndDisplayExistingProtections(
      model: TransformedReadResponseModel
  )(implicit request: Request[AnyContent], lang: Lang): Future[Result] = {
    val displayModel: ExistingProtectionsDisplayModel = displayConstructors.createExistingProtectionsDisplayModel(model)
    for {
      _ <- saveActiveProtection(model.activeProtection)
      _ <- saveAmendableProtections(model)
    } yield Ok(existingProtections(displayModel))
  }

  def saveActiveProtection(
      activeModel: Option[ProtectionModel]
  )(implicit request: Request[AnyContent]): Future[Boolean] =
    activeModel
      .map(model => sessionCacheService.saveFormData[ProtectionModel]("openProtection", model).map(_ => true))
      .getOrElse(Future.successful(true))

  def saveAmendableProtections(model: TransformedReadResponseModel)(
      implicit request: Request[AnyContent]
  ): Future[Seq[CacheMap]] =
    Future.sequence(getAmendableProtections(model).map(saveProtection))

  def getAmendableProtections(model: TransformedReadResponseModel): Seq[ProtectionModel] =
    model.inactiveProtections.filter(Helpers.protectionIsAmendable) ++ model.activeProtection.filter(
      Helpers.protectionIsAmendable
    )

  private def saveProtection(protection: ProtectionModel)(implicit request: Request[AnyContent]): Future[CacheMap] =
    sessionCacheService.saveFormData[AmendProtectionModel](
      Strings.cacheProtectionName(protection),
      AmendProtectionModel(protection, protection)
    )

}
