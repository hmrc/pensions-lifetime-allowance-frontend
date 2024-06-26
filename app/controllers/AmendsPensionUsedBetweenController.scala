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
import connectors.PLAConnector
import constructors.{AmendsGAConstructor, DisplayConstructors, ResponseConstructors}
import enums.ApplicationType
import forms.AmendCurrentPensionForm._
import forms.AmendOverseasPensionsForm._
import forms.AmendPSODetailsForm._
import forms.AmendPensionsTakenBeforeForm._
import forms.AmendPensionsTakenBetweenForm._
import forms.AmendPensionsUsedBetweenForm._
import forms.AmendPensionsWorthBeforeForm.amendPensionsWorthBeforeForm
import forms.AmendmentTypeForm._
import models.amendModels._
import models.{AmendResponseModel, PensionDebitModel, ProtectionModel}
import play.api.Logging
import play.api.data.FormError
import play.api.i18n.{I18nSupport, Lang}
import play.api.mvc._
import services.SessionCacheService
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever
import utils.Constants
import views.html.pages

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AmendsPensionUsedBetweenController @Inject()(val sessionCacheService: SessionCacheService,
                                                   mcc: MessagesControllerComponents,
                                                   authFunction: AuthFunction,
                                                   technicalError: views.html.pages.fallback.technicalError,
                                                   amendPensionsUsedBetween: pages.amends.amendPensionsUsedBetween,
                                                   amendIP14PensionsUsedBetween: pages.amends.amendIP14PensionsUsedBetween)
                                                  (implicit val appConfig: FrontendAppConfig,
                                 implicit val partialRetriever: FormPartialRetriever,
                                 implicit val formWithCSRF: FormWithCSRF,
                                 implicit val plaContext: PlaContext,
                                 implicit val ec: ExecutionContext)
extends FrontendController(mcc) with I18nSupport with Logging{

  val submitAmendPensionsUsedBetween: Action[AnyContent] = Action.async { implicit request => authFunction.genericAuthWithNino("existingProtections") { nino =>
    amendPensionsUsedBetweenForm.bindFromRequest().fold(
      errors => {
        val form = errors.copy(errors = errors.errors.map { er => FormError(er.key, er.message) })
        Future.successful(BadRequest(amendPensionsUsedBetween(form)))
      },
      success => {
        sessionCacheService.fetchAndGetFormData[AmendProtectionModel](Strings.cacheAmendFetchString(success.protectionType, success.status)).flatMap {
          case Some(model) =>
            val updatedAmount = success.amendedPensionsUsedBetweenAmt.get.toDouble
            val updated = model.updatedProtection.copy(postADayBenefitCrystallisationEvents = Some(updatedAmount))
            val updatedTotal = updated.copy(relevantAmount = Some(Helpers.totalValue(updated)))
            val amendProtModel = AmendProtectionModel(model.originalProtection, updatedTotal)

            sessionCacheService.saveFormData[AmendProtectionModel](Strings.cacheProtectionName(updated), amendProtModel).map {
              _ => Redirect(routes.AmendsController.amendsSummary(updated.protectionType.get.toLowerCase, updated.status.get.toLowerCase))
            }

          case _ =>
            logger.warn(s"Could not retrieve amend protection model for user with nino $nino after submitting amend pensions used between")
            Future.successful(InternalServerError(technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache"))
        }
      }
    )

    }
  }

  def amendPensionsUsedBetween(protectionType: String, status: String): Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithNino("existingProtections") { nino =>
      sessionCacheService.fetchAndGetFormData[AmendProtectionModel](Strings.cacheAmendFetchString(protectionType, status)).map {
        case Some(data) =>
          protectionType match {
            case "ip2016" =>  Ok(amendPensionsUsedBetween(amendPensionsUsedBetweenForm.fill(AmendPensionsUsedBetweenModel(
              Some(Display.currencyInputDisplayFormat(data.updatedProtection.preADayPensionInPayment.getOrElse[Double](0))),
              protectionType,
              status))))
            case "ip2014" => Ok(amendIP14PensionsUsedBetween(amendPensionsUsedBetweenForm.fill(AmendPensionsUsedBetweenModel(
              Some(Display.currencyInputDisplayFormat(data.updatedProtection.preADayPensionInPayment.getOrElse[Double](0))),
              protectionType,
              status))))
          }
        case _ =>
          logger.warn(s"Could not retrieve amend protection model for user with nino $nino when loading the amend pensionsUsedBetween page")
          InternalServerError(technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
      }
    }
  }

}
