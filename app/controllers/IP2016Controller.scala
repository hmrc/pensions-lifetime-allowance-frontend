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
import config.{FrontendAppConfig, PlaContext}
import forms.CurrentPensionsForm.currentPensionsForm
import forms.OverseasPensionsForm.overseasPensionsForm
import forms.PSODetailsForm.psoDetailsForm
import forms.PensionDebitsForm.pensionDebitsForm
import forms.PensionsTakenBeforeForm.pensionsTakenBeforeForm
import forms.PensionsWorthBeforeForm.pensionsWorthBeforeForm
import forms.PensionsTakenBetweenForm.pensionsTakenBetweenForm
import forms.PensionsUsedBetweenForm.pensionsUsedBetweenForm
import forms.PensionsTakenForm.pensionsTakenForm
import javax.inject.Inject
import models._
import play.api.Application
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionCacheService
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever
import views.html._

import scala.concurrent.{ExecutionContext, Future}

class IP2016Controller @Inject()(val sessionCacheService: SessionCacheService,
                                 mcc: MessagesControllerComponents,
                                 authFunction: AuthFunction,
                                 pensionsTaken: pages.ip2016.pensionsTaken,
                                 pensionsTakenBefore: pages.ip2016.pensionsTakenBefore,
                                 pensionsWorthBefore: pages.ip2016.pensionsWorthBefore,
                                 pensionsTakenBetween: pages.ip2016.pensionsTakenBetween,
                                 pensionsUsedBetween: pages.ip2016.pensionsUsedBetween,
                                 overseasPensions: pages.ip2016.overseasPensions,
                                 currentPensions: pages.ip2016.currentPensions,
                                 psoDetails: pages.ip2016.psoDetails,
                                 RemovePsoDetails: pages.ip2016.removePsoDetails,
                                 pensionDebits: pages.ip2016.pensionDebits)
                                (implicit val appConfig: FrontendAppConfig,
                                 implicit val partialRetriever: FormPartialRetriever,
                                 implicit val plaContext: PlaContext,
                                 implicit val formWithCSRF: FormWithCSRF,
                                 implicit val application: Application,
                                 implicit val ec: ExecutionContext) extends FrontendController(mcc) {

  lazy val postSignInRedirectUrl = appConfig.ipStartUrl

  //PENSIONS TAKEN
  def pensionsTaken: Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithoutNino("IP2016") {
      sessionCacheService.fetchAndGetFormData[PensionsTakenModel]("pensionsTaken").map {
        case Some(data) => Ok(pensionsTaken(pensionsTakenForm.fill(data)))
        case None => Ok(pensionsTaken(pensionsTakenForm))
      }
    }
  }

  def submitPensionsTaken: Action[AnyContent] = Action.async {
    implicit request =>
        authFunction.genericAuthWithoutNino("IP2016") {
        pensionsTakenForm.bindFromRequest().fold(
          errors => {
            val form = errors.copy(errors = errors.errors.map { er => FormError(er.key, Messages(er.message)) })
            Future.successful(BadRequest(pensionsTaken(form)))
          },
          success => {
            sessionCacheService.saveFormData("pensionsTaken", success).map {
              _ =>
                success.pensionsTaken.get match {
                  case "yes" => Redirect(routes.IP2016Controller.pensionsTakenBefore)
                  case "no" => Redirect(routes.IP2016Controller.overseasPensions)
                }
            }

          }
        )
      }
  }

  //PENSIONS TAKEN BEFORE
  def pensionsTakenBefore: Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithoutNino("IP2016") {
      sessionCacheService.fetchAndGetFormData[PensionsTakenBeforeModel]("pensionsTakenBefore").map {
        case Some(data) => Ok(pensionsTakenBefore(pensionsTakenBeforeForm.fill(data)))
        case _ => Ok(pensionsTakenBefore(pensionsTakenBeforeForm))
      }
    }
  }

  def submitPensionsTakenBefore: Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithoutNino("IP2016") {
      pensionsTakenBeforeForm.bindFromRequest().fold(
        errors => {
          val form = errors.copy(errors = errors.errors.map { er => FormError(er.key, Messages(er.message)) })
          Future.successful(BadRequest(pensionsTakenBefore(form)))
        },
        success => {
          sessionCacheService.saveFormData("pensionsTakenBefore", success).map {
            _ =>
              success.pensionsTakenBefore match {
                case "yes" => Redirect(routes.IP2016Controller.pensionsWorthBefore)
                case "no" => Redirect(routes.IP2016Controller.pensionsTakenBetween)
              }
          }
        }
      )
    }
  }

  //PENSIONS WORTH BEFORE
  def pensionsWorthBefore: Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithoutNino("IP2016") {
      sessionCacheService.fetchAndGetFormData[PensionsWorthBeforeModel]("pensionsWorthBefore").map {
        case Some(data) => Ok(pensionsWorthBefore(pensionsWorthBeforeForm.fill(data)))
        case _ => Ok(pensionsWorthBefore(pensionsWorthBeforeForm))
      }
    }
  }

  def submitPensionsWorthBefore: Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithoutNino("IP2016") {
      pensionsWorthBeforeForm.bindFromRequest().fold(
        errors => {
          val form = errors.copy(errors = errors.errors.map { er => FormError(er.key, Messages(er.message)) })
          Future.successful(BadRequest(pensionsWorthBefore(form)))
        },
        success => {
          sessionCacheService.saveFormData("pensionsWorthBefore", success).flatMap {
              _ => Future.successful(Redirect(routes.IP2016Controller.pensionsTakenBetween))
            }
          }
      )
    }
  }


  //PENSIONS TAKEN BETWEEN
  def pensionsTakenBetween: Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithoutNino("IP2016") {
      sessionCacheService.fetchAndGetFormData[PensionsTakenBetweenModel]("pensionsTakenBetween").map {
        case Some(data) => Ok(pensionsTakenBetween(pensionsTakenBetweenForm.fill(data)))
        case _ => Ok(pensionsTakenBetween(pensionsTakenBetweenForm))
      }
    }
  }

  def submitPensionsTakenBetween: Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithoutNino("IP2016") {
      pensionsTakenBetweenForm.bindFromRequest().fold(
        errors => {
          val form = errors.copy(errors = errors.errors.map { er => FormError(er.key, Messages(er.message)) })
          Future.successful(BadRequest(pensionsTakenBetween(form)))
        },
        success => {
          sessionCacheService.saveFormData("pensionsTakenBetween", success).map {
            _ =>
              success.pensionsTakenBetween match {
                case "yes" => Redirect(routes.IP2016Controller.pensionsUsedBetween)
                case "no" => Redirect(routes.IP2016Controller.overseasPensions)
              }
          }
        }
      )
    }
  }

  //PENSIONS USED BETWEEN
  def pensionsUsedBetween:  Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithoutNino("IP2016") {
      sessionCacheService.fetchAndGetFormData[PensionsUsedBetweenModel]("pensionsUsedBetween").map {
        case Some(data) => Ok(pensionsUsedBetween(pensionsUsedBetweenForm.fill(data)))
        case _ => Ok(pensionsUsedBetween(pensionsUsedBetweenForm))
      }
    }
  }

  def submitPensionsUsedBetween: Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithoutNino("IP2016") {
      pensionsUsedBetweenForm.bindFromRequest().fold(
        errors => {
          val form = errors.copy(errors = errors.errors.map { er => FormError(er.key, Messages(er.message)) })
          Future.successful(BadRequest(pensionsUsedBetween(form)))
        },
        success => {
          sessionCacheService.saveFormData("pensionsUsedBetween", success).flatMap {
            _ => Future.successful(Redirect(routes.IP2016Controller.overseasPensions))
          }
        }
      )
    }
  }

  //OVERSEAS PENSIONS
  def overseasPensions: Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithoutNino("IP2016") {
      sessionCacheService.fetchAndGetFormData[OverseasPensionsModel]("overseasPensions").map {
        case Some(data) => Ok(overseasPensions(overseasPensionsForm.fill(data)))
        case _ => Ok(overseasPensions(overseasPensionsForm))
      }
    }
  }

  def submitOverseasPensions: Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithoutNino("IP2016") {
      overseasPensionsForm.bindFromRequest().fold(
        errors => {
          val form = errors.copy(errors = errors.errors.map { er => FormError(er.key, Messages(er.message)) })
          Future.successful(BadRequest(overseasPensions(form)))
        },
        success => {
          sessionCacheService.saveFormData("overseasPensions", success).flatMap {
            _ => Future.successful(Redirect(routes.IP2016Controller.currentPensions))
          }
        }
      )
    }
  }


  //CURRENT PENSIONS
  def currentPensions: Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithoutNino("IP2016") {
      sessionCacheService.fetchAndGetFormData[CurrentPensionsModel]("currentPensions").map {
        case Some(data) => Ok(currentPensions(currentPensionsForm.fill(data)))
        case _ => Ok(currentPensions(currentPensionsForm))
      }
    }
  }

  def submitCurrentPensions: Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithoutNino("IP2016") {
      currentPensionsForm.bindFromRequest().fold(
        errors => {
          val form = errors.copy(errors = errors.errors.map { er => FormError(er.key, Messages(er.message)) })
          Future.successful(BadRequest(currentPensions(form)))
        },
        success => {
          sessionCacheService.saveFormData("currentPensions", success).flatMap {
            _ => Future.successful(Redirect(routes.IP2016Controller.pensionDebits))
          }
        }
      )
    }
  }


  //PENSION DEBITS
  def pensionDebits: Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithoutNino("IP2016") {
      sessionCacheService.fetchAndGetFormData[PensionDebitsModel]("pensionDebits").map {
        case Some(data) => Ok(pensionDebits(pensionDebitsForm.fill(data)))
        case None => Ok(pensionDebits(pensionDebitsForm))
      }
    }
  }

  def submitPensionDebits: Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithoutNino("IP2016") {
      pensionDebitsForm.bindFromRequest().fold(
        errors => {
          val form = errors.copy(errors = errors.errors.map { er => FormError(er.key, Messages(er.message)) })
          Future.successful(BadRequest(pensionDebits(form)))
        },
        success => {
          sessionCacheService.saveFormData("pensionDebits", success).map {
            _ =>
              success.pensionDebits.get match {
                case "yes" => Redirect(routes.IP2016Controller.psoDetails)
                case "no" => Redirect(routes.SummaryController.summaryIP16)

              }
          }
        }
      )
    }
  }

  //PENSION SHARING ORDER DETAILS
  def psoDetails: Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithoutNino("IP2016") {
      sessionCacheService.fetchAndGetFormData[PSODetailsModel]("psoDetails").map {
        case Some(data) => Ok(psoDetails(psoDetailsForm().fill(data)))
        case _ => Ok(psoDetails(psoDetailsForm()))
      }
    }
  }

  def submitPSODetails: Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithoutNino("IP2016") {
      psoDetailsForm().bindFromRequest().fold(
        errors => {
          val form = errors.copy(errors = errors.errors.map { er => FormError(er.key, Messages(er.message)) })
          Future.successful(BadRequest(psoDetails(form)))
        },
        form => {
          sessionCacheService.saveFormData(s"psoDetails", form).flatMap {
            _ => Future.successful(Redirect(routes.SummaryController.summaryIP16))
          }
        }
      )
    }
  }

  def removePsoDetails: Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithoutNino("IP2016") {
      Future(Ok(RemovePsoDetails()))
    }
  }

  def submitRemovePsoDetails: Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithoutNino("IP2016") {
      val updatedModel = PensionDebitsModel(Some("no"))
      sessionCacheService.saveFormData[PensionDebitsModel]("pensionDebits", updatedModel).map {
        _ => Redirect(routes.SummaryController.summaryIP16)
      }
    }
  }

}
