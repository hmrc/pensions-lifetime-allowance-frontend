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
import play.api.mvc.RequestHeader
import play.twirl.api.Html
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler

class PlaFrontendErrorHandler @Inject() (errorTemplate: views.html.error_template)(
    implicit val messagesApi: MessagesApi,
    val appConfig: FrontendAppConfig,
    val plaContext: PlaContext,
    val ec: ExecutionContext
) extends FrontendErrorHandler {

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(
      implicit rh: RequestHeader
  ): Future[Html] =
    Future.successful(errorTemplate(pageTitle, heading, message))

  override def badRequestTemplate(implicit request: RequestHeader): Future[Html] =
    standardErrorTemplate(
      Messages("global.error.400.title"),
      Messages("global.error.400.heading"),
      Messages("global.error.400.message")
    )

  override def notFoundTemplate(implicit request: RequestHeader): Future[Html] =
    standardErrorTemplate(
      Messages("global.error.404.title"),
      Messages("global.error.404.heading"),
      Messages("global.error.404.message")
    )

  override def internalServerErrorTemplate(implicit request: RequestHeader): Future[Html] =
    standardErrorTemplate(
      Messages("pla.error.InternalServerError500.title"),
      Messages("pla.error.InternalServerError500.heading"),
      Messages("pla.error.InternalServerError500.message")
    )

}
