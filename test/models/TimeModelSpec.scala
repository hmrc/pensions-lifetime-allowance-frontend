/*
 * Copyright 2025 HM Revenue & Customs
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

package models

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsBoolean, JsError, JsNull, JsNumber, JsString, JsSuccess, JsValue, Json}

class TimeModelSpec extends AnyWordSpec with Matchers {

  "reads" should {
    "correctly parse valid time string" when
      Seq(
        "093022" -> TimeModel.of(9, 30, 22),
        "155627" -> TimeModel.of(15, 56, 27),
        "000001" -> TimeModel.of(0, 0, 1)
      ).foreach { case (timeString, timeModel) =>
        s"provided with '$timeString'" in {
          TimeModel.reads.reads(JsString(timeString)) shouldBe JsSuccess(timeModel)
        }
      }

    "reject correctly formatted, but invalid time string" when
      Seq(
        "250101",
        "999999",
        "126100",
        "130477"
      ).foreach { timeString =>
        s"provided with '$timeString'" in {
          TimeModel.reads.reads(JsString(timeString)) shouldBe JsError("invalid time string")
        }
      }

    "reject incorrectly formatted time string" when
      Seq(
        "11:34:02",
        "123456.000",
        "12:43:65.000",
        "2025-12-03T13:44:12.000"
      ).foreach { timeString =>
        s"provided with '$timeString'" in {
          TimeModel.reads.reads(JsString(timeString)) shouldBe JsError("invalid time string")
        }
      }

    "reject non-string Js value" when
      Seq[JsValue](
        JsNumber(2025),
        Json.obj("year" -> JsNumber(2025), "month" -> JsNumber(12), "day" -> JsNumber(8)),
        JsBoolean(true),
        JsNull,
        Json.arr(JsNumber(2025), JsNumber(12), JsNumber(8))
      ).foreach { timeValue =>
        s"provided with $timeValue" in {
          TimeModel.reads.reads(timeValue) shouldBe JsError("time must be a string")
        }
      }
  }

  "writes" should {
    "correctly format time" when
      Seq(
        TimeModel.of(9, 30, 20)  -> "093020",
        TimeModel.of(12, 34, 56) -> "123456",
        TimeModel.of(23, 23, 23) -> "232323"
      ).foreach { case (timeModel, timeString) =>
        s"provided with $timeModel" in {
          TimeModel.writes.writes(timeModel) shouldBe JsString(timeString)
        }
      }
  }

}
