/*
 * Copyright 2020 HM Revenue & Customs
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
import javax.inject.Inject
import models.PersonalDetailsModel
import play.api.Logger
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.http.HttpReads.Implicits._

class CitizenDetailsConnector @Inject()(appConfig: FrontendAppConfig,
                                            http: DefaultHttpClient
                                          ) {

   val serviceUrl = appConfig.servicesConfig.baseUrl("citizen-details")

    private def url(nino: String) = s"$serviceUrl/citizen-details/$nino/designatory-details"

    def getPersonDetails(nino: String)(implicit hc: HeaderCarrier): Future[Option[PersonalDetailsModel]] = {

      http.GET[HttpResponse](url(nino)) map {
        response => response.status match {
          case 200 => response.json.validate[PersonalDetailsModel].asOpt
          case _ => {
            Logger.warn(s"Unable to retrieve personal details for nino: $nino")
            None
          }
        }
      }
    }
}
