/*
 * Copyright 2016 HM Revenue & Customs
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

  val lifetimeAllowance = baseGovUrl + "tax-on-your-private-pension/lifetime-allowance"
  val plaMain           = baseGovUrl + "guidance/pension-schemes-protect-your-lifetime-allowance"
  val contactInfo       = baseGovUrl + "contact-hmrc"
  val helpDesk          = baseGovUrl + "government/organisations/hm-revenue-customs/contact/online-services-helpdesk"
  val callCharges       = baseGovUrl + "call-charges"

  val pensionValue      = baseGovUrl + "guidance/pension-schemes-value-your-pension-for-lifetime-allowance-protection"

  // Sidebar Read More link locations
  val ip16Info              = plaMain + "#individual-protection-2016"
  val ip14Info              = plaMain + "#individual-protection-2014"
  val pensionsTakenBefore   = pensionValue + "#income-taken-from-pensions-before-6-april-2006"
  val pensionsTakenBetween  = pensionValue + "#lifetime-allowance-used-between-6-april-2006-and-the-relevant-date"
  val overseasPension       = pensionValue + "#money-put-into-overseas-pensions-between-6-april-2006-and-the-relevant-date"
  val currentPensions       = pensionValue + "#pensions-youve-not-taken-yet"




}
