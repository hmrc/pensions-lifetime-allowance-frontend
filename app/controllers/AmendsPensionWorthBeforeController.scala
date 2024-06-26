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
import forms.AmendPensionsWorthBeforeForm.amendPensionsWorthBeforeForm
import models.amendModels._
import play.api.Logging
import play.api.data.FormError
import play.api.i18n.{I18nSupport, Lang}
import play.api.mvc._
import services.SessionCacheService
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever
import views.html.pages
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AmendsPensionWorthBeforeController @Inject()(val sessionCacheService: SessionCacheService,
                                                   mcc: MessagesControllerComponents,
                                                   authFunction: AuthFunction,
                                                   technicalError: views.html.pages.fallback.technicalError,
                                                   amendPensionsWorthBefore: pages.amends.amendPensionsWorthBefore,
                                                   amendIP14PensionsWorthBefore: pages.amends.amendIP14PensionsWorthBefore)
                                                  (implicit val appConfig: FrontendAppConfig,
                                 implicit val partialRetriever: FormPartialRetriever,
                                 implicit val formWithCSRF: FormWithCSRF,
                                 implicit val plaContext: PlaContext,
                                 implicit val ec: ExecutionContext)
extends FrontendController(mcc) with I18nSupport with Logging{

    val submitAmendPensionsWorthBefore: Action[AnyContent] = Action.async { implicit request =>
      authFunction.genericAuthWithNino("existingProtections") { nino =>
        amendPensionsWorthBeforeForm.bindFromRequest().fold(
          errors => {
            val form = errors.copy(errors = errors.errors.map { er => FormError(er.key, er.message) })
            Future.successful(BadRequest(amendPensionsWorthBefore(form)))
          },
          success => {
            sessionCacheService.fetchAndGetFormData[AmendProtectionModel](Strings.cacheAmendFetchString(success.protectionType, success.status)).flatMap {
              case Some(model) =>
                val updatedAmount = success.amendedPensionsTakenBeforeAmt.get.toDouble
                val updated = model.updatedProtection.copy(preADayPensionInPayment = Some(updatedAmount))
                val updatedTotal = updated.copy(relevantAmount = Some(Helpers.totalValue(updated)))
                val amendProtModel = AmendProtectionModel(model.originalProtection, updatedTotal)

                sessionCacheService.saveFormData[AmendProtectionModel](Strings.cacheProtectionName(updated), amendProtModel).map {
                  _ => Redirect(routes.AmendsController.amendsSummary(updated.protectionType.get.toLowerCase, updated.status.get.toLowerCase))
                }

              case _ =>
                logger.warn(s"Could not retrieve amend protection model for user with nino $nino after submitting amend pensions worth before amount")
                Future.successful(InternalServerError(technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache"))
            }
          }
        )
      }
    }

    def amendPensionsWorthBefore(protectionType: String, status: String): Action[AnyContent] = Action.async { implicit request =>
      authFunction.genericAuthWithNino("existingProtections") { nino =>
        sessionCacheService.fetchAndGetFormData[AmendProtectionModel](Strings.cacheAmendFetchString(protectionType, status)).map {
          case Some(data) =>
            protectionType match {
              case "ip2016" =>  Ok(amendPensionsWorthBefore(amendPensionsWorthBeforeForm.fill(AmendPensionsWorthBeforeModel(
                Some(Display.currencyInputDisplayFormat(data.updatedProtection.preADayPensionInPayment.getOrElse[Double](0))),
                protectionType,
                status))))
              case "ip2014" => Ok(amendIP14PensionsWorthBefore(amendPensionsWorthBeforeForm.fill(AmendPensionsWorthBeforeModel(
                Some(Display.currencyInputDisplayFormat(data.updatedProtection.preADayPensionInPayment.getOrElse[Double](0))),
                protectionType,
                status))))
            }
          case _ =>
            logger.warn(s"Could not retrieve amend protection model for user with nino $nino when loading the amend pensionWorthBefore page")
            InternalServerError(technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
        }
      }
    }
}
