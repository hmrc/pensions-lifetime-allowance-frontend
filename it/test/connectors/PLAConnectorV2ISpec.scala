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
import models.ProtectionModel
import models.pla.response.ReadProtectionsResponse
import models.pla.{AmendProtectionLifetimeAllowanceType, AmendProtectionResponseStatus}
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status.CONFLICT
import play.api.libs.json.Json
import play.api.test.Helpers.{INTERNAL_SERVER_ERROR, LOCKED, NOT_FOUND, OK}
import testdata.PlaV2TestData
import testdata.PlaV2TestData._
import uk.gov.hmrc.http.HeaderCarrier
import utils.IntegrationBaseSpec

import scala.concurrent.ExecutionContext

class PLAConnectorV2ISpec extends IntegrationBaseSpec with ScalaFutures {

  private val connector: PlaConnectorV2 = app.injector.instanceOf[PlaConnectorV2]

  private implicit val hc: HeaderCarrier    = HeaderCarrier()
  private implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  private val testNino          = "AB999999C"
  private val psaCheckReference = "PSA12345678A"
  private val protectionId      = PlaV2TestData.lifetimeAllowanceIdentifier

  "PlaConnectorV2 on amendProtection" when {

    val amendProtectionInputProtectionModel: ProtectionModel = ProtectionModel(
      psaCheckReference = None,
      protectionID = Some(lifetimeAllowanceIdentifier),
      version = Some(lifetimeAllowanceSequenceNumber),
      protectionType = Some("IP2014"),
      certificateDate = Some("2025-07-15T174312"),
      status = Some("dormant"),
      protectionReference = Some(protectionReference),
      relevantAmount = Some(105000),
      preADayPensionInPayment = Some(1500.00),
      postADayBenefitCrystallisationEvents = Some(2500.00),
      uncrystallisedRights = Some(75500.00),
      nonUKRights = Some(0.00),
      pensionDebitAmount = Some(25000),
      pensionDebitEnteredAmount = Some(25000),
      notificationId = Some(3),
      protectedAmount = Some(120000),
      pensionDebitStartDate = Some("2026-07-09"),
      pensionDebitTotalAmount = Some(40000)
    )

    val correctResponseBodyStr =
      s"""{
         |    "lifetimeAllowanceIdentifier": $lifetimeAllowanceIdentifier,
         |    "lifetimeAllowanceSequenceNumber": ${lifetimeAllowanceSequenceNumber + 1},
         |    "lifetimeAllowanceType": "${AmendProtectionLifetimeAllowanceType.IndividualProtection2014.toString}",
         |    "certificateDate": "2025-07-15",
         |    "certificateTime": "174312",
         |    "status": "${AmendProtectionResponseStatus.Dormant.toString}",
         |    "protectionReference": "$protectionReference",
         |    "relevantAmount": 105000,
         |    "preADayPensionInPaymentAmount": 1500,
         |    "postADayBenefitCrystallisationEventAmount": 2500,
         |    "uncrystallisedRightsAmount": 75500,
         |    "nonUKRightsAmount": 0,
         |    "pensionDebitAmount": 25000,
         |    "pensionDebitEnteredAmount": 25000,
         |    "notificationIdentifier": 3,
         |    "protectedAmount": 120000,
         |    "pensionDebitStartDate": "2026-07-09",
         |    "pensionDebitTotalAmount": 40000
         |}
         |""".stripMargin

    val url = s"/protect-your-lifetime-allowance/v2/individuals/$testNino/protections/$protectionId"

    "everything works correctly" should {

      "call correct endpoint and provide correct request body" in {
        stubFor(
          post(urlMatching(url))
            .willReturn(aResponse().withStatus(OK).withBody(correctResponseBodyStr))
        )

        connector.amendProtection(testNino, amendProtectionInputProtectionModel).futureValue

        val expectedRequestBody = Json.toJson(amendProtectionRequest).toString
        verify(postRequestedFor(urlEqualTo(url)).withRequestBody(equalToJson(expectedRequestBody)))
      }

      "return Right containing AmendProtectionResponse" in {
        stubFor(
          post(urlMatching(url))
            .willReturn(aResponse().withStatus(OK).withBody(correctResponseBodyStr))
        )

        val result = connector.amendProtection(testNino, amendProtectionInputProtectionModel).futureValue

        result shouldBe Right(amendProtectionResponse)
      }
    }

    "it receives response with body in incorrect format" should {
      "return Left containing IncorrectResponseBodyError" in {
        val incorrectResponseBody =
          """{
            |   "incorrectField": "incorrect-value"
            |}""".stripMargin
        stubFor(
          post(urlMatching(url))
            .willReturn(aResponse().withStatus(OK).withBody(incorrectResponseBody))
        )

        val result = connector.amendProtection(testNino, amendProtectionInputProtectionModel).futureValue

        result shouldBe Left(IncorrectResponseBodyError)
      }
    }

    "it receives Conflict response" should {
      "return Left containing ConflictResponseError" in {
        stubFor(
          post(urlMatching(url))
            .willReturn(aResponse().withStatus(CONFLICT))
        )

        val result = connector.amendProtection(testNino, amendProtectionInputProtectionModel).futureValue

        result shouldBe Left(ConflictResponseError)
      }
    }

    "it receives Locked response" should {
      "return Left containing LockedResponseError" in {
        stubFor(
          post(urlMatching(url))
            .willReturn(aResponse().withStatus(LOCKED))
        )

        val result = connector.amendProtection(testNino, amendProtectionInputProtectionModel).futureValue

        result shouldBe Left(LockedResponseError)
      }
    }

    "it receives a different error response" should {
      "return Left containing UnexpectedResponseError" in {
        stubFor(
          post(urlMatching(url))
            .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR))
        )

        val result = connector.amendProtection(testNino, amendProtectionInputProtectionModel).futureValue

        result shouldBe Left(UnexpectedResponseError(INTERNAL_SERVER_ERROR))
      }
    }
  }

  "PlaConnectorV2 on readProtections" when {

    val correctResponseBodyStr =
      s"""{
         |   "pensionSchemeAdministratorCheckReference": "$psaCheckReference"
         |}""".stripMargin

    val url = s"/protect-your-lifetime-allowance/v2/individuals/$testNino/protections"

    "everything works correctly" should {
      "return Right containing ReadProtectionsResponse" in {
        stubFor(
          get(urlMatching(url))
            .willReturn(aResponse().withStatus(OK).withBody(correctResponseBodyStr))
        )

        val result = connector.readProtections(testNino).futureValue

        result shouldBe Right(ReadProtectionsResponse(psaCheckReference))
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
      "return Left containing ResponseLockedError" in {
        stubFor(
          get(urlMatching(url))
            .willReturn(aResponse().withStatus(NOT_FOUND))
        )

        val result = connector.readProtections(testNino).futureValue

        result shouldBe Left(UnexpectedResponseError(NOT_FOUND))
      }
    }

    "it receives Locked response" should {
      "return Left containing ResponseLockedError" in {
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
