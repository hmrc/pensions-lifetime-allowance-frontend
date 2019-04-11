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

package config

import config.wiring.PlaFormPartialRetriever
import javax.inject.Inject
import play.api.Configuration
import play.api.i18n.MessagesApi
import play.api.mvc.Request
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.http.FrontendErrorHandler
import uk.gov.hmrc.renderer.TemplateRenderer

class frontendErrorHandler @Inject()(val messagesApi: MessagesApi,
                                     val configuration: Configuration,
                                     val PlaFormPartialRetriever: PlaFormPartialRetriever,
                                     val localTemplateRenderer: LocalTemplateRenderer) extends FrontendErrorHandler {

  implicit val Placontext = PlaContextImpl
  implicit val partialRetriever: uk.gov.hmrc.play.partials.FormPartialRetriever = PlaFormPartialRetriever
  implicit val templateRenderer: TemplateRenderer = localTemplateRenderer

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit rh: Request[_]): Html =
    views.html.error_template(pageTitle, heading, message)

}