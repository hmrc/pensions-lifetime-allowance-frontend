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
import connectors.{CitizenDetailsConnector, PlaConnector, PlaConnectorError}
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
    plaConnector: PlaConnector,
    displayConstructors: DisplayConstructors,
    mcc: MessagesControllerComponents,
    authFunction: AuthFunction,
    manualCorrespondenceNeeded: views.html.pages.result.manualCorrespondenceNeeded,
    technicalError: views.html.pages.fallback.technicalError,
    outcomeActive: views.html.pages.amends.outcomeActive,
    outcomeInactive: views.html.pages.amends.outcomeInactive,
    outcomeAmended: views.html.pages.amends.outcomeAmended,
    outcomeNoNotificationId: views.html.pages.amends.outcomeNoNotificationId,
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
            saveAndRedirectToDisplay(amendResponseModel)

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
    plaConnector
      .amendProtection(nino, protection)
      .map(_.map(AmendResponseModel.from(_, protection.psaCheckReference)))

  private def saveAndRedirectToDisplay(amendResponseModel: AmendResponseModel)(
      implicit request: Request[AnyContent]
  ): Future[Result] =
    sessionCacheService.saveFormData[AmendResponseModel]("amendResponseModel", amendResponseModel).map { _ =>
      Redirect(routes.AmendsController.amendmentOutcome)
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
        model.protection.notificationId match {
          case None =>
            val displayModel =
              displayConstructors.createAmendResultDisplayModelNoNotificationId(model, personalDetailsModelOpt, nino)

            Future.successful(Ok(outcomeNoNotificationId(displayModel)))
          case Some(notificationId) =>
            if (Constants.amendmentCodesList.contains(notificationId)) {
              createProtectionModel(notificationId, model, nino).map {

                case Some(protectionModel) =>
                  sessionCacheService.saveFormData[ProtectionModel]("openProtection", protectionModel.protection)
                  val displayModel =
                    displayConstructors.createAmendResultDisplayModel(protectionModel, personalDetailsModelOpt, nino)
                  Ok(outcomeAmended(displayModel))

                case None =>
                  logger.warn(
                    s"Unable to retrieve fixed protection model from API GET endpoint for user with nino :$nino"
                  )
                  buildTechnicalError(technicalError)

              }
            } else if (Constants.activeAmendmentCodes.contains(notificationId)) {
              sessionCacheService.saveFormData[ProtectionModel]("openProtection", model.protection)
              val displayModel =
                displayConstructors.createActiveAmendResponseDisplayModel(model, personalDetailsModelOpt, nino)
              Future.successful(Ok(outcomeActive(displayModel, modelGA, appConfig)))

            } else {
              val displayModel = displayConstructors.createInactiveAmendResponseDisplayModel(model)
              Future.successful(Ok(outcomeInactive(displayModel, modelGA)))
            }
        }
      }
      .getOrElse {
        logger.warn(s"Unable to retrieve amendment outcome model from cache for user nino :$nino")
        Future.successful(
          buildTechnicalError(technicalError)
        )
      }
  }

  private def createProtectionModel(
      notificationId: Int,
      model: AmendResponseModel,
      nino: String
  )(implicit request: Request[AnyContent]): Future[Option[AmendResponseModel]] =
    if (Constants.fixedProtectionNotificationIds.contains(notificationId)) {
      createFixedAndIndividualProtectionModel(notificationId, nino)
    } else {
      Future.successful(Some(model))
    }

  private def createFixedAndIndividualProtectionModel(
      notificationId: Int,
      nino: String
  )(implicit request: Request[AnyContent]): Future[Option[AmendResponseModel]] =
    for {
      fixedProtectionOpt <- fetchProtections(nino).map(_.toOption.flatMap(filterActiveFixedProtection2016))

      res = fixedProtectionOpt.map { fixedProtection =>
        AmendResponseModel(fixedProtection.copy(notificationId = Some(notificationId)))
      }
    } yield res

  private def fetchProtections(
      nino: String
  )(implicit hc: HeaderCarrier): Future[Either[PlaConnectorError, TransformedReadResponseModel]] =
    plaConnector.readProtections(nino).map(_.map(TransformedReadResponseModel.from))

  private def filterActiveFixedProtection2016(model: TransformedReadResponseModel): Option[ProtectionModel] = {
    val fixedProtectionType = model.activeProtection.filter(_.isFixedProtection2016)
    if (fixedProtectionType.isEmpty) {
      logger.warn("There is no active Fixed Protection 2016")
      None
    } else {
      fixedProtectionType
    }
  }

}
