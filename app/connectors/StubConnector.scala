/*
 * Copyright 2018 HM Revenue & Customs
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
import play.api.Logger
import play.api.http.Status
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

object StubConnector extends StubConnector with ServicesConfig {
  override val serviceUrl: String = baseUrl("pla-dynamic-stub")

  override def http: WSHttp = WSHttp
}

trait StubConnector {
  val serviceUrl: String

  def http: WSHttp

  private def deleteProtectionByNinoUrl(nino: String) = s"$serviceUrl/test-only/individuals/$nino/protections"
  private def deleteProtectionsUrl = s"$serviceUrl/test-only/protections/removeAll"
  private def insertProtectionsUrl = s"$serviceUrl/test-only/protections/insert"

  def deleteProtectionByNino(nino: String)(implicit hc: HeaderCarrier): Future[Int] = {

    http.DELETE(deleteProtectionByNinoUrl(nino)) map {
      case HttpResponse(Status.OK, _, _, _) => Status.OK
      case HttpResponse(statusCode, _, _, _) =>
        Logger.warn(s"Unable to delete protections for nino: $nino")
        statusCode
    }
  }

  def deleteProtections()(implicit hc: HeaderCarrier): Future[Int] = {

    http.DELETE(deleteProtectionsUrl) map {
      case HttpResponse(Status.OK, _, _, _) => Status.OK
      case HttpResponse(statusCode, _, _, _) =>
        Logger.warn(s"Unable to delete all protections")
        statusCode
    }
  }

  def insertProtections(payload: JsValue)(implicit hc: HeaderCarrier): Future[Int] = {

    http.POST(insertProtectionsUrl,payload) map {
      case HttpResponse(Status.OK, _, _, _) => Status.OK
      case HttpResponse(statusCode, _, _, _) =>
        Logger.warn(s"Unable to insert protections")
        statusCode
    }
  }
}

