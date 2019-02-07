/*
 * Copyright 2019 HM Revenue & Customs
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

import com.kenshoo.play.metrics.PlayModule
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import play.api.libs.json.{JsValue, Json}
import models._
import enums.ApplicationType

class ResponseConstructorsSpec extends UnitSpec with WithFakeApplication {
  override def bindModules = Seq(new PlayModule)

  object TestResponseConstructors extends ResponseConstructors {
  }

  val testFPSuccessProtectionModel = ProtectionModel (
    Some("testPSARef"),
    notificationId = Some(24),
    protectionID = Some(12345),
    protectionType = Some("FP2016"),
    certificateDate = Some("2016-04-17"),
    protectionReference = Some("PSA123456"))
  val testFPSuccessApplyResponseModel     = ApplyResponseModel(testFPSuccessProtectionModel)

  "ResponseConstructors" should {

    "create the correct ApplyResponse model from Json" in {
      implicit val applicationType = ApplicationType.FP2016
      val jsn: JsValue = Json.parse(
        """{"certificateDate":"2016-04-17",
          |"notificationId":24,
          |"protectionID":12345,
          |"protectionReference":"PSA123456",
          |"protectionType":"FP2016",
          |"psaCheckReference":"testPSARef"}""".stripMargin)
      val testApplyResponseModel = TestResponseConstructors.createApplyResponseModelFromJson(jsn).get
      testApplyResponseModel shouldBe testFPSuccessApplyResponseModel
    }

    "return None if an ApplyResponse model can't be created" in {
      implicit val applicationType = ApplicationType.FP2016
      val jsn: JsValue = Json.parse(
        """{"protectionID":"wrong"
          |}""".stripMargin)
      val testApplyResponseModel = TestResponseConstructors.createApplyResponseModelFromJson(jsn)
      testApplyResponseModel shouldBe None
    }

    "create the correct TransformedReadResponse model from Json" in {
      val tstPSACheckRef = "testPsaRef"

      val tstProtectionModelOpen = ProtectionModel (
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(2),
        status = Some("Open"),
        version = Some(2)
      )
      val tstProtectionModelDormant = ProtectionModel (
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(1),
        status = Some("Withdrawn"),
        version = Some(1)
      )

      val jsn: JsValue = Json.parse(
        """{
          |"psaCheckReference":"testPsaRef",
          |"lifetimeAllowanceProtections":
            |[
              |{
                | "protectionID":1,
                | "status":"Withdrawn",
                | "version":1
              |},
              |{
                |"protectionID":2,
                |"status":"Open",
                |"version":2
              |}
            |]
          |}
        """.stripMargin)
      val tstTransformedReadResponseModel = TransformedReadResponseModel(Some(tstProtectionModelOpen), List(tstProtectionModelDormant))
      ResponseConstructors.createTransformedReadResponseModelFromJson(jsn) shouldBe Some(tstTransformedReadResponseModel)

    }

    "return None if a TransformedReadResponse model can't be created" in {

      val jsn: JsValue = Json.parse(
        """{"psaCheckReference":"wrong"
          |}""".stripMargin)

      ResponseConstructors.createTransformedReadResponseModelFromJson(jsn) shouldBe None
    }
  }
}
