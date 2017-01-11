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

import models._
import common.Validation._
import utils.Constants
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object OverseasPensionsForm {

  def validateForm(form: Form[OverseasPensionsModel]): Form[OverseasPensionsModel] = {
    if(!validationNeeded(form)) form else {
      if(!validateFieldCompleted(form)) form.withError("overseasPensionsAmt", Messages("pla.overseasPensions.errorQuestion"))
      else if(!validateMinimum(form)) form.withError("overseasPensionsAmt", Messages("pla.overseasPensions.errorNegative"))
      else if(!validateMaximum(form)) form.withError("overseasPensionsAmt", Messages("pla.overseasPensions.errorMaximum"))
      else if(!validateTwoDec(form)) form.withError("overseasPensionsAmt", Messages("pla.overseasPensions.errorDecimalPlaces"))
      else form
    }
  }

  private def validationNeeded(data: Form[OverseasPensionsModel]): Boolean = {
    data("overseasPensions").value.get match {
      case "yes" => true
      case "no" => false
    }
  }

  private def validateFieldCompleted(data: Form[OverseasPensionsModel]) = data("overseasPensionsAmt").value.isDefined

  private def validateMinimum(data: Form[OverseasPensionsModel]) = isPositive(data("overseasPensionsAmt").value.getOrElse("0").toDouble)

  private def validateMaximum(data: Form[OverseasPensionsModel]) = isLessThanDouble(data("overseasPensionsAmt").value.getOrElse("0").toDouble, Constants.npsMaxCurrency)

  private def validateTwoDec(data: Form[OverseasPensionsModel]) = isMaxTwoDecimalPlaces(data("overseasPensionsAmt").value.getOrElse("0").toDouble)

  val overseasPensionsForm = Form (
    mapping(
      "overseasPensions" -> nonEmptyText,
      "overseasPensionsAmt" -> optional(bigDecimal)
    )(OverseasPensionsModel.apply)(OverseasPensionsModel.unapply)
  )
}
