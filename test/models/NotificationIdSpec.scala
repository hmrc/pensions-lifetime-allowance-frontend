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

import models.NotificationId._
import models.pla.response.AmendProtectionResponseStatus._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsBoolean, JsError, JsNull, JsNumber, JsString, JsSuccess, JsValue, Json}

class NotificationIdSpec extends AnyWordSpec with Matchers {

  val numbers = Seq(
    1  -> NotificationId1,
    2  -> NotificationId2,
    3  -> NotificationId3,
    4  -> NotificationId4,
    5  -> NotificationId5,
    6  -> NotificationId6,
    7  -> NotificationId7,
    8  -> NotificationId8,
    9  -> NotificationId9,
    10 -> NotificationId10,
    11 -> NotificationId11,
    12 -> NotificationId12,
    13 -> NotificationId13,
    14 -> NotificationId14
  )

  "toInt" should {
    "return the correct number" when
      numbers.foreach { case (number, notificationId) =>
        s"provided with notification $notificationId" in {
          notificationId.toInt shouldBe number
        }
      }
  }

  "status" should {
    "return the correct status" when
      Seq(
        NotificationId1  -> Open,
        NotificationId2  -> Dormant,
        NotificationId3  -> Dormant,
        NotificationId4  -> Dormant,
        NotificationId5  -> Open,
        NotificationId6  -> Withdrawn,
        NotificationId7  -> Withdrawn,
        NotificationId8  -> Open,
        NotificationId9  -> Dormant,
        NotificationId10 -> Dormant,
        NotificationId11 -> Dormant,
        NotificationId12 -> Dormant,
        NotificationId13 -> Withdrawn,
        NotificationId14 -> Withdrawn
      ).foreach { case (notificationId, status) =>
        s"provided with notification $notificationId" in {
          notificationId.status shouldBe status
        }
      }
  }

  "reads" should {
    "correctly parse valid notification id" when
      numbers.foreach { case (number, notificationId) =>
        s"provided with '$number'" in {
          NotificationId.reads.reads(JsNumber(number)) shouldBe JsSuccess(notificationId)
        }
      }

    "reject number outside of range 1-14 inclusive" when
      Seq(0, 15, 999, -222, 10101010).foreach { number =>
        s"provided with '$number'" in {
          NotificationId.reads.reads(JsNumber(number)) shouldBe JsError(
            s"notificationIdentifier must be in range 1 to 14 inclusive, got $number"
          )
        }
      }

    "reject non-number Js value" when
      Seq[JsValue](
        JsString("12"),
        Json.obj("year" -> JsNumber(2025), "month" -> JsNumber(12), "day" -> JsNumber(8)),
        JsBoolean(true),
        JsNull,
        Json.arr(JsNumber(2025), JsNumber(12), JsNumber(8))
      ).foreach { timeValue =>
        s"provided with $timeValue" in {
          NotificationId.reads.reads(timeValue) shouldBe JsError("notificationIdentifier must be a number")
        }
      }
  }

  "writes" should {
    "correctly format notificationId" when
      numbers.foreach { case (number, notificationId) =>
        s"provided with notification $notificationId" in {
          NotificationId.writes.writes(notificationId) shouldBe JsNumber(number)
        }
      }
  }

}
