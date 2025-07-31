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

import common.Exceptions
import config.FrontendAppConfig
import connectors.PlaConnectorError._
import models.ProtectionModel
import models.pla.request.AmendProtectionRequest
import models.pla.response.{AmendProtectionResponse, ReadProtectionsResponse}
import play.api.Logging
import play.api.http.Status.{CONFLICT, LOCKED}
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http._

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
          Left(LockedResponseError)

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

  def amendProtection(
      nino: String,
      protection: ProtectionModel
  )(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[Either[PlaConnectorError, AmendProtectionResponse]] = {
    val id = protection.protectionID.getOrElse(
      throw new Exceptions.RequiredValueNotDefinedForNinoException("amendProtection", "protectionID", nino)
    )
    val requestBody = AmendProtectionRequest.from(protection)
    val url         = s"$serviceUrl/protect-your-lifetime-allowance/v2/individuals/$nino/protections/$id"

    http
      .post(url"$url")
      .withBody(Json.toJson(requestBody))
      .execute[AmendProtectionResponse]
      .map(Right(_))
      .recover {
        case _: JsValidationException =>
          logger.warn(s"Unable to create AmendProtectionResponse from PLA response for user nino: $nino")
          Left(IncorrectResponseBodyError)

        case err: UpstreamErrorResponse if err.statusCode == LOCKED =>
          logger.info(s"locked response returned for amend request for user nino $nino")
          Left(LockedResponseError)

        case err: UpstreamErrorResponse if err.statusCode == CONFLICT =>
          logger.warn(s"conflict response returned for amend request for user nino $nino")
          Left(ConflictResponseError)

        case err: UpstreamErrorResponse =>
          logger.error(s"Unexpected status ${err.statusCode} passed to currentProtections for nino: $nino")
          Left(UnexpectedResponseError(err.statusCode))

        case err =>
          Left(GenericPlaConnectorError(err))
      }

  }

}
