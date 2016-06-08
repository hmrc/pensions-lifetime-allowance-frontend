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
import play.api.data.Forms._
import play.api.data._
import common.Validation._
import play.api.i18n.Messages

object OverseasPensionsForm {


  def validateForm(form: Form[OverseasPensionsModel]) = {
    if(!validate(form)) form.withError("overseasPensionsAmt", Messages("pla.overseasPensions.errorQuestion"))
    else if(!validateMinimum(form)) form.withError("overseasPensionsAmt", Messages("pla.overseasPensions.errorNegative"))
    else if(!validateTwoDec(form)) form.withError("overseasPensionsAmt", Messages("pla.overseasPensions.errorDecimalPlaces"))
    else form
  }

  private def validate(data: Form[OverseasPensionsModel]) = {
    data("overseasPensions").value.get match {
      case "yes" => data("overseasPensionsAmt").value.isDefined
      case "no" => true
    }
  }

  private def validateMinimum(data: Form[OverseasPensionsModel]) = {
    data("overseasPensions").value.get match {
      case "yes" => isPositive(data("overseasPensionsAmt").value.getOrElse("0").toDouble)
      case "no" => true
    }
  }

  private def validateTwoDec(data: Form[OverseasPensionsModel]) = {
    data("overseasPensions").value.get match {
      case "yes" => isMaxTwoDecimalPlaces(data("overseasPensionsAmt").value.getOrElse("0").toDouble)
      case "no" => true
    }
  }

  val overseasPensionsForm = Form (
    mapping(
      "overseasPensions" -> nonEmptyText,
      "overseasPensionsAmt" -> optional(bigDecimal)
    )(OverseasPensionsModel.apply)(OverseasPensionsModel.unapply)
  )
}
