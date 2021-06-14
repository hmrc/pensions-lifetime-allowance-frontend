/*
 * Copyright 2021 HM Revenue & Customs
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

import config.wiring.PlaFormPartialRetriever
import config.{FrontendAppConfig, LocalTemplateRenderer, PlaContext}
import connectors.{IdentityVerificationConnector, KeyStoreConnector}
import enums.IdentityVerificationResult

import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.Application
import play.api.Logger.logger
import uk.gov.hmrc.http.Upstream4xxResponse
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.pages.ivFailure._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class UnauthorisedController @Inject()(identityVerificationConnector: IdentityVerificationConnector,
                                       keystoreConnector: KeyStoreConnector,
                                       mcc: MessagesControllerComponents,
                                       lockedOut: views.html.pages.ivFailure.lockedOut,
                                       technicalIssue: views.html.pages.ivFailure.technicalIssue,
                                       unauthorised: views.html.pages.ivFailure.unauthorised,
                                       timeout: views.html.pages.timeout)(
                                       implicit val appConfig: FrontendAppConfig,
                                       implicit val plaContext: PlaContext,
                                       implicit val partialRetriever: PlaFormPartialRetriever,
                                       implicit val templateRenderer:LocalTemplateRenderer,
                                       implicit val application: Application)
extends FrontendController(mcc) with I18nSupport {

  val issuesKey = "previous-technical-issues"

  def showNotAuthorised(journeyId: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    val result: Future[Result] = journeyId map { id =>
      val identityVerificationResult = identityVerificationConnector.identityVerificationResponse(id)
      identityVerificationResult.flatMap {
        case IdentityVerificationResult.TechnicalIssue =>
          logger.warn("Technical Issue relating to Identity verification, user directed to technical issue page")
          keystoreConnector.fetchAndGetFormData[Boolean](issuesKey).flatMap {
            case Some(true) => Future.successful(Ok(technicalIssue()))
            case _ =>
              keystoreConnector.saveFormData(issuesKey, true).map { map =>
                InternalServerError(technicalIssue())
              }
          }
        case IdentityVerificationResult.LockedOut => Future.successful(Unauthorized(lockedOut()))
        case IdentityVerificationResult.Timeout =>
          logger.info("User session timed out during IV uplift")
          Future.successful(Unauthorized(timeout()))
        case _ =>
          logger.info("Unauthorised identity verification, returned to unauthorised page")
          Future.successful(Unauthorized(unauthorised()))
      } recover {
        case Upstream4xxResponse(_, NOT_FOUND, _, _) =>
          logger.warn("Could not find unauthorised journey ID")
          Unauthorized(unauthorised())
      }
    } getOrElse Future.successful(Unauthorized(unauthorised()))

    result
  }
}
