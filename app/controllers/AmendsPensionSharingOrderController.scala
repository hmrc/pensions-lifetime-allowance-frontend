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
import forms.AmendPSODetailsForm._
import models.PensionDebitModel
import models.amendModels._
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.SessionCacheService
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.pages

import java.time.LocalDate
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
    with AmendControllerCacheHelper
    with AmendControllerErrorHelper
    with I18nSupport
    with Logging {

  def submitAmendPsoDetails(protectionType: String, status: String, existingPSO: Boolean): Action[AnyContent] =
    Action.async { implicit request =>
      authFunction.genericAuthWithNino("existingProtections") { _ =>
        amendPsoDetailsForm(protectionType)
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(BadRequest(amendPsoDetails(formWithErrors, protectionType, status, existingPSO))),
            amendPsoDetailsModel =>
              for {
                currentProtectionModel <- fetchAmendProtectionModel(protectionType, status)
                pensionDebitModel = createPensionDebitModel(amendPsoDetailsModel)
                updatedModel      = updateAmendModelWithPso(currentProtectionModel, pensionDebitModel)

                _ <- saveAmendProtectionModel(protectionType, status, updatedModel)
              } yield Redirect(routes.AmendsController.amendsSummary(protectionType.toLowerCase, status.toLowerCase))
          )
      }
    }

  private[controllers] def createPensionDebitModel(formModel: AmendPSODetailsModel): PensionDebitModel = {
    val date = formModel.pso.toString
    val amt = formModel.psoAmt.getOrElse {
      throw Exceptions.RequiredValueNotDefinedException("createPensionDebitModel", "psoAmt")
    }
    PensionDebitModel(startDate = date, amount = amt.toDouble)
  }

  private def updateAmendModelWithPso(
      amendModelOption: Option[AmendProtectionModel],
      pensionDebitModel: PensionDebitModel
  ): AmendProtectionModel = {
    val amendModel = amendModelOption.getOrElse {
      throw Exceptions.RequiredValueNotDefinedException("updateAmendModelWithPso", "amendModel")
    }
    val newUpdatedProtection = amendModel.updatedProtection.copy(
      pensionDebits = Some(List(pensionDebitModel)),
      pensionDebitStartDate = Some(pensionDebitModel.startDate),
      pensionDebitEnteredAmount = Some(pensionDebitModel.amount)
    )

    amendModel.copy(updatedProtection = newUpdatedProtection)
  }

  def amendPsoDetails(protectionType: String, status: String): Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithNino("existingProtections") { nino =>
      fetchAmendProtectionModel(protectionType, status)
        .map {
          case Some(amendProtectionModel) =>
            amendProtectionModel.updatedProtection.pensionDebits match {
              case Some(debits) =>
                routeFromPensionDebitsList(debits, protectionType, status, nino)
              case None =>
                Ok(amendPsoDetails(amendPsoDetailsForm(protectionType), protectionType, status, existingPSO = false))
            }
          case _ =>
            logger.warn(couldNotRetrieveModelForNino(nino, "when loading the amend PSO details page"))
            buildTechnicalError(technicalError)
        }
    }
  }

  private def routeFromPensionDebitsList(
      debits: Seq[PensionDebitModel],
      protectionType: String,
      status: String,
      nino: String
  )(
      implicit request: Request[AnyContent]
  ): Result =
    debits.length match {
      case 0 => Ok(amendPsoDetails(amendPsoDetailsForm(protectionType), protectionType, status, existingPSO = false))
      case 1 =>
        Ok(
          amendPsoDetails(
            amendPsoDetailsForm(protectionType).fill(createAmendPsoDetailsModel(debits.head)),
            protectionType,
            status,
            existingPSO = true
          )
        )
      case num =>
        logger.warn(s"$num pension debits recorded for user nino $nino during amend journey")
        buildTechnicalError(technicalError)
    }

  private def createAmendPsoDetailsModel(psoDetails: PensionDebitModel): AmendPSODetailsModel = {
    val (day, month, year) = Dates.extractDMYFromAPIDateString(psoDetails.startDate)
    val date               = LocalDate.of(year, month, day)
    AmendPSODetailsModel(date, Some(Display.currencyInputDisplayFormat(psoDetails.amount)))
  }

}
