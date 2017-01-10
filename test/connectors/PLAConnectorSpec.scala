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

import java.util.UUID

import config.WSHttp
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneServerPerSuite
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.{HttpResponse, HeaderCarrier}
import uk.gov.hmrc.play.http.ws.WSHttp
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.http.cache.client.CacheMap
import scala.collection.immutable.List

import scala.concurrent.Future

class PLAConnectorSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite {

  val mockHttp : WSHttp = mock[WSHttp]

  object TestPLAConnector extends PLAConnector {
    val http = mockHttp
    val serviceUrl = "http://localhost:9012"
  }

  val validApplyFP16Json = """{"protectionType":"FP2016"}"""
  val nino = "AB999999C"
  val tstId = "testUserID"


  val negativePensionsTakenTuple = "pensionsTaken" -> Json.toJson(PensionsTakenModel(Some("no")))
  val negativeOverseasPensionsTuple = "overseasPensions" -> Json.toJson(OverseasPensionsModel("no", None))
  val validCurrentPensionsTuple = "currentPensions" -> Json.toJson(CurrentPensionsModel(Some(BigDecimal(1001))))
  val negativePensionDebitsTuple =  "pensionDebits" -> Json.toJson(PensionDebitsModel(Some("no")))

  val negativeIP14PensionsTakenTuple = "ip14PensionsTaken" -> Json.toJson(PensionsTakenModel(Some("no")))
  val negativeIP14OverseasPensionsTuple = "ip14OverseasPensions" -> Json.toJson(OverseasPensionsModel("no", None))
  val validIP14CurrentPensionsTuple = "ip14CurrentPensions" -> Json.toJson(CurrentPensionsModel(Some(BigDecimal(1001))))
  val negativeIP14PensionDebitsTuple =  "ip14PensionDebits" -> Json.toJson(PensionDebitsModel(Some("no")))

  implicit val hc: HeaderCarrier = HeaderCarrier()

  override def beforeEach() {
    reset(mockHttp)
  }

  "PLAConnector" should {
    "use the correct http" in {
      PLAConnector.http shouldBe WSHttp
    }
  }

  "Calling applyFP16" should {
    "should return a 200 from a valid apply FP16 request" in {
      when(mockHttp.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(OK)))
      val response = TestPLAConnector.applyFP16(nino)
      await(response).status shouldBe OK
    }
  }

  "Calling applyIP16" should {
    "should return a 200 from a valid apply IP16 request" in {
      when(mockHttp.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(OK)))
      val tstMap = CacheMap(tstId, Map(negativePensionsTakenTuple,
                                      negativeOverseasPensionsTuple,
                                      validCurrentPensionsTuple,
                                      negativePensionDebitsTuple))
      val response = TestPLAConnector.applyIP16(nino, tstMap)
      await(response).status shouldBe OK
    }
  }

  "Calling applyIP14" should {
    "should return a 200 from a valid apply IP14 request" in {
      when(mockHttp.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(OK)))
      val tstMap = CacheMap(tstId, Map(negativeIP14PensionsTakenTuple,
                                      negativeIP14OverseasPensionsTuple,
                                      validIP14CurrentPensionsTuple,
                                      negativeIP14PensionDebitsTuple))

      val response = TestPLAConnector.applyIP14(nino, tstMap)
      await(response).status shouldBe OK
    }
  }

  "Calling amendProtection" should {
    "return 200 from a valid amendProtection request" in {
      when(mockHttp.PUT[JsValue, HttpResponse](Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(HttpResponse(OK)))
      val protectionModel = ProtectionModel(
        psaCheckReference = Some("testPSARef"),
        uncrystallisedRights = Some(100000.00),
        nonUKRights = Some(2000.00),
        preADayPensionInPayment = Some(2000.00),
        postADayBenefitCrystallisationEvents = Some(2000.00),
        notificationId = Some(12),
        protectionID = Some(12345),
        protectionType = Some("IP2016"),
        status = Some("dormant"),
        certificateDate = Some("2016-04-17"),
        protectedAmount = Some(1250000),
        protectionReference = Some("PSA123456"))

      val response = TestPLAConnector.amendProtection(nino, protectionModel)
      await(response).status shouldBe OK
    }
  }

  "Calling readProtections" should {
    "should return a 200 from a valid apply readProtections request" in {
      when(mockHttp.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(OK)))

      val response = TestPLAConnector.readProtections(nino)
      await(response).status shouldBe OK
    }
  }
}
