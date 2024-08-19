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
import common.Strings
import config._
import enums.ApplicationType
import forms.WithdrawDateForm._
import models.ProtectionModel
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.{Application, Logging}
import services.SessionCacheService
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WithdrawProtectionDateInputController @Inject()(sessionCacheService: SessionCacheService,
                                                      mcc: MessagesControllerComponents,
                                                      authFunction: AuthFunction,
                                                      withdrawDate: views.html.pages.withdraw.withdrawDate,
                                                      technicalError: views.html.pages.fallback.technicalError
                                            )(implicit val appConfig: FrontendAppConfig,
                                              implicit val plaContext: PlaContext,
                                              implicit val formWithCSRF: FormWithCSRF,
                                              implicit val application: Application,
                                              implicit val ec: ExecutionContext)
extends FrontendController(mcc) with I18nSupport with Logging {

  def getWithdrawDateInput: Action[AnyContent] = Action.async {
    implicit request =>
      authFunction.genericAuthWithNino("existingProtections") { nino =>
        sessionCacheService.fetchAndGetFormData[ProtectionModel]("openProtection") map {
          case Some(currentProtection) =>
            val protectionStartDate = LocalDateTime.parse(currentProtection.certificateDate.get).toLocalDate
            Ok(withdrawDate(withdrawDateForm(protectionStartDate),
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

  def postWithdrawDateInput: Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithoutNino("existingProtections") {
      sessionCacheService.fetchAndGetFormData[ProtectionModel]("openProtection") flatMap {
        case Some(protection) => validateAndSaveWithdrawDateForm(protection)
        case _ =>
          logger.error(s"Could not retrieve protection data for user when loading the withdraw date input page")
          Future.successful(InternalServerError(technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache"))
      }
    }
  }

}
