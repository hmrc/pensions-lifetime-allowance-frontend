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

import models.AmendedProtectionType.IndividualProtection2014
import models.NotificationId.NotificationId1
import models.amend.AmendsGAModel
import models.display.{AmendOutcomeDisplayModel, AmendPrintDisplayModel}
import models.pla.response.AmendProtectionResponseStatus.Dormant

object AmendProtectionOutcomeViewsTestData {

  val protectedAmountIP14Str = "£1,350,000.11"

  val amendsGAModel: AmendsGAModel = AmendsGAModel(
    current = Some("current"),
    before = Some("before"),
    between = Some("between"),
    overseas = Some("overseas"),
    pso = Some("pso")
  )

  val printDisplayModelIP14: AmendPrintDisplayModel = AmendPrintDisplayModel(
    firstName = "Jim",
    surname = "Davis",
    nino = "nino",
    protectionType = IndividualProtection2014,
    status = Some(Dormant),
    psaCheckReference = Some("psaRef"),
    protectionReference = Some("IP14XXXXXX"),
    fixedProtectionReference = None,
    protectedAmount = Some(protectedAmountIP14Str),
    certificateDate = Some("14/07/2015"),
    certificateTime = Some("3:14pm")
  )

  val amendResultDisplayModelIP14: AmendOutcomeDisplayModel = AmendOutcomeDisplayModel(
    notificationId = NotificationId1,
    protectedAmount = protectedAmountIP14Str,
    details = Some(printDisplayModelIP14)
  )

}
