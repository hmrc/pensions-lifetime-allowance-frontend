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
import constructors.DisplayConstructors
import config._
import connectors.{CitizenDetailsConnector, KeyStoreConnector}
import javax.inject.Inject
import models.{ExistingProtectionsDisplayModel, PersonalDetailsModel, ProtectionModel}
import play.api.{Configuration, Environment, Logger, Play}
import play.api.mvc._
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.i18n.{I18nSupport, Lang}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.renderer.TemplateRenderer

import scala.concurrent.ExecutionContext.Implicits.global

class PrintController @Inject()(val keyStoreConnector: KeyStoreConnector,
                                val citizenDetailsConnector: CitizenDetailsConnector,
                                implicit val partialRetriever: PlaFormPartialRetriever,
                                implicit val templateRenderer:LocalTemplateRenderer,
                                implicit val context: PlaContext = PlaContextImpl,
                                implicit val appConfig: FrontendAppConfig,
                                implicit val lang: Lang,
                                implicit val env: Environment,
                                mcc: MessagesControllerComponents) extends FrontendController(mcc) with AuthFunction {

  val displayConstructors: DisplayConstructors = DisplayConstructors
  override lazy val authConnector: AuthConnector = AuthClientConnector
  lazy val postSignInRedirectUrl = appConfig.existingProtectionsUrl

  def config: Configuration = Play.current.configuration

  val printView = Action.async { implicit request =>
    genericAuthWithNino("existingProtections") { nino =>
      for {
        personalDetailsModel <- citizenDetailsConnector.getPersonDetails(nino)
        protectionModel <- keyStoreConnector.fetchAndGetFormData[ProtectionModel]("openProtection")
      } yield routePrintView(personalDetailsModel, protectionModel, nino)
    }
  }


  private def routePrintView(personalDetailsModel: Option[PersonalDetailsModel],
                             protectionModel: Option[ProtectionModel],
                             nino: String)(implicit request: Request[AnyContent]): Result = {
    protectionModel match {
      case Some(model) =>
        val displayModel = displayConstructors.createPrintDisplayModel(personalDetailsModel, model, nino)
        Ok(views.html.pages.result.resultPrint(displayModel))
      case _ =>
        Logger.warn(s"Forced redirect to PrintView for $nino")
        Redirect(routes.ReadProtectionsController.currentProtections())
    }
  }
}
