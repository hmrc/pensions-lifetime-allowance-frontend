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

import java.text.DecimalFormat

import auth.AuthorisedForPLA
import auth.{PLAUser, AuthorisedForPLA}
import common.{Exceptions, Helpers, Strings}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{PLAConnector, KeyStoreConnector}
import constructors.{ResponseConstructors, DisplayConstructors}
import enums.ApplicationType
import forms.AmendCurrentPensionForm._
import forms.AmendmentTypeForm._
import models.AmendResponseModel
import models.amendModels.AmendmentTypeModel
import play.api.mvc._
import forms.AmendPensionsTakenBeforeForm
import forms.AmendPensionsTakenBeforeForm._
import models.amendModels.{AmendCurrentPensionModel, AmendPensionsTakenBeforeModel, AmendProtectionModel}
import play.api.Logger
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HttpResponse
import utils.Constants
import views.html.pages
import views.html.pages.result.manualCorrespondenceNeeded

import scala.concurrent.Future

object AmendsController extends AmendsController{
  val keyStoreConnector = KeyStoreConnector
  val displayConstructors = DisplayConstructors
  val responseConstructors = ResponseConstructors
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val postSignInRedirectUrl = FrontendAppConfig.ipStartUrl
}

trait AmendsController  extends FrontendController with AuthorisedForPLA {

  val keyStoreConnector: KeyStoreConnector
  val displayConstructors: DisplayConstructors
  val responseConstructors: ResponseConstructors

  def amendsSummary(protectionType: String, status: String): Action[AnyContent] = AuthorisedByAny.async { implicit user => implicit request =>
    val protectionKey = Strings.keyStoreAmendFetchString(protectionType, status)
    keyStoreConnector.fetchAndGetFormData[AmendProtectionModel](protectionKey).map {
      case Some(amendModel) => Ok(views.html.pages.amends.amendSummary(displayConstructors.createAmendDisplayModel(amendModel),
                                                                        amendmentTypeForm.fill(AmendmentTypeModel(protectionType, status))
                                                                      ))
      case _ =>
        Logger.error(s"Could not retrieve amend protection model for user with nino ${user.nino} when loading the amend summary page")
        InternalServerError(views.html.pages.fallback.technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
    }
  }

  val amendProtection = AuthorisedByAny.async { implicit user => implicit request =>
    amendmentTypeForm.bindFromRequest.fold(
      errors => {
        Logger.error(s"Couldn't bind protection type or status to amend request for user with nino ${user.nino}")
        Future.successful(InternalServerError(views.html.pages.fallback.technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache"))
      },
      success => for {
        protectionAmendment <- keyStoreConnector.fetchAndGetFormData[AmendProtectionModel](Strings.keyStoreAmendFetchString(success.protectionType, success.status))
        response <- PLAConnector.amendProtection(user.nino.get, protectionAmendment.get.updatedProtection)
        result <- routeViaMCNeededCheck(response)
      } yield result
    )

  }

  private def routeViaMCNeededCheck(response: HttpResponse)(implicit request: Request[AnyContent], user: PLAUser): Future[Result] = {
    response.status match {
      case 423 => Future.successful(Locked(manualCorrespondenceNeeded()))
      case _ => saveAndRedirectToDisplay(response)
    }
  }

  def saveAndRedirectToDisplay(response: HttpResponse)(implicit request: Request[AnyContent], user: PLAUser): Future[Result] = {
    responseConstructors.createAmendResponseModelFromJson(response.json).map{
      model => keyStoreConnector.saveData[AmendResponseModel]("amendResponseModel", model).map {
        cacheMap => Redirect(routes.AmendsController.amendmentOutcome())
      }
    }.getOrElse {
      Logger.error(s"Unable to create Amend Response Model from PLA response for user nino: ${user.nino}")
      Future.successful(InternalServerError(views.html.pages.fallback.technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache"))
    }
  }


  def amendmentOutcome = AuthorisedByAny.async { implicit user => implicit request =>
    keyStoreConnector.fetchAndGetFormData[AmendResponseModel]("amendResponseModel").map {
      case Some(model) => {
        val id = model.protection.notificationId.getOrElse{throw new Exceptions.RequiredValueNotDefinedException("amendmentOutcome", "notificationId") }
        if(Constants.activeAmendmentCodes.contains(id)) {
          Ok(views.html.pages.amends.outcomeActive(displayConstructors.createActiveAmendResponseDisplayModel(model)))
        } else {
          Ok(views.html.pages.amends.outcomeInactive(displayConstructors.createInactiveAmendResponseDisplayModel(model)))
        }
      }
      case _ => {
        Logger.error(s"Unable to retrieve amendment outcome model from keyStore for user nino :${user.nino}")
        InternalServerError(views.html.pages.fallback.technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
      }
    }
  }
  def amendPensionsTakenBefore(protectionType: String, status: String): Action[AnyContent] = AuthorisedByAny.async { implicit user => implicit request =>

    keyStoreConnector.fetchAndGetFormData[AmendProtectionModel](Strings.keyStoreAmendFetchString(protectionType, status)).map {
      case Some(data) =>
        def df(n: BigDecimal):String = new DecimalFormat("0.00").format(n).replace(".00","")
        val yesNoValue = if (data.updatedProtection.preADayPensionInPayment.get > 0) "yes" else "no"

        val amendModel = AmendPensionsTakenBeforeModel(yesNoValue, Some(BigDecimal(df(data.updatedProtection.preADayPensionInPayment.get))), protectionType, status)
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
                val updatedAmount = success.amendedPensionsTakenBefore match {
                  case "yes" => success.amendedPensionsTakenBeforeAmt.get.toDouble
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
        def df(n: BigDecimal):String = new DecimalFormat("0.00").format(n).replace(".00","")
        val amendModel = AmendCurrentPensionModel(Some(BigDecimal(df(data.updatedProtection.uncrystallisedRights.get))), protectionType, status)
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
            InternalServerError(views.html.pages.fallback.technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
        }
      }
    )
  }

}
