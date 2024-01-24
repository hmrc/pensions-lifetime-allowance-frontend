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

import common.Validation._
import forms.formatters.DateFormatter
import models.amendModels.AmendPSODetailsModel
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.Messages
import utils.Constants

import java.time.LocalDate

object AmendPSODetailsForm extends CommonBinders {

  val key = "amendPsoDetails"
  val amountMap = "psoAmt"
  val protectionTypeMap = "protectionType"
  val statusMap = "status"
  val existingPSOMap = "existingPSO"

  private val minDateDay = 5
  private val minDateMonth = 4
  private val minDateYear = 2016
  private val minDate = LocalDate.of(minDateYear, minDateMonth, minDateDay)

  def amendPsoDetailsForm(minDate: LocalDate = minDate, maxDate: LocalDate = LocalDate.now())(implicit messages: Messages): Form[AmendPSODetailsModel] = Form(
    mapping(
      key -> of(
        DateFormatter(
          key,
          optMinDate = Some(minDate),
          optMaxDate = Some(maxDate)
        )
      ),
      amountMap -> optional(bigDecimal)
        .verifying("pla.psoDetails.amount.errors.max", psoAmt => isLessThanDouble(psoAmt.getOrElse(BigDecimal(0.0)).toDouble, Constants.npsMaxCurrency))
        .verifying("pla.psoDetails.amount.errors.negative", psoAmt => isPositive(psoAmt.getOrElse(BigDecimal(0.0)).toDouble))
        .verifying("pla.psoDetails.amount.errors.decimal", psoAmt => isMaxTwoDecimalPlaces(psoAmt.getOrElse(BigDecimal(0.0)).toDouble))
        .verifying("pla.psoDetails.amount.errors.mandatoryError", _.isDefined),
      protectionTypeMap -> protectionTypeFormatter,
      statusMap -> text,
      existingPSOMap -> boolean
    )((date, amount, protectionType, status, existingPso) =>
      AmendPSODetailsModel(
        Some(date.getDayOfMonth),
        Some(date.getMonthValue),
        Some(date.getYear),
        amount,
        protectionType,
        status,
        existingPso
      )
    )(model => Some((LocalDate.of(model.psoYear.get, model.psoMonth.get, model.psoDay.get), model.psoAmt, model.protectionType, model.status, model.existingPSO)))
  )
}
