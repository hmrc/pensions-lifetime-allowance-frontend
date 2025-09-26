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
import forms.AmendPensionsUsedBetweenForm._
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

class AmendsPensionUsedBetweenController @Inject() (
    val sessionCacheService: SessionCacheService,
    mcc: MessagesControllerComponents,
    authFunction: AuthFunction,
    technicalError: views.html.pages.fallback.technicalError,
    amendPensionsUsedBetween: pages.amends.amendPensionsUsedBetween,
    amendIP14PensionsUsedBetween: pages.amends.amendIP14PensionsUsedBetween
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

  def amendPensionsUsedBetween(protectionTypeString: String, status: String): Action[AnyContent] =
    Action.async { implicit request =>
      authFunction.genericAuthWithNino("existingProtections") { nino =>
        AmendProtectionLifetimeAllowanceType
          .tryFrom(protectionTypeString)
          .map { protectionType =>
            fetchAmendProtectionModel(protectionType.toString, status)
              .map {
                case Some(data) =>
                  protectionType match {
                    case IndividualProtection2016 | IndividualProtection2016LTA =>
                      Ok(
                        amendPensionsUsedBetween(
                          amendPensionsUsedBetweenForm(protectionType.toString).fill(
                            AmendPensionsUsedBetweenModel(
                              Some(
                                Display.currencyInputDisplayFormat(
                                  data.updatedProtection.postADayBenefitCrystallisationEvents.getOrElse[Double](0)
                                )
                              )
                            )
                          ),
                          protectionType.toString,
                          status
                        )
                      )
                    case IndividualProtection2014 | IndividualProtection2014LTA =>
                      Ok(
                        amendIP14PensionsUsedBetween(
                          amendPensionsUsedBetweenForm(protectionType.toString).fill(
                            AmendPensionsUsedBetweenModel(
                              Some(
                                Display.currencyInputDisplayFormat(
                                  data.updatedProtection.postADayBenefitCrystallisationEvents.getOrElse[Double](0)
                                )
                              )
                            )
                          ),
                          protectionType.toString,
                          status
                        )
                      )
                  }
                case _ =>
                  logger.warn(couldNotRetrieveModelForNino(nino, "when loading the amend pensionsUsedBetween page"))
                  buildTechnicalError(technicalError)
              }
          }
          .getOrElse {
            logger.warn(unknownProtectionType(protectionTypeString, "when loading the amend pensionsUsedBetween page"))
            Future.successful(buildTechnicalError(technicalError))
          }
      }
    }

  def submitAmendPensionsUsedBetween(protectionTypeString: String, status: String): Action[AnyContent] =
    Action.async { implicit request =>
      authFunction.genericAuthWithNino("existingProtections") { nino =>
        AmendProtectionLifetimeAllowanceType
          .tryFrom(protectionTypeString)
          .map { protectionType =>
            amendPensionsUsedBetweenForm(protectionType.toString)
              .bindFromRequest()
              .fold(
                errors =>
                  protectionType match {
                    case IndividualProtection2016 | IndividualProtection2016LTA =>
                      Future
                        .successful(BadRequest(amendPensionsUsedBetween(errors, protectionType.toString, status)))
                    case IndividualProtection2014 | IndividualProtection2014LTA =>
                      Future.successful(
                        BadRequest(amendIP14PensionsUsedBetween(errors, protectionType.toString, status))
                      )
                  },
                success =>
                  fetchAmendProtectionModel(protectionType.toString, status)
                    .flatMap {
                      case Some(model) =>
                        val updatedAmount = success.amendedPensionsUsedBetweenAmt.get.toDouble
                        val updated =
                          model.updatedProtection.copy(postADayBenefitCrystallisationEvents = Some(updatedAmount))
                        val updatedTotal   = updated.copy(relevantAmount = Some(Helpers.totalValue(updated)))
                        val amendProtModel = AmendProtectionModel(model.originalProtection, updatedTotal)

                        saveAmendProtectionModel(protectionType.toString, status, amendProtModel)
                          .map(_ => redirectToSummary(amendProtModel))

                      case _ =>
                        logger.warn(couldNotRetrieveModelForNino(nino, "after submitting amend pensions used between"))
                        Future.successful(buildTechnicalError(technicalError))
                    }
              )
          }
          .getOrElse {
            logger.warn(unknownProtectionType(protectionTypeString, "after submitting amend pensions used between"))
            Future.successful(buildTechnicalError(technicalError))
          }

      }
    }

}
