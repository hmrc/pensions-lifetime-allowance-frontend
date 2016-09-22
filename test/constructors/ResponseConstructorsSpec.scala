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

import uk.gov.hmrc.play.test.{WithFakeApplication, UnitSpec}
import play.api.libs.json.{JsValue, Json}
import models._
import enums.ApplicationType

class ResponseConstructorsSpec extends UnitSpec with WithFakeApplication {

  object TestResponseConstructors extends ResponseConstructors {
  }

  // SuccessResponseModel(protectionType: ApplicationType.Value, notificationId: String, protectedAmount: String, details: Option[ProtectionDetailsModel], additionalInfo: Seq[String])


  val testProtectionDetailsModel = ProtectionDetailsDisplayModel(protectionReference = Some("FP16138722390C"), psaReference = "testPSARef", applicationDate = Some("10 May 2016"))

  val testSuccessDisplayModel = SuccessDisplayModel(
    protectionType = ApplicationType.FP2016,
    notificationId = "23",
    protectedAmount = "£1,250,000",
    printable = true,
    details = Some(testProtectionDetailsModel),
    additionalInfo = List("1", "2")
  )

  val testNoPrintSuccessResponseModel = testSuccessDisplayModel.copy(notificationId = "24", printable = false, details = None)

  val testRejectionDisplayModel = RejectionDisplayModel(
    notificationId = "20",
    additionalInfo = List("1"),
    protectionType = ApplicationType.FP2016
  )

  "ResponseConstructors" should {

    "create the correct printable success model from Json" in {
      implicit val applicationType = ApplicationType.FP2016
      val jsn: JsValue = Json.parse(
        """{"certificateDate":"2016-05-10T17:20:55.138",
          |"nino":"AA123456A",
          |"notificationId":23,
          |"protectedAmount":1250000,
          |"protectionID":8243168284792526522,
          |"protectionReference":"FP16138722390C",
          |"protectionType":"FP2016",
          |"status":"Open",
          |"version":1,
          |"psaCheckReference":"testPSARef"}""".stripMargin)
      val testApplyResponseModel = TestResponseConstructors.createApplyResponseModelFromJson(jsn).get
      //Test against testapplyresponsemodel
    }

    "create the correct non-printable success model from Json" in {
      implicit val applicationType = ApplicationType.FP2016
      val jsn: JsValue = Json.parse(
        """{"certificateDate":"2016-05-10T17:20:55.138",
          |"nino":"AA123456A","notificationId":24,
          |"protectedAmount":1250000,
          |"protectionID":8243168284792526522,
          |"protectionReference":"FP16138722390C",
          |"protectionType":"FP2016",
          |"status":"Open",
          |"version":1,
          |"psaCheckReference":"testPSARef"}""".stripMargin)
      val testApplyResponseModel = TestResponseConstructors.createApplyResponseModelFromJson(jsn).get
      //Test against testapplyresponsemodel
    }

    "create the correct rejection model from Json" in {
      implicit val applicationType = ApplicationType.FP2016
      val jsn: JsValue = Json.parse(
        """{"nino":"AA123456A",
          |"notificationId":20,
          |"protectionID":-4645895724767334826,
          |"protectionType":"FP2016",
          |"status":"Rejected",
          |"version":1}""".stripMargin)
      val testApplyResponseModel = TestResponseConstructors.createApplyResponseModelFromJson(jsn).get
      //Test against testapplyresponsemodel
    }
  }
}
