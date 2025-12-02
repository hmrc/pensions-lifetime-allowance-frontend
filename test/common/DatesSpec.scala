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

package common

import models.TimeModel._
import testHelpers.FakeApplication

import java.time.LocalDateTime

class DatesSpec extends FakeApplication {

  "constructDateTimeFromAPIString" should {
    "correctly handle no time component" in {
      val apiString = "2025-09-22"
      constructDateTimeFromAPIString(apiString) shouldBe LocalDateTime.of(2025, 9, 22, 0, 0, 0)
    }

    "correctly handle time component without colons" in {
      val apiString = "2025-09-22T172309"
      constructDateTimeFromAPIString(apiString) shouldBe LocalDateTime.of(2025, 9, 22, 17, 23, 9)
    }

    "correctly handle time component with colons" in {
      val apiString = "2025-09-22T17:23:09"
      constructDateTimeFromAPIString(apiString) shouldBe LocalDateTime.of(2025, 9, 22, 17, 23, 9)
    }
  }

}
