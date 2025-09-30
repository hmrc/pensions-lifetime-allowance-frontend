/*
 * Copyright 2025 HM Revenue & Customs
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

import config.FrontendAppConfig
import play.api.mvc.Results.Redirect
import play.api.mvc.{Request, Result}

import scala.concurrent.Future

trait WithdrawControllerRedirectHelper {

  def redirectToExistingProtectionsIfWithdrawnJourneyDisabled(
      handler: => Future[Result]
  )(implicit appConfig: FrontendAppConfig, request: Request[_]): Future[Result] =
    if (appConfig.hipMigrationEnabled) {
      Future.successful(Redirect(routes.ReadProtectionsController.currentProtections))
    } else {
      handler
    }

}
