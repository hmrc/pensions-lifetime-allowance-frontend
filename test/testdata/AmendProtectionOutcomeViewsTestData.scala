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
import models.amendModels.AmendsGAModel
import models.display.{
  ActiveAmendResultDisplayModel,
  AmendPrintDisplayModel,
  AmendResultDisplayModel,
  ProtectionDetailsDisplayModel
}

object AmendProtectionOutcomeViewsTestData {

  val protectedAmountIP14Str = "£1,350,000.11"

  val amendsGAModel: AmendsGAModel = AmendsGAModel(
    current = Some("current"),
    before = Some("before"),
    between = Some("between"),
    overseas = Some("overseas"),
    pso = Some("pso")
  )

  val amendsActiveResultModelIP16: ActiveAmendResultDisplayModel = ActiveAmendResultDisplayModel(
    firstName = "Jim",
    surname = "Davis",
    nino = "nino",
    protectionType = ApplicationType.IP2016,
    notificationId = "44",
    protectedAmount = "£1,350,000.45",
    details = Some(
      ProtectionDetailsDisplayModel(
        protectionReference = "protectionRef",
        psaReference = "psaRef",
        applicationDate = Some("14 June 2017")
      )
    )
  )

  val amendsActiveResultModelIP16WithNoneProtectionReference: ActiveAmendResultDisplayModel =
    ActiveAmendResultDisplayModel(
      firstName = "Jim",
      surname = "Davis",
      nino = "nino",
      protectionType = ApplicationType.IP2016,
      notificationId = "44",
      protectedAmount = "£1,350,000.45",
      details = Some(
        ProtectionDetailsDisplayModel(
          protectionReference = "None",
          psaReference = "psaRef",
          applicationDate = Some("14 June 2017")
        )
      )
    )

  val amendsActiveResultModelIP14: ActiveAmendResultDisplayModel = ActiveAmendResultDisplayModel(
    firstName = "Jim",
    surname = "Davis",
    nino = "nino",
    protectionType = ApplicationType.IP2014,
    notificationId = "33",
    protectedAmount = "£1,350,000.11",
    details = Some(
      ProtectionDetailsDisplayModel(
        protectionReference = "protectionRef",
        psaReference = "psaRef",
        applicationDate = Some("14 June 2017")
      )
    )
  )

  val printDisplayModelIP14: AmendPrintDisplayModel = AmendPrintDisplayModel(
    firstName = "Jim",
    surname = "Davis",
    nino = "nino",
    protectionType = "IP2014",
    status = Some("Dormant"),
    psaCheckReference = Some("psaRef"),
    protectionReference = Some("IP14XXXXXX"),
    fixedProtectionReference = None,
    protectedAmount = Some(protectedAmountIP14Str),
    certificateDate = Some("14/07/2015"),
    certificateTime = Some("3:14pm")
  )

  val amendResultDisplayModelIP14: AmendResultDisplayModel = AmendResultDisplayModel(
    notificationId = 15,
    protectedAmount = protectedAmountIP14Str,
    details = Some(printDisplayModelIP14)
  )

}
