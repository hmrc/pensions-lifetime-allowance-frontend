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
import models._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalacheck.Gen
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.Environment
import play.api.libs.json.Json
import play.api.test.Helpers._
import testHelpers.FakeApplication
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, UpstreamErrorResponse}

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class PLAConnectorSpec
    extends FakeApplication
    with MockitoSugar
    with ScalaCheckDrivenPropertyChecks
    with BeforeAndAfterEach {

  val mockEnv       = mock[Environment]
  val mockAppConfig = fakeApplication().injector.instanceOf[FrontendAppConfig]
  val mockHttp      = mock[HttpClientV2]

  implicit val executionContext: ExecutionContext = fakeApplication().injector.instanceOf[ExecutionContext]

  class Setup {
    val connector = new PLAConnector(mockAppConfig, mockHttp)
  }

  val validApplyFP16Json             = """{"protectionType":"FP2016"}"""
  val nino                           = "AB999999C"
  val tstId                          = "testUserID"
  val psaRef                         = "testPSARef"
  val ltaRef                         = "testLTARef"
  val requestBuilder: RequestBuilder = mock[RequestBuilder]

  val negativePensionsTakenTuple    = "pensionsTaken"    -> Json.toJson(PensionsTakenModel(Some("no")))
  val negativeOverseasPensionsTuple = "overseasPensions" -> Json.toJson(OverseasPensionsModel("no", None))
  val validCurrentPensionsTuple     = "currentPensions"  -> Json.toJson(CurrentPensionsModel(Some(BigDecimal(1001))))
  val negativePensionDebitsTuple    = "pensionDebits"    -> Json.toJson(PensionDebitsModel(Some("no")))

  val negativeIP14PensionsTakenTuple = "ip14PensionsTaken" -> Json.toJson(PensionsTakenModel(Some("no")))

  val validIP14PensionUsedBetweenTuple =
    "ip14PensionsUsedBetween" -> Json.toJson(PensionsUsedBetweenModel(Some(BigDecimal(1001))))

  val negativeIP14OverseasPensionsTuple = "ip14OverseasPensions" -> Json.toJson(OverseasPensionsModel("no", None))
  val validIP14CurrentPensionsTuple = "ip14CurrentPensions" -> Json.toJson(CurrentPensionsModel(Some(BigDecimal(1001))))
  val negativeIP14PensionDebitsTuple = "ip14PensionDebits" -> Json.toJson(PensionDebitsModel(Some("no")))

  val positivePensionsTakenTuple       = "pensionsTaken"       -> Json.toJson(PensionsTakenModel(Some("yes")))
  val positivePensionsTakenBeforeTuple = "pensionsTakenBefore" -> Json.toJson(PensionsTakenBeforeModel("yes"))
  val negativePensionsTakenBeforeTuple = "pensionsTakenBefore" -> Json.toJson(PensionsTakenBeforeModel("no"))

  val validPensionsWorthBeforeTuple =
    "pensionsWorthBefore" -> Json.toJson(PensionsWorthBeforeModel(Some(BigDecimal(1000.1234567891))))

  val positivePensionsTakenBetweenTuple = "pensionsTakenBetween" -> Json.toJson(PensionsTakenBetweenModel("yes"))
  val negativePensionsTakenBetweenTuple = "pensionsTakenBetween" -> Json.toJson(PensionsTakenBetweenModel("no"))

  val validPensionUsedBetweenTuple =
    "pensionsUsedBetween" -> Json.toJson(PensionsUsedBetweenModel(Some(BigDecimal(1001))))

  val positiveOverseasPensionsTuple =
    "overseasPensions" -> Json.toJson(OverseasPensionsModel("yes", Some(BigDecimal(1010.1234567891))))

  val validCurrentPensionsTuple2 =
    "currentPensions" -> Json.toJson(CurrentPensionsModel(Some(BigDecimal(1001.1234567891))))

  val positivePensionDebitsTuple = "pensionDebits" -> Json.toJson(PensionDebitsModel(Some("yes")))

  val psoDetailsTuple =
    "psoDetails" -> Json.toJson(PSODetailsModel(LocalDate.of(2016, 2, 1), Some(BigDecimal(10000.1234567891))))

  implicit val hc: HeaderCarrier = HeaderCarrier()

  override def beforeEach() =
    reset(mockHttp)

  "PLAConnector on  psaLookup" should {
    "should return a 200 from a valid psa lookup request" in new Setup {
      when(mockHttp.get(any)(any)).thenReturn(requestBuilder)
      when(requestBuilder.execute[HttpResponse](any, any))
        .thenReturn(Future.successful(HttpResponse(OK, "")))

      val response = connector.psaLookup(psaRef, ltaRef)

      await(response).status shouldBe OK
    }
  }

  "ResponseHandler" must {

    "return the response directly" when {

      s"response is $OK" in {
        val response = HttpResponse(OK, validApplyFP16Json)

        ResponseHandler.handlePLAResponse("GET", "/foo", response) shouldBe response
      }

      Seq(CONFLICT, LOCKED).foreach { code =>
        s"status code of the response is $code" in {
          val response = HttpResponse(code, validApplyFP16Json)

          ResponseHandler.handlePLAResponse("GET", "/foo", response) shouldBe response
        }
      }
    }

    "return the response upon handling a response" when {

      s"status code of the response any 4xx/5xx code other than $CONFLICT, $LOCKED, or $NOT_FOUND" in
        forAll(
          Gen
            .oneOf(BAD_REQUEST to NETWORK_AUTHENTICATION_REQUIRED)
            .suchThat(code => code != CONFLICT && code != LOCKED && code != NOT_FOUND)
        ) { code =>
          val response = HttpResponse(code, validApplyFP16Json)
          val result   = ResponseHandler.handlePLAResponse("GET", "/foo", response)

          result shouldBe response
        }
    }

    "throw and UpstreamErrorResponse" when {

      s"status code of the response is $NOT_FOUND" in {
        an[UpstreamErrorResponse] mustBe thrownBy {
          ResponseHandler.handlePLAResponse("GET", "/foo", HttpResponse(NOT_FOUND, validApplyFP16Json))
        }
      }
    }
  }

}
