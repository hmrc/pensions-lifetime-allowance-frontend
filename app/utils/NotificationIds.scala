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

package utils

import models.NotificationId
import models.NotificationId._

object NotificationIds {

  val showingFixedProtection2016Details: List[NotificationId] = List(
    NotificationId7,
    NotificationId14
  )

  val showingProtectionReference: List[NotificationId] = List(
    NotificationId1,
    NotificationId5,
    NotificationId8
  )

  val showingPsaCheckReference: List[NotificationId] = List(
    NotificationId1,
    NotificationId5,
    NotificationId8
  )

  val showingStatus: List[NotificationId] = List(
    NotificationId2,
    NotificationId3,
    NotificationId4,
    NotificationId6,
    NotificationId9,
    NotificationId10,
    NotificationId11,
    NotificationId12,
    NotificationId13
  )

  val showingInsetText: List[NotificationId] = List(
    NotificationId2,
    NotificationId3,
    NotificationId4,
    NotificationId6,
    NotificationId7,
    NotificationId9,
    NotificationId10,
    NotificationId11,
    NotificationId12,
    NotificationId13,
    NotificationId14
  )

  val showingSecondInsetText: List[NotificationId] =
    List(NotificationId7, NotificationId14)

  val showingShortChangeDetailsSection: List[NotificationId] = List(
    NotificationId1,
    NotificationId5,
    NotificationId6,
    NotificationId8,
    NotificationId13
  )

  val showingLongChangeDetailsSection: List[NotificationId] = List(
    NotificationId2,
    NotificationId3,
    NotificationId4,
    NotificationId7,
    NotificationId9,
    NotificationId10,
    NotificationId11,
    NotificationId12,
    NotificationId14
  )

}
