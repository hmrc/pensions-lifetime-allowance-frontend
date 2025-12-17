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

package generators

import models.{DateModel, TimeModel}
import models.pla.response.{
  ProtectionRecord,
  ProtectionRecordsList,
  ProtectionStatus,
  ProtectionType,
  ReadProtectionsResponse
}
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import java.time.LocalDate

trait ModelGenerators extends ScalaCheckDrivenPropertyChecks {

  val dateGen: Gen[DateModel] = for {
    year  <- Gen.choose(2000, 2025)
    month <- Gen.choose(1, 12)
    day   <- Gen.choose(1, LocalDate.of(year, month, 1).lengthOfMonth())
  } yield DateModel.of(year, month, day)

  val timeGen: Gen[TimeModel] = for {
    hour   <- Gen.choose(0, 23)
    minute <- Gen.choose(0, 59)
    second <- Gen.choose(0, 59)
  } yield TimeModel.of(hour, minute, second)

  val intGen: Gen[Int] = Gen.posNum[Int]

  val protectionRecordGen: Gen[ProtectionRecord] = for {
    identifier                                <- intGen
    sequenceNumber                            <- intGen
    protectionType                            <- Gen.oneOf(ProtectionType.values)
    certificateDate                           <- dateGen
    certificateTime                           <- timeGen
    status                                    <- Gen.oneOf(ProtectionStatus.values)
    protectionReference                       <- Gen.option(Gen.alphaStr)
    relevantAmount                            <- Gen.option(intGen)
    preADayPensionInPaymentAmount             <- Gen.option(intGen)
    postADayBenefitCrystallisationEventAmount <- Gen.option(intGen)
    uncrystallisedRightsAmount                <- Gen.option(intGen)
    nonUKRightsAmount                         <- Gen.option(intGen)
    pensionDebitAmount                        <- Gen.option(intGen)
    pensionDebitEnteredAmount                 <- Gen.option(intGen)
    protectedAmount                           <- Gen.option(intGen)
    pensionDebitStartDate                     <- Gen.option(dateGen)
    pensionDebitTotalAmount                   <- Gen.option(intGen)
    lumpSumAmount                             <- Gen.option(intGen)
    lumpSumPercentage                         <- Gen.option(intGen)
    enhancementFactor                         <- Gen.option(Gen.double)
  } yield ProtectionRecord(
    identifier = identifier,
    sequenceNumber = sequenceNumber,
    `type` = protectionType,
    certificateDate = certificateDate,
    certificateTime = certificateTime,
    status = status,
    protectionReference = protectionReference,
    relevantAmount = relevantAmount,
    preADayPensionInPaymentAmount = preADayPensionInPaymentAmount,
    postADayBenefitCrystallisationEventAmount = postADayBenefitCrystallisationEventAmount,
    uncrystallisedRightsAmount = uncrystallisedRightsAmount,
    nonUKRightsAmount = nonUKRightsAmount,
    pensionDebitAmount = pensionDebitAmount,
    pensionDebitEnteredAmount = pensionDebitEnteredAmount,
    protectedAmount = protectedAmount,
    pensionDebitStartDate = pensionDebitStartDate,
    pensionDebitTotalAmount = pensionDebitTotalAmount,
    lumpSumAmount = lumpSumAmount,
    lumpSumPercentage = lumpSumPercentage,
    enhancementFactor = enhancementFactor
  )

  val protectionRecordsListGen: Gen[ProtectionRecordsList] = for {
    protectionRecord      <- protectionRecordGen
    historicalDetailsList <- Gen.option(Gen.nonEmptyListOf(protectionRecordGen))
  } yield ProtectionRecordsList(protectionRecord, historicalDetailsList)

  val readProtectionsResponseGen: Gen[ReadProtectionsResponse] = for {
    pensionSchemeAdministratorCheckReference <- Gen.alphaNumStr.suchThat(_.nonEmpty)
    protectionRecordsList                    <- Gen.nonEmptyListOf(protectionRecordsListGen)
  } yield ReadProtectionsResponse(
    pensionSchemeAdministratorCheckReference,
    Some(protectionRecordsList)
  )

}
