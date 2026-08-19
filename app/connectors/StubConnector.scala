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

import config.AppConfig
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.writeableOf_JsValue
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StubConnector @Inject() (appConfig: AppConfig, http: HttpClientV2)(using ExecutionContext) {

  def deleteProtectionByNino(nino: String)(using HeaderCarrier): Future[Int] =
    http
      .delete(url"${appConfig.stubBaseUrl}/test-only/individuals/$nino/protections")
      .execute[HttpResponse]
      .map(_.status)

  def deleteProtections()(using HeaderCarrier): Future[Int] =
    http
      .delete(url"${appConfig.stubBaseUrl}/test-only/protections/removeAll")
      .execute[HttpResponse]
      .map(_.status)

  def insertProtections(payload: JsValue)(using HeaderCarrier): Future[Int] =
    http
      .post(url"${appConfig.stubBaseUrl}/test-only/protections/insert")
      .withBody(Json.toJson(payload))
      .execute[HttpResponse]
      .map(_.status)

}
