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

import models._
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

  val mockSessionRepository = mock[SessionRepository]
  val sessionId = UUID.randomUUID.toString
  implicit val executionContext: ExecutionContext = fakeApplication().injector.instanceOf[ExecutionContext]
  lazy implicit val fakeRequest: FakeRequest[AnyContent] = FakeRequest()

  object TestsessionCacheService extends SessionCacheService(mockSessionRepository)

  "Calculator Connector" should {
    "fetch and get from repo" in {
      val testModel = PensionsTakenModel(Some("No"))
      when(mockSessionRepository.getFromSession[PensionsTakenModel](DataKey[PensionsTakenModel](any()))(any(), any()))
        .thenReturn(Future.successful(Option(testModel)))

      lazy val result = TestsessionCacheService.fetchAndGetFormData[PensionsTakenModel]("willAddToPension")
      await(result) shouldBe Some(testModel)
    }

    "save form data to repo" in {
      val testModel = PensionsTakenModel(Some("No"))
      val returnedCacheMap = CacheMap("haveAddedToPension", Map("data" -> Json.toJson(testModel)))
      when(mockSessionRepository.putInSession[PensionsTakenModel](DataKey[PensionsTakenModel](any()), any())(any(), any(), any()))
        .thenReturn(Future.successful(returnedCacheMap))

      lazy val result = TestsessionCacheService.saveFormData("haveAddedToPension", testModel)
      await(result) shouldBe returnedCacheMap
    }
  }
}
