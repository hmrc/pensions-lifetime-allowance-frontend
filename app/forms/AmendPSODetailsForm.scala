/*
 * Copyright 2022 HM Revenue & Customs
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
import models.amendModels.AmendPSODetailsModel
import play.api.data.Forms._
import play.api.data._
import utils.Constants

object AmendPSODetailsForm  extends CommonBinders{
  def amendPsoDetailsForm = Form(
    mapping(
      "psoDay"    -> dateFormatterFromInt,
      "psoMonth"  -> psoPartialDateBinder("monthEmpty"),
      "psoYear"   -> psoPartialDateBinder("yearEmpty"),
      "psoAmt"    -> optional(bigDecimal)
        .verifying("pla.base.errors.errorMaximum", psoAmt => isLessThanDouble(psoAmt.getOrElse(BigDecimal(0.0)).toDouble, Constants.npsMaxCurrency))
        .verifying("pla.base.errors.errorNegative", psoAmt => isPositive(psoAmt.getOrElse(BigDecimal(0.0)).toDouble))
        .verifying("pla.base.errors.errorDecimalPlaces", psoAmt => isMaxTwoDecimalPlaces(psoAmt.getOrElse(BigDecimal(0.0)).toDouble))
        .verifying("pla.psoDetails.errorQuestion", _.isDefined),

      "protectionType" -> protectionTypeFormatter,
      "status"         -> text,
      "existingPSO"    -> boolean
    )(AmendPSODetailsModel.apply)(AmendPSODetailsModel.unapply)
  )
}
