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

package common

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}

object Dates {

  def constructDateTimeFromAPIString(date: String): LocalDateTime =
    if (date.contains('T')) {
      val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss")
      LocalDateTime.parse(date.replace(":", "").split('.')(0), dateFormat)
    } else {
      val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
      LocalDate.parse(date, dateFormat).atTime(0, 0, 0)
    }

  def extractDMYFromAPIDateString(dateString: String): (Int, Int, Int) = {
    val date = constructDateTimeFromAPIString(dateString)
    (date.getDayOfMonth, date.getMonthValue, date.getYear)
  }

}
