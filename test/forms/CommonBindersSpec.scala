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

package forms

import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Lang
import testHelpers.{FakeApplication, PSODetailsMessages}


class CommonBindersSpec extends FakeApplication with PSODetailsMessages with MockitoSugar {
  implicit val lang: Lang = mock[Lang]
  object testForm extends CommonBinders

  "stringToOptionalIntFormatter form binder" should {
    "return a form error" when {
      "given an incorrect data type" in {
        val testMap = Map(
          "pso.day" -> "P",
          "pso.month" -> "5",
          "pso.year" -> "2016",
          "psoAmt" -> "0.0",
          "protectionType" -> "ip2016",
          "status" -> "status",
          "existingPSO" -> "true")


        val result = AmendPSODetailsForm.amendPsoDetailsForm.bind(testMap)

        result.errors.size shouldBe 1
        result.error("pso.day").get.message shouldBe errorRealKey
      }
    }
  }
}
