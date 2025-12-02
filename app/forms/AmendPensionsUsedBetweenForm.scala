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

import models.amendModels.AmendPensionsUsedBetweenModel
import play.api.data.Forms._
import play.api.data._
import common.Validation._
import models.pla.AmendableProtectionType

object AmendPensionsUsedBetweenForm extends CommonBinders {

  def amendPensionsUsedBetweenForm(protectionType: AmendableProtectionType) = Form(
    mapping(
      "amendedPensionsUsedBetweenAmt" -> of(
        decimalFormatter(
          s"pla.pensionsUsedBetween.amount.errors.mandatoryError.$protectionType",
          s"pla.pensionsUsedBetween.amount.errors.notReal.$protectionType"
        )
      )
        .verifying(
          s"pla.pensionsUsedBetween.amount.errors.decimal.$protectionType",
          pensionsWorthBeforeAmt => isMaxTwoDecimalPlaces(pensionsWorthBeforeAmt.getOrElse(0))
        )
        .verifying(
          s"pla.pensionsUsedBetween.amount.errors.negative.$protectionType",
          pensionsWorthBeforeAmt => isPositive(pensionsWorthBeforeAmt.getOrElse(0))
        )
        .verifying(
          s"pla.pensionsUsedBetween.amount.errors.max.$protectionType",
          pensionsWorthBeforeAmt => isLessThanMax(pensionsWorthBeforeAmt.getOrElse(0))
        )
    )(AmendPensionsUsedBetweenModel.apply)(AmendPensionsUsedBetweenModel.unapply)
  )

}
