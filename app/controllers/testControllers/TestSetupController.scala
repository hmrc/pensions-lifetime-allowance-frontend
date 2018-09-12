/*
 * Copyright 2018 HM Revenue & Customs
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

package controllers.testControllers

import config.{FrontendAppConfig, LocalTemplateRenderer, PlaContext}
import config.wiring.PlaFormPartialRetriever
import connectors.StubConnector
import controllers.BaseController
import javax.inject.Inject
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global


class TestSetupController @Inject()(connector: StubConnector,
                                    implicit val partialRetriever: PlaFormPartialRetriever,
                                    implicit val templateRenderer:LocalTemplateRenderer) extends BaseController {

  def insertProtections(): Action[JsValue] = Action.async(BodyParsers.parse.json) { implicit request =>
    val payload: JsValue = request.body
    connector.insertProtections(payload).map {
      case OK => Ok
    }
  }

  def removeAllProtections(): Action[AnyContent] = Action.async { implicit request =>
    connector.deleteProtections().map {
      case OK => Ok("All protections deleted")
    }
  }

  def removeProtections(nino: String): Action[AnyContent] = Action.async { implicit request =>
    connector.deleteProtectionByNino(nino).map {
      case OK => Ok(s"$nino deleted")
    }
  }

}
