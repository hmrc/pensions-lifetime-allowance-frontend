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
import enums.ApplicationType
import forms.AmendPSODetailsForm._
import models.amendModels._
import models.{PensionDebitModel}
import play.api.Logging
import play.api.i18n.{I18nSupport, Lang}
import play.api.mvc._
import services.SessionCacheService
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever
import views.html.pages

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AmendsPensionSharingOrderController @Inject()(val sessionCacheService: SessionCacheService,
                                                    mcc: MessagesControllerComponents,
                                                    authFunction: AuthFunction,
                                                    amendPsoDetails: pages.amends.amendPsoDetails,
                                                    technicalError: views.html.pages.fallback.technicalError)
                                                   (implicit val appConfig: FrontendAppConfig,
                                 implicit val partialRetriever: FormPartialRetriever,
                                 implicit val formWithCSRF: FormWithCSRF,
                                 implicit val plaContext: PlaContext,
                                 implicit val ec: ExecutionContext)
extends FrontendController(mcc) with I18nSupport with Logging{

  def submitAmendPsoDetails(protectionType: String, status: String, existingPSO: Boolean): Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithNino("existingProtections") { nino =>
      amendPsoDetailsForm(protectionType).bindFromRequest().fold(
        formWithErrors => {
          Future.successful(BadRequest(amendPsoDetails(formWithErrors, protectionType, status, existingPSO)))
        },
        success => {
            val details = createPsoDetailsList(success)
            for {
              amendModel <- sessionCacheService.fetchAndGetFormData[AmendProtectionModel](Strings.cacheAmendFetchString(protectionType, status))
              storedModel <- updateAndSaveAmendModelWithPso(details, amendModel, Strings.cacheAmendFetchString(protectionType, status))
            } yield Redirect(routes.AmendsController.amendsSummary(protectionType.toLowerCase, status.toLowerCase))
        }
      )
    }
  }

  def amendPsoDetails(protectionType: String, status: String): Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithNino("existingProtections") { nino =>
      sessionCacheService.fetchAndGetFormData[AmendProtectionModel](Strings.cacheAmendFetchString(protectionType, status)).map {
        case Some(amendProtectionModel) =>
          amendProtectionModel.updatedProtection.pensionDebits match {
            case Some(debits) =>
              routeFromPensionDebitsList(debits, protectionType, status, nino)
            case None =>
              Ok(amendPsoDetails(amendPsoDetailsForm(protectionType), protectionType, status, existingPSO = false))
          }
        case _ =>
          logger.warn(s"Could not retrieve amend protection model for user with nino $nino when loading the amend PSO details page")
          InternalServerError(technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
      }
    }
  }

  def routeFromPensionDebitsList(debits: Seq[PensionDebitModel], protectionType: String, status: String, nino: String)(implicit request: Request[AnyContent]): Result = {
    debits.length match {
        case 0 => Ok(amendPsoDetails(amendPsoDetailsForm(protectionType), protectionType, status, existingPSO = false))
        case 1 => Ok(amendPsoDetails(amendPsoDetailsForm(protectionType).fill(createAmendPsoDetailsModel(debits.head)), protectionType, status, existingPSO = true))
        case num =>
          logger.warn(s"$num pension debits recorded for user nino $nino during amend journey")
          InternalServerError(technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
      }
    }

  def createAmendPsoDetailsModel(psoDetails: PensionDebitModel): AmendPSODetailsModel = {
    val (day, month, year) = Dates.extractDMYFromAPIDateString(psoDetails.startDate)
    val date = LocalDate.of(year, month, day)
    AmendPSODetailsModel(date, Some(Display.currencyInputDisplayFormat(psoDetails.amount)))
  }

  private def updateAndSaveAmendModelWithPso(debits: Option[List[PensionDebitModel]], amendModelOption: Option[AmendProtectionModel], key: String)
                                            (implicit request: Request[AnyContent]) = {
    val amendModel = amendModelOption.getOrElse {throw new Exceptions.RequiredValueNotDefinedException("updateAndSaveAmendModelWithPso", "amendModel")}
    val newUpdatedProtection = amendModel.updatedProtection.copy(pensionDebits = debits)
    sessionCacheService.saveFormData[AmendProtectionModel](key, amendModel.copy(updatedProtection = newUpdatedProtection))
  }

  private[controllers] def createPsoDetailsList(formModel: AmendPSODetailsModel): Option[List[PensionDebitModel]] = {
    val date = formModel.pso.toString
    val amt = formModel.psoAmt.getOrElse{ throw new Exceptions.RequiredValueNotDefinedException("createPsoDetailsList", "psoAmt") }
    Some(List(PensionDebitModel(startDate = date, amount = amt.toDouble)))
  }

}
