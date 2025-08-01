/*
 * Copyright 2025 HM Revenue & Customs
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

import com.github.tomakehurst.wiremock.client.WireMock._
import connectors.PlaConnectorError.{
  ConflictResponseError,
  IncorrectResponseBodyError,
  LockedResponseError,
  UnexpectedResponseError
}
import models.{ProtectionModel, ReadResponseModel}
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status.CONFLICT
import play.api.libs.json.Json
import play.api.test.Helpers.{INTERNAL_SERVER_ERROR, LOCKED, NOT_FOUND, OK}
import uk.gov.hmrc.http.HeaderCarrier
import utils.IntegrationBaseSpec

import scala.concurrent.ExecutionContext

class PLAConnectorISpec extends IntegrationBaseSpec with ScalaFutures {

  private val connector: PLAConnector = app.injector.instanceOf[PLAConnector]

  private implicit val hc: HeaderCarrier    = HeaderCarrier()
  private implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  private val testNino          = "AB999999C"
  private val psaCheckReference = "PSA12345678A"
  private val protectionId      = 33

  "PLAConnector on amendProtection" when {

    val inputProtectionModel = ProtectionModel(
      psaCheckReference = Some(psaCheckReference),
      uncrystallisedRights = Some(100000.00),
      nonUKRights = Some(2000.00),
      preADayPensionInPayment = Some(2000.00),
      postADayBenefitCrystallisationEvents = Some(2000.00),
      notificationId = Some(12),
      protectionID = Some(protectionId),
      protectionType = Some("IP2016"),
      status = Some("dormant"),
      certificateDate = Some("2016-04-17"),
      protectedAmount = Some(1250000),
      protectionReference = Some("PSA123456")
    )

    val correctResponseBodyStr =
      s"""{
         |   "psaCheckReference": "$psaCheckReference",
         |   "protectionID": $protectionId
         |}""".stripMargin

    val url = s"/protect-your-lifetime-allowance/individuals/$testNino/protections/$protectionId"

    "everything works correctly" should {

      "call correct endpoint and provide correct request body" when {

        "request contains fields with at most 2 decimal places" in {
          stubFor(
            put(urlMatching(url))
              .willReturn(aResponse().withStatus(OK).withBody(correctResponseBodyStr))
          )

          connector.amendProtection(testNino, inputProtectionModel).futureValue

          val expectedRequestBody =
            Json.parse(s"""{
                          |  "psaCheckReference": "$psaCheckReference",
                          |  "uncrystallisedRights": 100000.00,
                          |  "nonUKRights": 2000.00,
                          |  "preADayPensionInPayment": 2000.00,
                          |  "postADayBenefitCrystallisationEvents": 2000.00,
                          |  "notificationId": 12,
                          |  "protectionID": $protectionId,
                          |  "protectionType": "IP2016",
                          |  "status": "dormant",
                          |  "certificateDate": "2016-04-17",
                          |  "protectedAmount": 1250000,
                          |  "protectionReference": "PSA123456"
                          |}""".stripMargin)

          verify(
            putRequestedFor(urlEqualTo(url)).withRequestBody(equalToJson(expectedRequestBody.toString))
          )
        }

        "request contains fields with 10 decimal places" in {
          stubFor(
            put(urlMatching(url))
              .willReturn(aResponse().withStatus(OK).withBody(correctResponseBodyStr))
          )

          val inputProtectionModelWithTenDecimalPlaces = ProtectionModel(
            psaCheckReference = Some(psaCheckReference),
            uncrystallisedRights = Some(100000.1234567891),
            nonUKRights = Some(2000.1234567891),
            preADayPensionInPayment = Some(2000.1254567891),
            postADayBenefitCrystallisationEvents = Some(2000.1254567891),
            notificationId = Some(12),
            protectionID = Some(protectionId),
            protectionType = Some("IP2016"),
            status = Some("dormant"),
            certificateDate = Some("2016-04-17"),
            protectedAmount = Some(1250000),
            protectionReference = Some("PSA123456")
          )

          connector.amendProtection(testNino, inputProtectionModelWithTenDecimalPlaces).futureValue

          val expectedRequestBody =
            Json.parse(s"""{
                          |  "psaCheckReference": "$psaCheckReference",
                          |  "uncrystallisedRights": 100000.12,
                          |  "nonUKRights": 2000.12,
                          |  "preADayPensionInPayment": 2000.12,
                          |  "postADayBenefitCrystallisationEvents": 2000.12,
                          |  "notificationId": 12,
                          |  "protectionID": $protectionId,
                          |  "protectionType": "IP2016",
                          |  "status": "dormant",
                          |  "certificateDate": "2016-04-17",
                          |  "protectedAmount": 1250000,
                          |  "protectionReference": "PSA123456"
                          |}""".stripMargin)

          verify(putRequestedFor(urlEqualTo(url)).withRequestBody(equalToJson(expectedRequestBody.toString)))
        }
      }

      "return Right containing ProtectionModel" in {
        stubFor(
          put(urlMatching(url))
            .willReturn(aResponse().withStatus(OK).withBody(correctResponseBodyStr))
        )

        val result = connector.amendProtection(testNino, inputProtectionModel).futureValue

        result shouldBe Right(
          ProtectionModel(psaCheckReference = Some(psaCheckReference), protectionID = Some(protectionId))
        )
      }
    }

    "it receives response with body in incorrect format" should {
      "return Left containing IncorrectResponseBodyError" in {
        val incorrectResponseBody =
          """{
            |   "incorrectField": "incorrect-value"
            |}""".stripMargin
        stubFor(
          put(urlMatching(url))
            .willReturn(aResponse().withStatus(OK).withBody(incorrectResponseBody))
        )

        val result = connector.amendProtection(testNino, inputProtectionModel).futureValue

        result shouldBe Left(IncorrectResponseBodyError)
      }
    }

    "it receives Conflict response" should {
      "return Left containing ConflictResponseError" in {
        stubFor(
          put(urlMatching(url))
            .willReturn(aResponse().withStatus(CONFLICT))
        )

        val result = connector.amendProtection(testNino, inputProtectionModel).futureValue

        result shouldBe Left(ConflictResponseError)
      }
    }

    "it receives Locked response" should {
      "return Left containing LockedResponseError" in {
        stubFor(
          put(urlMatching(url))
            .willReturn(aResponse().withStatus(LOCKED))
        )

        val result = connector.amendProtection(testNino, inputProtectionModel).futureValue

        result shouldBe Left(LockedResponseError)
      }
    }

    "it receives a different error response" should {
      "return Left containing UnexpectedResponseError" in {
        stubFor(
          put(urlMatching(url))
            .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR))
        )

        val result = connector.amendProtection(testNino, inputProtectionModel).futureValue

        result shouldBe Left(UnexpectedResponseError(INTERNAL_SERVER_ERROR))
      }
    }
  }

  "PLAConnector on readProtections" when {

    val correctResponseBodyStr =
      s"""{
         |   "psaCheckReference": "$psaCheckReference",
         |   "lifetimeAllowanceProtections": []
         |}""".stripMargin

    val url = s"/protect-your-lifetime-allowance/individuals/$testNino/protections"

    "everything works correctly" should {

      "call correct endpoint" in {
        stubFor(
          get(urlMatching(url))
            .willReturn(aResponse().withStatus(OK).withBody(correctResponseBodyStr))
        )

        connector.readProtections(testNino).futureValue

        verify(getRequestedFor(urlEqualTo(url)))
      }

      "return Right containing TransformedReadResponseModel" in {
        stubFor(
          get(urlMatching(url))
            .willReturn(aResponse().withStatus(OK).withBody(correctResponseBodyStr))
        )

        val result = connector.readProtections(testNino).futureValue

        result shouldBe Right(ReadResponseModel(psaCheckReference, Seq.empty))
      }
    }

    "it receives response with body in incorrect format" should {
      "return Left containing IncorrectResponseBodyError" in {
        val incorrectResponseBody =
          """{
            |   "incorrectField": "incorrect-value"
            |}""".stripMargin
        stubFor(
          get(urlMatching(url))
            .willReturn(aResponse().withStatus(OK).withBody(incorrectResponseBody))
        )

        val result = connector.readProtections(testNino).futureValue

        result shouldBe Left(IncorrectResponseBodyError)
      }
    }

    "it receives NotFound response" should {
      "return Left containing UnexpectedResponseError" in {
        stubFor(
          get(urlMatching(url))
            .willReturn(aResponse().withStatus(NOT_FOUND))
        )

        val result = connector.readProtections(testNino).futureValue

        result shouldBe Left(UnexpectedResponseError(NOT_FOUND))
      }
    }

    "it receives Locked response" should {
      "return Left containing LockedResponseError" in {
        stubFor(
          get(urlMatching(url))
            .willReturn(aResponse().withStatus(LOCKED))
        )

        val result = connector.readProtections(testNino).futureValue

        result shouldBe Left(LockedResponseError)
      }
    }

    "it receives a different error response" should {
      "return Left containing UnexpectedResponseError" in {
        stubFor(
          get(urlMatching(url))
            .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR))
        )

        val result = connector.readProtections(testNino).futureValue

        result shouldBe Left(UnexpectedResponseError(INTERNAL_SERVER_ERROR))
      }
    }
  }

}
