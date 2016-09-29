/*
 * Copyright 2016 HM Revenue & Customs
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

package enums

object ApplicationStage extends Enumeration {

  type ApplicationStage = Value
  val PensionsTakenBefore = Value
  val PensionsTakenBetween = Value
  val OverseasPensions = Value
  val CurrentPensions = Value

  def fromString(applicationStage: String): Option[Value] = {
    applicationStage.toLowerCase match {
      case "pensionstakenbefore"  => Some(PensionsTakenBefore)
      case "pensionstakenbetween" => Some(PensionsTakenBetween)
      case "overseaspensions"     => Some(OverseasPensions)
      case "currentpensions"      => Some(CurrentPensions)
      case _                      => None
    }
  }
}
