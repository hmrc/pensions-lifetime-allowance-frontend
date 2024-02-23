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

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import play.api.i18n.{Lang, Messages}

object Dates {

  def constructDate(day: Int, month: Int, year: Int): LocalDate = {
    LocalDate.of(year, month, day)
  }

  def constructDateFromAPIString(date: String): LocalDate = {
    val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    LocalDate.parse(date.split("T")(0), dateFormat)
  }

  def extractDMYFromAPIDateString(dateString: String): (Int, Int, Int) = {
    val date = constructDateFromAPIString(dateString)
    (date.getDayOfMonth, date.getMonthValue, date.getYear)
  }

  def dateBefore(day: Int, month: Int, year: Int, date2: LocalDate): Boolean = {
    LocalDate.of(year, month, day).isBefore(date2)
  }

  def dateAfter(day: Int, month: Int, year: Int, date2: LocalDate): Boolean = {
    LocalDate.of(year, month, day).isAfter(date2)
  }

  def futureDate(day: Int, month: Int, year: Int): Boolean = {
    dateAfter(day, month, year, LocalDate.now)
  }

  def apiDateFormat(day: Int, month: Int, year: Int): String = {

    val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    LocalDate.of(year, month, day).format(dateFormat)
  }

  def withDrawDateString(date: String)(implicit lang: Lang, messages: Messages) : String = {
    val localDate = constructDateFromAPIString(date)
    Display.dateDisplayString(localDate)
  }
}
