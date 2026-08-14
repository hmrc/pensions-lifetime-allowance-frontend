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

import auth.AuthActions
import config.AppConfig
import connectors.PlaConnectorError.LockedResponseError
import connectors.{PlaConnector, PlaConnectorError}
import constructors.display.DisplayConstructors
import models._
import models.amend.AmendProtectionModel
import models.cache.CacheMap
import play.api.i18n.Messages
import play.api.mvc._
import play.api.{Application, Logging}
import services.SessionCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReadProtectionsController @Inject() (
    plaConnector: PlaConnector,
    sessionCacheService: SessionCacheService,
    displayConstructors: DisplayConstructors,
    mcc: MessagesControllerComponents,
    authActions: AuthActions,
    technicalError: views.html.pages.fallback.technicalError,
    manualCorrespondenceNeeded: views.html.pages.result.manualCorrespondenceNeeded,
    existingProtections: pages.existingProtections.existingProtections,
    appConfig: AppConfig
)(
    using ExecutionContext
) extends FrontendController(mcc)
    with Logging {

  def currentProtections: Action[AnyContent] = authActions.authenticateWithNino.async { implicit request =>
    fetchProtections(request.nino).flatMap {

      case Right(transformedReadResponseModel: TransformedReadResponseModel) =>
        saveAndDisplayExistingProtections(transformedReadResponseModel)

      case Left(LockedResponseError) => Future.successful(Locked(manualCorrespondenceNeeded()))

      case Left(_) =>
        Future.successful(
          InternalServerError(technicalError())
            .withHeaders(CACHE_CONTROL -> "no-cache")
        )
    }
  }

  private[controllers] def fetchProtections(
      nino: String
  )(implicit hc: HeaderCarrier): Future[Either[PlaConnectorError, TransformedReadResponseModel]] =
    plaConnector.readProtections(nino).map(_.map(TransformedReadResponseModel.from))

  private[controllers] def saveAndDisplayExistingProtections(
      transformedReadResponseModel: TransformedReadResponseModel
  )(implicit request: RequestHeader, messages: Messages): Future[Result] =
    for {
      _ <- saveActiveProtection(transformedReadResponseModel.activeProtection)
      _ <- saveAmendableProtections(transformedReadResponseModel)

      displayModel = displayConstructors.createExistingProtectionsDisplayModel(transformedReadResponseModel)
    } yield Ok(existingProtections(displayModel))

  private[controllers] def saveActiveProtection(
      activeModel: Option[ProtectionModel]
  )(implicit request: RequestHeader): Future[Option[CacheMap]] =
    activeModel.map(sessionCacheService.saveOpenProtection) match {
      case Some(future) => future.map(Some(_))
      case None         => Future.successful(None)
    }

  private[controllers] def saveAmendableProtections(model: TransformedReadResponseModel)(
      implicit request: RequestHeader
  ): Future[Seq[CacheMap]] = {
    val allProtections = getAllProtections(model)
    val protections    = allProtections.flatMap(saveIfAmendable)
    Future.sequence(protections)
  }

  private[controllers] def getAllProtections(model: TransformedReadResponseModel): Seq[ProtectionModel] =
    model.activeProtection.toSeq ++ model.inactiveProtections

  private[controllers] def saveIfAmendable(protection: ProtectionModel)(
      implicit request: RequestHeader
  ): Option[Future[CacheMap]] =
    AmendProtectionModel.tryFromProtection(protection).map(sessionCacheService.saveAmendProtectionModel)

}
