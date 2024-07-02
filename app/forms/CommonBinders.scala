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

import play.api.data.FormError
import play.api.data.format.Formatter

import scala.util.{Failure, Success, Try}

trait CommonBinders {

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

  //#################HELPER METHODS#################################//


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

  def stripCurrencyCharacters(input: String): String = {
    input
      .trim()
      .replaceAll(",", "")
  }

}