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
import connectors.KeyStoreConnector
import forms.CurrentPensionsForm.currentPensionsForm
import forms.OverseasPensionsForm.overseasPensionsForm
import forms.PSODetailsForm.psoDetailsForm
import forms.PensionDebitsForm.pensionDebitsForm
import forms.PensionsTakenBeforeForm.pensionsTakenBeforeForm
import forms.PensionsTakenBetweenForm.pensionsTakenBetweenForm
import forms.PensionsTakenForm.pensionsTakenForm
import javax.inject.Inject
import models._
import play.api.Application
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever
import views.html._

import scala.concurrent.{ExecutionContext, Future}

class IP2016Controller @Inject()(val keyStoreConnector: KeyStoreConnector,
                                 mcc: MessagesControllerComponents,
                                 authFunction: AuthFunction,
                                 pensionsTaken: pages.ip2016.pensionsTaken,
                                 pensionsTakenBefore: pages.ip2016.pensionsTakenBefore,
                                 pensionsTakenBetween: pages.ip2016.pensionsTakenBetween,
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
      keyStoreConnector.fetchAndGetFormData[PensionsTakenModel]("pensionsTaken").map {
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
            keyStoreConnector.saveFormData("pensionsTaken", success).map {
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
      keyStoreConnector.fetchAndGetFormData[PensionsTakenBeforeModel]("pensionsTakenBefore").map {
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
          keyStoreConnector.saveFormData("pensionsTakenBefore", success).flatMap {
            _ => Future.successful(Redirect(routes.IP2016Controller.pensionsTakenBetween))
          }
        }
      )
    }
  }


  //PENSIONS TAKEN BETWEEN
  def pensionsTakenBetween: Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithoutNino("IP2016") {
      keyStoreConnector.fetchAndGetFormData[PensionsTakenBetweenModel]("pensionsTakenBetween").map {
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
          keyStoreConnector.saveFormData("pensionsTakenBetween", success).flatMap {
            _ => Future.successful(Redirect(routes.IP2016Controller.overseasPensions))
          }
        }
      )
    }
  }


  //OVERSEAS PENSIONS
  def overseasPensions: Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithoutNino("IP2016") {
      keyStoreConnector.fetchAndGetFormData[OverseasPensionsModel]("overseasPensions").map {
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
          keyStoreConnector.saveFormData("overseasPensions", success).flatMap {
            _ => Future.successful(Redirect(routes.IP2016Controller.currentPensions))
          }
        }
      )
    }
  }


  //CURRENT PENSIONS
  def currentPensions: Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithoutNino("IP2016") {
      keyStoreConnector.fetchAndGetFormData[CurrentPensionsModel]("currentPensions").map {
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
          keyStoreConnector.saveFormData("currentPensions", success).flatMap {
            _ => Future.successful(Redirect(routes.IP2016Controller.pensionDebits))
          }
        }
      )
    }
  }


  //PENSION DEBITS
  def pensionDebits: Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithoutNino("IP2016") {
      keyStoreConnector.fetchAndGetFormData[PensionDebitsModel]("pensionDebits").map {
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
          keyStoreConnector.saveFormData("pensionDebits", success).map {
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
      keyStoreConnector.fetchAndGetFormData[PSODetailsModel]("psoDetails").map {
        case Some(data) => Ok(psoDetails(psoDetailsForm.fill(data)))
        case _ => Ok(psoDetails(psoDetailsForm))
      }
    }
  }

  def submitPSODetails: Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithoutNino("IP2016") {
      psoDetailsForm.bindFromRequest().fold(
        errors => {
          val form = errors.copy(errors = errors.errors.map { er => FormError(er.key, Messages(er.message)) })
          Future.successful(BadRequest(psoDetails(form)))
        },
        form => {
          keyStoreConnector.saveFormData(s"psoDetails", form).flatMap {
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
      keyStoreConnector.saveData[PensionDebitsModel]("pensionDebits", updatedModel).map {
        _ => Redirect(routes.SummaryController.summaryIP16)
      }
    }
  }

}
