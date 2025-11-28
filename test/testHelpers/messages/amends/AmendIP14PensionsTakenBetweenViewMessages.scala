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

trait AmendIP14PensionsTakenBetweenViewMessages extends AmendIP16PensionsTakenBetweenViewMessages {
  val plaIP14PensionsTakenBetweenTitle = "Did any of these events happen between 6 April 2006 and 5 April 2014?"

  val plaIP14PensionsTakenBetweenParaTwo =
    "You can ask your pension scheme administrator to tell you the amount of lifetime allowance you've used for individual protection 2014."

  val plaIP14PensionsTakenBetweenStepOne =
    "Take the lifetime allowance at 5 April 2014 (£1.5m) and divide by the value of your lifetime allowance on the date the event happened."

  val plaIP14PensionsTakenBetweenLegendText =
    "Between 6 April 2006 and 5 April 2014 did you turn 75, take money from your pensions, or transfer to an overseas pension?"

}
