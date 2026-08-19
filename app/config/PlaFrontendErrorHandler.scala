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

import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.RequestHeader
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PlaFrontendErrorHandler @Inject() (
    errorTemplate: views.html.error_template
)(
    using override val messagesApi: MessagesApi,
    override val ec: ExecutionContext
) extends FrontendErrorHandler {

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(
      using RequestHeader
  ): Future[Html] =
    Future.successful(errorTemplate(pageTitle, heading, message))

  override def badRequestTemplate(using RequestHeader): Future[Html] = {
    val messages: Messages = summon[Messages]

    standardErrorTemplate(
      messages("global.error.400.title"),
      messages("global.error.400.heading"),
      messages("global.error.400.message")
    )
  }

  override def notFoundTemplate(using RequestHeader): Future[Html] =
    val messages: Messages = summon[Messages]

    standardErrorTemplate(
      messages("global.error.404.title"),
      messages("global.error.404.heading"),
      messages("global.error.404.message")
    )

  override def internalServerErrorTemplate(using RequestHeader): Future[Html] =
    val messages: Messages = summon[Messages]

    standardErrorTemplate(
      messages("pla.error.InternalServerError500.title"),
      messages("pla.error.InternalServerError500.heading"),
      messages("pla.error.InternalServerError500.message")
    )

}
