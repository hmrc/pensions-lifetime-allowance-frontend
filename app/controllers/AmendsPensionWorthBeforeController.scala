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
import forms.AmendPensionsWorthBeforeForm.amendPensionsWorthBeforeForm
import models.amend.value.AmendPensionsWorthBeforeModel
import models.pla.AmendableProtectionType
import models.pla.AmendableProtectionType.*
import models.pla.request.AmendProtectionRequestStatus
import play.api.Logging
import play.api.mvc.*
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.pages

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AmendsPensionWorthBeforeController @Inject() (
    sessionCacheService: SessionCacheService,
    mcc: MessagesControllerComponents,
    authActions: AuthActions,
    override val technicalError: views.html.pages.fallback.technicalError,
    amendIP16PensionsWorthBefore: pages.amends.amendIP16PensionsWorthBefore,
    amendIP14PensionsWorthBefore: pages.amends.amendIP14PensionsWorthBefore
)(
    using ExecutionContext
) extends FrontendController(mcc)
    with AmendControllerErrorHelper
    with Logging {

  def amendPensionsWorthBefore(
      protectionType: AmendableProtectionType,
      status: AmendProtectionRequestStatus
  ): Action[AnyContent] =
    authActions.authenticateWithNino.async { request =>
      given MessagesRequest[?] = request

      sessionCacheService
        .fetchAmendProtectionModel(protectionType, status)
        .map {
          case Some(data) =>
            val form = amendPensionsWorthBeforeForm(protectionType).fill(
              AmendPensionsWorthBeforeModel(
                Some(
                  Display.currencyInputDisplayFormat(
                    data.updated.preADayPensionInPaymentAmount.getOrElse[Double](0)
                  )
                )
              )
            )
            protectionType match {
              case IndividualProtection2016 | IndividualProtection2016LTA =>
                Ok(amendIP16PensionsWorthBefore(form, protectionType, status))
              case IndividualProtection2014 | IndividualProtection2014LTA =>
                Ok(amendIP14PensionsWorthBefore(form, protectionType, status))
            }
          case _ =>
            logger.warn(couldNotRetrieveModelForNino(request.nino, "when loading the amend pensionWorthBefore page"))
            technicalErrorResult
        }

    }

  def submitAmendPensionsWorthBefore(
      protectionType: AmendableProtectionType,
      status: AmendProtectionRequestStatus
  ): Action[AnyContent] =
    authActions.authenticateWithNino.async { request =>
      given MessagesRequest[?] = request

      amendPensionsWorthBeforeForm(protectionType)
        .bindFromRequest()
        .fold(
          errors =>
            protectionType match {
              case IndividualProtection2016 | IndividualProtection2016LTA =>
                Future.successful(BadRequest(amendIP16PensionsWorthBefore(errors, protectionType, status)))
              case IndividualProtection2014 | IndividualProtection2014LTA =>
                Future.successful(BadRequest(amendIP14PensionsWorthBefore(errors, protectionType, status)))
            },
          success =>
            sessionCacheService
              .fetchAmendProtectionModel(protectionType, status)
              .flatMap {
                case Some(model) =>
                  val updatedAmount = success.amendedPensionsTakenBeforeAmt.get.toDouble
                  val updatedModel  = model.withPreADayPensionInPaymentAmount(Some(updatedAmount))

                  sessionCacheService
                    .saveAmendProtectionModel(updatedModel)
                    .map(_ => Redirect(routes.AmendsController.amendsSummary(protectionType, status)))

                case _ =>
                  logger.warn(
                    couldNotRetrieveModelForNino(request.nino, "after submitting amend pensions worth before amount")
                  )
                  Future.successful(technicalErrorResult)
              }
        )

    }

}
