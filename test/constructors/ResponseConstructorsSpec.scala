/*
 * Copyright 2023 HM Revenue & Customs
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

import models._
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsValue, Json}
import testHelpers.FakeApplication

class ResponseConstructorsSpec extends FakeApplication with MockitoSugar {

  val mockResponseConstructor: ResponseConstructors = fakeApplication().injector.instanceOf[ResponseConstructors]

  val testFPSuccessProtectionModel: ProtectionModel = ProtectionModel(
    Some("testPSARef"),
    notificationId = Some(24),
    protectionID = Some(12345),
    protectionType = Some("FP2016"),
    certificateDate = Some("2016-04-17"),
    protectionReference = Some("PSA123456")
  )

  val testFPSuccessApplyResponseModel: ApplyResponseModel = ApplyResponseModel(testFPSuccessProtectionModel)

  "ResponseConstructors" should {

    "create the correct ApplyResponse model from Json" in {
      val jsn: JsValue = Json.parse("""{"certificateDate":"2016-04-17",
                                      |"notificationId":24,
                                      |"protectionID":12345,
                                      |"protectionReference":"PSA123456",
                                      |"protectionType":"FP2016",
                                      |"psaCheckReference":"testPSARef"}""".stripMargin)
      val testApplyResponseModel = mockResponseConstructor.createApplyResponseModelFromJson(jsn).get
      testApplyResponseModel shouldBe testFPSuccessApplyResponseModel
    }

    "return None if an ApplyResponse model can't be created" in {
      val jsn: JsValue = Json.parse("""{"protectionID":"wrong"
                                      |}""".stripMargin)
      val testApplyResponseModel = mockResponseConstructor.createApplyResponseModelFromJson(jsn)
      testApplyResponseModel shouldBe None
    }
  }

}
