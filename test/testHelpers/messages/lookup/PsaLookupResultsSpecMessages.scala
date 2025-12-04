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

package testHelpers.messages.lookup

trait PsaLookupResultsSpecMessages {

  val titleText =
    "Results of lifetime allowance protection check - Check your pension protections - GOV.UK"

  val headingText                                = "Results of lifetime allowance protection check"
  val schemeAdministratorRowTitleText            = "Scheme Administrator Reference:"
  val statusRowTitleText                         = "Status:"
  val statusRowValidText                         = "This protection is valid"
  val statusRowInvalidText                       = "This protection is no longer valid"
  val checkedOnTitleText                         = "Checked on:"
  val timestampText                              = "timestamp"
  val protectionNumberTitleText                  = "Protection Notification Number:"
  val protectionTypeTitleText                    = "Protection Type:"
  val protectionTypeFixedProtection2016Text      = "Fixed protection 2016"
  val protectionTypeIndividualProtection2014Text = "Individual protection 2014"
  val protectionTypeIndividualProtection2016Text = "Individual protection 2016"
  val protectionTypePrimaryText                  = "Primary protection"
  val protectionTypeEnhancedText                 = "Enhanced protection"
  val protectionTypeFixedProtection2012Text      = "Fixed protection 2012"
  val protectionTypeFixedProtection2014Text      = "Fixed protection 2014"
  val protectedAmountTitleText                   = "Protected Amount:"
  val pdfLinkText                                = "Review and print these results"
  val startAgainLinkText                         = "Check another certificate"
}
