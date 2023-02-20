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

import common.Transformers.{optionalBigDecimalToString, stringToOptionalBigDecimal}
import common.Validation.{isLessThanMax, isMaxTwoDecimalPlaces, isPositive, mandatoryCheck, yesNoCheck}
import models._
import play.api.data.Forms._
import play.api.data._

object OverseasPensionsForm extends CommonBinders{

  val verifyMandatory: OverseasPensionsModel => Boolean = {
    case OverseasPensionsModel("yes", value) => value.isDefined
    case _ => true
  }

  val verifyDecimal: OverseasPensionsModel => Boolean = {
    case OverseasPensionsModel("yes", Some(value)) => isMaxTwoDecimalPlaces(value)
    case _ => true
  }

  val verifyPositive: OverseasPensionsModel => Boolean = {
    case OverseasPensionsModel("yes", Some(value)) => isPositive(value)
    case _ => true
  }

  val verifyMax: OverseasPensionsModel => Boolean = {
    case OverseasPensionsModel("yes", Some(value)) => isLessThanMax(value)
    case _ => true
  }

  def overseasPensionsForm = Form (
    mapping(
      "overseasPensions" -> common.Validation.newText("pla.overseasPensions.errors.mandatoryError")
        .verifying("pla.overseasPensions.errors.mandatoryError", mandatoryCheck)
        .verifying("pla.overseasPensions.errors.mandatoryError", yesNoCheck),
      "overseasPensionsAmt" -> text
        .transform(stringToOptionalBigDecimal, optionalBigDecimalToString)
    )(OverseasPensionsModel.apply)(OverseasPensionsModel.unapply)
      .verifying("pla.overseasPensions.amount.errors.mandatoryError", verifyMandatory)
      .verifying("pla.overseasPensions.amount.errors.decimal", verifyDecimal)
      .verifying("pla.overseasPensions.amount.errors.negative", verifyPositive)
      .verifying("pla.overseasPensions.amount.errors.max", verifyMax)
  )
}
