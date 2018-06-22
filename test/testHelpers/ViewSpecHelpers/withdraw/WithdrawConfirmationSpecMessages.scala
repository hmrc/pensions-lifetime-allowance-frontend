/*
 * Copyright 2018 HM Revenue & Customs
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

package testHelpers.ViewSpecHelpers.withdraw

trait WithdrawConfirmationSpecMessages {

  def plaWithdrawConfirmationMessage(protectionType: String)             = s"Your $protectionType has been removed"
  val plaWithdrawProtectionIP2014label                                   = "individual protection 2014"
  val plaWithdrawConfirmationCheckDetails                                = "You can check the details of your pension in your personal tax account."
  val plaWithdrawConfirmationContactYouIfNeeded                          = "We may need to contact you to confirm the information you’ve given us."
  val plaWithdrawConfirmFeedbackHeading                                  = "Give us feedback"
  val plaWithdrawConfirmFeedbackText                                     = "What did you think of this service? We use your feedback to make our services better."
  val plaWithdrawConfirmFeedbackLink                                     = "What did you think of this service?"
  val plaWithdrawConfirmFeedbackUrl                                      = "/protect-your-lifetime-allowance/exit"
  val plaWithdrawConfirmationOtherProtections                            = "View details about other protections and how to apply on GOV.UK."
  val plaWithdrawConfirmationOtherProtectionsLink                        = "details about other protections and how to apply"
  val plaWithdrawConfirmationOtherProtectionsUrl                         = "https://www.gov.uk/guidance/pension-schemes-protect-your-lifetime-allowance"

}
