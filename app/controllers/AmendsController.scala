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
import connectors.PLAConnector
import constructors.{AmendsGAConstructor, DisplayConstructors, ResponseConstructors}
import enums.ApplicationType
import models.amendModels._
import models.{AmendResponseModel, PensionDebitModel, ProtectionModel}
import play.api.Logging
import play.api.i18n.{I18nSupport, Lang}
import play.api.mvc._
import services.SessionCacheService
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Constants

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AmendsController @Inject() (
    val sessionCacheService: SessionCacheService,
    val plaConnector: PLAConnector,
    displayConstructors: DisplayConstructors,
    mcc: MessagesControllerComponents,
    responseConstructors: ResponseConstructors,
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
        protectionAmendment <- sessionCacheService.fetchAndGetFormData[AmendProtectionModel](
          Strings.cacheAmendFetchString(protectionType, status)
        )
        saveAmendsGA <- sessionCacheService.saveFormData[AmendsGAModel](
          "AmendsGA",
          AmendsGAConstructor.identifyAmendsChanges(
            protectionAmendment.get.updatedProtection,
            protectionAmendment.get.originalProtection
          )
        )
        response <- plaConnector.amendProtection(nino, protectionAmendment.get.updatedProtection)
        result   <- routeViaMCNeededCheck(response, nino)
      } yield result
    }
  }

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

  private def routeViaMCNeededCheck(response: HttpResponse, nino: String)(
      implicit request: Request[AnyContent]
  ): Future[Result] =
    response.status match {
      case 409 =>
        logger.warn(s"conflict response returned for amend request for user nino $nino")
        Future.successful(
          InternalServerError(technicalError(ApplicationType.existingProtections.toString))
            .withHeaders(CACHE_CONTROL -> "no-cache")
        )
      case 423 =>
        logger.info(s"locked reponse returned for amend request for user nino $nino")
        Future.successful(Locked(manualCorrespondenceNeeded()))
      case _ => saveAndRedirectToDisplay(response, nino)
    }

  def saveAndRedirectToDisplay(response: HttpResponse, nino: String)(
      implicit request: Request[AnyContent]
  ): Future[Result] =
    responseConstructors
      .createAmendResponseModelFromJson(response.json)
      .map { model =>
        if (model.protection.notificationId.isDefined) {
          sessionCacheService.saveFormData[AmendResponseModel]("amendResponseModel", model).map { cacheMap =>
            Redirect(routes.AmendsController.amendmentOutcome)
          }
        } else {
          logger.warn(s"No notification ID found in the AmendResponseModel for user with nino $nino")
          Future.successful(InternalServerError(noNotificationId()).withHeaders(CACHE_CONTROL -> "no-cache"))
        }
      }
      .getOrElse {
        logger.warn(s"Unable to create Amend Response Model from PLA response for user nino: $nino")
        Future.successful(
          InternalServerError(technicalError(ApplicationType.existingProtections.toString))
            .withHeaders(CACHE_CONTROL -> "no-cache")
        )
      }

  private[controllers] def createPsoDetailsList(formModel: AmendPSODetailsModel): Option[List[PensionDebitModel]] = {
    val date = formModel.pso.toString
    val amt = formModel.psoAmt.getOrElse {
      throw new Exceptions.RequiredValueNotDefinedException("createPsoDetailsList", "psoAmt")
    }
    Some(List(PensionDebitModel(startDate = date, amount = amt.toDouble)))
  }

}
