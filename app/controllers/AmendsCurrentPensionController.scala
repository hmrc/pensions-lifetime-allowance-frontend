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

import auth.AuthActions
import common.*
import config.AppConfig
import forms.AmendCurrentPensionForm.*
import models.amend.value.AmendCurrentPensionModel
import models.pla.AmendableProtectionType
import models.pla.AmendableProtectionType.*
import models.pla.request.AmendProtectionRequestStatus
import play.api.Logging
import play.api.mvc.*
import services.SessionCacheService
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.pages

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AmendsCurrentPensionController @Inject() (
    val sessionCacheService: SessionCacheService,
    mcc: MessagesControllerComponents,
    authActions: AuthActions,
    technicalError: views.html.pages.fallback.technicalError,
    amendIP16CurrentPensions: pages.amends.amendIP16CurrentPensions,
    amendIP14CurrentPensions: pages.amends.amendIP14CurrentPensions
)(
    implicit val appConfig: AppConfig,
    val formWithCSRF: FormWithCSRF,
    val ec: ExecutionContext
) extends FrontendController(mcc)
    with AmendControllerErrorHelper
    with Logging {

  def amendCurrentPensions(
      protectionType: AmendableProtectionType,
      status: AmendProtectionRequestStatus
  ): Action[AnyContent] =
    authActions.authenticateWithNino.async { implicit request =>
      sessionCacheService
        .fetchAmendProtectionModel(protectionType, status)
        .map {
          case Some(amendProtectionModel) =>
            val form = amendCurrentPensionForm(protectionType).fill(
              AmendCurrentPensionModel(
                Some(
                  Display.currencyInputDisplayFormat(
                    amendProtectionModel.updated.uncrystallisedRightsAmount
                  )
                )
              )
            )
            protectionType match {
              case IndividualProtection2016 | IndividualProtection2016LTA =>
                Ok(amendIP16CurrentPensions(form, protectionType, status))
              case IndividualProtection2014 | IndividualProtection2014LTA =>
                Ok(amendIP14CurrentPensions(form, protectionType, status))
            }
          case _ =>
            logger.warn(couldNotRetrieveModelForNino(request.nino, "when loading the amend currentPension page"))
            buildTechnicalError(technicalError)
        }
    }

  def submitAmendCurrentPension(
      protectionType: AmendableProtectionType,
      status: AmendProtectionRequestStatus
  ): Action[AnyContent] =
    authActions.authenticateWithNino.async { implicit request =>
      amendCurrentPensionForm(protectionType)
        .bindFromRequest()
        .fold(
          errors =>
            protectionType match {
              case IndividualProtection2016 | IndividualProtection2016LTA =>
                Future.successful(BadRequest(amendIP16CurrentPensions(errors, protectionType, status)))
              case IndividualProtection2014 | IndividualProtection2014LTA =>
                Future.successful(BadRequest(amendIP14CurrentPensions(errors, protectionType, status)))
            },
          amendCurrentPensionsModel =>
            sessionCacheService
              .fetchAmendProtectionModel(protectionType, status)
              .flatMap {
                case Some(model) =>
                  val updatedModel =
                    model.withUncrystallisedRightsAmount(amendCurrentPensionsModel.amendedUKPensionAmt.get.toDouble)

                  sessionCacheService
                    .saveAmendProtectionModel(updatedModel)
                    .map(_ => Redirect(routes.AmendsController.amendsSummary(protectionType, status)))

                case _ =>
                  logger.warn(couldNotRetrieveModelForNino(request.nino, "after submitting amend current UK pension"))
                  Future.successful(buildTechnicalError(technicalError))
              }
        )
    }

}
