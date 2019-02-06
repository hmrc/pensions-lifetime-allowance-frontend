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

package testHelpers

import config.{AppConfig, LocalTemplateRenderer}
import org.scalatest.mockito.MockitoSugar
import play.api.{Configuration, Environment}
import play.api.i18n.Messages
import uk.gov.hmrc.renderer.TemplateRenderer
import play.twirl.api.Html

import scala.concurrent.duration._
import scala.concurrent.Future


object MockTemplateRenderer extends MockitoSugar {
  val renderer = new LocalTemplateRenderer(mock[Configuration],mock[Environment]) {
    override lazy val templateServiceBaseUrl = "http://example.com/template/mustache"
    override val refreshAfter = 10 minutes

    override def fetchTemplate(path: String): Future[String] = ???

    override def renderDefaultTemplate(path: String, content: Html, extraArgs: Map[String, Any])(implicit messages: Messages) = {
      Html("<title>" + extraArgs("pageTitle") + "</title>" + content)
    }
  }
}


