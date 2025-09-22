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

import java.text.DecimalFormat
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import play.api.i18n.{Lang, Messages}

case class MoneyPounds(value: BigDecimal, decimalPlaces: Int = 2, roundUp: Boolean = false) {

  def isNegative = value < 0

  def quantity =
    s"%,.${decimalPlaces}f".format(
      value
        .setScale(decimalPlaces, if (roundUp) BigDecimal.RoundingMode.CEILING else BigDecimal.RoundingMode.FLOOR)
        .abs
    )

}

object Display {

  def currencyDisplayString(amt: BigDecimal): String = {
    val amount = MoneyPounds(amt)
    val minus  = if (amount.isNegative) "-" else ""
    val str    = s"£$minus${amount.quantity}"
    if (str.endsWith(".00")) {
      str.takeWhile(_ != '.')
    } else str
  }

  def dateDisplayString(date: LocalDateTime)(implicit lang: Lang, messages: Messages): String =
    if (lang.language == "cy") {
      val dateFormat = DateTimeFormatter.ofPattern("d MMMM yyyy")
      date.format(dateFormat)
      val monthNum       = date.getMonthValue
      val welshFormatter = DateTimeFormatter.ofPattern(s"""d '${messages(s"pla.month.$monthNum")}' yyyy""")
      date.format(welshFormatter)
    } else {
      val dateFormat = DateTimeFormatter.ofPattern("d MMMM yyyy")
      date.format(dateFormat)
    }

  def timeDisplayString(dateTime: LocalDateTime): String = {
    val timeFormat = DateTimeFormatter.ofPattern("h:mma")
    dateTime.format(timeFormat).toLowerCase
  }

  def currencyInputDisplayFormat(amt: BigDecimal): BigDecimal = {
    def df(n: BigDecimal): String = new DecimalFormat("0.00").format(n).replace(".00", "")

    BigDecimal(df(amt))
  }

  def percentageDisplayString(percentage: Int): String = s"$percentage%"

  def factorDisplayString(factor: Double): String = new DecimalFormat("0.00").format(factor)

}
