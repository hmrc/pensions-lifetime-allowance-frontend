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
import play.api.Logging
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.client.HttpClientV2

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PsaLookupConnector @Inject() (
    appConfig: FrontendAppConfig,
    http: HttpClientV2
) extends Logging {

  val serviceUrl: String = appConfig.servicesConfig.baseUrl("pensions-lifetime-allowance")

  implicit val hc: HeaderCarrier =
    HeaderCarrier().withExtraHeaders("Accept" -> "application/vnd.hmrc.1.0+json", "Content-Type" -> "application/json")

  implicit val readApiResponse: HttpReads[HttpResponse] = (method: String, url: String, response: HttpResponse) =>
    ResponseHandler.handlePLAResponse(method, url, response)

  def psaLookup(
      psaRef: String,
      ltaRef: String
  )(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[HttpResponse] = {
    val url = s"$serviceUrl/protect-your-lifetime-allowance/psalookup/$psaRef/$ltaRef"
    http
      .get(url"$url")
      .execute[HttpResponse]
  }

}

object ResponseHandler extends ResponseHandler {}

trait ResponseHandler extends HttpErrorFunctions {

  def handlePLAResponse(method: String, url: String, response: HttpResponse): HttpResponse =
    response.status match {
      case 409 => response // this is an expected response for this API, so don't throw an exception
      case 423 => response // this is a possible response for this API that must be handled separately, so don't throw an exception
      case 404 => throw UpstreamErrorResponse(response.body, 404, 404) // this is a possible response for this API that must be handled separately, so don't throw an exception
      case _ => handleResponseEither(method, url)(response).getOrElse(response)
    }

}
