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

package config

import javax.inject.Inject
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.Request
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler
import uk.gov.hmrc.play.partials.FormPartialRetriever

class PlaFrontendErrorHandler @Inject()(errorTemplate: views.html.error_template
                                       )(implicit val messagesApi: MessagesApi,
                                       implicit val PlaFormPartialRetriever: FormPartialRetriever,
                                       implicit val localTemplateRenderer: LocalTemplateRenderer,
                                       implicit val appConfig: FrontendAppConfig,
                                       implicit val plaContext: PlaContext) extends FrontendErrorHandler {


  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit rh: Request[_]): Html = {
    errorTemplate(pageTitle, heading, message)
  }


  override def badRequestTemplate(implicit request: Request[_]): Html = {
    standardErrorTemplate(
      Messages("global.error.400.title"),
      Messages("global.error.400.heading"),
      Messages("global.error.400.message")
    )
  }

  override def notFoundTemplate(implicit request: Request[_]): Html = {
    standardErrorTemplate(
      Messages("global.error.404.title"),
      Messages("global.error.404.heading"),
      Messages("global.error.404.message")
    )
  }

  override def internalServerErrorTemplate(implicit request: Request[_]): Html =
    standardErrorTemplate(
      Messages("pla.error.InternalServerError500.title"),
      Messages("pla.error.InternalServerError500.heading"),
      Messages("pla.error.InternalServerError500.message")
    )
}