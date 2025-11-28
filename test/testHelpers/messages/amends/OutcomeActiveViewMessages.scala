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

package testHelpers.messages.amends

trait OutcomeActiveViewMessages {
  val plaResultSuccessOutcomeActiveTitle = "Protection amended"

  val plaResultSuccessIP16Heading         = "You've amended individual protection 2016"
  val plaResultSuccessIP14Heading         = "You've amended individual protection 2014"
  val plaResultSuccessallowanceSubheading = "Your protected amount is"
  val plaResultSuccessProtectionDetails   = "Your protection details"

  val plaResultSuccessPrintNew        = "Review and print your protection details (opens in new tab)"
  val plaResultSuccessIPChangeDetails = "Changing your protection details"

  val plaResultSuccessIPPensionSharing =
    "If your pension gets shared in a divorce or civil partnership split, contact HMRC Pension Schemes Services within 60 days."

  val plaResultSuccessIPPensionSharingLinkText = "HMRC Pension Schemes Services"

  val plaResultSuccessIPPensionsSharingLink =
    "https://www.gov.uk/government/organisations/hm-revenue-customs/contact/pension-scheme-enquiries"

  val plaResultSuccessViewDetailsLinkText = "view or change details of your protections"
  val plaResultSuccessGiveFeedback        = "Give us feedback"

  val plaResultSuccessExitSurvey =
    "What did you think of this service? We use your feedback to make our services better."

  val plaResultSuccessExitSurveyLinkText = "What did you think of this service?"
}
