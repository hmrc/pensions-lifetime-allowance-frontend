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

import config.FrontendAppConfig
import models.{Person, PersonalDetailsModel}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.test.Helpers._
import testHelpers.FakeApplication
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

import scala.concurrent.Future

class CitizenDetailsConnectorSpec extends FakeApplication with MockitoSugar {

  val mockAppConfig = fakeApplication.injector.instanceOf[FrontendAppConfig]
  val mockHttp = mock[DefaultHttpClient]

  val tstDetails = PersonalDetailsModel(Person("McTestFace", "Testy"))
  val x = Json.toJson(tstDetails)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  class Setup {
    val controller = new CitizenDetailsConnector(mockAppConfig, mockHttp)
  }

  "Calling getPersonDetails with valid response" should {
    "return a defined Option on PersonalDetailsModel" in new Setup {
      when(mockHttp.GET[HttpResponse](ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(HttpResponse(status = OK, json = Json.toJson(tstDetails), headers = Map.empty)))

      val response = controller.getPersonDetails("tstNino")
      await(response) shouldBe Some(tstDetails)
    }
  }

  "Calling getPersonDetails with invalid response" should {
    "return an undefined Option on PersonalDetailsModel" in new Setup {
      when(mockHttp.GET[HttpResponse](ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(HttpResponse(status = OK, json = Json.toJson("""name:NoName"""), headers = Map.empty)))

      val response = controller.getPersonDetails("tstNino")
      await(response) shouldBe None
    }
  }

  "Calling getPersonDetails with error response" should {
    "return an undefined Option on PersonalDetailsModel" in new Setup {
      when(mockHttp.GET[HttpResponse](ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR, "")))

      val response = controller.getPersonDetails("tstNino")
      await(response) shouldBe None
    }
  }
}
