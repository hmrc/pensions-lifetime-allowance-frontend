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

package connectors

import java.util.UUID

import config.PLASessionCache
import models._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.Mode.Mode
import play.api.{Application, Configuration, Environment}
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.{CacheMap, SessionCache}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.SessionId

class KeyStoreConnectorSpec extends UnitSpec with MockitoSugar with WithFakeApplication {

  val mockSessionCache = mock[SessionCache]
  val sessionId = UUID.randomUUID.toString
  val env = mock[Environment]



  object TestKeyStoreConnector extends KeyStoreConnector {
    override val sessionCache = mockSessionCache

    override protected def mode: Mode = mock[Mode]

    override protected def runModeConfiguration: Configuration = mock[Configuration]
  }


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
