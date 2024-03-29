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

class AmendsController @Inject()(val sessionCacheService: SessionCacheService,
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
                                 amendPensionsWorthBefore: pages.amends.amendPensionsWorthBefore,
                                 amendPensionsTakenBetween: pages.amends.amendPensionsTakenBetween,
                                 amendPensionsUsedBetween: pages.amends.amendPensionsUsedBetween,
                                 amendIP14CurrentPensions: pages.amends.amendIP14CurrentPensions,
                                 amendIP14PensionsTakenBefore: pages.amends.amendIP14PensionsTakenBefore,
                                 amendIP14PensionsWorthBefore: pages.amends.amendIP14PensionsWorthBefore,
                                 amendIP14PensionsTakenBetween: pages.amends.amendIP14PensionsTakenBetween,
                                 amendIP14PensionsUsedBetween: pages.amends.amendIP14PensionsUsedBetween,
                                 amendOverseasPensions: pages.amends.amendOverseasPensions,
                                 amendIP14OverseasPensions: pages.amends.amendIP14OverseasPensions,
                                 outcomeActive: views.html.pages.amends.outcomeActive,
                                 outcomeInactive: views.html.pages.amends.outcomeInactive,
                                 removePsoDebits: pages.amends.removePsoDebits,
                                 amendSummary: views.html.pages.amends.amendSummary)
                                (implicit val appConfig: FrontendAppConfig,
                                 implicit val partialRetriever: FormPartialRetriever,
                                 implicit val formWithCSRF: FormWithCSRF,
                                 implicit val plaContext: PlaContext,
                                 implicit val ec: ExecutionContext)
