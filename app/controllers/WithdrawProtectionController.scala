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
import common.{Dates, Strings}
import config._
import connectors.PLAConnector
import enums.ApplicationType
import forms.WithdrawDateForm._
import models.{ProtectionModel, WithdrawDateFormModel}
import play.api.data.Form
import play.api.i18n.{I18nSupport, Lang}
import play.api.mvc._
import play.api.{Application, Logging}
import services.SessionCacheService
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WithdrawProtectionController @Inject()(sessionCacheService: SessionCacheService,
                                             plaConnector: PLAConnector,
                                             mcc: MessagesControllerComponents,
                                             authFunction: AuthFunction,
                                             withdrawConfirm: views.html.pages.withdraw.withdrawConfirm,
                                             withdrawConfirmation: views.html.pages.withdraw.withdrawConfirmation,
                                             withdrawDate: views.html.pages.withdraw.withdrawDate,
                                             withdrawImplications: views.html.pages.withdraw.withdrawImplications,
                                             technicalError: views.html.pages.fallback.technicalError
                                            )
                                            (implicit val appConfig: FrontendAppConfig,
                                             implicit val partialRetriever: FormPartialRetriever,
                                             implicit val plaContext: PlaContext,
                                             implicit val formWithCSRF: FormWithCSRF,
                                             implicit val application: Application,
                                             implicit val ec: ExecutionContext)
extends FrontendController(mcc) with I18nSupport with Logging {

  def withdrawImplications: Action[AnyContent] = Action.async {
    implicit request =>
      authFunction.genericAuthWithNino("existingProtections") { nino =>
        sessionCacheService.fetchAndGetFormData[ProtectionModel]("openProtection") map {
          case Some(currentProtection) =>
            Ok(withdrawImplications(withdrawDateForm(LocalDateTime.parse(currentProtection.certificateDate.get).toLocalDate),
              Strings.protectionTypeString(currentProtection.protectionType),
              Strings.statusString(currentProtection.status)))
          case _ =>
            logger.error(s"Could not retrieve protection data for user with nino $nino when loading the withdraw summary page")
            InternalServerError(technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
        }
      }
  }

  private[controllers] def validateAndSaveWithdrawDateForm(protection: ProtectionModel)(implicit request: Request[_]) = {
    withdrawDateForm(minDate = LocalDateTime.parse(protection.certificateDate.get).toLocalDate).bindFromRequest().fold(
      errors => {
        Future.successful(BadRequest(withdrawDate(errors, Strings.protectionTypeString(protection.protectionType), Strings.statusString(protection.status))))
      },
      form => {
        sessionCacheService.saveFormData(s"withdrawProtectionForm", form).flatMap {
          _ => Future.successful(Redirect(routes.WithdrawProtectionDateInputConfirmationController.getSubmitWithdrawDateInput))
        }
      }
    )
  }

  private[controllers] def fetchWithdrawDateForm(protection: ProtectionModel)(implicit request: Request[_], lang: Lang) = {
    sessionCacheService.fetchAndGetFormData[WithdrawDateFormModel]("withdrawProtectionForm") map  {
      case Some(form) =>
        Ok(withdrawConfirm(
          getWithdrawDateModel(form), Strings.protectionTypeString(protection.protectionType),
          Strings.statusString(protection.status)))
      case _ =>
        logger.error(s"Could not retrieve withdraw form data for user")
        InternalServerError(technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
    }
  }

  def displayWithdrawConfirmation(withdrawDate: String): Action[AnyContent] = Action.async {
    implicit request =>
      authFunction.genericAuthWithNino("existingProtections") { nino =>
        for {
          protectionAmendment <- sessionCacheService.fetchAndGetFormData[ProtectionModel]("openProtection")
          response <- plaConnector.amendProtection(nino, protectionAmendment.get.copy(withdrawnDate = Some(withdrawDate), status = Some("Withdrawn")))
          result <- routeToWithdrawConfirmation(protectionAmendment.get.protectionType, response, nino)
        } yield result
      }
  }

  private def routeToWithdrawConfirmation(protectionType: Option[String], response: HttpResponse, nino: String)
                                         (implicit request: Request[AnyContent]): Future[Result] = {
    response.status match {
      case OK => Future.successful(Redirect(routes.WithdrawProtectionController.showWithdrawConfirmation(Strings.protectionTypeString(protectionType))))
      case _ =>
        logger.error(s"conflict response returned for withdrawal request for user nino $nino")
        Future.successful(InternalServerError(
          technicalError(ApplicationType.existingProtections.toString))
          .withHeaders(CACHE_CONTROL -> "no-cache"))
    }
  }


  def showWithdrawConfirmation(protectionType: String): Action[AnyContent] = Action.async {
    implicit request =>
      authFunction.genericAuthWithoutNino("existingProtections") {
        sessionCacheService.remove.map {
          _ => Ok(withdrawConfirmation(protectionType))
        }
      }
  }


  def getWithdrawDate(form: Form[WithdrawDateFormModel]): String = {
    Dates.apiDateFormat(form.get.withdrawDate.getDayOfMonth, form.get.withdrawDate.getMonthValue, form.get.withdrawDate.getYear)
  }

  def getWithdrawDateModel(form: WithdrawDateFormModel): String = {
    Dates.apiDateFormat(form.withdrawDate.getDayOfMonth, form.withdrawDate.getMonthValue, form.withdrawDate.getYear)
  }
}
