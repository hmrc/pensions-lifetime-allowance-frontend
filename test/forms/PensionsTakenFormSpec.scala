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

package forms

import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import PensionsTakenForm._
import models.PensionsTakenModel
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Lang
import testHelpers.CommonErrorMessages

class PensionsTakenFormSpec extends UnitSpec with WithFakeApplication with CommonErrorMessages with MockitoSugar {
  implicit val lang: Lang = mock[Lang]

  "The PensionsTakenForm" should {

    "return a valid form" when {

      "supplied with a valid model" in {
        val model = PensionsTakenModel(Some("text"))
        val result = pensionsTakenForm.fill(model)

        result.data shouldBe Map("pensionsTaken" -> "text")
      }

      "supplied with a valid form" in {
        val map = Map("pensionsTaken" -> "text")
        val result = pensionsTakenForm.bind(map)

        result.value shouldBe Some(PensionsTakenModel(Some("text")))
      }
    }

    "return an invalid form with one error and the correct message" when {

      "not provided with a pensionsTaken value" in {
        val map = Map.empty[String, String]
        val result = pensionsTakenForm.bind(map)

        result.errors.size shouldBe 1
        result.error("pensionsTaken").get.message shouldBe errorQuestion
      }
    }
  }
}
