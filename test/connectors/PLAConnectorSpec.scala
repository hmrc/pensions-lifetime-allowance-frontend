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

import java.util.UUID

import config.WSHttp
import models._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.Mode.Mode
import play.api.{Application, Configuration, Environment, Mode}
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import testHelpers.TestConfigHelper
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.collection.immutable.List
import scala.concurrent.Future
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

class PLAConnectorSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with WithFakeApplication {

  val mockHttp : WSHttp = mock[WSHttp]
  val env = mock[Environment]

  when(env.mode).thenReturn(Mode(0))

  object connector extends PLAConnector {
    val http = mockHttp
    val serviceUrl = "http://localhost:9012"

    override protected def mode: Mode = mock[Mode]

    override protected def runModeConfiguration: Configuration = mock[Configuration]
  }

  val validApplyFP16Json = """{"protectionType":"FP2016"}"""
  val nino = "AB999999C"
  val tstId = "testUserID"
  val psaRef = "testPSARef"
  val ltaRef = "testLTARef"


  val negativePensionsTakenTuple = "pensionsTaken" -> Json.toJson(PensionsTakenModel(Some("no")))
  val negativeOverseasPensionsTuple = "overseasPensions" -> Json.toJson(OverseasPensionsModel("no", None))
  val validCurrentPensionsTuple = "currentPensions" -> Json.toJson(CurrentPensionsModel(Some(BigDecimal(1001))))
  val negativePensionDebitsTuple =  "pensionDebits" -> Json.toJson(PensionDebitsModel(Some("no")))

  val negativeIP14PensionsTakenTuple = "ip14PensionsTaken" -> Json.toJson(PensionsTakenModel(Some("no")))
  val negativeIP14OverseasPensionsTuple = "ip14OverseasPensions" -> Json.toJson(OverseasPensionsModel("no", None))
  val validIP14CurrentPensionsTuple = "ip14CurrentPensions" -> Json.toJson(CurrentPensionsModel(Some(BigDecimal(1001))))
  val negativeIP14PensionDebitsTuple =  "ip14PensionDebits" -> Json.toJson(PensionDebitsModel(Some("no")))

  val positivePensionsTakenTuple = "pensionsTaken" -> Json.toJson(PensionsTakenModel(Some("yes")))
  val positivePensionsTakenBeforeTuple = "pensionsTakenBefore" -> Json.toJson(PensionsTakenBeforeModel("yes", Some(BigDecimal(1000.1234567891))))
  val negativePensionsTakenBeforeTuple = "pensionsTakenBefore" -> Json.toJson(PensionsTakenBeforeModel("no", None))
  val positivePensionsTakenBetweenTuple = "pensionsTakenBetween" -> Json.toJson(PensionsTakenBetweenModel("yes", Some(BigDecimal(1100.1234567891))))
  val negativePensionsTakenBetweenTuple = "pensionsTakenBetween" -> Json.toJson(PensionsTakenBetweenModel("no", None))
  val positiveOverseasPensionsTuple = "overseasPensions" -> Json.toJson(OverseasPensionsModel("yes", Some(BigDecimal(1010.1234567891))))
  val validCurrentPensionsTuple2 = "currentPensions" -> Json.toJson(CurrentPensionsModel(Some(BigDecimal(1001.1234567891))))
  val positivePensionDebitsTuple =  "pensionDebits" -> Json.toJson(PensionDebitsModel(Some("yes")))
  val psoDetailsTuple = "psoDetails" -> Json.toJson(PSODetailsModel(1, 2, 2016, BigDecimal(10000.1234567891)))

  implicit val hc: HeaderCarrier = HeaderCarrier()

  override def beforeEach() {
    reset(mockHttp)
  }

