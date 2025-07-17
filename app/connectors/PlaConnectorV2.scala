/*
 * Copyright 2025 HM Revenue & Customs
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
import connectors.PlaConnectorError.{
  GenericPlaConnectorError,
  IncorrectResponseBodyError,
  ResponseLockedError,
  UnexpectedResponseError
}
import models.pla.response.ReadProtectionsResponse
import play.api.Logging
import play.api.http.Status.LOCKED
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{
  HeaderCarrier,
  JsValidationException,
  NotFoundException,
  StringContextOps,
  UpstreamErrorResponse
}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PlaConnectorV2 @Inject() (
    appConfig: FrontendAppConfig,
    http: HttpClientV2
) extends Logging {

  private val serviceUrl: String = appConfig.servicesConfig.baseUrl("pensions-lifetime-allowance")

  def readProtections(nino: String)(
      implicit hc: HeaderCarrier,
      ex: ExecutionContext
  ): Future[Either[PlaConnectorError, ReadProtectionsResponse]] = {
    val url = s"$serviceUrl/protect-your-lifetime-allowance/v2/individuals/$nino/protections"

    http
      .get(url"$url")
      .execute[ReadProtectionsResponse]
      .map(Right(_))
      .recover {
        case _: JsValidationException =>
          logger.warn(s"Unable to parse response body from pensions-lifetime-allowance for nino: $nino")
          Left(IncorrectResponseBodyError)

        case err: UpstreamErrorResponse if err.statusCode == LOCKED =>
          Left(ResponseLockedError)

        case err: NotFoundException =>
          logger.warn(s"Error 404 passed to currentProtections for nino: $nino")
          Left(UnexpectedResponseError(err.responseCode))

        case err: UpstreamErrorResponse =>
          logger.error(s"Unexpected status ${err.statusCode} passed to currentProtections for nino: $nino")
          Left(UnexpectedResponseError(err.statusCode))

        case err =>
          Left(GenericPlaConnectorError(err))
      }
  }

}
