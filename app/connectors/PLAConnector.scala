/*
 * Copyright 2017 HM Revenue & Customs
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
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._
import play.api.libs.json.{JsValue, Json}
import scala.concurrent.Future
import config.WSHttp
import enums.ApplicationType
import constructors.IPApplicationConstructor
import models._

object PLAConnector extends PLAConnector with ServicesConfig {

  val serviceUrl: String = baseUrl("pensions-lifetime-allowance")
  val http = WSHttp
}

trait PLAConnector {

  implicit val hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("Accept" -> "application/vnd.hmrc.1.0+json", "Content-Type" -> "application/json")

  val http: HttpGet with HttpPost with HttpPut
  val serviceUrl: String

  implicit val readApiResponse: HttpReads[HttpResponse] = new HttpReads[HttpResponse] {
    def read(method: String, url: String, response: HttpResponse) = ResponseHandler.handlePLAResponse(method, url, response)
  }

  def applyFP16(nino: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val requestJson: JsValue = Json.parse("""{"protectionType":"FP2016"}""")
    http.POST[JsValue, HttpResponse](s"$serviceUrl/protect-your-lifetime-allowance/individuals/$nino/protections", requestJson)
  }

  def applyIP16(nino: String, userData: CacheMap)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    implicit val protectionType = ApplicationType.IP2016
    val application = IPApplicationConstructor.createIPApplication(userData)
    val requestJson: JsValue = Json.toJson[IPApplicationModel](application)
    http.POST[JsValue, HttpResponse](s"$serviceUrl/protect-your-lifetime-allowance/individuals/$nino/protections", requestJson)
  }

  def applyIP14(nino: String, userData: CacheMap)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    implicit val protectionType = ApplicationType.IP2014
    val application = IPApplicationConstructor.createIPApplication(userData)
    val requestJson: JsValue = Json.toJson[IPApplicationModel](application)
    http.POST[JsValue, HttpResponse](s"$serviceUrl/protect-your-lifetime-allowance/individuals/$nino/protections", requestJson)
  }

  def readProtections(nino: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    http.GET[HttpResponse](s"$serviceUrl/protect-your-lifetime-allowance/individuals/$nino/protections")
  }

  def amendProtection(nino: String, protection: ProtectionModel)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val id = protection.protectionID.getOrElse(throw new Exceptions.RequiredValueNotDefinedForNinoException("amendProtection", "protectionID", nino))
    val requestJson = Json.toJson[ProtectionModel](protection)
    http.PUT[JsValue, HttpResponse](s"$serviceUrl/protect-your-lifetime-allowance/individuals/$nino/protections/$id", requestJson)
  }
}

object ResponseHandler extends ResponseHandler{

}

trait ResponseHandler extends HttpErrorFunctions {
  def handlePLAResponse(method: String, url: String, response: HttpResponse): HttpResponse = {
    response.status match {
      case 409 => response  // this is an expected response for this API, so don't throw an exception
      case 423 => response  // this is a possible response for this API that must be handled separately, so don't throw an exception
      case 404 => throw new Upstream4xxResponse(response.body, 404, 500)  // this is a possible response for this API that must be handled separately, so don't throw an exception
      case _ => handleResponse(method, url)(response)
    }
  }
}
