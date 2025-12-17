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

import connectors.IdentityVerificationConnector
import enums.IdentityVerificationResult
import play.api.{Application, Logging}
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.SessionCacheService
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UnauthorisedController @Inject() (
    identityVerificationConnector: IdentityVerificationConnector,
    sessionCacheService: SessionCacheService,
    mcc: MessagesControllerComponents,
    lockedOut: views.html.pages.ivFailure.lockedOut,
    technicalIssue: views.html.pages.ivFailure.technicalIssue,
    unauthorised: views.html.pages.ivFailure.unauthorised,
    timeout: views.html.pages.timeout
)(
    implicit val application: Application,
    implicit val ec: ExecutionContext
) extends FrontendController(mcc)
    with I18nSupport
    with Logging {

  def showNotAuthorised(journeyId: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    val result: Future[Result] = journeyId
      .map { id =>
        val identityVerificationResult = identityVerificationConnector.identityVerificationResponse(id)
        identityVerificationResult
          .flatMap {
            case IdentityVerificationResult.TechnicalIssue =>
              logger.warn("Technical Issue relating to Identity verification, user directed to technical issue page")
              sessionCacheService.fetchPreviousTechnicalIssues.flatMap {
                case Some(true) => Future.successful(Ok(technicalIssue()))
                case _ =>
                  sessionCacheService
                    .savePreviousTechnicalIssues(previousTechnicalIssues = true)
                    .map(_ => InternalServerError(technicalIssue()))
              }
            case IdentityVerificationResult.LockedOut => Future.successful(Unauthorized(lockedOut()))
            case IdentityVerificationResult.Timeout =>
              logger.info("User session timed out during IV uplift")
              Future.successful(Unauthorized(timeout()))
            case _ =>
              logger.info("Unauthorised identity verification, returned to unauthorised page")
              Future.successful(Unauthorized(unauthorised()))
          }
          .recover { case UpstreamErrorResponse(_, NOT_FOUND, _, _) =>
            logger.warn("Could not find unauthorised journey ID")
            Unauthorized(unauthorised())
          }
      }
      .getOrElse(Future.successful(Unauthorized(unauthorised())))

    result
  }

}
