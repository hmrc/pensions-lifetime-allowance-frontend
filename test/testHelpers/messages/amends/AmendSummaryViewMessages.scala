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

trait AmendSummaryViewMessages {
  val plaAmendsSummaryTitle = "Protection summary - Check your pension protections and enhancements - GOV.UK"
  val plaAmendsHeaderOne       = "Check your answers and submit the changes"

  val plaAmendsAdditionalPsoAmount         = "£123456"
  val plaAmendsAdditionalPsoDate           = "2 March 2017"
  val plaAmendsCancelText                  = "Alternatively, you can cancel the changes and go back."
  val plaAmendsCancelLinkText              = "cancel the changes"
  val plaAmendsCancelLinkLocation          = "/check-your-pension-protections-and-enhancements/existing-protections"
  val plaAmendsAddAPensionSharingOrderText = "Add a pension sharing order"

  val plaAmendsDeclaration =
    "The information that I have provided is true and complete to the best of my knowledge and belief."

  val plaAmendsSubmitButton = "Submit your changes"

  val plaSummaryPensionsHeading = "Value of pensions"

  val plaSummaryQuestionsPensionsTakenBefore = "Did you get an income from any of your pensions before 6 April 2006?"

  val plaSummaryQuestionsOverseasPensions = "Have you put money into a pension scheme held overseas?"

  val plaSummaryQuestionsOverseasPensionsAmt = "How much did you contribute?"

  val plaSummaryQuestionsTotalPensionsAmt = "Total value of pensions"
  val plaSummaryPsosHeading               = "Value of pension sharing orders"

  val plaSummaryQuestionsPsoDetails = "Date and amount of pension sharing order"

}
