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

package common

object Links {

  final val baseGovUrl = "https://www.gov.uk/"

  val plaMain: String     = baseGovUrl + "guidance/pension-schemes-protect-your-lifetime-allowance"
  val contactInfo: String = baseGovUrl + "contact-hmrc"
  val callCharges: String = baseGovUrl + "call-charges"

  val contactHMRC: String =
    baseGovUrl + "government/organisations/hm-revenue-customs/contact/income-tax-enquiries-for-individuals-pensioners-and-employees"

  val pensionValue: String =
    baseGovUrl + "guidance/pension-schemes-value-your-pension-for-lifetime-allowance-protection"

  // Sidebar Read More link locations
  val pensionsWorthBefore: String = pensionValue + "#income-taken-from-pensions-before-6-april-2006"
  val pensionsUsedBetween: String = pensionValue + "#lifetime-allowance-used-between-6-april-2006-and-the-relevant-date"
  val currentPensions: String     = pensionValue + "#pensions-youve-not-taken-yet"
}
