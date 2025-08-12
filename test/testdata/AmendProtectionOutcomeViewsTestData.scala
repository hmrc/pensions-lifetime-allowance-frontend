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

package testdata

import enums.ApplicationType
import models.{AmendResultDisplayModel, PrintDisplayModel}

object AmendProtectionOutcomeViewsTestData {

  val protectedAmountIP14Str = "£1,350,000.11"
  val protectedAmountIP16Str = "£1,350,000.45"

  val printDisplayModelIP14: PrintDisplayModel = PrintDisplayModel(
    firstName = "Jim",
    surname = "Davis",
    nino = "nino",
    protectionType = "IP2014",
    status = "dormant",
    psaCheckReference = "psaRef",
    protectionReference = "IP14XXXXXX",
    protectedAmount = Some(protectedAmountIP14Str),
    certificateDate = Some("14/07/2015"),
    notificationId = 15
  )

  val amendResultDisplayModelIP14: AmendResultDisplayModel = AmendResultDisplayModel(
    protectionType = ApplicationType.IP2014,
    notificationId = 15,
    protectedAmount = protectedAmountIP14Str,
    details = Some(printDisplayModelIP14)
  )

  val printDisplayModelIP16: PrintDisplayModel = PrintDisplayModel(
    firstName = "Jim",
    surname = "Davis",
    nino = "nino",
    protectionType = "IP2016",
    status = "dormant",
    psaCheckReference = "psaRef",
    protectionReference = "IP16XXXXXX",
    protectedAmount = Some(protectedAmountIP16Str),
    certificateDate = Some("14/07/2017"),
    notificationId = 15
  )

  val amendResultDisplayModelIP16: AmendResultDisplayModel = AmendResultDisplayModel(
    protectionType = ApplicationType.IP2016,
    notificationId = 15,
    protectedAmount = protectedAmountIP16Str,
    details = Some(printDisplayModelIP16)
  )

}
