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

  def validate(data: PensionsTakenBeforeModel) = {
    data.pensionsTakenBefore match {
      case "Yes" => data.pensionsTakenBeforeAmt.isDefined
      case "No" => true
    }
  }

  def validateMinimum(data: PensionsTakenBeforeModel) = {
    data.pensionsTakenBefore match {
      case "Yes" => isPositive(data.pensionsTakenBeforeAmt.getOrElse(0))
      case "No" => true
    }
  }

  def validateTwoDec(data: PensionsTakenBeforeModel) = {
    data.pensionsTakenBefore match {
      case "Yes" => isMaxTwoDecimalPlaces(data.pensionsTakenBeforeAmt.getOrElse(0))
      case "No" => true
    }
  }

  val pensionsTakenBeforeForm = Form (
    mapping(
      "pensionsTakenBefore" -> nonEmptyText,
      "pensionsTakenBeforeAmt" -> optional(bigDecimal)
    )(PensionsTakenBeforeModel.apply)(PensionsTakenBeforeModel.unapply).verifying(Messages("pla.pensionsTakenBefore.errorQuestion"),
      pensionsTakenBeforeForm => validate(pensionsTakenBeforeForm))
      .verifying(Messages("pla.pensionsTakenBefore.errorNegative"),
        pensionsTakenBeforeForm => validateMinimum(pensionsTakenBeforeForm))
      .verifying(Messages("pla.pensionsTakenBefore.errorDecimalPlaces"),
        pensionsTakenBeforeForm => validateTwoDec(pensionsTakenBeforeForm))
  )
}