  "Calling applyFP16" should {
    "should return a 200 from a valid apply FP16 request" in {
      when(mockHttp.POST[JsValue, HttpResponse](ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(HttpResponse(OK)))
      val response = connector.applyFP16(nino)
      await(response).status shouldBe OK
    }
  }

  "Calling applyIP16" should {
    "should return a 200 from a valid apply IP16 request" in {
      when(mockHttp.POST[JsValue, HttpResponse](ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(HttpResponse(OK)))
      val tstMap = CacheMap(tstId, Map(negativePensionsTakenTuple,
                                      negativeOverseasPensionsTuple,
                                      validCurrentPensionsTuple,
                                      negativePensionDebitsTuple))
      val response = connector.applyIP16(nino, tstMap)
      await(response).status shouldBe OK
    }
  }

  "Calling applyIP14" should {
    "should return a 200 from a valid apply IP14 request" in {
      when(mockHttp.POST[JsValue, HttpResponse](ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(HttpResponse(OK)))
      val tstMap = CacheMap(tstId, Map(negativeIP14PensionsTakenTuple,
                                      negativeIP14OverseasPensionsTuple,
                                      validIP14CurrentPensionsTuple,
                                      negativeIP14PensionDebitsTuple))

      val response = connector.applyIP14(nino, tstMap)
      await(response).status shouldBe OK
    }
  }

  "Calling amendProtection" should {
    "return 200 from a valid amendProtection request" in {
      when(mockHttp.PUT[JsValue, HttpResponse](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(HttpResponse(OK)))
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

      val response = connector.amendProtection(nino, protectionModel)
      await(response).status shouldBe OK
    }
  }

  "Calling readProtections" should {
    "should return a 200 from a valid apply readProtections request" in {
      when(mockHttp.GET[HttpResponse](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(HttpResponse(OK)))

      val response = connector.readProtections(nino)
      await(response).status shouldBe OK
    }
  }

  "Calling psaLookup" should {
    "should return a 200 from a valid psa lookup request" in {
      when(mockHttp.GET[HttpResponse](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(HttpResponse(OK)))

      val response = connector.psaLookup(psaRef, ltaRef)
      await(response).status shouldBe OK
    }
  }

  "Calling with 10 decimal places" should {
    import enums.ApplicationType
    import constructors.IPApplicationConstructor

    "convert json double values to 2 decimal places for applying for ip" in {
      val expectedJson = Json.parse("""{"uncrystallisedRights":1001.12,"protectionType":"IP2016","postADayBenefitCrystallisationEvents":1100.12,"nonUKRights":1010.12,"relevantAmount":4111.49,"preADayPensionInPayment":1000.12,"pensionDebits":[{"startDate":"2016-02-01","amount":10000.12}]}""")
      when(mockHttp.POST[JsValue, HttpResponse](ArgumentMatchers.any(), ArgumentMatchers.eq(expectedJson), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(HttpResponse(OK)))

      val userData = CacheMap(tstId, Map(positivePensionsTakenTuple,
                                        positivePensionsTakenBeforeTuple,
                                        positivePensionsTakenBetweenTuple,
                                        positiveOverseasPensionsTuple,
                                        validCurrentPensionsTuple2,
                                        positivePensionDebitsTuple,
                                        psoDetailsTuple
                                        ))

      val response = connector.applyIP16(nino, userData)
      await(response).status shouldBe OK
    }

    "convert json double values to 2 decimal places for amending ips" in {
      val expectedJson = Json.parse("""{"psaCheckReference":"testPSARef","protectionID":12345,"certificateDate":"2016-04-17",
                                        "protectionType":"IP2016","status":"dormant","protectedAmount":1250000.12,
                                        "postADayBenefitCrystallisationEvents":2000.12,"preADayPensionInPayment":2000.12,
                                        "uncrystallisedRights":100000.12,"nonUKRights":2000.12,"notificationId":12,
                                        "protectionReference":"PSA123456"}""")
      when(mockHttp.PUT[JsValue, HttpResponse](ArgumentMatchers.any(), ArgumentMatchers.eq(expectedJson))(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(HttpResponse(OK)))
      val protectionModel = ProtectionModel(
        psaCheckReference = Some("testPSARef"),
        uncrystallisedRights = Some(100000.1234567891),
        nonUKRights = Some(2000.1234567891),
        preADayPensionInPayment = Some(2000.1234567891),
        postADayBenefitCrystallisationEvents = Some(2000.1234567891),
        notificationId = Some(12),
        protectionID = Some(12345),
        protectionType = Some("IP2016"),
        status = Some("dormant"),
        certificateDate = Some("2016-04-17"),
        protectedAmount = Some(1250000.1234567891),
        protectionReference = Some("PSA123456"))

      val response = connector.amendProtection(nino, protectionModel)
      await(response).status shouldBe OK
    }

    "not fail when not able to convert json double values to 2 decimal places for amending ips" in {
      val expectedJson = Json.parse("""{"psaCheckReference":"testPSARef","protectionID":12345,"certificateDate":"2016-04-17",
                                        "protectionType":"IP2016","status":"dormant","notificationId":12,
                                        "protectionReference":"PSA123456"}""")
      when(mockHttp.PUT[JsValue, HttpResponse](ArgumentMatchers.any(), ArgumentMatchers.eq(expectedJson))(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(HttpResponse(OK)))
      val protectionModel = ProtectionModel(
        psaCheckReference = Some("testPSARef"),
        uncrystallisedRights = None,
        nonUKRights = None,
        preADayPensionInPayment = None,
        postADayBenefitCrystallisationEvents = None,
        notificationId = Some(12),
        protectionID = Some(12345),
        protectionType = Some("IP2016"),
        status = Some("dormant"),
        certificateDate = Some("2016-04-17"),
        protectedAmount = None,
        protectionReference = Some("PSA123456"))

      val response = connector.amendProtection(nino, protectionModel)
      await(response).status shouldBe OK
    }

    "be able to convert just one json double values to 2 decimal places for amending ips" in {
      val expectedJson = Json.parse("""{"psaCheckReference":"testPSARef","protectionID":12345,"certificateDate":"2016-04-17",
                                        "protectionType":"IP2016","status":"dormant","protectedAmount":1250000.12,"notificationId":12,
                                        "protectionReference":"PSA123456"}""")
      when(mockHttp.PUT[JsValue, HttpResponse](ArgumentMatchers.any(), ArgumentMatchers.eq(expectedJson))(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(HttpResponse(OK)))
      val protectionModel = ProtectionModel(
        psaCheckReference = Some("testPSARef"),
        uncrystallisedRights = None,
        nonUKRights = None,
        preADayPensionInPayment = None,
        postADayBenefitCrystallisationEvents = None,
        notificationId = Some(12),
        protectionID = Some(12345),
        protectionType = Some("IP2016"),
        status = Some("dormant"),
        certificateDate = Some("2016-04-17"),
        protectedAmount = Some(1250000.1234567891),
        protectionReference = Some("PSA123456"))

      val response = connector.amendProtection(nino, protectionModel)
      await(response).status shouldBe OK
    }
  }
}
