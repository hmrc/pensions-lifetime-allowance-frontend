/*
 * Copyright 2025 HM Revenue & Customs
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

trait PSODetailsErrorMessages extends CommonErrorMessages {

  val errorAmendPsoDetailsMissingAmount = "pla.psoDetails.amount.errors.mandatoryError"
  val errorPsoDay                       = "pso.error.required.day"
  val errorPsoMonth                     = "pso.error.required.month"
  val errorPsoYear                      = "pso.error.required.year"
  val errorPsoNotRealDay                = "pso.error.notReal.day"
  val errorPsoDateInFuture              = "pso.error.range.max"
  val errorPsoDateBeforeMin             = "pso.error.range.min"

}
