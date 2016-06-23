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
import utils.Constants

import models._

object PLAConnector extends PLAConnector with ServicesConfig {

  val stubUrl: String = baseUrl("pensions-lifetime-allowance")
  val http = WSHttp
}

trait PLAConnector {

    implicit val hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("Accept" -> "application/vnd.hmrc.1.0+json", "Content-Type" -> "application/json")
    

    val http: HttpGet with HttpPost with HttpPut
    val stubUrl: String

    implicit val readApiResponse: HttpReads[HttpResponse] = new HttpReads[HttpResponse] {
                    def read(method: String, url: String, response: HttpResponse) = ResponseHandler.handlePLAResponse(method, url, response)
                  }

    def applyFP16(nino: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
        val requestJson: JsValue = Json.parse("""{"protectionType":"FP2016"}""") // TODO: change to use FP application model
        http.POST[JsValue, HttpResponse](s"$stubUrl/protect-your-lifetime-allowance/individuals/$nino/protections", requestJson)
    }








    def applyIP16(nino: String, relAmount: BigDecimal, preADayPIP: BigDecimal, postADayBCE: BigDecimal, nonUKRights: BigDecimal)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
        val requestJson: JsValue = Json.parse({"protectionType":"IP2016",
                                                "relevantAmount":relAmount,
                                                "preADayPensionInPayment":preADayPIP,
                                                "postADayBCE":postADayBCE,
                                                "nonUKRights":nonUKRights})
        http.POST[JsValue, HttpResponse](s"$stubUrl/protect-your-lifetime-allowance/individuals/$nino/protections", requestJson)
    }


}

object ResponseHandler extends ResponseHandler{

}

trait ResponseHandler extends HttpErrorFunctions {
    def handlePLAResponse(method: String, url: String, response: HttpResponse): HttpResponse = {
      response.status match {
        case 409 => response
        case _ => handleResponse(method, url)(response)
      }
    } 
}
