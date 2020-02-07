/*
 * Copyright 2020 HM Revenue & Customs
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

import config.{FrontendAppConfig, LocalTemplateRenderer, PlaContext, PlaContextImpl}
import config.wiring.PlaFormPartialRetriever
import connectors.{IdentityVerificationConnector, KeyStoreConnector}
import enums.IdentityVerificationResult
import javax.inject.Inject
import play.api.Logger
import play.api.Play.current
import play.api.i18n.{I18nSupport, Lang}
import play.api.i18n.Messages.Implicits._
import play.api.mvc._
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.pages.ivFailure._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


class UnauthorisedController @Inject()(identityVerificationConnector: IdentityVerificationConnector,
                                       keystoreConnector: KeyStoreConnector,
                                       mcc: MessagesControllerComponents)(
                                       implicit val appConfig: FrontendAppConfig,
                                       implicit val plaContext: PlaContext,
                                       implicit val partialRetriever: PlaFormPartialRetriever,
                                       implicit val templateRenderer:LocalTemplateRenderer) extends FrontendController(mcc) with I18nSupport {

  val issuesKey = "previous-technical-issues"

  def showNotAuthorised(journeyId: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    val result: Future[Result] = journeyId map { id =>
      val identityVerificationResult = identityVerificationConnector.identityVerificationResponse(id)
      identityVerificationResult.flatMap {
        case IdentityVerificationResult.TechnicalIssue =>
          Logger.warn("Technical Issue relating to Identity verification, user directed to technical issue page")
          keystoreConnector.fetchAndGetFormData[Boolean](issuesKey).flatMap {
            case Some(true) => Future.successful(Ok(technicalIssue()))
            case _ =>
              keystoreConnector.saveFormData(issuesKey, true).map { map =>
                InternalServerError(technicalIssue())
              }
          }
        case IdentityVerificationResult.LockedOut => Future.successful(Unauthorized(lockedOut()))
        case IdentityVerificationResult.Timeout =>
          Logger.info("User session timed out during IV uplift")
          Future.successful(Unauthorized(views.html.pages.timeout()))
        case _ =>
          Logger.info("Unauthorised identity verification, returned to unauthorised page")
          Future.successful(Unauthorized(unauthorised()))
      } recover {
        case e: NotFoundException =>
          Logger.warn("Could not find unauthorised journey ID")
          Unauthorized(unauthorised())
      }
    } getOrElse Future.successful(Unauthorized(unauthorised()))

    result
  }
}
