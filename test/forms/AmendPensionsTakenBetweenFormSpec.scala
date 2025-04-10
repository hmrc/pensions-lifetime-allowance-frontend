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

import forms.AmendPensionsTakenBetweenForm._
import models.amendModels.AmendPensionsTakenBetweenModel
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Lang
import testHelpers.{CommonErrorMessages, FakeApplication}

class AmendPensionsTakenBetweenFormSpec extends FakeApplication with CommonErrorMessages with MockitoSugar {
  implicit val lang: Lang = mock[Lang]

  val messageKey = "pensionsTakenBetween"

  "The AmendPensionsTakenBetweenForm" should {
    val validMap = Map("amendedPensionsTakenBetween" -> "yes")

    "produce a valid form with additional validation" when {

      "provided with a valid model" in {
        val model  = AmendPensionsTakenBetweenModel("yes")
        val result = amendPensionsTakenBetweenForm("ip2016").fill(model)

        result.data shouldBe validMap
      }

      "provided with a valid map with no amount" in {
        val map    = Map("amendedPensionsTakenBetween" -> "no")
        val result = amendPensionsTakenBetweenForm("ip2016").bind(map)

        result.value shouldBe Some(AmendPensionsTakenBetweenModel("no"))
      }
    }

    "produce an invalid form".which {

      "has one error with the correct error message" when {

        "not provided with a value for amendedPensionsTakenBetween" in {
          val map    = validMap - "amendedPensionsTakenBetween"
          val result = amendPensionsTakenBetweenForm("ip2016").bind(map)

          result.errors.size shouldBe 1
          result.error("amendedPensionsTakenBetween").get.message shouldBe errorQuestion(messageKey, "ip2016")
        }
      }
    }
  }

}
