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

import common.Validation.{isLessThanMax, isMaxTwoDecimalPlaces, isPositive}
import models.PensionsWorthBeforeModel
import play.api.data.Form
import play.api.data.Forms.{bigDecimal, mapping, optional}

object PensionsWorthBeforeForm extends CommonBinders {

  def pensionsWorthBeforeForm = Form (
    mapping(
      "pensionsWorthBeforeAmt" -> optional(bigDecimal)
        .verifying("pla.pensionsWorthBefore.amount.errors.mandatoryError", pensionsWorthBeforeAmt => pensionsWorthBeforeAmt.isDefined)
        .verifying("pla.pensionsWorthBefore.amount.errors.decimal", pensionsWorthBeforeAmt => isMaxTwoDecimalPlaces(pensionsWorthBeforeAmt.getOrElse(0)))
        .verifying("pla.pensionsWorthBefore.amount.errors.negative", pensionsWorthBeforeAmt => isPositive(pensionsWorthBeforeAmt.getOrElse(0)))
        .verifying("pla.pensionsWorthBefore.amount.errors.max", pensionsWorthBeforeAmt => isLessThanMax(pensionsWorthBeforeAmt.getOrElse(0)))
    )(PensionsWorthBeforeModel.apply)(PensionsWorthBeforeModel.unapply)
  )

}
