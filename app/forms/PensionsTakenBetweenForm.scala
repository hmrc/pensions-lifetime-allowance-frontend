/*
 * Copyright 2023 HM Revenue & Customs
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

import common.Validation.{isLessThanMax, isMaxTwoDecimalPlaces, isPositive, mandatoryCheck, yesNoCheck}
import models._
import play.api.data.Forms._
import play.api.data._
import common.Transformers._

object PensionsTakenBetweenForm extends CommonBinders{

  val verifyMandatory: PensionsTakenBetweenModel => Boolean = {
    case PensionsTakenBetweenModel("yes", value) => value.isDefined
    case _ => true
  }

  val verifyDecimal: PensionsTakenBetweenModel => Boolean = {
    case PensionsTakenBetweenModel("yes", Some(value)) => isMaxTwoDecimalPlaces(value)
    case _ => true
  }

  val verifyPositive: PensionsTakenBetweenModel => Boolean = {
    case PensionsTakenBetweenModel("yes", Some(value)) => isPositive(value)
    case _ => true
  }

  val verifyMax: PensionsTakenBetweenModel => Boolean = {
    case PensionsTakenBetweenModel("yes", Some(value)) => isLessThanMax(value)
    case _ => true
  }

  def pensionsTakenBetweenForm = Form (
    mapping(
      "pensionsTakenBetween" -> common.Validation.newText("pla.pensionsTakenBetween.errors.mandatoryError")
        .verifying("pla.pensionsTakenBetween.errors.mandatoryError", mandatoryCheck)
        .verifying("pla.pensionsTakenBetween.errors.mandatoryError", yesNoCheck),
      "pensionsTakenBetweenAmt" -> text
        .transform(stringToOptionalBigDecimal, optionalBigDecimalToString)
    )(PensionsTakenBetweenModel.apply)(PensionsTakenBetweenModel.unapply)
      .verifying("pla.pensionsTakenBetween.amount.errors.mandatoryError", verifyMandatory)
      .verifying("pla.pensionsTakenBetween.amount.errors.decimal", verifyDecimal)
      .verifying("pla.pensionsTakenBetween.amount.errors.negative", verifyPositive)
      .verifying("pla.pensionsTakenBetween.amount.errors.max", verifyMax)
  )
}
