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

import auth.{PLAUser, AuthorisedForPLA}
import common._
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{KeyStoreConnector, PLAConnector}
import constructors.{DisplayConstructors, ResponseConstructors}
import enums.ApplicationType
import forms.AmendCurrentPensionForm._
import forms.AmendOverseasPensionsForm
import forms.AmendOverseasPensionsForm._
import forms.AmendPensionsTakenBeforeForm
import forms.AmendPensionsTakenBeforeForm._
import forms.AmendPensionsTakenBetweenForm
import forms.AmendPensionsTakenBetweenForm._
import forms.AmendPSODetailsForm
import forms.AmendPSODetailsForm._
import forms.AmendmentTypeForm._
import models.{PensionDebitModel, ProtectionModel, AmendResponseModel}
import models.amendModels._
import play.api.Logger
import play.api.mvc.{Action, AnyContent, Result, _}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HttpResponse
import utils.Constants
import views.html.pages
import views.html.pages.result.manualCorrespondenceNeeded

import scala.concurrent.Future

object AmendsController extends AmendsController {
  val keyStoreConnector = KeyStoreConnector
  val displayConstructors = DisplayConstructors
  val responseConstructors = ResponseConstructors
  val plaConnector = PLAConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val postSignInRedirectUrl = FrontendAppConfig.ipStartUrl
}

trait AmendsController extends FrontendController with AuthorisedForPLA {

  val keyStoreConnector: KeyStoreConnector
  val displayConstructors: DisplayConstructors
  val responseConstructors: ResponseConstructors
  val plaConnector: PLAConnector

  def dummy: Action[AnyContent] = AuthorisedByAny.async { implicit user => implicit request =>
    Future(Ok)
  }

  def amendsSummary(protectionType: String, status: String): Action[AnyContent] = AuthorisedByAny.async { implicit user => implicit request =>
    val protectionKey = Strings.keyStoreAmendFetchString(protectionType, status)
    keyStoreConnector.fetchAndGetFormData[AmendProtectionModel](protectionKey).map {
      case Some(amendModel) => Ok(views.html.pages.amends.amendSummary(
        displayConstructors.createAmendDisplayModel(amendModel),
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
        response <- plaConnector.amendProtection(user.nino.get, protectionAmendment.get.updatedProtection)
        result <- routeViaMCNeededCheck(response)
      } yield result
    )
  }

  private def routeViaMCNeededCheck(response: HttpResponse)(implicit request: Request[AnyContent], user: PLAUser): Future[Result] = {
    response.status match {
      case 409 => {
        Logger.error(s"conflict response returned for amend request for user nino ${user.nino}")
        Future.successful(InternalServerError(views.html.pages.fallback.technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache"))
      }
      case 423 => Future.successful(Locked(manualCorrespondenceNeeded()))
      case _ => saveAndRedirectToDisplay(response)
    }
  }

  def saveAndRedirectToDisplay(response: HttpResponse)(implicit request: Request[AnyContent], user: PLAUser): Future[Result] = {
    responseConstructors.createAmendResponseModelFromJson(response.json).map {
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
        val id = model.protection.notificationId.getOrElse {
          throw new Exceptions.RequiredValueNotDefinedException("amendmentOutcome", "notificationId")
        }
        if (Constants.activeAmendmentCodes.contains(id)) {
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

    amendRoute(AmendJourney.pensionTakenBefore, protectionType, status).apply(request)
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
                  case "no" => 0.asInstanceOf[Double]
                }
                val updated = model.updatedProtection.copy(preADayPensionInPayment = Some(updatedAmount))
                val updatedTotal = updated.copy(relevantAmount = Some(Helpers.totalValue(updated)))
                val amendProtModel = AmendProtectionModel(model.originalProtection, updatedTotal)

                keyStoreConnector.saveFormData[AmendProtectionModel](Strings.keyStoreProtectionName(updated), amendProtModel)
                Redirect(routes.AmendsController.amendsSummary(updated.protectionType.get.toLowerCase, updated.status.get.toLowerCase))
              case _ =>
                Logger.error(s"Could not retrieve amend protection model for user with nino ${user.nino} after submitting amend pensions taken before")
                InternalServerError(views.html.pages.fallback.technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
            }
          }
        }
      )
  }

  def amendPensionsTakenBetween(protectionType: String, status: String): Action[AnyContent] = AuthorisedByAny.async { implicit user => implicit request =>
    amendRoute(AmendJourney.pensionTakenBetween, protectionType, status).apply(request)
  }

  val submitAmendPensionsTakenBetween = AuthorisedByAny.async { implicit user => implicit request =>
    amendPensionsTakenBetweenForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(pages.amends.amendPensionsTakenBetween(errors))),
      success => {
        val validatedForm = AmendPensionsTakenBetweenForm.validateForm(amendPensionsTakenBetweenForm.fill(success))
        if (validatedForm.hasErrors) {
          Future.successful(BadRequest(pages.amends.amendPensionsTakenBetween(validatedForm)))
        } else {
          keyStoreConnector.fetchAndGetFormData[AmendProtectionModel](Strings.keyStoreAmendFetchString(success.protectionType, success.status)).map {
            case Some(model) =>
              val updatedAmount = success.amendedPensionsTakenBetween match {
                case "yes" => success.amendedPensionsTakenBetweenAmt.get.toDouble
                case "no" => 0.asInstanceOf[Double]
              }
              val updated = model.updatedProtection.copy(postADayBenefitCrystallisationEvents = Some(updatedAmount))
              val updatedTotal = updated.copy(relevantAmount = Some(Helpers.totalValue(updated)))
              val amendProtModel = AmendProtectionModel(model.originalProtection, updatedTotal)

              keyStoreConnector.saveFormData[AmendProtectionModel](Strings.keyStoreProtectionName(updated), amendProtModel)
              Redirect(routes.AmendsController.amendsSummary(updated.protectionType.get.toLowerCase, updated.status.get.toLowerCase))
            case _ =>
              Logger.error(s"Could not retrieve amend protection model for user with nino ${user.nino} after submiiting amend pensions takne between")
              InternalServerError(views.html.pages.fallback.technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
          }
        }
      }
    )

  }

