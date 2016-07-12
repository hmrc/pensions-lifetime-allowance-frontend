/*
 * Copyright 2016 HM Revenue & Customs
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

import play.api.mvc.{AnyContent, Action}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import enums.ApplicationType

import scala.concurrent.Future


object FallbackController extends FallbackController

trait FallbackController extends FrontendController {

  def technicalError(pType: String):Action[AnyContent] = Action.async { implicit request =>
    ApplicationType.fromString(pType).map {
      protectionType => Future.successful(Ok(views.html.pages.fallback.technicalError(protectionType.toString)))
    }.getOrElse {
      Future.successful(NotFound(views.html.pages.fallback.notFound()))
    }
  }

}
