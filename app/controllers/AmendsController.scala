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
import connectors.PlaConnectorError.{ConflictResponseError, IncorrectResponseBodyError, LockedResponseError}
import connectors.{CitizenDetailsConnector, PlaConnector, PlaConnectorError}
import constructors.AmendsGAConstructor
import constructors.display.DisplayConstructors
import models.amend.{AmendProtectionModel, AmendsGAModel}
import models.cache.CacheMap
import models.pla.AmendableProtectionType
import models.pla.request.AmendProtectionRequestStatus
import models.{AmendResponseModel, NotificationId, PersonalDetailsModel, TransformedReadResponseModel}
import play.api.Logging
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, MessagesRequest, RequestHeader, Result}
import services.SessionCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import util.NotificationIds

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AmendsController @Inject() (
    sessionCacheService: SessionCacheService,
    citizenDetailsConnector: CitizenDetailsConnector,
    plaConnector: PlaConnector,
    displayConstructors: DisplayConstructors,
    mcc: MessagesControllerComponents,
    authActions: AuthActions,
    manualCorrespondenceNeeded: views.html.pages.result.manualCorrespondenceNeeded,
    override val technicalError: views.html.pages.fallback.technicalError,
    amendOutcome: views.html.pages.amends.amendOutcome,
    amendOutcomeNoNotificationId: views.html.pages.amends.amendOutcomeNoNotificationId,
    amendSummary: views.html.pages.amends.amendSummary
)(using ExecutionContext)
    extends FrontendController(mcc)
    with Logging
    with AmendControllerErrorHelper {

  def amendsSummary(
      protectionType: AmendableProtectionType,
      status: AmendProtectionRequestStatus
  ): Action[AnyContent] =
    authActions.authenticateWithNino.async { request =>
      given MessagesRequest[?] = request

      sessionCacheService.fetchAmendProtectionModel(protectionType, status).map {
        case Some(amendModel) =>
          Ok(
            amendSummary(
              displayConstructors.createAmendDisplayModel(amendModel),
              protectionType,
              status
            )
          )
        case _ =>
          logger.warn(couldNotRetrieveModelForNino(request.nino, "when loading the amend summary page"))
          technicalErrorResult
      }
    }

  def amendProtection(
      protectionType: AmendableProtectionType,
      status: AmendProtectionRequestStatus
  ): Action[AnyContent] =
    authActions.authenticateWithNino.async { request =>
      given MessagesRequest[?] = request

      for {
        protectionAmendment <- sessionCacheService.fetchAmendProtectionModel(protectionType, status)
        _                   <- saveAmendsGA(protectionAmendment)

        response <- sendAmendProtectionRequest(request.nino, protectionAmendment.get)

        result <- response match {

          case Right(amendResponseModel: AmendResponseModel) =>
            saveAndRedirectToDisplay(amendResponseModel)

          case Left(LockedResponseError) =>
            Future.successful(Locked(manualCorrespondenceNeeded()))

          case Left(ConflictResponseError) =>
            Future.successful(technicalErrorResult)

          case Left(IncorrectResponseBodyError) =>
            Future.successful(technicalErrorResult)

          case Left(_) =>
            Future.successful(technicalErrorResult)
        }
      } yield result
    }

  private def saveAmendsGA(protectionAmendment: Option[AmendProtectionModel])(using RequestHeader): Future[CacheMap] =
    sessionCacheService.saveAmendsGAModel(
      AmendsGAConstructor.identifyAmendsChanges(
        protectionAmendment.get.updated,
        protectionAmendment.get.original
      )
    )

  private def sendAmendProtectionRequest(nino: String, protection: AmendProtectionModel)(
      using HeaderCarrier
  ): Future[Either[PlaConnectorError, AmendResponseModel]] =
    plaConnector
      .amendProtection(nino, protection)
      .map(_.map(AmendResponseModel.from(_, protection.psaCheckReference)))

  private def saveAndRedirectToDisplay(amendResponseModel: AmendResponseModel)(using RequestHeader): Future[Result] =
    sessionCacheService.saveAmendResponseModel(amendResponseModel).map { _ =>
      Redirect(routes.AmendsController.amendmentOutcome)
    }

  def amendmentOutcome: Action[AnyContent] =
    authActions.authenticateWithNino.async { request =>
      given MessagesRequest[?] = request

      val nino = request.nino

      for {
        modelAR                 <- sessionCacheService.fetchAmendResponseModel
        modelGA                 <- sessionCacheService.fetchAmendsGAModel
        personalDetailsModelOpt <- citizenDetailsConnector.getPersonDetails(nino)

        result <- amendmentOutcomeResult(modelAR, modelGA, personalDetailsModelOpt, nino)
      } yield result
    }

  private def amendmentOutcomeResult(
      modelAR: Option[AmendResponseModel],
      modelGA: Option[AmendsGAModel],
      personalDetailsModelOpt: Option[PersonalDetailsModel],
      nino: String
  )(using RequestHeader, Messages): Future[Result] = {
    if (modelGA.isEmpty) {
      logger.warn(s"Unable to retrieve amendsGAModel from cache for user nino :$nino")
    }

    modelAR
      .map { model =>
        model.notificationId match {
          case None =>
            val displayModel =
              displayConstructors.createAmendOutcomeDisplayModelNoNotificationId(model, personalDetailsModelOpt, nino)

            Future.successful(Ok(amendOutcomeNoNotificationId(displayModel)))
          case Some(notificationId) =>
            createProtectionModel(notificationId, model, nino).map {

              case Some(protectionModel) =>
                sessionCacheService.saveOpenProtection(protectionModel.toProtectionModel)
                val displayModel =
                  displayConstructors.createAmendOutcomeDisplayModel(
                    protectionModel,
                    personalDetailsModelOpt,
                    nino,
                    notificationId
                  )
                Ok(amendOutcome(displayModel))

              case None =>
                logger.warn(
                  s"Unable to retrieve fixed protection model from API GET endpoint for user with nino :$nino"
                )
                technicalErrorResult

            }
        }
      }
      .getOrElse {
        logger.warn(s"Unable to retrieve amendment outcome model from cache for user nino :$nino")
        Future.successful(
          technicalErrorResult
        )
      }
  }

  private def createProtectionModel(
      notificationId: NotificationId,
      model: AmendResponseModel,
      nino: String
  )(using RequestHeader): Future[Option[AmendResponseModel]] =
    if (NotificationIds.showingFixedProtection2016Details.contains(notificationId)) {
      createCombinedFixedAndIndividualProtectionModel(model, nino)
    } else {
      Future.successful(Some(model))
    }

  private def createCombinedFixedAndIndividualProtectionModel(
      amendResponseModel: AmendResponseModel,
      nino: String
  )(using RequestHeader): Future[Option[AmendResponseModel]] =
    for {
      protections <- fetchProtections(nino)
      activeProtection = protections.toOption.flatMap(_.activeProtection)
      combinedModel    = activeProtection.flatMap(amendResponseModel.combineWithFixedProtection2016)
    } yield combinedModel

  private def fetchProtections(
      nino: String
  )(using HeaderCarrier): Future[Either[PlaConnectorError, TransformedReadResponseModel]] =
    plaConnector.readProtections(nino).map(_.map(TransformedReadResponseModel.from))

}
