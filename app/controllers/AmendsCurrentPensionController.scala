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

class AmendsCurrentPensionController @Inject()(val sessionCacheService: SessionCacheService,
                                               mcc: MessagesControllerComponents,
                                               authFunction: AuthFunction,
                                               technicalError: views.html.pages.fallback.technicalError,
                                               amendCurrentPensions: pages.amends.amendCurrentPensions,
                                               amendIP14CurrentPensions: pages.amends.amendIP14CurrentPensions)
                                              (implicit val appConfig: FrontendAppConfig,
                                 implicit val partialRetriever: FormPartialRetriever,
                                 implicit val formWithCSRF: FormWithCSRF,
                                 implicit val plaContext: PlaContext,
                                 implicit val ec: ExecutionContext)
extends FrontendController(mcc) with I18nSupport with Logging{

  val submitAmendCurrentPension: Action[AnyContent] = Action.async { implicit request => authFunction.genericAuthWithNino("existingProtections") { nino =>

      amendCurrentPensionForm.bindFromRequest().fold(
        errors => {
          val form = errors.copy(errors = errors.errors.map { er => FormError(er.key, er.message) })
          Future.successful(BadRequest(amendCurrentPensions(form)))
        },
        success => {
          sessionCacheService.fetchAndGetFormData[AmendProtectionModel](Strings.cacheAmendFetchString(success.protectionType, success.status)).flatMap {
            case Some(model) =>
              val updated = model.updatedProtection.copy(uncrystallisedRights = Some(success.amendedUKPensionAmt.get.toDouble))
              val updatedTotal = updated.copy(relevantAmount = Some(Helpers.totalValue(updated)))
              val amendProtModel = AmendProtectionModel(model.originalProtection, updatedTotal)

              sessionCacheService.saveFormData[AmendProtectionModel](Strings.cacheProtectionName(updated), amendProtModel).map {
                _ => Redirect(routes.AmendsController.amendsSummary(updated.protectionType.get.toLowerCase, updated.status.get.toLowerCase))
              }

            case _ =>
              logger.warn(s"Could not retrieve amend protection model for user with nino $nino after submitting amend current UK pension")
              Future.successful(InternalServerError(technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache"))
          }
        }
      )
    }
  }

  def amendCurrentPensions(protectionType: String, status: String): Action[AnyContent] = Action.async { implicit request =>
     authFunction.genericAuthWithNino("existingProtections") { nino =>
       sessionCacheService.fetchAndGetFormData[AmendProtectionModel](Strings.cacheAmendFetchString(protectionType, status)).map {
         case Some(data) =>
           protectionType match {
             case "ip2016" =>  Ok(amendCurrentPensions(amendCurrentPensionForm.fill(AmendCurrentPensionModel(
               Some(Display.currencyInputDisplayFormat(data.updatedProtection.uncrystallisedRights.getOrElse[Double](0))), protectionType, status))))
             case "ip2014" => Ok(amendIP14CurrentPensions(amendCurrentPensionForm.fill(AmendCurrentPensionModel(
               Some(Display.currencyInputDisplayFormat(data.updatedProtection.uncrystallisedRights.getOrElse[Double](0))), protectionType, status))))
           }
         case _ =>
           logger.warn(s"Could not retrieve amend protection model for user with nino $nino when loading the amend currentPension page")
           InternalServerError(technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
       }
    }
  }

}
