/*
 * Copyright 2019 HM Revenue & Customs
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
import play.api.i18n.Lang
import utils.Constants

object PSODetailsForm extends CommonBinders {

  def psoDetailsForm(implicit lang: Lang): Form[PSODetailsModel] = Form(
  mapping(
    "psoDay"    -> psoDateFormatterFromString,
    "psoMonth"  -> intWithCustomError("monthEmpty"),
    "psoYear"   -> intWithCustomError("yearEmpty"),
    "psoAmt"    -> bigDecimal
      .verifying("pla.base.errors.errorMaximum", psoAmt => isLessThanDouble(psoAmt.toDouble, Constants.npsMaxCurrency))
      .verifying("pla.base.errors.errorNegative", psoAmt => isPositive(psoAmt.toDouble))
      .verifying("pla.base.errors.errorDecimalPlaces", psoAmt => isMaxTwoDecimalPlaces(psoAmt.toDouble))
    )(PSODetailsModel.apply)(PSODetailsModel.unapply)
  )
}
