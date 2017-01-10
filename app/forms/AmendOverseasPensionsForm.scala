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
import models.amendModels.AmendOverseasPensionsModel
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{Lang, Messages}
import utils.Constants

object AmendOverseasPensionsForm {

  val amendOverseasPensionsForm = Form (
    mapping(
      "amendedOverseasPensions" -> nonEmptyText,
      "amendedOverseasPensionsAmt" -> optional(bigDecimal),
      "protectionType" -> text,
      "status" -> text
    )(AmendOverseasPensionsModel.apply)(AmendOverseasPensionsModel.unapply)
  )

  def validateForm(form: Form[AmendOverseasPensionsModel])(implicit lang:Lang): Form[AmendOverseasPensionsModel] = {
    if(!validationNeeded(form)) form else {
      if(!validateFieldCompleted(form)) form.withError("amendedOverseasPensionsAmt", Messages("pla.base.errors.errorQuestion"))
      else if(!validateMinimum(form)) form.withError("amendedOverseasPensionsAmt", Messages("pla.base.errors.errorNegative"))
      else if(!validateMaximum(form)) form.withError("amendedOverseasPensionsAmt", Messages("pla.base.errors.errorMaximum"))
      else if(!validateTwoDec(form)) form.withError("amendedOverseasPensionsAmt", Messages("pla.base.errors.errorDecimalPlaces"))
      else form
    }
  }

  private def validationNeeded(data: Form[AmendOverseasPensionsModel]): Boolean = {
    data("amendedOverseasPensions").value.get match {
      case "yes" => true
      case "no" => false
    }
  }

  private def validateFieldCompleted(data: Form[AmendOverseasPensionsModel]) = data("amendedOverseasPensionsAmt").value.isDefined

  private def validateMinimum(data: Form[AmendOverseasPensionsModel]) = isPositive(data("amendedOverseasPensionsAmt").value.getOrElse("0").toDouble)

  private def validateMaximum(data: Form[AmendOverseasPensionsModel]) = isLessThanDouble(data("amendedOverseasPensionsAmt").value.getOrElse("0").toDouble, Constants.npsMaxCurrency)

  private def validateTwoDec(data: Form[AmendOverseasPensionsModel]) = isMaxTwoDecimalPlaces(data("amendedOverseasPensionsAmt").value.getOrElse("0").toDouble)

}
