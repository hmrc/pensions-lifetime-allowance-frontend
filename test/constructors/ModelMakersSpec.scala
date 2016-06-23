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

package constructors

import play.api.i18n.Messages
import uk.gov.hmrc.play.test.{WithFakeApplication, UnitSpec}
import play.api.libs.json.{JsValue, Json}
import models._

class ResponseConstructorsSpec extends UnitSpec with WithFakeApplication {
    object TestResponseConstructors extends ResponseConstructors {

    }

    val testSuccessResponseModel = SuccessResponseModel(
                                                "24",
                                                Some("FP16138722390C"),
                                                None,
                                                List("1","2","3")
                                                )

    val testRejectionResponseModel = RejectionResponseModel(
                                                "21",
                                                List.empty
                                                )

    "ResponseConstructors" should {

        "create the correct success model from Json" in {
            val jsn:JsValue = Json.parse("""{"certificateDate":"2016-05-10T17:20:55.138","nino":"AA123456A","notificationId":24,"protectionID":8243168284792526522,"protectionReference":"FP16138722390C","protectionType":"FP2016","status":"Open","version":1}""")
            TestResponseConstructors.createSuccessResponseFromJson(jsn) shouldBe testSuccessResponseModel
        }

        "create the correct rejection model from Json" in {
            val json:JsValue = Json.parse("""{"nino":"AA123456A","notificationId":21,"protectionID":-4645895724767334826,"protectionType":"FP2016","status":"Rejected","version":1}""")
            TestResponseConstructors.createRejectionResponseFromJson(json) shouldBe testRejectionResponseModel
        }
    }
}
