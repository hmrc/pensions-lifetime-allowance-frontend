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

package controllers

import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.test.{WithFakeApplication, UnitSpec}
import org.scalatest.mock.MockitoSugar
import config.{FrontendAppConfig,FrontendAuthConnector}
import play.api.libs.json.{JsValue, Json}
import connectors.PLAConnector


class ResultControllerSpec extends UnitSpec with MockitoSugar with WithFakeApplication {

  object TestResultController extends ResultController {
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val postSignInRedirectUrl = "http://localhost:9012/protect-your-lifetime-allowance/confirm-fp"

  override val plaConnector = mock[PLAConnector]

  }

  val successJson = Json.parse("""{"certificateDate":"2016-05-10T17:20:55.138","nino":"AA123456A","notificationId":24,"protectionID":8243168284792526522,"protectionReference":"FP16138722390C","protectionType":"FP2016","status":"Open","version":1}""")
  val rejectionJson = Json.parse("""{"nino":"AA123456A","notificationId":21,"protectionID":-4645895724767334826,"protectionType":"FP2016","status":"Rejected","version":1}""")

  val testSuccessResponse = HttpResponse(200,Some(successJson))

  val testRejectionResponse = HttpResponse(409,Some(rejectionJson))

  ///////////////////////////////////////////////
  // Initial Setup
  ///////////////////////////////////////////////
  "ResultController should be correctly initialised" in {
    ResultController.authConnector shouldBe FrontendAuthConnector
  }

  "ResultController" should {

    "obtain the correct outcome form a successful FP application" in {
      TestResultController.applicationOutcome(testSuccessResponse) shouldBe("successful")
    }

    "obtain the correct outcome form an unsuccessful FP application" in {
      TestResultController.applicationOutcome(testRejectionResponse) shouldBe("rejected")
    }
  }


}
