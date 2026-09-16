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

package forms.formatters

import play.api.data.FormError
import play.api.data.format.Formatter
import play.api.i18n.Messages

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.{Failure, Success, Try}

case class DateFormatter(
    key: String,
    minDate: Option[LocalDate] = None,
    maxDate: Option[LocalDate] = None
)(using messages: Messages)
    extends Formatter[LocalDate] {

  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", messages.lang.toLocale)

  private val dateRequiredError      = s"$key.error.required"
  private val dayRequiredError       = s"$key.error.required.day"
  private val dayMonthRequiredError  = s"$key.error.required.dayMonth"
  private val dayYearRequiredError   = s"$key.error.required.dayYear"
  private val monthRequiredError     = s"$key.error.required.month"
  private val monthYearRequiredError = s"$key.error.required.monthYear"
  private val yearRequiredError      = s"$key.error.required.year"

  private val dateInvalidError      = s"$key.error.invalid"
  private val dayInvalidError       = s"$key.error.invalid.day"
  private val dayMonthInvalidError  = s"$key.error.invalid.dayMonth"
  private val dayYearInvalidError   = s"$key.error.invalid.dayYear"
  private val monthInvalidError     = s"$key.error.invalid.month"
  private val monthYearInvalidError = s"$key.error.invalid.monthYear"
  private val yearInvalidError      = s"$key.error.invalid.year"

  private val dateNotInRangeError        = s"$key.error.notInRange"
  private val dayNotInRangeError         = s"$key.error.notInRange.day"
  private val dayNotInRangeForMonthError = s"$key.error.notInRange.day.forMonth"
  private val dayMonthNotInRangeError    = s"$key.error.notInRange.dayMonth"
  private val dayYearNotInRangeError     = s"$key.error.notInRange.dayYear"
  private val monthNotInRangeError       = s"$key.error.notInRange.month"
  private val monthYearNotInRangeError   = s"$key.error.notInRange.monthYear"
  private val yearNotInRangeError        = s"$key.error.notInRange.year"

  private val dateMinError = s"$key.error.range.min"
  private val dateMaxError = s"$key.error.range.max"

  private val dayKey   = s"$key.day"
  private val monthKey = s"$key.month"
  private val yearKey  = s"$key.year"

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], LocalDate] = {
    val optDayString   = data.get(s"$key.day").map(_.trim).filter(_.nonEmpty)
    val optMonthString = data.get(s"$key.month").map(_.trim).filter(_.nonEmpty)
    val optYearString  = data.get(s"$key.year").map(_.trim).filter(_.nonEmpty)

    for {
      stringTuple <- validateFieldsNonEmpty(optDayString, optMonthString, optYearString)
      (dayString, monthString, yearString) = stringTuple
      intTuple <- validateFieldsAreNumbers(dayString, monthString, yearString)
      (dayInt, monthInt, yearInt) = intTuple
      validDate   <- validateFieldsMakeRealDate(dayInt, monthInt, yearInt)
      inRangeDate <- validateDateWithinRange(validDate)
    } yield inRangeDate

  }

  override def unbind(key: String, value: LocalDate): Map[String, String] = Map(
    s"$key.day"   -> value.getDayOfMonth.toString,
    s"$key.month" -> value.getMonthValue.toString,
    s"$key.year"  -> value.getYear.toString
  )

  private def validateFieldsNonEmpty(
      optDay: Option[String],
      optMonth: Option[String],
      optYear: Option[String]
  ): Either[Seq[FormError], (String, String, String)] =
    (optDay, optMonth, optYear) match {
      case (Some(day), Some(month), Some(year)) => Right((day, month, year))
      case (None, Some(_), Some(_))             => Left(Seq(FormError(dayKey, dayRequiredError)))
      case (None, None, Some(_)) =>
        Left(Seq(FormError(dayKey, dayMonthRequiredError), FormError(monthKey, dayMonthRequiredError)))
      case (None, Some(_), None) =>
        Left(Seq(FormError(dayKey, dayYearRequiredError), FormError(yearKey, dayYearRequiredError)))
      case (Some(_), None, Some(_)) => Left(Seq(FormError(monthKey, monthRequiredError)))
      case (Some(_), None, None) =>
        Left(Seq(FormError(monthKey, monthYearRequiredError), FormError(yearKey, monthYearRequiredError)))
      case (Some(_), Some(_), None) => Left(Seq(FormError(yearKey, yearRequiredError)))
      case (None, None, None)       => Left(Seq(FormError(key, dateRequiredError)))
    }

  private def parseMonth(month: String): Option[Int] = month.toLowerCase match {
    case "1" | "jan" | "january" | "ion" | "ionawr"      => Some(1)
    case "2" | "feb" | "february" | "chwef" | "chwefror" => Some(2)
    case "3" | "mar" | "march" | "maw" | "mawrth"        => Some(3)
    case "4" | "apr" | "april" | "ebr" | "ebrill"        => Some(4)
    case "5" | "may" | "mai"                             => Some(5)
    case "6" | "jun" | "june" | "meh" | "mehefin"        => Some(6)
    case "7" | "jul" | "july" | "gorff" | "gorffennaf"   => Some(7)
    case "8" | "aug" | "august" | "awst"                 => Some(8)
    case "9" | "sep" | "sept" | "september" | "medi"     => Some(9)
    case "10" | "oct" | "october" | "hyd" | "hydref"     => Some(10)
    case "11" | "nov" | "november" | "tach" | "tachwedd" => Some(11)
    case "12" | "dec" | "december" | "rhag" | "rhagfyr"  => Some(12)
    case s                                               => s.toIntOption
  }

  private def validateFieldsAreNumbers(
      dayField: String,
      monthField: String,
      yearField: String
  ): Either[Seq[FormError], (Int, Int, Int)] =
    (dayField.toIntOption, parseMonth(monthField), yearField.toIntOption) match {
      case (Some(day), Some(month), Some(year)) => Right((day, month, year))
      case (None, Some(_), Some(_))             => Left(Seq(FormError(dayKey, dayInvalidError)))
      case (None, None, Some(_)) =>
        Left(Seq(FormError(dayKey, dayMonthInvalidError), FormError(monthKey, dayMonthInvalidError)))
      case (None, Some(_), None) =>
        Left(Seq(FormError(dayKey, dayYearInvalidError), FormError(yearKey, dayYearInvalidError)))
      case (Some(_), None, Some(_)) => Left(Seq(FormError(monthKey, monthInvalidError)))
      case (Some(_), None, None) =>
        Left(Seq(FormError(monthKey, monthYearInvalidError), FormError(yearKey, monthYearInvalidError)))
      case (Some(_), Some(_), None) => Left(Seq(FormError(yearKey, yearInvalidError)))
      case (None, None, None)       => Left(Seq(FormError(key, dateInvalidError)))
    }

  private def validateFieldsMakeRealDate(day: Int, month: Int, year: Int): Either[Seq[FormError], LocalDate] = {
    val validatedDay = Some(day).filter(d => d >= 1 && d <= 31)

    val validatedMonth = Some(month).filter(m => m >= 1 && m <= 12)

    val validatedYear = Some(year).filter(y => y >= 1000 && y <= 9999)

    (validatedDay, validatedMonth, validatedYear) match {
      case (Some(_), Some(_), Some(_)) =>
        Try(LocalDate.of(year, month, day)) match {
          case Success(date) =>
            Right(date)
          case Failure(_) =>
            Left(Seq(FormError(dayKey, dayNotInRangeForMonthError)))
        }
      case (None, Some(_), Some(_)) => Left(Seq(FormError(dayKey, dayNotInRangeError)))
      case (Some(_), None, Some(_)) => Left(Seq(FormError(monthKey, monthNotInRangeError)))
      case (Some(_), Some(_), None) => Left(Seq(FormError(yearKey, yearNotInRangeError)))
      case (None, None, Some(_)) =>
        Left(Seq(FormError(dayKey, dayMonthNotInRangeError), FormError(monthKey, dayMonthNotInRangeError)))
      case (None, Some(_), None) =>
        Left(Seq(FormError(dayKey, dayYearNotInRangeError), FormError(yearKey, dayYearNotInRangeError)))
      case (Some(_), None, None) =>
        Left(Seq(FormError(monthKey, monthYearNotInRangeError), FormError(yearKey, monthYearNotInRangeError)))
      case (None, None, None) => Left(Seq(FormError(key, dateNotInRangeError)))
    }
  }

  private def validateDateWithinRange(date: LocalDate): Either[Seq[FormError], LocalDate] =
    (minDate, maxDate) match {
      case (Some(min), _) if date.isBefore(min) =>
        Left(Seq(FormError(key, dateMinError, Seq(formatter.format(min)))))
      case (_, Some(max)) if date.isAfter(max) =>
        Left(Seq(FormError(key, dateMaxError, Seq(formatter.format(max)))))
      case _ => Right(date)
    }

}
