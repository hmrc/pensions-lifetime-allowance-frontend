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

package repositories

import models.{DateModel, PensionDebitModel}
import models.cache.CacheMap
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{Json, OFormat}
import play.api.mvc.{AnyContent, Request}
import play.api.test.{FakeRequest, Injecting}
import play.api.{Application, Configuration}
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.mongo.TimestampSupport
import uk.gov.hmrc.mongo.cache.DataKey
import uk.gov.hmrc.mongo.test.{CleanMongoCollectionSupport, MongoSupport}

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global

class SessionRepositoryISpec
    extends AnyWordSpec
    with MongoSupport
    with CleanMongoCollectionSupport
    with Matchers
    with ScalaFutures
    with Injecting {

  val app: Application = new GuiceApplicationBuilder()
    .configure("mongodb.uri" -> mongoUri)
    .build()

  implicit val pensionsTakenModelFormat: OFormat[PensionDebitModel] = Json.format[PensionDebitModel]

  val repository = new SessionRepository(mongoComponent, inject[Configuration], inject[TimestampSupport])

  val session1: String                      = UUID.randomUUID.toString
  val session2: String                      = UUID.randomUUID.toString
  val userRequest: Request[AnyContent]      = FakeRequest().withSession(SessionKeys.sessionId -> session1)
  val otherUserRequest: Request[AnyContent] = FakeRequest().withSession(SessionKeys.sessionId -> session2)

  val testAnswerKey: DataKey[PensionDebitModel] = DataKey[PensionDebitModel]("pensionsTaken")
  val testAnswer: PensionDebitModel             = PensionDebitModel(DateModel.of(2025, 12, 5), 200)
  val testOldAnswer: PensionDebitModel          = PensionDebitModel(DateModel.of(2024, 11, 23), 599)
  val testCacheMap: CacheMap      = CacheMap(session1, Map(testAnswerKey.unwrap -> Json.toJson(testAnswer)))
  val testOtherCacheMap: CacheMap = CacheMap(session2, Map(testAnswerKey.unwrap -> Json.toJson(testAnswer)))

  "putInSession" must {
    "successfully store data" in {
      val res = repository.putInSession(testAnswerKey, testAnswer)(pensionsTakenModelFormat, userRequest, global)

      res.futureValue shouldBe testCacheMap
    }

    "successfully overwrite existing data" in {
      repository.putInSession(testAnswerKey, testOldAnswer)(pensionsTakenModelFormat, userRequest, global).futureValue

      val res = repository.putInSession(testAnswerKey, testAnswer)(pensionsTakenModelFormat, userRequest, global)

      res.futureValue shouldBe testCacheMap
    }
  }

  "getFromSession" must {
    "return None when no data for this session exists" in {
      repository
        .putInSession(testAnswerKey, testAnswer)(pensionsTakenModelFormat, otherUserRequest, global)
        .futureValue

      val res = repository.getFromSession(testAnswerKey)(pensionsTakenModelFormat, userRequest)

      res.futureValue shouldBe None
    }

    "successfully return existing data" in {
      repository
        .putInSession(testAnswerKey, testAnswer)(pensionsTakenModelFormat, otherUserRequest, global)
        .futureValue
      repository.putInSession(testAnswerKey, testAnswer)(pensionsTakenModelFormat, userRequest, global).futureValue

      val res = repository.getFromSession(testAnswerKey)(pensionsTakenModelFormat, userRequest)

      res.futureValue shouldBe Some(testAnswer)
    }
  }

  "clearSession" must {
    "successfully remove session of a user" in {
      repository
        .putInSession(testAnswerKey, testAnswer)(pensionsTakenModelFormat, otherUserRequest, global)
        .futureValue
      repository.putInSession(testAnswerKey, testAnswer)(pensionsTakenModelFormat, userRequest, global).futureValue

      repository.clearSession(userRequest).futureValue

      repository
        .getFromSession[PensionDebitModel](testAnswerKey)(pensionsTakenModelFormat, userRequest)
        .futureValue shouldBe None
      repository
        .getFromSession[PensionDebitModel](testAnswerKey)(pensionsTakenModelFormat, otherUserRequest)
        .futureValue shouldBe Some(testAnswer)
    }
  }

}
