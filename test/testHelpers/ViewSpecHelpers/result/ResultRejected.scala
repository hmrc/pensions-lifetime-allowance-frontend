/*
 * Copyright 2021 HM Revenue & Customs
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

package testHelpers.ViewSpecHelpers.result

trait ResultRejected {

  val plaResultRejectionTitle                 = "Unsuccessful"
  val plaResultRejectionPageHeading           = "Your application was unsuccessful"
  val plaResultRejectionViewDetails           = "You can view or change details of your protections at any time."
  val plaResultRejectionViewDetailsLinkText   = "view or change details of your protections"

  val plaResultSuccessExitSurveyLinkText      = "What did you think of this service?"
  val plaResultSuccessExitSurveyLink          = "/protect-your-lifetime-allowance/sign-out"
  val plaResultSuccessTitle                   = "Success"
  val plaResultSuccessAllowanceSubHeading     = "Your protected lifetime allowance is"
  val plaResultSuccessProtectionDetails       = "Your protection details"
  val plaResultSuccessDetailsContent          = "When you decide to take money from your pension, give these details to your pension provider:"
  val plaResultSuccessProtectionRef           = "protection notification number"
  val plaResultSuccessPsaRef                  = "scheme administrator reference"
  val plaResultSuccessApplicationDate         = "application date"
  val plaResultSuccessPrint                   = "Print and save your details"
  val plaResultSuccessIPChangeDetails         = "Changing your protection details"
  val plaResultSuccessIPPensionSharing        = "If your pension gets shared in a divorce or civil partnership split, contact <a href='https://www.gov.uk/government/organisations/hm-revenue-customs/contact/pension-scheme-enquiries'>HMRC Pension Schemes Services</a> within 60 days."
  val plaResultSuccessFPAddToPension          = "If you or your employer adds to your pension, contact <a href='https://www.gov.uk/government/organisations/hm-revenue-customs/contact/pension-scheme-enquiries'>HMRC Pension Schemes Services</a> within 90 days."
  val plaResultSuccessViewDetails             = "You can view or change details of your protections at any time."
  val plaResultSuccessViewDetailsLinkText     = "view or change details of your protections"
  val plaResultSuccessGiveFeedback            = "Give us feedback"
  val plaResultSuccessExitSurvey              = "We use your feedback to make our services better"
  val plaResultSuccessYourName                = "your full name"
  val plaResultSuccessYourNino                = "your National Insurance number"

}
