/*
 * Copyright 2017 HM Revenue & Customs
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

import java.time.LocalDateTime

import play.api.mvc._
import uk.gov.hmrc.play.frontend.controller.{FrontendController, UnauthorisedAction}

import scala.concurrent.Future
import views.html.pages.ivFailure._
import connectors.{IdentityVerificationConnector, KeyStoreConnector}
import enums.IdentityVerificationResult
import play.api.Logger
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import uk.gov.hmrc.http.NotFoundException

object UnauthorisedController extends UnauthorisedController {
	override val identityVerificationConnector: IdentityVerificationConnector = IdentityVerificationConnector
  override val keystoreConnector: KeyStoreConnector = KeyStoreConnector
}

trait UnauthorisedController extends BaseController {

  val identityVerificationConnector: IdentityVerificationConnector
  val keystoreConnector: KeyStoreConnector
  val issuesKey = "technical-issues-timestamp"

  def showNotAuthorised(journeyId: Option[String]): Action[AnyContent] = UnauthorisedAction.async { implicit request =>
    val result: Future[Result] = journeyId map { id =>
      val identityVerificationResult = identityVerificationConnector.identityVerificationResponse(id)
      identityVerificationResult.flatMap {
        case IdentityVerificationResult.TechnicalIssue =>
          Logger.warn("Technical Issue relating to Identity verification, user directed to technical issue page")
          keystoreConnector.fetchAndGetFormData[LocalDateTime](issuesKey).flatMap {
            case Some(data) if data.isAfter(LocalDateTime.now().minusHours(1)) => Future.successful(Ok(technicalIssue()))
            case _ =>
              keystoreConnector.saveData(issuesKey, LocalDateTime.now()).map { map =>
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
        case e : NotFoundException =>
          Logger.warn("Could not find unauthorised journey ID")
          Unauthorized(unauthorised())
      }
    } getOrElse Future.successful(Unauthorized(unauthorised()))

    result
  }
}
