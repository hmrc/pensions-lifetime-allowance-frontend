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
import models.amendModels.AmendPensionsUsedBetweenModel
import play.api.data.Forms._
import play.api.data._
import common.Validation._

object AmendPensionsUsedBetweenForm extends CommonBinders {

  val verifyMandatory: AmendPensionsUsedBetweenModel => Boolean = {
    case AmendPensionsUsedBetweenModel(value, _,_) => value.isDefined
    case _ => true
  }

  val verifyDecimal: AmendPensionsUsedBetweenModel => Boolean = {
    case AmendPensionsUsedBetweenModel(Some(value), _,_) => isMaxTwoDecimalPlaces(value)
    case _ => true
  }

  val verifyPositive: AmendPensionsUsedBetweenModel => Boolean = {
    case AmendPensionsUsedBetweenModel(Some(value), _,_) => isPositive(value)
    case _ => true
  }

  val verifyMax: AmendPensionsUsedBetweenModel => Boolean = {
    case AmendPensionsUsedBetweenModel(Some(value), _,_) => isLessThanMax(value)
    case _ => true
  }

  def amendPensionsUsedBetweenForm = Form (
    mapping(
      "amendedPensionsUsedBetweenAmt" -> text
        .transform(stringToOptionalBigDecimal, optionalBigDecimalToString),
      "protectionType" -> text,
      "status" -> text
    )(AmendPensionsUsedBetweenModel.apply)(AmendPensionsUsedBetweenModel.unapply)
      .verifying("pla.pensionsUsedBetween.amount.errors.mandatoryError", verifyMandatory)
      .verifying("pla.pensionsUsedBetween.amount.errors.decimal", verifyDecimal)
      .verifying("pla.pensionsUsedBetween.amount.errors.negative", verifyPositive)
      .verifying("pla.pensionsUsedBetween.amount.errors.max", verifyMax)
  )
}