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

import models.{DateModel, TimeModel}
import play.api.i18n.{Lang, Messages}

import java.time.format.DateTimeFormatter

object Display {

  private def format2DecimalPlaces(amount: BigDecimal): String =
    "%,.2f".format(scaleBigDecimal(amount).abs)

  private def scaleBigDecimal(amount: BigDecimal): BigDecimal =
    if (amount.isWhole) {
      amount.setScale(0)
    } else {
      amount.setScale(2, BigDecimal.RoundingMode.FLOOR)
    }

  private def format2DecimalPlacesOrWholeNumber(amount: BigDecimal): String =
    format2DecimalPlaces(amount)
      .stripSuffix(".00")

  def currencyDisplayString(amount: BigDecimal): String = {
    val minus = if (amount < 0) "-" else ""
    s"£$minus${format2DecimalPlacesOrWholeNumber(amount)}"
  }

  private val englishDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

  def dateDisplayString(dateModel: DateModel)(implicit lang: Lang, messages: Messages): String =
    if (lang.language == "cy") {
      val monthNum       = dateModel.date.getMonthValue
      val welshFormatter = DateTimeFormatter.ofPattern(s"""d '${messages(s"pla.month.$monthNum")}' yyyy""")
      dateModel.date.format(welshFormatter)
    } else {
      dateModel.date.format(englishDateFormatter)
    }

  private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mma")

  def timeDisplayString(time: TimeModel): String =
    time.time.format(timeFormatter).toLowerCase

  def currencyInputDisplayFormat(amt: BigDecimal): BigDecimal =
    scaleBigDecimal(amt)

  def percentageDisplayString(percentage: Int): String = s"$percentage%"

  def factorDisplayString(factor: Double): String = format2DecimalPlaces(factor)

}
