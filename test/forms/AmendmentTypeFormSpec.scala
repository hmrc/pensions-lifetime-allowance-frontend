/*
 * Copyright 2020 HM Revenue & Customs
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

package forms

import AmendmentTypeForm._
import models.amendModels.AmendmentTypeModel
import testHelpers.CommonErrorMessages
import uk.gov.hmrc.play.test.UnitSpec

class AmendmentTypeFormSpec extends UnitSpec with CommonErrorMessages {

  "The AmendmentTypeForm" should {
    val validMap =  Map("protectionType" -> "type", "status" -> "status")

    "produce a valid form" when {

      "provided with a valid model" in {
        val model = AmendmentTypeModel("type", "status")

        amendmentTypeForm.fill(model).data shouldBe validMap
      }

      "provided with a valid map" in {
        val map = Map("protectionType" -> "anotherType", "status" -> "anotherStatus")

        amendmentTypeForm.bind(map).value shouldBe Some(AmendmentTypeModel("anotherType", "anotherStatus"))
      }
    }

    "produce an invalid form" which {

      "has one error with the correct error message" when {

        "provided with no type" in {
          val map = validMap - "protectionType"
          val result = amendmentTypeForm.bind(map)

          result.errors.size shouldBe 1
          result.error("protectionType").get.message shouldBe errorRequired
        }

        "provided with no status" in {
          val map = validMap - "status"
          val result = amendmentTypeForm.bind(map)

          result.errors.size shouldBe 1
          result.error("status").get.message shouldBe errorRequired
        }
      }


    }
  }
}
