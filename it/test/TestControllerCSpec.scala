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

import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import utils.{IntegrationBaseSpec, MockedAudit}

import scala.concurrent.Future

class TestControllerCSpec extends IntegrationBaseSpec with MockedAudit {

  val protectionInsertUrl = s"$localUrl/protect-your-lifetime-allowance/test-only/protections/insert"

  "Hitting the /protections/insert route" should {
    "return a 200 and valid result for protections" when {
      "nino: AA123456" in {

        stubPost("/test-only/protections/insert", OK , "")

        def request: Future[WSResponse] = ws.url(protectionInsertUrl)
          .addHttpHeaders(("Csrf-Token" , "nocheck"),("Content-Type" , "application/json"))
          .post(
            Json.parse(
              """{
                |  "pensionSchemeAdministratorCheckReference": "PSA12345678A",
                |  "nino": "AA123456",
                |  "id": 4294967270,
                |  "version": 10,
                |  "type": 2,
                |  "status": 1,
                |  "protectionReference": "FAKE1PRIMARY",
                |  "certificateDate" : "2015-12-18",
                |  "certificateTime" : "14:30:40",
                |  "protectedAmount": 1250000.00,
                |  "relevantAmount": 2000000.00,
                |  "uncrystallisedRights" : 500000.00,
                |  "preADayPensionInPayment": 500000.00,
                |  "postADayBCE": 500000.00,
                |  "nonUKRights": 500000.00
                |}""".stripMargin)
          )

        await(request).status shouldBe OK

        verify(postRequestedFor(urlEqualTo("/test-only/protections/insert"))
          .withRequestBody(equalToJson(Json.parse(
            s"""
               |{
               |  "pensionSchemeAdministratorCheckReference": "PSA12345678A",
               |  "nino": "AA123456",
               |  "id": 4294967270,
               |  "version": 10,
               |  "type": 2,
               |  "status": 1,
               |  "protectionReference": "FAKE1PRIMARY",
               |  "certificateDate" : "2015-12-18",
               |  "certificateTime" : "14:30:40",
               |  "protectedAmount": 1250000.00,
               |  "relevantAmount": 2000000.00,
               |  "uncrystallisedRights" : 500000.00,
               |  "preADayPensionInPayment": 500000.00,
               |  "postADayBCE": 500000.00,
               |  "nonUKRights": 500000.00
               |}
          """.stripMargin).toString()
          )
          )
        )
      }
    }
  }

  val protectionDeleteAllUrl = s"$localUrl/protect-your-lifetime-allowance/test-only/protections/removeAll"

  "Hitting the /protections/removeAll route" should {
    "return a 200 and valid result for deletion" when {
      "deleting all" in {

        stubDelete("/test-only/protections/removeAll", OK , "All protections deleted" )

        def request: Future[WSResponse] = ws.url(protectionDeleteAllUrl)
          .addHttpHeaders(("Csrf-Token" , "nocheck"),("Content-Type" , "application/json"))
          .delete()

        await(request).status shouldBe OK
        await(request).body shouldBe  "All protections deleted"
      }
    }
  }

  val nino = "AA123456"
  val protectionDeleteNinoUrl = s"$localUrl/protect-your-lifetime-allowance/test-only/individuals/$nino/protections"

  "Hitting the /individuals/:nino/protections route" should {
    "return a 200 and valid result for nino deletion" when {
      "deleting a specific nino" in {

        stubDelete(s"/test-only/individuals/$nino/protections", OK , s"$nino deleted" )

        def request: Future[WSResponse] = ws.url(protectionDeleteNinoUrl)
          .addHttpHeaders(("Csrf-Token" , "nocheck"),("Content-Type" , "application/json"))
          .delete()

        await(request).status shouldBe OK
        await(request).body shouldBe  s"$nino deleted"
      }
    }
  }
}

