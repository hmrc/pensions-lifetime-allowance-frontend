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

package services

import models.{DateModel, PensionDebitModel}
import models.cache.CacheMap
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import testHelpers.FakeApplication
import uk.gov.hmrc.mongo.cache.DataKey

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class SessionCacheServiceSpec extends FakeApplication with MockitoSugar with ScalaFutures {

  val mockSessionRepository: SessionRepository      = mock[SessionRepository]
  val sessionId: String                             = UUID.randomUUID.toString
  implicit val executionContext: ExecutionContext   = inject[ExecutionContext]
  implicit val fakeRequest: FakeRequest[AnyContent] = FakeRequest()

  object TestSessionCacheService extends SessionCacheService(mockSessionRepository)

  val testModel = PensionDebitModel(DateModel.of(2025, 12, 4), 10_000)
  val testKey   = "cache-key"

  "Calculator Connector" should {
    "fetch and get from repo" in {
      when(mockSessionRepository.getFromSession[PensionDebitModel](DataKey[PensionDebitModel](any()))(any(), any()))
        .thenReturn(Future.successful(Option(testModel)))

      val result = TestSessionCacheService.fetchAndGetFormData[PensionDebitModel](testKey)
      await(result) shouldBe Some(testModel)
    }

    "save form data to repo" in {
      val returnedCacheMap = CacheMap("haveAddedToPension", Map("data" -> Json.toJson(testModel)))
      when(
        mockSessionRepository
          .putInSession[PensionDebitModel](DataKey[PensionDebitModel](any()), any())(any(), any(), any())
      )
        .thenReturn(Future.successful(returnedCacheMap))

      val result = TestSessionCacheService.saveFormData(testKey, testModel)
      await(result) shouldBe returnedCacheMap
    }
  }

}
