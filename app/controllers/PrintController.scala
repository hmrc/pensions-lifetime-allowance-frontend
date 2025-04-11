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
import connectors.CitizenDetailsConnector
import constructors.DisplayConstructors
import javax.inject.Inject
import models.{PersonalDetailsModel, ProtectionModel}
import play.api.Logging
import play.api.i18n.{I18nSupport, Lang}
import play.api.mvc._
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.pages.result.resultPrint

import scala.concurrent.ExecutionContext

class PrintController @Inject() (
    val sessionCacheService: SessionCacheService,
    val citizenDetailsConnector: CitizenDetailsConnector,
    displayConstructors: DisplayConstructors,
    resultPrintView: resultPrint,
    mcc: MessagesControllerComponents,
    authFunction: AuthFunction
)(implicit val appConfig: FrontendAppConfig, implicit val plaContext: PlaContext, implicit val ec: ExecutionContext)
    extends FrontendController(mcc)
    with I18nSupport
    with Logging {

  lazy val postSignInRedirectUrl = appConfig.existingProtectionsUrl

  val printView = Action.async { implicit request =>
    implicit val lang = mcc.messagesApi.preferred(request).lang
    authFunction.genericAuthWithNino("existingProtections") { nino =>
      for {
        personalDetailsModel <- citizenDetailsConnector.getPersonDetails(nino)
        protectionModel      <- sessionCacheService.fetchAndGetFormData[ProtectionModel]("openProtection")
      } yield routePrintView(personalDetailsModel, protectionModel, nino)
    }
  }

  private def routePrintView(
      personalDetailsModel: Option[PersonalDetailsModel],
      protectionModel: Option[ProtectionModel],
      nino: String
  )(implicit request: Request[AnyContent], lang: Lang): Result =
    protectionModel match {
      case Some(model) =>
        val displayModel = displayConstructors.createPrintDisplayModel(personalDetailsModel, model, nino)
        Ok(resultPrintView(displayModel))
      case _ =>
        logger.warn(s"Forced redirect to PrintView for $nino")
        Redirect(routes.ReadProtectionsController.currentProtections)
    }

}
