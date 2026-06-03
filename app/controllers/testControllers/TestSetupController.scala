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

package controllers.testControllers

import connectors.StubConnector

import javax.inject.{Inject, Singleton}
import play.api.libs.json._
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.ExecutionContext

@Singleton
class TestSetupController @Inject() (connector: StubConnector, mcc: MessagesControllerComponents)(
    implicit ec: ExecutionContext
) extends FrontendController(mcc) {

  def insertProtections(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    val payload: JsValue = request.body
    connector.insertProtections(payload).map {
      case OK     => Ok("Successfully inserted protections")
      case status => InternalServerError(s"Error inserting protections: received $status from stub")
    }
  }

  def removeAllProtections(): Action[AnyContent] = Action.async { implicit request =>
    connector.deleteProtections().map {
      case OK     => Ok("All protections deleted")
      case status => InternalServerError(s"Error deleting all protections: received $status from stub")
    }
  }

  def removeProtections(nino: String): Action[AnyContent] = Action.async { implicit request =>
    connector.deleteProtectionByNino(nino).map {
      case OK     => Ok(s"$nino deleted")
      case status => InternalServerError(s"Error deleting protections for nino $nino: received $status from stub")
    }
  }

}
