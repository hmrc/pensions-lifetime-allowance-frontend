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
import play.api.http._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.play.test.{WithFakeApplication, UnitSpec}
import org.scalatest.mock.MockitoSugar
import config.{FrontendAppConfig,FrontendAuthConnector}
import models.SuccessResponseModel
import play.api.libs.json.{JsValue, Json}
import connectors.APIConnector


class ResultControllerSpec extends UnitSpec with MockitoSugar with WithFakeApplication {

  object TestResultController extends ResultController {
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val postSignInRedirectUrl = "http://localhost:9012/protect-your-lifetime-allowance/confirm-fp"

  override val apiConnector = mock[APIConnector]

  }

  val testSuccessResponse = SuccessResponseModel(
                                                "24",
                                                Some("FP16138722390C"),
                                                None,
                                                List("1","2","3")
                                                )



  "ResultController" should {
    "create the corect success model from Json" in {
      val json:JsValue = Json.parse("""{"certificateDate":"2016-05-10T17:20:55.138","nino":"AA123456A","notificationId":24,"protectionID":8243168284792526522,"protectionReference":"FP16138722390C","protectionType":"FP2016","status":"Open","version":1}""")
      TestResultController.createSuccessResponseFromJson(json) shouldBe testSuccessResponse
    }
  }


}
