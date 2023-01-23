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
import config.{FrontendAppConfig, LocalTemplateRenderer, PlaContext}
import javax.inject.Inject
import play.api.Application
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.partials.FormPartialRetriever
import uk.gov.hmrc.play.views.html.helpers.FormWithCSRF
import views.html.pages._

import scala.concurrent.Future

class ConfirmationController @Inject()(mcc: MessagesControllerComponents,
                                       authFunction: AuthFunction,
                                       ConfirmFP: confirmation.confirmFP)
                                      (implicit val application: Application,
                                       implicit val appConfig: FrontendAppConfig,
                                       implicit val partialRetriever: FormPartialRetriever,
                                       implicit val templateRenderer:LocalTemplateRenderer,
                                       implicit val formWithCSRF: FormWithCSRF,
                                       implicit val plaContext: PlaContext) extends FrontendController(mcc) {

  val confirmFP = Action.async {
    implicit request =>
      authFunction.genericAuthWithoutNino("FP2016") {
        Future.successful(Ok(ConfirmFP()))
      }
  }
}
