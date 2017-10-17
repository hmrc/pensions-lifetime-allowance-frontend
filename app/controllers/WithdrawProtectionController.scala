/*
 * Copyright 2017 HM Revenue & Customs
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

import java.time.LocalDateTime

import auth.{AuthorisedForPLA, PLAUser}
import common.Strings
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{KeyStoreConnector, PLAConnector}
import constructors.{AmendsGAConstructor, DisplayConstructors}
import enums.ApplicationType
import forms.WithdrawDateForm._
import models.amendModels.{AmendProtectionModel, AmendsGAModel}
import models.{ProtectionModel, WithdrawalRequest}
import play.api.Logger
import play.api.Play.current
import play.api.data.{Form, FormError}
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent, Request, Result}
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.pages
import views.html.pages.result.manualCorrespondenceNeeded

import scala.concurrent.Future



object WithdrawProtectionController extends WithdrawProtectionController {
  val keyStoreConnector = KeyStoreConnector
  val displayConstructors = DisplayConstructors
  val plaConnector = PLAConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val postSignInRedirectUrl = FrontendAppConfig.ipStartUrl
}

trait WithdrawProtectionController extends BaseController with AuthorisedForPLA {

  val keyStoreConnector: KeyStoreConnector
  val displayConstructors: DisplayConstructors
  val plaConnector: PLAConnector

  /** Withdraw protections journey **/
  def withdrawSummary: Action[AnyContent] = AuthorisedByAny.async { implicit user =>
    implicit request =>
      keyStoreConnector.fetchAndGetFormData[ProtectionModel]("openProtection") map {
        case Some(currentProtection) =>
          Ok(views.html.pages.withdraw.withdrawSummary(displayConstructors.createWithdrawSummaryTable(currentProtection)))
        case _ =>
          Logger.warn(s"Could not retrieve protection data for user with nino ${user.nino} when loading the withdraw summary page")
          InternalServerError(views.html.pages.fallback.technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
      }
  }

  def withdrawImplications: Action[AnyContent] = AuthorisedByAny.async { implicit user =>
    implicit request =>
    keyStoreConnector.fetchAndGetFormData[ProtectionModel]("openProtection") map {
      case Some(currentProtection) =>
        Ok(views.html.pages.withdraw.withdrawImplications(withdrawDateForm,
          Strings.protectionTypeString(currentProtection.protectionType),
          Strings.statusString(currentProtection.status)))
      case _ =>
        Logger.warn(s"Could not retrieve protection data for user with nino ${user.nino} when loading the withdraw summary page")
        InternalServerError(views.html.pages.fallback.technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
    }
  }

  def withdrawDateInput: Action[AnyContent] = AuthorisedByAny.async { implicit user =>
    implicit request =>
      keyStoreConnector.fetchAndGetFormData[ProtectionModel]("openProtection") map {
        case Some(currentProtection) =>
          Ok(views.html.pages.withdraw.withdrawDate(withdrawDateForm,
            Strings.protectionTypeString(currentProtection.protectionType),
            Strings.statusString(currentProtection.status)))
        case _ =>
          Logger.warn(s"Could not retrieve protection data for user with nino ${user.nino} when loading the withdraw summary page")
          InternalServerError(views.html.pages.fallback.technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
      }
  }


  def submitWithdrawDateInput: Action[AnyContent] = AuthorisedByAny.async { implicit user =>
    implicit request =>
      keyStoreConnector.fetchAndGetFormData[ProtectionModel]("openProtection") map {
        case Some(protection) => validateWithdrawDate(withdrawDateForm.bindFromRequest(),
          LocalDateTime.parse(protection.certificateDate.get)).fold(
          formWithErrors => BadRequest(views.html.pages.withdraw.withdrawDate(buildInvalidForm(formWithErrors),
            Strings.protectionTypeString(protection.protectionType),
            Strings.statusString(protection.status))),
          _ => Ok(views.html.pages.withdraw.withdrawConfirm(
            getWithdrawDate(withdrawDateForm.bindFromRequest()),Strings.protectionTypeString(protection.protectionType),
            Strings.statusString(protection.status)))
        )
        case _ => Logger.warn(s"Could not retrieve protection data for user with nino ${user.nino} when loading the withdraw date input page")
          InternalServerError(views.html.pages.fallback.technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
      }
  }



  def displayWithdrawConfirmation(withdrawDate: String): Action[AnyContent] = AuthorisedByAny.async { implicit user =>
    implicit request => for {
      protectionAmendment <- keyStoreConnector.fetchAndGetFormData[ProtectionModel]("openProtection")
      response <- plaConnector.amendProtection(user.nino.get, protectionAmendment.get.copy(withdrawnDate=Some(withdrawDate) , status = Some("Withdrawn")))
      result <- routeToWithdrawConfirmation(protectionAmendment.get.protectionType,response)
    } yield result

  }

  private def routeToWithdrawConfirmation(protectionType: Option[String] , response: HttpResponse)
                                   (implicit request: Request[AnyContent], user: PLAUser): Future[Result] = {
    response.status match {
      case OK => Future.successful(Ok(
        views.html.pages.withdraw.withdrawConfirmation(
          Strings.protectionTypeString(protectionType))))
      case _ => {
        Logger.warn(s"conflict response returned for withdrawal request for user nino ${user.nino}")
        Future.successful(InternalServerError(
          views.html.pages.fallback.technicalError(ApplicationType.existingProtections.toString))
          .withHeaders(CACHE_CONTROL -> "no-cache"))
      }
    }
  }





  private def buildInvalidForm(errorForm: Form[(Option[Int], Option[Int], Option[Int])]): Form[(Option[Int], Option[Int], Option[Int])] = {
    if (errorForm.errors.size > 1) {
      val formFields: Seq[String] = errorForm.errors map (field => field.key)
      val formFieldsWithErrors: Seq[FormError] = formFields map (key => FormError(key, ""))
      val finalErrors: Seq[FormError] = formFieldsWithErrors ++ Seq(FormError("", Messages("pla.withdraw.date-input-form.date-invalid")))
      withdrawDateForm.copy(errors = finalErrors, data = errorForm.data)
    }
    else errorForm
  }
}
