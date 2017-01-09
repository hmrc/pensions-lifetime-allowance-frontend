/*
 * Copyright 2017 HM Revenue & Customs
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
import play.api.i18n.Messages
import utils.Constants._

object AmendCurrentPensionForm {
  val amendCurrentPensionForm = Form(
    mapping(
      "amendedUKPensionAmt" -> optional(bigDecimal)
        .verifying(Messages("pla.base.errors.errorQuestion"), currentPensionsAmt => currentPensionsAmt.isDefined)
        .verifying(Messages("pla.base.errors.errorNegative"), currentPensionsAmt => isPositive(currentPensionsAmt.getOrElse(0)))
        .verifying(Messages("pla.base.errors.errorDecimalPlaces"), currentPensionsAmt => isMaxTwoDecimalPlaces(currentPensionsAmt.getOrElse(0)))
        .verifying(Messages("pla.base.errors.errorMaximum"), currentPensionsAmt => isLessThanDouble(currentPensionsAmt.getOrElse(BigDecimal(0)).toDouble, npsMaxCurrency)),
      "protectionType" -> text,
      "status" -> text
    )(AmendCurrentPensionModel.apply)(AmendCurrentPensionModel.unapply)
  )
}
