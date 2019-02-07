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

package connectors

import javax.inject.Inject
import play.api.{Configuration, Environment}
import play.api.Mode.Mode
import play.api.libs.ws.{WS, WSResponse}
import uk.gov.hmrc.play.config.ServicesConfig
import play.api.Play.current

import scala.concurrent.Future

class PdfGeneratorConnector@Inject()(override val runModeConfiguration: Configuration,
                                     environment: Environment) extends ServicesConfig {
  val pdfServiceUrl: String = baseUrl("pdf-generator-service")
  val serviceURL: String = s"$pdfServiceUrl/pdf-generator-service/generate"

  def generatePdf(html: String): Future[WSResponse] = WS.url(serviceURL).post(Map("html" -> Seq(html)))

  override protected def mode: Mode = environment.mode
}


