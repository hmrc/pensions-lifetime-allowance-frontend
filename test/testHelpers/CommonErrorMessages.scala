/*
 * Copyright 2019 HM Revenue & Customs
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

package testHelpers

trait CommonErrorMessages {
  val errorRequired = "error.required"
  val errorReal ="error.real"
  val errorMissingAmount = "pla.base.errors.errorQuestion"
  val errorNegative = "pla.base.errors.errorNegative"
  val errorDecimal = "pla.base.errors.errorDecimalPlaces"
  val errorMaximum = "pla.base.errors.errorMaximum"
  val errorMissingDay = "pla.base.errors.dayEmpty"
  val errorMissingMonth = "pla.base.errors.monthEmpty"
  val errorMissingYear = "pla.base.errors.yearEmpty"
  val errorDate = "pla.base.errors.invalidDate"
  val errorQuestion = "pla.base.errors.mandatoryError"
  val errorEmptyDay = "pla.withdraw.date-input.form.day-empty"
  val errorEmptyMonth = "pla.withdraw.date-input.form.month-empty"
  val errorEmptyYear = "pla.withdraw.date-input.form.year-empty"
  val errorLowDay = "pla.withdraw.date-input.form.day-too-low"
  val errorLowMonth = "pla.withdraw.date-input.form.month-too-low"
  val errorHighDay = "pla.withdraw.date-input.form.day-too-high"
  val errorHighMonth = "pla.withdraw.date-input.form.month-too-high"
  val errorFutureDate = "pla.withdraw.date-input.form.date-in-future"
}

trait PSODetailsMessages extends CommonErrorMessages {
  val errorAmendPsoDetailsMissingAmount = "pla.psoDetails.errorQuestion"
  val errorDateRange = "pla.IP16PsoDetails.errorDateOutOfRange"
}