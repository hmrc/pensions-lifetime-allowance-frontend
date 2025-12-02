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

import enums.ApplicationType
import play.api.mvc.{Request, Result}
import play.api.mvc.Results.InternalServerError
import play.api.http.HeaderNames.CACHE_CONTROL
import play.api.i18n.Messages

trait AmendControllerErrorHelper {

  def couldNotRetrieveModelForNino(nino: String, when: String): String =
    s"Could not retrieve amend protection model for user with nino $nino $when"

  def buildTechnicalError(
      technicalError: views.html.pages.fallback.technicalError
  )(implicit request: Request[_], messages: Messages): Result =
    InternalServerError(technicalError(ApplicationType.existingProtections.toString))
      .withHeaders(CACHE_CONTROL -> "no-cache")

}
