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
import common.Strings
import config.{FrontendAppConfig, PlaContext}
import connectors.PlaConnectorError.LockedResponseError
import connectors.{PLAConnector, PlaConnectorError, PlaConnectorV2}
import constructors.DisplayConstructors
import enums.ApplicationType
import models._
import models.amendModels.AmendProtectionModel
import models.cache.CacheMap
import play.api.i18n.{I18nSupport, Lang}
import play.api.mvc._
import play.api.{Application, Logging}
import services.SessionCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReadProtectionsController @Inject() (
    plaConnector: PLAConnector,
    plaConnectorV2: PlaConnectorV2,
    sessionCacheService: SessionCacheService,
    displayConstructors: DisplayConstructors,
    mcc: MessagesControllerComponents,
    authFunction: AuthFunction,
    technicalError: views.html.pages.fallback.technicalError,
    manualCorrespondenceNeeded: views.html.pages.result.manualCorrespondenceNeeded,
    existingProtections: pages.existingProtections.existingProtections
)(
    implicit val plaContext: PlaContext,
    implicit val application: Application,
    implicit val appConfig: FrontendAppConfig,
    implicit val ec: ExecutionContext
) extends FrontendController(mcc)
    with I18nSupport
    with Logging {

  val currentProtections: Action[AnyContent] = Action.async { implicit request =>
    implicit val lang: Lang = mcc.messagesApi.preferred(request).lang

    authFunction.genericAuthWithNino("existingProtections") { nino =>
      fetchProtections(nino).flatMap {

        case Right(transformedReadResponseModel: TransformedReadResponseModel) =>
          saveAndDisplayExistingProtections(transformedReadResponseModel)

        case Left(LockedResponseError) => Future.successful(Locked(manualCorrespondenceNeeded()))

        case Left(_) =>
          Future.successful(
            InternalServerError(technicalError(ApplicationType.existingProtections.toString))
              .withHeaders(CACHE_CONTROL -> "no-cache")
          )
      }
    }
  }

  private def fetchProtections(
      nino: String
  )(implicit hc: HeaderCarrier): Future[Either[PlaConnectorError, TransformedReadResponseModel]] =
    if (appConfig.hipMigrationEnabled) {
      plaConnectorV2.readProtections(nino).map(_.map(TransformedReadResponseModel.from))
    } else {
      plaConnector.readProtections(nino).map(_.map(TransformedReadResponseModel.from))
    }

  private def saveAndDisplayExistingProtections(
      transformedReadResponseModel: TransformedReadResponseModel
  )(implicit request: Request[AnyContent], lang: Lang): Future[Result] =
    for {
      _ <- saveActiveProtection(transformedReadResponseModel.activeProtection)
      _ <- saveAmendableProtections(transformedReadResponseModel)

      displayModel = displayConstructors.createExistingProtectionsDisplayModel(transformedReadResponseModel)
    } yield Ok(existingProtections(displayModel))

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
    model.inactiveProtections.filter(_.isAmendable) ++
      model.activeProtection.filter(_.isAmendable)

  private def saveProtection(protection: ProtectionModel)(implicit request: Request[AnyContent]): Future[CacheMap] = {
    val cacheKey = Strings.protectionCacheKey(protection.protectionType, protection.status)
    sessionCacheService.saveFormData[AmendProtectionModel](
      cacheKey,
      AmendProtectionModel(protection, protection)
    )
  }

}