extends FrontendController(mcc) with I18nSupport with Logging{

  val amendProtection: Action[AnyContent] = Action.async { implicit request =>
     authFunction.genericAuthWithNino("existingProtections") { nino =>
      amendmentTypeForm.bindFromRequest().fold(
        errors => {
          logger.warn(s"Couldn't bind protection type or status to amend request for user with nino $nino")
          Future.successful(InternalServerError(technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache"))
        },
        success => for {
          protectionAmendment <- sessionCacheService.fetchAndGetFormData[AmendProtectionModel](
            Strings.cacheAmendFetchString(success.protectionType, success.status))
          saveAmendsGA <- sessionCacheService.saveFormData[AmendsGAModel]("AmendsGA", AmendsGAConstructor.identifyAmendsChanges(
            protectionAmendment.get.updatedProtection, protectionAmendment.get.originalProtection))
          response <- plaConnector.amendProtection(nino, protectionAmendment.get.updatedProtection)
          result <- routeViaMCNeededCheck(response, nino)
        } yield result
      )
    }
  }
  val submitAmendPensionsTakenBefore: Action[AnyContent] = Action.async {
    implicit request =>
       authFunction.genericAuthWithNino("existingProtections") { nino =>
        amendPensionsTakenBeforeForm.bindFromRequest().fold(
          errors => {
            val form = errors.copy(errors = errors.errors.map { er => FormError(er.key, er.message) })
            Future.successful(BadRequest(amendPensionsTakenBefore(form)))
          },
          success => {
              sessionCacheService.fetchAndGetFormData[AmendProtectionModel](Strings.cacheAmendFetchString(success.protectionType, success.status)).flatMap {
                case Some(model) =>
                  success.amendedPensionsTakenBefore match {
                    case "yes" => Future.successful(Redirect(routes.AmendsController.amendPensionsWorthBefore(
                      success.protectionType.toLowerCase, success.status.toLowerCase)))
                    case "no" =>
                      val updated = model.updatedProtection.copy(preADayPensionInPayment = Some(0))
                      val updatedTotal = updated.copy(relevantAmount = Some(Helpers.totalValue(updated)))
                      val amendProtModel = AmendProtectionModel(model.originalProtection, updatedTotal)

                      sessionCacheService.saveFormData[AmendProtectionModel](Strings.cacheProtectionName(updated), amendProtModel).map {
                        _ => Redirect(routes.AmendsController.amendsSummary(updated.protectionType.get.toLowerCase, updated.status.get.toLowerCase))
                      }
                  }

                case _ =>
                  logger.warn(s"Could not retrieve amend protection model for user with nino $nino after submitting amend pensions taken before")
                  Future.successful(InternalServerError(technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache"))

            }
          }
        )
      }
  }
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
              logger.warn(s"Could not retrieve amend protection model for user with nino $nino after submitting amend pensions taken before amount")
              Future.successful(InternalServerError(technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache"))
          }
        }
      )
    }
  }
  val submitAmendPensionsTakenBetween: Action[AnyContent] = Action.async { implicit request => authFunction.genericAuthWithNino("existingProtections") { nino =>
      amendPensionsTakenBetweenForm.bindFromRequest().fold(
        errors => {
          val form = errors.copy(errors = errors.errors.map { er => FormError(er.key, er.message) })
          Future.successful(BadRequest(amendPensionsTakenBetween(form)))
        },
        success => {
            sessionCacheService.fetchAndGetFormData[AmendProtectionModel](Strings.cacheAmendFetchString(success.protectionType, success.status)).flatMap {
              case Some(model) =>
                 success.amendedPensionsTakenBetween match {
                    case "yes" => Future.successful(Redirect(routes.AmendsController.amendPensionsUsedBetween(success.protectionType.toLowerCase, success.status.toLowerCase)))
                    case "no" =>
                      val updated = model.updatedProtection.copy(postADayBenefitCrystallisationEvents = Some(0))
                      val updatedTotal = updated.copy(relevantAmount = Some(Helpers.totalValue(updated)))
                      val amendProtModel = AmendProtectionModel(model.originalProtection, updatedTotal)

                      sessionCacheService.saveFormData[AmendProtectionModel](Strings.cacheProtectionName(updated), amendProtModel).map {
                        _ =>
                          Redirect(routes.AmendsController.amendsSummary(updated.protectionType.get.toLowerCase, updated.status.get.toLowerCase))
                      }

                 }

              case _ =>
                logger.warn(s"Could not retrieve amend protection model for user with nino $nino after submitting amend pensions taken between")
                Future.successful(InternalServerError(technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache"))
            }
        }
      )

    }
  }
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
  val submitAmendOverseasPensions: Action[AnyContent] = Action.async {
    implicit request =>
       authFunction.genericAuthWithNino("existingProtections") { nino =>
        amendOverseasPensionsForm.bindFromRequest().fold(
          errors => {
            val form = errors.copy(errors = errors.errors.map { er => FormError(er.key, er.message) })
            Future.successful(BadRequest(amendOverseasPensions(form)))
          },
          success => {
              sessionCacheService.fetchAndGetFormData[AmendProtectionModel](Strings.cacheAmendFetchString(success.protectionType, success.status)).flatMap {
                case Some(model) =>
                  val updatedAmount = success.amendedOverseasPensions match {
                    case "yes" => success.amendedOverseasPensionsAmt.get.toDouble
                    case "no" => 0.asInstanceOf[Double]
                  }
                  val updated = model.updatedProtection.copy(nonUKRights = Some(updatedAmount))
                  val updatedTotal = updated.copy(relevantAmount = Some(Helpers.totalValue(updated)))
                  val amendProtModel = AmendProtectionModel(model.originalProtection, updatedTotal)

                  sessionCacheService.saveFormData[AmendProtectionModel](Strings.cacheProtectionName(updated), amendProtModel).map {
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
  val submitRemovePso: Action[AnyContent] = Action.async { implicit request => authFunction.genericAuthWithNino("existingProtections") { nino =>
      amendmentTypeForm.bindFromRequest().fold(
        errors => {
          val form = errors.copy(errors = errors.errors.map { er => FormError(er.key, er.message) })
          Future.successful(BadRequest(removePsoDebits(form)))
        },
        success => {
          sessionCacheService.fetchAndGetFormData[AmendProtectionModel](Strings.cacheAmendFetchString(success.protectionType, success.status)).flatMap {
            case Some(model) =>
              val updated = model.updatedProtection.copy(pensionDebits = None)
              val updatedTotal = updated.copy(relevantAmount = Some(Helpers.totalValue(updated)))
              val amendProtModel = AmendProtectionModel(model.originalProtection, updatedTotal)
              sessionCacheService.saveFormData[AmendProtectionModel](Strings.cacheProtectionName(updated), amendProtModel).map {
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
  def submitAmendPsoDetails(protectionType: String, status: String, existingPSO: Boolean): Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithNino("existingProtections") { nino =>
      amendPsoDetailsForm(protectionType).bindFromRequest().fold(
        formWithErrors => {
          Future.successful(BadRequest(amendPsoDetails(formWithErrors, protectionType, status, existingPSO)))
        },
        success => {
            val details = createPsoDetailsList(success)
            for {
              amendModel <- sessionCacheService.fetchAndGetFormData[AmendProtectionModel](Strings.cacheAmendFetchString(protectionType, status))
              storedModel <- updateAndSaveAmendModelWithPso(details, amendModel, Strings.cacheAmendFetchString(protectionType, status))
            } yield Redirect(routes.AmendsController.amendsSummary(protectionType.toLowerCase, status.toLowerCase))
        }
      )
    }
  }

  def amendsSummary(protectionType: String, status: String): Action[AnyContent] = Action.async { implicit request =>
    implicit val lang: Lang = mcc.messagesApi.preferred(request).lang
     authFunction.genericAuthWithNino("existingProtections") { nino =>
      val protectionKey = Strings.cacheAmendFetchString(protectionType, status)
      sessionCacheService.fetchAndGetFormData[AmendProtectionModel](protectionKey).map {
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
        modelAR <- sessionCacheService.fetchAndGetFormData[AmendResponseModel]("amendResponseModel")
        modelGA <- sessionCacheService.fetchAndGetFormData[AmendsGAModel]("AmendsGA")
        result <- amendmentOutcomeResult(modelAR, modelGA, nino)
      } yield result
    }
  }

  def amendmentOutcomeResult(modelAR: Option[AmendResponseModel], modelGA: Option[AmendsGAModel], nino: String)
                            (implicit request:Request[AnyContent]):Future[Result] = {
    if (modelGA.isEmpty) {
        logger.warn(s"Unable to retrieve amendsGAModel from cache for user nino :$nino")
      }
      Future(modelAR.map {
        model => {
          val id = model.protection.notificationId.getOrElse {
            throw new Exceptions.RequiredValueNotDefinedException("amendmentOutcome", "notificationId")
          }
          if (Constants.activeAmendmentCodes.contains(id)) {
            sessionCacheService.saveFormData[ProtectionModel]("openProtection", model.protection)
            Ok(outcomeActive(displayConstructors.createActiveAmendResponseDisplayModel(model), modelGA))
          } else {
            Ok(outcomeInactive(displayConstructors.createInactiveAmendResponseDisplayModel(model), modelGA))
          }
        }
      }.getOrElse {
        logger.warn(s"Unable to retrieve amendment outcome model from cache for user nino :$nino")
        InternalServerError(technicalError(ApplicationType.existingProtections.toString))
          .withHeaders(CACHE_CONTROL -> "no-cache")
      })
    }

  def amendPensionsTakenBefore(protectionType: String, status: String): Action[AnyContent] = Action.async { implicit request =>
     authFunction.genericAuthWithNino("existingProtections") { nino =>
      amendRoute(AmendJourney.pensionTakenBefore, protectionType, status, nino).apply(request)
    }
  }

  def amendPensionsWorthBefore(protectionType: String, status: String): Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithNino("existingProtections") { nino =>
      amendRoute(AmendJourney.pensionWorthBefore, protectionType, status, nino).apply(request)
    }
  }

  def amendPensionsTakenBetween(protectionType: String, status: String): Action[AnyContent] = Action.async { implicit request =>
     authFunction.genericAuthWithNino("existingProtections") { nino =>
      amendRoute(AmendJourney.pensionTakenBetween, protectionType, status, nino).apply(request)
    }
  }

  def amendPensionsUsedBetween(protectionType: String, status: String): Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithNino("existingProtections") { nino =>
      amendRoute(AmendJourney.pensionUsedBetween, protectionType, status, nino).apply(request)
    }
  }

  def amendOverseasPensions(protectionType: String, status: String): Action[AnyContent] = Action.async { implicit request =>
     authFunction.genericAuthWithNino("existingProtections") { nino =>
      amendRoute(AmendJourney.overseasPension, protectionType, status, nino).apply(request)
    }
  }

  private def amendRoute(journey: AmendJourney.Value, protectionType: String, status: String, nino: String) = Action.async { implicit request =>
    import AmendJourney._
    sessionCacheService.fetchAndGetFormData[AmendProtectionModel](Strings.cacheAmendFetchString(protectionType, status)).map {

      case Some(data) =>
        getRouteUsingModel(
          journey match {
            case `currentPension` =>
              AmendCurrentPensionModel(Some(Display.currencyInputDisplayFormat(data.updatedProtection.uncrystallisedRights.getOrElse[Double](0))), protectionType, status)
            case `pensionTakenBefore` =>
              val yesNoValue = if (data.updatedProtection.preADayPensionInPayment.getOrElse[Double](0) > 0) "yes" else "no"
              AmendPensionsTakenBeforeModel(yesNoValue,
                protectionType,
                status)
            case `pensionWorthBefore` =>
              AmendPensionsWorthBeforeModel(
                Some(Display.currencyInputDisplayFormat(data.updatedProtection.preADayPensionInPayment.getOrElse[Double](0))),
                protectionType,
                status
              )
            case `pensionTakenBetween` =>
              val yesNoValue = if (data.updatedProtection.postADayBenefitCrystallisationEvents.getOrElse[Double](0) > 0) "yes" else "no"
              AmendPensionsTakenBetweenModel(
                yesNoValue,
                protectionType,
                status)
            case `pensionUsedBetween` =>
              AmendPensionsUsedBetweenModel(
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

      case AmendPensionsTakenBeforeModel(_,"ip2016",_) =>
        Ok(amendPensionsTakenBefore(amendPensionsTakenBeforeForm.fill(model.asInstanceOf[AmendPensionsTakenBeforeModel])))

      case AmendPensionsWorthBeforeModel(_, "ip2016", _) =>
        Ok(amendPensionsWorthBefore(amendPensionsWorthBeforeForm.fill(model.asInstanceOf[AmendPensionsWorthBeforeModel])))

      case AmendPensionsTakenBeforeModel(_,"ip2014",_) =>
        Ok(amendIP14PensionsTakenBefore(amendPensionsTakenBeforeForm.fill(model.asInstanceOf[AmendPensionsTakenBeforeModel])))

      case AmendPensionsWorthBeforeModel(_,"ip2014",_) =>
        Ok(amendIP14PensionsWorthBefore(amendPensionsWorthBeforeForm.fill(model.asInstanceOf[AmendPensionsWorthBeforeModel])))

      case AmendPensionsTakenBetweenModel(_,"ip2016",_) =>
        Ok(amendPensionsTakenBetween(amendPensionsTakenBetweenForm.fill(model.asInstanceOf[AmendPensionsTakenBetweenModel])))

      case AmendPensionsUsedBetweenModel(_,"ip2016",_) =>
        Ok(amendPensionsUsedBetween(amendPensionsUsedBetweenForm.fill(model.asInstanceOf[AmendPensionsUsedBetweenModel])))

      case AmendPensionsTakenBetweenModel(_,"ip2014",_) =>
        Ok(amendIP14PensionsTakenBetween(amendPensionsTakenBetweenForm.fill(model.asInstanceOf[AmendPensionsTakenBetweenModel])))

      case AmendPensionsUsedBetweenModel(_,"ip2014",_) =>
        Ok(amendIP14PensionsUsedBetween(amendPensionsUsedBetweenForm.fill(model.asInstanceOf[AmendPensionsUsedBetweenModel])))

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
      sessionCacheService.fetchAndGetFormData[AmendProtectionModel](Strings.cacheAmendFetchString(protectionType, status)).map {
        case Some(amendProtectionModel) =>
          amendProtectionModel.updatedProtection.pensionDebits match {
            case Some(debits) =>
              routeFromPensionDebitsList(debits, protectionType, status, nino)
            case None =>
              Ok(amendPsoDetails(amendPsoDetailsForm(protectionType), protectionType, status, existingPSO = false))
          }
        case _ =>
          logger.warn(s"Could not retrieve amend protection model for user with nino $nino when loading the amend PSO details page")
          InternalServerError(technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
      }
    }
  }

  def routeFromPensionDebitsList(debits: Seq[PensionDebitModel], protectionType: String, status: String, nino: String)(implicit request: Request[AnyContent]): Result = {
    debits.length match {
        case 0 => Ok(amendPsoDetails(amendPsoDetailsForm(protectionType), protectionType, status, existingPSO = false))
        case 1 => Ok(amendPsoDetails(amendPsoDetailsForm(protectionType).fill(createAmendPsoDetailsModel(debits.head)), protectionType, status, existingPSO = true))
        case num =>
          logger.warn(s"$num pension debits recorded for user nino $nino during amend journey")
          InternalServerError(technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
      }
    }

  def createAmendPsoDetailsModel(psoDetails: PensionDebitModel): AmendPSODetailsModel = {
    val (day, month, year) = Dates.extractDMYFromAPIDateString(psoDetails.startDate)
    val date = LocalDate.of(year, month, day)
    AmendPSODetailsModel(date, Some(Display.currencyInputDisplayFormat(psoDetails.amount)))
  }

  def removePso(protectionType: String, status: String): Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithNino("existingProtections") { nino =>
      sessionCacheService.fetchAndGetFormData[AmendProtectionModel](Strings.cacheAmendFetchString(protectionType, status)).map {
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
          sessionCacheService.saveFormData[AmendResponseModel]("amendResponseModel", model).map {
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
    val date = formModel.pso.toString
    val amt = formModel.psoAmt.getOrElse{ throw new Exceptions.RequiredValueNotDefinedException("createPsoDetailsList", "psoAmt") }
    Some(List(PensionDebitModel(startDate = date, amount = amt.toDouble)))
  }

  private def updateAndSaveAmendModelWithPso(debits: Option[List[PensionDebitModel]], amendModelOption: Option[AmendProtectionModel], key: String)
                                            (implicit request: Request[AnyContent]) = {
    val amendModel = amendModelOption.getOrElse {throw new Exceptions.RequiredValueNotDefinedException("updateAndSaveAmendModelWithPso", "amendModel")}
    val newUpdatedProtection = amendModel.updatedProtection.copy(pensionDebits = debits)
    sessionCacheService.saveFormData[AmendProtectionModel](key, amendModel.copy(updatedProtection = newUpdatedProtection))
  }

  private object AmendJourney extends Enumeration {
    val currentPension = Value
    val pensionTakenBefore = Value
    val pensionWorthBefore = Value
    val pensionTakenBetween = Value
    val pensionUsedBetween = Value
    val overseasPension = Value
  }

}
