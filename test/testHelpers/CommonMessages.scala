/*
 * Copyright 2017 HM Revenue & Customs
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

trait CommonMessages {
  val errorRequired = "error.required"
  val errorReal ="error.real"
  val errorMissingAmount = "Enter an amount in the correct format eg £500000"
  val errorNegative = "Enter an amount that's £0 or more"
  val errorDecimal = "The amount you've entered has too many decimal places"
  val errorMaximum = "Enter an amount less than £99,999,999,999,999.98"
  val errorMissingDay = "Enter a day"
  val errorMissingMonth = "Enter a month"
  val errorMissingYear = "Enter a year"
  val errorDate = "Enter a date in the correct format eg 14 6 2016"
  val errorQuestion = "Please answer this question"
}

trait PSODetailsMessages extends CommonMessages {
  val errorAmendPsoDetailsMissingAmount = "Enter a number without commas, for example 10000.00"
  val errorDateRange = "Enter a date between 5 April 2016 and today"
}