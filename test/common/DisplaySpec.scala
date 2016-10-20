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

package common

import uk.gov.hmrc.play.test.UnitSpec
import java.time.LocalDate
import common.Display._

class DisplaySpec extends UnitSpec {

  "currencyDisplayString" should {

    "correctly create a currency string for 1000.45" in { 
      val tstAmt = BigDecimal(1000.45)
      currencyDisplayString(tstAmt) shouldBe "£1,000.45"
    }

    "correctly create a currency string for -1000.45" in {
      val tstAmt = BigDecimal(-1000.45)
      currencyDisplayString(tstAmt) shouldBe "£-1,000.45"
    }

    "create a currency string for 7" in { 
      val tstAmt = BigDecimal(7)
      currencyDisplayString(tstAmt) shouldBe "£7"
    }

    "create a currency string for 3.9" in { 
      val tstAmt = BigDecimal(3.9)
      currencyDisplayString(tstAmt) shouldBe "£3.90"
    }
  }

  "dateDisplayString" should {

    "correctly create a date string for 17/04/2018" in { 
      val tstDate = LocalDate.of(2018, 4, 17)
      dateDisplayString(tstDate) shouldBe "17 April 2018"
    }
  }

}
