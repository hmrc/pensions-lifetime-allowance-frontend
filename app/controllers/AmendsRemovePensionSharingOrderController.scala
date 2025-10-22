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
import models.amendModels._
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.SessionCacheService
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.pages

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AmendsRemovePensionSharingOrderController @Inject() (
    val sessionCacheService: SessionCacheService,
    val plaConnector: PLAConnector,
    mcc: MessagesControllerComponents,
    authFunction: AuthFunction,
    technicalError: views.html.pages.fallback.technicalError,
    removePsoDebits: pages.amends.removePsoDebits
)(
    implicit val appConfig: FrontendAppConfig,
    val formWithCSRF: FormWithCSRF,
    val plaContext: PlaContext,
    val ec: ExecutionContext
) extends FrontendController(mcc)
    with AmendControllerCacheHelper
    with AmendControllerErrorHelper
    with I18nSupport
    with Logging {

  def removePso(protectionType: String, status: String): Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithNino("existingProtections") { nino =>
      fetchAmendProtectionModel(protectionType, status)
        .map {
          case Some(_) =>
            Ok(removePsoDebits(protectionType, status))
          case _ =>
            logger.warn(couldNotRetrieveModelForNino(nino, "when removing the new pension debit"))
            buildTechnicalError(technicalError)
        }
    }
  }

  def submitRemovePso(protectionType: String, status: String): Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithNino("existingProtections") { nino =>
      fetchAmendProtectionModel(protectionType, status)
        .flatMap {
          case Some(model) =>
            val updated = model.updatedProtection.copy(
              pensionDebits = None,
              pensionDebitStartDate = None,
              pensionDebitEnteredAmount = None
            )
            val updatedTotal   = updated.copy(relevantAmount = Some(Helpers.totalValue(updated)))
            val amendProtModel = AmendProtectionModel(model.originalProtection, updatedTotal)

            saveAmendProtectionModel(protectionType, status, amendProtModel).map(_ => redirectToSummary(amendProtModel))

          case None =>
            logger.warn(couldNotRetrieveModelForNino(nino, "when submitting a removal of a pension debit"))
            Future.successful(
              buildTechnicalError(technicalError)
            )
        }
    }
  }

}
