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
import enums.IdentityVerificationResult
import play.api.libs.json.{Json, OFormat}
import services.MetricsService
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpReadsInstances, HttpResponse, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class IdentityVerificationConnector @Inject() (appConfig: AppConfig, http: HttpClientV2)(
    using ExecutionContext
) {

  private def url(journeyId: String) = s"${appConfig.identityVerificationBaseUrl}/mdtp/journey/journeyId/$journeyId"

  private[connectors] case class IdentityVerificationResponse(result: IdentityVerificationResult)

  private[connectors] object IdentityVerificationResponse {
    given OFormat[IdentityVerificationResponse] = Json.format[IdentityVerificationResponse]
  }

  given HttpReads[HttpResponse] =
    HttpReadsInstances.throwOnFailure(HttpReadsInstances.readEitherOf(HttpReadsInstances.readRaw))

  def identityVerificationResponse(
      journeyId: String
  )(using HeaderCarrier): Future[IdentityVerificationResult] = {
    val context      = MetricsService.identityVerificationTimer.time()
    val journeyIdUrl = url(journeyId)
    val ivFuture =
      http
        .get(url"$journeyIdUrl")
        .execute[HttpResponse]
        .flatMap { httpResponse =>
          context.stop()
          httpResponse.json
            .validate[IdentityVerificationResponse]
            .fold(
              errs => Future.failed(new JsonValidationException(s"Unable to deserialise: $errs")),
              valid => Future.successful(valid.result)
            )
        }

    ivFuture.onComplete {
      case Failure(_) => MetricsService.identityVerificationFailedCounter.inc()
      case Success(_) =>
    }

    ivFuture
  }

  private[connectors] class JsonValidationException(message: String) extends Exception(message)
}
