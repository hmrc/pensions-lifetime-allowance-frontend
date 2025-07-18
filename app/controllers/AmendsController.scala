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
import config.{FrontendAppConfig, PlaContext}
import connectors.PlaConnectorError.{ConflictResponseError, IncorrectResponseBodyError, LockedResponseError}
import connectors.{PLAConnector, PlaConnectorError}
import constructors.{AmendsGAConstructor, DisplayConstructors}
import enums.ApplicationType
import models.amendModels._
import models.cache.CacheMap
import models.{AmendResponseModel, ProtectionModel}
import play.api.Logging
import play.api.i18n.{I18nSupport, Lang}
import play.api.mvc._
import services.SessionCacheService
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Constants

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AmendsController @Inject() (
    val sessionCacheService: SessionCacheService,
    val plaConnector: PLAConnector,
    displayConstructors: DisplayConstructors,
    mcc: MessagesControllerComponents,
    authFunction: AuthFunction,
    manualCorrespondenceNeeded: views.html.pages.result.manualCorrespondenceNeeded,
    noNotificationId: views.html.pages.fallback.noNotificationId,
    technicalError: views.html.pages.fallback.technicalError,
    outcomeActive: views.html.pages.amends.outcomeActive,
    outcomeInactive: views.html.pages.amends.outcomeInactive,
    amendSummary: views.html.pages.amends.amendSummary
)(
    implicit val appConfig: FrontendAppConfig,
    val formWithCSRF: FormWithCSRF,
    val plaContext: PlaContext,
    val ec: ExecutionContext
) extends FrontendController(mcc)
    with I18nSupport
    with Logging {

  def amendsSummary(protectionType: String, status: String): Action[AnyContent] = Action.async { implicit request =>
    implicit val lang: Lang = mcc.messagesApi.preferred(request).lang
    authFunction.genericAuthWithNino("existingProtections") { nino =>
      val protectionKey = Strings.cacheAmendFetchString(protectionType, status)
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
          logger.warn(
            s"Could not retrieve amend protection model for user with nino $nino when loading the amend summary page"
          )
          InternalServerError(technicalError(ApplicationType.existingProtections.toString))
            .withHeaders(CACHE_CONTROL -> "no-cache")
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

          case Left(ConflictResponseError) =>
            logger.warn(s"conflict response returned for amend request for user nino $nino")
            Future.successful(
              InternalServerError(technicalError(ApplicationType.existingProtections.toString))
                .withHeaders(CACHE_CONTROL -> "no-cache")
            )

          case Left(LockedResponseError) =>
            logger.info(s"locked response returned for amend request for user nino $nino")
            Future.successful(Locked(manualCorrespondenceNeeded()))

          case Left(IncorrectResponseBodyError) =>
            logger.warn(s"Unable to create Amend Response Model from PLA response for user nino: $nino")
            Future.successful(
              InternalServerError(technicalError(ApplicationType.existingProtections.toString))
                .withHeaders(CACHE_CONTROL -> "no-cache")
            )

          case Left(_) =>
            Future.successful(
              InternalServerError(technicalError(ApplicationType.existingProtections.toString))
                .withHeaders(CACHE_CONTROL -> "no-cache")
            )
        }
      } yield result
    }
  }

  private def fetchProtectionAmendment(protectionType: String, status: String)(
      implicit request: Request[AnyContent]
  ): Future[Option[AmendProtectionModel]] =
    sessionCacheService.fetchAndGetFormData[AmendProtectionModel](Strings.cacheAmendFetchString(protectionType, status))

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
      .map(_.map(AmendResponseModel(_)))

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

//  private def routeViaMCNeededCheck(response: HttpResponse, nino: String)(
//      implicit request: Request[AnyContent]
//  ): Future[Result] =
//    response.status match {
//      case CONFLICT =>
//        logger.warn(s"conflict response returned for amend request for user nino $nino")
//        Future.successful(
//          InternalServerError(technicalError(ApplicationType.existingProtections.toString))
//            .withHeaders(CACHE_CONTROL -> "no-cache")
//        )
//      case LOCKED =>
//        logger.info(s"locked response returned for amend request for user nino $nino")
//        Future.successful(Locked(manualCorrespondenceNeeded()))
//      case _ => saveAndRedirectToDisplay(response, nino)
//    }
//
//  private def saveAndRedirectToDisplay(response: HttpResponse, nino: String)(
//      implicit request: Request[AnyContent]
//  ): Future[Result] =
//    responseConstructors
//      .createAmendResponseModelFromJson(response.json)
//      .map { model =>
//        if (model.protection.notificationId.isDefined) {
//          sessionCacheService.saveFormData[AmendResponseModel]("amendResponseModel", model).map { _ =>
//            Redirect(routes.AmendsController.amendmentOutcome)
//          }
//        } else {
//          logger.warn(s"No notification ID found in the AmendResponseModel for user with nino $nino")
//          Future.successful(InternalServerError(noNotificationId()).withHeaders(CACHE_CONTROL -> "no-cache"))
//        }
//      }
//      .getOrElse {
//        logger.warn(s"Unable to create Amend Response Model from PLA response for user nino: $nino")
//        Future.successful(
//          InternalServerError(technicalError(ApplicationType.existingProtections.toString))
//            .withHeaders(CACHE_CONTROL -> "no-cache")
//        )
//      }

  def amendmentOutcome: Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithNino("existingProtections") { nino =>
      for {
        modelAR <- sessionCacheService.fetchAndGetFormData[AmendResponseModel]("amendResponseModel")
        modelGA <- sessionCacheService.fetchAndGetFormData[AmendsGAModel]("AmendsGA")
        result  <- amendmentOutcomeResult(modelAR, modelGA, nino)
      } yield result
    }
  }

  def amendmentOutcomeResult(modelAR: Option[AmendResponseModel], modelGA: Option[AmendsGAModel], nino: String)(
      implicit request: Request[AnyContent]
  ): Future[Result] = {
    if (modelGA.isEmpty) {
      logger.warn(s"Unable to retrieve amendsGAModel from cache for user nino :$nino")
    }
    Future(
      modelAR
        .map { model =>
          val id = model.protection.notificationId.getOrElse {
            throw new Exceptions.RequiredValueNotDefinedException("amendmentOutcome", "notificationId")
          }
          if (Constants.activeAmendmentCodes.contains(id)) {
            sessionCacheService.saveFormData[ProtectionModel]("openProtection", model.protection)
            Ok(outcomeActive(displayConstructors.createActiveAmendResponseDisplayModel(model), modelGA))
          } else {
            Ok(outcomeInactive(displayConstructors.createInactiveAmendResponseDisplayModel(model), modelGA))
          }
        }
        .getOrElse {
          logger.warn(s"Unable to retrieve amendment outcome model from cache for user nino :$nino")
          InternalServerError(technicalError(ApplicationType.existingProtections.toString))
            .withHeaders(CACHE_CONTROL -> "no-cache")
        }
    )
  }

}
