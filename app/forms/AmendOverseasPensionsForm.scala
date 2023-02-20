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
import models.amendModels.AmendOverseasPensionsModel
import play.api.data.Form
import play.api.data.Forms._
import common.Validation._

object AmendOverseasPensionsForm extends CommonBinders{

  val verifyMandatory: AmendOverseasPensionsModel => Boolean = {
    case AmendOverseasPensionsModel("yes", value, _,_) => value.isDefined
    case _ => true
  }

  val verifyDecimal: AmendOverseasPensionsModel => Boolean = {
    case AmendOverseasPensionsModel("yes", Some(value), _,_) => isMaxTwoDecimalPlaces(value)
    case _ => true
  }

  val verifyPositive: AmendOverseasPensionsModel => Boolean = {
    case AmendOverseasPensionsModel("yes", Some(value), _,_) => isPositive(value)
    case _ => true
  }

  val verifyMax: AmendOverseasPensionsModel => Boolean = {
    case AmendOverseasPensionsModel("yes", Some(value), _,_) => isLessThanMax(value)
    case _ => true
  }

  def amendOverseasPensionsForm = Form (
    mapping(
      "amendedOverseasPensions" -> common.Validation.newText("pla.overseasPensions.errors.mandatoryError")
        .verifying("pla.overseasPensions.errors.mandatoryError", mandatoryCheck)
        .verifying("pla.overseasPensions.errors.mandatoryError", yesNoCheck),
      "amendedOverseasPensionsAmt" -> text
        .transform(stringToOptionalBigDecimal, optionalBigDecimalToString),
      "protectionType" -> text,
      "status" -> text
    )(AmendOverseasPensionsModel.apply)(AmendOverseasPensionsModel.unapply)
      .verifying("pla.overseasPensions.amount.errors.mandatoryError", verifyMandatory)
      .verifying("pla.overseasPensions.amount.errors.decimal", verifyDecimal)
      .verifying("pla.overseasPensions.amount.errors.negative", verifyPositive)
      .verifying("pla.overseasPensions.amount.errors.max", verifyMax)
  )
}
