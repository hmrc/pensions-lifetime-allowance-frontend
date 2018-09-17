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
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.Mode.Mode
import play.api.{Application, Configuration, Environment}
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

class CitizenDetailsConnectorSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach {


//  val mockHttp : WSHttp = mock[WSHttp]
//  val env = mock[Environment]
//
//  val connector = new CitizenDetailsConnector(fakeApplication.configuration, env)
//
//  val tstDetails = PersonalDetailsModel(Person("McTestFace", "Testy"))
//  val x = Json.toJson(tstDetails)
//
//  implicit val hc: HeaderCarrier = HeaderCarrier()
//
//  override def beforeEach() {
//    reset(mockHttp)
//  }
//

//  class Setup {
//    val TestCitizenDetailsConnector = new CitizenDetailsConnector {
//      def config: Configuration = mock[Configuration]
//      def env: Environment = mock[Environment]
//    }
//  }


  val mockHttp : WSHttp = mock[WSHttp]
  object TestCitizenDetailsConnector extends CitizenDetailsConnector {
    val http = mockHttp
    val serviceUrl = "localhostUrl"

    override protected def mode: Mode = mock[Mode]

    override protected def runModeConfiguration: Configuration = mock[Configuration]
  }

  val tstDetails = PersonalDetailsModel(Person("McTestFace", "Testy"))
  val x = Json.toJson(tstDetails)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  override def beforeEach() {
    reset(mockHttp)
  }



  "Calling getPersonDetails with valid response" should {
    "return a defined Option on PersonalDetailsModel" in {
      when(mockHttp.GET[HttpResponse](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(tstDetails)))))

      val response = TestCitizenDetailsConnector.getPersonDetails("tstNino")
      await(response) shouldBe Some(tstDetails)
    }
  }

  "Calling getPersonDetails with invalid response" should {
    "return an undefined Option on PersonalDetailsModel" in {
      when(mockHttp.GET[HttpResponse](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson("""name:NoName""")))))

      val response = TestCitizenDetailsConnector.getPersonDetails("tstNino")
      await(response) shouldBe None
    }
  }

  "Calling getPersonDetails with error response" should {
    "return an undefined Option on PersonalDetailsModel" in {
      when(mockHttp.GET[HttpResponse](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR)))

      val response = TestCitizenDetailsConnector.getPersonDetails("tstNino")
      await(response) shouldBe None
    }
  }
}
