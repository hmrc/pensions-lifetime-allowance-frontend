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

import forms.mappings.{CurrencyMappings, YesNoMappings}
import models.amend.value.AmendOverseasPensionsModel
import models.pla.AmendableProtectionType
import play.api.data.Form
import play.api.data.Forms.mapping
import uk.gov.voa.play.form.ConditionalMappings.{isEqual, mandatoryIf}

object AmendOverseasPensionsForm extends CurrencyMappings with YesNoMappings {

  def amendOverseasPensionsForm(protectionType: AmendableProtectionType) = Form(
    mapping(
      "amendedOverseasPensions" -> yesNoMappingFromPrefixAndProtectionType(
        messageKeyPrefix = "pla.overseasPensions.errors",
        protectionTypeSuffix = protectionType
      ),
      "amendedOverseasPensionsAmt" -> mandatoryIf(
        isEqual("amendedOverseasPensions", "yes"),
        currencyMappingFromPrefixAndProtectionType(
          messageKeyPrefix = "pla.overseasPensions.amount.errors",
          protectionTypeSuffix = protectionType
        )
      )
        .transform[Option[BigDecimal]](
          f1 = doubleOption => doubleOption.flatten,
          f2 = singleOption => Some(singleOption)
        )
    )(AmendOverseasPensionsModel.apply)(AmendOverseasPensionsModel.unapply)
  )

}
