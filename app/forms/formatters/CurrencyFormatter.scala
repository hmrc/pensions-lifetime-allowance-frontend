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

import scala.util.Try

case class CurrencyFormatter(mandatoryMessageKey: String, invalidMessageKey: String)
    extends Formatter[Option[BigDecimal]] {

  def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[BigDecimal]] =
    data
      .get(key)
      .map(validateBigDecimal(key, _))
      .getOrElse(Left(Seq(FormError(key, mandatoryMessageKey))))

  def unbind(key: String, value: Option[BigDecimal]): Map[String, String] =
    value match {
      case None => Map.empty
      case Some(data) =>
        val scaled = if (data.isValidInt) {
          data.setScale(0)
        } else {
          data.setScale(2)
        }

        Map(key -> scaled.toString)
    }

  private[formatters] def validateBigDecimal(
      key: String,
      number: String
  ): Either[Seq[FormError], Some[BigDecimal]] =
    number.trim match {
      case "" => Left(Seq(FormError(key, mandatoryMessageKey)))
      case value =>
        parseBigDecimal(value) match {
          case Some(bigDecimal) => Right(Some(bigDecimal))
          case None             => Left(Seq(FormError(key, invalidMessageKey)))
        }
    }

  private[formatters] def parseBigDecimal(input: String): Option[BigDecimal] = Try(
    BigDecimal(stripCommas(input))
  ).toOption

  private[formatters] def stripCommas(input: String): String =
    input
      .trim()
      .replaceAll(",", "")

}
