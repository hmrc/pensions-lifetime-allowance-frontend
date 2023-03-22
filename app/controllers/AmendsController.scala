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
import connectors.{KeyStoreConnector, PLAConnector}
import constructors.{AmendsGAConstructor, DisplayConstructors, ResponseConstructors}
import enums.ApplicationType
import forms.AmendCurrentPensionForm._
import forms.AmendOverseasPensionsForm._
import forms.AmendPSODetailsForm._
import forms.AmendPensionsTakenBeforeForm._
import forms.AmendPensionsTakenBetweenForm._
import forms.AmendmentTypeForm._

import javax.inject.Inject
import models.amendModels._
import models.{AmendResponseModel, PensionDebitModel, ProtectionModel}
import play.api.Logging
import play.api.data.FormError
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Result, _}
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever
import utils.Constants
import views.html.pages

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendsController @Inject()(val keyStoreConnector: KeyStoreConnector,
                                 val plaConnector: PLAConnector,
                                 displayConstructors: DisplayConstructors,
                                 mcc: MessagesControllerComponents,
                                 responseConstructors: ResponseConstructors,
                                 authFunction: AuthFunction,
                                 manualCorrespondenceNeeded: views.html.pages.result.manualCorrespondenceNeeded,
                                 noNotificationId: views.html.pages.fallback.noNotificationId,
                                 amendPsoDetails: pages.amends.amendPsoDetails,
                                 technicalError: views.html.pages.fallback.technicalError,
                                 amendCurrentPensions: pages.amends.amendCurrentPensions,
                                 amendPensionsTakenBefore: pages.amends.amendPensionsTakenBefore,
                                 amendPensionsTakenBetween: pages.amends.amendPensionsTakenBetween,
                                 amendIP14CurrentPensions: pages.amends.amendIP14CurrentPensions,
                                 amendIP14PensionsTakenBefore: pages.amends.amendIP14PensionsTakenBefore,
                                 amendIP14PensionsTakenBetween: pages.amends.amendIP14PensionsTakenBetween,
                                 amendOverseasPensions: pages.amends.amendOverseasPensions,
                                 amendIP14OverseasPensions: pages.amends.amendIP14OverseasPensions,
                                 outcomeActive: views.html.pages.amends.outcomeActive,
                                 outcomeInactive: views.html.pages.amends.outcomeInactive,
                                 removePsoDebits: pages.amends.removePsoDebits,
                                 amendSummary: views.html.pages.amends.amendSummary)
                                (implicit val appConfig: FrontendAppConfig,
                                 implicit val partialRetriever: FormPartialRetriever,
                                 implicit val formWithCSRF: FormWithCSRF,
                                 implicit val plaContext: PlaContext)
