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

import play.api.data.Forms.of
import play.api.data.format.Formatter
import play.api.data.validation.{Constraint, Invalid, Valid}
import play.api.data.{FieldMapping, FormError}
import utils.Constants.npsMaxCurrency

import java.time.LocalDate
import scala.util.{Failure, Success, Try}

object Validation {

  def isMaxTwoDecimalPlaces(amount: BigDecimal): Boolean =
    amount match {
      case checkAmount if checkAmount.scale <= 2 => true
      case _                                     => false
    }

  def isPositive(amount: BigDecimal): Boolean =
    amount match {
      case checkAmount if checkAmount < 0 => false
      case _                              => true
    }

  def isLessThanMax(amount: BigDecimal): Boolean =
    amount <= npsMaxCurrency

  def isLessThanDouble(amount: Double, target: Double): Boolean =
    amount < target

  def isValidDate(day: Int, month: Int, year: Int): Boolean =
    Try(LocalDate.of(year, month, day)).map(_ => true).getOrElse(false)

  def mandatoryCheck: String => Boolean = input => input.trim != ""

  val yesNoCheck: String => Boolean = {
    case "yes" => true
    case "no"  => true
    case ""    => true
    case _     => false
  }

  def newText(errorKey: String = "error.required", optional: Boolean = false): FieldMapping[String] =
    of(stringFormatter(errorKey, optional))

  private def stringFormatter(errorKey: String, optional: Boolean): Formatter[String] = new Formatter[String] {

    def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
      data.get(key) match {
        case None                                   => Left(Seq(FormError(key, errorKey)))
        case Some(x) if x.trim.isEmpty && !optional => Left(Seq(FormError(key, errorKey)))
        case Some(x) if x.trim.isEmpty && optional  => Right(x.trim)
        case Some(s)                                => Right(s.trim)
      }

    def unbind(key: String, value: String): Map[String, String] =
      Map(key -> value.trim)
  }

  def decimalPlaceConstraint(errMsgKey: String, decimalPlace: Int = 3): Constraint[BigDecimal] = Constraint {
    case input if input.scale < decimalPlace =>
      Valid
    case _ =>
      Invalid(errMsgKey)
  }

  def negativeConstraint(errMsgKey: String): Constraint[BigDecimal] = Constraint {
    case input if input >= 0 =>
      Valid
    case _ =>
      Invalid(errMsgKey)
  }

  def maxMoneyCheck(maxValue: BigDecimal, errMsgKey: String): Constraint[BigDecimal] = Constraint {
    case input if input <= maxValue =>
      Valid
    case _ =>
      Invalid(errMsgKey)
  }

  val bigDecimalCheck: String => Boolean = input =>
    Try(BigDecimal(input)) match {
      case Success(_)                     => true
      case Failure(_) if input.trim == "" => true
      case Failure(_)                     => tryBigDecimalWithoutComma(input)
    }

  val commaCheck: String => Boolean = input =>
    (tryBigDecimalWithoutComma(input), Try(BigDecimal(input))) match {
      case (true, Success(_))  => true
      case (true, Failure(_))  => false
      case (false, Failure(_)) => true
    }

  private def tryBigDecimalWithoutComma(input: String): Boolean =
    Try(BigDecimal(input.trim().replaceAll(",", ""))) match {
      case Success(_) => true
      case Failure(_) => false
    }

  def stopOnFirstFail[T](constraints: Constraint[T]*): Constraint[T] = Constraint { field: T =>
    constraints.toList.dropWhile(constraint => constraint(field) == Valid) match {
      case Nil             => Valid
      case constraint :: _ => constraint(field)
    }
  }

}
