/*
 * Copyright 2021 HM Revenue & Customs
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

package mocks

import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.renderer.TemplateRenderer

import scala.collection.immutable
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

class MockTemplateRenderer extends TemplateRenderer {

  override lazy val templateServiceBaseUrl: String = ???
  override val refreshAfter: FiniteDuration = 10 minutes

  override def fetchTemplate(path: String): Future[String] = ???

  override def renderDefaultTemplate(path: String, content: Html, extraArgs: Map[String, Any])(implicit messages: Messages): Html = {
    val theTitle = Html(s"<title>${extraArgs("pageTitle").toString}</title>")

    val args = HtmlFormat.fill(
      extraArgs.map {
        case (key, value) => Html(s"<div id=$key>$value</div>")
      }.toList
    )

    HtmlFormat.fill(immutable.Seq(theTitle, content, args))
  }

} 