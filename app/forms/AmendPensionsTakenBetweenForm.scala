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
import models.amendModels.AmendPensionsTakenBetweenModel
import play.api.data.Forms._
import play.api.data._
import common.Validation._

object AmendPensionsTakenBetweenForm extends CommonBinders {

  val verifyMandatory: AmendPensionsTakenBetweenModel => Boolean = {
    case AmendPensionsTakenBetweenModel("yes", value, _,_) => value.isDefined
    case _ => true
  }

  val verifyDecimal: AmendPensionsTakenBetweenModel => Boolean = {
    case AmendPensionsTakenBetweenModel("yes", Some(value), _,_) => isMaxTwoDecimalPlaces(value)
    case _ => true
  }

  val verifyPositive: AmendPensionsTakenBetweenModel => Boolean = {
    case AmendPensionsTakenBetweenModel("yes", Some(value), _,_) => isPositive(value)
    case _ => true
  }

  val verifyMax: AmendPensionsTakenBetweenModel => Boolean = {
    case AmendPensionsTakenBetweenModel("yes", Some(value), _,_) => isLessThanMax(value)
    case _ => true
  }

  def amendPensionsTakenBetweenForm = Form (
    mapping(
      "amendedPensionsTakenBetween" -> common.Validation.newText("pla.pensionsTakenBetween.errors.mandatoryError")
        .verifying("pla.pensionsTakenBetween.errors.mandatoryError", mandatoryCheck)
        .verifying("pla.pensionsTakenBetween.errors.mandatoryError", yesNoCheck),
      "amendedPensionsTakenBetweenAmt" -> text
        .transform(stringToOptionalBigDecimal, optionalBigDecimalToString),
      "protectionType" -> text,
      "status" -> text
    )(AmendPensionsTakenBetweenModel.apply)(AmendPensionsTakenBetweenModel.unapply)
      .verifying("pla.pensionsTakenBetween.amount.errors.mandatoryError", verifyMandatory)
      .verifying("pla.pensionsTakenBetween.amount.errors.decimal", verifyDecimal)
      .verifying("pla.pensionsTakenBetween.amount.errors.negative", verifyPositive)
      .verifying("pla.pensionsTakenBetween.amount.errors.max", verifyMax)
  )
}
