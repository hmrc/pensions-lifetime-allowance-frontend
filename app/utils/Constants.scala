/*
 * Copyright 2016 HM Revenue & Customs
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

    val successCodes = List(22,23,24)
    val rejectCodes = List(17,18,19,20,21)
    val strippedNInoLength = 8
    val npsMaxCurrency: Double = 99999999999999.99
    val minIP14PSODate: LocalDate = LocalDate.of(2014, 4, 7)
    val minIP16PSODate: LocalDate = LocalDate.of(2016, 4, 7)


    val ip16SuccessCodes = List(12,13,14,15,16)
    val ip16RejectCodes = List(9,10,11)

    val ip14SuccessCodes = List(3,4,5,6,7,8)
    val ip14RejectCodes = List(1,2)
}
