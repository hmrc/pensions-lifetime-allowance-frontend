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
import connectors.PlaConnectorError.{ConflictResponseError, IncorrectResponseBodyError, LockedResponseError, UnexpectedResponseError}
import models.amend.AmendProtectionModel
import models.pla.response.ProtectionType.IndividualProtection2014
import models.{DateModel, PensionDebitModel, ProtectionModel, TimeModel}
import models.pla.AmendableProtectionType
import models.pla.response.ProtectionStatus.Dormant
import models.pla.response.{AmendProtectionResponseStatus, ReadProtectionsResponse}
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status.CONFLICT
import play.api.libs.json.Json
import play.api.test.Helpers.{INTERNAL_SERVER_ERROR, LOCKED, NOT_FOUND, OK}
import testdata.PlaConnectorTestData
import testdata.PlaConnectorTestData._
import uk.gov.hmrc.http.HeaderCarrier
import utils.IntegrationBaseSpec

import scala.concurrent.ExecutionContext

class PlaConnectorISpec extends IntegrationBaseSpec with ScalaFutures {

  private val connector: PlaConnector = app.injector.instanceOf[PlaConnector]

  private implicit val hc: HeaderCarrier    = HeaderCarrier()
  private implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  private val testNino          = "AB999999C"
  private val protectionId      = PlaConnectorTestData.lifetimeAllowanceIdentifier

  "PlaConnector on amendProtection" when {

    val protectionInputProtectionModel: ProtectionModel = ProtectionModel(
      psaCheckReference = "psaCheckReference",
      identifier = lifetimeAllowanceIdentifier,
      sequenceNumber = lifetimeAllowanceSequenceNumber,
      protectionType = IndividualProtection2014,
      status = Dormant,
      certificateDate = Some(DateModel.of(2025, 7, 15)),
      certificateTime = Some(TimeModel.of(17, 43, 12)),
      protectionReference = Some(protectionReference),
      relevantAmount = Some(105000),
      preADayPensionInPaymentAmount = Some(1500.00),
      postADayBenefitCrystallisationEventAmount = Some(2500.00),
      uncrystallisedRightsAmount = Some(75500.00),
      nonUKRightsAmount = Some(0.00),
      protectedAmount = Some(120000),
      pensionDebitTotalAmount = Some(40000)
    )

    val amendProtectionInputProtectionModel = AmendProtectionModel.tryFromProtection(protectionInputProtectionModel).get.withPensionDebit(Some(PensionDebitModel(DateModel.of(2026, 7, 9), 25_000)))

    val correctResponseBodyStr =
      s"""{
         |    "lifetimeAllowanceIdentifier": $lifetimeAllowanceIdentifier,
         |    "lifetimeAllowanceSequenceNumber": ${lifetimeAllowanceSequenceNumber + 1},
         |    "lifetimeAllowanceType": "${AmendableProtectionType.IndividualProtection2014.jsonValue}",
         |    "certificateDate": "2025-07-15",
         |    "certificateTime": "174312",
         |    "status": "${AmendProtectionResponseStatus.Dormant.jsonValue}",
         |    "protectionReference": "$protectionReference",
         |    "relevantAmount": 105000,
         |    "preADayPensionInPaymentAmount": 1500,
         |    "postADayBenefitCrystallisationEventAmount": 2500,
         |    "uncrystallisedRightsAmount": 75500,
         |    "nonUKRightsAmount": 0,
         |    "pensionDebitAmount": 25000,
         |    "pensionDebitEnteredAmount": 25000,
         |    "notificationIdentifier": 7,
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

    "the backend returns 200 with a correct body, with no protectionRecordsList field" should {
      "return Right containing ReadProtectionsResponse" in {
        val responseBodyMissingProtectionRecordsListStr =
          """
            |{
            |  "pensionSchemeAdministratorCheckReference": "PSA34728911G"
            |}""".stripMargin

        stubFor(
          get(urlMatching(url))
            .willReturn(aResponse().withStatus(OK).withBody(responseBodyMissingProtectionRecordsListStr))
        )

        val result = connector.readProtections(testNino).futureValue

        result shouldBe Right(Json.parse(responseBodyMissingProtectionRecordsListStr).as[ReadProtectionsResponse])
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
