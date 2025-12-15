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
import forms.AmendPsoDetailsForm._
import models.amend.AmendPsoDetailsModel
import models.pla.AmendableProtectionType
import models.pla.request.AmendProtectionRequestStatus
import models.{DateModel, PensionDebitModel}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.SessionCacheService
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.pages

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AmendsPensionSharingOrderController @Inject() (
    val sessionCacheService: SessionCacheService,
    mcc: MessagesControllerComponents,
    authFunction: AuthFunction,
    amendPsoDetails: pages.amends.amendPsoDetails,
    technicalError: views.html.pages.fallback.technicalError
)(
    implicit val appConfig: FrontendAppConfig,
    val formWithCSRF: FormWithCSRF,
    val ec: ExecutionContext
) extends FrontendController(mcc)
    with AmendControllerErrorHelper
    with I18nSupport
    with Logging {

  def submitAmendPsoDetails(
      protectionType: AmendableProtectionType,
      status: AmendProtectionRequestStatus,
      existingPSO: Boolean
  ): Action[AnyContent] =
    Action.async { implicit request =>
      authFunction.genericAuthWithNino { _ =>
        amendPsoDetailsForm(protectionType)
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(BadRequest(amendPsoDetails(formWithErrors, protectionType, status, existingPSO))),
            amendPsoDetailsModel =>
              for {
                amendProtectionModelOpt <- sessionCacheService.fetchAmendProtectionModel(protectionType, status)
                amendProtectionModel = amendProtectionModelOpt.getOrElse {
                  throw Exceptions.RequiredValueNotDefinedException("updateAmendModelWithPso", "amendModel")
                }
                pensionDebit = createPensionDebit(amendPsoDetailsModel)
                updatedModel = amendProtectionModel.withPensionDebit(Some(pensionDebit))

                _ <- sessionCacheService.saveAmendProtectionModel(updatedModel)
              } yield Redirect(routes.AmendsController.amendsSummary(protectionType, status))
          )
      }
    }

  private[controllers] def createPensionDebit(formModel: AmendPsoDetailsModel): PensionDebitModel = {
    val date = formModel.startDate
    val amt = formModel.enteredAmount.getOrElse {
      throw Exceptions.RequiredValueNotDefinedException("createPensionDebit", "psoAmt")
    }
    PensionDebitModel(startDate = DateModel(date), enteredAmount = amt.toDouble)
  }

  def amendPsoDetails(
      protectionType: AmendableProtectionType,
      status: AmendProtectionRequestStatus
  ): Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithNino { nino =>
      sessionCacheService
        .fetchAmendProtectionModel(protectionType, status)
        .map {
          case Some(amendProtectionModel) =>
            amendProtectionModel.updated.pensionDebit match {
              case Some(debit) =>
                Ok(
                  amendPsoDetails(
                    amendPsoDetailsForm(protectionType).fill(createAmendPsoDetailsModel(debit)),
                    protectionType,
                    status,
                    existingPso = true
                  )
                )
              case None =>
                Ok(amendPsoDetails(amendPsoDetailsForm(protectionType), protectionType, status, existingPso = false))
            }
          case _ =>
            logger.warn(couldNotRetrieveModelForNino(nino, "when loading the amend PSO details page"))
            buildTechnicalError(technicalError)
        }
    }
  }

  private def createAmendPsoDetailsModel(psoDetails: PensionDebitModel): AmendPsoDetailsModel = {
    val date = psoDetails.startDate.date
    AmendPsoDetailsModel(date, Some(Display.currencyInputDisplayFormat(psoDetails.enteredAmount)))
  }

}
