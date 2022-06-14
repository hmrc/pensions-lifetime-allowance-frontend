/*
 * Copyright 2022 HM Revenue & Customs
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

package testHelpers.ViewSpecHelpers.ip2016

trait SummaryViewMessages{
  val plaSummaryTitle                                   = "Submit your application"
  val plaSummaryPageHeading                             = "Check and submit your protection details"
  val plaSummaryErrorSummaryLabel                       = "There's a problem with your application"
  val plaSummaryUnderThresholdErrorIP2016               = "Your total pension savings were less than £1,000,001 on 5 April 2016. You can only apply for individual protection 2016 if your pension savings were £1,000,001 or more."
  val plaSummaryPensionsHeading                         = "Value of pensions"
  val plaSummaryChange                                  = "Change"
  val plaSummaryQuestionsPensionsTaken                  = "Before 5 April 2016, did you get money from your pensions, transfer a pension overseas, or turn 75 with money still in a pension?"
  val plaSummaryHiddenLinkTextPensionsTaken             = "Change your response to the question 'have you taken any of your pensions'"
  val plaSummaryQuestionsPensionsTakenBefore            = "Did you get an income from any of your pensions before 6 April 2006?"
  val plaSummaryHiddenLinkTextPensionsTakenBefore       = "Change your response to the question 'Did you get an income from any of your pensions before 6 April 2006?'"
  val plaSummaryQuestionsPensionsTakenBeforeAmt         = "What's the value of the pensions you took before 6 April 2006?"
  val plaSummaryHiddenLinkTextPensionsTakenBeforeAmt    = "Change your response to the value of the pensions you took before 6 April 2006"
  val plaSummaryQuestionsPensionsTakenBetween           = "Between 6 April 2006 and 5 April 2016, did you get money from your pensions, transfer a pension overseas, or turn 75 with money still in a pension?"
  val plaSummaryHiddenLinkTextPensionsTakenBetween      = "Change your response to the question 'did you get money from your pensions between 6 April 2006 and 5 April 2016'"
  val plaSummaryQuestionsPensionsTakenBetweenAmt        = "How much lifetime allowance have you used?"
  val plaSummaryHiddenLinkTextPensionsTakenBetweenAmt   = "Change your response to the amount of lifetime allowance used"
  val plaSummaryQuestionsOverseasPensions               = "Have you put money into a pension scheme held overseas?"
  val plaSummaryHiddenLinkTextOverseasPensions          = "Change your response to the question 'Have you put money into a pension scheme held overseas?'"
  val plaSummaryQuestionsOverseasPensionsAmt            = "How much did you contribute?"
  val plaSummaryHiddenLinkTextOverseasPensionsAmt       = "Change your response to the amount of overseas pensions you put in between 6 April 2006 and 5 April 2016"
  val plaSummaryQuestionsCurrentPensionsAmt             = "What were your UK pensions worth on 5 April 2016?"
  val plaSummaryHiddenLinkTextCurrentPensionsAmt        = "Change your response to the value of your UK pension savings on 5 April 2016"
  val plaSummaryQuestionsTotalPensionsAmt               = "Total value of pensions"
  val plaSummaryPsosHeading                             = "Value of pension sharing orders"
  val plaSummaryQuestionsPensionDebits                  = "Have any of your pensions been shared in a divorce since 5 April 2016?"
  val plaSummaryHiddenLinkTextPensionDebits             = "Change your response to the question 'Have any of your pensions been shared in a divorce since 5 April 2016?'"

  val plaSummaryQuestionsPsoDetails                     = "Date and amount of pension sharing order"
  val plaSummaryHiddenLinkTextPsoDetails                = "Change the details of your pension sharing order"
  val plaSummaryGetIP16                                 = "Get individual protection 2016"
  val plaSummaryMustAgree                               = "To get this protection you must agree to these declarations."
  val plaSummaryConfirmation                            = "I declare:"
  val plaSummaryConfirm1                                = "On 5 April 2016 I was a member of an HMRC-registered pension scheme or a relieved member of a relieved non-UK pension scheme."
  val plaSummaryConfirm2                                = "On 6 April 2016 I didn't hold either of these protections:"
  val plaSummaryConfirmBullet1                          = "primary protection"
  val plaSummaryConfirmBullet2                          = "individual protection 2014"
  val plaSummaryHelp                                    = "Help with these conditions"
  val plaSummaryHiddenParaOne                           = "Speak to your pension scheme administrator to get professional help understanding these conditions."
  val plaSummaryHiddenParaTwo                           = "HMRC's Pensions Tax Manual has technical guidance about individual protection 2016 (opens in a new window)"
  val plaSummaryHiddenParaLinkText                      = "technical guidance about individual protection 2016 (opens in a new window)"
  val plaSummaryConfirmation2                           = "I declare that the information I have provided is true and complete to the best of my knowledge and belief."
  val plaSummaryDeclaration                             = "By confirming, you will automatically submit your application for individual protection 2016."

  val plaHelpLinkLocation = "https://www.gov.uk/hmrc-internal-manuals/pensions-tax-manual/ptm094210"
}
