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

package testHelpers.messages

trait CommonErrorMessages {
  def errorReal(errorKey: String, protectionType: String) = s"pla.$errorKey.amount.errors.notReal.$protectionType"
  def errorReal(errorKey: String)                         = s"pla.$errorKey.amount.errors.notReal"

  def errorMissingAmount(errorKey: String, protectionType: String) =
    s"pla.$errorKey.amount.errors.mandatoryError.$protectionType"

  def errorMissingAmount(errorKey: String) = s"pla.$errorKey.amount.errors.mandatoryError"

  def errorNegative(errorKey: String)                         = s"pla.$errorKey.amount.errors.negative"
  def errorNegative(errorKey: String, protectionType: String) = s"pla.$errorKey.amount.errors.negative.$protectionType"

  def errorDecimal(errorKey: String)                         = s"pla.$errorKey.amount.errors.decimal"
  def errorDecimal(errorKey: String, protectionType: String) = s"pla.$errorKey.amount.errors.decimal.$protectionType"

  def errorMaximum(errorKey: String)                         = s"pla.$errorKey.amount.errors.max"
  def errorMaximum(errorKey: String, protectionType: String) = s"pla.$errorKey.amount.errors.max.$protectionType"

  def errorRequired(errorKey: String, modifier: String)       = s"$errorKey.error.required$modifier"
  def errorNotReal(errorKey: String, modifier: String)        = s"$errorKey.error.notReal$modifier"
  def errorInvalid(errorKey: String, modifier: String)        = s"$errorKey.error.invalid$modifier"
  def errorRange(errorKey: String, modifier: String)          = s"$errorKey.error.range$modifier"
  def errorQuestion(errorKey: String, protectionType: String) = s"pla.$errorKey.errors.mandatoryError.$protectionType"
  def errorQuestion(errorKey: String)                         = s"pla.$errorKey.errors.mandatoryError"

}
