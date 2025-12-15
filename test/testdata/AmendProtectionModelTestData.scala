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

import models.amend.AmendProtectionModel
import models.pla.response.ProtectionStatus.Dormant
import models.{DateModel, ProtectionModel, TimeModel}
import models.pla.response.ProtectionType

trait AmendProtectionModelTestData {

  val dormantIndividualProtection2016 = ProtectionModel(
    psaCheckReference = "testPSARef",
    identifier = 1,
    sequenceNumber = 1,
    protectionType = ProtectionType.IndividualProtection2016,
    status = Dormant,
    certificateDate = Some(DateModel.of(2016, 4, 17)),
    certificateTime = Some(TimeModel.of(14, 52, 25)),
    uncrystallisedRightsAmount = Some(100_000.00),
    nonUKRightsAmount = Some(2000.00),
    preADayPensionInPaymentAmount = Some(2000.00),
    postADayBenefitCrystallisationEventAmount = Some(2000.00),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456")
  )

  val amendDormantIndividualProtection2016: AmendProtectionModel =
    AmendProtectionModel.tryFromProtection(dormantIndividualProtection2016).get

  val dormantIndividualProtection2016LTA: ProtectionModel = dormantIndividualProtection2016.copy(
    protectionType = ProtectionType.IndividualProtection2016LTA
  )

  val amendDormantIndividualProtection2016LTA: AmendProtectionModel =
    AmendProtectionModel.tryFromProtection(dormantIndividualProtection2016LTA).get

  val dormantIndividualProtection2014: ProtectionModel = dormantIndividualProtection2016.copy(
    protectionType = ProtectionType.IndividualProtection2014
  )

  val amendDormantIndividualProtection2014: AmendProtectionModel =
    AmendProtectionModel.tryFromProtection(dormantIndividualProtection2014).get

  val dormantIndividualProtection2014LTA: ProtectionModel = dormantIndividualProtection2016.copy(
    protectionType = ProtectionType.IndividualProtection2014LTA
  )

  val amendDormantIndividualProtection2014LTA: AmendProtectionModel =
    AmendProtectionModel.tryFromProtection(dormantIndividualProtection2014LTA).get

}
