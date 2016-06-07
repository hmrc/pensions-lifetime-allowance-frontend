/*
 * Copyright 2016 HM Revenue & Customs
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

import config.WSHttp
import models.ApplyFP16Model
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneServerPerSuite
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.{HttpResponse, HeaderCarrier}
import uk.gov.hmrc.play.http.ws.WSHttp
import uk.gov.hmrc.play.test.UnitSpec
import scala.collection.immutable.List

import scala.concurrent.Future

class PLAConnectorSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite {

  val mockHttp : WSHttp = mock[WSHttp]

  object TestPLAConnector extends PLAConnector {
    val http = mockHttp
    val stubUrl = "http://localhost:9012"
  }

  val validApplyFP16Json = """{"protectionType":"FP2016"}"""
  val nino = "AB999999C"

  implicit val hc: HeaderCarrier = HeaderCarrier()

  override def beforeEach() {
    reset(mockHttp)
  }

  "PLAConnector" should {
    "use the correct http" in {
      PLAConnector.http shouldBe WSHttp
    }
    "use the correct stubUrl" in {
      PLAConnector.stubUrl shouldBe "http://localhost:9011"
    }
  }

  "Calling applyFP16" should {
    "use valid Json for FP 16 request" in {
      Json.toJson[ApplyFP16Model](ApplyFP16Model("FP2016")).toString shouldBe validApplyFP16Json
    }
    "should return a 200 from a valid apply FP16 request" in {
      when(mockHttp.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(OK)))
      val response = TestPLAConnector.applyFP16(nino)
      await(response).status shouldBe OK
    }
  }
}
