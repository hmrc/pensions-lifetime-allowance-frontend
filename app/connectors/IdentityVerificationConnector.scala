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
import enums.IdentityVerificationResult.IdentityVerificationResult

import javax.inject.Inject
import play.api.libs.json.{Json, OFormat}
import services.MetricsService
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpReadsInstances, HttpResponse, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class IdentityVerificationConnector @Inject() (appConfig: FrontendAppConfig, http: HttpClientV2)(
    implicit executionContext: ExecutionContext
) {
  val serviceUrl: String = appConfig.servicesConfig.baseUrl("identity-verification")

  private def url(journeyId: String) = s"$serviceUrl/mdtp/journey/journeyId/$journeyId"
  private[connectors] case class IdentityVerificationResponse(result: IdentityVerificationResult)
  private implicit val formats: OFormat[IdentityVerificationResponse] = Json.format[IdentityVerificationResponse]

  implicit val legacyRawReads: HttpReads[HttpResponse] =
    HttpReadsInstances.throwOnFailure(HttpReadsInstances.readEitherOf(HttpReadsInstances.readRaw))

  def identityVerificationResponse(
      journeyId: String
  )(implicit hc: HeaderCarrier): Future[IdentityVerificationResult] = {
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
