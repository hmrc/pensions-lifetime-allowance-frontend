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

import common.Exceptions
import config.FrontendAppConfig
import constructors.IPApplicationConstructor
import enums.ApplicationType
import javax.inject.Inject
import models._
import play.api.Logging
import play.api.libs.json.Reads._
import play.api.libs.json._
import uk.gov.hmrc.http._
import models.cache.CacheMap
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2

import scala.concurrent.{ExecutionContext, Future}

class PLAConnector @Inject() (appConfig: FrontendAppConfig, http: HttpClientV2) extends Logging {

  val serviceUrl: String = appConfig.servicesConfig.baseUrl("pensions-lifetime-allowance")

  implicit val hc: HeaderCarrier =
    HeaderCarrier().withExtraHeaders("Accept" -> "application/vnd.hmrc.1.0+json", "Content-Type" -> "application/json")

  implicit val readApiResponse: HttpReads[HttpResponse] = new HttpReads[HttpResponse] {
    def read(method: String, url: String, response: HttpResponse) =
      ResponseHandler.handlePLAResponse(method, url, response)
  }

  protected val roundDown = of[JsNumber].map { case JsNumber(n) =>
    JsNumber(n.setScale(2, BigDecimal.RoundingMode.DOWN))
  }

  protected def getProperties(cc: AnyRef): Map[String, Any] = cc.getClass.getDeclaredFields
    .foldLeft(Map[String, Any]()) { (a, f) =>
      f.setAccessible(true)
      a + (f.getName -> f.get(cc))
    }

  protected def getReads(fields: List[Symbol], props: Map[String, Any]) = {
    val t = (__ \ Symbol("amount")).json.update(roundDown)
    fields.map { s =>
      props.get(s.name).flatMap { value =>
        value match {
          case Some(_) =>
            s.name match {
              case "pensionDebits" =>
                Some((__ \ s).json.update(of[JsArray].map { case JsArray(arr) =>
                  JsArray(arr.map(item => item.transform(t).get))
                }))
              case _ => Some((__ \ s).json.update(roundDown))
            }
          case _ => None
        }
      }
    }
  }

  protected def transformer(model: ProtectionModel) = {
    val fields = List(
      Symbol("protectedAmount"),
      Symbol("relevantAmount"),
      Symbol("postADayBenefitCrystallisationEvents"),
      Symbol("preADayPensionInPayment"),
      Symbol("uncrystallisedRights"),
      Symbol("nonUKRights"),
      Symbol("pensionDebitAmount"),
      Symbol("pensionDebitEnteredAmount"),
      Symbol("pensionDebitTotalAmount"),
      Symbol("pensionDebits")
    )
    val jsonTransformerList = getReads(fields, getProperties(model))

    val list = jsonTransformerList.filter(_.isDefined)
    val r: Reads[JsObject] = list.size match {
      case 0 => __.json.pickBranch
      case 1 => list.head.get
      case _ => list.drop(1).foldLeft(list.head.get)((combined, reads) => combined.andThen(reads.get))
    }
    r
  }

  def readProtections(nino: String)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[HttpResponse] = {
    val url = s"$serviceUrl/protect-your-lifetime-allowance/individuals/$nino/protections"
    http
      .get(url"$url")
      .execute[HttpResponse]
  }

  def amendProtection(
      nino: String,
      protection: ProtectionModel
  )(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[HttpResponse] = {
    val id = protection.protectionID.getOrElse(
      throw new Exceptions.RequiredValueNotDefinedForNinoException("amendProtection", "protectionID", nino)
    )
    val requestJson = Json.toJson[ProtectionModel](protection)
    val body        = requestJson.transform(transformer(protection)).get
    val url         = s"$serviceUrl/protect-your-lifetime-allowance/individuals/$nino/protections/$id"
    logger.info(body.toString)
    http
      .put(url"$url")
      .withBody(Json.toJson(body))
      .execute[HttpResponse]
  }

  def psaLookup(
      psaRef: String,
      ltaRef: String
  )(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[HttpResponse] = {
    val url = s"$serviceUrl/protect-your-lifetime-allowance/psalookup/$psaRef/$ltaRef"
    http
      .get(url"$url")
      .execute[HttpResponse]
  }

}

object ResponseHandler extends ResponseHandler {}

trait ResponseHandler extends HttpErrorFunctions {

  def handlePLAResponse(method: String, url: String, response: HttpResponse): HttpResponse =
    response.status match {
      case 409 => response // this is an expected response for this API, so don't throw an exception
      case 423 => response // this is a possible response for this API that must be handled separately, so don't throw an exception
      case 404 => throw UpstreamErrorResponse(response.body, 404, 404) // this is a possible response for this API that must be handled separately, so don't throw an exception
      case _ => handleResponseEither(method, url)(response).getOrElse(response)
    }

}
