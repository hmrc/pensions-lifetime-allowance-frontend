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
import forms.AmendCurrentPensionForm._
import models.amendModels._
import models.pla.AmendProtectionLifetimeAllowanceType
import models.pla.AmendProtectionLifetimeAllowanceType._
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.SessionCacheService
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.pages

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AmendsCurrentPensionController @Inject() (
    val sessionCacheService: SessionCacheService,
    mcc: MessagesControllerComponents,
    authFunction: AuthFunction,
    technicalError: views.html.pages.fallback.technicalError,
    amendCurrentPensions: pages.amends.amendCurrentPensions,
    amendIP14CurrentPensions: pages.amends.amendIP14CurrentPensions
)(
    implicit val appConfig: FrontendAppConfig,
    val formWithCSRF: FormWithCSRF,
    val plaContext: PlaContext,
    val ec: ExecutionContext
) extends FrontendController(mcc)
    with AmendControllerCacheHelper
    with I18nSupport
    with Logging {

  def amendCurrentPensions(protectionType: String, status: String): Action[AnyContent] =
    Action.async { implicit request =>
      authFunction.genericAuthWithNino("existingProtections") { nino =>
        fetchAmendProtectionModel(protectionType, status)
          .map {
            case Some(data) =>
              AmendProtectionLifetimeAllowanceType.tryFrom(protectionType) match {
                case Some(IndividualProtection2016) =>
                  Ok(
                    amendCurrentPensions(
                      amendCurrentPensionForm(IndividualProtection2016.toString).fill(
                        AmendCurrentPensionModel(
                          Some(
                            Display.currencyInputDisplayFormat(
                              data.updatedProtection.uncrystallisedRights.getOrElse[Double](0)
                            )
                          )
                        )
                      ),
                      IndividualProtection2016.toString,
                      status
                    )
                  )
                case Some(IndividualProtection2014) =>
                  Ok(
                    amendIP14CurrentPensions(
                      amendCurrentPensionForm(IndividualProtection2014.toString).fill(
                        AmendCurrentPensionModel(
                          Some(
                            Display.currencyInputDisplayFormat(
                              data.updatedProtection.uncrystallisedRights.getOrElse[Double](0)
                            )
                          )
                        )
                      ),
                      IndividualProtection2014.toString,
                      status
                    )
                  )
              }
            case _ =>
              logger.warn(
                s"Could not retrieve amend protection model for user with nino $nino when loading the amend currentPension page"
              )
              InternalServerError(technicalError(ApplicationType.existingProtections.toString))
                .withHeaders(CACHE_CONTROL -> "no-cache")
          }
      }
    }

  def submitAmendCurrentPension(protectionType: String, status: String): Action[AnyContent] =
    Action.async { implicit request =>
      authFunction.genericAuthWithNino("existingProtections") { nino =>
        amendCurrentPensionForm(protectionType)
          .bindFromRequest()
          .fold(
            errors =>
              AmendProtectionLifetimeAllowanceType.tryFrom(protectionType) match {
                case Some(IndividualProtection2016) =>
                  Future.successful(BadRequest(amendCurrentPensions(errors, IndividualProtection2016.toString, status)))
                case Some(IndividualProtection2014) =>
                  Future
                    .successful(BadRequest(amendIP14CurrentPensions(errors, IndividualProtection2014.toString, status)))
              },
            success =>
              fetchAmendProtectionModel(protectionType, status)
                .flatMap {
                  case Some(model) =>
                    val updated = model.updatedProtection
                      .copy(uncrystallisedRights = Some(success.amendedUKPensionAmt.get.toDouble))
                    val updatedTotal   = updated.copy(relevantAmount = Some(Helpers.totalValue(updated)))
                    val amendProtModel = AmendProtectionModel(model.originalProtection, updatedTotal)

                    saveAmendProtectionModel(protectionType, status, amendProtModel)
                      .map(_ => redirectToSummary(amendProtModel))

                  case _ =>
                    logger.warn(
                      s"Could not retrieve amend protection model for user with nino $nino after submitting amend current UK pension"
                    )
                    Future.successful(
                      InternalServerError(technicalError(ApplicationType.existingProtections.toString))
                        .withHeaders(CACHE_CONTROL -> "no-cache")
                    )
                }
          )
      }
    }

}
