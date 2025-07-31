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
import models.pla.response.{AmendProtectionResponse, ReadProtectionsResponse, UpdatedLifetimeAllowanceProtectionRecord}
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status.CONFLICT
import play.api.libs.json.Json
import play.api.test.Helpers.{INTERNAL_SERVER_ERROR, LOCKED, NOT_FOUND, OK}
import uk.gov.hmrc.http.HeaderCarrier
import utils.IntegrationBaseSpec

import scala.concurrent.ExecutionContext

class PLAConnectorV2ISpec extends IntegrationBaseSpec with ScalaFutures {

  private val connector: PlaConnectorV2 = app.injector.instanceOf[PlaConnectorV2]

  private implicit val hc: HeaderCarrier    = HeaderCarrier()
  private implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  private val testNino          = "AB999999C"
  private val psaCheckReference = "PSA12345678A"
  private val protectionId      = 33

  "PlaConnectorV2 on amendProtection" when {

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
         |   "updatedLifetimeAllowanceProtectionRecord": {
         |      "identifier": $protectionId
         |   }
         |}""".stripMargin

    val url = s"/protect-your-lifetime-allowance/v2/individuals/$testNino/protections/$protectionId"

    "everything works correctly" should {

      "call correct endpoint and provide correct request body" in {
        stubFor(
          post(urlMatching(url))
            .willReturn(aResponse().withStatus(OK).withBody(correctResponseBodyStr))
        )

        connector.amendProtection(testNino, inputProtectionModel).futureValue

        val expectedRequestBody =
          Json.parse(s"""{
                        |  "typ": "IP2016"
                        |}""".stripMargin)

        verify(
          postRequestedFor(urlEqualTo(url)).withRequestBody(equalToJson(expectedRequestBody.toString))
        )
      }

      "return Right containing AmendProtectionResponse" in {
        stubFor(
          post(urlMatching(url))
            .willReturn(aResponse().withStatus(OK).withBody(correctResponseBodyStr))
        )

        val result = connector.amendProtection(testNino, inputProtectionModel).futureValue

        result shouldBe Right(
          AmendProtectionResponse(UpdatedLifetimeAllowanceProtectionRecord(protectionId))
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
          post(urlMatching(url))
            .willReturn(aResponse().withStatus(OK).withBody(incorrectResponseBody))
        )

        val result = connector.amendProtection(testNino, inputProtectionModel).futureValue

        result shouldBe Left(IncorrectResponseBodyError)
      }
    }

    "it receives Conflict response" should {
      "return Left containing ConflictResponseError" in {
        stubFor(
          post(urlMatching(url))
            .willReturn(aResponse().withStatus(CONFLICT))
        )

        val result = connector.amendProtection(testNino, inputProtectionModel).futureValue

        result shouldBe Left(ConflictResponseError)
      }
    }

    "it receives Locked response" should {
      "return Left containing LockedResponseError" in {
        stubFor(
          post(urlMatching(url))
            .willReturn(aResponse().withStatus(LOCKED))
        )

        val result = connector.amendProtection(testNino, inputProtectionModel).futureValue

        result shouldBe Left(LockedResponseError)
      }
    }

    "it receives a different error response" should {
      "return Left containing UnexpectedResponseError" in {
        stubFor(
          post(urlMatching(url))
            .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR))
        )

        val result = connector.amendProtection(testNino, inputProtectionModel).futureValue

        result shouldBe Left(UnexpectedResponseError(INTERNAL_SERVER_ERROR))
      }
    }
  }

  "PlaConnectorV2 on readProtections" when {

    val correctResponseBodyStr =
      """
        |{
        |  "pensionSchemeAdministratorCheckReference": "PSA34728911G",
        |  "protectionRecordsList": [
        |    {
        |      "protectionRecord": {
        |        "identifier": 20,
        |        "sequenceNumber": 3,
        |        "type": "ENHANCED PROTECTION LTA",
        |        "certificateDate": "2021-02-19",
        |        "certificateTime": "091732",
        |        "status": "OPEN",
        |        "protectionReference": "EPRO1034571625B",
        |        "lumpSumPercentage": 12
        |      },
        |      "historicaldetailsList": [
        |        {
        |          "identifier": 21,
        |          "sequenceNumber": 1,
        |          "type": "ENHANCED PROTECTION",
        |          "certificateDate": "2021-02-19",
        |          "certificateTime": "091732",
        |          "status": "REJECTED",
        |          "protectionReference": "EPRO1034571626B",
        |          "lumpSumPercentage": 99
        |        },
        |        {
        |          "identifier": 21,
        |          "sequenceNumber": 1,
        |          "type": "ENHANCED PROTECTION",
        |          "certificateDate": "2021-02-19",
        |          "certificateTime": "091732",
        |          "status": "WITHDRAWN",
        |          "protectionReference": "EPRO1034571627B",
        |          "lumpSumPercentage": 99
        |        }
        |      ]
        |    },
        |    {
        |      "protectionRecord": {
        |        "identifier": 1,
        |        "sequenceNumber": 3,
        |        "type": "PRIMARY PROTECTION LTA",
        |        "certificateDate": "2021-02-19",
        |        "certificateTime": "091732",
        |        "status": "WITHDRAWN",
        |        "protectionReference": "PPRO1034571625B",
        |        "pensionDebitAmount": 25000,
        |        "pensionDebitEnteredAmount": 25000,
        |        "pensionDebitStartDate": "2022-07-09",
        |        "pensionDebitTotalAmount": 40000,
        |        "lumpSumAmount": 750000,
        |        "enhancementFactor": 12
        |      },
        |      "historicaldetailsList": [
        |        {
        |          "identifier": 11,
        |          "sequenceNumber": 1,
        |          "type": "PRIMARY PROTECTION",
        |          "certificateDate": "2021-02-19",
        |          "certificateTime": "091732",
        |          "status": "REJECTED",
        |          "protectionReference": "PPRO1034571625B",
        |          "pensionDebitAmount": 25000,
        |          "pensionDebitEnteredAmount": 25000,
        |          "pensionDebitStartDate": "2022-07-09",
        |          "pensionDebitTotalAmount": 40000,
        |          "lumpSumAmount": 750000,
        |          "enhancementFactor": 12
        |        }
        |      ]
        |    },
        |    {
        |      "protectionRecord": {
        |        "identifier": 2,
        |        "sequenceNumber": 3,
        |        "type": "FIXED PROTECTION LTA",
        |        "certificateDate": "2021-02-19",
        |        "certificateTime": "091732",
        |        "status": "WITHDRAWN",
        |        "protectionReference": "FP121034571625B"
        |      },
        |      "historicaldetailsList": [
        |        {
        |          "identifier": 12,
        |          "sequenceNumber": 3,
        |          "type": "FIXED PROTECTION",
        |          "certificateDate": "2021-02-19",
        |          "certificateTime": "091732",
        |          "status": "WITHDRAWN",
        |          "protectionReference": "FP121034571625B"
        |        }
        |      ]
        |    },
        |    {
        |      "protectionRecord": {
        |        "identifier": 3,
        |        "sequenceNumber": 3,
        |        "type": "FIXED PROTECTION 2014 LTA",
        |        "certificateDate": "2021-02-19",
        |        "certificateTime": "091732",
        |        "status": "WITHDRAWN",
        |        "protectionReference": "FP141034571625B"
        |      },
        |      "historicaldetailsList": [
        |        {
        |          "identifier": 13,
        |          "sequenceNumber": 3,
        |          "type": "FIXED PROTECTION 2014",
        |          "certificateDate": "2021-02-19",
        |          "certificateTime": "091732",
        |          "status": "WITHDRAWN",
        |          "protectionReference": "FP141034571625B"
        |        }
        |      ]
        |    },
        |    {
        |      "protectionRecord": {
        |        "identifier": 4,
        |        "sequenceNumber": 3,
        |        "type": "INDIVIDUAL PROTECTION 2014 LTA",
        |        "certificateDate": "2021-02-19",
        |        "certificateTime": "091732",
        |        "status": "DORMANT",
        |        "relevantAmount": 105000,
        |        "preADayPensionInPaymentAmount": 1500,
        |        "postADayBenefitCrystallisationEventAmount": 2500,
        |        "uncrystallisedRightsAmount": 75500,
        |        "nonUKRightsAmount": 0,
        |        "pensionDebitAmount": 25000,
        |        "pensionDebitEnteredAmount": 25000,
        |        "protectedAmount": 120000,
        |        "pensionDebitStartDate": "2022-07-09",
        |        "pensionDebitTotalAmount": 40000
        |      },
        |      "historicaldetailsList": [
        |        {
        |          "identifier": 14,
        |          "sequenceNumber": 3,
        |          "type": "INDIVIDUAL PROTECTION 2014",
        |          "certificateDate": "2021-02-19",
        |          "certificateTime": "091732",
        |          "status": "WITHDRAWN",
        |          "protectionReference": "IP141034571625B",
        |          "relevantAmount": 105000,
        |          "preADayPensionInPaymentAmount": 1500,
        |          "postADayBenefitCrystallisationEventAmount": 2500,
        |          "uncrystallisedRightsAmount": 75500,
        |          "nonUKRightsAmount": 0,
        |          "pensionDebitAmount": 25000,
        |          "pensionDebitEnteredAmount": 25000,
        |          "protectedAmount": 120000,
        |          "pensionDebitStartDate": "2022-07-09",
        |          "pensionDebitTotalAmount": 40000
        |        }
        |      ]
        |    },
        |    {
        |      "protectionRecord": {
        |        "identifier": 5,
        |        "sequenceNumber": 3,
        |        "type": "FIXED PROTECTION 2016 LTA",
        |        "certificateDate": "2021-02-19",
        |        "certificateTime": "091732",
        |        "status": "WITHDRAWN",
        |        "protectionReference": "FP161034571625B"
        |      },
        |      "historicaldetailsList": [
        |        {
        |          "identifier": 15,
        |          "sequenceNumber": 3,
        |          "type": "FIXED PROTECTION 2016",
        |          "certificateDate": "2021-02-19",
        |          "certificateTime": "091732",
        |          "status": "WITHDRAWN",
        |          "protectionReference": "FP161034571625B"
        |        }
        |      ]
        |    },
        |    {
        |      "protectionRecord": {
        |        "identifier": 6,
        |        "sequenceNumber": 3,
        |        "type": "INDIVIDUAL PROTECTION 2016 LTA",
        |        "certificateDate": "2021-02-19",
        |        "certificateTime": "091732",
        |        "status": "DORMANT",
        |        "relevantAmount": 105000,
        |        "preADayPensionInPaymentAmount": 1500,
        |        "postADayBenefitCrystallisationEventAmount": 2500,
        |        "uncrystallisedRightsAmount": 75500,
        |        "nonUKRightsAmount": 0,
        |        "pensionDebitAmount": 25000,
        |        "pensionDebitEnteredAmount": 25000,
        |        "protectedAmount": 120000,
        |        "pensionDebitStartDate": "2022-07-09",
        |        "pensionDebitTotalAmount": 40000
        |      },
        |      "historicaldetailsList": [
        |        {
        |          "identifier": 16,
        |          "sequenceNumber": 3,
        |          "type": "INDIVIDUAL PROTECTION 2016",
        |          "certificateDate": "2021-02-19",
        |          "certificateTime": "091732",
        |          "status": "WITHDRAWN",
        |          "protectionReference": "IP161034571625B",
        |          "relevantAmount": 105000,
        |          "preADayPensionInPaymentAmount": 1500,
        |          "postADayBenefitCrystallisationEventAmount": 2500,
        |          "uncrystallisedRightsAmount": 75500,
        |          "nonUKRightsAmount": 0,
        |          "pensionDebitAmount": 25000,
        |          "pensionDebitEnteredAmount": 25000,
        |          "protectedAmount": 120000,
        |          "pensionDebitStartDate": "2022-07-09",
        |          "pensionDebitTotalAmount": 40000
        |        }
        |      ]
        |    },
        |    {
        |      "protectionRecord": {
        |        "identifier": 7,
        |        "sequenceNumber": 1,
        |        "type": "INTERNATIONAL ENHANCEMENT (S221)",
        |        "certificateDate": "2021-02-19",
        |        "certificateTime": "091732",
        |        "status": "WITHDRAWN",
        |        "protectionReference": "IE211034571625B",
        |        "enhancementFactor": 12
        |      }
        |    },
        |    {
        |      "protectionRecord": {
        |        "identifier": 8,
        |        "sequenceNumber": 1,
        |        "type": "INTERNATIONAL ENHANCEMENT (S224)",
        |        "certificateDate": "2021-02-19",
        |        "certificateTime": "091732",
        |        "status": "WITHDRAWN",
        |        "protectionReference": "IE241034571625B",
        |        "enhancementFactor": 12
        |      }
        |    },
        |    {
        |      "protectionRecord": {
        |        "identifier": 9,
        |        "sequenceNumber": 1,
        |        "type": "PENSION CREDIT RIGHTS",
        |        "certificateDate": "2021-02-19",
        |        "certificateTime": "091732",
        |        "status": "WITHDRAWN",
        |        "protectionReference": "PCRD1034571625B",
        |        "enhancementFactor": 12
        |      }
        |    }
        |  ]
        |}""".stripMargin

    val url = s"/protect-your-lifetime-allowance/v2/individuals/$testNino/protections"

    "the backend returns 200 with a correct body" should {
      "return Right containing ReadProtectionsResponse" in {
        stubFor(
          get(urlMatching(url))
            .willReturn(aResponse().withStatus(OK).withBody(correctResponseBodyStr))
        )

        val result = connector.readProtections(testNino).futureValue

        result shouldBe Right(Json.parse(correctResponseBodyStr).as[ReadProtectionsResponse])
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
