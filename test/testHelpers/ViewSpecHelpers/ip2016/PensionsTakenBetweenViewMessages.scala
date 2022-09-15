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

trait PensionsTakenBetweenViewMessages {
  val plaPensionsTakenBetweenTitle = "Did any of these events happen between 6 April 2006 and 5 April 2016?"
  val plaPensionsTakenBetweenTitleNew = "Did any of these events happen between 6 April 2006 and 5 April 2016? - Protect your lifetime allowance - GOV.UK"
  val plaPensionsTakenBetweenBulletOne = "you got money from your pensions"
  val plaPensionsTakenBetweenBulletTwo = "you transferred a pension to a scheme held overseas"
  val plaPensionsTakenBetweenBulletThree = "you turned 75 with pension savings which you hadn't yet taken"

  val plaPensionsTakenBetweenParaOne = "You'll have used some of your lifetime allowance if any of these events happened."
  val plaPensionsTakenBetweenParaTwo = "You can ask your pension scheme administrator to tell you the amount of lifetime allowance you've used for individual protection 2016."
  val plaPensionsTakenBetweenParaThree = "Read technical guidance on working out how much lifetime allowance you've used (opens in a new window)"
  val plaPensionsTakenBetweenParaThreeNew = "Read technical guidance on working out how much lifetime allowance you've used (opens in new tab)."

  val plaPensionsTakenBetweenQuestionTwo = "How much lifetime allowance have you used?"
  val plaPensionsTakenBetweenHelp = "How do I work out the value?"

  val plaPensionsTakenBetweenStepOne = "Take the lifetime allowance at 5 April 2016 (£1.25m) and divide by the value of your lifetime allowance on the date the event happened."
  val plaPensionsTakenBetweenStepTwo = "Multiply the value of your lifetime allowance on the date the event happened by the percentage of lifetime allowance used."
  val plaPensionsTakenBetweenStepThree = "Take the result from step 1 and multiply by the result of step 2."
  val plaPensionsTakenBetweenStepFour = "Repeat for each event and total the amounts together."

  val plaPensionsTakenBetweenHelpLinkText = "working out how much lifetime allowance you've used (opens in a new window)"
  val plaPensionsTakenBetweenHelpLinkTextNew = "working out how much lifetime allowance you've used (opens in new tab)."
  val plaPensionsTakenBetweenHelpLinkLocation = "https://www.gov.uk/guidance/pension-schemes-value-your-pension-for-lifetime-allowance-protection#lifetime-allowance-used-between-6-april-2006-and-the-relevant-date"
  val plaPensionsTakenBetweenLegendText = "Between 6 April 2006 and 5 April 2016 did you turn 75, take money from your pensions, or transfer to an overseas pension?"
}
