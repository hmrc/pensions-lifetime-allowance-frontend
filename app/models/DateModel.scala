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

package models

import play.api.libs.json.{JsError, JsString, JsSuccess, Reads, Writes}

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Try

case class DateModel(toLocalDate: LocalDate) extends AnyVal

object DateModel {

  private val dateFormat = DateTimeFormatter.ISO_DATE

  private def parseDate(dateString: String): Option[DateModel] =
    Try(LocalDate.parse(dateString, dateFormat)).toOption.map(DateModel(_))

  private def serialiseDate(dateModel: DateModel): String = dateModel.toLocalDate.format(dateFormat)

  given Reads[DateModel] = {
    case JsString(dateString) =>
      parseDate(dateString) match {
        case Some(date) => JsSuccess(date)
        case None       => JsError("invalid date string")
      }
    case _ => JsError("date must be a string")
  }

  given Writes[DateModel] = date => JsString(serialiseDate(date))

  def of(year: Int, month: Int, day: Int): DateModel = DateModel(LocalDate.of(year, month, day))

}