extends FrontendController(mcc) with I18nSupport with Logging{

  lazy val postSignInRedirectUrl = appConfig.existingProtectionsUrl

  val amendProtection = Action.async { implicit request =>
     authFunction.genericAuthWithNino("existingProtections") { nino =>
      amendmentTypeForm.bindFromRequest().fold(
        errors => {
          logger.warn(s"Couldn't bind protection type or status to amend request for user with nino $nino")
          Future.successful(InternalServerError(technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache"))
        },
        success => for {
          protectionAmendment <- keyStoreConnector.fetchAndGetFormData[AmendProtectionModel](Strings.keyStoreAmendFetchString(success.protectionType, success.status))
          saveAmendsGA <- keyStoreConnector.saveData[AmendsGAModel]("AmendsGA", AmendsGAConstructor.identifyAmendsChanges(protectionAmendment.get.updatedProtection, protectionAmendment.get.originalProtection))
          response <- plaConnector.amendProtection(nino, protectionAmendment.get.updatedProtection)
          result <- routeViaMCNeededCheck(response, nino)
        } yield result
      )
    }
  }
  val submitAmendPensionsTakenBefore = Action.async {
    implicit request =>
       authFunction.genericAuthWithNino("existingProtections") { nino =>
        amendPensionsTakenBeforeForm.bindFromRequest().fold(
          errors => {
            val form = errors.copy(errors = errors.errors.map { er => FormError(er.key, er.message) })
            Future.successful(BadRequest(amendPensionsTakenBefore(form)))
          },
          success => {
              keyStoreConnector.fetchAndGetFormData[AmendProtectionModel](Strings.keyStoreAmendFetchString(success.protectionType, success.status)).flatMap {
                case Some(model) =>
                  val updatedAmount = success.amendedPensionsTakenBefore match {
                    case "yes" => success.amendedPensionsTakenBeforeAmt.get.toDouble
                    case "no" => 0.asInstanceOf[Double]
                  }
                  val updated = model.updatedProtection.copy(preADayPensionInPayment = Some(updatedAmount))
                  val updatedTotal = updated.copy(relevantAmount = Some(Helpers.totalValue(updated)))
                  val amendProtModel = AmendProtectionModel(model.originalProtection, updatedTotal)

                  keyStoreConnector.saveFormData[AmendProtectionModel](Strings.keyStoreProtectionName(updated), amendProtModel).map {
                    _ => Redirect(routes.AmendsController.amendsSummary(updated.protectionType.get.toLowerCase, updated.status.get.toLowerCase))
                  }

                case _ =>
                  logger.warn(s"Could not retrieve amend protection model for user with nino $nino after submitting amend pensions taken before")
                  Future.successful(InternalServerError(technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache"))

            }
          }
        )
      }
  }
  val submitAmendPensionsTakenBetween = Action.async { implicit request => authFunction.genericAuthWithNino("existingProtections") { nino =>
      amendPensionsTakenBetweenForm.bindFromRequest().fold(
        errors => {
          val form = errors.copy(errors = errors.errors.map { er => FormError(er.key, er.message) })
          Future.successful(BadRequest(amendPensionsTakenBetween(form)))
        },
        success => {
            keyStoreConnector.fetchAndGetFormData[AmendProtectionModel](Strings.keyStoreAmendFetchString(success.protectionType, success.status)).flatMap {
              case Some(model) =>
                val updatedAmount = success.amendedPensionsTakenBetween match {
                  case "yes" => success.amendedPensionsTakenBetweenAmt.get.toDouble
                  case "no" => 0.asInstanceOf[Double]
                }
                val updated = model.updatedProtection.copy(postADayBenefitCrystallisationEvents = Some(updatedAmount))
                val updatedTotal = updated.copy(relevantAmount = Some(Helpers.totalValue(updated)))
                val amendProtModel = AmendProtectionModel(model.originalProtection, updatedTotal)

                keyStoreConnector.saveFormData[AmendProtectionModel](Strings.keyStoreProtectionName(updated), amendProtModel).map {
                  _ => Redirect(routes.AmendsController.amendsSummary(updated.protectionType.get.toLowerCase, updated.status.get.toLowerCase))
                }

              case _ =>
                logger.warn(s"Could not retrieve amend protection model for user with nino $nino after submitting amend pensions taken between")
                Future.successful(InternalServerError(technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache"))
            }
        }
      )

    }
  }
  val submitAmendOverseasPensions = Action.async {
    implicit request =>
       authFunction.genericAuthWithNino("existingProtections") { nino =>
        amendOverseasPensionsForm.bindFromRequest().fold(
          errors => {
            val form = errors.copy(errors = errors.errors.map { er => FormError(er.key, er.message) })
            Future.successful(BadRequest(amendOverseasPensions(form)))
          },
          success => {
              keyStoreConnector.fetchAndGetFormData[AmendProtectionModel](Strings.keyStoreAmendFetchString(success.protectionType, success.status)).flatMap {
                case Some(model) =>
                  val updatedAmount = success.amendedOverseasPensions match {
                    case "yes" => success.amendedOverseasPensionsAmt.get.toDouble
                    case "no" => 0.asInstanceOf[Double]
                  }
                  val updated = model.updatedProtection.copy(nonUKRights = Some(updatedAmount))
                  val updatedTotal = updated.copy(relevantAmount = Some(Helpers.totalValue(updated)))
                  val amendProtModel = AmendProtectionModel(model.originalProtection, updatedTotal)

                  keyStoreConnector.saveFormData[AmendProtectionModel](Strings.keyStoreProtectionName(updated), amendProtModel).map {
                    _ => Redirect(routes.AmendsController.amendsSummary(updated.protectionType.get.toLowerCase, updated.status.get.toLowerCase))
                  }

                case _ =>
                  logger.warn(s"Could not retrieve amend protection model for user with nino $nino after submitting amend pensions taken before")
                  Future.successful(InternalServerError(technicalError(ApplicationType.IP2016.toString)).withHeaders(CACHE_CONTROL -> "no-cache"))
              }
          }
        )
      }
  }
  val submitAmendCurrentPension = Action.async { implicit request => authFunction.genericAuthWithNino("existingProtections") { nino =>

      amendCurrentPensionForm.bindFromRequest().fold(
        errors => {
          val form = errors.copy(errors = errors.errors.map { er => FormError(er.key, er.message) })
          Future.successful(BadRequest(amendCurrentPensions(form)))
        },
        success => {
          keyStoreConnector.fetchAndGetFormData[AmendProtectionModel](Strings.keyStoreAmendFetchString(success.protectionType, success.status)).flatMap {
            case Some(model) =>
              val updated = model.updatedProtection.copy(uncrystallisedRights = Some(success.amendedUKPensionAmt.get.toDouble))
              val updatedTotal = updated.copy(relevantAmount = Some(Helpers.totalValue(updated)))
              val amendProtModel = AmendProtectionModel(model.originalProtection, updatedTotal)

              keyStoreConnector.saveFormData[AmendProtectionModel](Strings.keyStoreProtectionName(updated), amendProtModel).map {
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
  val submitRemovePso = Action.async { implicit request => authFunction.genericAuthWithNino("existingProtections") { nino =>
      amendmentTypeForm.bindFromRequest().fold(
        errors => {
          val form = errors.copy(errors = errors.errors.map { er => FormError(er.key, er.message) })
          Future.successful(BadRequest(removePsoDebits(form)))
        },
        success => {
          keyStoreConnector.fetchAndGetFormData[AmendProtectionModel](Strings.keyStoreAmendFetchString(success.protectionType, success.status)).flatMap {
            case Some(model) =>
              val updated = model.updatedProtection.copy(pensionDebits = None)
              val updatedTotal = updated.copy(relevantAmount = Some(Helpers.totalValue(updated)))
              val amendProtModel = AmendProtectionModel(model.originalProtection, updatedTotal)
              keyStoreConnector.saveFormData[AmendProtectionModel](Strings.keyStoreProtectionName(updated), amendProtModel).map {
                _ => Redirect(routes.AmendsController.amendsSummary(updated.protectionType.get.toLowerCase, updated.status.get.toLowerCase))
              }

            case None =>
              logger.warn(s"Could not retrieve Amend Protection Model for user with nino $nino when submitting a removal of a pension debit")
              Future.successful(InternalServerError(technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache"))
          }
        }
      )

    }
  }
  val submitAmendPsoDetails = Action.async { implicit request => authFunction.genericAuthWithNino("existingProtections") { nino =>
      amendPsoDetailsForm.bindFromRequest().fold(
        errors => {
          val form = errors.copy(errors = errors.errors.map { er => FormError(er.key, er.message) })
          Future.successful(BadRequest(amendPsoDetails(form)))
        },
        success => {
            val details = createPsoDetailsList(success)
            for {
              amendModel <- keyStoreConnector.fetchAndGetFormData[AmendProtectionModel](Strings.keyStoreAmendFetchString(success.protectionType, success.status))
              storedModel <- updateAndSaveAmendModelWithPso(details, amendModel, Strings.keyStoreAmendFetchString(success.protectionType, success.status))
            } yield Redirect(routes.AmendsController.amendsSummary(success.protectionType.toLowerCase, success.status.toLowerCase))
        }
      )
    }
  }

  def amendsSummary(protectionType: String, status: String): Action[AnyContent] = Action.async { implicit request =>
    implicit val lang = mcc.messagesApi.preferred(request).lang
     authFunction.genericAuthWithNino("existingProtections") { nino =>
      val protectionKey = Strings.keyStoreAmendFetchString(protectionType, status)
      keyStoreConnector.fetchAndGetFormData[AmendProtectionModel](protectionKey).map {
        case Some(amendModel) =>
            Ok(amendSummary(
            displayConstructors.createAmendDisplayModel(amendModel),
            status,
            amendmentTypeForm.fill(AmendmentTypeModel(protectionType, status))
          ))
        case _ =>
          logger.warn(s"Could not retrieve amend protection model for user with nino $nino when loading the amend summary page")
          InternalServerError(technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
      }
    }
  }

  def amendmentOutcome: Action[AnyContent] = Action.async { implicit request =>
     authFunction.genericAuthWithNino("existingProtections") { nino =>
      for {
        modelAR <- keyStoreConnector.fetchAndGetFormData[AmendResponseModel]("amendResponseModel")
        modelGA <- keyStoreConnector.fetchAndGetFormData[AmendsGAModel]("AmendsGA")
        result <- amendmentOutcomeResult(modelAR, modelGA, nino)
      } yield result
    }
  }

  def amendmentOutcomeResult(modelAR: Option[AmendResponseModel], modelGA: Option[AmendsGAModel], nino: String)
                            (implicit request:Request[AnyContent]):Future[Result] = {
    if (modelGA.isEmpty) {
        logger.warn(s"Unable to retrieve amendsGAModel from keyStore for user nino :$nino")
      }
      Future(modelAR.map {
        case model => {
          val id = model.protection.notificationId.getOrElse {
            throw new Exceptions.RequiredValueNotDefinedException("amendmentOutcome", "notificationId")
          }
          if (Constants.activeAmendmentCodes.contains(id)) {
            keyStoreConnector.saveData[ProtectionModel]("openProtection", model.protection)
            Ok(outcomeActive(displayConstructors.createActiveAmendResponseDisplayModel(model), modelGA))
          } else {
            Ok(outcomeInactive(displayConstructors.createInactiveAmendResponseDisplayModel(model), modelGA))
          }
        }
      }.getOrElse {
        logger.warn(s"Unable to retrieve amendment outcome model from keyStore for user nino :$nino")
        InternalServerError(technicalError(ApplicationType.existingProtections.toString))
          .withHeaders(CACHE_CONTROL -> "no-cache")
      })
    }

  def amendPensionsTakenBefore(protectionType: String, status: String): Action[AnyContent] = Action.async { implicit request =>
     authFunction.genericAuthWithNino("existingProtections") { nino =>
      amendRoute(AmendJourney.pensionTakenBefore, protectionType, status, nino).apply(request)
    }
  }

  def amendPensionsTakenBetween(protectionType: String, status: String): Action[AnyContent] = Action.async { implicit request =>
     authFunction.genericAuthWithNino("existingProtections") { nino =>
      amendRoute(AmendJourney.pensionTakenBetween, protectionType, status, nino).apply(request)
    }
  }

  def amendOverseasPensions(protectionType: String, status: String): Action[AnyContent] = Action.async { implicit request =>
     authFunction.genericAuthWithNino("existingProtections") { nino =>
      amendRoute(AmendJourney.overseasPension, protectionType, status, nino).apply(request)
    }
  }

  private def amendRoute(journey: AmendJourney.Value, protectionType: String, status: String, nino: String) = Action.async { implicit request =>
    import AmendJourney._
    keyStoreConnector.fetchAndGetFormData[AmendProtectionModel](Strings.keyStoreAmendFetchString(protectionType, status)).map {

      case Some(data) =>
        getRouteUsingModel(
          journey match {
            case `currentPension` =>
              AmendCurrentPensionModel(Some(Display.currencyInputDisplayFormat(data.updatedProtection.uncrystallisedRights.getOrElse[Double](0))), protectionType, status)
            case `pensionTakenBefore` =>
              val yesNoValue = if (data.updatedProtection.preADayPensionInPayment.getOrElse[Double](0) > 0) "yes" else "no"
              AmendPensionsTakenBeforeModel(yesNoValue,
                Some(Display.currencyInputDisplayFormat(data.updatedProtection.preADayPensionInPayment.getOrElse[Double](0))),
                protectionType,
                status)
            case `pensionTakenBetween` =>
              val yesNoValue = if (data.updatedProtection.postADayBenefitCrystallisationEvents.getOrElse[Double](0) > 0) "yes" else "no"
              AmendPensionsTakenBetweenModel(
                yesNoValue,
                Some(Display.currencyInputDisplayFormat(data.updatedProtection.postADayBenefitCrystallisationEvents.getOrElse[Double](0))),
                protectionType,
                status)
            case `overseasPension` =>
              val yesNoValue = if (data.updatedProtection.nonUKRights.getOrElse[Double](0) > 0) "yes" else "no"
              AmendOverseasPensionsModel(yesNoValue, Some(Display.currencyInputDisplayFormat(data.updatedProtection.nonUKRights.getOrElse[Double](0))), protectionType, status)
          }
        )(request)
      case _ =>
        logger.warn(s"Could not retrieve amend protection model for user with nino $nino when loading the amend $journey page")
        InternalServerError(technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
    }
  }

  private[controllers] def getRouteUsingModel(model: AmendValueModel)(implicit request: Request[AnyContent]) = {
    model match {
      case AmendCurrentPensionModel(_,"ip2016",_) =>
        Ok(amendCurrentPensions(amendCurrentPensionForm.fill(model.asInstanceOf[AmendCurrentPensionModel])))

      case AmendCurrentPensionModel(_,"ip2014",_) =>
        Ok(amendIP14CurrentPensions(amendCurrentPensionForm.fill(model.asInstanceOf[AmendCurrentPensionModel])))

      case AmendPensionsTakenBeforeModel(_,_,"ip2016",_) =>
        Ok(amendPensionsTakenBefore(amendPensionsTakenBeforeForm.fill(model.asInstanceOf[AmendPensionsTakenBeforeModel])))

      case AmendPensionsTakenBeforeModel(_,_,"ip2014",_) =>
        Ok(amendIP14PensionsTakenBefore(amendPensionsTakenBeforeForm.fill(model.asInstanceOf[AmendPensionsTakenBeforeModel])))

      case AmendPensionsTakenBetweenModel(_,_,"ip2016",_) =>
        Ok(amendPensionsTakenBetween(amendPensionsTakenBetweenForm.fill(model.asInstanceOf[AmendPensionsTakenBetweenModel])))

      case AmendPensionsTakenBetweenModel(_,_,"ip2014",_) =>
        Ok(amendIP14PensionsTakenBetween(amendPensionsTakenBetweenForm.fill(model.asInstanceOf[AmendPensionsTakenBetweenModel])))

      case AmendOverseasPensionsModel(_,_,"ip2016",_) =>
        Ok(amendOverseasPensions(amendOverseasPensionsForm.fill(model.asInstanceOf[AmendOverseasPensionsModel])))

      case AmendOverseasPensionsModel(_,_,"ip2014",_) =>
        Ok(amendIP14OverseasPensions(amendOverseasPensionsForm.fill(model.asInstanceOf[AmendOverseasPensionsModel])))
    }
  }

  def amendCurrentPensions(protectionType: String, status: String): Action[AnyContent] = Action.async { implicit request =>
     authFunction.genericAuthWithNino("existingProtections") { nino =>
      amendRoute(AmendJourney.currentPension, protectionType, status, nino).apply(request)
    }
  }

  def amendPsoDetails(protectionType: String, status: String): Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithNino("existingProtections") { nino =>
      keyStoreConnector.fetchAndGetFormData[AmendProtectionModel](Strings.keyStoreAmendFetchString(protectionType, status)).map {
        case Some(amendProtectionModel) =>
          amendProtectionModel.updatedProtection.pensionDebits match {
            case Some(debits) =>
              routeFromPensionDebitsList(debits, protectionType, status, nino)
            case None =>
              Ok(amendPsoDetails(amendPsoDetailsForm.fill(createBlankAmendPsoDetailsModel(protectionType, status))))
          }
        case _ =>
          logger.warn(s"Could not retrieve amend protection model for user with nino $nino when loading the amend PSO details page")
          InternalServerError(technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
      }
    }
  }

  def routeFromPensionDebitsList(debits: Seq[PensionDebitModel], protectionType: String, status: String, nino: String)(implicit request: Request[AnyContent]): Result = {
    debits.length match {
        case 0 => Ok(amendPsoDetails(amendPsoDetailsForm.fill(createBlankAmendPsoDetailsModel(protectionType, status))))
        case 1 => Ok(amendPsoDetails(amendPsoDetailsForm.fill(createAmendPsoDetailsModel(debits.head, protectionType, status))))
        case num => {
          logger.warn(s"$num pension debits recorded for user nino $nino during amend journey")
          InternalServerError(technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
        }
      }
    }

  def createAmendPsoDetailsModel(psoDetails: PensionDebitModel, protectionType: String, status: String): AmendPSODetailsModel = {
    val (day, month, year) = Dates.extractDMYFromAPIDateString(psoDetails.startDate)
    AmendPSODetailsModel(Some(day), Some(month), Some(year), Some(Display.currencyInputDisplayFormat(psoDetails.amount)), protectionType, status, existingPSO = true)
  }

  def createBlankAmendPsoDetailsModel(protectionType: String, status: String): AmendPSODetailsModel = {
    AmendPSODetailsModel(psoDay = None, psoMonth = None, psoYear = None, psoAmt = None, protectionType, status, existingPSO = false)
  }

  def removePso(protectionType: String, status: String): Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithNino("existingProtections") { nino =>
      keyStoreConnector.fetchAndGetFormData[AmendProtectionModel](Strings.keyStoreAmendFetchString(protectionType, status)).map {
        case Some(model) =>
          Ok(removePsoDebits(amendmentTypeForm.fill(AmendmentTypeModel(protectionType, status))))
        case _ =>
          logger.warn(s"Could not retrieve Amend ProtectionModel for user with nino $nino when removing the new pension debit")
          InternalServerError(technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
      }
    }
  }

  private def routeViaMCNeededCheck(response: HttpResponse, nino: String)(implicit request: Request[AnyContent]): Future[Result] = {
    response.status match {
      case 409 => {
        logger.warn(s"conflict response returned for amend request for user nino $nino")
        Future.successful(InternalServerError(technicalError(ApplicationType.existingProtections.toString))
          .withHeaders(CACHE_CONTROL -> "no-cache"))
      }
      case 423 =>
        logger.info(s"locked reponse returned for amend request for user nino $nino")
        Future.successful(Locked(manualCorrespondenceNeeded()))
      case _ => saveAndRedirectToDisplay(response, nino)
    }
  }

  def saveAndRedirectToDisplay(response: HttpResponse, nino: String)(implicit request: Request[AnyContent]): Future[Result] = {
    responseConstructors.createAmendResponseModelFromJson(response.json).map {
      model =>
        if (model.protection.notificationId.isDefined) {
          keyStoreConnector.saveData[AmendResponseModel]("amendResponseModel", model).map {
            cacheMap => Redirect(routes.AmendsController.amendmentOutcome)
          }
        } else {
          logger.warn(s"No notification ID found in the AmendResponseModel for user with nino $nino")
          Future.successful(InternalServerError(noNotificationId()).withHeaders(CACHE_CONTROL -> "no-cache"))
        }
    }.getOrElse {
      logger.warn(s"Unable to create Amend Response Model from PLA response for user nino: $nino")
      Future.successful(InternalServerError(technicalError(ApplicationType.existingProtections.toString))
        .withHeaders(CACHE_CONTROL -> "no-cache"))
    }
  }

  private[controllers] def createPsoDetailsList(formModel: AmendPSODetailsModel): Option[List[PensionDebitModel]] = {
    val date = Dates.apiDateFormat(formModel.psoDay.get, formModel.psoMonth.get, formModel.psoYear.get)
    val amt = formModel.psoAmt.getOrElse{ throw new Exceptions.RequiredValueNotDefinedException("createPsoDetailsList", "psoAmt") }
    Some(List(PensionDebitModel(startDate = date, amount = amt.toDouble)))
  }

  private def updateAndSaveAmendModelWithPso(debits: Option[List[PensionDebitModel]], amendModelOption: Option[AmendProtectionModel], key: String)(implicit request: Request[AnyContent]) = {
    val amendModel = amendModelOption.getOrElse {throw new Exceptions.RequiredValueNotDefinedException("updateAndSaveAmendModelWithPso", "amendModel")}
    val newUpdatedProtection = amendModel.updatedProtection.copy(pensionDebits = debits)
    keyStoreConnector.saveData[AmendProtectionModel](key, amendModel.copy(updatedProtection = newUpdatedProtection))
  }

  private object AmendJourney extends Enumeration {
    val currentPension = Value
    val pensionTakenBefore = Value
    val pensionTakenBetween = Value
    val overseasPension = Value
  }

}
