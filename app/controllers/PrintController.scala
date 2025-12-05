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
import connectors.CitizenDetailsConnector
import constructors.display.DisplayConstructors
import models.ProtectionModel
import play.api.Logging
import play.api.i18n.{I18nSupport, Lang}
import play.api.mvc._
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.pages.result.printProtection

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PrintController @Inject() (
    sessionCacheService: SessionCacheService,
    citizenDetailsConnector: CitizenDetailsConnector,
    displayConstructors: DisplayConstructors,
    printProtectionView: printProtection,
    mcc: MessagesControllerComponents,
    authFunction: AuthFunction
)(implicit val ec: ExecutionContext)
    extends FrontendController(mcc)
    with I18nSupport
    with Logging {

  val printView: Action[AnyContent] = Action.async { implicit request =>
    authFunction.genericAuthWithNino { nino =>
      for {
        protectionModel <- sessionCacheService.fetchOpenProtection
        result          <- routePrintView(protectionModel, nino)
      } yield result
    }
  }

  private def routePrintView(
      protectionModel: Option[ProtectionModel],
      nino: String
  )(implicit request: Request[AnyContent]): Future[Result] = {
    implicit val lang: Lang = mcc.messagesApi.preferred(request).lang

    protectionModel match {
      case Some(model) =>
        citizenDetailsConnector.getPersonDetails(nino).map { personalDetailsModel =>
          val displayModel = displayConstructors.createPrintDisplayModel(personalDetailsModel, model, nino)
          Ok(printProtectionView(displayModel))
        }
      case _ =>
        logger.warn(s"Forced redirect to PrintView for $nino")
        Future.successful(Redirect(routes.ReadProtectionsController.currentProtections))
    }
  }

}
