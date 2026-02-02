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

import forms.formatters.StringFormatter
import models.pla.AmendableProtectionType
import play.api.data.{Forms, Mapping}

trait YesNoMappings {

  private[mappings] def yesNoMapping(
      mandatoryMessageKey: String
  ): Mapping[String] = Forms
    .of(
      StringFormatter(
        mandatoryMessageKey
      )
    )
    .verifying(mandatoryMessageKey, isPresent)
    .verifying(mandatoryMessageKey, isYesOrNo)

  def yesNoMappingFromPrefixAndProtectionType(
      messageKeyPrefix: String,
      protectionTypeSuffix: AmendableProtectionType
  ) =
    yesNoMapping(
      mandatoryMessageKey = s"$messageKeyPrefix.mandatoryError.$protectionTypeSuffix"
    )

  private[mappings] val isPresent: String => Boolean = value => value.trim.nonEmpty

  private[mappings] val isYesOrNo: String => Boolean = value =>
    value.trim match {
      case ""    => true
      case "yes" => true
      case "no"  => true
      case _     => false
    }

}
