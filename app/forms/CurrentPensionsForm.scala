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
import utils.Constants._

object CurrentPensionsForm extends CommonBinders {

  def currentPensionsForm(protectionType: String) = Form(
    mapping(
      "currentPensionsAmt" -> of(
        decimalFormatter(
          s"pla.currentPensions.amount.errors.mandatoryError.$protectionType",
          s"pla.currentPensions.amount.errors.notReal.$protectionType"
        )
      )
        .verifying(
          s"pla.currentPensions.amount.errors.negative.$protectionType",
          currentPensionsAmt => isPositive(currentPensionsAmt.getOrElse(0))
        )
        .verifying(
          s"pla.currentPensions.amount.errors.decimal.$protectionType",
          currentPensionsAmt => isMaxTwoDecimalPlaces(currentPensionsAmt.getOrElse(0))
        )
        .verifying(
          s"pla.currentPensions.amount.errors.max.$protectionType",
          currentPensionsAmt => isLessThanDouble(currentPensionsAmt.getOrElse(BigDecimal(0)).toDouble, npsMaxCurrency)
        )
    )(CurrentPensionsModel.apply)(CurrentPensionsModel.unapply)
  )

}
