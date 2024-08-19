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
import enums.ApplicationType
import forms.WithdrawDateForm._
import models.{ProtectionModel, WithdrawDateFormModel}
import play.api.data.{Form, FormError}
import play.api.i18n.{I18nSupport, Lang, Messages}
import play.api.mvc._
import play.api.{Application, Logging}
import services.SessionCacheService
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import java.time.{LocalDate, LocalDateTime}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WithdrawProtectionDateInputConfirmationController @Inject()(sessionCacheService: SessionCacheService,
                                                                  mcc: MessagesControllerComponents,
                                                                  authFunction: AuthFunction,
                                                                  withdrawConfirm: views.html.pages.withdraw.withdrawConfirm,
                                                                  withdrawDate: views.html.pages.withdraw.withdrawDate,
                                                                  technicalError: views.html.pages.fallback.technicalError
                                            )(implicit val appConfig: FrontendAppConfig,
                                              implicit val plaContext: PlaContext,
                                              implicit val formWithCSRF: FormWithCSRF,
                                              implicit val application: Application,
                                              implicit val ec: ExecutionContext)
extends FrontendController(mcc) with I18nSupport with Logging {

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

  def getSubmitWithdrawDateInput: Action[AnyContent] = Action.async {
    implicit request =>
      implicit val lang: Lang = mcc.messagesApi.preferred(request).lang
      authFunction.genericAuthWithNino("existingProtections") { nino =>
        sessionCacheService.fetchAndGetFormData[ProtectionModel]("openProtection") flatMap  {
          case Some(protection) =>
            fetchWithdrawDateForm(protection)
          case _ =>
            logger.error(s"Could not retrieve protection data for user with nino $nino when loading the withdraw date input page")
            Future.successful(InternalServerError(technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache"))
        }
      }
  }

  def submitWithdrawDateInput: Action[AnyContent] = Action.async {
    implicit request =>
      implicit val lang: Lang = mcc.messagesApi.preferred(request).lang
      authFunction.genericAuthWithNino("existingProtections") { nino =>
        sessionCacheService.fetchAndGetFormData[ProtectionModel]("openProtection") map {
          case Some(protection) =>
            val protectionStartDate = LocalDateTime.parse(protection.certificateDate.get).toLocalDate
            withdrawDateForm(protectionStartDate).bindFromRequest().fold(
              formWithErrors =>
                BadRequest(withdrawDate(buildInvalidForm(formWithErrors),
                  Strings.protectionTypeString(protection.protectionType),
                  Strings.statusString(protection.status))),
              _ => Ok(withdrawConfirm(
                getWithdrawDate(withdrawDateForm(LocalDateTime.parse(protection.certificateDate.get).toLocalDate).bindFromRequest()),
                Strings.protectionTypeString(protection.protectionType),
                Strings.statusString(protection.status)))
            )
          case _ =>
            logger.error(s"Could not retrieve protection data for user with nino $nino when loading the withdraw date input page")
            InternalServerError(technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
        }
      }
  }

  private def buildInvalidForm(errorForm: Form[WithdrawDateFormModel])(implicit messages: Messages): Form[WithdrawDateFormModel] = {
    if (errorForm.errors.size > 1) {
      val formFields: Seq[String] = errorForm.errors map (field => field.key)
      val formFieldsWithErrors: Seq[FormError] = formFields map (key => FormError(key, "withdrawDate.day"))
      val finalErrors: Seq[FormError] = formFieldsWithErrors ++ Seq(FormError("withdrawDate.day", Messages("pla.withdraw.date-input-form.date-invalid")))
      withdrawDateForm(LocalDate.now()).copy(errors = finalErrors, data = errorForm.data)
    }
    else errorForm
  }

  def getWithdrawDate(form: Form[WithdrawDateFormModel]): String = {
    Dates.apiDateFormat(form.get.withdrawDate.getDayOfMonth, form.get.withdrawDate.getMonthValue, form.get.withdrawDate.getYear)
  }

  def getWithdrawDateModel(form: WithdrawDateFormModel): String = {
    Dates.apiDateFormat(form.withdrawDate.getDayOfMonth, form.withdrawDate.getMonthValue, form.withdrawDate.getYear)
  }
}
