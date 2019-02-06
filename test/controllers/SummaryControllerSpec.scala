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

package controllers


import java.util.UUID

import config.LocalTemplateRenderer
import config.wiring.PlaFormPartialRetriever
import connectors.KeyStoreConnector
import constructors.SummaryConstructor
import enums.ApplicationType
import mocks.AuthMock
import models.SummaryModel
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.JsValue
import play.api.test.FakeRequest
import testHelpers._
import uk.gov.hmrc.auth.core.retrieve.Retrievals
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SummaryControllerSpec extends UnitSpec with MockitoSugar with AuthMock with WithFakeApplication {

  implicit val templateRenderer: LocalTemplateRenderer = MockTemplateRenderer.renderer
  implicit val partialRetriever: PlaFormPartialRetriever = mock[PlaFormPartialRetriever]

  val mockKeyStoreConnector = mock[KeyStoreConnector]
  val mockSummaryConstructor = mock[SummaryConstructor]
  val fakeRequest = FakeRequest()

  val tstSummaryModel = SummaryModel(ApplicationType.FP2016, false, List.empty, List.empty)

  val testSummaryController = new SummaryController(mockKeyStoreConnector, partialRetriever, templateRenderer) {
    override lazy val authConnector = mockAuthConnector
    override val summaryConstructor = mockSummaryConstructor
  }

  val sessionId = UUID.randomUUID.toString
  val mockUsername = "mockuser"
  val mockUserId = "/auth/oid/" + mockUsername

  "Navigating to summary when there is no user data" when {

    "user is applying for IP16" should {
      "return 500" in {
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        when(mockKeyStoreConnector.fetchAllUserData(ArgumentMatchers.any())).thenReturn(Future(None))
        val result = await(testSummaryController.summaryIP16(fakeRequest))

        status(result) shouldBe 500
      }
    }
  }

  "Navigating to summary when there is invalid user data" when {

    "user is applying for IP16" should {
      lazy val result = await(testSummaryController.summaryIP16(fakeRequest))

      when(testSummaryController.summaryConstructor.createSummaryData(ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(None)
      when(mockKeyStoreConnector.fetchAllUserData(ArgumentMatchers.any())).thenReturn(Future(Some(CacheMap("tstID", Map.empty[String, JsValue]))))

      "return 500" in {
        status(result) shouldBe 500
      }
    }
  }

  "Navigating to summary when user has valid data" when {
    "user is applying for IP16" should {
      "return 200" in {
        when(testSummaryController.summaryConstructor.createSummaryData(ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Some(tstSummaryModel))
        when(mockKeyStoreConnector.fetchAllUserData(ArgumentMatchers.any())).thenReturn(Future(Some(CacheMap("tstID", Map.empty[String, JsValue]))))

        val result = await(testSummaryController.summaryIP16(fakeRequest))
        status(result) shouldBe 200
      }
    }
  }

  "Checking for data metrics flags" should {
    "return true for 'pensionsTakenBetween'" in {
      SummaryController.recordDataMetrics("pensionsTakenBetween") shouldBe true
    }

    "return false for 'pensionsTakenBetweenAmt'" in {
      SummaryController.recordDataMetrics("pensionsTakenBetweenAmt") shouldBe false
    }
  }
}
