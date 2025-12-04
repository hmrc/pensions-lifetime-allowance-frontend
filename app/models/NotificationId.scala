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

import models.pla.response.AmendProtectionResponseStatus._
import models.pla.response.AmendProtectionResponseStatus
import play.api.libs.json.{Format, JsError, JsNumber, JsSuccess, Reads, Writes}

sealed abstract class NotificationId(
    val toInt: Int,
    val status: AmendProtectionResponseStatus
) {
  override val toString: String = toInt.toString
}

object NotificationId {

  case object NotificationId1 extends NotificationId(1, Open)
  case object NotificationId2 extends NotificationId(2, Dormant)
  case object NotificationId3 extends NotificationId(3, Dormant)
  case object NotificationId4 extends NotificationId(4, Dormant)
  case object NotificationId5 extends NotificationId(5, Open)
  case object NotificationId6 extends NotificationId(6, Withdrawn)
  case object NotificationId7 extends NotificationId(7, Withdrawn)

  case object NotificationId8  extends NotificationId(8, Open)
  case object NotificationId9  extends NotificationId(9, Dormant)
  case object NotificationId10 extends NotificationId(10, Dormant)
  case object NotificationId11 extends NotificationId(11, Dormant)
  case object NotificationId12 extends NotificationId(12, Dormant)
  case object NotificationId13 extends NotificationId(13, Withdrawn)
  case object NotificationId14 extends NotificationId(14, Withdrawn)

  implicit val reads: Reads[NotificationId] = {
    case JsNumber(number) =>
      number.toInt match {
        case 1  => JsSuccess(NotificationId1)
        case 2  => JsSuccess(NotificationId2)
        case 3  => JsSuccess(NotificationId3)
        case 4  => JsSuccess(NotificationId4)
        case 5  => JsSuccess(NotificationId5)
        case 6  => JsSuccess(NotificationId6)
        case 7  => JsSuccess(NotificationId7)
        case 8  => JsSuccess(NotificationId8)
        case 9  => JsSuccess(NotificationId9)
        case 10 => JsSuccess(NotificationId10)
        case 11 => JsSuccess(NotificationId11)
        case 12 => JsSuccess(NotificationId12)
        case 13 => JsSuccess(NotificationId13)
        case 14 => JsSuccess(NotificationId14)
        case n  => JsError(s"notificationIdentifier must be in range 1 to 14 inclusive, got $n")
      }
    case _ => JsError("notificationIdentifier must be a number")
  }

  implicit val writes: Writes[NotificationId] = notificationIdentifier => JsNumber(notificationIdentifier.toInt)

  implicit val format: Format[NotificationId] = Format(reads, writes)

  val values: List[NotificationId] = List(
    NotificationId1,
    NotificationId2,
    NotificationId3,
    NotificationId4,
    NotificationId5,
    NotificationId6,
    NotificationId7,
    NotificationId8,
    NotificationId9,
    NotificationId10,
    NotificationId11,
    NotificationId12,
    NotificationId13,
    NotificationId14
  )

}
