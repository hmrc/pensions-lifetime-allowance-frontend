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
import models.amend.value.RemovePensionSharingModel
import forms.RemovePensionSharingForm.removePensionSharingForm
import models.pla.AmendableProtectionType
import models.pla.AmendableProtectionType.{IndividualProtection2014, IndividualProtection2014LTA, IndividualProtection2016, IndividualProtection2016LTA}
import models.pla.request.AmendProtectionRequestStatus
import play.api.Logging
import play.api.mvc.*
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.pages

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RemovePensionSharingOrderController @Inject()(
    sessionCacheService: SessionCacheService,
    mcc: MessagesControllerComponents,
    authActions: AuthActions,
    override val technicalError: views.html.pages.fallback.technicalError,
    removePensionSharingOrder: pages.amends.removePensionSharingOrder
)(
    using ExecutionContext
) extends FrontendController(mcc)
    with AmendControllerErrorHelper
    with Logging {

  def removePso(
                 protectionType: AmendableProtectionType,
                 status: AmendProtectionRequestStatus
               ): Action[AnyContent] = authActions.authenticateWithNino.async { request =>
    given MessagesRequest[?] = request

    sessionCacheService
      .fetchAmendProtectionModel(protectionType, status)
      .map {
        case Some(_) =>
          val form = removePensionSharingForm(protectionType)
          protectionType match {
            case IndividualProtection2016 | IndividualProtection2016LTA | IndividualProtection2014 | IndividualProtection2014LTA  =>
              Ok(removePensionSharingOrder(form, protectionType, status))
            case _ =>
              logger.warn(couldNotRetrieveModelForNino(request.nino, "when removing the new pension debit"))
              technicalErrorResult
          }
      }
  }

  def submitRemovePso(
                       protectionType: AmendableProtectionType,
                       status: AmendProtectionRequestStatus
                     ): Action[AnyContent] = authActions.authenticateWithNino.async { request =>
    given MessagesRequest[?] = request

    removePensionSharingForm(protectionType)
      .bindFromRequest()
      .fold(
        errors =>
          Future.successful(
            BadRequest(
              removePensionSharingOrder(errors, protectionType, status)
            )
          ),
        success =>
          sessionCacheService
            .fetchAmendProtectionModel(protectionType, status)
            .flatMap {
              case Some(model) =>
                success.removePensionSharing match {
                  case "yes" =>
                    val updatedModel = model.withPensionDebit(None)

                    sessionCacheService
                      .saveAmendProtectionModel(updatedModel)
                      .map(_ =>
                        Redirect(
                          routes.AmendsController
                            .amendsSummary(protectionType, status)
                        )
                      )

                  case "no" =>
                    Future.successful(
                      Redirect(
                        routes.AmendsController
                          .amendsSummary(protectionType, status)
                      )
                    )
                }

              case _ =>
                logger.warn(
                  couldNotRetrieveModelForNino(
                    request.nino,
                    "when submitting a removal of a pension debit"
                  )
                )
                Future.successful(technicalErrorResult)
            }
      )
  }
}
