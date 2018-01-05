/*
 * Copyright 2018 HM Revenue & Customs
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

import config.WSHttp
import models.{Person, PersonalDetailsModel}
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneServerPerSuite
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future
import uk.gov.hmrc.http.{ HeaderCarrier, HttpResponse }

class CitizenDetailsConnectorSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite {

  val mockHttp : WSHttp = mock[WSHttp]
  object TestCitizenDetailsConnector extends CitizenDetailsConnector {
    val http = mockHttp
    val serviceUrl = "localhostUrl"
  }

  val tstDetails = PersonalDetailsModel(Person("McTestFace", "Testy"))
  val x = Json.toJson(tstDetails)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  override def beforeEach() {
    reset(mockHttp)
  }


  "CitizenDetailsConnector" should {
    "use the correct http" in {
      CitizenDetailsConnector.http shouldBe WSHttp
    }
  }

  "Calling getPersonDetails with valid response" should {
    "return a defined Option on PersonalDetailsModel" in {
      when(mockHttp.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(tstDetails)))))

      val response = TestCitizenDetailsConnector.getPersonDetails("tstNino")
      await(response) shouldBe Some(tstDetails)
    }
  }

  "Calling getPersonDetails with invalid response" should {
    "return an undefined Option on PersonalDetailsModel" in {
      when(mockHttp.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson("""name:NoName""")))))

      val response = TestCitizenDetailsConnector.getPersonDetails("tstNino")
      await(response) shouldBe None
    }
  }

  "Calling getPersonDetails with error response" should {
    "return an undefined Option on PersonalDetailsModel" in {
      when(mockHttp.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR)))

      val response = TestCitizenDetailsConnector.getPersonDetails("tstNino")
      await(response) shouldBe None
    }
  }
}
