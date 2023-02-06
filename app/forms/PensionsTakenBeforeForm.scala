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

import play.api.data.Forms._
import play.api.data._
import common.Validation._
import models.PensionsTakenBeforeModel
import common.Transformers._

object PensionsTakenBeforeForm extends CommonBinders{

  val verifyMandatory: PensionsTakenBeforeModel => Boolean = {
    case PensionsTakenBeforeModel("yes", value) => value.isDefined
    case _ => true
  }

  val verifyDecimal: PensionsTakenBeforeModel => Boolean = {
    case PensionsTakenBeforeModel("yes", Some(value)) => isMaxTwoDecimalPlaces(value)
    case _ => true
  }

  val verifyPositive: PensionsTakenBeforeModel => Boolean = {
    case PensionsTakenBeforeModel("yes", Some(value)) => isPositive(value)
    case _ => true
  }

  val verifyMax: PensionsTakenBeforeModel => Boolean = {
    case PensionsTakenBeforeModel("yes", Some(value)) => isLessThanMax(value)
    case _ => true
  }

  def pensionsTakenBeforeForm = Form (
    mapping(
      "pensionsTakenBefore" -> common.Validation.newText("pla.pensionsTakenBefore.errors.mandatoryError")
        .verifying("pla.pensionsTakenBefore.errors.mandatoryError", mandatoryCheck)
        .verifying("pla.pensionsTakenBefore.errors.mandatoryError", yesNoCheck),
      "pensionsTakenBeforeAmt" -> text
        .transform(stringToOptionalBigDecimal, optionalBigDecimalToString)
    )(PensionsTakenBeforeModel.apply)(PensionsTakenBeforeModel.unapply)
      .verifying("pla.pensionsTakenBefore.amount.errors.mandatoryError", verifyMandatory)
      .verifying("pla.pensionsTakenBefore.amount.errors.decimal", verifyDecimal)
      .verifying("pla.pensionsTakenBefore.amount.errors.negative", verifyPositive)
      .verifying("pla.pensionsTakenBefore.amount.errors.max", verifyMax)
  )
}
