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

import common.Validation._
import models.PensionsTakenBeforeModel
import models.amendModels.AmendPensionsTakenBeforeModel
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import utils.Constants


object AmendPensionsTakenBeforeForm {

  val amendPensionsTakenBeforeForm = Form (
    mapping(
      "pensionsTakenBefore" -> nonEmptyText,
      "pensionsTakenBeforeAmt" -> optional(bigDecimal),
      "protectionType" -> text,
      "status" -> text
    )(AmendPensionsTakenBeforeModel.apply)(AmendPensionsTakenBeforeModel.unapply)
  )

  def validateForm(form: Form[AmendPensionsTakenBeforeModel]): Form[AmendPensionsTakenBeforeModel] = {
    if(!validationNeeded(form)) form else {
      if(!validateFieldCompleted(form)) form.withError("pensionsTakenBeforeAmt", Messages("pla.pensionsTakenBefore.errorQuestion"))
      else if(!validateMinimum(form)) form.withError("pensionsTakenBeforeAmt", Messages("pla.pensionsTakenBefore.errorNegative"))
      else if(!validateMaximum(form)) form.withError("pensionsTakenBeforeAmt", Messages("pla.pensionsTakenBefore.errorMaximum"))
      else if(!validateTwoDec(form)) form.withError("pensionsTakenBeforeAmt", Messages("pla.pensionsTakenBefore.errorDecimalPlaces"))
      else form
    }
  }

  private def validationNeeded(data: Form[AmendPensionsTakenBeforeModel]): Boolean = {
    data("pensionsTakenBefore").value.get match {
      case "yes" => true
      case "no" => false
    }
  }

  private def validateFieldCompleted(data: Form[AmendPensionsTakenBeforeModel]) = data("pensionsTakenBeforeAmt").value.isDefined

  private def validateMinimum(data: Form[AmendPensionsTakenBeforeModel]) = isPositive(data("pensionsTakenBeforeAmt").value.getOrElse("0").toDouble)

  private def validateMaximum(data: Form[AmendPensionsTakenBeforeModel]) = isLessThanDouble(data("pensionsTakenBeforeAmt").value.getOrElse("0").toDouble, Constants.npsMaxCurrency)

  private def validateTwoDec(data: Form[AmendPensionsTakenBeforeModel]) = isMaxTwoDecimalPlaces(data("pensionsTakenBeforeAmt").value.getOrElse("0").toDouble)
}
