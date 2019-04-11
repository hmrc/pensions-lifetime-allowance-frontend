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

import config.WSHttp
import javax.inject.Inject
import play.api.Mode.Mode
import play.api.{Configuration, Environment, Logger}
import play.api.http.Status
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

class StubConnector@Inject()(override val runModeConfiguration: Configuration,
                             environment: Environment) extends ServicesConfig {
  lazy val serviceUrl: String = baseUrl("pla-dynamic-stub")
  val http = WSHttp

  override protected def mode: Mode = environment.mode

  private def deleteProtectionByNinoUrl(nino: String) = s"$serviceUrl/test-only/individuals/$nino/protections"
  private def deleteProtectionsUrl = s"$serviceUrl/test-only/protections/removeAll"
  private def insertProtectionsUrl = s"$serviceUrl/test-only/protections/insert"

  def deleteProtectionByNino(nino: String)(implicit hc: HeaderCarrier): Future[Int] = {

    http.DELETE(deleteProtectionByNinoUrl(nino)).map{_.status}

  }

  def deleteProtections()(implicit hc: HeaderCarrier): Future[Int] = {

    http.DELETE(deleteProtectionsUrl).map{_.status}

  }

  def insertProtections(payload: JsValue)(implicit hc: HeaderCarrier): Future[Int] = {

    http.POST(insertProtectionsUrl,payload).map{_.status}

  }
}

