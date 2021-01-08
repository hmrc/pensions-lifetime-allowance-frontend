/*
 * Copyright 2021 HM Revenue & Customs
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

import java.util.UUID

import config.PLASessionCache
import models._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.Environment
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.logging.SessionId
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.{ExecutionContext, Future}

class KeyStoreConnectorSpec extends UnitSpec with MockitoSugar with WithFakeApplication {

  val mockSessionCache     = mock[PLASessionCache]
  val sessionId            = UUID.randomUUID.toString
  val mockEnv: Environment = mock[Environment]
  implicit val executionContext = fakeApplication.injector.instanceOf[ExecutionContext]

  object TestKeyStoreConnector extends KeyStoreConnector(mockSessionCache)

  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(sessionId.toString)))

  "Calculator Connector" should {

    "fetch and get from keystore" in {
      val testModel = PensionsTakenModel(Some("No"))
      when(mockSessionCache.fetchAndGetEntry[PensionsTakenModel](ArgumentMatchers.anyString())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Option(testModel)))

      lazy val result = TestKeyStoreConnector.fetchAndGetFormData[PensionsTakenModel]("willAddToPension")
      await(result) shouldBe Some(testModel)
    }

    "save form data to keystore" in {
      val testModel = PensionsTakenModel(Some("No"))
      val returnedCacheMap = CacheMap("haveAddedToPension", Map("data" -> Json.toJson(testModel)))
      when(mockSessionCache.cache[PensionsTakenModel](ArgumentMatchers.anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(returnedCacheMap))

      lazy val result = TestKeyStoreConnector.saveFormData("haveAddedToPension", testModel)
      await(result) shouldBe returnedCacheMap
    }

    "save data to keystore" in {
      val testModel = PensionsTakenModel(Some("No"))
      val returnedCacheMap = CacheMap("haveAddedToPension", Map("data" -> Json.toJson(testModel)))
      when(mockSessionCache.cache[PensionsTakenModel](ArgumentMatchers.anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(returnedCacheMap))

      lazy val result = TestKeyStoreConnector.saveData("haveAddedToPension", testModel)
      await(result) shouldBe returnedCacheMap
    }
  }
}
