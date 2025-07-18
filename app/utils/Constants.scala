/*
 * Copyright 2023 HM Revenue & Customs
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

import java.time.LocalDate

object Constants {

  val successCodes              = List(22, 23, 24)
  val rejectCodes               = List(17, 18, 19, 20, 21)
  val strippedNInoLength        = 8
  val npsMaxCurrency: Double    = 1000000000
  val minIP14PSODate: LocalDate = LocalDate.of(2014, 4, 5)
  val minIP16PSODate: LocalDate = LocalDate.of(2016, 4, 5)

  val fpProtectedAmount = 1250000.0

  val ip16SuccessCodes = List(12, 13, 14, 15, 16, 40, 41, 42, 43, 44)

  val ip14SuccessCodes = List(3, 4, 5, 6, 7, 8, 30, 31, 32, 33, 34)

  val inactiveSuccessCodes = List(5, 6, 7, 13, 14, 15, 16, 24, 30, 31, 32, 40, 41, 42, 43)

  val successCodesRequiringProtectionInfo = List(3, 4, 8, 12, 22, 23, 33, 34, 44)
  val fpShowPensionSharing                = List(23, 24)
  val ipShowAddToPension                  = List(8, 16)

  val ip16RelevantAmountThreshold = 1000001.0
  val ip14RelevantAmountThreshold = 1250001.0

  val activeProtectionCodes = List(3, 4, 8, 12, 22, 23, 33, 34, 44)

  // Result codes 4 and 12 are a temporary solution to align with NPS responses for certain amendment outcomes
  val activeAmendmentCodes = List(33, 34, 44, 4, 12)

  val amendmentCodesList = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14)
}
