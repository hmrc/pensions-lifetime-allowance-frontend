/*
 * Copyright 2022 HM Revenue & Customs
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

package common

import org.scalatest.{Matchers, OptionValues, WordSpecLike}
import common.Validation._

class ValidationSpec extends WordSpecLike with Matchers with OptionValues {

  "Two decimal places check" should {

    "pass for 1" in { isMaxTwoDecimalPlaces(1) shouldBe true }
    "pass for 0.5" in { isMaxTwoDecimalPlaces(0.5) shouldBe true }
    "pass for 17.34" in { isMaxTwoDecimalPlaces(17.34) shouldBe true }
    "pass for 10.34000" in { isMaxTwoDecimalPlaces(10.34000) shouldBe true }
    "pass for .25" in { isMaxTwoDecimalPlaces(.25) shouldBe true }
    "fail for 0.222" in { isMaxTwoDecimalPlaces(0.222) shouldBe false }
    "fail for 3.004" in { isMaxTwoDecimalPlaces(3.004) shouldBe false }
  }

  "Is positive check" should {

  	"pass for 1" in { isPositive(1) shouldBe true }
  	"pass for 0" in { isPositive(0) shouldBe true }
  	"pass for 0.0000001" in { isPositive(0.0000001) shouldBe true }
  	"fail for -1" in { isPositive(-1) shouldBe false }
  	"fail for -0.0000001" in { isPositive(-0.0000001) shouldBe false }
  }

  "Is valid date" should {
    "pass for 29/2/2016" in { isValidDate(29, 2, 2016) shouldBe true }
    "fail for 29/2/2015" in { isValidDate(29, 2, 2015) shouldBe false }
    "fail for -3/2/2015" in { isValidDate(-3, 2, 2015) shouldBe false }
  }
}
