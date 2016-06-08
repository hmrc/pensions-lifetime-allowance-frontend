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

object PensionsTakenBeforeForm {


  def validatePensionsTakenBeforeForm(form: Form[PensionsTakenBeforeModel]) = {
    if(!validate(form)) form.withError("pensionsTakenBeforeAmt", Messages("pla.pensionsTakenBefore.errorQuestion"))
    else if(!validateMinimum(form)) form.withError("pensionsTakenBeforeAmt", Messages("pla.pensionsTakenBefore.errorNegative"))
    else if(!validateTwoDec(form)) form.withError("pensionsTakenBeforeAmt", Messages("pla.pensionsTakenBefore.errorDecimalPlaces"))
    else form
  }

  private def validate(data: Form[PensionsTakenBeforeModel]) = {
    data("pensionsTakenBefore").value.get match {
      case "Yes" => data("pensionsTakenBeforeAmt").value.isDefined
      case "No" => true
    }
  }

  private def validateMinimum(data: Form[PensionsTakenBeforeModel]) = {
    data("pensionsTakenBefore").value.get match {
      case "Yes" => isPositive(data("pensionsTakenBeforeAmt").value.getOrElse("1").toDouble)
      case "No" => true
    }
  }

  private def validateTwoDec(data: Form[PensionsTakenBeforeModel]) = {
    data("pensionsTakenBefore").value.get match {
      case "Yes" => isMaxTwoDecimalPlaces(data("pensionsTakenBeforeAmt").value.getOrElse("0").toDouble)
      case "No" => true
    }
  }

  val pensionsTakenBeforeForm = Form (
    mapping(
      "pensionsTakenBefore" -> nonEmptyText,
      "pensionsTakenBeforeAmt" -> optional(bigDecimal)
    )(PensionsTakenBeforeModel.apply)(PensionsTakenBeforeModel.unapply)
  )
}
