/*
 * Copyright 2016 HM Revenue & Customs
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

import models._
import common.Validation._
import utils.Constants
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.Messages

object PSODetailsForm {

  def validateForm(form: Form[PSODetailsModel]): Form[PSODetailsModel] = {
    val dateValidationResult = validateDateFormat(form)
    dateValidationResult match {
      case "year"  => form.withError("psoYear", Messages("pla.base.errors.invalidYear"))
      case "month" => form.withError("psoMonth", Messages("pla.base.errors.invalidMonth"))
      case "day"   => form.withError("psoDay", Messages("pla.base.errors.invalidDay"))
      case "valid" => form
    }
  }

  private def validateDateFormat(form: Form[PSODetailsModel]): String = {
    if(invalidYear(form("psoYear").value.get.toInt)) "year"
    else if(invalidMonth(form("psoMonth").value.get.toInt)) "month"
    else if(isValidDate(form("psoDay").value.get.toInt, form("psoMonth").value.get.toInt, form("psoYear").value.get.toInt)) "valid" else "day"
  }

  private def invalidYear(yr: Int): Boolean = yr < 1900 || yr > 2100
  private def invalidMonth(mnth: Int): Boolean = mnth < 1 || mnth > 12

  val psoDetailsForm = Form(
    mapping(
        "psoNumber" -> number,
        "psoDay"    -> number,
        "psoMonth"  -> number,
        "psoYear"   -> number,
        "psoAmt"    -> bigDecimal
                        .verifying(Messages("pla.psoDetails.errorMaximum"), psoAmt => isLessThanDouble(psoAmt.toDouble, Constants.npsMaxCurrency))
                        .verifying(Messages("pla.psoDetails.errorNegative"), psoAmt => isPositive(psoAmt.toDouble))
                        .verifying(Messages("pla.psoDetails.errorDecimalPlaces"), psoAmt => isMaxTwoDecimalPlaces(psoAmt.toDouble))
    )(PSODetailsModel.apply)(PSODetailsModel.unapply)
  )
}
