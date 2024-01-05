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
import models.amendModels.AmendPensionsWorthBeforeModel
import common.Validation._
import play.api.data.Form
import play.api.data.Forms._

object AmendPensionsWorthBeforeForm extends CommonBinders {

  val verifyMandatory: AmendPensionsWorthBeforeModel => Boolean = {
    case AmendPensionsWorthBeforeModel(value, _, _) => value.isDefined
    case _ => true
  }

  val verifyDecimal: AmendPensionsWorthBeforeModel => Boolean = {
    case AmendPensionsWorthBeforeModel(Some(value), _, _) => isMaxTwoDecimalPlaces(value)
    case _ => true
  }

  val verifyPositive: AmendPensionsWorthBeforeModel => Boolean = {
    case AmendPensionsWorthBeforeModel(Some(value), _, _) => isPositive(value)
    case _ => true
  }

  val verifyMax: AmendPensionsWorthBeforeModel => Boolean = {
    case AmendPensionsWorthBeforeModel(Some(value), _, _) => isLessThanMax(value)
    case _ => true
  }

  def amendPensionsWorthBeforeForm: Form[AmendPensionsWorthBeforeModel] = Form (
    mapping(
      "amendedPensionsTakenBeforeAmt" -> text
        .transform(stringToOptionalBigDecimal, optionalBigDecimalToString),
      "protectionType" -> text,
      "status" -> text
    )(AmendPensionsWorthBeforeModel.apply)(AmendPensionsWorthBeforeModel.unapply)
      .verifying("pla.pensionsWorthBefore.amount.errors.mandatoryError", verifyMandatory)
      .verifying("pla.pensionsWorthBefore.amount.errors.decimal", verifyDecimal)
      .verifying("pla.pensionsWorthBefore.amount.errors.negative", verifyPositive)
      .verifying("pla.pensionsWorthBefore.amount.errors.max", verifyMax)
  )

}
