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

import common.Transformers.{bigDecimalToString, stringToBigDecimal}
import uk.gov.voa.play.form.ConditionalMappings.{isEqual, mandatoryIf}
import models.amendModels.AmendOverseasPensionsModel
import play.api.data.Form
import play.api.data.Forms._
import common.Validation._
import models.pla.AmendableProtectionType
import utils.Constants.npsMaxCurrency

object AmendOverseasPensionsForm extends CommonBinders {

  def amendOverseasPensionsForm(protectionType: AmendableProtectionType) = Form(
    mapping(
      "amendedOverseasPensions" -> common.Validation
        .newText(s"pla.overseasPensions.errors.mandatoryError.$protectionType")
        .verifying(s"pla.overseasPensions.errors.mandatoryError.$protectionType", mandatoryCheck)
        .verifying(s"pla.overseasPensions.errors.mandatoryError.$protectionType", yesNoCheck),
      "amendedOverseasPensionsAmt" -> mandatoryIf(
        isEqual("amendedOverseasPensions", "yes"),
        common.Validation
          .newText(s"pla.overseasPensions.amount.errors.mandatoryError.$protectionType")
          .verifying(s"pla.overseasPensions.amount.errors.notReal.$protectionType", bigDecimalCheck)
          .verifying("pla.psoDetails.errorQuestion", commaCheck)
          .transform(stringToBigDecimal, bigDecimalToString)
          .verifying(
            stopOnFirstFail(
              negativeConstraint(s"pla.overseasPensions.amount.errors.negative.$protectionType"),
              decimalPlaceConstraint(s"pla.overseasPensions.amount.errors.decimal.$protectionType"),
              maxMoneyCheck(npsMaxCurrency, s"pla.overseasPensions.amount.errors.max.$protectionType")
            )
          )
      )
    )(AmendOverseasPensionsModel.apply)(AmendOverseasPensionsModel.unapply)
  )

}
