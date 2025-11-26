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

package testHelpers.messages.withdraw

trait WithdrawConfirmSpecMessages {

  val plaWithdrawWhatHappensInfoHeading = "What happens if you withdraw"
  val plaWithdrawImplicationsSubmit     = "Accept and continue"

  def plaWithdrawProtectionWhatHappensInfo1(withdrawDate: String) =
    s"Without any protection your pension will not be protected from $withdrawDate and will be taxed when you start using it."

  val plaWithdrawProtectionWhatHappensInfo2 =
    "You may be able to apply for a new protection in the future, but it might cover less than the amount you are covered for now."

  def plaWithdrawProtectionWhatHappensInfo3(withdrawDate: String) =
    s"If you have a dormant protection it will protect your pension from its application date. This might mean that your pension is unprotected between $withdrawDate and the application date of the dormant protection."

  def plaWithdrawProtectionWhatHappensInfo4(withdrawDate: String) =
    s"You might be asked to provide evidence that your protection was valid and eligible to be withdrawn on $withdrawDate."

  val plaWithdrawProtectionWhatHappensInfo5 = "You will not be able to cancel or amend this request once you submit."

}
