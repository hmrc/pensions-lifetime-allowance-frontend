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

trait AmendIP14CurrentPensionsViewMessages extends AmendIP16CurrentPensionsViewMessages {
  val plaIp14CurrentPensionsTitle = "What were your UK pensions worth on 5 April 2014?"

  val plaIp14CurrentPensionsTitleNew =
    "What were your UK pensions worth on 5 April 2014? - Check your pension protections and enhancements - GOV.UK"

  val plaIp14CurrentPensionsHiddenTextPara = "Use figures that were correct at 5 April 2014."
}
