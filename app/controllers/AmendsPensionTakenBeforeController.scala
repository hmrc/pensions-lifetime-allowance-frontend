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
import config.FrontendAppConfig
import forms.AmendPensionsTakenBeforeForm._
import models.amend.value.AmendPensionsTakenBeforeModel
import models.pla.AmendableProtectionType
import models.pla.AmendableProtectionType._
import models.pla.request.AmendProtectionRequestStatus
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
    amendIP16PensionsTakenBefore: pages.amends.amendIP16PensionsTakenBefore,
    amendIP14PensionsTakenBefore: pages.amends.amendIP14PensionsTakenBefore
)(
    implicit val appConfig: FrontendAppConfig,
    val formWithCSRF: FormWithCSRF,
    val ec: ExecutionContext
) extends FrontendController(mcc)
    with AmendControllerErrorHelper
    with I18nSupport
    with Logging {

  def amendPensionsTakenBefore(
      protectionType: AmendableProtectionType,
      status: AmendProtectionRequestStatus
  ): Action[AnyContent] =
    Action.async { implicit request =>
      authFunction.genericAuthWithNino { nino =>
        sessionCacheService
          .fetchAmendProtectionModel(protectionType, status)
          .map {
            case Some(data) =>
              val yesNoValue = if (data.updated.preADayPensionInPayment.getOrElse[Double](0) > 0) "yes" else "no"
              val form = amendPensionsTakenBeforeForm(protectionType)
                .fill(AmendPensionsTakenBeforeModel(yesNoValue))
              protectionType match {
                case IndividualProtection2016 | IndividualProtection2016LTA =>
                  Ok(amendIP16PensionsTakenBefore(form, protectionType, status))
                case IndividualProtection2014 | IndividualProtection2014LTA =>
                  Ok(amendIP14PensionsTakenBefore(form, protectionType, status))
              }
            case _ =>
              logger.warn(couldNotRetrieveModelForNino(nino, "when loading the amend pensionTakenBefore page"))
              buildTechnicalError(technicalError)
          }
      }
    }

  def submitAmendPensionsTakenBefore(
      protectionType: AmendableProtectionType,
      status: AmendProtectionRequestStatus
  ): Action[AnyContent] =
    Action.async { implicit request =>
      authFunction.genericAuthWithNino { nino =>
        amendPensionsTakenBeforeForm(protectionType)
          .bindFromRequest()
          .fold(
            errors =>
              protectionType match {
                case IndividualProtection2016 | IndividualProtection2016LTA =>
                  Future.successful(BadRequest(amendIP16PensionsTakenBefore(errors, protectionType, status)))
                case IndividualProtection2014 | IndividualProtection2014LTA =>
                  Future.successful(BadRequest(amendIP14PensionsTakenBefore(errors, protectionType, status)))
              },
            success =>
              sessionCacheService
                .fetchAmendProtectionModel(protectionType, status)
                .flatMap {
                  case Some(model) =>
                    success.amendedPensionsTakenBefore match {
                      case "yes" =>
                        Future.successful(
                          Redirect(
                            routes.AmendsPensionWorthBeforeController
                              .amendPensionsWorthBefore(
                                protectionType,
                                status
                              )
                          )
                        )
                      case "no" =>
                        val updatedModel = model.withPreADayPensionInPayment(None)
                        sessionCacheService
                          .saveAmendProtectionModel(updatedModel)
                          .map(_ => Redirect(routes.AmendsController.amendsSummary(protectionType, status)))
                    }
                  case _ =>
                    logger.warn(couldNotRetrieveModelForNino(nino, "after submitting amend pensions taken before"))
                    Future.successful(buildTechnicalError(technicalError))
                }
          )
      }
    }

}
