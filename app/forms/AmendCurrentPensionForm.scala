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
import models.amendModels.AmendCurrentPensionModel
import play.api.data.Form
import play.api.data.Forms._
import utils.Constants._

object AmendCurrentPensionForm extends CommonBinders {
  def amendCurrentPensionForm = Form(
    mapping(
      "amendedUKPensionAmt" -> of(decimalFormatter("pla.currentPensions.amount.errors.mandatoryError", "pla.currentPensions.amount.errors.notReal"))
        .verifying("pla.currentPensions.amount.errors.mandatoryError", currentPensionsAmt => currentPensionsAmt.isDefined)
        .verifying("pla.currentPensions.amount.errors.negative", currentPensionsAmt => isPositive(currentPensionsAmt.getOrElse(0)))
        .verifying("pla.currentPensions.amount.errors.decimal", currentPensionsAmt => isMaxTwoDecimalPlaces(currentPensionsAmt.getOrElse(0)))
        .verifying("pla.currentPensions.amount.errors.max", currentPensionsAmt => isLessThanDouble(currentPensionsAmt.getOrElse(BigDecimal(0)).toDouble, npsMaxCurrency))
    )(AmendCurrentPensionModel.apply)(AmendCurrentPensionModel.unapply)
  )
}
