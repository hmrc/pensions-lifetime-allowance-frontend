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
import forms.AmendPensionsTakenBeforeForm._
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
    with AmendControllerErrorHelper
    with I18nSupport
    with Logging {

  def amendPensionsTakenBefore(protectionTypeString: String, status: String): Action[AnyContent] =
    Action.async { implicit request =>
      authFunction.genericAuthWithNino("existingProtections") { nino =>
        AmendProtectionLifetimeAllowanceType
          .tryFrom(protectionTypeString)
          .map { protectionType =>
            fetchAmendProtectionModel(protectionType.toString, status)
              .map {
                case Some(data) =>
                  val yesNoValue =
                    if (data.updatedProtection.preADayPensionInPayment.getOrElse[Double](0) > 0) "yes" else "no"
                  protectionType match {
                    case IndividualProtection2016 | IndividualProtection2016LTA =>
                      Ok(
                        amendPensionsTakenBefore(
                          amendPensionsTakenBeforeForm(protectionType.toString)
                            .fill(AmendPensionsTakenBeforeModel(yesNoValue)),
                          protectionType.toString,
                          status
                        )
                      )
                    case IndividualProtection2014 | IndividualProtection2014LTA =>
                      Ok(
                        amendIP14PensionsTakenBefore(
                          amendPensionsTakenBeforeForm(protectionType.toString)
                            .fill(AmendPensionsTakenBeforeModel(yesNoValue)),
                          protectionType.toString,
                          status
                        )
                      )
                  }
                case _ =>
                  logger.warn(couldNotRetrieveModelForNino(nino, "when loading the amend pensionTakenBefore page"))
                  buildTechnicalError(technicalError)
              }
          }
          .getOrElse {
            logger.warn(unknownProtectionType(protectionTypeString, "when loading the amend pensionTakenBefore page"))
            Future.successful(buildTechnicalError(technicalError))
          }
      }
    }

  def submitAmendPensionsTakenBefore(protectionTypeString: String, status: String): Action[AnyContent] =
    Action.async { implicit request =>
      authFunction.genericAuthWithNino("existingProtections") { nino =>
        AmendProtectionLifetimeAllowanceType
          .tryFrom(protectionTypeString)
          .map { protectionType =>
            amendPensionsTakenBeforeForm(protectionType.toString)
              .bindFromRequest()
              .fold(
                errors =>
                  protectionType match {
                    case IndividualProtection2016 | IndividualProtection2016LTA =>
                      Future
                        .successful(BadRequest(amendPensionsTakenBefore(errors, protectionType.toString, status)))
                    case IndividualProtection2014 | IndividualProtection2014LTA =>
                      Future.successful(
                        BadRequest(amendIP14PensionsTakenBefore(errors, protectionType.toString, status))
                      )
                  },
                success =>
                  fetchAmendProtectionModel(protectionType.toString, status)
                    .flatMap {
                      case Some(model) =>
                        success.amendedPensionsTakenBefore match {
                          case "yes" =>
                            Future.successful(
                              Redirect(
                                routes.AmendsPensionWorthBeforeController
                                  .amendPensionsWorthBefore(
                                    Strings.protectionTypeUrlString(Some(protectionType.toString)),
                                    status.toLowerCase
                                  )
                              )
                            )
                          case "no" =>
                            val updated        = model.updatedProtection.copy(preADayPensionInPayment = Some(0))
                            val updatedTotal   = updated.copy(relevantAmount = Some(Helpers.totalValue(updated)))
                            val amendProtModel = AmendProtectionModel(model.originalProtection, updatedTotal)

                            saveAmendProtectionModel(protectionType.toString, status, amendProtModel)
                              .map(_ => redirectToSummary(amendProtModel))
                        }
                      case _ =>
                        logger.warn(couldNotRetrieveModelForNino(nino, "after submitting amend pensions taken before"))
                        Future.successful(buildTechnicalError(technicalError))
                    }
              )
          }
          .getOrElse {
            logger.warn(unknownProtectionType(protectionTypeString, "after submitting amend pensions taken before"))
            Future.successful(buildTechnicalError(technicalError))
          }
      }
    }

}
