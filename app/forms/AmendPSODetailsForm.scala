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
import models.pla.AmendProtectionLifetimeAllowanceType
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.Messages
import utils.Constants

import java.time.LocalDate

object AmendPSODetailsForm extends CommonBinders {

  val key    = "pso"
  val amount = "psoAmt"

  def amendPsoDetailsForm(protectionType: String)(implicit messages: Messages): Form[AmendPSODetailsModel] = Form(
    mapping(
      key -> of(
        DateFormatter(
          key,
          optMinDate = Some(
            if (
              AmendProtectionLifetimeAllowanceType
                .tryFrom(protectionType)
                .contains(AmendProtectionLifetimeAllowanceType.IndividualProtection2016)
            ) Constants.minIP16PSODate
            else Constants.minIP14PSODate
          ),
          optMaxDate = Some(LocalDate.now.plusDays(1))
        )
      ),
      amount -> optional(bigDecimal)
        .verifying(
          "pla.psoDetails.amount.errors.max",
          psoAmt => isLessThanDouble(psoAmt.getOrElse(BigDecimal(0.0)).toDouble, Constants.npsMaxCurrency)
        )
        .verifying(
          "pla.psoDetails.amount.errors.negative",
          psoAmt => isPositive(psoAmt.getOrElse(BigDecimal(0.0)).toDouble)
        )
        .verifying(
          "pla.psoDetails.amount.errors.decimal",
          psoAmt => isMaxTwoDecimalPlaces(psoAmt.getOrElse(BigDecimal(0.0)).toDouble)
        )
        .verifying("pla.psoDetails.amount.errors.mandatoryError", _.isDefined)
    )((date, amount) => AmendPSODetailsModel(date, amount))(model => Some((model.pso, model.psoAmt)))
  )

}
