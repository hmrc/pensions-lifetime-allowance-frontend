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
import models.PensionsUsedBetweenModel
import play.api.data.Form
import play.api.data.Forms.{mapping, of}

object PensionsUsedBetweenForm extends CommonBinders {

  def pensionsUsedBetweenForm(protectionType: String) = Form (
    mapping(
      "pensionsUsedBetweenAmt" -> of(decimalFormatter(s"pla.pensionsUsedBetween.amount.errors.mandatoryError.$protectionType", "pla.pensionsUsedBetween.amount.errors.notReal"))
        .verifying(s"pla.pensionsUsedBetween.amount.errors.decimal.$protectionType", pensionsUsedBetweenAmt => isMaxTwoDecimalPlaces(pensionsUsedBetweenAmt.getOrElse(0)))
        .verifying(s"pla.pensionsUsedBetween.amount.errors.negative.$protectionType", pensionsUsedBetweenAmt => isPositive(pensionsUsedBetweenAmt.getOrElse(0)))
        .verifying(s"pla.pensionsUsedBetween.amount.errors.max.$protectionType", pensionsUsedBetweenAmt => isLessThanMax(pensionsUsedBetweenAmt.getOrElse(0)))
    )(PensionsUsedBetweenModel.apply)(PensionsUsedBetweenModel.unapply)
  )

}