  def amendOverseasPensions(protectionType: String, status: String): Action[AnyContent] = AuthorisedByAny.async { implicit user => implicit request =>
    amendRoute(AmendJourney.overseasPension, protectionType, status).apply(request)
  }

  val submitAmendOverseasPensions = AuthorisedByAny.async {
    implicit user => implicit request =>
      amendOverseasPensionsForm.bindFromRequest.fold(
        errors => Future.successful(BadRequest(pages.amends.amendOverseasPensions(errors))),
        success => {
          val validatedForm = AmendOverseasPensionsForm.validateForm(amendOverseasPensionsForm.fill(success))
          if (validatedForm.hasErrors) {
            Future.successful(BadRequest(pages.amends.amendOverseasPensions(validatedForm)))
          } else {
            keyStoreConnector.fetchAndGetFormData[AmendProtectionModel](Strings.keyStoreAmendFetchString(success.protectionType, success.status)).map {
              case Some(model) =>
                val updatedAmount = success.amendedOverseasPensions match {
                  case "yes" => success.amendedOverseasPensionsAmt.get.toDouble
                  case "no" => 0.asInstanceOf[Double]
                }
                val updated = model.updatedProtection.copy(nonUKRights = Some(updatedAmount))
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

  def amendCurrentPensions(protectionType: String, status: String): Action[AnyContent] = AuthorisedByAny.async { implicit user => implicit request =>
    amendRoute(AmendJourney.currentPension, protectionType, status).apply(request)
  }

  val submitAmendCurrentPension = AuthorisedByAny.async { implicit user => implicit request =>

    amendCurrentPensionForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(pages.amends.amendCurrentPensions(errors))),
      success => {
        keyStoreConnector.fetchAndGetFormData[AmendProtectionModel](Strings.keyStoreAmendFetchString(success.protectionType, success.status)).map {
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

  def amendPsoDetails(protectionType: String, status: String) = AuthorisedByAny.async { implicit user => implicit request =>
    keyStoreConnector.fetchAndGetFormData[AmendProtectionModel](Strings.keyStoreAmendFetchString(protectionType, status)).map {
      case Some(amendProtectionModel) => amendProtectionModel.updatedProtection.pensionDebits.map { debits =>
        routeFromPensionDebitsList(debits, protectionType, status)
      }.getOrElse(Ok(pages.amends.amendPsoDetails(amendPsoDetailsForm.fill(createBlankAmendPsoDetailsModel(protectionType, status)))))
      case _ =>
        Logger.error(s"Could not retrieve amend protection model for user with nino ${user.nino} when loading the amend PSO details page")
        InternalServerError(views.html.pages.fallback.technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
    }
  }

  def routeFromPensionDebitsList(debits: Seq[PensionDebitModel], protectionType: String, status: String)(implicit user: PLAUser, request: Request[AnyContent]): Result = {
    debits.length match {
      case 0 => Ok(pages.amends.amendPsoDetails(amendPsoDetailsForm.fill(createBlankAmendPsoDetailsModel(protectionType, status))))
      case 1 => Ok(pages.amends.amendPsoDetails(amendPsoDetailsForm.fill(createAmendPsoDetailsModel(debits.head, protectionType, status))))
      case num => {
        Logger.error(s"$num pension debits recorded for user nino ${user.nino.getOrElse("NO NINO")} during amend journey")
        InternalServerError(views.html.pages.fallback.technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
      }
    }
  }

  def createAmendPsoDetailsModel(psoDetails: PensionDebitModel, protectionType: String, status: String): AmendPSODetailsModel = {
    val (day, month, year) = Dates.extractDMYFromAPIDateString(psoDetails.startDate)
    AmendPSODetailsModel(Some(day), Some(month), Some(year), BigDecimal(psoDetails.amount), protectionType, status)
  }

  def createBlankAmendPsoDetailsModel(protectionType: String, status: String): AmendPSODetailsModel = {
    AmendPSODetailsModel(psoDay = None, psoMonth = None, psoYear = None, psoAmt = 0, protectionType, status)
  }

  val submitAmendPsoDetails = AuthorisedByAny.async { implicit user => implicit request =>
    amendPsoDetailsForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(pages.amends.amendPsoDetails(errors))),
      success => {
        val validatedForm = AmendPSODetailsForm.validateForm(amendPsoDetailsForm.fill(success))
        if (validatedForm.hasErrors) {
          Future.successful(BadRequest(pages.amends.amendPsoDetails(validatedForm)))
        } else {
          val details = createPsoDetailsList(success)
          for {
            amendModel <- keyStoreConnector.fetchAndGetFormData[AmendProtectionModel](Strings.keyStoreAmendFetchString(success.protectionType, success.status))
            storedModel <- updateAndSaveAmendModelWithPso(details, amendModel, Strings.keyStoreAmendFetchString(success.protectionType, success.status))
          } yield Redirect(routes.AmendsController.amendsSummary(success.protectionType.toLowerCase, success.status.toLowerCase))
        }
      }
    )
  }

  private def createPsoDetailsList(formModel: AmendPSODetailsModel): Option[List[PensionDebitModel]] = {
    val date = Dates.apiDateFormat(formModel.psoDay, formModel.psoMonth, formModel.psoYear)
    val amt = formModel.psoAmt.toDouble
    Some(List(PensionDebitModel(startDate = date, amount = amt)))
  }

  private def updateAndSaveAmendModelWithPso(debits: Option[List[PensionDebitModel]], amendModelOption: Option[AmendProtectionModel], key: String)(implicit request: Request[AnyContent]) = {
    val amendModel = amendModelOption.getOrElse {throw new Exceptions.RequiredValueNotDefinedException("updateAndSaveAmendModelWithPso", "amendModel")}
    val newUpdatedProtection = amendModel.updatedProtection.copy(pensionDebits = debits)
    keyStoreConnector.saveData[AmendProtectionModel](key, amendModel.copy(updatedProtection = newUpdatedProtection))
  }

  private def getRouteUsingModel(model: AmendValueModel)(implicit request: Request[AnyContent]) = {

    model match {
      case AmendCurrentPensionModel(_,"ip2016",_) =>
        Ok(pages.amends.amendCurrentPensions(amendCurrentPensionForm.fill(model.asInstanceOf[AmendCurrentPensionModel])))

      case AmendCurrentPensionModel(_,"ip2014",_) =>
        Ok(pages.amends.amendIP14CurrentPensions(amendCurrentPensionForm.fill(model.asInstanceOf[AmendCurrentPensionModel])))

      case AmendPensionsTakenBeforeModel(_,_,"ip2016",_) =>
        Ok(pages.amends.amendPensionsTakenBefore(amendPensionsTakenBeforeForm.fill(model.asInstanceOf[AmendPensionsTakenBeforeModel])))

      case AmendPensionsTakenBeforeModel(_,_,"ip2014",_) =>
        Ok(pages.amends.amendIP14PensionsTakenBefore(amendPensionsTakenBeforeForm.fill(model.asInstanceOf[AmendPensionsTakenBeforeModel])))

      case AmendPensionsTakenBetweenModel(_,_,"ip2016",_) =>
        Ok(pages.amends.amendPensionsTakenBetween(amendPensionsTakenBetweenForm.fill(model.asInstanceOf[AmendPensionsTakenBetweenModel])))

      case AmendPensionsTakenBetweenModel(_,_,"ip2014",_) =>
        Ok(pages.amends.amendIP14PensionsTakenBetween(amendPensionsTakenBetweenForm.fill(model.asInstanceOf[AmendPensionsTakenBetweenModel])))

      case AmendOverseasPensionsModel(_,_,"ip2016",_) =>
        Ok(pages.amends.amendOverseasPensions(amendOverseasPensionsForm.fill(model.asInstanceOf[AmendOverseasPensionsModel])))

      case AmendOverseasPensionsModel(_,_,"ip2014",_) =>
        Ok(pages.amends.amendIP14OverseasPensions(amendOverseasPensionsForm.fill(model.asInstanceOf[AmendOverseasPensionsModel])))
    }
  }

  private def amendRoute(journey: AmendJourney.Value, protectionType: String, status: String) = AuthorisedByAny.async { implicit user => implicit request =>
    import AmendJourney._

    keyStoreConnector.fetchAndGetFormData[AmendProtectionModel](Strings.keyStoreAmendFetchString(protectionType, status)).map {
      case Some(data) =>
        getRouteUsingModel(
          journey match {
            case `currentPension` =>
              AmendCurrentPensionModel(Some(Display.currencyInputDisplayFormat(data.updatedProtection.uncrystallisedRights.get)), protectionType, status)
            case `pensionTakenBefore` =>
              val yesNoValue = if (data.updatedProtection.preADayPensionInPayment.get > 0) "yes" else "no"
              AmendPensionsTakenBeforeModel(yesNoValue,
                Some(Display.currencyInputDisplayFormat(data.updatedProtection.preADayPensionInPayment.get)),
                protectionType,
                status)
            case `pensionTakenBetween` =>
              val yesNoValue = if (data.updatedProtection.postADayBenefitCrystallisationEvents.get > 0) "yes" else "no"
              AmendPensionsTakenBetweenModel(
                yesNoValue,
                Some(Display.currencyInputDisplayFormat(data.updatedProtection.postADayBenefitCrystallisationEvents.get)),
                protectionType,
                status)
            case `overseasPension` =>
              val yesNoValue = if (data.updatedProtection.nonUKRights.get > 0) "yes" else "no"
              AmendOverseasPensionsModel(yesNoValue, Some(Display.currencyInputDisplayFormat(data.updatedProtection.nonUKRights.get)), protectionType, status)
          }
        )(request)
      case _ =>
        Logger.error(s"Could not retrieve amend protection model for user with nino ${user.nino} when loading the amend $journey page")
        InternalServerError(views.html.pages.fallback.technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
    }

  }

  private object AmendJourney extends Enumeration {
    val currentPension = Value
    val pensionTakenBefore = Value
    val pensionTakenBetween = Value
    val overseasPension = Value
  }

}
