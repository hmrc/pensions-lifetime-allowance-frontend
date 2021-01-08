/*
 * Copyright 2021 HM Revenue & Customs
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
import constructors.IPApplicationConstructor
import enums.ApplicationType
import javax.inject.Inject
import models._
import play.api.libs.json.Reads._
import play.api.libs.json._
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

import scala.concurrent.{ExecutionContext, Future}

class PLAConnector @Inject()(appConfig: FrontendAppConfig,
                                http: DefaultHttpClient) {

  val serviceUrl: String = appConfig.servicesConfig.baseUrl("pensions-lifetime-allowance")

  implicit val hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("Accept" -> "application/vnd.hmrc.1.0+json", "Content-Type" -> "application/json")


  implicit val readApiResponse: HttpReads[HttpResponse] = new HttpReads[HttpResponse] {
    def read(method: String, url: String, response: HttpResponse) = ResponseHandler.handlePLAResponse(method, url, response)
  }

  def applyFP16(nino: String)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[HttpResponse] = {
    val requestJson: JsValue = Json.parse("""{"protectionType":"FP2016"}""")
    http.POST[JsValue, HttpResponse](s"$serviceUrl/protect-your-lifetime-allowance/individuals/$nino/protections", requestJson)
  }

  protected val roundDown = of[JsNumber].map { case JsNumber(n) => JsNumber(n.setScale(2, BigDecimal.RoundingMode.DOWN)) }

  protected def getProperties(cc: AnyRef): Map[String, Any] = cc.getClass.getDeclaredFields
    .foldLeft(Map[String, Any]()) { (a, f) =>
    f.setAccessible(true)
    a + (f.getName -> f.get(cc))
  }

  protected def getReads(fields: List[Symbol], props: Map[String, Any]) = {
    val t = (__ \ 'amount).json.update(roundDown)
    fields.map { s =>
      props.get(s.name).flatMap { value =>
        value match {
          case Some(_) =>
            s.name match {
              case "pensionDebits" => Some((__ \ s).json.update(of[JsArray].map { case JsArray(arr) => JsArray(arr.map(item => item.transform(t).get)) }))
              case _ => Some((__ \ s).json.update(roundDown))
            }
          case _ => None
        }
      }
    }
  }

  protected def transformer(application: IPApplicationModel) = {
    val fields = List('uncrystallisedRights, 'preADayPensionInPayment, 'postADayBenefitCrystallisationEvents, 'nonUKRights, 'pensionDebits)
    val jsonTransformerList = getReads(fields, getProperties(application))
    jsonTransformerList.filter(_.isDefined).foldLeft((__ \ 'relevantAmount).json.update(roundDown)) { (combined, reads) =>
      combined andThen reads.get
    }
  }

  protected def transformer(model: ProtectionModel) = {
    val fields = List('protectedAmount, 'relevantAmount, 'postADayBenefitCrystallisationEvents, 'preADayPensionInPayment,
      'uncrystallisedRights, 'nonUKRights, 'pensionDebitAmount, 'pensionDebitEnteredAmount,
      'pensionDebitTotalAmount, 'pensionDebits)
    val jsonTransformerList = getReads(fields, getProperties(model))

    val list = jsonTransformerList.filter(_.isDefined)
    val r: Reads[JsObject] = list.size match {
      case 0 => (__).json.pickBranch
      case 1 => list.head.get
      case _ => list.drop(1).foldLeft(list.head.get) { (combined, reads) => combined andThen reads.get }
    }
    r
  }

  def applyIP16(nino: String, userData: CacheMap)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[HttpResponse] = {
    implicit val protectionType = ApplicationType.IP2016
    val application = IPApplicationConstructor.createIPApplication(userData)
    val requestJson: JsValue = Json.toJson[IPApplicationModel](application)
    val body = requestJson.transform(transformer(application)).get
    http.POST[JsValue, HttpResponse](s"$serviceUrl/protect-your-lifetime-allowance/individuals/$nino/protections", body)
  }

  def applyIP14(nino: String, userData: CacheMap)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[HttpResponse] = {
    implicit val protectionType = ApplicationType.IP2014
    val application = IPApplicationConstructor.createIPApplication(userData)
    val requestJson: JsValue = Json.toJson[IPApplicationModel](application)
    http.POST[JsValue, HttpResponse](s"$serviceUrl/protect-your-lifetime-allowance/individuals/$nino/protections", requestJson)
  }

  def readProtections(nino: String)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[HttpResponse] = {
    http.GET[HttpResponse](s"$serviceUrl/protect-your-lifetime-allowance/individuals/$nino/protections")
  }

  def amendProtection(nino: String, protection: ProtectionModel)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[HttpResponse] = {
    val id = protection.protectionID.getOrElse(throw new Exceptions.RequiredValueNotDefinedForNinoException("amendProtection", "protectionID", nino))
    val requestJson = Json.toJson[ProtectionModel](protection)
    val body = requestJson.transform(transformer(protection)).get
    play.Logger.info(body.toString)
    http.PUT[JsValue, HttpResponse](s"$serviceUrl/protect-your-lifetime-allowance/individuals/$nino/protections/$id", body)
  }

  def psaLookup(psaRef: String, ltaRef: String)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[HttpResponse] = {
    http.GET[HttpResponse](s"$serviceUrl/protect-your-lifetime-allowance/psalookup/$psaRef/$ltaRef")
  }
}

object ResponseHandler extends ResponseHandler {

}

trait ResponseHandler extends HttpErrorFunctions {
  def handlePLAResponse(method: String, url: String, response: HttpResponse): HttpResponse = {
    response.status match {
      case 409 => response // this is an expected response for this API, so don't throw an exception
      case 423 => response // this is a possible response for this API that must be handled separately, so don't throw an exception
      case 404 => throw UpstreamErrorResponse(response.body, 404, 404) // this is a possible response for this API that must be handled separately, so don't throw an exception
      case _ => handleResponseEither(method, url)(response).getOrElse(response)
    }
  }
}
