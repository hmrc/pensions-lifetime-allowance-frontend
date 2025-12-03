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
import config.FrontendAppConfig
import forms.AmendPensionsTakenBetweenForm._
import models.amend.AmendProtectionModel
import models.amend.value.AmendPensionsTakenBetweenModel
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

class AmendsPensionTakenBetweenController @Inject() (
    val sessionCacheService: SessionCacheService,
    mcc: MessagesControllerComponents,
    authFunction: AuthFunction,
    technicalError: views.html.pages.fallback.technicalError,
    amendIP16PensionsTakenBetween: pages.amends.amendIP16PensionsTakenBetween,
    amendIP14PensionsTakenBetween: pages.amends.amendIP14PensionsTakenBetween
)(
    implicit val appConfig: FrontendAppConfig,
    val formWithCSRF: FormWithCSRF,
    val ec: ExecutionContext
) extends FrontendController(mcc)
    with AmendControllerErrorHelper
    with I18nSupport
    with Logging {

  def amendPensionsTakenBetween(
      protectionType: AmendableProtectionType,
      status: AmendProtectionRequestStatus
  ): Action[AnyContent] =
    Action.async { implicit request =>
      authFunction.genericAuthWithNino { nino =>
        sessionCacheService
          .fetchAmendProtectionModel(protectionType, status)
          .map {
            case Some(data) =>
              val yesNoValue =
                if (data.updated.postADayBenefitCrystallisationEvents.getOrElse[Double](0) > 0) "yes"
                else "no"
              val form =
                amendPensionsTakenBetweenForm(protectionType)
                  .fill(AmendPensionsTakenBetweenModel(yesNoValue))
              protectionType match {
                case IndividualProtection2016 | IndividualProtection2016LTA =>
                  Ok(amendIP16PensionsTakenBetween(form, protectionType, status))
                case IndividualProtection2014 | IndividualProtection2014LTA =>
                  Ok(amendIP14PensionsTakenBetween(form, protectionType, status))
              }
            case _ =>
              logger.warn(couldNotRetrieveModelForNino(nino, "when loading amend pensionTakenBetween page"))
              buildTechnicalError(technicalError)
          }

      }
    }

  def submitAmendPensionsTakenBetween(
      protectionType: AmendableProtectionType,
      status: AmendProtectionRequestStatus
  ): Action[AnyContent] =
    Action.async { implicit request =>
      authFunction.genericAuthWithNino { nino =>
        amendPensionsTakenBetweenForm(protectionType)
          .bindFromRequest()
          .fold(
            errors =>
              protectionType match {
                case IndividualProtection2016 | IndividualProtection2016LTA =>
                  Future.successful(
                    BadRequest(amendIP16PensionsTakenBetween(errors, protectionType, status))
                  )
                case IndividualProtection2014 | IndividualProtection2014LTA =>
                  Future.successful(
                    BadRequest(amendIP14PensionsTakenBetween(errors, protectionType, status))
                  )
              },
            success =>
              sessionCacheService
                .fetchAmendProtectionModel(protectionType, status)
                .flatMap {
                  case Some(model) =>
                    success.amendedPensionsTakenBetween match {
                      case "yes" =>
                        Future.successful(
                          Redirect(
                            routes.AmendsPensionUsedBetweenController
                              .amendPensionsUsedBetween(
                                protectionType,
                                status
                              )
                          )
                        )
                      case "no" =>
                        val updatedModel = model.withPostADayBenefitCrystallisationEvents(None)

                        sessionCacheService
                          .saveAmendProtectionModel(updatedModel)
                          .map(_ => Redirect(routes.AmendsController.amendsSummary(protectionType, status)))
                    }
                  case _ =>
                    logger.warn(couldNotRetrieveModelForNino(nino, "after submitting amend pensions taken between"))
                    Future.successful(buildTechnicalError(technicalError))
                }
          )

      }
    }

}
