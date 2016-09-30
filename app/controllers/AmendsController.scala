/*
 * Copyright 2016 HM Revenue & Customs
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

import auth.AuthorisedForPLA
import common.{Helpers, Strings}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.KeyStoreConnector
import constructors.DisplayConstructors
import enums.ApplicationType
import forms.AmendCurrentPensionForm._
import forms.AmendPensionsTakenBeforeForm
import forms.AmendPensionsTakenBeforeForm._
import models.amendModels.{AmendCurrentPensionModel, AmendPensionsTakenBeforeModel, AmendProtectionModel}
import play.api.Logger
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.pages

import scala.concurrent.Future

object AmendsController extends AmendsController{
  val keyStoreConnector = KeyStoreConnector
  val displayConstructors = DisplayConstructors
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val postSignInRedirectUrl = FrontendAppConfig.ipStartUrl
}

trait AmendsController  extends FrontendController with AuthorisedForPLA {

  val keyStoreConnector: KeyStoreConnector
  val displayConstructors: DisplayConstructors

  def amendsSummary(protectionType: String, status: String): Action[AnyContent] = AuthorisedByAny.async { implicit user => implicit request =>
    val protectionKey = Strings.keyStoreAmendFetchString(protectionType, status)
    keyStoreConnector.fetchAndGetFormData[AmendProtectionModel](protectionKey).map {
      case Some(amendModel) => Ok(views.html.pages.amends.amendSummary(displayConstructors.createAmendDisplayModel(amendModel)))
      case _ =>
        Logger.error(s"Could not retrieve amend protection model for user with nino ${user.nino} when loading the amend summary page")
        InternalServerError(views.html.pages.fallback.technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
    }
  }


  def amendPensionsTakenBefore(protectionType: String, status: String): Action[AnyContent] = AuthorisedByAny.async { implicit user => implicit request =>

    keyStoreConnector.fetchAndGetFormData[AmendProtectionModel](Strings.keyStoreAmendFetchString(protectionType, status)).map {
      case Some(data) =>
        val yesNoValue = if (data.updatedProtection.preADayPensionInPayment.get > 0) "yes" else "no"
        val amendModel = AmendPensionsTakenBeforeModel(yesNoValue, Some(data.updatedProtection.preADayPensionInPayment.get), protectionType, status)
        protectionType match {
          case "ip2016" => Ok(pages.amends.amendPensionsTakenBefore(amendPensionsTakenBeforeForm.fill(amendModel)))
          case "ip2014" => Ok(pages.amends.amendIP14PensionsTakenBefore(amendPensionsTakenBeforeForm.fill(amendModel)))
        }
      case _ =>
      Logger.error(s"Could not retrieve amend protection model for user with nino ${user.nino} when loading the amend pensions taken before page")
      InternalServerError(views.html.pages.fallback.technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
    }
  }

  val submitAmendPensionsTakenBefore = AuthorisedByAny.async {
    implicit user => implicit request =>
      amendPensionsTakenBeforeForm.bindFromRequest.fold(
        errors => Future.successful(BadRequest(pages.amends.amendPensionsTakenBefore(errors))),
        success => {
          val validatedForm = AmendPensionsTakenBeforeForm.validateForm(amendPensionsTakenBeforeForm.fill(success))
          if (validatedForm.hasErrors) {
            Future.successful(BadRequest(pages.amends.amendPensionsTakenBefore(validatedForm)))
          } else {
            keyStoreConnector.fetchAndGetFormData[AmendProtectionModel](Strings.keyStoreAmendFetchString(success.protectionType, success.status)).map {
              case Some(model) =>
                val updatedAmount = success.pensionsTakenBefore match {
                  case "yes" => success.pensionsTakenBeforeAmt.get.toDouble
                  case "no"  => 0.asInstanceOf[Double]
                }
                val updated = model.updatedProtection.copy(preADayPensionInPayment = Some(updatedAmount))
                val updatedTotal = updated.copy(relevantAmount = Some(Helpers.totalValue(updated)))
                val amendProtModel = AmendProtectionModel(model.originalProtection, updatedTotal)

                keyStoreConnector.saveFormData[AmendProtectionModel](Strings.keyStoreProtectionName(updated), amendProtModel)
                Redirect(routes.AmendsController.amendsSummary(updated.protectionType.get.toLowerCase, updated.status.get.toLowerCase))
              case _ =>
                Logger.error(s"Could not retrieve amend protection model for user with nino ${user.nino} after submitting amend pensions taken before")
                InternalServerError(views.html.pages.fallback.technicalError(ApplicationType.IP2016.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
            }
          }
        }
      )
  }

  def amendPensionsTakenBetween(protectionType: String, status: String): Action[AnyContent] = AuthorisedByAny.async { implicit user => implicit request =>
    Future.successful(Ok)
  }

  def amendOverseasPensions(protectionType: String, status: String): Action[AnyContent] = AuthorisedByAny.async { implicit user => implicit request =>
    Future.successful(Ok)
  }

  def amendCurrentPensions(protectionType: String, status: String): Action[AnyContent] = AuthorisedByAny.async { implicit user => implicit request =>
    keyStoreConnector.fetchAndGetFormData[AmendProtectionModel](Strings.keyStoreAmendFetchString(protectionType, status)).map {
      case Some(data) =>
        val amendModel = AmendCurrentPensionModel(Some(data.updatedProtection.uncrystallisedRights.get), protectionType, status)
        protectionType match {
          case "ip2016" => Ok(pages.amends.amendCurrentPensions(amendCurrentPensionForm.fill(amendModel)))
          case "ip2014" => Ok(pages.amends.amendIP14CurrentPensions(amendCurrentPensionForm.fill(amendModel)))
        }
      case _ =>
        Logger.error(s"Could not retrieve amend protection model for user with nino ${user.nino} when loading the amend current UK pension page")
        InternalServerError(views.html.pages.fallback.technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
    }
  }

  val submitAmendCurrentPension = AuthorisedByAny.async { implicit user => implicit request =>

      amendCurrentPensionForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(pages.amends.amendCurrentPensions(errors))),
      success => {
        keyStoreConnector.fetchAndGetFormData[AmendProtectionModel](Strings.keyStoreAmendFetchString(success.protectionType, success.status)).map{
          case Some(model) =>
            val updated = model.updatedProtection.copy(uncrystallisedRights = Some(success.amendedUKPensionAmt.get.toDouble))
            val updatedTotal = updated.copy(relevantAmount = Some(Helpers.totalValue(updated)))
            val amendProtModel = AmendProtectionModel(model.originalProtection, updatedTotal)

            keyStoreConnector.saveFormData[AmendProtectionModel](Strings.keyStoreProtectionName(updated), amendProtModel)
            Redirect(routes.AmendsController.amendsSummary(updated.protectionType.get.toLowerCase, updated.status.get.toLowerCase))

          case _ =>
            Logger.error(s"Could not retrieve amend protection model for user with nino ${user.nino} after submitting amend current UK pension")
            InternalServerError(views.html.pages.fallback.technicalError(ApplicationType.IP2016.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
        }
      }
    )
  }

}
