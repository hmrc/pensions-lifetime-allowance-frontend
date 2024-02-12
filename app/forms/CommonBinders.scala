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

package forms

import java.time.LocalDate

import play.api.data.{FormError, Forms, Mapping}
import play.api.data.format.Formatter
import utils.Constants
import common.Dates._
import common.Exceptions
import common.Validation._

import scala.util.{Failure, Success, Try}

trait CommonBinders {

  private val EMPTY_STRING = ""
  private val DAY_LIMIT = 31
  private val MONTH_LIMIT = 12


  //############General Formatters###################
  private def stringToOptionalIntFormatter(errorLabel: String) = new Formatter[Option[Int]] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[Int]] = {
      data.getOrElse(key, EMPTY_STRING) match {
        case EMPTY_STRING => Left(Seq(FormError(key, s"pla.base.errors.$errorLabel")))
        case str => Try(str.toInt) match {
          case Success(result) => Right(Some(result))
          case Failure(_) => Left(Seq(FormError(key, "error.real")))
        }
      }
    }
    override def unbind(key: String, value: Option[Int]): Map[String, String] = Map(key -> value.map(_.toString).getOrElse(""))
  }
  def psoPartialDateBinder(errorLabel: String): Mapping[Option[Int]] = Forms.of[Option[Int]](stringToOptionalIntFormatter(errorLabel))


  private def stringToIntFormatter(errorLabel: String) = new Formatter[Int] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Int] = {
      data.getOrElse(key, EMPTY_STRING) match {
        case EMPTY_STRING => Left(Seq(FormError(key, s"pla.base.errors.$errorLabel")))
        case str => Try(str.toInt) match {
          case Success(result) => Right(result)
          case Failure(_) => Left(Seq(FormError(key, "error.real")))
        }
      }
    }
    override def unbind(key: String, value: Int): Map[String, String] = Map(key -> value.toString)
  }
  def intWithCustomError(errorLabel: String): Mapping[Int] = Forms.of[Int](stringToIntFormatter(errorLabel))


  private def protectionFormatter = new Formatter[String] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] = {
      data.getOrElse(key, "") match {
        case success if success.equals("ip2016") || success.equals("ip2014") => Right(success)
        case other => throw new Exceptions.RequiredNotFoundProtectionTypeException("[protectionFormatter]")
      }
    }
    override def unbind(key: String, value: String): Map[String, String] = Map(key -> value)
  }
  def protectionTypeFormatter: Mapping[String] = Forms.of[String](protectionFormatter)


  def decimalFormatter(requiredKey: String, invalidKey: String, args: Any*) = new Formatter[Option[BigDecimal]] {
    def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[BigDecimal]] =
        data.get(key).map(validateNumber(requiredKey, invalidKey, key, _)).getOrElse(Left(Seq(FormError(key, requiredKey, Nil))))

    def unbind(key: String, value: Option[BigDecimal]): Map[String, String] = {
      value match {
        case Some(data) => Map(key -> data.toString())
        case None => Map()
      }
    }
  }
  
  //############Withdrawal Date###################
  private[forms] def withdrawDateValidationFormatter(errorLabel: String) = new Formatter[Option[Int]] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[Int]] = {
      data.getOrElse(key, EMPTY_STRING) match {
        case EMPTY_STRING => Left(Seq(FormError(key,s"pla.withdraw.date-input.form.$errorLabel-empty", Nil )))
        case str => Try(str.toInt) match {
          case Success(result) =>
            if (key.equals("withdrawDate.day")) dateBoundaryValidation(key,result,errorLabel, DAY_LIMIT)
            else if(key.equals("withdrawDate.month")) dateBoundaryValidation(key,result,errorLabel, MONTH_LIMIT)
            else Right(Some(result))
          case Failure(_) => Left(Seq(FormError(key, "error.real")))
        }
      }
    }

    override def unbind(key: String, value: Option[Int]): Map[String, String] = Map(key -> value.map(_.toString).getOrElse(""))
  }
  def withdrawDatePartialFormatter(errorLabel: String): Mapping[Option[Int]] = Forms.of[Option[Int]](withdrawDateValidationFormatter(errorLabel))

  private[forms] def withdrawDateStringToIntFormatter = new Formatter[Option[Int]] {
    override def bind(key: String, data: Map[String, String]) = {

      val groupedIntsWithCustomErrors: Either[Seq[FormError], (Int, Int, Int)] = for {
        day   <- withdrawDateValidationFormatter("day").bind("withdrawDate.day", data)
        month <- withdrawDateValidationFormatter("month").bind("withdrawDate.month", data)
        year  <- withdrawDateValidationFormatter("year").bind("withdrawDate.year", data)
      } yield {
        (day.get, month.get, year.get)
      }

      val returnValue: Either[Seq[FormError], Option[Int]] = groupedIntsWithCustomErrors fold(
        errs => withdrawDateValidationFormatter("day").bind("withdrawDate.day", data),
        success => {
          val (day, month, year) = (success._1, success._2, success._3)
          validateWithdrawDate(key, day, month, year)
        }
      )
      returnValue
    }
    override def unbind(key: String, value: Option[Int]) = Map(key -> value.map(_.toString).getOrElse(""))
  }
  def withdrawDateFormatter: Mapping[Option[Int]] = Forms.of[Option[Int]](withdrawDateStringToIntFormatter)


  //############PSO Date###################
  private def psoDateStringToIntFormatter = new Formatter[Int] {
    override def bind(key: String, data: Map[String, String]) = {
      val groupedIntsWithCustomErrors: Either[Seq[FormError], (Int, Int, Int)] = for {
        day   <- stringToIntFormatter("dayEmpty").bind("pso.day", data)
        month <- stringToIntFormatter("monthEmpty").bind("pso.month", data)
        year  <- stringToIntFormatter("yearEmpty").bind("pso.year", data)
      } yield {
        (day, month, year)
      }

      val returnValue = groupedIntsWithCustomErrors fold (
        errs => stringToIntFormatter("dayEmpty").bind("pso.day", data),
        success => {
          val (day, month, year) = (success._1, success._2, success._3)
          validateCompleteDate(key,Constants.minIP16PSODate,"IP16PsoDetails",day,month,year)
        }
      )
      returnValue
    }
    override def unbind(key: String, value: Int) = Map(key -> value.toString)
  }
  def psoDateFormatterFromString: Mapping[Int] = Forms.of[Int](psoDateStringToIntFormatter)

  private def dateStringToOptionalIntFormatter = new Formatter[Option[Int]] {
    override def bind(key: String, data: Map[String, String]) = {
      val groupedIntsWithCustomErrors: Either[Seq[FormError], (Int, Int, Int, String)] = for {
        day   <- stringToOptionalIntFormatter("dayEmpty").bind("pso.day", data)
        month <- stringToOptionalIntFormatter("monthEmpty").bind("pso.month", data)
        year  <- stringToOptionalIntFormatter("yearEmpty").bind("pso.year", data)
        protectionType <- protectionFormatter.bind("protectionType", data)
      } yield {
        (day.get, month.get, year.get, protectionType)
      }

      val returnValue: Either[Seq[FormError], Option[Int]] = groupedIntsWithCustomErrors.fold(
        errs => stringToOptionalIntFormatter("dayEmpty").bind("pso.day", data),
        success => {
          val (day, month, year, protectionType) = (success._1, success._2, success._3, success._4)
          val protectionTypeErrorMsg = if (protectionType.toLowerCase.equals("ip2016")) "IP16PsoDetails" else "IP14PsoDetails"
          val dateComparator = if (protectionType.toLowerCase.equals("ip2016")) Constants.minIP16PSODate else Constants.minIP14PSODate
          validateCompleteDate(key,dateComparator,protectionTypeErrorMsg,day,month,year).fold[Either[Seq[FormError], Option[Int]]] (
            errors => Left(errors),
            value => Right(Some(value))
          )
        }
      )
      returnValue
    }
    override def unbind(key: String, value: Option[Int]) = Map(key -> value.map(_.toString).getOrElse(""))

  }
  def dateFormatterFromInt: Mapping[Option[Int]] = Forms.of[Option[Int]](dateStringToOptionalIntFormatter)



  //#################HELPER METHODS#################################//
  private def dateBoundaryValidation(key: String, value: Int, errorMsg: String, upperLimit: Int):  Either[Seq[FormError], Option[Int]] ={
    value match {
      case lowValue if lowValue <= 0 => Left(Seq(FormError(key,s"pla.withdraw.date-input.form.$errorMsg-too-low", Nil )))
      case highValue if highValue > upperLimit => Left(Seq(FormError(key,s"pla.withdraw.date-input.form.$errorMsg-too-high", Nil )))
      case _ => Right(Some(value))
    }
  }

  private def validateWithdrawDate(key: String, day: Int, month: Int, year: Int): Either[Seq[FormError], Option[Int]] = {
    if (!isValidDate(day, month, year)) Left(Seq(FormError(key, "pla.base.errors.invalidDate", Nil)))
    else if (futureDate(day, month, year)) Left(Seq(FormError(key, "pla.withdraw.date-input.form.date-in-future", Nil)))
    else Right(Some(day))
  }

  private def validateCompleteDate(key: String, dateComparator: LocalDate, errorMsg: String, day: Int, month: Int, year: Int): Either[Seq[FormError], Int] ={
    if (!isValidDate(day, month, year)) Left(Seq(FormError(key, "pla.base.errors.invalidDate", Nil)))
    else if (dateBefore(day, month, year, dateComparator)) Left(Seq(FormError(key, s"pla.$errorMsg.errorDateOutOfRange", Nil)))
    else if (futureDate(day, month, year)) Left(Seq(FormError(key, s"pla.$errorMsg.errorDateOutOfRange", Nil)))
    else Right(day)
  }

  private def validateNumber(requiredKey: String, invalidKey: String, key: String, number: String): Either[Seq[FormError], Some[BigDecimal]] = {
    number match {
      case "" => Left(Seq(FormError(key, requiredKey, Nil)))
      case value => checkIfValidBigDecimal(value, invalidKey, key)
    }
  }

  private def checkIfValidBigDecimal(value: String, invalidKey: String, key: String): Either[Seq[FormError], Some[BigDecimal]] = {
    val output = Try(BigDecimal(value))
    val outputWithoutCommas = Try(BigDecimal(stripCurrencyCharacters(value)))
    (output, outputWithoutCommas) match {
      case (Success(_), Success(_)) => Right(Some(BigDecimal(value)))
      case (Failure(_), Success(_)) => Left(Seq(FormError(key, "pla.psoDetails.errorQuestion")))
      case (Failure(_), Failure(_)) => Left(Seq(FormError(key, invalidKey)))
      case _ => Left(Seq(FormError(key, invalidKey)))
    }
  }

  def stripCurrencyCharacters(input: String):String = {
    input
      .trim()
      .replaceAll(",", "")
  }

  private def ifAmtIsRequired(key: String, data: Map[String, String]): Boolean ={
    data.getOrElse(key.take(key.length - 3), "no").equals("yes")
  }
}