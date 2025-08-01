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

package testHelpers.ViewSpecHelpers.amends

trait OutcomeAmendedViewSpecMessages {
  val plaAmendTitle               = "Protection amended - Check your pension protections - GOV.UK"
  val plaAmendIP16Heading         = "You have amended your individual protection 2016"
  val plaAmendIP14Heading         = "You have amended your individual protection 2014"
  val plaAmendAllowanceSubHeading = "Your new protected amount is"
  val plaAmendProtectionDetails   = "Your protection details"

  val plaAmendName               = "Full Name"
  val plaAmendNino               = "National Insurance number"
  val plaAmendProtectionRef      = "Protection reference number"
  val plaAmendProtectionType     = "Protection type"
  val plaAmendPsaRef             = "Pension Scheme administrator check reference"
  val plaAmendFixedProtectionRef = "Fixed protection 2016 reference number"
  val plaAmendApplicationDate    = "Application date"
  val plaStatus                  = "Status"
  val plaDormant                 = "Dormant"
  val plaWithdrawn               = "Withdrawn"

  val plaAmendPrintGuidancePara =
    "Keep a copy of your protection details for your records. If you decide to take money from your pension, give these details to your pension provider."

  val plaAmendPrintNew        = "Review and print your protection details (opens in new tab)"
  val plaAmendIPChangeDetails = "Changing your protection details"

  val plaAmendChangeDetailsSubHeading     = "You must contact HMRC Pension Scheme services within 60 days if:"
  val plaAmendChangeDetailsSubHeadingLink = "contact HMRC Pension Scheme services"

  val plaAmendWhatToDoLink = "What to do if you lose your protection (opens in new tab)"

  val plaAmendIPPensionSharing =
    "If your pension gets shared in a divorce or civil partnership, you must contact HMRC Pension Scheme services within 60 days."

  val plaAmendIPPensionSharingLink = "contact HMRC Pension Scheme services"

  val plaResultSuccessViewDetails         = "You can view or change details of your protections at any time."
  val plaResultSuccessViewDetailsLinkText = "view or change details of your protections"
  val plaResultSuccessGiveFeedback        = "Give us feedback"

  val plaResultSuccessExitSurvey =
    "What did you think of this service? We use your feedback to make our services better."

  val plaResultSuccessExitSurveyLinkText = "What did you think of this service?"
}
