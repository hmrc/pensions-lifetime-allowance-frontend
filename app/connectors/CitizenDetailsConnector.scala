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

import models.PersonalDetailsModel
import play.api.libs.json.{JsResult, JsValue, Json}
import config.WSHttp
import javax.inject.Inject
import play.api.Mode.Mode
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import play.api.{Configuration, Environment, Logger}

import scala.concurrent.Future
import uk.gov.hmrc.http.{HeaderCarrier, HttpGet}

class CitizenDetailsConnectorImpl @Inject()(override val runModeConfiguration: Configuration,
                                            val environment: Environment) extends CitizenDetailsConnector {

  override val serviceUrl = baseUrl("citizen-details")
  override def http: HttpGet = WSHttp

  override protected def mode: Mode = environment.mode
}


  trait CitizenDetailsConnector extends ServicesConfig{

    val serviceUrl: String
    def http: HttpGet

    private def url(nino: String) = s"$serviceUrl/citizen-details/$nino/designatory-details"

    def getPersonDetails(nino: String)(implicit hc: HeaderCarrier): Future[Option[PersonalDetailsModel]] = {

      http.GET(url(nino)) map {
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
