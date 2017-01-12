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
import models.amendModels.AmendPensionsTakenBetweenModel
import play.api.Play.current
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{Lang, Messages}
import play.api.i18n.Messages.Implicits._
import utils.Constants

object AmendPensionsTakenBetweenForm {

  def validateForm(form: Form[AmendPensionsTakenBetweenModel])(implicit lang:Lang): Form[AmendPensionsTakenBetweenModel] = {
    if(!validationNeeded(form)) form else {
      if(!validateFieldCompleted(form)) form.withError("amendedPensionsTakenBetweenAmt", Messages("pla.base.errors.errorQuestion"))
      else if(!validateMinimum(form)) form.withError("amendedPensionsTakenBetweenAmt", Messages("pla.base.errors.errorNegative"))
      else if(!validateMaximum(form)) form.withError("amendedPensionsTakenBetweenAmt", Messages("pla.base.errors.errorMaximum"))
      else if(!validateTwoDec(form)) form.withError("amendedPensionsTakenBetweenAmt", Messages("pla.base.errors.errorDecimalPlaces"))
      else form
    }
  }

  private def validationNeeded(data: Form[AmendPensionsTakenBetweenModel]): Boolean = {
    data("amendedPensionsTakenBetween").value.get match {
      case "yes" => true
      case "no" => false
    }
  }

  private def validateFieldCompleted(data: Form[AmendPensionsTakenBetweenModel]) = data("amendedPensionsTakenBetweenAmt").value.isDefined

  private def validateMinimum(data: Form[AmendPensionsTakenBetweenModel]) = isPositive(data("amendedPensionsTakenBetweenAmt").value.getOrElse("0").toDouble)

  private def validateMaximum(data: Form[AmendPensionsTakenBetweenModel]) = isLessThanDouble(data("amendedPensionsTakenBetweenAmt").value.getOrElse("0").toDouble, Constants.npsMaxCurrency)

  private def validateTwoDec(data: Form[AmendPensionsTakenBetweenModel]) = isMaxTwoDecimalPlaces(data("amendedPensionsTakenBetweenAmt").value.getOrElse("0").toDouble)

  def amendPensionsTakenBetweenForm(implicit lang:Lang) = Form (
    mapping(
      "amendedPensionsTakenBetween" -> nonEmptyText,
      "amendedPensionsTakenBetweenAmt" -> optional(bigDecimal),
      "protectionType" -> text,
      "status" -> text
    )(AmendPensionsTakenBetweenModel.apply)(AmendPensionsTakenBetweenModel.unapply)
  )
}
