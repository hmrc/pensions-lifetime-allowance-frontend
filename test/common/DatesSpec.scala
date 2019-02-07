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

package common

import uk.gov.hmrc.play.test.UnitSpec
import common.Dates._
import java.time.LocalDate

class DatesSpec extends UnitSpec {

  "constructDate" should {

    "correctly create a LocalDate" in { 
      val tstDate = LocalDate.of(2016, 6, 4)
      constructDate(4, 6, 2016) shouldBe tstDate
    }
  }

  "dateBefore" should {

    "return true when comparing 01/01/2016 to 02/02/2016" in {
        dateBefore(1, 1, 2016, LocalDate.of(2016, 2, 2)) shouldBe true
    }

    "return false when comparing 02/02/2016 to 01/01/2016" in {
        dateBefore(2, 2, 2016, LocalDate.of(2016, 1, 1)) shouldBe false
    }

    "return false when comparing the same dates" in {
        dateBefore(2, 2, 2016, LocalDate.of(2016, 2, 2)) shouldBe false
    }
  }

  "dateAfter" should {

    "return false when comparing 01/01/2016 to 02/02/2016" in {
        dateAfter(1, 1, 2016, LocalDate.of(2016, 2, 2)) shouldBe false
    }

    "return true when comparing 02/02/2016 to 01/01/2016" in {
        dateAfter(2, 2, 2016, LocalDate.of(2016, 1, 1)) shouldBe true
    }

    "return false when comparing the same dates" in {
        dateAfter(2, 2, 2016, LocalDate.of(2016, 2, 2)) shouldBe false
    }
  }

  "futureDate" should {
    val today = LocalDate.now
    val tomorrow = today.plusDays(1)
    val yesterday = today.minusDays(1)

    "return true for tomorrow" in {
      futureDate(tomorrow.getDayOfMonth, tomorrow.getMonthValue, tomorrow.getYear) shouldBe true
    }

    "return false for today" in {
      futureDate(today.getDayOfMonth, today.getMonthValue, today.getYear) shouldBe false
    }

    "return false for yesterday" in {
      futureDate(yesterday.getDayOfMonth, yesterday.getMonthValue, yesterday.getYear) shouldBe false
    }
  }

}
