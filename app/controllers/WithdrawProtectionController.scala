/*
 * Copyright 2019 HM Revenue & Customs
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

import auth.AuthFunction
import common.Strings
import config.wiring.PlaFormPartialRetriever
import config.{AuthClientConnector, FrontendAppConfig, LocalTemplateRenderer}
import connectors.{KeyStoreConnector, PLAConnector}
import constructors.DisplayConstructors
import enums.ApplicationType
import forms.WithdrawDateForm._
import javax.inject.Inject
import models.ProtectionModel
import play.api.{Configuration, Environment, Logger, Play}
import play.api.Play.current
import play.api.data.{Form, FormError}
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent, Request, Result}
import uk.gov.hmrc.auth.core.retrieve.Retrievals
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions, Enrolment}
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future


class WithdrawProtectionController @Inject()(keyStoreConnector: KeyStoreConnector,
                                             plaConnector: PLAConnector,
                                             implicit val partialRetriever: PlaFormPartialRetriever,
                                             implicit val templateRenderer:LocalTemplateRenderer) extends BaseController with AuthFunction {
  val displayConstructors: DisplayConstructors = DisplayConstructors
  lazy val appConfig = FrontendAppConfig
  override lazy val authConnector: AuthConnector = AuthClientConnector
  lazy val postSignInRedirectUrl = FrontendAppConfig.existingProtectionsUrl

  override def config: Configuration = Play.current.configuration

  override def env: Environment = Play.current.injector.instanceOf[Environment]

  /** Withdraw protections journey **/
  def withdrawSummary: Action[AnyContent] = Action.async {
    implicit request =>
      genericAuthWithNino("existingProtections") { nino =>
        keyStoreConnector.fetchAndGetFormData[ProtectionModel]("openProtection") map {
          case Some(currentProtection) =>
            Ok(views.html.pages.withdraw.withdrawSummary(displayConstructors.createWithdrawSummaryTable(currentProtection)))
          case _ =>
            Logger.error(s"Could not retrieve protection data for user with nino $nino when loading the withdraw summary page")
            InternalServerError(views.html.pages.fallback.technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
        }
      }
  }

  def withdrawImplications: Action[AnyContent] = Action.async {
    implicit request =>
      genericAuthWithNino("existingProtections") { nino =>
        keyStoreConnector.fetchAndGetFormData[ProtectionModel]("openProtection") map {
          case Some(currentProtection) =>
            Ok(views.html.pages.withdraw.withdrawImplications(withdrawDateForm,
              Strings.protectionTypeString(currentProtection.protectionType),
              Strings.statusString(currentProtection.status)))
          case _ =>
            Logger.error(s"Could not retrieve protection data for user with nino $nino when loading the withdraw summary page")
            InternalServerError(views.html.pages.fallback.technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
        }
      }
  }

  def withdrawDateInput: Action[AnyContent] = Action.async {
    implicit request =>
      genericAuthWithNino("existingProtections") { nino =>
        keyStoreConnector.fetchAndGetFormData[ProtectionModel]("openProtection") map {
          case Some(currentProtection) =>
            Ok(views.html.pages.withdraw.withdrawDate(withdrawDateForm,
              Strings.protectionTypeString(currentProtection.protectionType),
              Strings.statusString(currentProtection.status)))
          case _ =>
            Logger.error(s"Could not retrieve protection data for user with nino $nino when loading the withdraw summary page")
            InternalServerError(views.html.pages.fallback.technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
        }
      }
  }


  def submitWithdrawDateInput: Action[AnyContent] = Action.async {
    implicit request =>
      genericAuthWithNino("existingProtections") { nino =>
        keyStoreConnector.fetchAndGetFormData[ProtectionModel]("openProtection") map {
          case Some(protection) =>
            validateWithdrawDate(withdrawDateForm.bindFromRequest(),
            LocalDateTime.parse(protection.certificateDate.get)).fold(
            formWithErrors =>
              BadRequest(views.html.pages.withdraw.withdrawDate(buildInvalidForm(formWithErrors),
              Strings.protectionTypeString(protection.protectionType),
              Strings.statusString(protection.status))),
            _ => Ok(views.html.pages.withdraw.withdrawConfirm(
              getWithdrawDate(withdrawDateForm.bindFromRequest()), Strings.protectionTypeString(protection.protectionType),
              Strings.statusString(protection.status)))
          )
          case _ => Logger.error(s"Could not retrieve protection data for user with nino $nino when loading the withdraw date input page")
            InternalServerError(views.html.pages.fallback.technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
        }
      }
  }


  def displayWithdrawConfirmation(withdrawDate: String): Action[AnyContent] = Action.async {
    implicit request =>
      genericAuthWithNino("existingProtections") { nino =>
        for {
          protectionAmendment <- keyStoreConnector.fetchAndGetFormData[ProtectionModel]("openProtection")
          response <- plaConnector.amendProtection(nino, protectionAmendment.get.copy(withdrawnDate = Some(withdrawDate), status = Some("Withdrawn")))
          result <- routeToWithdrawConfirmation(protectionAmendment.get.protectionType, response, nino)
        } yield result
      }
  }

  private def routeToWithdrawConfirmation(protectionType: Option[String], response: HttpResponse, nino: String)
                                         (implicit request: Request[AnyContent]): Future[Result] = {
    response.status match {
      case OK => Future.successful(Redirect(routes.WithdrawProtectionController.showWithdrawConfirmation(Strings.protectionTypeString(protectionType))))
      case _ => {
        Logger.error(s"conflict response returned for withdrawal request for user nino $nino")
        Future.successful(InternalServerError(
          views.html.pages.fallback.technicalError(ApplicationType.existingProtections.toString))
          .withHeaders(CACHE_CONTROL -> "no-cache"))
      }
    }
  }


  def showWithdrawConfirmation(protectionType: String): Action[AnyContent] = Action.async {
    implicit request =>
      genericAuthWithoutNino("existingProtections") {
        keyStoreConnector.remove.map {
          _ => Ok(views.html.pages.withdraw.withdrawConfirmation(protectionType))
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
