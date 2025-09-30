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
import common._
import config.FrontendAppConfig
import connectors.PlaConnectorError.{ConflictResponseError, IncorrectResponseBodyError, LockedResponseError}
import connectors.{CitizenDetailsConnector, PLAConnector, PlaConnectorError, PlaConnectorV2}
import constructors.{AmendsGAConstructor, DisplayConstructors}
import models.amendModels._
import models.cache.CacheMap
import models.{AmendResponseModel, PersonalDetailsModel, ProtectionModel, TransformedReadResponseModel}
import play.api.Logging
import play.api.i18n.{I18nSupport, Lang}
import play.api.mvc._
import services.SessionCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Constants

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AmendsController @Inject() (
    sessionCacheService: SessionCacheService,
    citizenDetailsConnector: CitizenDetailsConnector,
    plaConnector: PLAConnector,
    plaConnectorV2: PlaConnectorV2,
    displayConstructors: DisplayConstructors,
    mcc: MessagesControllerComponents,
    authFunction: AuthFunction,
    manualCorrespondenceNeeded: views.html.pages.result.manualCorrespondenceNeeded,
    noNotificationId: views.html.pages.fallback.noNotificationId,
    technicalError: views.html.pages.fallback.technicalError,
    outcomeActive: views.html.pages.amends.outcomeActive,
    outcomeInactive: views.html.pages.amends.outcomeInactive,
    outcomeAmended: views.html.pages.amends.outcomeAmended,
    amendSummary: views.html.pages.amends.amendSummary
)(implicit appConfig: FrontendAppConfig, ec: ExecutionContext)
    extends FrontendController(mcc)
    with I18nSupport
    with Logging
    with AmendControllerErrorHelper {

  def amendsSummary(protectionType: String, status: String): Action[AnyContent] = Action.async { implicit request =>
    implicit val lang: Lang = mcc.messagesApi.preferred(request).lang
    authFunction.genericAuthWithNino("existingProtections") { nino =>
      val protectionKey = Strings.protectionCacheKey(protectionType, status)
      sessionCacheService.fetchAndGetFormData[AmendProtectionModel](protectionKey).map {
        case Some(amendModel) =>
          Ok(
            amendSummary(
              displayConstructors.createAmendDisplayModel(amendModel),
              protectionType,
              status
            )
          )
        case _ =>
          logger.warn(couldNotRetrieveModelForNino(nino, "when loading the amend summary page"))
          buildTechnicalError(technicalError)
      }
    }
  }

  def amendProtection(protectionType: String, status: String): Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithNino("existingProtections") { nino =>
      for {
        protectionAmendment <- fetchProtectionAmendment(protectionType, status)
        _                   <- saveAmendsGA(protectionAmendment)

        response <- sendAmendProtectionRequest(nino, protectionAmendment.get.updatedProtection)

        result <- response match {

          case Right(amendResponseModel: AmendResponseModel) =>
            saveAndRedirectToDisplay(nino, amendResponseModel)

          case Left(LockedResponseError) =>
            Future.successful(Locked(manualCorrespondenceNeeded()))

          case Left(ConflictResponseError) =>
            Future.successful(buildTechnicalError(technicalError))

          case Left(IncorrectResponseBodyError) =>
            Future.successful(buildTechnicalError(technicalError))

          case Left(_) =>
            Future.successful(buildTechnicalError(technicalError))
        }
      } yield result
    }
  }

  private def fetchProtectionAmendment(protectionType: String, status: String)(
      implicit request: Request[AnyContent]
  ): Future[Option[AmendProtectionModel]] =
    sessionCacheService.fetchAndGetFormData[AmendProtectionModel](Strings.protectionCacheKey(protectionType, status))

  private def saveAmendsGA(
      protectionAmendment: Option[AmendProtectionModel]
  )(implicit request: Request[AnyContent]): Future[CacheMap] =
    sessionCacheService.saveFormData[AmendsGAModel](
      "AmendsGA",
      AmendsGAConstructor.identifyAmendsChanges(
        protectionAmendment.get.updatedProtection,
        protectionAmendment.get.originalProtection
      )
    )

  private def sendAmendProtectionRequest(nino: String, protection: ProtectionModel)(
      implicit hc: HeaderCarrier
  ): Future[Either[PlaConnectorError, AmendResponseModel]] =
    if (appConfig.hipMigrationEnabled) {
      plaConnectorV2
        .amendProtection(nino, protection)
        .map(_.map(AmendResponseModel.from(_, protection.psaCheckReference)))
    } else {
      plaConnector.amendProtection(nino, protection).map(_.map(AmendResponseModel(_)))
    }

  private def saveAndRedirectToDisplay(nino: String, amendResponseModel: AmendResponseModel)(
      implicit request: Request[AnyContent]
  ): Future[Result] =
    if (amendResponseModel.protection.notificationId.isDefined) {
      sessionCacheService.saveFormData[AmendResponseModel]("amendResponseModel", amendResponseModel).map { _ =>
        Redirect(routes.AmendsController.amendmentOutcome)
      }
    } else {
      logger.warn(s"No notification ID found in the AmendResponseModel for user with nino $nino")
      Future.successful(InternalServerError(noNotificationId()).withHeaders(CACHE_CONTROL -> "no-cache"))
    }

  def amendmentOutcome: Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithNino("existingProtections") { nino =>
      for {
        modelAR                 <- sessionCacheService.fetchAndGetFormData[AmendResponseModel]("amendResponseModel")
        modelGA                 <- sessionCacheService.fetchAndGetFormData[AmendsGAModel]("AmendsGA")
        personalDetailsModelOpt <- citizenDetailsConnector.getPersonDetails(nino)

        result <- amendmentOutcomeResult(modelAR, modelGA, personalDetailsModelOpt, nino)
      } yield result
    }
  }

  private def amendmentOutcomeResult(
      modelAR: Option[AmendResponseModel],
      modelGA: Option[AmendsGAModel],
      personalDetailsModelOpt: Option[PersonalDetailsModel],
      nino: String
  )(implicit request: Request[AnyContent]): Future[Result] = {
    if (modelGA.isEmpty) {
      logger.warn(s"Unable to retrieve amendsGAModel from cache for user nino :$nino")
    }

    modelAR
      .map { model =>
        val id = model.protection.notificationId.getOrElse {
          throw Exceptions.RequiredValueNotDefinedException("amendmentOutcome", "notificationId")
        }
        if (Constants.amendmentCodesList.contains(id)) {
          val protectionModel: Future[Either[Result, AmendResponseModel]] =
            if (Constants.fixedProtectionNotificationId.contains(id)) {
              createFixedAndIndividualProtectionModel(model, nino)
            } else {
              Future.successful(Right(model))
            }
          protectionModel.map {
            case Right(protectionModel) =>
              sessionCacheService.saveFormData[ProtectionModel]("openProtection", protectionModel.protection)
              val displayModel =
                displayConstructors.createAmendResultDisplayModel(protectionModel, personalDetailsModelOpt, nino)
              Ok(outcomeAmended(displayModel))
            case Left(result) =>
              result
          }
        } else if (Constants.activeAmendmentCodes.contains(id)) {
          sessionCacheService.saveFormData[ProtectionModel]("openProtection", model.protection)
          val displayModel =
            displayConstructors.createActiveAmendResponseDisplayModel(model, personalDetailsModelOpt, nino)
          Future.successful(Ok(outcomeActive(displayModel, modelGA, appConfig)))

        } else {
          val displayModel = displayConstructors.createInactiveAmendResponseDisplayModel(model)
          Future.successful(Ok(outcomeInactive(displayModel, modelGA)))
        }
      }
      .getOrElse {
        logger.warn(s"Unable to retrieve amendment outcome model from cache for user nino :$nino")
        Future.successful(
          buildTechnicalError(technicalError)
        )
      }
  }

  private def fetchFixedProtection2016(nino: String)(implicit hc: HeaderCarrier): Future[Option[ProtectionModel]] =

    fetchProtections(nino).map {

      case Right(transformedReadResponseModel: TransformedReadResponseModel) =>
        filterActiveFixedProtection2016(transformedReadResponseModel)

      case Left(_) =>
        logger.warn("Unable to retrieve Protection from GET point")
        None
    }

  private def fetchProtections(
      nino: String
  )(implicit hc: HeaderCarrier): Future[Either[PlaConnectorError, TransformedReadResponseModel]] =
    plaConnectorV2.readProtections(nino).map(_.map(TransformedReadResponseModel.from))

  private def filterActiveFixedProtection2016(model: TransformedReadResponseModel): Option[ProtectionModel] = {
    val fixedProtectionType = model.activeProtection.filter(_.isFixedProtection2016)
    if (fixedProtectionType.isEmpty) {
      logger.warn("There is no active Fixed Protection 2016")
      None
    } else {
      fixedProtectionType
    }
  }

  private def createFixedAndIndividualProtectionModel(
      model: AmendResponseModel,
      nino: String
  )(implicit request: Request[AnyContent]): Future[Either[Result, AmendResponseModel]] =
    fetchFixedProtection2016(nino).map { modelOption =>
      modelOption
        .map { fixedProtectionModel =>
          val modelCopy = model.protection.copy(
            protectionReference = fixedProtectionModel.protectionReference,
            certificateDate = fixedProtectionModel.certificateDate,
            protectionType = fixedProtectionModel.protectionType
          )
          Right(
            AmendResponseModel(modelCopy)
          )
        }
        .getOrElse(
          Left(throw Exceptions.RequiredNotFoundProtectionModelException("amendmentOutcome"))
        )
    }

}
