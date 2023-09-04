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

package repositories

import models.PensionsTakenModel
import models.cache.CacheMap
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import testHelpers.FakeApplication
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.mongo.cache.DataKey

import java.util.UUID
import scala.concurrent.ExecutionContext

class SessionRepositorySpec extends FakeApplication
  with MockitoSugar
  with BeforeAndAfterEach
  with FutureAwaits
  with DefaultAwaitTimeout {

  val repository: SessionRepository = app.injector.instanceOf[SessionRepository]
  implicit val executionContext: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  val session1: String = UUID.randomUUID.toString
  val session2: String = UUID.randomUUID.toString
  val userRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession(SessionKeys.sessionId -> session1)
  val otherUserRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession(SessionKeys.sessionId -> session2)

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(repository.cacheRepo.collection.drop().toFuture())
  }

  val testAnswerKey: DataKey[PensionsTakenModel] = DataKey[PensionsTakenModel]("pensionsTaken")
  val testAnswer: PensionsTakenModel = PensionsTakenModel(Some("no"))
  val testOldAnswer: PensionsTakenModel = PensionsTakenModel(Some("yes"))
  val testCacheMap: CacheMap = CacheMap(session1, Map(testAnswerKey.unwrap -> Json.toJson(testAnswer)))
  val testOtherCacheMap: CacheMap = CacheMap(session2, Map(testAnswerKey.unwrap -> Json.toJson(testAnswer)))

  "putInSession" must {
    "successfully store data" in {
      val res = repository.putInSession(testAnswerKey, testAnswer)(PensionsTakenModel.format, userRequest, executionContext)

      await(res) shouldBe testCacheMap
    }

    "successfully overwrite existing data" in {
      await(repository.putInSession(testAnswerKey, testOldAnswer)(PensionsTakenModel.format, userRequest, executionContext))

      val res = repository.putInSession(testAnswerKey, testAnswer)(PensionsTakenModel.format, userRequest, executionContext)

      await(res) shouldBe testCacheMap
    }
  }

  "getFromSession" must {
    "return None when no data for this session exists" in {
      await(repository.putInSession(testAnswerKey, testAnswer)(PensionsTakenModel.format, otherUserRequest, executionContext))

      val res = repository.getFromSession(testAnswerKey)(PensionsTakenModel.format, userRequest)

      await(res) shouldBe None
    }

    "successfully return existing data" in {
      await(repository.putInSession(testAnswerKey, testAnswer)(PensionsTakenModel.format, otherUserRequest, executionContext))
      await(repository.putInSession(testAnswerKey, testAnswer)(PensionsTakenModel.format, userRequest, executionContext))

      val res = repository.getFromSession(testAnswerKey)(PensionsTakenModel.format, userRequest)

      await(res) shouldBe Some(testAnswer)
    }
  }

  "getAllFromSession" must {
    "return None when no data for this session exists" in {
      await(repository.putInSession(testAnswerKey, testAnswer)(PensionsTakenModel.format, otherUserRequest, executionContext))

      val res = repository.getAllFromSession(userRequest)

      await(res) shouldBe None
    }

    "successfully return cacheMap with existing data" in {
      await(repository.putInSession(testAnswerKey, testAnswer)(PensionsTakenModel.format, otherUserRequest, executionContext))
      await(repository.putInSession(testAnswerKey, testAnswer)(PensionsTakenModel.format, userRequest, executionContext))

      val res = repository.getAllFromSession(userRequest)

      await(res) shouldBe Some(testCacheMap)
    }
  }

  "clearSession" must {
    "successfully remove session of a user" in {
      await(repository.putInSession(testAnswerKey, testAnswer)(PensionsTakenModel.format, otherUserRequest, executionContext))
      await(repository.putInSession(testAnswerKey, testAnswer)(PensionsTakenModel.format, userRequest, executionContext))

      await(repository.clearSession(userRequest))

      await(repository.getAllFromSession(userRequest)) shouldBe None
      await(repository.getAllFromSession(otherUserRequest)) shouldBe Some(testOtherCacheMap)
    }
  }

}
