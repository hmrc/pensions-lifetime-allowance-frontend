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

package testHelpers.ViewSpecHelpers.amends

trait OutcomeInactiveViewSpecMessages extends OutcomeActiveViewSpecMessages{
  val plaAmendResultCodeIP14AdditionalInfoOne        = "As you already have fixed protection 2014 in place, individual protection 2014 will only become active if you lose fixed protection 2014."
  val plaAmendResultCodeIP16AdditionalInfoOne        = "As you already have fixed protection 2016 in place, individual protection 2016 will only become active if you lose fixed protection 2016."
  val plaAmendResultCodeAdditionalInfoTwo        = "If this happens, you must tell HMRC Pension Schemes Services."
  val plaResultSuccessViewDetailInactive         = "You can view or change details of your protections in your personal tax account."
}