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

import auth.AuthFunction
import config.wiring.PlaFormPartialRetriever
import config.{AppConfig, AuthClientConnector, FrontendAppConfig, LocalTemplateRenderer}
import connectors.KeyStoreConnector
import play.api.{Configuration, Environment, Play}
import play.api.i18n.Messages

import scala.concurrent.Future
import forms._
import forms.PensionsTakenForm.pensionsTakenForm
import forms.PensionsTakenBeforeForm.pensionsTakenBeforeForm
import forms.PensionsTakenBetweenForm.pensionsTakenBetweenForm
import forms.OverseasPensionsForm.overseasPensionsForm
import forms.CurrentPensionsForm.currentPensionsForm
import forms.PSODetailsForm.psoDetailsForm
import forms.PensionDebitsForm.pensionDebitsForm
import javax.inject.Inject
import models._
import play.api.data.FormError
import play.api.mvc.{Action, AnyContent}
import views.html._
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.renderer.TemplateRenderer

class IP2016Controller @Inject()(val keyStoreConnector: KeyStoreConnector,
                                 implicit val partialRetriever: PlaFormPartialRetriever,
                                 implicit val templateRenderer:LocalTemplateRenderer) extends BaseController with AuthFunction {
    lazy val appConfig = FrontendAppConfig
    override lazy val authConnector: AuthConnector = AuthClientConnector
    lazy val postSignInRedirectUrl = appConfig.ipStartUrl

    override def config: Configuration = Play.current.configuration
    override def env: Environment = Play.current.injector.instanceOf[Environment]

    //PENSIONS TAKEN
    val pensionsTaken = Action.async { implicit request =>
        genericAuthWithoutNino("IP2016") {
            keyStoreConnector.fetchAndGetFormData[PensionsTakenModel]("pensionsTaken").map {
                case Some(data) => Ok(pages.ip2016.pensionsTaken(pensionsTakenForm.fill(data)))
                case None => Ok(pages.ip2016.pensionsTaken(pensionsTakenForm))
            }
        }
    }

    val submitPensionsTaken = Action.async { implicit request =>
        genericAuthWithoutNino("IP2016") {
            pensionsTakenForm.bindFromRequest.fold(
                errors => {
                    val form = errors.copy(errors = errors.errors.map { er => FormError(er.key, Messages(er.message)) })
                    Future.successful(BadRequest(pages.ip2016.pensionsTaken(form)))
                },
                success => {
                    keyStoreConnector.saveFormData("pensionsTaken", success).map {
                        _ =>
                            success.pensionsTaken.get match {
                                case "yes" => Redirect(routes.IP2016Controller.pensionsTakenBefore())
                                case "no" => Redirect(routes.IP2016Controller.overseasPensions())
                            }
                    }

                }
            )
        }
    }


    //PENSIONS TAKEN BEFORE
    val pensionsTakenBefore = Action.async { implicit request =>
        genericAuthWithoutNino("IP2016") {
            keyStoreConnector.fetchAndGetFormData[PensionsTakenBeforeModel]("pensionsTakenBefore").map {
                case Some(data) => Ok(pages.ip2016.pensionsTakenBefore(pensionsTakenBeforeForm.fill(data)))
                case _ => Ok(pages.ip2016.pensionsTakenBefore(pensionsTakenBeforeForm))
            }
        }
    }

    val submitPensionsTakenBefore = Action.async { implicit request =>
        genericAuthWithoutNino("IP2016") {
            pensionsTakenBeforeForm.bindFromRequest.fold(
                errors => {
                    val form = errors.copy(errors = errors.errors.map { er => FormError(er.key, Messages(er.message)) })
                    Future.successful(BadRequest(pages.ip2016.pensionsTakenBefore(form)))
                },
                success => {
                        keyStoreConnector.saveFormData("pensionsTakenBefore", success).flatMap {
                            _ => Future.successful(Redirect(routes.IP2016Controller.pensionsTakenBetween()))
                    }
                }
            )
        }
    }


    //PENSIONS TAKEN BETWEEN
    val pensionsTakenBetween = Action.async { implicit request =>
        genericAuthWithoutNino("IP2016") {
            keyStoreConnector.fetchAndGetFormData[PensionsTakenBetweenModel]("pensionsTakenBetween").map {
                case Some(data) => Ok(pages.ip2016.pensionsTakenBetween(pensionsTakenBetweenForm.fill(data)))
                case _ => Ok(pages.ip2016.pensionsTakenBetween(pensionsTakenBetweenForm))
            }
        }
    }

    val submitPensionsTakenBetween = Action.async { implicit request =>
        genericAuthWithoutNino("IP2016") {
            pensionsTakenBetweenForm.bindFromRequest.fold(
                errors => {
                    val form = errors.copy(errors = errors.errors.map { er => FormError(er.key, Messages(er.message)) })
                    Future.successful(BadRequest(pages.ip2016.pensionsTakenBetween(form)))
                },
                success => {
                        keyStoreConnector.saveFormData("pensionsTakenBetween", success).flatMap {
                            _ => Future.successful(Redirect(routes.IP2016Controller.overseasPensions()))
                    }
                }
            )
        }
    }


    //OVERSEAS PENSIONS
    val overseasPensions = Action.async { implicit request =>
        genericAuthWithoutNino("IP2016") {
            keyStoreConnector.fetchAndGetFormData[OverseasPensionsModel]("overseasPensions").map {
                case Some(data) => Ok(pages.ip2016.overseasPensions(overseasPensionsForm.fill(data)))
                case _ => Ok(pages.ip2016.overseasPensions(overseasPensionsForm))
            }
        }
    }

    val submitOverseasPensions: Action[AnyContent] = Action.async { implicit request =>
        genericAuthWithoutNino("IP2016") {
            overseasPensionsForm.bindFromRequest.fold(
                errors => {
                    val form = errors.copy(errors = errors.errors.map { er => FormError(er.key, Messages(er.message)) })
                    Future.successful(BadRequest(pages.ip2016.overseasPensions(form)))
                },
                success => {
                        keyStoreConnector.saveFormData("overseasPensions", success).flatMap {
                            _ => Future.successful(Redirect(routes.IP2016Controller.currentPensions()))
                    }
                }
            )
        }
    }


    //CURRENT PENSIONS
    val currentPensions = Action.async { implicit request =>
        genericAuthWithoutNino("IP2016") {
            keyStoreConnector.fetchAndGetFormData[CurrentPensionsModel]("currentPensions").map {
                case Some(data) => Ok(pages.ip2016.currentPensions(currentPensionsForm.fill(data)))
                case _ => Ok(pages.ip2016.currentPensions(currentPensionsForm))
            }
        }
    }

    val submitCurrentPensions = Action.async { implicit request =>
        genericAuthWithoutNino("IP2016") {
            currentPensionsForm.bindFromRequest.fold(
                errors => {
                    val form = errors.copy(errors = errors.errors.map { er => FormError(er.key, Messages(er.message)) })
                    Future.successful(BadRequest(pages.ip2016.currentPensions(form)))
                },
                success => {
                    keyStoreConnector.saveFormData("currentPensions", success).flatMap {
                        _ => Future.successful(Redirect(routes.IP2016Controller.pensionDebits()))
                    }
                }
            )
        }
    }


    //PENSION DEBITS
    val pensionDebits = Action.async { implicit request =>
        genericAuthWithoutNino("IP2016") {
            keyStoreConnector.fetchAndGetFormData[PensionDebitsModel]("pensionDebits").map {
                case Some(data) => Ok(pages.ip2016.pensionDebits(pensionDebitsForm.fill(data)))
                case None => Ok(pages.ip2016.pensionDebits(pensionDebitsForm))
            }
        }
    }

    val submitPensionDebits = Action.async { implicit request =>
        genericAuthWithoutNino("IP2016") {
            pensionDebitsForm.bindFromRequest.fold(
                errors => {
                    val form = errors.copy(errors = errors.errors.map { er => FormError(er.key, Messages(er.message)) })
                    Future.successful(BadRequest(pages.ip2016.pensionDebits(form)))
                },
                success => {
                    keyStoreConnector.saveFormData("pensionDebits", success).map {
                        _ =>
                            success.pensionDebits.get match {
                                case "yes" => Redirect(routes.IP2016Controller.psoDetails())
                                case "no" => Redirect(routes.SummaryController.summaryIP16())

                            }
                    }
                }
            )
        }
    }

    //PENSION SHARING ORDER DETAILS
    val psoDetails = Action.async { implicit request =>
        genericAuthWithoutNino("IP2016") {
            keyStoreConnector.fetchAndGetFormData[PSODetailsModel]("psoDetails").map {
                case Some(data) => Ok(pages.ip2016.psoDetails(psoDetailsForm.fill(data)))
                case _ => Ok(pages.ip2016.psoDetails(psoDetailsForm))
            }
        }
    }

    val submitPSODetails = Action.async { implicit request =>
        genericAuthWithoutNino("IP2016") {
            psoDetailsForm.bindFromRequest.fold(
                errors => {
                    val form = errors.copy(errors = errors.errors.map { er => FormError(er.key, Messages(er.message)) })
                    Future.successful(BadRequest(pages.ip2016.psoDetails(form)))
                },
                form => {
                        keyStoreConnector.saveFormData(s"psoDetails", form).flatMap {
                            _ => Future.successful(Redirect(routes.SummaryController.summaryIP16()))
                    }
                }
            )
        }
    }

    val removePsoDetails = Action.async { implicit request =>
        genericAuthWithoutNino("IP2016") {
            Future(Ok(pages.ip2016.removePsoDetails()))
        }
    }

    val submitRemovePsoDetails = Action.async { implicit request =>
        genericAuthWithoutNino("IP2016") {
            val updatedModel = PensionDebitsModel(Some("no"))
            keyStoreConnector.saveData[PensionDebitsModel]("pensionDebits", updatedModel).map {
                _ => Redirect(routes.SummaryController.summaryIP16())
            }
        }
    }

}
