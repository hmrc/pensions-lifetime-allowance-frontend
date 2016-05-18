/*
 * Copyright 2016 HM Revenue & Customs
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

import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._
import play.api.libs.json.{JsValue, Json}
import scala.concurrent.Future
import config.WSHttp

import models._

object APIConnector extends APIConnector with ServicesConfig {

  //val stubUrl: String = baseUrl("protect-your-lifetime-allowance")
  val stubUrl: String = "http://localhost:9012/protect-your-lifetime-allowance"
  val http = WSHttp
}

trait APIConnector {

    implicit val hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("Accept" -> "application/vnd.hmrc.1.0+json", "Content-Type" -> "application/json")

    val http: HttpGet with HttpPost with HttpPut
    val stubUrl: String

    def applyFP16(nino: String)(implicit hc: HeaderCarrier): Future[Option[HttpResponse]] = {
        val requestJson: JsValue = Json.toJson[ApplyFP16Model](ApplyFP16Model("FP2016"))
        http.POST[JsValue, Option[HttpResponse]](s"$stubUrl/individuals/$nino/protections", requestJson)
    }
}
