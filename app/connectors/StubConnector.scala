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

package connectors

import config.FrontendAppConfig
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StubConnector @Inject() (appConfig: FrontendAppConfig, http: HttpClientV2)(implicit ec: ExecutionContext) {

  val serviceUrl: String = appConfig.servicesConfig.baseUrl("pla-dynamic-stub")

  private def deleteProtectionByNinoUrl(nino: String) = s"$serviceUrl/test-only/individuals/$nino/protections"
  private def deleteProtectionsUrl                    = s"$serviceUrl/test-only/protections/removeAll"
  private def insertProtectionsUrl                    = s"$serviceUrl/test-only/protections/insert"

  def deleteProtectionByNino(nino: String)(implicit hc: HeaderCarrier): Future[Int] = {
    val dpUrl = deleteProtectionByNinoUrl(nino)
    http
      .delete(url"$dpUrl")
      .execute[HttpResponse]
      .map(_.status)
  }

  def deleteProtections()(implicit hc: HeaderCarrier): Future[Int] =
    http
      .delete(url"$deleteProtectionsUrl")
      .execute[HttpResponse]
      .map(_.status)

  def insertProtections(payload: JsValue)(implicit hc: HeaderCarrier): Future[Int] =
    http
      .post(url"$insertProtectionsUrl")
      .withBody(Json.toJson(payload))
      .execute[HttpResponse]
      .map(_.status)

}
