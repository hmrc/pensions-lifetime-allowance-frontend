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

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, stubFor, urlMatching}
import connectors.PlaConnectorError.{IncorrectResponseBodyError, ResponseLockedError, UnexpectedResponseError}
import models.pla.response.ReadProtectionsResponse
import org.scalatest.concurrent.ScalaFutures
import play.api.test.Helpers.{INTERNAL_SERVER_ERROR, LOCKED, NOT_FOUND, OK}
import uk.gov.hmrc.http.HeaderCarrier
import utils.IntegrationBaseSpec

import scala.concurrent.ExecutionContext

class PLAConnectorV2ISpec extends IntegrationBaseSpec with ScalaFutures {

  private val connector: PlaConnectorV2 = app.injector.instanceOf[PlaConnectorV2]

  private val testNino = "AB999999C"

  private implicit val hc: HeaderCarrier    = HeaderCarrier()
  private implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  private val psaCheckReference = "PSA12345678A"

  private val correctResponseBodyStr =
    s"""{
       |   "pensionSchemeAdministratorCheckReference": "$psaCheckReference"
       |}""".stripMargin

  "PlaConnectorV2 on readProtections" when {

    val url = s"/protect-your-lifetime-allowance/v2/individuals/$testNino/protections"

    "everything works correctly" should {
      "return Right containing TransformedReadResponseModel" in {
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

        result shouldBe Left(ResponseLockedError)
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
