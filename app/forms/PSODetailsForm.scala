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
import models._
import play.api.data.Forms._
import play.api.data._
import utils.Constants

object PSODetailsForm extends CommonBinders {

  def psoDetailsForm: Form[PSODetailsModel] = Form(
  mapping(
    "pso.day"    -> psoDateFormatterFromString,
    "pso.month"  -> intWithCustomError("monthEmpty"),
    "pso.year"   -> intWithCustomError("yearEmpty"),
    "psoAmt"    -> optional(bigDecimal)
      .verifying("pla.psoDetails.amount.errors.max", psoAmt => isLessThanDouble(psoAmt.getOrElse(BigDecimal(0.0)).toDouble, Constants.npsMaxCurrency))
      .verifying("pla.psoDetails.amount.errors.negative", psoAmt => isPositive(psoAmt.getOrElse(BigDecimal(0.0)).toDouble))
      .verifying("pla.psoDetails.amount.errors.decimal", psoAmt => isMaxTwoDecimalPlaces(psoAmt.getOrElse(BigDecimal(0.0)).toDouble))
      .verifying("pla.psoDetails.amount.errors.mandatoryError", _.isDefined)
    )(PSODetailsModel.apply)(PSODetailsModel.unapply)
  )
}
