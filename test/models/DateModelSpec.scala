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

class DateModelSpec extends AnyWordSpec with Matchers {

  "reads" should {
    "correctly parse valid date string" when
      Seq(
        "2025-12-08" -> DateModel.of(2025, 12, 8),
        "2016-01-31" -> DateModel.of(2016, 1, 31),
        "2020-02-29" -> DateModel.of(2020, 2, 29)
      ).foreach { case (dateString, dateModel) =>
        s"provided with '$dateString'" in {
          DateModel.reads.reads(JsString(dateString)) shouldBe JsSuccess(dateModel)
        }
      }

    "reject correctly formatted, but invalid date string" when
      Seq(
        "2025-02-29",
        "2022-07-32",
        "2021-06-31",
        "2014-00-01",
        "2025-31-01"
      ).foreach { dateString =>
        s"provided with '$dateString'" in {
          DateModel.reads.reads(JsString(dateString)) shouldBe JsError("invalid date string")
        }
      }

    "reject incorrectly formatted date string" when
      Seq(
        "08/12/2025",
        "The fourth of may",
        "2025,31,01",
        "2025-12-03T13:44:12.000"
      ).foreach { dateString =>
        s"provided with '$dateString'" in {
          DateModel.reads.reads(JsString(dateString)) shouldBe JsError("invalid date string")
        }
      }

    "reject non-string Js value" when
      Seq[JsValue](
        JsNumber(2025),
        Json.obj("year" -> JsNumber(2025), "month" -> JsNumber(12), "day" -> JsNumber(8)),
        JsBoolean(true),
        JsNull,
        Json.arr(JsNumber(2025), JsNumber(12), JsNumber(8))
      ).foreach { dateValue =>
        s"provided with $dateValue" in {
          DateModel.reads.reads(dateValue) shouldBe JsError("date must be a string")
        }
      }
  }

  "writes" should {
    "correctly format date" when
      Seq(
        DateModel.of(2025, 4, 1)  -> "2025-04-01",
        DateModel.of(2020, 2, 29) -> "2020-02-29",
        DateModel.of(2016, 9, 22) -> "2016-09-22"
      ).foreach { case (dateModel, dateString) =>
        s"provided with $dateModel" in {
          DateModel.writes.writes(dateModel) shouldBe JsString(dateString)
        }
      }
  }

}
