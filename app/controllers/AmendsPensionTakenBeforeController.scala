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
import forms.AmendPensionsTakenBeforeForm._
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

class AmendsPensionTakenBeforeController @Inject() (
    val sessionCacheService: SessionCacheService,
    mcc: MessagesControllerComponents,
    authFunction: AuthFunction,
    technicalError: views.html.pages.fallback.technicalError,
    amendPensionsTakenBefore: pages.amends.amendPensionsTakenBefore,
    amendIP14PensionsTakenBefore: pages.amends.amendIP14PensionsTakenBefore
)(
    implicit val appConfig: FrontendAppConfig,
    val formWithCSRF: FormWithCSRF,
    val plaContext: PlaContext,
    val ec: ExecutionContext
) extends FrontendController(mcc)
    with AmendControllerCacheHelper
    with I18nSupport
    with Logging {

  def amendPensionsTakenBefore(protectionType: String, status: String): Action[AnyContent] =
    Action.async { implicit request =>
      authFunction.genericAuthWithNino("existingProtections") { nino =>
        sessionCacheService
          .fetchAndGetFormData[AmendProtectionModel](Strings.cacheAmendFetchString(protectionType, status))
          .map {
            case Some(data) =>
              val yesNoValue =
                if (data.updatedProtection.preADayPensionInPayment.getOrElse[Double](0) > 0) "yes" else "no"
              protectionType match {
                case "ip2016" =>
                  Ok(
                    amendPensionsTakenBefore(
                      amendPensionsTakenBeforeForm(protectionType).fill(AmendPensionsTakenBeforeModel(yesNoValue)),
                      protectionType,
                      status
                    )
                  )
                case "ip2014" =>
                  Ok(
                    amendIP14PensionsTakenBefore(
                      amendPensionsTakenBeforeForm(protectionType).fill(AmendPensionsTakenBeforeModel(yesNoValue)),
                      protectionType,
                      status
                    )
                  )
              }
            case _ =>
              logger.warn(
                s"Could not retrieve amend protection model for user with nino $nino when loading the amend pensionTakenBefore page"
              )
              InternalServerError(technicalError(ApplicationType.existingProtections.toString))
                .withHeaders(CACHE_CONTROL -> "no-cache")
          }
      }
    }

  def submitAmendPensionsTakenBefore(protectionType: String, status: String): Action[AnyContent] =
    Action.async { implicit request =>
      authFunction.genericAuthWithNino("existingProtections") { nino =>
        amendPensionsTakenBeforeForm(protectionType)
          .bindFromRequest()
          .fold(
            errors =>
              protectionType match {
                case "ip2016" => Future.successful(BadRequest(amendPensionsTakenBefore(errors, protectionType, status)))
                case "ip2014" =>
                  Future.successful(BadRequest(amendIP14PensionsTakenBefore(errors, protectionType, status)))
              },
            success => {
              val cacheKey = Strings.cacheAmendFetchString(protectionType, status)
              fetchAmendProtectionModel(cacheKey)
                .flatMap {
                  case Some(model) =>
                    success.amendedPensionsTakenBefore match {
                      case "yes" =>
                        Future.successful(
                          Redirect(
                            routes.AmendsPensionWorthBeforeController
                              .amendPensionsWorthBefore(protectionType.toLowerCase, status.toLowerCase)
                          )
                        )
                      case "no" =>
                        val updated        = model.updatedProtection.copy(preADayPensionInPayment = Some(0))
                        val updatedTotal   = updated.copy(relevantAmount = Some(Helpers.totalValue(updated)))
                        val amendProtModel = AmendProtectionModel(model.originalProtection, updatedTotal)

                        saveAndRedirectToSummary(cacheKey, amendProtModel)
                    }
                  case _ =>
                    logger.warn(
                      s"Could not retrieve amend protection model for user with nino $nino after submitting amend pensions taken before"
                    )
                    Future.successful(
                      InternalServerError(technicalError(ApplicationType.existingProtections.toString))
                        .withHeaders(CACHE_CONTROL -> "no-cache")
                    )
                }
            }
          )
      }
    }

}
