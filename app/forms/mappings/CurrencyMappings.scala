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

package forms.mappings

import forms.formatters.CurrencyFormatter
import models.pla.AmendableProtectionType
import play.api.data.{Forms, Mapping}
import utils.Constants

trait CurrencyMappings {

  private[mappings] def currencyMapping(
      mandatoryMessageKey: String,
      invalidMessageKey: String,
      tooHighMessageKey: String,
      negativeMessageKey: String,
      tooManyDecimalPlacesMessageKey: String
  ): Mapping[Option[BigDecimal]] =
    Forms
      .of(
        CurrencyFormatter(
          mandatoryMessageKey,
          invalidMessageKey
        )
      )
      .verifying(tooManyDecimalPlacesMessageKey, isMaxTwoDecimalPlacesOption)
      .verifying(tooHighMessageKey, isBelowMaxOption)
      .verifying(negativeMessageKey, isPositiveOption)

  def currencyMappingFromPrefix(messageKeyPrefix: String): Mapping[Option[BigDecimal]] =
    currencyMapping(
      mandatoryMessageKey = s"$messageKeyPrefix.mandatoryError",
      invalidMessageKey = s"$messageKeyPrefix.notReal",
      tooHighMessageKey = s"$messageKeyPrefix.max",
      negativeMessageKey = s"$messageKeyPrefix.negative",
      tooManyDecimalPlacesMessageKey = s"$messageKeyPrefix.decimal"
    )

  def currencyMappingFromPrefixAndProtectionType(
      messageKeyPrefix: String,
      protectionTypeSuffix: AmendableProtectionType
  ): Mapping[Option[BigDecimal]] =
    currencyMapping(
      mandatoryMessageKey = s"$messageKeyPrefix.mandatoryError.$protectionTypeSuffix",
      invalidMessageKey = s"$messageKeyPrefix.notReal.$protectionTypeSuffix",
      tooHighMessageKey = s"$messageKeyPrefix.max.$protectionTypeSuffix",
      negativeMessageKey = s"$messageKeyPrefix.negative.$protectionTypeSuffix",
      tooManyDecimalPlacesMessageKey = s"$messageKeyPrefix.decimal.$protectionTypeSuffix"
    )

  private[mappings] val isMaxTwoDecimalPlaces: BigDecimal => Boolean = amount => amount.scale <= 2

  private[mappings] val isMaxTwoDecimalPlacesOption: Option[BigDecimal] => Boolean = amount =>
    amount.forall(isMaxTwoDecimalPlaces)

  private[mappings] val isPositive: BigDecimal => Boolean               = amount => amount >= 0
  private[mappings] val isPositiveOption: Option[BigDecimal] => Boolean = amount => amount.forall(isPositive)

  private[mappings] val isBelowMax: BigDecimal => Boolean               = amount => amount <= Constants.npsMaxCurrency
  private[mappings] val isBelowMaxOption: Option[BigDecimal] => Boolean = amount => amount.forall(isBelowMax)

}
