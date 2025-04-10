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

package testHelpers.ViewSpecHelpers.amends

import testHelpers.ViewSpecHelpers.ip2016.SummaryViewMessages

trait AmendSummaryViewSpecMessages extends SummaryViewMessages {
  val plaAmendsSummaryTitle       = "Protection summary - Check your pension protections - GOV.UK"
  val plaAmendsHeaderOne          = "Check your answers and submit the changes"
  val plaAmendsSummaryPageHeading = "Your individual protection 2016 details"

  val plaAmendsVisuallyHiddenTextPensionsTakenBeforeYesNo =
    "Change whether you received an income from any of your pensions before 6 April 2006"

  val plaAmendsVisuallyHiddenTextPensionsTakenBeforeAmt =
    "Change the value of the pensions you took before 6 April 2006"

  val plaAmendsVisuallyHiddenTextPensionsTakenBetweenYesNo =
    "Change whether you got any money from your pensions before 5 April 2016?"

  val plaAmendsVisuallyHiddenTextPensionsTakenBetweenAmt = "Change the amount of lifetime allowance you used"

  val plaAmendsVisuallyHiddenTextOverseasPensionsYesNo =
    "Change whether you put money into a pension scheme held overseas"

  val plaAmendsVisuallyHiddenTextOverseasPensionsAmt = "Change how much you contributed to your overseas pension"
  val plaAmendsVisuallyHiddenTextCurrentPensionsAmt  = "Change the amount that your pensions were worth on 5 April 2016"
  val plaAmendsVisuallyHiddenTextChangeText          = "Change the details of your pension sharing order"
  val plaAmendsVisuallyHiddenTextRemoveText = "Remove your response to the amount or date of your pension sharing order"
  val plaAmendsAdditionalPsoAmount          = "£123456"
  val plaAmendsAdditionalPsoDate            = "2 March 2017"
  val plaAmendsCancelText                   = "Alternatively, you can cancel the changes and go back."
  val plaAmendsCancelLinkText               = "cancel the changes"
  val plaAmendsCancelLinkLocation           = "/check-your-pension-protections/existing-protections"
  val plaAmendsWithdrawProtectionText       = "Withdraw your open protection"
  val plaAmendsAddAPensionSharingOrderText  = "Add a pension sharing order"
  val plaAmendsWithdrawProtectionLinkLocation = "/check-your-pension-protections/withdraw-protection/implications"

  val plaAmendsDeclaration =
    "The information that I have provided is true and complete to the best of my knowledge and belief."

  val plaAmendsSubmitButton = "Submit your changes"
}
