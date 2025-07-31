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

  val dateGen: Gen[String] = for {
    year  <- Gen.choose(2000, 2025)
    month <- Gen.choose(1, 12)
    day   <- Gen.choose(1, LocalDate.of(year, month, 1).lengthOfMonth())
  } yield f"$year%04d-$month%02d-$day%02d"

  val timeGen: Gen[String] = for {
    hour   <- Gen.choose(0, 23)
    minute <- Gen.choose(0, 59)
    second <- Gen.choose(0, 59)
  } yield f"$hour%02d$minute%02d$second%02d"

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
    identifier,
    sequenceNumber,
    protectionType,
    certificateDate,
    certificateTime,
    status,
    protectionReference,
    relevantAmount,
    preADayPensionInPaymentAmount,
    postADayBenefitCrystallisationEventAmount,
    uncrystallisedRightsAmount,
    nonUKRightsAmount,
    pensionDebitAmount,
    pensionDebitEnteredAmount,
    protectedAmount,
    pensionDebitStartDate,
    pensionDebitTotalAmount,
    lumpSumAmount,
    lumpSumPercentage,
    enhancementFactor
  )

  val protectionRecordsListGen: Gen[ProtectionRecordsList] = for {
    protectionRecord      <- protectionRecordGen
    historicaldetailsList <- Gen.option(Gen.listOf(protectionRecordGen))
  } yield ProtectionRecordsList(protectionRecord, historicaldetailsList)

  val readProtectionsResponseGen: Gen[ReadProtectionsResponse] = for {
    pensionSchemeAdministratorCheckReference <- Gen.alphaNumStr.suchThat(_.nonEmpty)
    protectionRecordsList                    <- Gen.listOf(protectionRecordsListGen)
  } yield ReadProtectionsResponse(
    pensionSchemeAdministratorCheckReference,
    protectionRecordsList
  )

}
