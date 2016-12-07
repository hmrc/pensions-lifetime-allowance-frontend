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

object PensionsTakenBeforeForm {

  def validateForm(form: Form[PensionsTakenBeforeModel]): Form[PensionsTakenBeforeModel] = {
    if(!validationNeeded(form)) form else {
      if(!validateFieldCompleted(form)) form.withError("pensionsTakenBeforeAmt", Messages("pla.base.errors.errorQuestion"))
      else if(!validateMinimum(form)) form.withError("pensionsTakenBeforeAmt", Messages("pla.base.errors.errorNegative"))
      else if(!validateMaximum(form)) form.withError("pensionsTakenBeforeAmt", Messages("pla.base.errors.errorMaximum"))
      else if(!validateTwoDec(form)) form.withError("pensionsTakenBeforeAmt", Messages("pla.base.errors.errorDecimalPlaces"))
      else form
    }
  }

  private def validationNeeded(data: Form[PensionsTakenBeforeModel]): Boolean = {
    data("pensionsTakenBefore").value.get match {
      case "yes" => true
      case "no" => false
    }
  }

  private def validateFieldCompleted(data: Form[PensionsTakenBeforeModel]) = data("pensionsTakenBeforeAmt").value.isDefined

  private def validateMinimum(data: Form[PensionsTakenBeforeModel]) = isPositive(data("pensionsTakenBeforeAmt").value.getOrElse("0").toDouble)

  private def validateMaximum(data: Form[PensionsTakenBeforeModel]) = isLessThanDouble(data("pensionsTakenBeforeAmt").value.getOrElse("0").toDouble, Constants.npsMaxCurrency)

  private def validateTwoDec(data: Form[PensionsTakenBeforeModel]) = isMaxTwoDecimalPlaces(data("pensionsTakenBeforeAmt").value.getOrElse("0").toDouble)

  val pensionsTakenBeforeForm = Form (
    mapping(
      "pensionsTakenBefore" -> nonEmptyText,
      "pensionsTakenBeforeAmt" -> optional(bigDecimal)
    )(PensionsTakenBeforeModel.apply)(PensionsTakenBeforeModel.unapply)
  )
}
