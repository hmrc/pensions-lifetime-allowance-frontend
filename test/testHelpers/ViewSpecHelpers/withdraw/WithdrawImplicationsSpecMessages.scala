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

package testHelpers.ViewSpecHelpers.withdraw

trait WithdrawImplicationsSpecMessages {
  def plaWithdrawProtectionTitle(protectionType: String)        = s"Withdraw from $protectionType - Protect your lifetime allowance - GOV.UK"
  def plaWithdrawProtectionHeading(protectionType: String)      = s"Withdraw from $protectionType"
  val plaWithdrawProtectionIP2014label                          = "individual protection 2014"
  val plaWithdrawProtectionIP2016label                          = "individual protection 2016"
  def plaWithdrawImplicationInfo(protectionType: String)        = s"Once you withdraw, you will no longer have $protectionType. This might mean you have to pay a higher rate of tax on your pension."
  def plaWithdrawProtectionIfInfo(protectionType: String)       = s"Use this service to withdraw from your $protectionType if:"
  def plaWithdrawProtectionIfInfo1(protectionType: String)      = s"you no longer want to have $protectionType"
  def plaWithdrawProtectionIfInfo2(protectionType: String)      = s"you are no longer eligible to have $protectionType"
  val plaWithdrawProtectionContinueTitle                        = "Continue"
}
