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

import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.pages.ip2014.withdrawnAP2014
import views.html.pages.ip2016.withdrawnAP2016

import javax.inject.Inject

class WithdrawnController @Inject() (
    mcc: MessagesControllerComponents,
    withdrawn2014: withdrawnAP2014,
    withdrawn2016: withdrawnAP2016
) extends FrontendController(mcc) {

  def showWithdrawn2014(): Action[AnyContent] = Action(implicit request => Ok(withdrawn2014()))

  def showWithdrawn2016(): Action[AnyContent] = Action(implicit request => Ok(withdrawn2016()))
}
