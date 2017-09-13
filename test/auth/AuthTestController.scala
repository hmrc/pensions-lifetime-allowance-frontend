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

package auth

import controllers.BaseController
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import testHelpers.MockTemplateRenderer
import uk.gov.hmrc.renderer.TemplateRenderer

object AuthTestController extends AuthTestController {

  override lazy val applicationConfig = mockConfig
  override lazy val authConnector = mockAuthConnector
  override lazy val postSignInRedirectUrl = applicationConfig.confirmFPUrl
}

trait AuthTestController extends BaseController with AuthorisedForPLA {
  override implicit val templateRenderer: TemplateRenderer = MockTemplateRenderer

  val authorisedAsyncAction = AuthorisedByAny.async {
    implicit user =>  implicit request => Future.successful(Ok(views.html.pages.confirmation.confirmFP()))
  }

  val authorisedAction = AuthorisedByAny {
    implicit user =>  implicit request => Ok(views.html.pages.confirmation.confirmFP())
  }

}
